package com.example.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MediaService extends Service {
    private MediaPlayer mediaPlayer;
    private List<Integer> trackList = new ArrayList<>(); // List to hold track resources
    private List<String> trackTitles = new ArrayList<>(); // List to hold track titles
    private int currentTrackIndex = 0; // Current track index
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Create notification channel
        loadTracks(); // Load tracks when the service is created
        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex)); // Start with the first track
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MEDIA_CHANNEL",
                    "Media Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MediaService", "Service started with action: " + (intent != null ? intent.getAction() : "null"));
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "PLAY":
                    playMusic();
                    break;
                case "PAUSE":
                    pauseMusic();
                    break;
                case "NEXT":
                    nextTrack();
                    break;
                case "PREVIOUS":
                    previousTrack();
                    break;
            }
        }
        return START_STICKY;
    }

    private void loadTracks() {
        // Use reflection to retrieve all music files from the raw folder
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
                // Add track title (adjust as needed)
                trackTitles.add(field.getName().replace("_", " ")); // Use file names as track titles
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }



    private void playMusic() {
        Log.d("MediaService", "Playing music");
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true; // Update state
            showNotification(); // Show notification when playing
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false; // Update state
            showNotification(); // Show notification when paused
        }
    }

    private void nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size(); // Circular switch
        initializeMediaPlayer(); // Reinitialize the player with the new track
        playMusic();
    }

    private void previousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size(); // Circular switch
        initializeMediaPlayer(); // Reinitialize the player with the new track
        playMusic();
    }

    private void initializeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        mediaPlayer.setOnCompletionListener(mp -> nextTrack()); // Automatically play next track on completion
    }

    private void showNotification() {
        Log.d("MediaService", "Showing notification");
        @SuppressLint("RemoteViewLayout") RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_bar); // Change to your XML file name

        // Set up the text and image for your custom notification
        notificationLayout.setTextViewText(R.id.nottxextsongtitle, trackTitles.get(currentTrackIndex));
        notificationLayout.setImageViewResource(R.id.imageView, R.drawable.music); // Set your music icon

        // Create intents for the buttons
        Intent playIntent = new Intent(this, MediaService.class);
        playIntent.setAction(isPlaying ? "PAUSE" : "PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MediaService.class);
        nextIntent.setAction("NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, MediaService.class);
        prevIntent.setAction("PREVIOUS");
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the pending intents to the buttons in the custom layout
        notificationLayout.setOnClickPendingIntent(R.id.button, playPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.button2, playPendingIntent); // Assuming you want the same action for pause/play
        notificationLayout.setOnClickPendingIntent(R.id.button3, prevPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.button4, nextPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MEDIA_CHANNEL")
                .setSmallIcon(R.drawable.music) // Your small icon
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout) // Use the custom layout
                .setOngoing(true); // Keep notification ongoing

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

