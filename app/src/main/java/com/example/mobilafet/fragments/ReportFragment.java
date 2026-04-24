package com.example.mobilafet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilafet.R;
import com.example.mobilafet.models.Report;
import com.example.mobilafet.models.ReportNotificationRequest;
import com.example.mobilafet.network.BackendApiService;
import com.example.mobilafet.network.FirestoreHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportFragment extends Fragment {

    private TextInputEditText etTitle, etCity, etDesc;
    private Spinner spinnerType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        etTitle = view.findViewById(R.id.et_report_title);
        etCity = view.findViewById(R.id.et_report_city);
        etDesc = view.findViewById(R.id.et_report_desc);
        spinnerType = view.findViewById(R.id.spinner_report_type);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit_report);

        // Tür seçimleri
        String[] types = {"Deprem", "Yangın", "Sel / Su Baskını", "Tahliye Talebi", "Diğer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> submitReport());
        return view;
    }

    private void submitReport() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        String type = spinnerType.getSelectedItem().toString();

        if (title.isEmpty() || city.isEmpty() || desc.isEmpty()) {
            Toast.makeText(getContext(), "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Report report = new Report();
        report.title = title;
        report.description = desc;
        report.type = type;
        report.city = city;
        report.status = "yeni";
        report.createdAt = System.currentTimeMillis();
        report.userId = user != null ? user.getUid() : "anonim";
        report.latitude = 41.0; // İleride cihaz konumundan alınacak
        report.longitude = 29.0;

        FirestoreHelper.saveReport(report, reportId -> {
            Toast.makeText(getContext(), "Bildirim başarıyla gönderildi!", Toast.LENGTH_LONG).show();
            etTitle.setText(""); etCity.setText(""); etDesc.setText("");

            // Node.js Backend'e Push Notification atması için HTTP isteği yapıyoruz
            // Emulator üzerinden localhost'a erişmek için 10.0.2.2 kullanılır.
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:3000/").addConverterFactory(GsonConverterFactory.create()).build();
            BackendApiService api = retrofit.create(BackendApiService.class);
            
            ReportNotificationRequest req = new ReportNotificationRequest(reportId, title, desc, type, city, report.userId);
            api.sendReportNotification(req).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {}
                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });

        }, e -> {
            Toast.makeText(getContext(), "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}