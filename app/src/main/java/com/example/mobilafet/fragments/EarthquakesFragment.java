package com.example.mobilafet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilafet.R;
import com.example.mobilafet.adapters.EarthquakeAdapter;
import com.example.mobilafet.models.EarthquakeResponse;
import com.example.mobilafet.network.RetrofitClient;
import com.example.mobilafet.network.UsgsApiService;
import com.example.mobilafet.utils.ApiConfig;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EarthquakesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_earthquakes, container, false);
        ProgressBar progressBar = view.findViewById(R.id.progress_earthquakes);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_earthquakes);
        RecyclerView recyclerView = view.findViewById(R.id.rv_earthquakes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        UsgsApiService service = RetrofitClient.getClient(ApiConfig.USGS_BASE_URL).create(UsgsApiService.class);
        service.getRecentEarthquakes().enqueue(new Callback<EarthquakeResponse>() {
            @Override
            public void onResponse(Call<EarthquakeResponse> call, Response<EarthquakeResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().features != null) {
                    List<EarthquakeResponse.Feature> features = response.body().features;
                    recyclerView.setAdapter(new EarthquakeAdapter(features));
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Veri bulunamadı.");
                }
            }
            @Override
            public void onFailure(Call<EarthquakeResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Bağlantı hatası!");
            }
        });

        return view;
    }
}