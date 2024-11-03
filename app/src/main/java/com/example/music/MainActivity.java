package com.example.music;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

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

        // Αρχικοποίηση του RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();

        // Φόρτωση κομματιών από τον φάκελο raw
        loadTracks();

        // Ρύθμιση των listeners για τα κουμπιά
        Button btnPlayPause = findViewById(R.id.PlayPauseButton);
        Button btnNext = findViewById(R.id.NextButton);
        Button btnPrevious = findViewById(R.id.PrevButton);

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                btnPlayPause.setText("PLAY");
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
        // Καθαρίστε τη λίστα πριν την προσθήκη των τραγουδιών
        trackList.clear();
        trackTitles.clear();

        // Χρήση reflection για την ανάκτηση όλων των μουσικών αρχείων από το φάκελο raw
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

    private void startMusicService() {
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra("track_index", currentTrackIndex); // Μεταφορά του τρέχοντος δείκτη κομματιού
        startService(serviceIntent); // Εκκίνηση της υπηρεσίας μουσικής
        playMusic(); // Αναπαραγωγή μουσικής
    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Αναπαραγωγή Κομματιού " + (currentTrackIndex + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size(); // Περιστροφική εναλλαγή
        startMusicService(); // Εκκίνηση υπηρεσίας μουσικής για το επόμενο κομμάτι
    }

    private void previousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size(); // Περιστροφική εναλλαγή
        startMusicService(); // Εκκίνηση υπηρεσίας μουσικής για το προηγούμενο κομμάτι
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Παύση της αναπαραγωγής
            Button btnPlayPause = findViewById(R.id.PlayPauseButton);
            btnPlayPause.setText("PLAY"); // Ενημέρωση του κουμπιού
            isPlaying = false; // Κατάσταση μη αναπαραγωγής
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Απελευθέρωση πόρων
            mediaPlayer = null; // Μηδενισμός της αναφοράς
        }
    }
}
