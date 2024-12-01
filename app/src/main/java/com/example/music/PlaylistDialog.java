package com.example.music;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDialog {

    private final Context context;
    private final DatabaseReference playlistsRef;
    private final Track track;

    public PlaylistDialog(Context context, DatabaseReference playlistsRef, Track track) {
        this.context = context;
        this.playlistsRef = playlistsRef;
        this.track = track;
    }

    public void show() {
        // Create the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_playlist, null);
        final EditText playlistNameEditText = dialogView.findViewById(R.id.playlistNameEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Playlist")
                .setView(dialogView)
                .setPositiveButton("Create New Playlist", (dialog, which) -> {
                    // Handle new playlist creation
                    String playlistName = playlistNameEditText.getText().toString();
                    if (!playlistName.isEmpty()) {
                        createNewPlaylist(playlistName);
                    } else {
                        Toast.makeText(context, "Please enter a playlist name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Add to Existing Playlist", (dialog, which) -> {
                    // Handle adding to existing playlist
                    showExistingPlaylists();
                })
                .setCancelable(true)
                .show();
    }

    private void createNewPlaylist(String playlistName) {
        DatabaseReference newPlaylistRef = playlistsRef.push(); // Create a new playlist node
        newPlaylistRef.child("name").setValue(playlistName);

        // Add track to the new playlist
        String trackId = track.getTrackId(); // Using track's unique ID
        if (trackId != null) {
            newPlaylistRef.child("tracks").child(trackId).setValue(track); // Add track to the playlist
            Toast.makeText(context, "Playlist created and track added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error with track ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExistingPlaylists() {
        // Fetch the existing playlists from Firebase
        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> playlistNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String playlistName = snapshot.child("name").getValue(String.class);
                    playlistNames.add(playlistName);
                }

                if (!playlistNames.isEmpty()) {
                    showPlaylistSelectionDialog(playlistNames);
                } else {
                    Toast.makeText(context, "No playlists available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error fetching playlists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistSelectionDialog(List<String> playlistNames) {
        // Create a dialog with the available playlists
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Playlist");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, playlistNames);
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedPlaylist = playlistNames.get(which);
            addToSelectedPlaylist(selectedPlaylist);
        });

        builder.setCancelable(true);
        builder.show();
    }

    private void addToSelectedPlaylist(String playlistName) {
        // Find the selected playlist in Firebase
        playlistsRef.orderByChild("name").equalTo(playlistName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String playlistId = snapshot.getKey(); // Get the playlist ID

                        // Create a new child for the track inside the "tracks" node of the selected playlist
                        DatabaseReference playlistTracksRef = playlistsRef.child(playlistId).child("tracks");

                        // Using push() to add a new unique key for each track in the playlist
                        String newTrackId = playlistTracksRef.push().getKey();

                        if (newTrackId != null) {
                            playlistTracksRef.child(newTrackId).setValue(track); // Add track with a new unique ID
                            Toast.makeText(context, "Track added to " + playlistName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error generating unique track ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, "Playlist not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error adding track to playlist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
