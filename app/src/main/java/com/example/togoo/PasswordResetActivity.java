package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText inputEmail;
    private Button resetPasswordButton;
    private TextView loginLink; // Back to Login Link

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        auth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        loginLink = findViewById(R.id.loginLink);

        resetPasswordButton.setOnClickListener(v -> resetPassword());

        // Navigate back to LoginActivity when the link is clicked
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(PasswordResetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void resetPassword() {
        String email = inputEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter a registered email", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PasswordResetActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to send reset email. Check email and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}