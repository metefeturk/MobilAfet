package com.example.mobilafet.ui;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.mobilafet.utils.DisplayFormatter;

/**
 * Small UI helper for consistent empty-state text until real data is bound.
 */
public final class PlaceholderBinder {

    private PlaceholderBinder() {
    }

    public static void applyDashPlaceholders(@Nullable TextView... fields) {
        if (fields == null) {
            return;
        }
        String placeholder = DisplayFormatter.notAvailable();
        for (TextView field : fields) {
            if (field != null) {
                field.setText(placeholder);
            }
        }
    }
}
