package com.example.smartoutfitapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartoutfitapp.ui.outfit.OutfitFragment;
import com.example.smartoutfitapp.ui.profile.ProfileFragment;
import com.example.smartoutfitapp.ui.tryon.TryOnFragment;
import com.example.smartoutfitapp.ui.weather.WeatherFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // 1. 设置点击事件监听器
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // 根据点击的 ID 判断是哪个 Fragment
            if (itemId == R.id.nav_weather) {
                selectedFragment = new WeatherFragment();
            } else if (itemId == R.id.nav_outfit) {
                selectedFragment = new OutfitFragment();
            } else if (itemId == R.id.nav_tryon) {
                selectedFragment = new TryOnFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            // 切换页面
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // 2. 默认选中第一个页面 (天气)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new WeatherFragment())
                    .commit();
        }
    }
}