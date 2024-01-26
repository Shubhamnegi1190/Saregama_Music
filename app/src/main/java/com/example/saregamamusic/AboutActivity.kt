package com.example.saregamamusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.saregamamusic.databinding.ActivityAboutBinding
import com.example.saregamamusic.databinding.ActivityFeedBackBinding

class AboutActivity : AppCompatActivity() {


    lateinit var binding : ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setTheme(R.style.Theme_SaregamaMusic)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
        binding.idAbouttext.text = aboutText()
    }

    private fun aboutText():String{
        return " Developed By : Shubham Negi" +
                "\n\nif you want to provide feed back i will love to hear that"
    }
}