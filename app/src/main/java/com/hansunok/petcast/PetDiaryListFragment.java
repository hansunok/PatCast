package com.hansunok.petcast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PetDiaryListFragment extends PetDiaryBaseFragment {

    public PetDiaryListFragment(){}
    public PetDiaryListFragment(int petId) {
        super(petId, 0); // 부모 생성자 호출
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_pet_list, container, false);

        initView(view);

        LinearLayout selCalLayout = view.findViewById(R.id.selCalLayout); //등록
        selCalLayout.setVisibility(View.GONE);


        return view;
    }

    @Override
    public void redrawView(){

    }

}
