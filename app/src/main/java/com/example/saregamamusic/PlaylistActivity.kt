package com.example.saregamamusic

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saregamamusic.databinding.ActivityPlaylistBinding
import com.example.saregamamusic.databinding.AddPlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var PLaylistadapter: PlaylistViewAdapter
    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.splash_screen)

        binding=ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity ,2)
        PLaylistadapter = PlaylistViewAdapter(this , playlistList = musicPlaylist.ref)
        binding.playlistRV.adapter = PLaylistadapter


        binding.backbtnPLA.setOnClickListener { finish() }
        binding.addPlaylistBtn.setOnClickListener {
            customeAlertDailog()
        }
    }

    private fun customeAlertDailog(){
        val customeDailog = LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist , binding.root , false)
       val binder = AddPlaylistBinding.bind(customeDailog)
        val buider = MaterialAlertDialogBuilder(this)
        buider.setView(customeDailog)
            .setTitle("Playlist Details")
            .setPositiveButton("Add") { dialog, _ ->
             val playlistName =    binder.playlistName.text
                val createdBy = binder.yourName.text
                if (playlistName!=null && createdBy!=null){
                    if (playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlaylist(playlistName.toString() , createdBy.toString())}

                }

dialog.dismiss()

            }.show()

    }
    private fun addPlaylist(name:String , createdby:String){
        var playlistExists = false
        for (i in musicPlaylist.ref){
            if(name == i.name){
                playlistExists = true
                break
            }
        }
      if(playlistExists) Toast.makeText(this , "Playlist Exists!!" , Toast.LENGTH_SHORT).show()
        else{
            val tempPlaylist = Playlist()
          tempPlaylist.name = name
          tempPlaylist.playlist = ArrayList()
          tempPlaylist.createdBy = createdby
          val calendar = java.util.Calendar.getInstance().time
          val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
          tempPlaylist.createdOn = sdf.format(calendar)
          musicPlaylist.ref.add(tempPlaylist)
          PLaylistadapter.refreshPlaylist()
      }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        PLaylistadapter.notifyDataSetChanged()
    }
}