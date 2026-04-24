package com.example.mobilafet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilafet.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Brief branded entry; hands off to {@link MainActivity}. No data loading in this phase.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long DISPLAY_MS = 2000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openMain = new Runnable() {
        @Override
        public void run() {
            // Firebase Oturum Kontrolü
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        View content = findViewById(R.id.splash_content);
        content.animate().alpha(1.0f).setDuration(1200).start();
        
        handler.postDelayed(openMain, DISPLAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openMain);
        super.onDestroy();
    }
}
