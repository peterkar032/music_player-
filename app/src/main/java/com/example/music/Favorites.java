package com.example.music;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Favorites extends Fragment {

    private RecyclerView favoritesRecyclerView;
    private TrackAdapter trackAdapter;
    private List<Track> favoriteTracks;
    private DatabaseReference likesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        favoritesRecyclerView = rootView.findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteTracks = new ArrayList<>();

        // Δημιουργία του TrackAdapter με σωστό OnItemClickListener
        trackAdapter = new TrackAdapter(favoriteTracks, new TrackAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Track track) {
                // Δημιουργία YouTube URL για αναζήτηση
                String query = track.getTitle() + " " + track.getArtist();
                String url = "https://www.youtube.com/results?search_query=" + Uri.encode(query);

                // Πρόθεση για άνοιγμα στον browser
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setPackage("com.android.chrome"); // Χρήση του Chrome browser

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // Fallback: Άνοιγμα στον προεπιλεγμένο browser αν δεν υπάρχει ο Chrome
                    intent.setPackage(null);
                    startActivity(intent);
                }
            }
        }, getContext());

        favoritesRecyclerView.setAdapter(trackAdapter);

        loadFavoritesFromFirebase();

        return rootView;
    }

    private void loadFavoritesFromFirebase() {
        likesRef = FirebaseDatabase.getInstance().getReference("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteTracks.clear();
                for (DataSnapshot trackSnapshot : snapshot.getChildren()) {
                    Track track = trackSnapshot.getValue(Track.class);
                    if (track != null) {
                        favoriteTracks.add(track);
                    }
                }
                trackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading favorites: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
