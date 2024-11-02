package com.example.music;

import android.content.Context;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TrackLoader {
    private List<Integer> trackList;  // Λίστα με τα resource IDs των τραγουδιών
    private List<String> trackTitles; // Λίστα με τους τίτλους των τραγουδιών

    public TrackLoader(Context context) {
        trackList = new ArrayList<>();
        trackTitles = new ArrayList<>();
        loadTracks(context);
    }

    // Μέθοδος για τη φόρτωση των τραγουδιών από το φάκελο raw
    private void loadTracks(Context context) {
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                trackList.add(resId);
                trackTitles.add(field.getName().replace("_", " "));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // Επιστροφή της λίστας με τα IDs των τραγουδιών
    public List<Integer> getTrackList() {
        return trackList;
    }

    // Επιστροφή της λίστας με τους τίτλους των τραγουδιών
    public List<String> getTrackTitles() {
        return trackTitles;
    }
}
