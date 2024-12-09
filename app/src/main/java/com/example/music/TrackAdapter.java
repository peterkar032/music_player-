package com.example.music;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> trackList;
    private OnItemClickListener listener;
    private Context context;
    private DatabaseReference playlistsRef;

    // Interface για το OnItemClickListener
    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    // Constructor
    public TrackAdapter(List<Track> trackList, OnItemClickListener listener, Context context) {
        this.trackList = trackList;
        this.listener = listener;
        this.context = context;
        playlistsRef = FirebaseDatabase.getInstance().getReference("playlists");
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Δημιουργία του item view για κάθε τραγούδι
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track currentTrack = trackList.get(position);

        // Ορισμός των δεδομένων στα views
        holder.trackTitleTextView.setText(currentTrack.getTitle());
        holder.artistTextView.setText(currentTrack.getArtist());

        // Χρήση της βιβλιοθήκης Picasso για φόρτωση εικόνας album art
        Picasso.get()
                .load(currentTrack.getAlbumArtUrl())
                .placeholder(R.drawable.music)
                .error(R.drawable.music1)
                .into(holder.albumArtImageView);

        // Διαχείριση click για το τραγούδι
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentTrack);
            }
        });

        // Διαχείριση long click για το context menu (Προσθήκη σε Playlist)
        holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(Menu.NONE, R.id.menu_like, Menu.NONE, "Like")
                    .setOnMenuItemClickListener(item -> {
                        saveToFirebase(currentTrack);
                        return true;
                    });

            menu.add(Menu.NONE, R.id.menu_unlike, Menu.NONE, "Unlike")
                    .setOnMenuItemClickListener(item -> {
                        removeFromFirebase(currentTrack, holder.getAdapterPosition());
                        return true;
                    });

            // Προσθήκη επιλογής "Προσθήκη στην Playlist"
            menu.add(Menu.NONE, R.id.menu_add_to_playlist, Menu.NONE, "Add to Playlist")
                    .setOnMenuItemClickListener(item -> {
                        showAddToPlaylistDialog(currentTrack);
                        return true;
                    });
        });

        holder.itemView.setLongClickable(true);
    }

    @Override
    public int getItemCount() {
        return trackList != null ? trackList.size() : 0;
    }

    // Μέθοδος για να προσθέσουμε τραγούδι σε playlist
    private void showAddToPlaylistDialog(Track track) {
        // Εμφανίζει dialog για την επιλογή ή δημιουργία μιας νέας playlist
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add to Playlist");

        // Ανάκτηση των υπαρχουσών playlist από τη βάση δεδομένων Firebase
        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> playlistNames = new ArrayList<>();
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    String playlistName = playlistSnapshot.getKey();
                    playlistNames.add(playlistName);
                }

                // Προσθήκη επιλογής για δημιουργία νέας playlist
                playlistNames.add("Create New Playlist");

                // Δημιουργία απλού διαλόγου με λίστα για την επιλογή playlist
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, playlistNames);
                builder.setAdapter(adapter, (dialog, which) -> {
                    if (which == playlistNames.size() - 1) {
                        // Δημιουργία νέας playlist
                        showCreateNewPlaylistDialog(track);
                    } else {
                        // Προσθήκη του τραγουδιού στην επιλεγμένη playlist
                        String selectedPlaylist = playlistNames.get(which);
                        addToPlaylist(selectedPlaylist, track);
                    }
                });

                builder.create().show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error loading playlists", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showCreateNewPlaylistDialog(Track track) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Create New Playlist");

        final EditText playlistNameEditText = new EditText(context);
        builder.setView(playlistNameEditText);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = playlistNameEditText.getText().toString().trim();
            if (!playlistName.isEmpty()) {
                // Δημιουργία νέας playlist και προσθήκη του τραγουδιού
                createNewPlaylist(playlistName, track);
            } else {
                Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    public void createNewPlaylist(String playlistName, Track track) {
        // Δημιουργία νέας playlist
        Playlist newPlaylist = new Playlist(playlistName);


        playlistsRef.child(playlistName).setValue(newPlaylist)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Playlist created successfully", Toast.LENGTH_SHORT).show();

                    addTrackToPlaylist(track, playlistName);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error creating playlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addTrackToPlaylist(Track track, String playlistName) {
        playlistsRef.child(playlistName).child("tracks").push().setValue(track)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Track added to playlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void addToPlaylist(String playlistName, Track track) {
        playlistsRef.child(playlistName).child("tracks").push().setValue(track)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Track added to playlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveToFirebase(Track track) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");
        String trackId = likesRef.push().getKey();
        if (trackId != null) {
            likesRef.child(trackId).setValue(track)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Track liked: " + track.getTitle(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Μέθοδος για να αφαιρέσουμε το τραγούδι από τα "Like"
    private void removeFromFirebase(Track track, int position) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");
        likesRef.orderByChild("title").equalTo(track.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot trackSnapshot : snapshot.getChildren()) {
                    trackSnapshot.getRef().removeValue();
                }
                trackList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Track removed from likes", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        public TextView trackTitleTextView, artistTextView;
        public ImageView albumArtImageView;

        public TrackViewHolder(View itemView) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(R.id.trackTitleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);
        }
    }
}
