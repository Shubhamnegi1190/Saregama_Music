package com.example.saregamamusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.saregamamusic.databinding.ActivityFeedBackBinding
import com.example.saregamamusic.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    lateinit var binding : ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SaregamaMusic)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Settings"
    }
}