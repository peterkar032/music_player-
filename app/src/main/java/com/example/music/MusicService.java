package com.example.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private int currentTrackIndex = 0;
    private List<Integer> trackList;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        loadTracks();
        initializeMediaPlayer();
        mediaPlayer.setLooping(true);
    }

    private void loadTracks() {
        trackList = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int trackIndex = intent.getIntExtra("track_index", 0);
            if (trackIndex != currentTrackIndex) {
                currentTrackIndex = trackIndex;
                initializeMediaPlayer(); // Αρχικοποίηση του MediaPlayer με το νέο κομμάτι
            }
            mediaPlayer.start(); // Ξεκινάμε την αναπαραγωγή
            showNotification(); // Ενημερώνουμε τη ειδοποίηση
        }
        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Playing")
                .setContentText("Track " + (currentTrackIndex + 1))
                .setSmallIcon(R.drawable.music1)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground(1, notification);
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
        return null; // Δεν παρέχουμε binding
    }

    private void initializeMediaPlayer() {
        if (currentTrackIndex < 0 || currentTrackIndex >= trackList.size()) {
            return; // Διασφάλιση έγκυρου δείκτη
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        mediaPlayer.setOnCompletionListener(mp -> {
            // Μπορείς να προσθέσεις λογική για να προχωρήσεις στο επόμενο κομμάτι αν χρειάζεται
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size();
            initializeMediaPlayer(); // Αρχικοποίηση του MediaPlayer με το νέο κομμάτι
            mediaPlayer.start();
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
