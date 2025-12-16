package com.example.smartoutfitapp.ui.outfit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.smartoutfitapp.R;
import com.example.smartoutfitapp.model.Outfit;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class OutfitFragment extends Fragment {

    private OutfitAdapter adapter;
    private List<Outfit> allData = new ArrayList<>();

    // 标签定义
    private final String[] STYLES = {"不限", "休闲", "商务"};
    private final String[] WEATHERS = {"不限", "晴天", "阴雨"};
    private final String[] SEASONS = {"不限", "春", "夏", "秋", "冬"};
    private final String[] OCCASIONS = {"不限", "工作", "居家", "出行"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_outfit, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        FloatingActionButton fabFilter = view.findViewById(R.id.fab_filter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);

        initData();

        adapter = new OutfitAdapter(getContext(), new ArrayList<>(allData));
        recyclerView.setAdapter(adapter);

        fabFilter.setOnClickListener(v -> showFilterDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次显示时，读取所有默认偏好并应用
        applyGlobalFilters();
    }

    private void applyGlobalFilters() {
        if (getContext() == null || adapter == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        // 1. 读取所有设置 (性别 + 4个偏好)
        String genderPref = prefs.getString("filter_gender", "all");
        String defStyle = prefs.getString("default_style", "不限");
        String defWeather = prefs.getString("default_weather", "不限");
        String defSeason = prefs.getString("default_season", "不限");
        String defOccasion = prefs.getString("default_occasion", "不限");

        // 2. 综合过滤
        List<Outfit> filtered = new ArrayList<>();

        for (Outfit item : allData) {
            // 条件1: 性别匹配
            boolean matchGender = genderPref.equals("all") || item.gender.equals(genderPref);

            // 条件2: 默认偏好匹配 (如果不限，则视为通过)
            boolean matchStyle = defStyle.equals("不限") || item.style.equals(defStyle);
            boolean matchWeather = defWeather.equals("不限") || item.weather.equals(defWeather);
            boolean matchSeason = defSeason.equals("不限") || item.season.equals(defSeason);
            boolean matchOccasion = defOccasion.equals("不限") || item.occasion.equals(defOccasion);

            if (matchGender && matchStyle && matchWeather && matchSeason && matchOccasion) {
                filtered.add(item);
            }
        }

        adapter.setList(filtered);
    }

    private void initData() {
        allData.clear();
        allData.add(new Outfit("春日连衣裙穿搭", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500", "female", "休闲", "晴天", "春", "不限"));
        allData.add(new Outfit("夏日清凉穿搭", "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=500", "female", "休闲", "晴天", "夏", "不限"));
        allData.add(new Outfit("秋日休闲穿搭", "https://images.unsplash.com/photo-1512353087810-25dfcd100962?w=500", "male", "休闲", "晴天", "秋", "不限"));
        allData.add(new Outfit("冬日羽绒服穿搭", "https://images.pexels.com/photos/26115819/pexels-photo-26115819.jpeg", "female", "休闲", "晴天", "冬", "不限"));
        allData.add(new Outfit("西装商务穿搭", "https://images.pexels.com/photos/3778876/pexels-photo-3778876.jpeg", "male", "商务", "晴天", "不限", "工作"));
    }

    private void showFilterDialog() {
        // ... (弹窗代码保持不变，还是支持临时筛选)
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);
        Spinner spStyle = dialogView.findViewById(R.id.spinner_style);
        Spinner spWeather = dialogView.findViewById(R.id.spinner_weather);
        Spinner spSeason = dialogView.findViewById(R.id.spinner_season);
        Spinner spOccasion = dialogView.findViewById(R.id.spinner_occasion);
        Button btnReset = dialogView.findViewById(R.id.btn_reset);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        setupSpinner(spStyle, STYLES);
        setupSpinner(spWeather, WEATHERS);
        setupSpinner(spSeason, SEASONS);
        setupSpinner(spOccasion, OCCASIONS);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();

        btnConfirm.setOnClickListener(v -> {
            // 手动筛选可以覆盖默认筛选 (暂时的)
            filterListManual(
                    spStyle.getSelectedItem().toString(),
                    spWeather.getSelectedItem().toString(),
                    spSeason.getSelectedItem().toString(),
                    spOccasion.getSelectedItem().toString()
            );
            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            applyGlobalFilters(); // 重置为默认偏好
            dialog.dismiss();
            Toast.makeText(getContext(), "已重置为默认偏好", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }

    private void filterListManual(String style, String weather, String season, String occasion) {
        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String genderPref = prefs.getString("filter_gender", "all");
        List<Outfit> filtered = new ArrayList<>();
        for (Outfit item : allData) {
            boolean mG = genderPref.equals("all") || item.gender.equals(genderPref);
            boolean m1 = style.equals("不限") || item.style.equals(style);
            boolean m2 = weather.equals("不限") || item.weather.equals(weather);
            boolean m3 = season.equals("不限") || item.season.equals(season);
            boolean m4 = occasion.equals("不限") || item.occasion.equals(occasion);
            if (mG && m1 && m2 && m3 && m4) filtered.add(item);
        }
        adapter.setList(filtered);
    }
}