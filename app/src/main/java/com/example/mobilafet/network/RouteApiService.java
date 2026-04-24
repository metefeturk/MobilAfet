package com.example.mobilafet.network;

import com.example.mobilafet.models.RouteRequest;
import com.example.mobilafet.models.RouteResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RouteApiService {
    @POST("v2/directions/driving-car")
    Call<RouteResponse> getDirections(
        @Header("Authorization") String authHeader,
        @Body RouteRequest request
    );
}