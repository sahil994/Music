package org.hocrox.music

import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log


class MusicListingActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null
    var selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_listing)

        recyclerView = findViewById(R.id.recyclerView) as RecyclerView

        getAllMusicFiles()

    }


    private fun getAllMusicFiles() {
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION)

        var cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)
        val songs = ArrayList<MusicDetailModel>()
        while (cursor.moveToNext()) {
            Log.e("testing", (cursor.getString(0) + "||" + cursor.getString(1) + "||" + cursor.getString(2) + "||" + cursor.getString(3) + "||" + cursor.getString(4) + "||" + cursor.getString(5)))

            songs.add(MusicDetailModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        }


        if (songs.size > 0) {

            var musicAdapter = MusicAdapter(this,songs)
            recyclerView?.layoutManager = LinearLayoutManager(this)
            recyclerView?.adapter = musicAdapter

        }

    }

}
