package com.example.mobilafet.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.example.mobilafet.R;
import com.example.mobilafet.base.BaseToolbarActivity;
import com.google.android.material.button.MaterialButton;

/**
 * Structural screen for {@link com.example.mobilafet.models.UserProfile} when auth/settings arrive.
 */
public class ProfileActivity extends BaseToolbarActivity {

    @Override
    protected int layoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
