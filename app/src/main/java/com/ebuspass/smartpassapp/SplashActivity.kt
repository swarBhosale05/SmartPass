package com.ebuspass.smartpassapp
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ebuspass.smartpassapp.databinding.ActivitySplashBinding
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("FDatabase", Context.MODE_PRIVATE)
        val loginStatus = sharedPreferences.getBoolean("isLoggedIn", false)
        Handler(Looper.getMainLooper()).postDelayed({
            if (loginStatus) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(applicationContext, InfoActivity::class.java))
                finish()
            }
        }, 3000)
    }
}