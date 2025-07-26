package com.hansunok.petcast;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hansunok.petcast.databinding.FragmentRecordBinding;

import java.util.List;

public class RecordFragment extends Fragment {

    private FragmentRecordBinding binding;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private RecordPagerAdapter recordPagerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DBHelper dbHelper = new DBHelper(getContext());
        List<Pet> petList = dbHelper.getAllPets();

        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;

        recordPagerAdapter = new RecordPagerAdapter(getActivity(), petList);
        viewPager.setAdapter(recordPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(petList.get(position).getName())
        ).attach();


        Bundle bundle = getArguments();
        String petName = "";
        int petId = -1;

        if (bundle != null) {
            petName = bundle.getString("petName");
            petId = bundle.getInt("petId");

            Log.d("petName", petName);
            Log.d("petId", petId+"");
            for (int i = 0; i < petList.size(); i++) {
                if (petList.get(i).getId() == petId) {
                    // 해당 탭 위치로 뷰페이저 페이지 설정
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null) {
                        tab.select();
                    }
                    viewPager.setCurrentItem(i, true); // true: 부드럽게 스크롤 이동
                    break;
                }
            }

        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding = null;
    }
}