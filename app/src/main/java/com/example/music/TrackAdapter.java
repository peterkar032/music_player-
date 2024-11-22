package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<Track> trackList;
    private final OnItemClickListener onItemClickListener;
    private Context context = null;

    // Σταθερές για τα IDs των επιλογών μενού
    private static final int MENU_LIKE = 1;
    private static final int MENU_ADD_TO_PLAYLIST = 2;

    // Interface για την επικοινωνία με την Activity όταν επιλεγεί ένα τραγούδι
    public interface OnItemClickListener {
        void onItemClick(Track track);  // Περνάμε το αντικείμενο Track στο listener
    }

    // Constructor για το adapter
    public TrackAdapter(List<Track> trackList, OnItemClickListener onItemClickListener) {
        this.trackList = trackList;
        this.onItemClickListener = onItemClickListener;
        this.context = context;
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

        // Ενημέρωση τίτλου και καλλιτέχνη
        holder.trackTitleTextView.setText(currentTrack.getTitle());
        holder.artistTextView.setText(currentTrack.getArtist());

        // Χρήση βιβλιοθήκης Picasso για φόρτωση εικόνας εξωφύλλου αν υπάρχει
        String albumArtUrl = currentTrack.getAlbumArtUrl();
        if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
            Picasso.get()
                    .load(albumArtUrl)
                    .placeholder(R.drawable.music)
                    .error(R.drawable.music1)
                    .into(holder.albumArtImageView);
        } else {
            holder.albumArtImageView.setImageResource(R.drawable.music1);
        }

        // Listener για κανονικό click
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(currentTrack));

        // Listener για παρατεταμένο πάτημα (context menu)
        holder.itemView.setOnLongClickListener(v -> {
            holder.itemView.setOnCreateContextMenuListener((menu, v1, menuInfo) -> {
                menu.add(Menu.NONE, MENU_LIKE, Menu.NONE, "Like")
                        .setOnMenuItemClickListener(item -> {
                            Toast.makeText(context, "Liked: " + currentTrack.getTitle(), Toast.LENGTH_SHORT).show();
                            return true;
                        });
                menu.add(Menu.NONE, MENU_ADD_TO_PLAYLIST, Menu.NONE, "Add to Playlist")
                        .setOnMenuItemClickListener(item -> {
                            Toast.makeText(context, "Added to playlist: " + currentTrack.getTitle(), Toast.LENGTH_SHORT).show();
                            return true;
                        });
            });
            return false; // Επιστρέφουμε false για να αφήσουμε και άλλες ενέργειες (π.χ., default actions)
        });
    }

    @Override
    public int getItemCount() {
        return trackList != null ? trackList.size() : 0;
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        public final TextView trackTitleTextView;
        public final TextView artistTextView;
        public final ImageView albumArtImageView;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(R.id.trackTitleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);
        }
    }
}
