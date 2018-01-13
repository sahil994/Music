package org.hocrox.music

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews


public class BackgroundService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    companion object {

        var background: BackgroundService? = null
        fun getServiceInstance(): BackgroundService? {

            return background
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("testing","on Create called")

        if(musicPlayer==null){
            musicPlayer = MediaPlayer()
        }

    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {

        mp?.start()

    }

    fun changeTrack() {

        id = 167
        musicPlayer!!.reset()
        val contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        musicPlayer!!.setDataSource(this, contentUri)
        musicPlayer!!.setOnPreparedListener(this)

        musicPlayer!!.prepareAsync()

    }


    var id: Long = 167
    var musicPlayer: MediaPlayer? = null
    var musicLength: Int = 0
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

   Log.e("testimg","onStartt")
        background = this
        musicPlayer!!.reset()
        val contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        musicPlayer!!.setDataSource(this, contentUri)
        musicPlayer!!.prepare()
        musicPlayer!!.seekTo(musicLength)
        musicPlayer!!.start()

        var remoteView = RemoteViews(this.packageName, R.layout.custom_music)
        var bigView = RemoteViews(this.packageName, R.layout.status_bar_expanded)


        Log.e("testomgg", "" + intent?.action)

        if (Constants.NEXT_ACTION == intent?.action) {

            changeTrack()
        } else if (Constants.PLAY_ACTION == intent?.action) {

            if(musicPlayer!!.isPlaying){stopMusic() }
            else{

                playMusic()
            }


        } else if (Constants.PREV_ACTION == intent?.action) {
            changeTrack()
        }


        showNotification()

        return super.onStartCommand(intent, flags, startId)
    }


    fun startMusic() {

        Log.e("id of music player", "" + id)
        id = MusicListActivity.id
        musicPlayer!!.reset()
        val contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        musicPlayer!!.setDataSource(this, contentUri)
        musicPlayer!!.setOnPreparedListener(this)
        musicPlayer!!.prepareAsync()
    }

    fun playMusic() {
        if (value) {
            musicPlayer!!.reset()
            val contentUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            musicPlayer!!.setDataSource(this, contentUri)
            musicPlayer!!.prepare()
            musicPlayer!!.seekTo(musicLength)
            musicPlayer!!.start()
        }
    }

    var value: Boolean = false
    fun stopMusic() {
        value = true
        musicLength = musicPlayer!!.currentPosition
        musicPlayer!!.stop()
    }


    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    private fun showNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        val views = RemoteViews(packageName,
                R.layout.status_bar)
        val bigViews = RemoteViews(packageName,
                R.layout.status_bar_expanded)

        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE)
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE)
        /*  bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                  Constants.getDefaultAlbumArt(this))
  */
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val previousIntent = Intent(this, BackgroundService::class.java)
        previousIntent.action = Constants.PREV_ACTION
        val ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0)

        val playIntent = Intent(this, BackgroundService::class.java)
        playIntent.action = Constants.PLAY_ACTION
        val pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0)

        val nextIntent = Intent(this, BackgroundService::class.java)
        nextIntent.action = Constants.NEXT_ACTION
        val pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0)

        val closeIntent = Intent(this, BackgroundService::class.java)
        closeIntent.action = Constants.STOPFOREGROUND_ACTION
        val pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0)

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)

        views.setImageViewResource(R.id.status_bar_play,
                R.drawable.ic_play_arrow_black_24dp)
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.drawable.ic_play_arrow_black_24dp)

        views.setTextViewText(R.id.status_bar_track_name, "Song Title")
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title")

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name")
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name")

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name")

        var status = Notification.Builder(this).build()
        status.contentView = views
        status.bigContentView = bigViews
        status.flags = Notification.FLAG_ONGOING_EVENT
        status.icon = R.mipmap.ic_launcher
        status.contentIntent = pendingIntent
        startForeground(Constants.NOTIFICATION_ID, status)
    }


}