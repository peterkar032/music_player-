package com.example.music;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
    private LinearLayout albumButtonsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        // Εύρεση στοιχείων UI
        albumButtonsContainer = view.findViewById(R.id.albumButtonsContainer);  // Το LinearLayout που περιέχει τα κουμπιά
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tracksContainer = view.findViewById(R.id.tracksContainer);

        // Ρύθμιση RecyclerView
        trackAdapter = new TrackAdapter(trackList, track -> {
            Toast.makeText(getContext(), "Track clicked: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        }
    , getContext());
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksRecyclerView.setAdapter(trackAdapter);

        // Χειριστές για τα κουμπιά άλμπουμ
        view.findViewById(R.id.rapAlbumImageButton).setOnClickListener(v -> {
            showTracksContainer();
            loadRapTracks();
        });

        view.findViewById(R.id.laikaAlbumImageButton).setOnClickListener(v -> {
            showTracksContainer();
            loadLaikaTracks();
        });

        view.findViewById(R.id.top100).setOnClickListener(v -> {
            showTracksContainer();
            loadTop100Tracks();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Χειριστής επιστροφής από την οθόνη
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (tracksContainer.getVisibility() == View.VISIBLE) {
                    hideTracksContainer();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void showTracksContainer() {
        albumButtonsContainer.setVisibility(View.GONE);  // Κρύβουμε τα κουμπιά
        tracksContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        tracksRecyclerView.setVisibility(View.GONE); // Αρχικά το RecyclerView είναι κρυφό
    }

    private void hideTracksContainer() {
        albumButtonsContainer.setVisibility(View.VISIBLE); // Επαναφορά των κουμπιών άλμπουμ
        tracksContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tracksRecyclerView.setVisibility(View.GONE);
    }

    private void loadRapTracks() {
        String url = "https://api.deezer.com/chart/116/tracks"; // URL για το rap album
        loadTracksFromApi(url);
    }

    private void loadLaikaTracks() {
        String url = "https://api.deezer.com/search?q=greek_laiko"; // URL για το laika album
        loadTracksFromApi(url);
    }

    private void loadTop100Tracks() {
        String url = "https://api.deezer.com/chart/0/tracks?limit=100"; // URL για τα κορυφαία 100 τραγούδια
        loadTracksFromApi(url);
    }

    private void loadTracksFromApi(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        progressBar.setVisibility(View.GONE); // Απόκρυψη του ProgressBar μόλις ληφθούν τα δεδομένα

                        if (response.has("data")) {
                            JSONArray tracksArray = response.getJSONArray("data");
                            trackList.clear(); // Καθαρισμός της λίστας πριν την προσθήκη νέων τραγουδιών

                            // Ανάγνωση των τραγουδιών από την απάντηση
                            for (int i = 0; i < tracksArray.length(); i++) {
                                JSONObject trackJson = tracksArray.getJSONObject(i);

                                String trackTitle = trackJson.optString("title", "Unknown Title");
                                String artist = trackJson.getJSONObject("artist").optString("name", "Unknown Artist");
                                String albumArtUrl = trackJson.getJSONObject("album").optString("cover_medium", "");
                                String trackUrl = trackJson.optString("preview", "");

                                trackList.add(new Track(trackTitle, artist, trackUrl, albumArtUrl));
                            }

                            // Ενημέρωση του RecyclerView με τα νέα δεδομένα
                            if (trackList.isEmpty()) {
                                Toast.makeText(getContext(), "No tracks found.", Toast.LENGTH_SHORT).show();
                            } else {
                                trackAdapter.notifyDataSetChanged();  // Ενημέρωση του Adapter για τα νέα δεδομένα
                                tracksRecyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), "No data found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("Response Error", "Error parsing response", e);
                        progressBar.setVisibility(View.GONE); // Απόκρυψη του ProgressBar σε περίπτωση λάθους
                        Toast.makeText(getContext(), "Error processing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Network Error", "Error: " + error.getMessage());
                    progressBar.setVisibility(View.GONE); // Απόκρυψη του ProgressBar σε περίπτωση λάθους δικτύου
                    Toast.makeText(getContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Προσθήκη της αίτησης στην ουρά του Volley
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
    }
}
