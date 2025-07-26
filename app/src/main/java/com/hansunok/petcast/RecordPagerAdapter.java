package com.hansunok.petcast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class RecordPagerAdapter extends FragmentStateAdapter {


    private final List<Pet> petList;

    public RecordPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                              List<Pet> petList) {
        super(fragmentActivity);
        this.petList = petList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Pet pet = petList.get(position);
        int petId = pet.getId();

        return new RecordCalendarFragment(petId);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }
}
