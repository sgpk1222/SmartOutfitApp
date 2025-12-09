package com.example.smartoutfitapp.ui.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smartoutfitapp.db.AppDatabase;
import com.example.smartoutfitapp.db.User;

public class LoginViewModel extends AndroidViewModel {
    private AppDatabase db;
    // 观察者模式：界面会监听这两个变量，一旦变动，界面自动更新
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
        // 查询数据库
        User user = db.userDao().login(username, pwd);
        if (user != null) {
            loginSuccess.setValue(user); // 通知界面登录成功
        } else {
            toastMessage.setValue("账号或密码错误");
        }
    }

    public void register(String username, String pwd) {
        if (username.isEmpty() || pwd.isEmpty()) {
            toastMessage.setValue("请填写完整信息");
            return;
        }
        User newUser = new User(username, pwd, "unspecified");
        try {
            db.userDao().insert(newUser);
            toastMessage.setValue("注册成功，请点击登录");
        } catch (Exception e) {
            toastMessage.setValue("注册失败，可能账号已存在");
        }
    }
}