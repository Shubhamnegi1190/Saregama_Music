package com.example.saregamamusic

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.saregamamusic.PlayerActivity.Companion.musicService

class MusicService : Service() , AudioManager.OnAudioFocusChangeListener{
    //Binder given to clients.
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    lateinit var audioManager: AudioManager
    private lateinit var mediaSession : MediaSessionCompat
    // runnable is used to execute the same code many times
    private lateinit var runnable: Runnable



  // this method is called when we bind this method with class
    // alse retruns a object of mybinder

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext , "My Music")
      return  myBinder
    }


    inner class MyBinder : Binder(){
        // Return this instance of LocalService so clients can call public methods.
        fun currentService():MusicService{
            return this@MusicService
        }
    }

    @SuppressLint("ForegroundServiceType", "UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn:Int , playbackSpeed:Float){
        // here we send the broadcast to NotificationReciver

        val intent = Intent(baseContext,MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this , 0 , intent ,flag )

        val prevIntent = Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent = Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent = Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent = Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)


        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
      val image =   if(imgArt !=null) {
            BitmapFactory.decodeByteArray(imgArt , 0 ,imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources , R.drawable.ic_launcher_background)

        }
              // here we implement our notification
              val notification= NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                  .setContentIntent(contentIntent)
                  .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
                  .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
                  .setSmallIcon(R.drawable.music_icon)
                  .setLargeIcon(image)
                  .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
                  .setPriority(NotificationCompat.PRIORITY_HIGH)
                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                  .setOnlyAlertOnce(true)
                  .addAction(R.drawable.previous_icon , "Previous" ,  prevPendingIntent)
                  .addAction(playPauseBtn , "Play" ,  playPendingIntent)
                  .addAction(R.drawable.next_icon , "Next" ,  nextPendingIntent)
                  .addAction(R.drawable.exit_icon , "Exit" ,  exitPendingIntent)
                  .build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION , mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING , mediaPlayer!!.currentPosition.toLong() ,playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }

         startForeground(13 , notification)
    }

    fun createMediaPlayer(){
        try {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            showNotification( R.drawable.pause_icon , 1F)
            PlayerActivity.binding.seekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id


        }catch (e:Exception) {return}
    }

    fun seekbarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.seekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable , 200)

        }
        Handler(Looper.getMainLooper()).postDelayed(runnable , 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange<=0){
//         // play music
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)

            showNotification(R.drawable.pause_icon , 1F )
            PlayerActivity.isPlaying = true
            mediaPlayer!!.start()
        }else{

                PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play)
            showNotification(R.drawable.play , 0F)
            PlayerActivity.isPlaying = false
            mediaPlayer!!.pause()

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}