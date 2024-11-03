package com.example.music;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private final List<String> trackTitles;
    private final OnTrackClickListener onTrackClickListener;

    public TrackAdapter(List<String> trackTitles, OnTrackClickListener onTrackClickListener) {
        this.trackTitles = trackTitles;
        this.onTrackClickListener = onTrackClickListener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        holder.bind(trackTitles.get(position), onTrackClickListener);
    }

    @Override
    public int getItemCount() {
        return trackTitles.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        private final TextView trackTitleTextView;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(android.R.id.text1);
        }

        public void bind(String trackTitle, OnTrackClickListener onTrackClickListener) {
            trackTitleTextView.setText(trackTitle);
            itemView.setOnClickListener(v -> onTrackClickListener.onTrackClick(getAdapterPosition()));
        }
    }

    public interface OnTrackClickListener {
        void onTrackClick(int position);
    }
}
