package com.example.music;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String name;
    private int numberOfTracks;
    private List<Track> tracks;

    public Playlist(String name) {
        this.name = name;
        this.numberOfTracks = 0;
        this.tracks = new ArrayList<>();
    }

    public Playlist(String name, int numberOfTracks) {
        this.name = name;
        this.numberOfTracks = numberOfTracks;
        this.tracks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(int numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
        this.numberOfTracks++;  // Αυξάνουμε τον αριθμό των τραγουδιών
    }
}