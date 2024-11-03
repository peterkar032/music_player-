package com.example.music;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean isPlaying = false;
    private int currentTrackIndex = 0;
    private List<Integer> trackList = new ArrayList<>();
    private List<String> trackTitles = new ArrayList<>();
    private LinearLayout playerContainer;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadTracks();
        MediaPlayerManager.initialize(this, trackList, currentTrackIndex);

        playerContainer = findViewById(R.id.player_container);
        gestureDetector = new GestureDetector(this, new GestureListener());

        setupPlayerClickListener();

        Button btnPlayPause = findViewById(R.id.PlayButton);
        Button btnNext = findViewById(R.id.NextButton);
        Button btnPrevious = findViewById(R.id.PrevButton);
        TextView songTitleTextView = findViewById(R.id.SongTitle);

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                btnPlayPause.setText("PLAY");
                MediaPlayerManager.pause();
            } else {
                btnPlayPause.setText("PAUSE");
                MediaPlayerManager.play(this);
            }
            isPlaying = !isPlaying;
        });

        btnNext.setOnClickListener(v -> {
            MediaPlayerManager.nextTrack(this);
            updateSongTitle();
        });

        btnPrevious.setOnClickListener(v -> {
            MediaPlayerManager.previousTrack(this);
            updateSongTitle();
        });
    }

    private void setupPlayerClickListener() {
        playerContainer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("TRACK_INDEX", currentTrackIndex);
            intent.putExtra("SONG_TITLE", trackTitles.get(currentTrackIndex));
            intent.putIntegerArrayListExtra("TRACK_LIST", new ArrayList<>(trackList)); // Προσθήκη της λίστας των resource IDs
            intent.putStringArrayListExtra("TRACK_TITLES", new ArrayList<>(trackTitles)); // Προσθήκη της λίστας τίτλων
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

    private void updateSongTitle() {
        TextView songTitleTextView = findViewById(R.id.SongTitle);
        songTitleTextView.setText(trackTitles.get(MediaPlayerManager.getCurrentTrackIndex()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaPlayerManager.isPlaying()) {
            MediaPlayerManager.pause();
            Button btnPlayPause = findViewById(R.id.PlayButton);
            btnPlayPause.setText("PLAY");
            isPlaying = false;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getY() > e2.getY()) {
                expandPlayer();
            } else if (e1.getY() < e2.getY()) {
                collapsePlayer();
            }
            return true;
        }

        private void expandPlayer() {
            playerContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }

        private void collapsePlayer() {
            playerContainer.animate()
                    .translationY(playerContainer.getHeight())
                    .setDuration(300)
                    .start();
        }
    }
}
