package com.hansunok.petcast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class PetPagerAdapter extends FragmentStateAdapter {

    public enum ViewMode {
        LIST, CALENDAR
    }

    private final List<Pet> petList;
    private final ViewMode viewMode;

    public PetPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                           List<Pet> petList,
                           ViewMode viewMode) {
        super(fragmentActivity);
        this.petList = petList;
        this.viewMode = viewMode;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Pet pet = petList.get(position);
        int petId = pet.getId();

        switch (viewMode) {
            case CALENDAR:
                return new PetDiaryCalendarFragment(petId);
            case LIST:
            default:
                return new PetDiaryListFragment(petId);
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }
}
