package com.hansunok.petcast;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hansunok.petcast.databinding.FragmentDiaryBinding;

import java.util.List;

public class DiaryFragment extends Fragment {

    private FragmentDiaryBinding binding;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PetPagerAdapter petPagerAdapter;

    List<Pet> petList;
    DBHelper dbHelper;

    private PetPagerAdapter.ViewMode viewMode = PetPagerAdapter.ViewMode.CALENDAR;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDiaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;

        dbHelper = new DBHelper(getContext());
        petList = dbHelper.getAllPets();

        setAdapterForViewMode();

        MaterialButton viewChangeBtn = binding.viewChangeBtn;
        updateViewChangeButton(viewChangeBtn);

        viewChangeBtn.setOnClickListener(v -> {
            // 보기 모드 전환
            if (viewMode == PetPagerAdapter.ViewMode.LIST) {
                viewMode = PetPagerAdapter.ViewMode.CALENDAR;
            } else {
                viewMode = PetPagerAdapter.ViewMode.LIST;
            }

            // 어댑터 재설정
            setAdapterForViewMode();
            updateViewChangeButton(viewChangeBtn);
        });

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

    private void setAdapterForViewMode() {
        int currentPosition = tabLayout.getSelectedTabPosition();
        if (currentPosition == -1) currentPosition = 0;

        petPagerAdapter = new PetPagerAdapter(requireActivity(), petList, viewMode);
        viewPager.setAdapter(petPagerAdapter);
        viewPager.setUserInputEnabled(false); // 스와이프 비활성화

        // 2. TabLayoutMediator 생성 후 attach
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(petList.get(position).getName())
        );
        mediator.attach();

        // 3. 저장해둔 위치로 탭과 뷰페이저 선택 위치 설정
        if (currentPosition < petList.size()) {
            TabLayout.Tab tab = tabLayout.getTabAt(currentPosition);
            if (tab != null) {
                tab.select();
            }
            viewPager.setCurrentItem(currentPosition, false);
        }
    }

    private void updateViewChangeButton(MaterialButton button) {
        if (viewMode == PetPagerAdapter.ViewMode.LIST) {
            button.setText("달력보기");
        } else {
            button.setText("리스트보기");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}