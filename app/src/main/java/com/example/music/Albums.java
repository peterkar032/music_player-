package com.example.music;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Albums extends Fragment {

    private RecyclerView tracksRecyclerView;
    private ProgressBar progressBar;
    private TrackAdapter trackAdapter;
    private List<Track> trackList = new ArrayList<>();
    private FrameLayout tracksContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        // Εύρεση στοιχείων UI
        ImageButton laikaAlbumImageButton = view.findViewById(R.id.laikaAlbumImageButton);
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tracksContainer = view.findViewById(R.id.tracksContainer);

        // Ρύθμιση RecyclerView
        trackAdapter = new TrackAdapter(trackList, track -> {
            // Ενέργεια για επιλογή τραγουδιού
            Toast.makeText(getContext(), "Playing: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        });
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksRecyclerView.setAdapter(trackAdapter);

        // Όταν πατηθεί το κουμπί-εικόνα του άλμπουμ
        laikaAlbumImageButton.setOnClickListener(v -> showTracksContainer());

        // Όταν πατηθεί το υπόβαθρο, κρύβεται η λίστα τραγουδιών
        tracksContainer.setOnClickListener(v -> hideTracksContainer());

        return view;
    }

    // Εμφάνιση λίστας τραγουδιών
    private void showTracksContainer() {
        tracksContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        tracksRecyclerView.setVisibility(View.GONE);

        loadRockTracks(); // Αλλαγή στη μέθοδο για ροκ τραγούδια
    }

    // Απόκρυψη λίστας τραγουδιών
    private void hideTracksContainer() {
        tracksContainer.setVisibility(View.GONE);
    }

    // Φόρτωση ροκ τραγουδιών από το Deezer API
    private void loadRockTracks() {
        String query = "Rock"; // Αναζήτηση για ροκ τραγούδια
        String url = "https://api.deezer.com/search?q=" + query;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
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
                            tracksRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getContext(), "No tracks found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("Response Error", "Error parsing response", e);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error processing results", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Search Error", "Error: " + error.getMessage());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error retrieving data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
    }
}
