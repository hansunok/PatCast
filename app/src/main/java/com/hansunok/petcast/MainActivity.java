package com.hansunok.petcast;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hansunok.petcast.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_info, R.id.navigation_diary, R.id.navigation_record, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            int currentDestId = navController.getCurrentDestination().getId();

            if (currentDestId == itemId) {
                // 이미 선택된 탭 -> 스택 초기화 (루트만 남김)
                navController.popBackStack(itemId, false);
            } else {
                // 다른 탭으로 전환
                navController.navigate(itemId);
            }

            return true;
        });

        //액션바 안 보이게.
        getSupportActionBar().hide();

        adView = findViewById(R.id.adView);

        // 광고 초기화
        MobileAds.initialize(this, initializationStatus -> {});

        // 광고 요청
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("AdMob", "광고 로드 성공");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.d("AdMob", "광고 로드 실패: " + adError.getMessage());
            }

            @Override
            public void onAdOpened() {
                Log.d("AdMob", "광고 클릭됨 - 광고 화면 열림");
            }

            @Override
            public void onAdClosed() {
                Log.d("AdMob", "광고 닫힘");
            }
        });




    }


    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

}