package com.example.smartoutfitapp.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    // 插入一个新用户
    @Insert
    void insert(User user);

    // 登录查询：根据账号和密码查找用户
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    // 根据ID获取用户信息
    @Query("SELECT * FROM users WHERE uid = :uid")
    User getUserById(int uid);

    // 更新用户信息 (例如修改性别)
    @Update
    void update(User user);
}