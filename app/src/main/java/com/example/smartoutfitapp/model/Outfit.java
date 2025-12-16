package com.example.smartoutfitapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits")
public class Outfit {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId; // 【新增】记录是谁收藏的

    public String title;
    public String imageUrl;
    public String gender;

    public String style;
    public String weather;
    public String season;
    public String occasion;

    public boolean isFavorite;

    public Outfit(String title, String imageUrl, String gender,
                  String style, String weather, String season, String occasion) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.gender = gender;
        this.style = style;
        this.weather = weather;
        this.season = season;
        this.occasion = occasion;
        this.isFavorite = false;
        this.userId = -1; // 默认值
    }
}