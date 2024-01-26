package com.example.saregamamusic

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.nio.channels.Channel

//In Android, the Application class is an integral part of the application lifecycle and serves as the entry point for an Android application. It acts as a base
// class for maintaining global application state and performing initialization tasks that need to be executed before any components of the application are created.
class ApplicationClass:Application() {
    companion object{
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play!"
        const val NEXT = "next!"
        const val PREVIOUS = "previous!"
        const val EXIT = "exit"
    }


    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Create the NotificationChannel.
            val notificationChannel = NotificationChannel(CHANNEL_ID ,"Now Playing Song" , NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is a important channel for showing songs!!"
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


}