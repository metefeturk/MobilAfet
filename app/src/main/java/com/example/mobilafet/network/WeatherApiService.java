package com.example.mobilafet.network;

import com.example.mobilafet.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
        @Query("lat") double lat,
        @Query("lon") double lon,
        @Query("appid") String appId,
        @Query("units") String units,
        @Query("lang") String lang
    );
}