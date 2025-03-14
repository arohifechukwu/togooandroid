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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dbReference;
    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private TextView signupLink, forgotPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        loginButton.setOnClickListener(v -> loginUser());

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

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
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            String uid = currentUser.getUid();
                            validateUserRole(uid);
                        } else {
                            Toast.makeText(this, "Account does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Login failed! Check credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ✅ Validate User Role and Handle Status Changes
     */
    private void validateUserRole(String uid) {
        // Check if user exists in Firebase Authentication before checking Realtime Database
        dbReference.child("customer").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleUserStatus(snapshot, uid);
                } else {
                    // Check in other nodes
                    checkUserRoleInNode("driver", uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRoleInNode(String node, String uid) {
        dbReference.child(node).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleUserStatus(snapshot, uid);
                } else {
                    if ("driver".equals(node)) checkUserRoleInNode("restaurant", uid);
                    else if ("restaurant".equals(node)) checkUserRoleInNode("admin", uid);
                    else if ("admin".equals(node)) checkAdminAccess(uid);
                    else Toast.makeText(LoginActivity.this, "User role not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Handle Different Account Statuses
     */
    private void handleUserStatus(DataSnapshot snapshot, String uid) {
        String role = snapshot.child("role").getValue(String.class);
        String status = snapshot.child("status").exists() ? snapshot.child("status").getValue(String.class) : "approved";

        if ("suspended".equals(status)) {
            Toast.makeText(LoginActivity.this, "Account Suspended. Contact Administrator", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("deleted".equals(status)) {
            auth.signOut(); // Ensure user is signed out
            Toast.makeText(LoginActivity.this, "Account does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("pending".equals(status)) {
            startActivity(new Intent(LoginActivity.this, RegistrationStatusActivity.class));
            finish();
            return;
        }

        navigateToDashboard(role);
    }

    /**
     * ✅ Admin Role Validation
     */
    private void checkAdminAccess(String uid) {
        dbReference.child("admin").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && "admin".equals(snapshot.child("role").getValue(String.class))) {
                    navigateToDashboard("admin");
                } else {
                    Toast.makeText(LoginActivity.this, "Access Denied: Not an Admin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Navigate User Based on Role
     */
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