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
import com.example.saregamamusic.databinding.FavouriteViewBinding
import com.example.saregamamusic.databinding.MusicViewBinding

class FavAdapter(private val context: Context, private var musicList: ArrayList<Music>): RecyclerView.Adapter<FavAdapter.MyHolder>(){
    class MyHolder(binding: FavouriteViewBinding): RecyclerView.ViewHolder(binding.root) {

        val image = binding.songImgFV
        val name  = binding.songNameFV
        val root  = binding.root


    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        // returns the newView holder
        return MyHolder(FavouriteViewBinding.inflate(LayoutInflater.from(context),parent,false))

    }
    // binds the list items to a view
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text= musicList[position].title
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {  val intent = Intent(context,PlayerActivity::class.java)
            intent.putExtra("index",position)
            intent.putExtra("class","FavouriteAdapter")
            ContextCompat.startActivity(context , intent , null) }

    }

    override fun getItemCount(): Int {
        return musicList.size
    }




}