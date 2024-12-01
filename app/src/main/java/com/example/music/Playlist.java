package com.example.music;

public class Playlist {
    private String title;
    private String albumArtUrl;
    private String id;

    // Default constructor required for Firebase
    public Playlist() {
        // Ανάγκη για το Firebase
    }

    // Constructor για δημιουργία Playlist
    public Playlist(String title, String albumArtUrl, String id) {
        this.title = title;
        this.albumArtUrl = albumArtUrl;
        this.id = id;
    }

    // Constructor για δημιουργία Playlist χωρίς ID (το ID μπορεί να προστεθεί από το Firebase)
    public Playlist(String title, String albumArtUrl) {
        this.title = title;
        this.albumArtUrl = albumArtUrl;
    }

    // Getters για τα δεδομένα
    public String getTitle() {
        return title;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public String getId() {
        return id;
    }

    // Setters για τα δεδομένα
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return title;  // Επιστρέφει το όνομα της playlist
    }
}
