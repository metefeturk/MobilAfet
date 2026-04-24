package com.example.mobilafet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.mobilafet.R;
import com.example.mobilafet.base.BaseToolbarActivity;
import com.example.mobilafet.ui.PlaceholderBinder;

/**
 * Structural screen for {@link com.example.mobilafet.models.Disaster} details once API data exists.
 */
public class DisasterDetailActivity extends BaseToolbarActivity {

    @Override
    protected int layoutResource() {
        return R.layout.activity_disaster_detail;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaceholderBinder.applyDashPlaceholders(
                findViewById(R.id.value_disaster_type),
                findViewById(R.id.value_severity),
                findViewById(R.id.value_location),
                findViewById(R.id.value_time),
                findViewById(R.id.value_description)
        );
    }
}
