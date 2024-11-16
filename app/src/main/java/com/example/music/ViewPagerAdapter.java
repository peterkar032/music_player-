package com.example.music;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new Search();
            case 1: return new Albums();
            case 2: return new Favorites();
            case 3: return new Playlists();
            default: return new Search();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
