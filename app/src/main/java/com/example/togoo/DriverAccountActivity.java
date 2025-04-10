package com.example.togoo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.togoo.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class DriverAccountActivity extends AppCompatActivity {

    private MaterialCardView profileCard, notificationsCard, availabilityCard, aboutUsCard, faqCard, languageCard, logoutCard;
    private Switch themeSwitch, availabilitySwitch;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference driverRef;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_account);

        // Note: Toolbar logic removed; layout uses a header TextView instead.

        // Initialize views
        profileCard = findViewById(R.id.cardProfile);
        notificationsCard = findViewById(R.id.cardNotifications);
        availabilityCard = findViewById(R.id.cardAvailability);
        aboutUsCard = findViewById(R.id.cardAboutUs);
        faqCard = findViewById(R.id.cardFAQ);
        languageCard = findViewById(R.id.cardLanguage);
        logoutCard = findViewById(R.id.cardLogout);
        themeSwitch = findViewById(R.id.switchTheme);
        availabilitySwitch = findViewById(R.id.switchAvailability);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set up theme switch state from SharedPreferences.
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        themeSwitch.setChecked(isDarkMode);

        // Card click listeners
        profileCard.setOnClickListener(view -> {
            startActivity(new Intent(this, DriverProfileActivity.class));
            overridePendingTransition(0, 0);
        });

        notificationsCard.setOnClickListener(view -> {
            startActivity(new Intent(this, DriverNotificationsActivity.class));
            overridePendingTransition(0, 0);
        });

        aboutUsCard.setOnClickListener(view -> {
            startActivity(new Intent(this, AboutUsActivity.class));
            overridePendingTransition(0, 0);
        });

        faqCard.setOnClickListener(view -> {
            startActivity(new Intent(this, FAQActivity.class));
            overridePendingTransition(0, 0);
        });

        languageCard.setOnClickListener(view -> {
            startActivity(new Intent(this, LanguageActivity.class));
            overridePendingTransition(0, 0);
        });

        logoutCard.setOnClickListener(view -> logoutUser());

        // Theme toggle listener.
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("darkMode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        // Availability toggle listener.
        availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            driverRef = FirebaseDatabase.getInstance().getReference("driver").child(uid);
            String newStatus = isChecked ? "available" : "unavailable";
            driverRef.child("availability").setValue(newStatus)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(DriverAccountActivity.this, "Availability set to " + newStatus, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DriverAccountActivity.this, "Failed to update availability", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Bottom navigation setup.
        bottomNavigationView.setSelectedItemId(R.id.navigation_account);
        setupBottomNavigation();

        // Load current driver data.
        loadDriverData();
    }

    private void loadDriverData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverRef = FirebaseDatabase.getInstance().getReference("driver").child(uid);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Remove the listener temporarily so that setChecked doesn't trigger an update.
                availabilitySwitch.setOnCheckedChangeListener(null);
                String availability = snapshot.child("availability").getValue(String.class);
                if (availability != null && availability.equalsIgnoreCase("available")) {
                    availabilitySwitch.setChecked(true);
                } else {
                    availabilitySwitch.setChecked(false);
                }
                // Reattach the listener.
                availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    driverRef = FirebaseDatabase.getInstance().getReference("driver").child(uid);
                    String newStatus = isChecked ? "available" : "unavailable";
                    driverRef.child("availability").setValue(newStatus)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Toast.makeText(DriverAccountActivity.this, "Availability set to " + newStatus, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DriverAccountActivity.this, "Failed to update availability", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverAccountActivity.this, "Failed to load driver data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(DriverAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_account) {
            return true;
        } else if (id == R.id.navigation_orders) {
            startActivity(new Intent(this, DriverLandingActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_notification) {
            startActivity(new Intent(this, DriverNotificationsActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_reports) {
            startActivity(new Intent(this, DriverReportsActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_account);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_account) return true;
            else if (id == R.id.navigation_notification) {
                startActivity(new Intent(this, DriverNotificationsActivity.class));
                return true;
            } else if (id == R.id.navigation_reports) {
                startActivity(new Intent(this, DriverReportsActivity.class));
                return true;
            } else if (id == R.id.navigation_orders) {
                startActivity(new Intent(this, DriverLandingActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // For back navigation via system (if needed)
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}