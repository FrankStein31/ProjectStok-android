package hadi.veri.project1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hadi.veri.project1.databinding.ActivityMainBinding
import hadi.veri.project1.fragments.DataFragment
import hadi.veri.project1.fragments.HomeFragment
import hadi.veri.project1.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)
    
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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
