package com.example.smartoutfitapp.ui.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.smartoutfitapp.R;
import com.example.smartoutfitapp.network.WeatherResponse;
import com.example.smartoutfitapp.network.WeatherService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {

    // TODO: 替换为你的 API Key
    private static final String API_KEY = "5b28d7f36796e35bd52a2e077db0221f";
    private static final String BASE_URL = "https://api.openweathermap.org/";

    private TextView tvCity, tvTemp, tvDesc;
    private EditText etCityInput;
    private WeatherService weatherService;
    private LocationManager locationManager;

    // 定位回调：当系统获取到最新位置时触发
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            fetchWeatherByLocation(location.getLatitude(), location.getLongitude());
            if (locationManager != null) {
                locationManager.removeUpdates(this); // 获取一次后即停止，省电
            }
        }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(@NonNull String provider) {}
        @Override public void onProviderDisabled(@NonNull String provider) {}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        tvCity = view.findViewById(R.id.tv_city);
        tvTemp = view.findViewById(R.id.tv_temperature);
        tvDesc = view.findViewById(R.id.tv_description);
        etCityInput = view.findViewById(R.id.et_city_input);
        Button btnSearch = view.findViewById(R.id.btn_search);
        Button btnLocation = view.findViewById(R.id.btn_location);

        // 初始化 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherService = retrofit.create(WeatherService.class);

        btnSearch.setOnClickListener(v -> {
            String city = etCityInput.getText().toString().trim();
            if (!city.isEmpty()) fetchWeatherByCity(city);
        });

        btnLocation.setOnClickListener(v -> startLocation());

        startLocation(); // 页面加载即定位

        return view;
    }

    private void startLocation() {
        if (getActivity() == null) return;
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        tvCity.setText("定位中...");

        // 策略：优先取缓存位置，若无缓存则请求系统刷新
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation == null) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastLocation != null) {
            fetchWeatherByLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            // 请求实时更新
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        }
    }

    private void fetchWeatherByLocation(double lat, double lon) {
        weatherService.getWeatherByLocation(lat, lon, API_KEY, "metric", "zh_cn").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    tvCity.setText("获取失败");
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvCity.setText("网络错误");
            }
        });
    }

    private void fetchWeatherByCity(String city) {
        weatherService.getWeatherByCity(city, API_KEY, "metric", "zh_cn").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(getContext(), "未找到该城市", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(WeatherResponse weather) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            tvCity.setText(weather.name);
            tvTemp.setText(Math.round(weather.main.temp) + "°C");
            if (weather.weather != null && !weather.weather.isEmpty()) {
                tvDesc.setText(weather.weather.get(0).description);
            }
        });
    }
}