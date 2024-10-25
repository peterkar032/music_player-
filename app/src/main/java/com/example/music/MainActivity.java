package com.example.music;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private boolean isPlaying = false; //για την κατάσταση play-pause


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Εύρεση των κουμπιών
        Button btnPlayPause = findViewById(R.id.btn_play_pause);
        Button btnNext = findViewById(R.id.btn_next);
        Button btnPrevious = findViewById(R.id.btn_previous);
        // Listener για το κουμπί Play/Pause
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    // Παύση μουσικής
                    btnPlayPause.setText("Play");
                    pauseMusic();
                } else {
                    // Έναρξη μουσικής
                    btnPlayPause.setText("Pause");
                    playMusic();
                }
                isPlaying = !isPlaying;  // Toggle κατάστασης
            }
        });

        // Listener για το κουμπί Next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTrack();
            }
        });

        // Listener για το κουμπί Previous
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousTrack();
            }
        });
    }

    private void playMusic() {
        // Λογική για την έναρξη της μουσικής
    }

    private void pauseMusic() {
        // Λογική για την παύση της μουσικής
    }

    private void nextTrack() {
        // Λογική για την μετάβαση στο επόμενο κομμάτι
    }

    private void previousTrack() {
        // Λογική για την επιστροφή στο προηγούμενο κομμάτ
    }
}