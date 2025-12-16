package com.example.smartoutfitapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String username;
    public String password;
    public String gender;   // 性别

    // === 新增：4个默认筛选偏好 ===
    public String defStyle;
    public String defWeather;
    public String defSeason;
    public String defOccasion;

    // 构造函数
    public User(String username, String password, String gender) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        // 默认值设为 "不限"
        this.defStyle = "不限";
        this.defWeather = "不限";
        this.defSeason = "不限";
        this.defOccasion = "不限";
    }
}