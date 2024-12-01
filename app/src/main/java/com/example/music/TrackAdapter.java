package com.example.music;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<Track> trackList;
    private final OnItemClickListener onItemClickListener;
    private final Context context;
    private DatabaseReference likesRef;
    private DatabaseReference playlistsRef;

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    public TrackAdapter(List<Track> trackList, OnItemClickListener onItemClickListener, Context context) {
        this.trackList = trackList;
        this.onItemClickListener = onItemClickListener;
        this.context = context;
        likesRef = FirebaseDatabase.getInstance().getReference("likes");
        playlistsRef = FirebaseDatabase.getInstance().getReference("playlists");
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track currentTrack = trackList.get(position);

        // Update track title and artist
        holder.trackTitleTextView.setText(currentTrack.getTitle());
        holder.artistTextView.setText(currentTrack.getArtist());

        // Load album art image
        Picasso.get()
                .load(currentTrack.getAlbumArtUrl())
                .placeholder(R.drawable.music)
                .error(R.drawable.music1)
                .into(holder.albumArtImageView);

        // Set click listener for selecting the track
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(currentTrack));

        // Set context menu listener
        holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(Menu.NONE, R.id.menu_like, Menu.NONE, "Like").setOnMenuItemClickListener(item -> {
                saveToFirebase(currentTrack);
                return true;
            });
            menu.add(Menu.NONE, R.id.menu_add_to_playlist, Menu.NONE, "Add to Playlist").setOnMenuItemClickListener(item -> {
                showPlaylistDialog(currentTrack);
                return true;
            });
        });
    }

    @Override
    public int getItemCount() {
        return trackList != null ? trackList.size() : 0;
    }

    private void saveToFirebase(Track track) {
        try {
            String trackId = likesRef.push().getKey(); // Generate unique ID
            if (trackId != null) {
                likesRef.child(trackId).setValue(track).addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Track liked: " + track.getTitle(), Toast.LENGTH_SHORT).show()
                ).addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(context, "Error generating key for Firebase", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Unexpected Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPlaylistDialog(Track track) {
        // Display a dialog to choose between creating a new playlist or adding to an existing one
        PlaylistDialog dialog = new PlaylistDialog(context, playlistsRef, track);
        dialog.show();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        public TextView trackTitleTextView;
        public TextView artistTextView;
        public ImageView albumArtImageView;

        public TrackViewHolder(View itemView) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(R.id.trackTitleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);
        }
    }
}
