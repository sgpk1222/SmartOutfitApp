package com.example.smartoutfitapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartoutfitapp.model.Outfit;

import java.util.List;

@Dao
public interface OutfitDao {
    @Insert
    void insert(Outfit outfit);

    @Delete
    void delete(Outfit outfit);

    @Query("SELECT * FROM outfits WHERE userId = :uid")
    List<Outfit> getFavoritesForUser(int uid);

    @Query("SELECT * FROM outfits WHERE title = :title AND userId = :uid LIMIT 1")
    Outfit getOutfitByTitleAndUser(String title, int uid);
}