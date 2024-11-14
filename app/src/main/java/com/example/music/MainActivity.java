package com.example.music;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import android.media.MediaPlayer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;
    private EditText searchEditText;
    private MediaPlayer mediaPlayer;
    private Button playPauseButton;  // Κουμπί για αναπαραγωγή/παύση

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Εύρεση στοιχείων από το layout
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);
        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        playPauseButton = findViewById(R.id.PlayPauseButton);

        // Αρχικοποίηση του RecyclerView και του adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackAdapter = new TrackAdapter(trackList, track -> {
            // Παίξε το τραγούδι και εμφάνισε τα στοιχεία του
            displayTrackDetails(track);  // Εμφάνιση στοιχείων τραγουδιού στην κύρια οθόνη
            playTrack(track);  // Αναπαραγωγή του τραγουδιού
        });

        recyclerView.setAdapter(trackAdapter);

        // Listener για το κουμπί αναζήτησης
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                try {
                    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
                    searchTracks(encodedQuery);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener για το κουμπί αναπαραγωγής/παύσης
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setText("Play");
                } else {
                    mediaPlayer.start();
                    playPauseButton.setText("Pause");
                }
            }
        });
    }

    private void displayTrackDetails(Track track) {
        // Ενημέρωση εικόνας άλμπουμ
        ImageView albumArtImageView = findViewById(R.id.imageView2);
        if (track.getAlbumArtUrl() != null && !track.getAlbumArtUrl().isEmpty()) {
            Picasso.get()
                    .load(track.getAlbumArtUrl())
                    .placeholder(R.drawable.music)
                    .error(R.drawable.music1)
                    .into(albumArtImageView);
        } else {
            albumArtImageView.setImageResource(R.drawable.play);
        }

        // Ενημέρωση τίτλου τραγουδιού
        TextView songTitleTextView = findViewById(R.id.SongTitle);
        songTitleTextView.setText(track.getTitle());

        // Ενημέρωση καλλιτέχνη
        TextView artistTitleTextView = findViewById(R.id.ArtistTitle);
        artistTitleTextView.setText(track.getArtist());
    }

    private void playTrack(Track track) {
        // Σταματάμε την προηγούμενη αναπαραγωγή αν υπάρχει
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // Αναπαραγωγή από URL
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(track.getTrackUrl());
            mediaPlayer.prepareAsync(); // Προετοιμασία για αναπαραγωγή (async)

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                playPauseButton.setText("Pause");
                Toast.makeText(MainActivity.this, "Playing: " + track.getTitle() + " by " + track.getArtist(), Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Toast.makeText(MainActivity.this, "Track Finished", Toast.LENGTH_SHORT).show();
                playPauseButton.setText("Play");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(MainActivity.this, "Error playing track", Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchTracks(String query) {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://api.deezer.com/search?q=" + query;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressBar.setVisibility(View.GONE);
                            if (response.has("data")) {
                                JSONArray tracksArray = response.getJSONArray("data");
                                trackList.clear();

                                for (int i = 0; i < tracksArray.length(); i++) {
                                    JSONObject trackJson = tracksArray.getJSONObject(i);
                                    String trackTitle = trackJson.getString("title");
                                    String artist = trackJson.getJSONObject("artist").getString("name");
                                    String albumArtUrl = trackJson.getJSONObject("album").getString("cover_medium");
                                    String trackUrl = trackJson.getString("preview");

                                    trackList.add(new Track(trackTitle, artist, trackUrl, albumArtUrl));
                                }
                                trackAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("Search Response", "No tracks found for query: " + query);
                                Toast.makeText(MainActivity.this, "No tracks found.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("Response Error", "Error parsing response", e);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Error processing results", Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Search Error", "Error: " + error.getMessage());
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error retrieving data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
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
