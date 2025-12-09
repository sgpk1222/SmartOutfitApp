package com.example.smartoutfitapp.network;

import java.util.List;

// 这是一个对应 OpenWeatherMap 返回的 JSON 结构类
public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public String name; // 城市名

    public static class Main {
        public float temp; // 温度
    }

    public static class Weather {
        public String description; // 天气描述 (如 light rain)
    }
}