package com.example.mobilafet.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.example.mobilafet.R;
import com.example.mobilafet.base.BaseToolbarActivity;
import com.example.mobilafet.ui.PlaceholderBinder;

/**
 * Structural screen for {@link com.example.mobilafet.models.EvacuationStep} content and routing later.
 */
public class EvacuationActivity extends BaseToolbarActivity {

    @Override
    protected int layoutResource() {
        return R.layout.activity_evacuation;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaceholderBinder.applyDashPlaceholders(
                findViewById(R.id.value_safe_zone),
                findViewById(R.id.value_evacuation_steps),
                findViewById(R.id.value_emergency_notes)
        );
    }
}
