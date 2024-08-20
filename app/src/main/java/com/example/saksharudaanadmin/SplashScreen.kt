package com.example.saksharudaanadmin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.saksharudaanadmin.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private val binding: ActivitySplashScreenBinding by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        val logo = binding.logoSplash

        // Load the animation
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        // Set an AnimationListener to handle what happens after the animation ends
        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // You can perform actions when the animation starts if needed
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Add a delay before starting the main activity
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this@SplashScreen, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 500) // 1000 milliseconds = 1 second
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // You can perform actions when the animation repeats if needed
            }
        })

        // Start the animation
        logo.startAnimation(scaleAnimation)
    }
}