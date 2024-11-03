package com.example.music;

import android.content.Intent;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.view.View;

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

    private SeekBar seekBar;


    private android.os.Handler handler = new android.os.Handler();

    private final  Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                seekBar.setProgress((int) (100.0 * currentPosition / duration));
                handler.postDelayed(this, 1000);
            }
        }
    };


    private boolean isPlaying = false; // Playback state
    private int currentTrackIndex = 0; // Current track index
    private List<Integer> trackList = new ArrayList<>(); // Track list
    private List<String> trackTitles = new ArrayList<>(); // Track titles

    private RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadTracks();
        MediaPlayerManager.initialize(this, trackList, currentTrackIndex);

        playerContainer = findViewById(R.id.player_container);
        gestureDetector = new GestureDetector(this, new GestureListener());

        setupPlayerClickListener();



        seekBar = findViewById(R.id.seekBar);



        // Καθαρίστε τις λίστες πριν την προσθήκη νέων τραγουδιών
        recycleList.clear();
        trackTitles.clear();


        // Φόρτωση κομματιών από τον φάκελο raw
        loadTracks();
        initializeMediaPlayer();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (v.getPaddingTop() == 0) {
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });



        // Αναφορά στα κουμπιά

        Button btnPlayPause = findViewById(R.id.PlayButton);

        Button btnPlayPause = findViewById(R.id.PlayPauseButton);

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


        // Αναφορά στο ListView και ρύθμιση της λίστας τραγουδιών
        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();


        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress * mediaPlayer.getDuration() / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });




        // Ρύθμιση listeners

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                btnPlayPause.setText("PLAY");
                stopService(new Intent(this, MusicService.class));
                isPlaying = false;
            } else {
                btnPlayPause.setText("PAUSE");
                startMusicService();
                isPlaying = true;
            }

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
    private void setupRecyclerView() {
    TrackAdapter adapter = new TrackAdapter(trackTitles, position -> {
        currentTrackIndex = position;
        initializeMediaPlayer();
        playMusic();
    });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
}



    private void loadTracks() {


        // Καθαρίστε τη λίστα πριν την προσθήκη των τραγουδιών
        recycleList.clear();
        trackTitles.clear();

        // Χρήση reflection για την ανάκτηση όλων των μουσικών αρχείων από το φάκελο raw

        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);

                trackTitles.add(field.getName().replace("_", " "));


                // Προσθέστε τον τίτλο του τραγουδιού στη λίστα (προσαρμόστε τους τίτλους όπως χρειάζεται)
                trackTitles.add(field.getName().replace("_", " ")); // Για παράδειγμα, αν τα ονόματα των αρχείων είναι τα ονόματα των κομματιών
                handler.removeCallbacks(updateSeekBar);

                recycleList.add(resId);
                // Προσθέστε τον τίτλο του τραγουδιού στη λίστα
                trackTitles.add(field.getName().replace("_", " "));


            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    private void updateSongTitle() {
        TextView songTitleTextView = findViewById(R.id.SongTitle);
        songTitleTextView.setText(trackTitles.get(MediaPlayerManager.getCurrentTrackIndex()));

    private void initializeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, recycleList.get(currentTrackIndex));
        mediaPlayer.setOnCompletionListener(mp -> nextTrack());
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Error occurred during playback", Toast.LENGTH_SHORT).show();
            return true;
        });
        updateSongTitle();
    }

    private void startMusicService() {
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra("track_index", currentTrackIndex);
        startService(serviceIntent);
        playMusic();
    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            handler.postDelayed(updateSeekBar, 0);
            Toast.makeText(this, "Playing Track " + (currentTrackIndex + 1), Toast.LENGTH_SHORT).show();
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

        currentTrackIndex = (currentTrackIndex + 1) % recycleList.size(); // Περιστροφική εναλλαγή
        initializeMediaPlayer();
        playMusic();
    }

    private void previousTrack() {

        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size();

        currentTrackIndex = (currentTrackIndex - 1 + recycleList.size()) % recycleList.size(); // Περιστροφική εναλλαγή

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

        if (MediaPlayerManager.isPlaying()) {
            MediaPlayerManager.pause();
            Button btnPlayPause = findViewById(R.id.PlayButton);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pauseMusic();
            Button btnPlayPause = findViewById(R.id.PlayPauseButton);

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
