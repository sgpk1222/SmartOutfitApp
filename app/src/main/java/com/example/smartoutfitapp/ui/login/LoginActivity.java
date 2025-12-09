package com.example.smartoutfitapp.ui.login;

import android.content.Intent;
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
        setContentView(R.layout.activity_login);

        // 1. 获取 ViewModel 实例
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 2. 绑定 UI 控件
        EditText etUser = findViewById(R.id.et_username);
        EditText etPwd = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnReg = findViewById(R.id.btn_register);

        // 3. 点击事件 -> 调用 ViewModel 方法
        btnLogin.setOnClickListener(v ->
                viewModel.login(etUser.getText().toString(), etPwd.getText().toString())
        );

        btnReg.setOnClickListener(v ->
                viewModel.register(etUser.getText().toString(), etPwd.getText().toString())
        );

        // 4. 观察数据变化 -> 更新 UI
        viewModel.toastMessage.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );

        viewModel.loginSuccess.observe(this, user -> {
            Toast.makeText(this, "欢迎回来: " + user.username, Toast.LENGTH_SHORT).show();
            // 登录成功，跳转到主页
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // 关闭登录页
        });
    }
}