package hadi.veri.project1

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.activity.LoginActivity
import hadi.veri.project1.activity.RegisterActivity
import hadi.veri.project1.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.title = "WELCOME"
        
        // Coba membuat database dengan memeriksa apakah tabel users sudah ada
        val dbHelper = hadi.veri.project1.database.DBHelper(this)
        val db = dbHelper.writableDatabase
        
        // Force upgrade database ke versi terbaru
        try {
            dbHelper.onUpgrade(db, 0, 1)
        } catch (e: Exception) {
            // Jika terjadi error, abaikan
        }
        
        db.close()
        
        sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE)
        
        // Cek apakah sudah login
        if (isLoggedIn()) {
            navigateToMain()
            finish()
        }

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.welcome_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                return true
            }
            R.id.menu_register -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                return true
            }
            R.id.menu_home -> {
                navigateToMain()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    
    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}