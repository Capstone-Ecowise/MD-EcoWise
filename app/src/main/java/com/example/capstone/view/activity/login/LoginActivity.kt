package com.example.capstone.view.activity.login

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
import com.example.capstone.databinding.ActivityLoginBinding
import com.example.capstone.di.ViewModelFactory
import com.example.capstone.view.activity.main.MainActivity
import com.example.capstone.view.activity.rank.RankActivity
import com.example.capstone.view.activity.registration.RegistrationActivity
import com.example.capstone.view.util.InputValidator

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
            isError?.let { error ->
                viewModel.message.observe(this) { message ->
                    if (error) {
                        showAlertDialog("Opss!", message, getString(R.string.try_again))
                    } else {
                        showAlertDialog("Yeah!", message, getString(R.string.next)) {
                            moveToList()
                        }
                    }
                }
            }
        }
    }

    private fun moveToList() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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

    private fun showLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.loginButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        binding.loginButton.text = if (isLoading) getString(R.string.loading) else getString(R.string.login)
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val userName = binding.edLoginUsername.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (InputValidator.isPasswordValid(password)) {
                viewModel.login(userName, password)
            } else {
                Toast.makeText(this, getString(R.string.invalid_password_email), Toast.LENGTH_SHORT).show()
            }
        }
        binding.signIn.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
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