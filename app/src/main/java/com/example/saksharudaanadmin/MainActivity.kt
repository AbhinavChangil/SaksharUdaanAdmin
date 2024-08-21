package com.example.saksharudaanadmin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.saksharudaanadmin.fragment.HomeFragment
import com.example.saksharudaanadmin.fragment.ProfileFragment
import com.example.saksharudaanadmin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

        //initialize firebase database and auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        //Initialize with home fragment
        binding.bottomNavBar.setItemSelected(R.id.home,true)
        binding.tvToolbarTilte.text = "SaksharUdaan"
        supportFragmentManager.beginTransaction().replace(R.id.container_main_ll, HomeFragment()).commit()

        // Set up the ChipNavigationBar
        setupChipNavigationBar()

    }

    private fun setupChipNavigationBar() {
        binding.bottomNavBar.setOnItemSelectedListener { itemId ->
            var selectedFragment: Fragment? = null
            when (itemId) {
                R.id.home -> { selectedFragment = HomeFragment()
                    binding.tvToolbarTilte.text = "SaksharUdaan"
                }
                R.id.profile -> {
                    selectedFragment = ProfileFragment()
                    binding.tvToolbarTilte.text = "Profile"
                }
//                R.id.add -> {
//                    startActivity(Intent(this, LoginActivity::class.java))
//                }
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.container_main_ll, selectedFragment).commit()
            }
        }
    }
}