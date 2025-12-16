package com.example.smartoutfitapp.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartoutfitapp.MainActivity;
import com.example.smartoutfitapp.R;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int savedUid = prefs.getInt("current_uid", -1);

        if (savedUid != -1) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        EditText etUser = findViewById(R.id.et_username);
        EditText etPwd = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnReg = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(v ->
                viewModel.login(etUser.getText().toString(), etPwd.getText().toString())
        );

        btnReg.setOnClickListener(v ->
                viewModel.register(etUser.getText().toString(), etPwd.getText().toString())
        );

        viewModel.toastMessage.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );

        viewModel.loginSuccess.observe(this, user -> {
            Toast.makeText(this, "登录成功: " + user.username, Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = prefs.edit();

            // 1. 保存 ID
            editor.putInt("current_uid", user.uid);

            // 2. 恢复性别
            editor.putString("filter_gender", user.gender != null ? user.gender : "all");

            // 3. 【核心修复】恢复 4 个默认偏好
            // 如果数据库里是 null (旧数据)，就默认为 "不限"
            editor.putString("default_style", user.defStyle != null ? user.defStyle : "不限");
            editor.putString("default_weather", user.defWeather != null ? user.defWeather : "不限");
            editor.putString("default_season", user.defSeason != null ? user.defSeason : "不限");
            editor.putString("default_occasion", user.defOccasion != null ? user.defOccasion : "不限");

            editor.apply();

            navigateToMain();
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}