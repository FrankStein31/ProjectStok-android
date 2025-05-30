package hadi.veri.project1.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.WelcomeActivity
import hadi.veri.project1.api.AuthApi
import hadi.veri.project1.api.RegisterRequest
import hadi.veri.project1.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val passwordConfirmation = binding.etPasswordConfirm.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                name = name,
                email = email,
                password = password,
                password_confirmation = passwordConfirmation,
                phone = if (phone.isEmpty()) null else phone,
                address = if (address.isEmpty()) null else address
            )

            // Menggunakan AuthApi berbasis Volley
            AuthApi.register(
                context = this,
                request = request,
                onSuccess = { registerResponse ->
                    val message = registerResponse.message ?: "Registrasi berhasil"
                    Toast.makeText(this@RegisterActivity, "$message. Silakan login.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                },
                onError = { error ->
                    Toast.makeText(
                        this@RegisterActivity,
                        "Gagal: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}
