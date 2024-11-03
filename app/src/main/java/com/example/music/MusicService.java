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
                String action = intent.getAction();
                if ("ACTION_PLAY".equals(action)) {
                    playMusic();
                } else if ("ACTION_PAUSE".equals(action)) {
                    pauseMusic();
                } else if ("ACTION_NEXT".equals(action)) {
                    nextTrack();
                } else if ("ACTION_PREVIOUS".equals(action)) {
                    previousTrack();
                }
            }

            // Initialize and start the media player
            mediaPlayer.start();
            showNotification();
            return START_STICKY;
        }


        private void showNotification() {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Actions for play, pause, next, previous
            Intent playIntent = new Intent(this, MusicControlReceiver.class).setAction("ACTION_PLAY");
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent pauseIntent = new Intent(this, MusicControlReceiver.class).setAction("ACTION_PAUSE");
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent nextIntent = new Intent(this, MusicControlReceiver.class).setAction("ACTION_NEXT");
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent previousIntent = new Intent(this, MusicControlReceiver.class).setAction("ACTION_PREVIOUS");
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(this, 3, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Music Playing")
                    .setContentText("Track " + (currentTrackIndex + 1))
                    .setSmallIcon(R.drawable.music1)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.play, "Play", playPendingIntent)
                    .addAction(R.drawable.pause, "Pause", pausePendingIntent)
                    .addAction(R.drawable.next, "Next", nextPendingIntent)
                    .addAction(R.drawable.previous, "Previous", previousPendingIntent)
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
            return null; // We don't provide binding
        }

        public void playMusic() {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

        public void pauseMusic() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }

        public void nextTrack() {
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size();
            initializeMediaPlayer();
            playMusic();
            showNotification(); // Update notification with new track info
        }

        public void previousTrack() {
            currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size();
            initializeMediaPlayer();
            playMusic();
            showNotification(); // Update notification with new track info
        }

        private void initializeMediaPlayer() {
            if (currentTrackIndex < 0 || currentTrackIndex >= trackList.size()) {
                return; // Ensure index is valid
            }

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
            mediaPlayer.setOnCompletionListener(mp -> nextTrack()); // Auto-play next track
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
