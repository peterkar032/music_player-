package com.example.music;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // Κατάσταση αναπαραγωγής/παύσης
    private int currentTrackIndex = 0; // Τρέχον κομμάτι
    private List<Integer> trackList = new ArrayList<>(); // Λίστα τραγουδιών
    private List<String> trackTitles = new ArrayList<>(); // Λίστα τίτλων τραγουδιών

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Φόρτωση κομματιών από τον φάκελο raw
        loadTracks();

        // Ρύθμιση του πρώτου κομματιού
        initializeMediaPlayer();

        // Διαχείριση των insets για edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (v.getPaddingTop() == 0) {
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });

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

    private void loadTracks() {
        // Χρήση reflection για την ανάκτηση όλων των μουσικών αρχείων από το φάκελο raw
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
                // Προσθέστε τον τίτλο του τραγουδιού στη λίστα (προσαρμόστε τους τίτλους όπως χρειάζεται)
                trackTitles.add(field.getName().replace("_", " ")); // Για παράδειγμα, αν τα ονόματα των αρχείων είναι τα ονόματα των κομματιών
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

        // Ενημέρωση του TextView με τον τίτλο του τραγουδιού
        updateSongTitle();
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            initializeMediaPlayer();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Playing Track " + (currentTrackIndex + 1), Toast.LENGTH_SHORT).show();
            updateSongTitle(); // Ενημέρωση τίτλου τραγουδιού κατά την αναπαραγωγή
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size(); // Περιστροφική εναλλαγή
        initializeMediaPlayer();
        playMusic();
    }

    private void previousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size(); // Περιστροφική εναλλαγή
        initializeMediaPlayer();
        playMusic();
    }

    private void updateSongTitle() {
        TextView songTitleTextView = findViewById(R.id.SongTitle); // Αναφορά στο TextView τίτλου τραγουδιού
        songTitleTextView.setText(trackTitles.get(currentTrackIndex)); // Ενημέρωση με τον τίτλο του τρέχοντος κομματιού
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
}
