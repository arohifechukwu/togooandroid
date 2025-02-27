package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private TextView signupLink, forgotPasswordLink; // Added for password reset navigation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink); // Initialize forgot password link

        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to SignupActivity when "Haven't Registered? Signup" is clicked
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Navigate to PasswordResetActivity when "Forgot Your Password? Reset It." is clicked
        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, PasswordResetActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        validateUserRole(uid);
                    } else {
                        Toast.makeText(this, "Login failed! Check credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void validateUserRole(String uid) {
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().getString("role");

                Intent intent;
                if ("customer".equals(role)) {
                    intent = new Intent(LoginActivity.this, CustomerLandingActivity.class);
                } else if ("driver".equals(role)) {
                    intent = new Intent(LoginActivity.this, DriverLandingActivity.class);
                } else if ("restaurant".equals(role)) {
                    intent = new Intent(LoginActivity.this, RestaurantLandingActivity.class);
                } else if ("admin".equals(role)) {
                    intent = new Intent(LoginActivity.this, AdminLandingActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, CustomerLandingActivity.class);
                }

                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "User role not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}