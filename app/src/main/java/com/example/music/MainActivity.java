package com.example.music;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Εύρεση στοιχείων από το layout
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);
        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);

        // Αρχικοποίηση του RecyclerView και του adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackAdapter = new TrackAdapter(trackList, position -> {
            // Παίξε το τραγούδι όταν επιλεγεί
            Track selectedTrack = trackList.get(position);
            playTrack(selectedTrack);
        });
        recyclerView.setAdapter(trackAdapter);

        // Ορισμός του listener για το κουμπί αναζήτησης
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                try {
                    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
                    searchTracks(encodedQuery); // Καλούμε τη μέθοδο αναζήτησης με το κωδικοποιημένο query
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playTrack(Track track) {
        // Ελέγχουμε αν υπάρχει ήδη αναπαραγωγή τραγουδιού και το σταματάμε
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // Αναπαραγωγή από URL
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(track.getTrackUrl());  // Χρησιμοποιούμε το URL του τραγουδιού
            mediaPlayer.prepareAsync();  // Προετοιμασία για αναπαραγωγή (asynchronous)
            mediaPlayer.setOnPreparedListener(mp -> mp.start());  // Ξεκινάμε την αναπαραγωγή όταν είναι έτοιμο
            mediaPlayer.setOnCompletionListener(mp -> {
                Toast.makeText(MainActivity.this, "Track Finished", Toast.LENGTH_SHORT).show();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(MainActivity.this, "Error playing track", Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Playing: " + track.getTitle() + " by " + track.getArtist(), Toast.LENGTH_SHORT).show();
    }

    private void searchTracks(String query) {
        // Εμφάνιση του progressBar κατά την αναζήτηση
        progressBar.setVisibility(View.VISIBLE);

        // Δημιουργία του URL για την αναζήτηση μέσω του TheAudioDB API
        String url = "https://www.theaudiodb.com/api/v1/json/ADEEB/searchtrack.php?s=" + query;

        // Δημιουργία αιτήματος με χρήση του Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Προσθήκη log για να δούμε την απόκριση
                        Log.d("Search Response", "Response: " + response.toString());

                        try {
                            if (response.has("track")) {
                                JSONArray tracksArray = response.getJSONArray("track");

                                // Καθαρισμός της λίστας για να προσθέσουμε τα νέα τραγούδια
                                trackList.clear();

                                // Προσθήκη των τραγουδιών στην λίστα
                                for (int i = 0; i < tracksArray.length(); i++) {
                                    JSONObject trackJson = tracksArray.getJSONObject(i);
                                    String trackTitle = trackJson.getString("strTrack");
                                    String artist = trackJson.getString("strArtist");

                                    // Εδώ παίρνουμε το URL του εξώφυλλου (αν υπάρχει)
                                    String albumArtUrl = trackJson.optString("strTrackThumb", ""); // Επέλεξε το σωστό πεδίο

                                    // Δημιουργία και προσθήκη του τραγουδιού στη λίστα
                                    trackList.add(new Track(trackTitle, artist, "", albumArtUrl)); // Προσθέτουμε το albumArtUrl
                                }

                                // Ενημέρωση του RecyclerView με τα αποτελέσματα της αναζήτησης
                                trackAdapter.notifyDataSetChanged();
                            } else {
                                // Αν δεν υπάρχουν αποτελέσματα, εμφανίζουμε μήνυμα
                                Log.d("Search Response", "No tracks found for query: " + query);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Απόκρυψη του progressBar όταν ολοκληρωθεί η αναζήτηση
                        progressBar.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Διαχείριση σφαλμάτων στην αίτηση
                        Log.e("Search Error", "Error: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

        // Προσθήκη του αιτήματος στη σειρά αιτήσεων του Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}
