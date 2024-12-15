package com.example.music;

import android.content.Context;
import android.util.Log;
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
    private OnPlaylistClickListener onPlaylistClickListener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public PlaylistAdapter(List<Playlist> playlistList, Context context, OnPlaylistClickListener onPlaylistClickListener) {
        this.playlistList = playlistList;
        this.context = context;
        this.onPlaylistClickListener = onPlaylistClickListener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist currentPlaylist = playlistList.get(position);
        holder.playlistNameTextView.setText(currentPlaylist.getName());
        holder.trackCountTextView.setText(currentPlaylist.getNumberOfTracks() + " Tracks");

        holder.itemView.setOnClickListener(v -> {
            Log.d("PlaylistAdapter", "Clicked on: " + currentPlaylist.getName());
            if (onPlaylistClickListener != null) {
                onPlaylistClickListener.onPlaylistClick(currentPlaylist);
            }
        });
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