package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationStatusActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dbReference;
    private TextView statusText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_status);

        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();

        statusText = findViewById(R.id.statusText);
        logoutButton = findViewById(R.id.logoutButton);

        checkRegistrationStatus();

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(RegistrationStatusActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void checkRegistrationStatus() {
        String uid = auth.getCurrentUser().getUid();

        // Check if the user is a driver or restaurant
        dbReference.child("driver").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("status").getValue() != null) {
                    String status = snapshot.child("status").getValue(String.class);
                    handleStatus(status, "driver");
                } else {
                    // Check restaurant if not found in driver
                    dbReference.child("restaurant").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.child("status").getValue() != null) {
                                String status = snapshot.child("status").getValue(String.class);
                                handleStatus(status, "restaurant");
                            } else {
                                statusText.setText("Error: Registration not found.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            statusText.setText("Error fetching data: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Error fetching data: " + error.getMessage());
            }
        });
    }

    private void handleStatus(String status, String role) {
        if ("approved".equals(status)) {
            Intent intent = role.equals("driver") ? new Intent(this, DriverLandingActivity.class)
                    : new Intent(this, RestaurantLandingActivity.class);
            startActivity(intent);
            finish();
        } else {
            statusText.setText("Registration awaiting approval, please check back later");
        }
    }
}