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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private int currentTrackIndex = 0;
    private List<String> trackUrls;  // Λίστα URLs για streaming
    private List<String> trackTitles;  // Λίστα τίτλων τραγουδιών
    private List<String> albumArtUrls;  // Λίστα URLs για εξώφυλλα άλμπουμ (αν υπάρχουν)

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        trackUrls = new ArrayList<>();   // Εδώ θα αποθηκεύεις τα URLs
        trackTitles = new ArrayList<>(); // Εδώ θα αποθηκεύεις τους τίτλους των τραγουδιών
        albumArtUrls = new ArrayList<>(); // Εδώ θα αποθηκεύεις τα URLs των εξώφυλλων
        initializeMediaPlayer();  // Αρχικοποίηση του MediaPlayer
        mediaPlayer.setLooping(true); // Ενεργοποίηση του loop για συνεχή αναπαραγωγή
        startForeground(1, createNotification());  // Ξεκινάμε την υπηρεσία ως foreground
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Δημιουργία της ειδοποίησης για το foreground service
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Playing")
                .setContentText("Track " + (currentTrackIndex + 1))  // Ενημέρωση τίτλου τραγουδιού
                .setSmallIcon(R.drawable.music1)  // Εικονίδιο για την ειδοποίηση
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
                initializeMediaPlayer();  // Αρχικοποίηση με το νέο τραγούδι
            }
            mediaPlayer.start();  // Ξεκινάμε την αναπαραγωγή
        }
        return START_STICKY;  // Εξασφαλίζει ότι η υπηρεσία θα παραμείνει ζωντανή
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopSelf();  // Σταματάμε την υπηρεσία όταν η εφαρμογή κλείνει
    }

    private void initializeMediaPlayer() {
        if (currentTrackIndex < 0 || currentTrackIndex >= trackUrls.size()) {
            return;  // Διασφαλίζει ότι ο δείκτης είναι έγκυρος
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        String trackUrl = trackUrls.get(currentTrackIndex);  // Λαμβάνουμε το URL του τραγουδιού
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(trackUrl);  // Χρησιμοποιούμε το URL για το streaming
            mediaPlayer.prepareAsync();  // Προετοιμασία για αναπαραγωγή ασύγχρονα
            mediaPlayer.setOnPreparedListener(mp -> mp.start());  // Ξεκινάμε την αναπαραγωγή όταν είναι έτοιμο
            mediaPlayer.setOnCompletionListener(mp -> {
                currentTrackIndex = (currentTrackIndex + 1) % trackUrls.size();
                initializeMediaPlayer();  // Επαναφορά του media player με το νέο τραγούδι
                mediaPlayer.start();  // Αναπαραγωγή του επόμενου τραγουδιού
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ενημέρωση της κύριας δραστηριότητας για τον τίτλο του τραγουδιού
        Intent intent = new Intent("TRACK_TITLE_UPDATE");
        intent.putExtra("track_title", trackTitles.get(currentTrackIndex));
        sendBroadcast(intent);  // Στέλνουμε το broadcast στην MainActivity

        // Ενημέρωση της ειδοποίησης για το τρέχον τραγούδι
        startForeground(1, createNotification());
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
        return null;  // Δεν παρέχουμε binding
    }

    // Μέθοδος για την αναζήτηση τραγουδιών και την προσθήκη τους στα trackUrls, trackTitles και albumArtUrls
    public void searchAndLoadTracks(List<String> urls, List<String> titles, List<String> albumArts) {
        trackUrls = urls;
        trackTitles = titles;
        albumArtUrls = albumArts;
        currentTrackIndex = 0;  // Ξεκινάμε από το πρώτο τραγούδι
        initializeMediaPlayer();  // Αρχικοποιούμε τον MediaPlayer για το πρώτο τραγούδι
    }
}
