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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private List<Track> trackList;
    private OnItemClickListener listener;
    private Context context;
    private DatabaseReference likesRef;

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    // Constructor
    public TrackAdapter(List<Track> trackList, OnItemClickListener listener, Context context) {
        this.trackList = trackList;
        this.listener = listener;
        this.context = context;
        likesRef = FirebaseDatabase.getInstance().getReference("likes");
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


        holder.trackTitleTextView.setText(currentTrack.getTitle());
        holder.artistTextView.setText(currentTrack.getArtist());

        Picasso.get()
                .load(currentTrack.getAlbumArtUrl())
                .placeholder(R.drawable.music)
                .error(R.drawable.music1)
                .into(holder.albumArtImageView);

        // Χρήση του listener για click events
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentTrack);
            }
        });

        // Δημιουργία του context menu
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
        });

        holder.itemView.setLongClickable(true);

    }

    @Override
    public int getItemCount() {
        return trackList != null ? trackList.size() : 0;
    }

    private void saveToFirebase(Track track) {

        String trackId = likesRef.push().getKey();
        if (trackId != null) {
            likesRef.child(trackId).setValue(track)
                    .addOnSuccessListener(aVoid -> {

                        trackList.add(track);
                        notifyItemInserted(trackList.size() - 1);
                        Toast.makeText(context, "Track liked: " + track.getTitle(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void removeFromFirebase(Track track, int position) {
        likesRef.orderByChild("title").equalTo(track.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot trackSnapshot : snapshot.getChildren()) {
                    trackSnapshot.getRef().removeValue();
                }
                trackList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Track removed: " + track.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error removing track: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
