package com.ebuspass.smartpassapp
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ebuspass.smartpassapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("DeletePasses", "Scheduling data deletion work...")
        scheduleDataDeletionWork()
        loadFragment(PassCreateFragment())
        val colorStateList =
            ContextCompat.getColorStateList(this, R.drawable.nav_item_selected_color)
        binding.bottomNav.itemIconTintList = colorStateList
        binding.bottomNav.itemTextColor = colorStateList
        binding.bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.createPass -> {
                    loadFragment(PassCreateFragment().apply {
                        arguments = Bundle().apply {
                        }
                    })
                    true
                }
                R.id.displayPass -> {
                    loadFragment(PassDisplayFragment())
                    true
                }
                R.id.route -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
        if (currentFragment is PassCreateFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }
}
