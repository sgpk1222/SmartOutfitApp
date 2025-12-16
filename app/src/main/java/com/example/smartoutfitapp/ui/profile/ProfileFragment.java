package com.example.smartoutfitapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartoutfitapp.R;
import com.example.smartoutfitapp.db.AppDatabase;
import com.example.smartoutfitapp.db.User;
import com.example.smartoutfitapp.ui.login.LoginActivity;

public class ProfileFragment extends Fragment {

    private TextView tvUsername;
    private RadioGroup rgGender;
    private RadioButton rbAll, rbMale, rbFemale;
    private Button btnLogout;
    private AppDatabase db;
    private Spinner spStyle, spWeather, spSeason, spOccasion;

    private final String[] STYLES = {"不限", "休闲", "商务"};
    private final String[] WEATHERS = {"不限", "晴天", "阴雨"};
    private final String[] SEASONS = {"不限", "春", "夏", "秋", "冬"};
    private final String[] OCCASIONS = {"不限", "工作", "居家", "出行"};

    private boolean isInitializing = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tv_username);
        rgGender = view.findViewById(R.id.rg_gender);
        rbAll = view.findViewById(R.id.rb_all);
        rbMale = view.findViewById(R.id.rb_male);
        rbFemale = view.findViewById(R.id.rb_female);
        btnLogout = view.findViewById(R.id.btn_logout);

        spStyle = view.findViewById(R.id.sp_def_style);
        spWeather = view.findViewById(R.id.sp_def_weather);
        spSeason = view.findViewById(R.id.sp_def_season);
        spOccasion = view.findViewById(R.id.sp_def_occasion);

        db = AppDatabase.getDatabase(getContext());

        // 初始化下拉框，传入对应的数据库字段名
        setupSpinner(spStyle, STYLES, "default_style", "defStyle");
        setupSpinner(spWeather, WEATHERS, "default_weather", "defWeather");
        setupSpinner(spSeason, SEASONS, "default_season", "defSeason");
        setupSpinner(spOccasion, OCCASIONS, "default_occasion", "defOccasion");

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (isInitializing) return;
            String gender = "all";
            if (checkedId == R.id.rb_male) gender = "male";
            else if (checkedId == R.id.rb_female) gender = "female";

            SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("filter_gender", gender).apply();

            // 更新数据库
            updateUserFieldInDb("gender", gender);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }

    // 设置 Spinner 的通用方法
    private void setupSpinner(Spinner spinner, String[] items, String spKey, String dbField) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitializing) return;
                String selected = items[position];

                // 1. 存缓存
                SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                prefs.edit().putString(spKey, selected).apply();

                // 2. 存数据库 (持久化记忆)
                updateUserFieldInDb(dbField, selected);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfoAndSettings();
    }

    private void loadUserInfoAndSettings() {
        isInitializing = true;

        SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int currentUid = prefs.getInt("current_uid", -1);

        if (currentUid == -1) tvUsername.setText("未登录");
        else {
            User user = db.userDao().getUserById(currentUid);
            if (user != null) tvUsername.setText(user.username);
        }

        // 回显性别
        String gender = prefs.getString("filter_gender", "all");
        if ("male".equals(gender)) rbMale.setChecked(true);
        else if ("female".equals(gender)) rbFemale.setChecked(true);
        else rbAll.setChecked(true);

        // 回显下拉框
        setSpinnerSelection(spStyle, STYLES, prefs.getString("default_style", "不限"));
        setSpinnerSelection(spWeather, WEATHERS, prefs.getString("default_weather", "不限"));
        setSpinnerSelection(spSeason, SEASONS, prefs.getString("default_season", "不限"));
        setSpinnerSelection(spOccasion, OCCASIONS, prefs.getString("default_occasion", "不限"));

        isInitializing = false;
    }

    private void setSpinnerSelection(Spinner spinner, String[] items, String value) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // 通用的数据库更新方法
    private void updateUserFieldInDb(String fieldType, String value) {
        SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int currentUid = prefs.getInt("current_uid", -1);
        if (currentUid != -1) {
            User user = db.userDao().getUserById(currentUid);
            if (user != null) {
                // 根据字段类型更新对应属性
                switch (fieldType) {
                    case "gender": user.gender = value; break;
                    case "defStyle": user.defStyle = value; break;
                    case "defWeather": user.defWeather = value; break;
                    case "defSeason": user.defSeason = value; break;
                    case "defOccasion": user.defOccasion = value; break;
                }
                db.userDao().update(user);
            }
        }
    }
}