package com.example.mobilafet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilafet.R;
import com.example.mobilafet.adapters.NotificationAdapter;
import com.example.mobilafet.models.AppNotification;
import com.example.mobilafet.network.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ProgressBar progressBar = view.findViewById(R.id.progress_notifications);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_notifications);
        RecyclerView recyclerView = view.findViewById(R.id.rv_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirestoreHelper.getActiveNotifications(queryDocumentSnapshots -> {
            progressBar.setVisibility(View.GONE);
            if (queryDocumentSnapshots.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                List<AppNotification> list = queryDocumentSnapshots.toObjects(AppNotification.class);
                recyclerView.setAdapter(new NotificationAdapter(list));
            }
        }, e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}