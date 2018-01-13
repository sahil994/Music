package org.hocrox.music
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import android.widget.MediaController
class MusicListActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl, SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }
    override fun surfaceCreated(holder: SurfaceHolder?) {
        BackgroundService.getServiceInstance()!!.musicPlayer?.setDisplay(holder)
        mediaController = MediaController(this,false);
        mediaController!!.setMediaPlayer(this)
        mediaController!!.setAnchorView(surface)
       mediaController!!.show(0)
        mediaController!!.setPrevNextListeners({

          MusicListActivity.id = 1207

          BackgroundService.getServiceInstance()!!.changeTrack()

      },{            MusicListActivity.id = 1210


          BackgroundService.getServiceInstance()!!.changeTrack()

      })
        handler.post {
            mediaController!!.setEnabled(true)
            mediaController!!.show()
        }
    }
    var handler: Handler = Handler()
    override fun isPlaying(): Boolean {
        return BackgroundService.getServiceInstance()!!.musicPlayer!!.isPlaying;
    }
    override fun canSeekForward(): Boolean {
        return true; }
    override fun getDuration(): Int {
        return BackgroundService.getServiceInstance()!!.musicPlayer!!.duration
    }
    override fun pause() {
        BackgroundService.getServiceInstance()!!.stopMusic()
    }
    override fun getBufferPercentage(): Int {
        return 0;
    }
    override fun seekTo(pos: Int) {
        return BackgroundService.getServiceInstance()!!.musicPlayer!!.seekTo(pos);
    }
    override fun getCurrentPosition(): Int {
        return BackgroundService.getServiceInstance()!!.musicPlayer!!.currentPosition;
    }
    override fun canSeekBackward(): Boolean {
        return true;
    }
    override fun start() {
        BackgroundService.getServiceInstance()!!.playMusic();
    }
    override fun onPause() {
        super.onPause()
    }
    override fun getAudioSessionId(): Int {
        return   BackgroundService.getServiceInstance()!!.musicPlayer!!.getAudioSessionId();
    }
    override fun canPause(): Boolean {
        return true;
    }
    companion object {
        var id: Long = 0
    }
    override fun onPrepared(mp: MediaPlayer?) {
        Log.e("testing", "called")
        if (mp != null) {
            mp.start();
        }
    }
    var selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
    var surfaceholder: SurfaceHolder? = null
    var mediaController: MediaController? = null
    var surface: SurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_list)
        surface = findViewById(R.id.surfacesView) as SurfaceView
        surfaceholder = surface?.holder
        surfaceholder?.addCallback(this)
        surface?.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (mediaController != null) {
                    mediaController?.show()
                }
                return false
            }
        })
        getAllMusicFiles()
        startService(Intent(this, BackgroundService::class.java))
        var button = findViewById(R.id.button)
        var changeTrack = findViewById(R.id.changeTrack)
        var pause = findViewById(R.id.pauseButton)
        var play = findViewById(R.id.playButton)
        button.setOnClickListener({
            MusicListActivity.id = intent.getStringExtra("id").toLong()
            BackgroundService.getServiceInstance()?.startMusic()
        })
        changeTrack.setOnClickListener({
            MusicListActivity.id = 1245
            BackgroundService.getServiceInstance()?.changeTrack()
        })
        play.setOnClickListener({
            BackgroundService.getServiceInstance()?.playMusic()
        })
        pause.setOnClickListener({
            BackgroundService.getServiceInstance()?.stopMusic()
        })


    }
    private fun getAllMusicFiles() {
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION)
        var cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)
        val songs = ArrayList<String>()
        while (cursor.moveToNext()) {
            Log.e("testing", (cursor.getString(0) + "||" + cursor.getString(1) + "||" + cursor.getString(2) + "||" + cursor.getString(3) + "||" + cursor.getString(4) + "||" + cursor.getString(5)))
            songs.add(cursor.getString(0) + "||" + cursor.getString(1) + "||" + cursor.getString(2) + "||" + cursor.getString(3) + "||" + cursor.getString(4) + "||" + cursor.getString(5));
        }
        /*     var uri = Uri.parse("/storage/0AFE-826A/download/Kothe_Di_Kanjri-Kanwar_Grewal_www.mp3")
             var mediaPlayer = MediaPlayer()
             mediaPlayer.setDataSource(this, uri)
             mediaPlayer.prepareAsync();*/
    }
}