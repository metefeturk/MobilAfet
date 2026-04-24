package com.example.mobilafet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilafet.R;
import com.example.mobilafet.network.FirestoreHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextInputEditText etUsername = findViewById(R.id.et_register_username);
        TextInputEditText etEmail = findViewById(R.id.et_register_email);
        TextInputEditText etPassword = findViewById(R.id.et_register_password);
        TextInputEditText etConfirmPassword = findViewById(R.id.et_register_confirm_password);
        MaterialButton btnRegister = findViewById(R.id.btn_register);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (username.isEmpty()) {
                etUsername.setError("Kullanıcı adı gerekli");
                etUsername.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("E-posta adresi gerekli");
                etEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Geçerli bir e-posta adresi giriniz");
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                etPassword.setError("Şifre en az 6 karakter olmalıdır");
                etPassword.requestFocus();
                return;
            }
            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Lütfen şifrenizi tekrar girin");
                etConfirmPassword.requestFocus();
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Şifreler eşleşmiyor");
                etConfirmPassword.requestFocus();
                return;
            }

            // Firebase Authentication ile kayıt
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user.getUid(), username, email);
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Kayıt başarısız";
                            Toast.makeText(RegisterActivity.this, "Hata: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void saveUserToFirestore(String uid, String username, String email) {
        FirestoreHelper.saveUser(uid, username, email,
                aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                },
                e -> {
                    Toast.makeText(RegisterActivity.this, "Kullanıcı verisi kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Authentication oldu ama firestore hata verdiyse yine de giriş yaptırabiliriz
                    navigateToMain();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        // Login ve Register activity'leri yığından (stack) temizle ki geri tuşuyla geri dönülmesin
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}