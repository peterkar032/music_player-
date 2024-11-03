package com.example.music;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private int currentTrackIndex;
    private String songTitle;
    private SeekBar seekBar;
    private List<Integer> trackList;
    private List<String> trackTitles;
    private ImageView albumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        MediaPlayer mediaPlayer = MediaPlayerManager.getMediaPlayer();

        currentTrackIndex = getIntent().getIntExtra("TRACK_INDEX", 0);
        songTitle = getIntent().getStringExtra("SONG_TITLE");
        trackList = getIntent().getIntegerArrayListExtra("TRACK_LIST");
        trackTitles = getIntent().getStringArrayListExtra("TRACK_TITLES");

        TextView songTitleTextView = findViewById(R.id.songTitle);
        albumArt = findViewById(R.id.albumArt);
        songTitleTextView.setText(songTitle);
        albumArt.setImageResource(R.drawable.music);

        setupSeekBar(mediaPlayer);

        Button playButton = findViewById(R.id.playButton);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button prevButton = findViewById(R.id.prevButton);

        playButton.setOnClickListener(v -> MediaPlayerManager.play(this));
        pauseButton.setOnClickListener(v -> MediaPlayerManager.pause());
        nextButton.setOnClickListener(v -> {
            MediaPlayerManager.nextTrack(this);
            updateSongTitle();
        });
        prevButton.setOnClickListener(v -> {
            MediaPlayerManager.previousTrack(this);
            updateSongTitle();
        });
    }

    private void setupSeekBar(MediaPlayer mediaPlayer) {
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(mediaPlayer.getDuration());

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBar.setProgress(0);
            MediaPlayerManager.nextTrack(this);
            updateSongTitle();
        });

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

    private void updateSongTitle() {
        TextView songTitleTextView = findViewById(R.id.songTitle);
        songTitle = trackTitles.get(MediaPlayerManager.getCurrentTrackIndex());
        songTitleTextView.setText(songTitle);
        Toast.makeText(this, "Now Playing: " + songTitle, Toast.LENGTH_SHORT).show();
    }
}
