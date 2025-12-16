package com.example.smartoutfitapp.ui.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smartoutfitapp.db.AppDatabase;
import com.example.smartoutfitapp.db.User;

public class LoginViewModel extends AndroidViewModel {
    private AppDatabase db;
    public MutableLiveData<User> loginSuccess = new MutableLiveData<>();
    public MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public void login(String username, String pwd) {
        if (username.isEmpty() || pwd.isEmpty()) {
            toastMessage.setValue("账号密码不能为空");
            return;
        }
        User user = db.userDao().login(username, pwd);
        if (user != null) {
            loginSuccess.setValue(user);
        } else {
            toastMessage.setValue("账号或密码错误");
        }
    }

    public void register(String username, String pwd) {
        if (username.isEmpty() || pwd.isEmpty()) {
            toastMessage.setValue("请填写完整信息");
            return;
        }
        // 这里的构造函数会自动把 defStyle 等设为 "不限"
        User newUser = new User(username, pwd, "unspecified");
        try {
            db.userDao().insert(newUser);
            toastMessage.setValue("注册成功，请点击登录");
        } catch (Exception e) {
            toastMessage.setValue("注册失败");
        }
    }
}