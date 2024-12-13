package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Playlists extends Fragment {

    private RecyclerView recyclerView;
    private PlaylistAdapter playlistAdapter;
    private List<Playlist> playlistList;
    private DatabaseReference playlistsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPlaylists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        playlistList = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(playlistList, getContext(), playlist -> showTracksDialog(playlist));
        recyclerView.setAdapter(playlistAdapter);

        playlistsRef = FirebaseDatabase.getInstance().getReference("playlists");

        playlistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playlistList.clear();
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    String playlistName = playlistSnapshot.getKey();

                    DataSnapshot tracksSnapshot = playlistSnapshot.child("tracks");
                    int numberOfTracks = tracksSnapshot.exists() ? (int) tracksSnapshot.getChildrenCount() : 0;

                    Playlist playlist = new Playlist(playlistName, numberOfTracks);
                    playlistList.add(playlist);
                }

                playlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void showTracksDialog(Playlist playlist) {
        DatabaseReference tracksRef = playlistsRef.child(playlist.getName()).child("tracks");

        tracksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Track> trackList = new ArrayList<>();
                for (DataSnapshot trackSnapshot : snapshot.getChildren()) {
                    Track track = trackSnapshot.getValue(Track.class);
                    if (track != null) {
                        trackList.add(track);
                    }
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                builder.setTitle(playlist.getName() + " Tracks");


                List<String> trackNames = new ArrayList<>();
                for (Track track : trackList) {
                    trackNames.add(track.getTitle() + " - " + track.getArtist());
                }

                CharSequence[] trackArray = trackNames.toArray(new CharSequence[0]);
                builder.setItems(trackArray, null);

                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading tracks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
