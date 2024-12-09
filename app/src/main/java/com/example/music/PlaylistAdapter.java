package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistList;
    private Context context;

    public PlaylistAdapter(List<Playlist> playlistList, Context context) {
        this.playlistList = playlistList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Δημιουργία του item view για κάθε playlist
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist currentPlaylist = playlistList.get(position);

        // Ορισμός του ονόματος και του αριθμού των τραγουδιών
        holder.playlistNameTextView.setText(currentPlaylist.getName());
        holder.trackCountTextView.setText(currentPlaylist.getNumberOfTracks() + " Tracks");
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public TextView playlistNameTextView;
        public TextView trackCountTextView;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            playlistNameTextView = itemView.findViewById(R.id.playlistNameTextView);
            trackCountTextView = itemView.findViewById(R.id.trackCountTextView);
        }
    }
}