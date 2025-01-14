package com.example.music;

public class Track {
    private String title;
    private String artist;
    private String trackUrl;
    private String albumArtUrl;

    // Default constructor για το Firebase
    public Track() {
    }

    public Track(String title, String artist, String trackUrl, String albumArtUrl) {
        this.title = title;
        this.artist = artist;
        this.trackUrl = trackUrl;
        this.albumArtUrl = albumArtUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }
}