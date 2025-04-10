package com.example.togoo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView profileCard, notificationsCard, aboutUsCard, faqCard, languageCard, reportCard, logoutCard;
    private Switch themeSwitch;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load theme preference before setting content view
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_settings);

        // Initialize UI components
        profileCard = findViewById(R.id.cardProfile);
        notificationsCard = findViewById(R.id.cardNotifications);
        aboutUsCard = findViewById(R.id.cardAboutUs);
        faqCard = findViewById(R.id.cardFAQ);
        languageCard = findViewById(R.id.cardLanguage);
        // Initialize the new My Report card
        reportCard = findViewById(R.id.cardMyReport);
        logoutCard = findViewById(R.id.cardLogout); // ✅ Logout button
        themeSwitch = findViewById(R.id.switchTheme);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set switch position based on preference
        themeSwitch.setChecked(isDarkMode);

        // Navigate to different activities
        profileCard.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });

        notificationsCard.setOnClickListener(view -> {
            startActivity(new Intent(this, NotificationsActivity.class));
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

        // Navigate to the My Report page
        reportCard.setOnClickListener(view -> {
            // Change AdminReportsActivity to your desired report activity if needed.
            startActivity(new Intent(this, AdminReportActivity.class));
            overridePendingTransition(0, 0);
        });

        // ✅ Logout functionality
        logoutCard.setOnClickListener(view -> {
            logoutUser();
        });

        // Theme toggle functionality
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("darkMode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        // Bottom Navigation setup
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    /**
     * ✅ Logs out the user by clearing session and navigating to LoginActivity.
     */
    private void logoutUser() {
        // Clear user session from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Remove all stored user data
        editor.apply();

        // Navigate to LoginActivity and clear back stack
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close current activity
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_home) {
            startActivity(new Intent(this, AdminLandingActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_users) {
            startActivity(new Intent(this, UserActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_approvals) {
            startActivity(new Intent(this, ApprovalActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_settings) {
            return true;
        } else if (id == R.id.navigation_transaction) {
            startActivity(new Intent(this, TransactionActivity.class));
            finish();
            return true;
        }
        return false;
    }
}
