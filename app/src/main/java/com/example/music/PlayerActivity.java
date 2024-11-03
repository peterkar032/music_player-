package com.example.music;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private int currentTrackIndex;
    private String songTitle;
    private SeekBar seekBar;
    private List<Integer> trackList;
    private ImageView albumArt; // Πρόσθετο ImageView για την εικόνα του άλμπουμ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Λάβετε τα δεδομένα από το Intent
        currentTrackIndex = getIntent().getIntExtra("TRACK_INDEX", 0);
        songTitle = getIntent().getStringExtra("SONG_TITLE");
        trackList = getIntent().getIntegerArrayListExtra("TRACK_LIST"); // Ανάκτηση της λίστας

        // Βρείτε τις αναφορές UI
        TextView songTitleTextView = findViewById(R.id.songTitle);
        albumArt = findViewById(R.id.albumArt); // Ανάκτηση του ImageView
        songTitleTextView.setText(songTitle);

        // Ρυθμίστε την εικόνα του άλμπουμ
        albumArt.setImageResource(R.drawable.music); // Χρησιμοποιήστε την σταθερή εικόνα σας

        // Ρύθμιση του MediaPlayer
        mediaPlayer = MediaPlayer.create(this, getTrackResource(currentTrackIndex));
        setupSeekBar();

        // Ρυθμίσεις κουμπιών
        Button playButton = findViewById(R.id.playButton);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button prevButton = findViewById(R.id.prevButton);

        playButton.setOnClickListener(v -> playMusic());
        pauseButton.setOnClickListener(v -> pauseMusic());
        nextButton.setOnClickListener(v -> nextTrack());
        prevButton.setOnClickListener(v -> previousTrack());
    }

    private void setupSeekBar() {
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(mediaPlayer.getDuration());

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBar.setProgress(0);
            nextTrack();
        });

        // Ενημέρωση SeekBar κατά την αναπαραγωγή
        new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(() -> {
                        if (mediaPlayer != null) {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private int getTrackResource(int index) {
        return trackList.get(index); // Χρησιμοποιήστε τη μεταβιβασμένη λίστα
    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Playing: " + songTitle, Toast.LENGTH_SHORT).show();
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
        updateTrack();
    }

    private void previousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size();
        updateTrack();
    }

    private void updateTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, getTrackResource(currentTrackIndex));
        mediaPlayer.start();

        // Ενημέρωση του τίτλου του τραγουδιού
        songTitle = trackList.get(currentTrackIndex).toString(); // Χρησιμοποιήστε τη μεταβιβασμένη λίστα
        Toast.makeText(this, "Now Playing: " + songTitle, Toast.LENGTH_SHORT).show();

        setupSeekBar(); // Επαναφέρετε το SeekBar
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
