package com.example.music;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // Κατάσταση αναπαραγωγής/παύσης
    private int currentTrackIndex = 0; // Τρέχον κομμάτι
    private List<Integer> trackList = new ArrayList<>(); // Λίστα τραγουδιών
    private List<String> trackTitles = new ArrayList<>(); // Λίστα τίτλων τραγουδιών
    private LinearLayout playerContainer;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loadTracks();
        initializeMediaPlayer();

        playerContainer = findViewById(R.id.player_container);
        gestureDetector = new GestureDetector(this, new GestureListener());

        setupPlayerClickListener();

        // Αναφορά στα κουμπιά
        Button btnPlayPause = findViewById(R.id.PlayButton);
        Button btnNext = findViewById(R.id.NextButton);
        Button btnPrevious = findViewById(R.id.PrevButton);
        TextView songTitleTextView = findViewById(R.id.SongTitle); // Αναφορά στο TextView τίτλου τραγουδιού

        // Ρύθμιση listeners
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                btnPlayPause.setText("PLAY");
                pauseMusic();
            } else {
                btnPlayPause.setText("PAUSE");
                playMusic();
            }
            isPlaying = !isPlaying; // Εναλλαγή κατάστασης αναπαραγωγής
        });

        btnNext.setOnClickListener(v -> nextTrack());
        btnPrevious.setOnClickListener(v -> previousTrack());
    }

    private void setupPlayerClickListener() {
        playerContainer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("TRACK_INDEX", currentTrackIndex);
            intent.putExtra("SONG_TITLE", trackTitles.get(currentTrackIndex));
            intent.putIntegerArrayListExtra("TRACK_LIST", new ArrayList<>(trackList)); // Προσθήκη της λίστας
            startActivity(intent);
        });
    }


    private void loadTracks() {
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
                trackTitles.add(field.getName().replace("_", " "));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        mediaPlayer.setOnCompletionListener(mp -> nextTrack());
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Error occurred during playback", Toast.LENGTH_SHORT).show();
            return true;
        });

        updateSongTitle();
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            initializeMediaPlayer();
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Toast.makeText(this, "Playing Track " + (currentTrackIndex + 1), Toast.LENGTH_SHORT).show();
            updateSongTitle();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size();
        initializeMediaPlayer();
        playMusic();
    }

    private void previousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size();
        initializeMediaPlayer();
        playMusic();
    }

    private void updateSongTitle() {
        TextView songTitleTextView = findViewById(R.id.SongTitle);
        songTitleTextView.setText(trackTitles.get(currentTrackIndex));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pauseMusic();
            Button btnPlayPause = findViewById(R.id.PlayButton);
            btnPlayPause.setText("PLAY");
            isPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public List<Integer> getTrackList() {
        return trackList; // Επιστρέφει τη λίστα τραγουδιών
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getY() > e2.getY()) {
                // Ο χρήστης σύρει προς τα πάνω
                expandPlayer();
            } else if (e1.getY() < e2.getY()) {
                // Ο χρήστης σύρει προς τα κάτω
                collapsePlayer();
            }
            return true;
        }

        private void expandPlayer() {
            // Μεγέθυνση του player
            playerContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }

        private void collapsePlayer() {
            // Μείωση του player
            playerContainer.animate()
                    .translationY(playerContainer.getHeight())
                    .setDuration(300)
                    .start();
        }
    }
}
