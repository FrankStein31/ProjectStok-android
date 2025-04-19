package hadi.veri.project1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hadi.veri.project1.databinding.ActivityMainBinding
import hadi.veri.project1.fragments.DataFragment
import hadi.veri.project1.fragments.HomeFragment
import hadi.veri.project1.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)
        sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE)
    
        replaceFragment(HomeFragment.newInstance())
        
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportActionBar?.title = "Home"
                    replaceFragment(HomeFragment.newInstance())
                    true
                }
                R.id.navigation_data -> {
                    supportActionBar?.title = "Data Barang"
                    replaceFragment(DataFragment.newInstance())
                    true
                }
                R.id.navigation_profile -> {
                    supportActionBar?.title = "Profil"
                    replaceFragment(ProfileFragment.newInstance())
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

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            
            // Jika ada fragment di backstack, update judul sesuai dengan fragment yang ada
            val currentFragment = fragmentManager.findFragmentById(R.id.frame_container)
            
            when (currentFragment) {
                is HomeFragment -> {
                    supportActionBar?.title = "Home"
                    binding.bottomNavigationView.selectedItemId = R.id.navigation_home
                }
                is DataFragment -> {
                    supportActionBar?.title = "Data Barang"
                    binding.bottomNavigationView.selectedItemId = R.id.navigation_data
                }
                is ProfileFragment -> {
                    supportActionBar?.title = "Profil"
                    binding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        
        // Kembali ke halaman Welcome
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
