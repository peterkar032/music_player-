package com.example.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        serviceIntent.setAction(intent.getAction()); // Περάστε την ενέργεια στην υπηρεσία
        context.startService(serviceIntent);
    }
}
