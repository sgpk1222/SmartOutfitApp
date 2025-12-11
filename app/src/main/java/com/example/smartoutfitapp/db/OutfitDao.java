package com.example.smartoutfitapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartoutfitapp.model.Outfit;

import java.util.List;

@Dao
public interface OutfitDao {
    // 收藏一个穿搭 (插入数据)
    @Insert
    void insert(Outfit outfit);

    // 取消收藏 (删除数据)
    @Delete
    void delete(Outfit outfit);

    // 查询所有收藏的穿搭 (以后"智能试衣"模块要用这个)
    @Query("SELECT * FROM outfits")
    List<Outfit> getAllFavorites();

    // 查询某个特定的穿搭 (根据标题查，用来判断当前图片是否已经红心了)
    @Query("SELECT * FROM outfits WHERE title = :title LIMIT 1")
    Outfit getOutfitByTitle(String title);
}