package com.example.capstone.view.fragment.bottomsheet

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.capstone.R
import com.example.capstone.data.api.ApiConfig
import com.example.capstone.data.api.response.PredictionResponse
import com.example.capstone.data.pref.UserPreference
import com.example.capstone.data.pref.dataStore
import com.example.capstone.databinding.BottomSheetCameraBinding
import com.example.capstone.view.activity.result.ResultActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale

class BottomSheetCamera : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetCameraBinding
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var outputDirectory: File
    private var isFlashEnabled = false
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    // Progress and error dialogs
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        outputDirectory = getOutputDirectory()
        setupClickListeners()
        startCamera()
    }

    private fun setupClickListeners() {
        binding.apply {
            captureButton.setOnClickListener { takePhoto() }
            flashButton.setOnClickListener { toggleFlash() }
        }
    }

    private fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled
        camera?.cameraControl?.enableTorch(isFlashEnabled)
        binding.flashButton.setImageResource(
            if (isFlashEnabled) R.drawable.ic_flash
            else R.drawable.ic_flash_slash
        )
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireContext().filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                preview?.setSurfaceProvider(binding.previewView.surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
                showErrorDialog("Camera initialization failed: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            showErrorDialog("Camera not ready. Please try again.")
            return
        }

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Show loading state with ProgressDialog
        showLoading()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (!photoFile.exists() || photoFile.length() == 0L) {
                        dismissLoading()
                        showErrorDialog("Failed to save image. File is empty.")
                        return
                    }

                    val userPreference = UserPreference.getInstance(requireContext().dataStore)
                    lifecycleScope.launch {
                        try {
                            val token = userPreference.getSession().first().token
                            sendImageToApi(photoFile, token)
                        } catch (e: Exception) {
                            dismissLoading()
                            showErrorDialog("Session error: ${e.localizedMessage}")
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    dismissLoading()
                    showErrorDialog("Image capture failed: ${exception.localizedMessage}")
                }
            }
        )
    }

    private fun sendImageToApi(imageFile: File, token: String) {
        var compressedFile = compressImage(imageFile)

        // Additional compression if file is still too large
        val MAX_FILE_SIZE = 1024 * 1024 // 1 MB
        if (compressedFile.length() > MAX_FILE_SIZE) {
            Log.d(TAG, "File still too large, applying additional compression")
            compressedFile = compressImageWithQuality(compressedFile)
        }

        // Create MultipartBody.Part from the file
        val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("image", compressedFile.name, requestImageFile)

        val apiService = ApiConfig.getApiService(token)

        // Make API call
        apiService.predictImage(multipartBody).enqueue(object : Callback<PredictionResponse> {
            override fun onResponse(
                call: Call<PredictionResponse>,
                response: Response<PredictionResponse>
            ) {
                dismissLoading()
                when {
                    response.isSuccessful -> {
                        response.body()?.let { predictionResponse ->
                            val predictionData = predictionResponse.data

                            // Create intent to ResultActivity
                            val intent = Intent(requireContext(), ResultActivity::class.java).apply {
                                putExtra("CATEGORY", predictionData.prediction)
                                putExtra("DESCRIPTION", predictionData.description)
                                putExtra("TIPS", predictionData.tips)
                                putExtra("ACCURACY", predictionData.accuracy)
                                putExtra("IMAGE", imageFile)
                            }
                            startActivity(intent)
                            dismiss()
                        } ?: run {
                            showErrorDialog("No prediction data received")
                        }
                    }
                    response.code() == 401 -> showErrorDialog("Unauthorized access. Please log in again.")
                    response.code() == 404 -> showErrorDialog("Prediction service not found")
                    response.code() == 500 -> showErrorDialog("Server error. Please try again later.")
                    else -> showErrorDialog("Unexpected error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                dismissLoading()
                val errorMessage = when (t) {
                    is UnknownHostException -> "No internet connection"
                    else -> "Network error: ${t.localizedMessage}"
                }
                Log.e(TAG, "API call failed", t)
                showErrorDialog(errorMessage)
            }
        })
    }

    private fun showLoading() {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Processing Image...")
            setCancelable(false)
            show()
        }
    }

    private fun dismissLoading() {
        progressDialog?.dismiss()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun compressImageWithQuality(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val outputStream = ByteArrayOutputStream()

        // Progressively reduce quality
        val qualities = listOf(30, 20, 10)
        for (quality in qualities) {
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            if (outputStream.size() <= 1024 * 1024) break
        }

        val compressedFile = File(requireContext().cacheDir, "ultra_compressed_image.jpg")
        val fileOutputStream = FileOutputStream(compressedFile)
        fileOutputStream.write(outputStream.toByteArray())
        fileOutputStream.close()

        return compressedFile
    }

    private fun compressImage(file: File): File {
        // Decode bitmap with options to reduce resolution
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.path, options)

        // Use a lower resolution target
        options.inSampleSize = calculateInSampleSize(options, 512, 512)
        options.inJustDecodeBounds = false

        val scaledBitmap = BitmapFactory.decodeFile(file.path, options)

        val outputStream = ByteArrayOutputStream()
        // Reduce compression quality to further reduce file size
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

        val compressedFile = File(requireContext().cacheDir, "compressed_image.jpg")
        val fileOutputStream = FileOutputStream(compressedFile)
        fileOutputStream.write(outputStream.toByteArray())
        fileOutputStream.close()

        // Log the file sizes for debugging
        Log.d(TAG, "Original file size: ${file.length() / 1024} KB")
        Log.d(TAG, "Compressed file size: ${compressedFile.length() / 1024} KB")

        return compressedFile
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    companion object {
        private const val TAG = "BottomSheetCamera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_KEY_CAMERA = "camera_request_key"
        const val KEY_IMAGE_URI = "image_uri"
    }
}