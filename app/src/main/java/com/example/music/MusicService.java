package com.example.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.media.MediaPlayer;
import androidx.core.app.NotificationCompat;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private int currentTrackIndex = 0;
    private List<Integer> trackList;
    private List<String> trackTitles;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        loadTracks(); // Φορτώνει τη λίστα τραγουδιών
        initializeMediaPlayer(); // Αρχικοποιεί το MediaPlayer
        mediaPlayer.setLooping(true); // Ενεργοποιεί το loop για συνεχή αναπαραγωγή

        // Ξεκινάμε την υπηρεσία ως foreground με ειδοποίηση
        startForeground(1, createNotification());
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Playing")
                .setContentText("Track " + (currentTrackIndex + 1)) // Ενημέρωση τίτλου τραγουδιού
                .setSmallIcon(R.drawable.music1)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
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
        }

        return START_STICKY; // Επιτρέπει στην υπηρεσία να παραμείνει ενεργή ακόμα και αν το σύστημα την σταματήσει
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Σταματάμε τη μουσική όταν η εφαρμογή κλείνει
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopSelf(); // Σταματάμε την υπηρεσία
    }

    private void loadTracks() {
        trackList = new ArrayList<>();
        trackTitles = new ArrayList<>();

        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
                trackTitles.add(field.getName().replace("_", " ")); // Ενημερώνει τη λίστα με τον τίτλο του τραγουδιού
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void initializeMediaPlayer() {
        if (currentTrackIndex < 0 || currentTrackIndex >= trackList.size()) {
            return; // Διασφάλιση έγκυρου δείκτη
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex)); // Αναπαράγει το επιλεγμένο τραγούδι
        mediaPlayer.setOnCompletionListener(mp -> {
            // Όταν το τραγούδι τελειώνει, μεταβαίνουμε στο επόμενο
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size();
            initializeMediaPlayer(); // Αρχικοποίηση του MediaPlayer με το νέο κομμάτι
            mediaPlayer.start();
        });

        // Ενημερώνουμε την κύρια δραστηριότητα για τον τίτλο του τραγουδιού
        Intent intent = new Intent("TRACK_TITLE_UPDATE");
        intent.putExtra("track_title", trackTitles.get(currentTrackIndex));
        sendBroadcast(intent); // Στέλνουμε το broadcast στην MainActivity
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

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Δεν παρέχουμε binding
    }
}
