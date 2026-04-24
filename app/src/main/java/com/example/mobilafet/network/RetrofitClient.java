package com.example.mobilafet.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    
    public static Retrofit getClient(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                // ScalarsConverterFactory is needed to parse raw String bodies (like CSV)
                .addConverterFactory(ScalarsConverterFactory.create()) 
                // GsonConverterFactory maps standard JSON to Java Objects
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}