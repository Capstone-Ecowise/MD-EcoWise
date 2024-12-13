package com.example.capstone.view.activity.registration

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capstone.R
import com.example.capstone.databinding.ActivityRegistrationBinding
import com.example.capstone.di.ViewModelFactory
import com.example.capstone.view.activity.login.LoginActivity
import com.example.capstone.view.util.InputValidator

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding


    private val viewModel by viewModels<RegistrationViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.isError.observe(this) { isError ->
            isError?.let { errorOccurred ->
                viewModel.message.observe(this) { message ->
                    if (errorOccurred) {
                        showAlertDialog("Ouchh!", message, getString(R.string.try_again))
                    } else {
                        showAlertDialog("Yeah!", message, getString(R.string.next)) {
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun showAlertDialog(title: String, message: String?, buttonText: String, onPositive: (() -> Unit)? = null) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(buttonText) { dialog, _ ->
                onPositive?.invoke()
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun showLoading(isLoading: Boolean){
        binding.signupButton.isEnabled = !isLoading
        binding.signupButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        binding.signupButton.text = if (isLoading) getString(R.string.loading) else getString(R.string.signup)
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val username = binding.edRegisterUsername.text.toString().trim()
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            if (InputValidator.isEmailValid(email) && InputValidator.isPasswordValid(password)) {
                viewModel.register(username, name, email, password)
            } else {
                Toast.makeText(this, getString(R.string.invalid_password_email), Toast.LENGTH_SHORT).show()
            }
        }

        binding.signIn.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}