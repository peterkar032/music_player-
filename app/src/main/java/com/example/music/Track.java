package com.example.music;

public class Track {
    private String title;
    private String artist;
    private String trackUrl;
    private String albumArtUrl;  // URL εικόνας άλμπουμ

    // Προεπιλεγμένες τιμές
    private static final String DEFAULT_TITLE = "Unknown Title";
    private static final String DEFAULT_ARTIST = "Unknown Artist";
    private static final String DEFAULT_TRACK_URL = "";
    private static final String DEFAULT_ALBUM_ART_URL = "";  // Μπορεί να αντικατασταθεί με ένα URL προς μια τοπική εικόνα

    public Track(String title, String artist, String trackUrl, String albumArtUrl) {
        this.title = (title != null && !title.isEmpty()) ? title : DEFAULT_TITLE;
        this.artist = (artist != null && !artist.isEmpty()) ? artist : DEFAULT_ARTIST;
        this.trackUrl = (trackUrl != null && !trackUrl.isEmpty()) ? trackUrl : DEFAULT_TRACK_URL;
        this.albumArtUrl = (albumArtUrl != null && !albumArtUrl.isEmpty()) ? albumArtUrl : DEFAULT_ALBUM_ART_URL;
    }

    public String getTitle() {
        return title != null ? title : DEFAULT_TITLE;
    }

    public String getArtist() {
        return artist != null ? artist : DEFAULT_ARTIST;
    }

    public String getTrackUrl() {
        return trackUrl != null ? trackUrl : DEFAULT_TRACK_URL;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl != null ? albumArtUrl : DEFAULT_ALBUM_ART_URL;
    }
}
