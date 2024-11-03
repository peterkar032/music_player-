package com.example.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;
import java.util.List;

public class MediaPlayerManager {
    private static MediaPlayer mediaPlayer;
    private static List<Integer> trackList;
    private static int currentTrackIndex = 0;
    private static boolean isPlaying = false;

    public static void initialize(Context context, List<Integer> tracks, int startIndex) {
        trackList = tracks;
        currentTrackIndex = startIndex;
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, trackList.get(currentTrackIndex));
            mediaPlayer.setOnCompletionListener(mp -> nextTrack(context));
        }
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static void play(Context context) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            Toast.makeText(context, "Playing", Toast.LENGTH_SHORT).show();
        }
    }

    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public static void nextTrack(Context context) {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size();
        switchTrack(context);
    }

    public static void previousTrack(Context context) {
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size();
        switchTrack(context);
    }

    private static void switchTrack(Context context) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(context, trackList.get(currentTrackIndex));
        mediaPlayer.start();
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
}
