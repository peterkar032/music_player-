package com.example.music;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // Κατάσταση αναπαραγωγής/παύσης
    private int currentTrackIndex = 0; // Τρέχον κομμάτι
    private List<Integer> trackList = new ArrayList<>(); // Λίστα τραγουδιών

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Προσθήκη κομματιών στη λίστα
        trackList.add(R.raw.sample1);
        trackList.add(R.raw.sample2);
        trackList.add(R.raw.sample3);

        // Ρύθμιση του πρώτου κομματιού
        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));

        // Διαχείριση των insets για edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Αναφορά στα κουμπιά
        Button btnPlayPause = findViewById(R.id.PlayButton);
        Button btnNext = findViewById(R.id.NextButton);
        Button btnPrevious = findViewById(R.id.PrevButton);

        // Ρύθμιση listeners
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    btnPlayPause.setText("PLAY");
                    pauseMusic();
                } else {
                    btnPlayPause.setText("PAUSE");
                    playMusic();
                }
                isPlaying = !isPlaying; // Εναλλαγή κατάστασης αναπαραγωγής
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTrack();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousTrack();
            }
        });
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        }
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> nextTrack()); // Αυτόματη μετάβαση στο επόμενο κομμάτι
        Toast.makeText(this, "Playing Track " + (currentTrackIndex + 1), Toast.LENGTH_SHORT).show();
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size(); // Περιστροφική εναλλαγή
        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        playMusic();
    }

    private void previousTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size(); // Περιστροφική εναλλαγή
        mediaPlayer = MediaPlayer.create(this, trackList.get(currentTrackIndex));
        playMusic();
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
