package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Favorites extends Fragment {
    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;
    private List<Track> favoriteTracks = new ArrayList<>();
    private DatabaseReference likesRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);


        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        likesRef = FirebaseDatabase.getInstance().getReference("likes");

        trackAdapter = new TrackAdapter(favoriteTracks, new TrackAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Track track) {

            }
        }, getContext());

        recyclerView.setAdapter(trackAdapter);
        loadFavoriteTracks();

        return view;
    }

    private void loadFavoriteTracks() {
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
                Toast.makeText(getContext(), "Failed to load favorite tracks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Μέθοδος για το "Unlike"
    private void removeFromFavorites(Track track) {
        Query trackQuery = likesRef.orderByChild("title").equalTo(track.getTitle());
        trackQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    snapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Track removed from favorites.", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to remove track.", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Toast.makeText(getContext(), "Track not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Παράδειγμα λειτουργίας για το context menu με "Unlike"
    private void showContextMenu(View view, Track track) {
        view.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(Menu.NONE, R.id.menu_unlike, Menu.NONE, "Unlike").setOnMenuItemClickListener(item -> {
                removeFromFavorites(track); // Αφαίρεση από τα αγαπημένα
                return true;
            });
        });
    }
}