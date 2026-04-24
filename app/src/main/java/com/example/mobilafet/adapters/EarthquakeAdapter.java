package com.example.mobilafet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilafet.R;
import com.example.mobilafet.models.EarthquakeResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EarthquakeAdapter extends RecyclerView.Adapter<EarthquakeAdapter.ViewHolder> {
    private List<EarthquakeResponse.Feature> features;

    public EarthquakeAdapter(List<EarthquakeResponse.Feature> features) {
        this.features = features;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_earthquake, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EarthquakeResponse.Feature feature = features.get(position);
        holder.mag.setText(String.format(Locale.US, "%.1f", feature.properties.mag));
        holder.place.setText(feature.properties.place);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("tr"));
        holder.time.setText(sdf.format(new Date(feature.properties.time)));
    }

    @Override
    public int getItemCount() {
        return features != null ? features.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mag, place, time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mag = itemView.findViewById(R.id.tv_eq_mag);
            place = itemView.findViewById(R.id.tv_eq_place);
            time = itemView.findViewById(R.id.tv_eq_time);
        }
    }
}