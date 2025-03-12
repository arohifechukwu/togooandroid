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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dbReference; // ✅ Switched to Realtime Database
    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private TextView signupLink, forgotPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference(); // ✅ Reference to Realtime Database root

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to SignupActivity
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Navigate to PasswordResetActivity
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

//    private void validateUserRole(String uid) {
//        // ✅ Check each node for the user role
//        checkUserRoleInNode("customer", uid);
//    }
//
//    private void checkUserRoleInNode(String node, String uid) {
//        dbReference.child(node).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists() && snapshot.child("role").getValue() != null) {
//                    String role = snapshot.child("role").getValue(String.class);
//                    navigateToDashboard(role);
//                } else {
//                    // If not found in "customer", check in "driver"
//                    if ("customer".equals(node)) checkUserRoleInNode("driver", uid);
//                    else if ("driver".equals(node)) checkUserRoleInNode("restaurant", uid);
//                    else if ("restaurant".equals(node)) checkUserRoleInNode("admin", uid);
//                    else Toast.makeText(LoginActivity.this, "User role not found!", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    private void validateUserRole(String uid) {
        checkUserRoleInNode("customer", uid);
    }

    private void checkUserRoleInNode(String node, String uid) {
        dbReference.child(node).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("role").getValue() != null) {
                    String role = snapshot.child("role").getValue(String.class);
                    String status = snapshot.child("status").exists() ? snapshot.child("status").getValue(String.class) : "approved";

                    if ("pending".equals(status)) {
                        startActivity(new Intent(LoginActivity.this, RegistrationStatusActivity.class));
                        finish();
                        return;
                    }

                    navigateToDashboard(role);
                } else {
                    if ("customer".equals(node)) checkUserRoleInNode("driver", uid);
                    else if ("driver".equals(node)) checkUserRoleInNode("restaurant", uid);
                    else if ("restaurant".equals(node)) checkUserRoleInNode("admin", uid);
                    else Toast.makeText(LoginActivity.this, "User role not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "customer":
                intent = new Intent(LoginActivity.this, CustomerLandingActivity.class);
                break;
            case "driver":
                intent = new Intent(LoginActivity.this, DriverLandingActivity.class);
                break;
            case "restaurant":
                intent = new Intent(LoginActivity.this, RestaurantLandingActivity.class);
                break;
            case "admin":
                intent = new Intent(LoginActivity.this, AdminLandingActivity.class);
                break;
            default:
                intent = new Intent(LoginActivity.this, CustomerLandingActivity.class);
                break;
        }

        startActivity(intent);
        finish();
    }
}