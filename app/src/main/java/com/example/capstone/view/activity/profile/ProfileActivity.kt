package com.example.capstone.view.activity.profile

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone.data.api.ApiConfig
import com.example.capstone.data.pref.UserPreference
import com.example.capstone.data.pref.dataStore
import com.example.capstone.databinding.ActivityProfileBinding
import com.example.capstone.di.Injection
import com.example.capstone.di.ViewModelFactory
import com.example.capstone.view.fragment.bottomsheet.BottomSheetCamera
import com.example.capstone.view.fragment.bottomsheet.BottomSheetCamera.Companion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var apiToken: String? = null
    private var selectedImageUri: Uri? = null

    private val viewModel: ProfileViewModel by viewModels {
        val repository = Injection.provideRepository(this)
        ViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch token secara asinkron
        lifecycleScope.launch {
            val userPreference = UserPreference.getInstance(dataStore)
            val session = userPreference.getSession().first()
            apiToken = session.token

            // Setelah token didapatkan, fetch profile
            apiToken?.let { token ->
                viewModel.fetchUserProfile(token)
            }
        }

        // Observer untuk username
        viewModel.username.observe(this) { username ->
            binding.EtProfilUsername.setText(username ?: "")
        }

        // Observer untuk profile image
        viewModel.profileImageUrl.observe(this) { profileImageUrl ->
            Glide.with(this)
                .load(profileImageUrl)
                .into(binding.imageView)
        }

        // Setup listeners
        binding.imageView.setOnClickListener { selectImageFromGallery() }
        binding.customTextViewRegular3.setOnClickListener { selectImageFromGallery() }
        binding.signupButton.setOnClickListener { updateUserProfile() }
    }

    private fun updateUserProfile() {

        // Pastikan token tersedia sebelum melakukan update
        apiToken?.let { token ->
            val username = binding.EtProfilUsername.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val password = binding.EtProfilPassword.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            var profileImagePart: MultipartBody.Part? = null
            selectedImageUri?.let { uri ->
                val filePath = getRealPathFromUri(uri) // Get file path
                filePath?.let {
                    var compressedFile = compressImage(it)

                    // Additional compression if file is still too large
                    val MAX_FILE_SIZE = 1024 * 1024 // 1 MB
                    if (compressedFile.length() > MAX_FILE_SIZE) {
                        Log.d(TAG, "File still too large, applying additional compression")
                        compressedFile = compressImageWithQuality(compressedFile)
                    }
                    val requestBody = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    profileImagePart = MultipartBody.Part.createFormData("profil", compressedFile.name, requestBody)
                } ?: run {
                    // Handle case where file path is null
                    Toast.makeText(this@ProfileActivity, "Error getting file path", Toast.LENGTH_SHORT).show()
                }
            }

            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getApiService(token)
                    val response = apiService.updateUserProfile(
                        username = username,
                        password = password,
                        profil = profileImagePart
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        // Refresh user profile setelah update
                        viewModel.fetchUserProfile(token)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Terjadi kesalahan: ${e.message}")
                }
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.imageView.setImageURI(selectedImageUri)
        }
    }

    private fun getRealPathFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(cacheDir, "temp_image.jpg")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return file
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

        val compressedFile = File(this.cacheDir, "ultra_compressed_image.jpg")
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

        val compressedFile = File(this.cacheDir, "compressed_image.jpg")
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
        private const val REQUEST_CODE_PICK_IMAGE = 123
    }
}