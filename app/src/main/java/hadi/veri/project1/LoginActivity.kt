package hadi.veri.project1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.bttn1.setOnClickListener {
            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        binding.txtSignUp.setOnClickListener {
            // Pindah ke RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}