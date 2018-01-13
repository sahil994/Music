package org.hocrox.music.Testing;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    PlaybackStateCompat.Builder playbackStateCompatBuilder;
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    MusicPlayerAdapter playerAdapter;
    private boolean mServiceInStartedState;



    MediaSessionCompat mediaSessionCompat;
    MediaNotificationManager mediaNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(this, "Music");
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
      /*  playbackStateCompatBuilder = new PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        mediaSessionCompat.setPlaybackState(playbackStateCompatBuilder.build());
     */   mediaSessionCompat.setCallback(new MediaSessionCallback());
        setSessionToken(mediaSessionCompat.getSessionToken());

        mediaNotificationManager = new MediaNotificationManager(this);
        playerAdapter = new MusicPlayerAdapter(this, new MediaPlayerListener());

    }
    class ServiceManager {

        private void moveServiceToStartedState(PlaybackStateCompat state) {
            Notification notification =
                    mediaNotificationManager.getNotification(
                            playerAdapter.getCurrentMedia(), state, getSessionToken());

            if (!mServiceInStartedState) {
              /*  ContextCompat.startForegroundService(
                        MusicService.this,
                        new Intent(MusicService.this, MusicService.class));*/
                //ContextCompat.st(MediaPlaybackService.this, new Intent(MediaPlaybackService.this, MediaPlaybackService.class));

                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(new Intent(MediaPlaybackService.this,MediaPlaybackService.class));
                } else {
                    // Pre-O behavior.
               startService(new Intent(MediaPlaybackService.this,MediaPlaybackService.class));
                }
               // startService(new Intent(MediaPlaybackService.this,MediaPlaybackService.class));

                mServiceInStartedState = true;
            }

            startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
        }

        private void updateNotificationForPause(PlaybackStateCompat state) {
            stopForeground(false);
            Notification notification =
                    mediaNotificationManager.getNotification(
                            playerAdapter.getCurrentMedia(), state, getSessionToken());
            mediaNotificationManager.getNotificationManager()
                    .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
        }

        private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
            stopForeground(true);
            stopSelf();
            mServiceInStartedState = false;
        }
    }
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // Nothing to play.
                return;
            }

            final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = MusicLibrary.getMetadata(MediaPlaybackService.this, mediaId);
            mediaSessionCompat.setMetadata(mPreparedMedia);

            if (!mediaSessionCompat.isActive()) {
                mediaSessionCompat.setActive(true);
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                // Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
                onPrepare();
            }

            playerAdapter.playFromMedia(mPreparedMedia);
        }

        @Override
        public void onPause() {
            playerAdapter.pause();
        }

        @Override
        public void onStop() {
            playerAdapter.stop();
            mediaSessionCompat.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            mQueueIndex = (++mQueueIndex % mPlaylist.size());
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            playerAdapter.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return (!mPlaylist.isEmpty());
        }
    }


        public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mediaSessionCompat.setPlaybackState(state);

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
    @Override
    public void onDestroy() {
        mediaNotificationManager.onDestroy();
        playerAdapter.stop();
        mediaSessionCompat.release();
    }




    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {


        return new BrowserRoot(MusicLibrary.getRoot(), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {



        result.sendResult(MusicLibrary.getMediaItems());

    }

    }
