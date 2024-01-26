package com.example.saregamamusic

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.saregamamusic.databinding.ActivityPlaylistBinding
import com.example.saregamamusic.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

@Suppress("DEPRECATION")
class PlaylistDetails : AppCompatActivity() {

    lateinit var binding:ActivityPlaylistDetailsBinding
    private lateinit var adapter:MusicAdapter

    companion object{
        var currentPlaylistPos: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.splash_screen)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        currentPlaylistPos = intent.extras?.get("index") as Int
        try{PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist =
            checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)}
        catch(e: Exception){}
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager =  LinearLayoutManager(this)
        adapter = MusicAdapter(this , PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetails = true)
        binding.playlistDetailsRV.adapter = adapter

        binding.backbtnPD.setOnClickListener { finish() }

        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }
        binding.AddBtnPD.setOnClickListener {
            startActivity(Intent(this , SelectionActivity::class.java))
        }
        binding.RemoveAllPD.setOnClickListener {
            val buider = MaterialAlertDialogBuilder(this)
            buider.setTitle("Remove")
                .setMessage("Do you want to remove all song from playlist")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()


                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()

                }
            val customeDailog = buider.create()
            customeDailog.show()
            customeDailog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customeDailog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.PlaylistNamePD.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.moreInfoPD.text = "Total ${adapter.itemCount} Songs.\n\n" +
         "Created On:\n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdOn}\n\n" +
                " -- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdBy}"


        if(adapter.itemCount>0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).centerCrop())
                .into(binding.playListImgPD)

            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()

        val editor = getSharedPreferences("FAVOURITES"  , MODE_PRIVATE).edit()
        val jsonStringplaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist" , jsonStringplaylist)
        editor.apply()
        }
    }



