package com.example.smartoutfitapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// @Entity 表示这是一张数据库表，表名叫 "users"
@Entity(tableName = "users")
public class User {

    // 主键，autoGenerate = true 表示 ID 会自动增加 (1, 2, 3...)
    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String username; // 账号
    public String password; // 密码
    public String gender;   // 性别: "male"(男), "female"(女), "unspecified"(未设置)

    // 构造函数
    public User(String username, String password, String gender) {
        this.username = username;
        this.password = password;
        this.gender = gender;
    }
}