package com.example.smartoutfitapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits")
public class Outfit {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;    // 标题
    public String imageUrl; // 图片地址
    public String gender;   // 性别

    // === 新增的 4 个标签 ===
    public String style;    // 风格 (如：休闲、商务)
    public String weather;  // 天气 (如：晴天、雨天)
    public String season;   // 季节 (如：春、夏)
    public String occasion; // 场景 (如：约会、上班)

    public boolean isFavorite; // 是否收藏

    // 更新后的构造函数
    public Outfit(String title, String imageUrl, String gender,
                  String style, String weather, String season, String occasion) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.gender = gender;
        // 初始化标签
        this.style = style;
        this.weather = weather;
        this.season = season;
        this.occasion = occasion;
        this.isFavorite = false;
    }
}