package com.example.saregamamusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.saregamamusic.databinding.ActivitySplashScreenBinding

class Splashscreen : AppCompatActivity() {
    lateinit var binding:ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.splash_screen)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.getStarted.setOnClickListener {
            val intent = Intent(this@Splashscreen , MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.continueAsGuest.setOnClickListener {
            val intent = Intent(this@Splashscreen , MainActivity::class.java)
            startActivity(intent)
            finish()
        }



//        // This is used to hide the status bar and make the splash screen as full screen
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//
//
//        )
//
//        // we used postdelayed(Runnable , time) method
//        // to send a message with a delayed time
//        // normal handler is deprecated , so we have to change the code little bit
//
//                    Handler(Looper.getMainLooper()).postDelayed({
//                val intent = Intent(this@Splashscreen , MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }, 3000000)  // 3000 is the delay in the time


    }



}