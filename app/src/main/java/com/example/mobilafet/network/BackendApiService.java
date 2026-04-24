package com.example.mobilafet.network;

import com.example.mobilafet.models.ReportNotificationRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BackendApiService {
    @POST("/send-report-notification")
    Call<Void> sendReportNotification(@Body ReportNotificationRequest request);
}