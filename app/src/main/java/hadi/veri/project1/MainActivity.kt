package hadi.veri.project1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hadi.veri.project1.api.AuthApi
import hadi.veri.project1.api.LogoutResponse
import hadi.veri.project1.api.RetrofitClient
import hadi.veri.project1.databinding.ActivityMainBinding
import hadi.veri.project1.fragments.DataFragment
import hadi.veri.project1.fragments.HomeFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)

        // Ambil SharedPreferences
        sharedPreferences = getSharedPreferences("login_session", Context.MODE_PRIVATE)

        // Ambil role dari SharedPreferences
        userRole = sharedPreferences.getString("role", null)

        // Log role untuk debugging
        println("Role in MainActivity: $userRole")

        // Tampilkan fragment awal (Home)
        replaceFragment(HomeFragment.newInstance())

        // Setup navigasi bottom menu
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportActionBar?.title = "Home"
                    replaceFragment(HomeFragment.newInstance())
                    true
                }
                R.id.navigation_data -> {
                    supportActionBar?.title = "Data Barang"
                    // Teruskan role ke FragmentData
                    val dataFragment = DataFragment.newInstance()
                    val bundle = Bundle()
                    bundle.putString("role", userRole)
                    dataFragment.arguments = bundle
                    replaceFragment(dataFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        val token = sharedPreferences.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(AuthApi::class.java)
        val call = api.logout("Bearer $token")

        call.enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful) {
                    // Hapus session dan redirect
                    sharedPreferences.edit().clear().apply()

                    val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Logout gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Logout error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
