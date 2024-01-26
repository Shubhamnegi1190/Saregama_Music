package com.example.saregamamusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.saregamamusic.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// here we also implement the service
@Suppress("DEPRECATION")
class PlayerActivity : AppCompatActivity() , ServiceConnection , MediaPlayer.OnCompletionListener {

    companion object{
       lateinit var musicListPA:ArrayList<Music>

       var songPosition: Int =0

        var isPlaying:Boolean = false

        var musicService:MusicService? = null


        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding

        var repeat: Boolean  = false

        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite : Boolean = false
        var fIndex:Int = -1

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.splash_screen)
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeLayout()

// Bind to the service.



        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM , Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent , "Sharing music file"))
        }

       binding.favbtnPA.setOnClickListener {
           if (isFavourite){
               isFavourite=false
               binding.favbtnPA.setImageResource(R.drawable.fav_empty_icon)
               FavouriteActivity.favouriteSongs.removeAt(fIndex)
           }else{

                   isFavourite=true
                   binding.favbtnPA.setImageResource(R.drawable.fav_icon)
                   FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
           }
       }

        binding.backBtnPA.setOnClickListener{
            finish()
        }




        binding.playPauseBtnPA.setOnClickListener {
           if (isPlaying) pauseMusic()
            else playMusic()
        }

        binding.previousBtnPA.setOnClickListener {
              prevNextSong(increment = false)
        }

        binding.nextBtnPA.setOnClickListener {
                prevNextSong(increment = true)
        }

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }



            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.repeatBtnPA.setOnClickListener{
            if(!repeat){
                repeat=true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.spotify))

            }else{
                repeat=false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.grey))

            }
        }

        binding.equalizerBtnPA.setOnClickListener {
           try {
               val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
               eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION , musicService!!.mediaPlayer!!.audioSessionId)
               eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME , baseContext.packageName)
               eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE , AudioEffect.CONTENT_TYPE_MUSIC)
               startActivityForResult(eqIntent , 13)
           }catch (e:Exception){Toast.makeText(this , "Equalizer Feature not Supported" , Toast.LENGTH_SHORT ).show()}
        }

        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if(!timer) showBottomSheetDialog()
            else {
                val buider = MaterialAlertDialogBuilder(this)
                buider.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer")
                    .setPositiveButton("Yes"){ _, _ ->

                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.grey))


                    }
                    .setNegativeButton("No"){dialog, _ ->
                        dialog.dismiss()

                    }
                val customeDailog = buider.create()
                customeDailog.show()
                customeDailog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customeDailog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }




        }


    }


    // this fun is used to set image layout to the player image view
    private fun setLayout(){
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).centerCrop())
            .into(binding.songImgPA)

        binding.songNamePA.text = musicListPA[songPosition].title
        if(repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.spotify))
        if(min15|| min30|| min60)        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.spotify))
       if(isFavourite) binding.favbtnPA.setImageResource(R.drawable.fav_icon)
        else binding.favbtnPA.setImageResource(R.drawable.fav_empty_icon)
    }





     private fun createMediaPlayer(){
         try {
             if(musicService!!.mediaPlayer==null) musicService!!.mediaPlayer= MediaPlayer()
             musicService!!.mediaPlayer!!.reset()
             musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
             musicService!!.mediaPlayer!!.prepare()
             musicService!!.mediaPlayer!!.start()
             isPlaying = true
             binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
             musicService!!.showNotification(R.drawable.pause_icon , 1F)

             binding.seekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
             binding.seekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
             // used to specifyies the default progress value btw 0-100
             binding.seekBarPA.progress = 0
             // max attribute used to specifies the max value
             binding.seekBarPA.max= musicService!!.mediaPlayer!!.duration
             musicService!!.mediaPlayer!!.setOnCompletionListener(this)
             nowPlayingId = musicListPA[songPosition].id


         }catch (e:Exception)
         {return}
     }





    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index" , 0)
        when(intent.getStringExtra("class")){
            "FavouriteAdapter"->{
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA =  ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }
            "NowPlaying"-> {
                setLayout()
                binding.seekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
                else binding.playPauseBtnPA.setIconResource(R.drawable.play)
            }
            "MusicAdapterSearch"-> {
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA =  ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" ->{
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()



            }
            "MainActivity"->{
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()

            }

            "FavouriteShuffle" ->{
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }

            "PLaylistDetailAdapter" ->{
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                setLayout()
            }

            "PlaylistDetailsShuffle" ->{
                val intent  = Intent(this ,  MusicService::class.java)
                bindService(intent , this , BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }


    }

    private fun playMusic(){
         // first change the icon
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon , 1F)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic(){
       // change the icon
        binding.playPauseBtnPA.setIconResource(R.drawable.play)
        musicService!!.showNotification(R.drawable.play , 0F)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment:Boolean){
        if(increment){
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }else{
            setSongPosition(increment=false)
             setLayout()
            createMediaPlayer()
        }
    }





    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekbarSetup()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService , AudioManager.STREAM_MUSIC , AudioManager.AUDIOFOCUS_GAIN)


    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null

    }

     override fun onCompletion(mp: MediaPlayer?) {
             setSongPosition(increment = true)
             createMediaPlayer()
         try {
             setLayout()
         }catch (e:Exception){return}

     }

     @Deprecated("Deprecated in Java")
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == 13 || resultCode == RESULT_OK)
             return
     }

     private fun showBottomSheetDialog(){
         val dialog = BottomSheetDialog(this@PlayerActivity)
         dialog.setContentView(R.layout.bottom_sheet_dialog)
         dialog.show()
         dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener{
             Toast.makeText(baseContext , "Music will stop after 15 minutes" , Toast.LENGTH_SHORT).show()
             binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.spotify))
             min15=true
             Thread{Thread.sleep((15*6000).toLong())
             if(min15) exitApplication()
             }.start()
             dialog.dismiss()
         }


         dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener{
             Toast.makeText(baseContext , "Music will stop after 30 minutes" , Toast.LENGTH_SHORT).show()
             binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.spotify))
             min30=true
             Thread{Thread.sleep((30*6000).toLong())
                 if(min30) exitApplication()
             }.start()
             dialog.dismiss()
         }


         dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener{
             Toast.makeText(baseContext , "Music will stop after 60 minutes" , Toast.LENGTH_SHORT).show()
             binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this , R.color.spotify))
             min60=true
             Thread{Thread.sleep((60*6000).toLong())
                 if(min60) exitApplication()
             }.start()
             dialog.dismiss()
         }
     }
 }