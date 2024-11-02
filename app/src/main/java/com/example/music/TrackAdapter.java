package com.example.music;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<String> trackTitles;
    private OnTrackClickListener listener;

    public interface OnTrackClickListener {
        void onTrackClick(int position);
    }

    public TrackAdapter(List<String> trackTitles, OnTrackClickListener listener) {
        this.trackTitles = trackTitles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        holder.titleTextView.setText(trackTitles.get(position));
        holder.itemView.setOnClickListener(v -> listener.onTrackClick(position));
    }

    @Override
    public int getItemCount() {
        return trackTitles.size();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
