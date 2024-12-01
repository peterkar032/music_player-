package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class Playlists extends Fragment {

    private RecyclerView playlistsRecyclerView;
    private PlaylistAdapter playlistAdapter;
    private List<Playlist> playlistList;
    private DatabaseReference playlistsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlists, container, false);

        // Αντίστοιχο RecyclerView
        playlistsRecyclerView = rootView.findViewById(R.id.playlistsRecyclerView);
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Firebase Database reference για τα playlists
        playlistsRef = FirebaseDatabase.getInstance().getReference("playlists");

        // Λίστα playlists
        playlistList = new ArrayList<>();

        // Ρύθμιση του Adapter
        playlistAdapter = new PlaylistAdapter(playlistList, getActivity());
        playlistsRecyclerView.setAdapter(playlistAdapter);

        // Φόρτωμα των playlists από το Firebase
        loadPlaylistsFromFirebase();

        return rootView;
    }

    private void loadPlaylistsFromFirebase() {
        playlistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playlistList.clear(); // Καθαρισμός της λίστας πριν τη νέα φόρτωση

                // Ανάγνωση των δεδομένων των playlists
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Playlist playlist = snapshot.getValue(Playlist.class);
                    playlistList.add(playlist); // Προσθήκη στο playlist list
                }

                // Ενημέρωση του Adapter
                playlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error loading playlists: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
