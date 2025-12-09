package com.example.smartoutfitapp.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    // 1. 根据城市名查询 (例如: q=Beijing)
    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units, // 单位: metric=公制(摄氏度)
            @Query("lang") String lang    // 语言: zh_cn=中文
    );

    // 2. 根据经纬度查询 (例如: lat=30, lon=120)
    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByLocation(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}