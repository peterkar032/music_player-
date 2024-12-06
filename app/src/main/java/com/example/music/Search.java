package com.example.music;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Search extends Fragment {
    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;
    private EditText searchEditText;
    private MediaPlayer mediaPlayer;
    private Button playPauseButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progressBar);
        searchEditText = view.findViewById(R.id.searchEditText);
        Button searchButton = view.findViewById(R.id.searchButton);
        playPauseButton = view.findViewById(R.id.PlayPauseButton);

        // Initialize RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass the context to the adapter and provide the listener
        trackAdapter = new TrackAdapter(trackList, new TrackAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Track track) {
                displayTrackDetails(view, track);
                playTrack(track);
            }
        }, getContext()); // Προσθέσαμε την παράμετρο boolean


        recyclerView.setAdapter(trackAdapter);

        // Set search button click listener
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
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

        // Play/Pause button click listener
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

        return view;
    }

    private void displayTrackDetails(View view, Track track) {
        // Update album art
        ImageView albumArtImageView = view.findViewById(R.id.imageView2);
        if (track.getAlbumArtUrl() != null && !track.getAlbumArtUrl().isEmpty()) {
            Picasso.get()
                    .load(track.getAlbumArtUrl())
                    .placeholder(R.drawable.music)
                    .error(R.drawable.music1)
                    .into(albumArtImageView);
        } else {
            albumArtImageView.setImageResource(R.drawable.play);
        }

        // Update song title
        TextView songTitleTextView = view.findViewById(R.id.SongTitle);
        songTitleTextView.setText(track.getTitle());

        // Update artist
        TextView artistTitleTextView = view.findViewById(R.id.ArtistTitle);
        artistTitleTextView.setText(track.getArtist());

        // Add click listener for the artist's name
        artistTitleTextView.setOnClickListener(v -> {
            String artistName = track.getArtist();
            searchArtistOnWeb(artistName);  // Open browser to search for the artist
        });
    }

    // Method to open a web search for the artist
    private void searchArtistOnWeb(String artistName) {
        // Construct the search query URL
        String searchQuery = "https://www.google.com/search?q=" + Uri.encode(artistName);
        // Create an Intent to open the search query in the browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchQuery));
        startActivity(intent);  // Open the web browser
    }

    private void playTrack(Track track) {
        // Stop the previous track if playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // Play track from URL
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(track.getTrackUrl());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                playPauseButton.setText("Pause");
                Toast.makeText(getContext(), "Playing: " + track.getTitle() + " by " + track.getArtist(), Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Toast.makeText(getContext(), "Track Finished", Toast.LENGTH_SHORT).show();
                playPauseButton.setText("Play");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(getContext(), "Error playing track", Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchTracks(String query) {
        progressBar.setVisibility(View.VISIBLE);
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
                        } else {
                            Log.d("Search Response", "No tracks found for query: " + query);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}