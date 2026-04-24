package com.example.mobilafet.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mobilafet.R;
import com.example.mobilafet.fragments.EvacuationFragment;
import com.example.mobilafet.fragments.HomeFragment;
import com.example.mobilafet.fragments.ReportFragment;
import com.example.mobilafet.fragments.NotificationsFragment;
import com.example.mobilafet.fragments.ProfileFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.mobilafet.network.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNotificationPermission();
        setupFCMToken(); // Sisteme cihazı kaydet

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                toolbar.setTitle("Mobil Afet");
            } else if (itemId == R.id.nav_map) {
                selectedFragment = new EvacuationFragment();
                toolbar.setTitle("Harita ve Tahliye");
            } else if (itemId == R.id.nav_report) {
                selectedFragment = new ReportFragment();
                toolbar.setTitle("Durum Bildir");
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
                toolbar.setTitle("Bildirimler");
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                toolbar.setTitle("Profilim");
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // İlk açılışta HomeFragment gösterilsin
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    // Android 13+ cihazlar için bildirim izni kontrolü
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // Cihazın FCM Token'ını alır ve Firestore'daki kullanıcı verisine yazar (Backend bu token'a bildirim atacak)
    private void setupFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) return;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) FirestoreHelper.updateFcmToken(user.getUid(), task.getResult());
        });
    }
}
