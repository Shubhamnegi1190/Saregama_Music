package com.example.saregamamusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.saregamamusic.databinding.FavouriteViewBinding
import com.example.saregamamusic.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistViewAdapter(private val context: Context, private var playlistList:ArrayList<Playlist>): RecyclerView.Adapter<PlaylistViewAdapter.MyHolder>(){

    class MyHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name  = binding.playlistName
        val root  = binding.root
        val delete = binding.PLaylistDeleteBtn
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        // returns the newView holder
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context),parent,false))

    }
    // binds the list items to a view
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.name.text= playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val buider = MaterialAlertDialogBuilder(context)
            buider.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
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


holder.root.setOnClickListener {
    val intent = Intent(context, PlaylistDetails::class.java)
    intent.putExtra("index",position)
    ContextCompat.startActivity(context , intent , null)
}

        if (PlaylistActivity.musicPlaylist.ref[position].playlist.size> 0 ){
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.play).centerCrop())
                .into(holder.image)
        }
    }


    override fun getItemCount(): Int {
        return playlistList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }




}