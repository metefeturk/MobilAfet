package com.example.mobilafet.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilafet.R;
import com.example.mobilafet.models.FireHotspot;
import com.example.mobilafet.models.RouteRequest;
import com.example.mobilafet.models.RouteResponse;
import com.example.mobilafet.models.WeatherResponse;
import com.example.mobilafet.models.EarthquakeResponse;
import com.example.mobilafet.network.FirmsApiService;
import com.example.mobilafet.network.RetrofitClient;
import com.example.mobilafet.network.RouteApiService;
import com.example.mobilafet.network.UsgsApiService;
import com.example.mobilafet.network.WeatherApiService;
import com.example.mobilafet.parsers.CsvParser;
import com.example.mobilafet.utils.ApiConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private TextView tvWeatherInfo, tvFireInfo, tvEarthquakeSummary;
    private ProgressBar progWeather, progressFire;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWeatherInfo = view.findViewById(R.id.tv_weather_info);
        tvFireInfo = view.findViewById(R.id.tv_fire_info);
        tvEarthquakeSummary = view.findViewById(R.id.tv_earthquake_summary);
        progWeather = view.findViewById(R.id.progress_weather);
        progressFire = view.findViewById(R.id.progress_fire);
        MaterialCardView cardEarthquakes = view.findViewById(R.id.card_go_earthquakes);

        cardEarthquakes.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_nav);
                // Deprem detaylarına gitmek isterseniz yeni Fragment'ı direkt replace edebiliriz
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EarthquakesFragment())
                        .addToBackStack(null).commit();
            }
        });

        fetchWeatherData();
        fetchFirmsData();
        fetchEarthquakeSummary();

        return view;
    }

    private void fetchWeatherData() {
        WeatherApiService service = RetrofitClient.getClient(ApiConfig.OPENWEATHER_BASE_URL).create(WeatherApiService.class);
        service.getCurrentWeather(41.38, 33.78, ApiConfig.OPENWEATHER_API_KEY, "metric", "tr").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                progWeather.setVisibility(View.GONE);
                tvWeatherInfo.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse body = response.body();
                    double temp = body.main != null ? body.main.temp : 0.0;
                    String desc = (body.weather != null && !body.weather.isEmpty()) ? body.weather.get(0).description : "N/A";
                    tvWeatherInfo.setText(temp + "°C, " + desc);
                } else {
                    tvWeatherInfo.setText("Veri alınamadı");
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                progWeather.setVisibility(View.GONE);
                tvWeatherInfo.setVisibility(View.VISIBLE);
                tvWeatherInfo.setText("Bağlantı hatası");
            }
        });
    }

    private void fetchFirmsData() {
        FirmsApiService service = RetrofitClient.getClient(ApiConfig.FIRMS_BASE_URL).create(FirmsApiService.class);
        service.getFireHotspots(ApiConfig.FIRMS_API_KEY).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Arkaplan thread - Skipped Frames optimizasyonu
                    new Thread(() -> {
                        try {
                            List<FireHotspot> hotspots = CsvParser.parseFirmsCsv(response.body());
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    progressFire.setVisibility(View.GONE);
                                    tvFireInfo.setVisibility(View.VISIBLE);
                                    tvFireInfo.setText("Tespit edilen nokta: " + hotspots.size());
                                });
                            }
                        } catch (Exception e) { Log.e(TAG, "CSV error", e); }
                    }).start();
                } else {
                    progressFire.setVisibility(View.GONE);
                    tvFireInfo.setVisibility(View.VISIBLE);
                    tvFireInfo.setText("Veri alınamadı");
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressFire.setVisibility(View.GONE);
                tvFireInfo.setVisibility(View.VISIBLE);
                tvFireInfo.setText("Bağlantı hatası");
            }
        });
    }

    private void fetchEarthquakeSummary() {
        UsgsApiService service = RetrofitClient.getClient(ApiConfig.USGS_BASE_URL).create(UsgsApiService.class);
        service.getRecentEarthquakes().enqueue(new Callback<EarthquakeResponse>() {
            @Override
            public void onResponse(Call<EarthquakeResponse> call, Response<EarthquakeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().features != null) {
                    tvEarthquakeSummary.setText("Kayıtlı Sarsıntı: " + response.body().features.size() + "\nDokunarak listeyi görüntüle");
                } else {
                    tvEarthquakeSummary.setText("Veri alınamadı\nYeniden denemek için dokunun");
                }
            }
            @Override
            public void onFailure(Call<EarthquakeResponse> call, Throwable t) {
                tvEarthquakeSummary.setText("Bağlantı hatası");
            }
        });
    }
}