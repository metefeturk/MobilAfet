package com.example.mobilafet.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FirmsApiService {
    @GET("api/area/csv/{apiKey}/VIIRS_SNPP_NRT/world/1")
    Call<String> getFireHotspots(@Path("apiKey") String apiKey);
}