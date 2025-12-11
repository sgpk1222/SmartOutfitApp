package com.example.smartoutfitapp.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartoutfitapp.model.Outfit;

// 注意：version = 1 不变，但在开发阶段如果报错可能需要卸载APP重装
@Database(entities = {User.class, Outfit.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract OutfitDao outfitDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_outfit_db")
                            .allowMainThreadQueries() // 允许主线程操作
                            .fallbackToDestructiveMigration() // 如果表结构变了，直接清空重建(开发专用)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}