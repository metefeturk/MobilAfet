package com.example.mobilafet.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilafet.R;
import com.example.mobilafet.activities.LoginActivity;
import com.example.mobilafet.adapters.ReportAdapter;
import com.example.mobilafet.models.Report;
import com.example.mobilafet.network.FirestoreHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvCreatedAt;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tv_profile_username);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvCreatedAt = view.findViewById(R.id.tv_profile_created_at);
        progressBar = view.findViewById(R.id.progress_profile);
        recyclerView = view.findViewById(R.id.rv_my_reports);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);
        Spinner spinnerFilter = view.findViewById(R.id.spinner_filter);

        String[] filters = {"Tümü", "Deprem", "Yangın", "Sel / Su Baskını", "Tahliye Talebi", "Diğer"};
        spinnerFilter.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filters));

        loadUserProfile();

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadUserReports(filters[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        progressBar.setVisibility(View.GONE);
                        if (doc.exists()) {
                            tvUsername.setText(doc.getString("username"));
                            tvEmail.setText(doc.getString("email"));
                            Long createdAt = doc.getLong("createdAt");
                            if (createdAt != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("tr"));
                                tvCreatedAt.setText("Kayıt: " + sdf.format(new Date(createdAt)));
                            }
                        } else {
                            Toast.makeText(getContext(), "Kullanıcı verisi bulunamadı", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadUserReports(String filter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (filter.equals("Tümü")) {
            FirestoreHelper.getUserReports(user.getUid(), queryDocumentSnapshots -> {
                List<Report> list = queryDocumentSnapshots.toObjects(Report.class);
                recyclerView.setAdapter(new ReportAdapter(list));
            }, e -> Toast.makeText(getContext(), "Yüklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Type'a göre filtrele ve sırala
            FirestoreHelper.getUserReportsByType(user.getUid(), filter, queryDocumentSnapshots -> {
                List<Report> list = queryDocumentSnapshots.toObjects(Report.class);
                recyclerView.setAdapter(new ReportAdapter(list));
            }, e -> Toast.makeText(getContext(), "Yüklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}