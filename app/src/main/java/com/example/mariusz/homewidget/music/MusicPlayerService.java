package com.example.mariusz.homewidget.music;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mariusz.homewidget.R;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.example.mariusz.homewidget.widget.HomeScreenWidget.ACTION_NEXT_TRACK;
import static com.example.mariusz.homewidget.widget.HomeScreenWidget.ACTION_PAUSE;
import static com.example.mariusz.homewidget.widget.HomeScreenWidget.ACTION_PLAY;
import static com.example.mariusz.homewidget.widget.HomeScreenWidget.ACTION_PREV_TRACK;

public class MusicPlayerService extends Service {

    private static final String TAG = "MusicPlayerService";
    private MediaPlayer mediaPlayer;
    private List<Integer> songsIds = new ArrayList<>();
    private int currentTrackIndex = 0;
    private boolean isPaused = false;

    private final Map<String, Consumer<Intent>> ACTION_TO_HANDLER_MAPPER =
            ImmutableMap.<String, Consumer<Intent>>builder()
                    .put(ACTION_PLAY, this::handlePlay)
                    .put(ACTION_PAUSE, this::handlePause)
                    .put(ACTION_NEXT_TRACK, this::handleNextTrack)
                    .put(ACTION_PREV_TRACK, this::handlePrevTrack)
                    .build();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG,"Action: " + action);
        ACTION_TO_HANDLER_MAPPER.getOrDefault(action, this::handleDefault).accept(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handlePlay(Intent intent) {
        if (mediaPlayer == null) {
            prepareMusicPlayer();
            playTrack(currentTrackIndex);
        } else if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
        } else if (!mediaPlayer.isPlaying()) {
            playTrack(currentTrackIndex);
        }
    }

    private void handlePause(Intent intent) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    private void handleNextTrack(Intent intent) {
        if (mediaPlayer != null) {
            playNextTrack();
        }
    }

    private void handlePrevTrack(Intent intent) {
        if (mediaPlayer != null) {
            playPreviousTrack();
        }
    }

    private void handleDefault(Intent intent) {

    }

    private void prepareMusicPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> playNextTrack());
        songsIds = loadSongsIds();
        if (songsIds.isEmpty()) {
            throw new RuntimeException("No songs available");
        }
        Log.d(TAG,"LOADED song ids: " + songsIds);
    }

    private AssetFileDescriptor prepareTrackResource(int trackIndex) {
        Preconditions.checkElementIndex(currentTrackIndex, songsIds.size());
        int resourceId = songsIds.get(trackIndex);
        AssetFileDescriptor assetFileDescriptor = getApplicationContext()
                .getResources()
                .openRawResourceFd(resourceId);

        Log.i(TAG, "File: " + assetFileDescriptor.getLength());
        return assetFileDescriptor;
    }

    private List<Integer> loadSongsIds() {
        List<Integer> songsIds = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                songsIds.add(fields[i].getInt(fields[i]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return songsIds;
    }

    private void playNextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % songsIds.size();
        playTrack(currentTrackIndex);
    }

    private void playPreviousTrack() {
        currentTrackIndex = Math.floorMod(currentTrackIndex - 1, songsIds.size());
        playTrack(currentTrackIndex);
    }

    private void playTrack(int trackIndex) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(prepareTrackResource(trackIndex));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}