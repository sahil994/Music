package org.hocrox.music

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

public class MusicAdapter(var musicListingActivity: MusicListingActivity, var songs: ArrayList<MusicDetailModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        var musicDetailModel: MusicDetailModel = songs.get(position)
        (holder as MyViewHolder).musicName.text = musicDetailModel.title
        (holder as MyViewHolder).rootLayout.setOnClickListener({

            musicListingActivity.startActivity(Intent(musicListingActivity,MusicListActivity::class.java).putExtra("id",musicDetailModel.id))

        })


    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        var view = LayoutInflater.from(musicListingActivity).inflate(R.layout.custom_music, parent, false)

        return MyViewHolder(view)
    }

    public class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var rootLayout: LinearLayout = view.findViewById(R.id.rootLayout)
        var musicName: TextView = view.findViewById<TextView>(R.id.tvMusicName) as TextView
        var iamgeView: ImageView = view.findViewById(R.id.ivImage)

    }
}