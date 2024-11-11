package com.example.music;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> trackList;
    private OnItemClickListener onItemClickListener;

    // Interface για την επικοινωνία με την Activity όταν επιλεγεί ένα τραγούδι
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Constructor για το adapter
    public TrackAdapter(List<Track> trackList, OnItemClickListener onItemClickListener) {
        this.trackList = trackList;
        this.onItemClickListener = onItemClickListener;
    }

    // Δημιουργία του ViewHolder για κάθε στοιχείο της λίστας
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(itemView);
    }

    // Σύνδεση δεδομένων με τα views του ViewHolder
    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track currentTrack = trackList.get(position);

        holder.trackTitleTextView.setText(currentTrack.getTitle());
        holder.artistTextView.setText(currentTrack.getArtist());

        // Χρήση βιβλιοθήκης Picasso για να φορτώσουμε την εικόνα εξωφύλλου αν υπάρχει
        if (!currentTrack.getAlbumArtUrl().isEmpty()) {
            Picasso.get().load(currentTrack.getAlbumArtUrl()).into(holder.albumArtImageView);
        }

        // Ορισμός του listener για την επιλογή του τραγουδιού
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
    }

    // Επιστρέφει τον αριθμό των τραγουδιών στη λίστα
    @Override
    public int getItemCount() {
        return trackList.size();
    }

    // ViewHolder για το κάθε στοιχείο του RecyclerView
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