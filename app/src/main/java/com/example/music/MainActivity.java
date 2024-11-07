package com.example.music;


import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.view.MenuItem;
import android.view.Menu;

import com.google.android.material.tabs.TabLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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


    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    private boolean isPlaying = false; // Κατάσταση αναπαραγωγής
    private int currentTrackIndex = 0; // Τρέχων δείκτης κομματιού
    private List<Integer> trackList = new ArrayList<>(); // Λίστα κομματιών
    private List<String> trackTitles = new ArrayList<>(); // Τίτλοι κομματιών
    private RecyclerView recyclerView; // RecyclerView για εμφάνιση κομματιών


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this); // Δυνατότητα edge-to-edge

        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {


        loadTracks();
        MediaPlayerManager.initialize(this, trackList, currentTrackIndex);

        playerContainer = findViewById(R.id.player_container);
        gestureDetector = new GestureDetector(this, new GestureListener());

        setupPlayerClickListener();

        Button btnPlayPause = findViewById(R.id.PlayButton);
        Button btnNext = findViewById(R.id.NextButton);
        Button btnPrevious = findViewById(R.id.PrevButton);
        TextView songTitleTextView = findViewById(R.id.SongTitle);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        // Αρχικοποίηση του RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();

        // Φόρτωση κομματιών από τον φάκελο raw
        loadTracks();

        // Ρύθμιση των listeners για τα κουμπιά
        Button btnPlayPause = findViewById(R.id.PlayPauseButton);
        Button btnNext = findViewById(R.id.NextButton);
        Button btnPrevious = findViewById(R.id.PrevButton);

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
                stopService(new Intent(this, MusicService.class));
                isPlaying = false;
            } else {
                btnPlayPause.setText("PAUSE");
                startMusicService(); // Εκκίνηση υπηρεσίας μουσικής
                isPlaying = true;
            }
        });

        btnNext.setOnClickListener(v -> nextTrack());
        btnPrevious.setOnClickListener(v -> previousTrack());

        // Ρύθμιση window insets για padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (v.getPaddingTop() == 0) {
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });
    }

    private void setupRecyclerView() {
        TrackAdapter adapter = new TrackAdapter(trackTitles, position -> {
            currentTrackIndex = position; // Ενημέρωση του δείκτη κομματιού
            startMusicService(); // Εκκίνηση υπηρεσίας μουσικής
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter); // Ρύθμιση του adapter
    }


    private void loadTracks() {
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
                trackTitles.add(field.getName().replace("_", " ")); // Προσθέστε τον τίτλο του τραγουδιού
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
