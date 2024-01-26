package com.example.saregamamusic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.SearchView.*
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saregamamusic.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {

    // lateinit is used for late initialization
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var adapter: MusicAdapter


    companion object {
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var musicListSearch : ArrayList<Music>
        var search: Boolean = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Home";
        actionBar?.setIcon(R.drawable.home);

        setTheme(R.style.Theme_SaregamaMusic)


        // here we initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // drawer and backbutton to close drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)

        // pass the open and close toggle for the drawer layout listner
        //to toggle the button
        binding.root.addDrawerListener(toggle)
        toggle.syncState()

        // to make Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (requestRuntimePermission())
            initializeLayout()

        // for retriving fav data using shared preferences
        FavouriteActivity.favouriteSongs = ArrayList()
        val editor = getSharedPreferences("FAVOURITES"  , MODE_PRIVATE)
        val jsonString =  editor.getString("FavouriteSongs" , null )
        val typeToken  = object: TypeToken<ArrayList<Music>>(){}.type
        if(jsonString !=null){
            val data:ArrayList<Music> = GsonBuilder().create().fromJson(jsonString ,typeToken)
            FavouriteActivity.favouriteSongs.addAll(data)
        }

        PlaylistActivity.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist =  editor.getString("MusicPlaylist" , null )
        if(jsonStringPlaylist !=null){
            val dataPlaylist:MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist ,MusicPlaylist::class.java)
            PlaylistActivity.musicPlaylist = dataPlaylist
        }


        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.FavBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, FavouriteActivity::class.java)
            startActivity(intent)
        }

        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)
        }
        // here we set click on each item of nav drawer
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedback -> startActivity(Intent(this@MainActivity , FeedBackActivity::class.java))

                R.id.navSetting -> startActivity(Intent(this@MainActivity , SettingsActivity::class.java))

                R.id.navAbout -> startActivity(Intent(this@MainActivity , AboutActivity::class.java))
                // this will exit the application
                R.id.navExit -> {
                    val buider = MaterialAlertDialogBuilder(this)
                    buider.setTitle("Exit")
                        .setMessage("Do you want to close app")
                        .setPositiveButton("Yes") { _, _ ->

                            exitApplication()


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
            true
        }
    }

    // for requesting permission
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                13
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initializeLayout()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )

            }
        }
    }


    //override the onoptionitem selected()
    // function to implement
    // the item clicklistner callback
    //to open and close the navigation
    // drawer when item is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        search = false
        MusicListMA = getAllAudio()
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        adapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = adapter
        binding.TotalSongs.text = "Total Songs : " + adapter.itemCount

    }

    // function used to get the list of music from the storage
    @SuppressLint("Range")
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = arrayListOf<Music>()
        // cursor used to get the data from the storage
        // below code tells cursor that which type of file we need
        val selection = MediaStore.Audio.Media.IS_MUSIC + " !=0"
        // what data we need
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC.toString()).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
            cursor.close()
        }


        return tempList

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApplication()
        }

    }

    override fun onResume() {
        super.onResume()
        // for storing fav data using shared preferences
        val editor = getSharedPreferences("FAVOURITES"  , MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs" , jsonString)
        val jsonStringplaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist" , jsonStringplaylist)
        editor.apply()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if(newText !=null){
                    val userInput = newText.lowercase()
                    for(song in MusicListMA)
                        if(song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    search = true
                    adapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)


    }
}


