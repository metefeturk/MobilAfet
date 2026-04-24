package com.example.mobilafet.network;

import com.example.mobilafet.models.EarthquakeResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface UsgsApiService {
    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    Call<EarthquakeResponse> getRecentEarthquakes();
}