package com.example.saregamamusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.saregamamusic.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var musicList:ArrayList<Music> , private var playlistDetails:Boolean=false ,
    private val selectionActivity: Boolean = false)
    : RecyclerView.Adapter<MusicAdapter.MyHolder>(){
    class MyHolder(binding: MusicViewBinding):RecyclerView.ViewHolder(binding.root) {
         val title = binding.songNameMV
         val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
           // returns the newView holder
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))

    }
    // binds the list items to a view
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).centerCrop())
            .into(holder.image)

        when{
            playlistDetails ->{
                holder.root.setOnClickListener {
                    sendIntent(ref = "PLaylistDetailAdapter" , pos=position)
                }
            }

            selectionActivity ->{
                holder.root.setOnClickListener {
                    if(addSong(musicList[position])){
                        holder.root.setBackgroundColor(ContextCompat.getColor(context , R.color.grey))
                    }
                    else
                        holder.root.setBackgroundColor(ContextCompat.getColor(context , R.color.white))
                }
            }
            else->  {holder.root.setOnClickListener{
                when{
                    MainActivity.search -> sendIntent(ref = "MusicAdapterSearch" , pos=position)
                    musicList[position].id == PlayerActivity.nowPlayingId -> sendIntent(ref = "NowPlaying" , pos=PlayerActivity.songPosition)
                    else->sendIntent(ref = "MusicAdapter" , pos  = position)
                }}

            }
        }
        // we set this click listner to oepn the player activity on clicking to any song

    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList: ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String , pos:Int){
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context , intent , null)
    }

    private fun addSong(song:Music):Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed{index , music->
            if(song.id==music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}