package org.hocrox.music;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubePlayerApiss extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player_api);
        YouTubePlayerView youTubePlayerView=findViewById(R.id.youTube);
        youTubePlayerView.initialize("AIzaSyCBMXTru8-CIM2ZQoLu4UF2ULelC7Mzdcg",this);

    }
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        if(!b){
            youTubePlayer.loadVideo("UQjEu-n21rk");
            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT );
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        Log.e("testingg","Errrr"+youTubeInitializationResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getYouTubeProvider().initialize("AIzaSyCBMXTru8-CIM2ZQoLu4UF2ULelC7Mzdcg",this);
    }

    private YouTubePlayer.Provider getYouTubeProvider() {

     return findViewById(R.id.youTube);
    }
}
