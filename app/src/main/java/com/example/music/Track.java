package com.example.music;

public class Track {
    private String trackId; // Unique ID for each track
    private String title;
    private String artist;
    private String trackUrl;
    private String albumArtUrl;

    // Default constructor for Firebase
    public Track() {
    }

    // Constructor to initialize track without trackId
    public Track(String title, String artist, String trackUrl, String albumArtUrl) {
        this.trackId = "";  // Empty or null until assigned
        this.title = title;
        this.artist = artist;
        this.trackUrl = trackUrl;
        this.albumArtUrl = albumArtUrl;
    }

    // Constructor to initialize all fields including trackId
    public Track(String trackId, String title, String artist, String trackUrl, String albumArtUrl) {
        this.trackId = trackId;  // Use assigned trackId
        this.title = title;
        this.artist = artist;
        this.trackUrl = trackUrl;
        this.albumArtUrl = albumArtUrl;
    }

    // Getter and Setter for trackId
    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    // Getters for other fields
    public String getTitle() {
        return title;
    }


    public String getArtist() {
        return artist;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }


}