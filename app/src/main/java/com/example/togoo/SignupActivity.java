package com.example.togoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dbReference;
    private EditText inputName, inputEmail, inputPhone, inputAddress, inputPassword, inputConfirmPassword;
    private CheckBox termsCheckbox;
    private Button signupButton;
    private TextView loginLink, passwordHint;
    private ProgressDialog progressDialog; // Added progress dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference("customer"); // ✅ Realtime Database reference

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputAddress = findViewById(R.id.inputAddress);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        signupButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);
        passwordHint = findViewById(R.id.passwordHint);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Registering..."); // ✅ Display before Firebase operations

        signupButton.setOnClickListener(v -> registerUser());

        // Navigate to LoginActivity when login link is clicked
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Navigate to RegistrationActivity when register business link is clicked
        TextView registerBusinessLink = findViewById(R.id.registerBusinessLink);
        registerBusinessLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // Navigate to Admin RegistrationActivity when register business link is clicked
        TextView registerAdmin = findViewById(R.id.registerAdmin);
        registerAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, AdminRegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(address) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) ||
                !termsCheckbox.isChecked()) {
            Toast.makeText(this, "All fields are required & accept terms", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters, contain letters, numbers, and symbols", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the "Registering..." progress dialog
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        DatabaseReference userRef = dbReference.child(uid); // ✅ Writing under "customer/{uid}"

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("address", address);
                        user.put("role", "customer");
                        user.put("createdAt", System.currentTimeMillis()); // ✅ Using timestamp for Realtime Database

                        userRef.setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss(); // ✅ Hide progress dialog
                                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();

                                    // Navigate to LoginActivity immediately after signup
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // Close SignupActivity
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss(); // ✅ Hide progress dialog on failure
                                    Toast.makeText(this, "Signup failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss(); // ✅ Hide progress dialog on failure
                        Toast.makeText(this, "Signup failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Validate password (At least 6 characters, alphanumeric, allows symbols)
    private boolean isValidPassword(String password) {
        return password.length() >= 6 && password.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");
    }
}