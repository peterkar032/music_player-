package com.example.music;

import android.content.Intent;
import android.net.Uri;
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
        playlistAdapter = new PlaylistAdapter(playlistList, getContext(), new PlaylistAdapter.OnPlaylistClickListener() {
            @Override
            public void onPlaylistClick(Playlist playlist) {
                showTracksDialog(playlist); // Εμφάνιση τραγουδιών
            }

            @Override
            public void onPlaylistLongClick(Playlist playlist) {
                showDeleteConfirmationDialog(playlist); // Επιλογή διαγραφής
            }
        });
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

    private void showDeleteConfirmationDialog(Playlist playlist) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete Playlist");
        builder.setMessage("Are you sure you want to delete the playlist \"" + playlist.getName() + "\"?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deletePlaylist(playlist);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void deletePlaylist(Playlist playlist) {
        DatabaseReference playlistRef = playlistsRef.child(playlist.getName());

        playlistRef.removeValue((error, ref) -> {
            if (error == null) {
                Toast.makeText(getContext(), "Playlist deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error deleting playlist: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                builder.setItems(trackArray, (dialog, which) -> {
                    // Όταν ο χρήστης πατήσει ένα τραγούδι, ανοίγουμε YouTube
                    Track selectedTrack = trackList.get(which);
                    openYoutubeSearch(selectedTrack.getTitle() + " " + selectedTrack.getArtist());
                });

                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading tracks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Μέθοδος για να ανοίξουμε την αναζήτηση στο YouTube
    private void openYoutubeSearch(String query) {
        String youtubeSearchUrl = "https://www.youtube.com/results?search_query=" + query.replace(" ", "+");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeSearchUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open YouTube", Toast.LENGTH_SHORT).show();
        }
    }
}
