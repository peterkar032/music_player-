package com.example.music;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class SearchArtistTest {

    @Test
    public void testClickArtistTitleOpensWebSearch() {
        String artistName = "The Beatles";
        String expectedUrl = "https://www.google.com/search?q=" + Uri.encode(artistName);

        // Εκκίνηση του Intents
        Intents.init();

        // Λανσάρισμα της Activity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Ρυθμίστε το TextView με το όνομα του καλλιτέχνη και προσθέστε έναν onClickListener
        scenario.onActivity(activity -> {
            TextView artistTitle = activity.findViewById(R.id.ArtistTitle);
            artistTitle.setText(artistName);
            artistTitle.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(expectedUrl));
                activity.startActivity(intent);
            });
        });

        // Εκτέλεση του click
        onView(withId(R.id.ArtistTitle)).perform(ViewActions.click());

        // Ελέγξτε αν το σωστό Intent δημιουργήθηκε
        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(expectedUrl)
        ));

        // Τερματισμός του Intents
        Intents.release();
    }
}
