package com.example.mobilafet.network;

import com.example.mobilafet.models.Report;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Kullanıcı Kaydetme (users koleksiyonu)
    public static void saveUser(String uid, String username, String email, OnSuccessListener<Void> success, OnFailureListener failure) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("fcmToken", ""); // Başlangıçta boş

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }

    // Cihaz FCM Token'ını Güncelleme
    public static void updateFcmToken(String uid, String token) {
        db.collection("users").document(uid).update("fcmToken", token);
    }

    // Yeni Afet/Sarsıntı bildirimi kaydetme
    public static void saveReport(Report report, OnSuccessListener<String> success, OnFailureListener failure) {
        DocumentReference docRef = db.collection("reports").document();
        docRef.set(report)
                .addOnSuccessListener(aVoid -> success.onSuccess(docRef.getId()))
                .addOnFailureListener(failure);
    }

    // Aktif bildirimleri tarihe göre azalan şekilde getirme
    public static void getActiveNotifications(OnSuccessListener<QuerySnapshot> success, OnFailureListener failure) {
        db.collection("notifications").whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(success).addOnFailureListener(failure);
    }

    // Sadece giriş yapan kullanıcının tüm raporlarını tarihe göre sıralı çekme
    public static void getUserReports(String userId, OnSuccessListener<QuerySnapshot> success, OnFailureListener failure) {
        db.collection("reports")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }

    // Sadece giriş yapan kullanıcının belirli bir türdeki (type) raporlarını tarihe göre sıralı çekme
    public static void getUserReportsByType(String userId, String type, OnSuccessListener<QuerySnapshot> success, OnFailureListener failure) {
        db.collection("reports")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }
}