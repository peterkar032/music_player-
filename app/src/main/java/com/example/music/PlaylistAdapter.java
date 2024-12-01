package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistList;
    private Context context;

    public PlaylistAdapter(List<Playlist> playlistList, Context context) {
        this.playlistList = playlistList;
        this.context = context;
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);
        holder.nameTextView.setText(playlist.getName());
        //holder.descriptionTextView.setText(playlist.getDescription());
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.playlist_name);
            descriptionTextView = itemView.findViewById(R.id.playlist_description);
        }
    }
}
