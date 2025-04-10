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

public class RestaurantAccountActivity extends AppCompatActivity {

    private MaterialCardView profileCard, notificationsCard, aboutUsCard, faqCard, languageCard, logoutCard;
    private Switch themeSwitch;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_account);

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        profileCard = findViewById(R.id.cardProfile);
        notificationsCard = findViewById(R.id.cardNotifications);
        aboutUsCard = findViewById(R.id.cardAboutUs);
        faqCard = findViewById(R.id.cardFAQ);
        languageCard = findViewById(R.id.cardLanguage);
        logoutCard = findViewById(R.id.cardLogout);
        themeSwitch = findViewById(R.id.switchTheme);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        themeSwitch.setChecked(isDarkMode);

        setupListeners();
        setupBottomNavigation();
    }

    private void setupListeners() {
        profileCard.setOnClickListener(view -> startActivity(new Intent(this, RestaurantProfileActivity.class)));
        notificationsCard.setOnClickListener(view -> startActivity(new Intent(this, NotificationsActivity.class)));
        aboutUsCard.setOnClickListener(view -> startActivity(new Intent(this, AboutUsActivity.class)));
        faqCard.setOnClickListener(view -> startActivity(new Intent(this, FAQActivity.class)));
        languageCard.setOnClickListener(view -> startActivity(new Intent(this, LanguageActivity.class)));

        logoutCard.setOnClickListener(view -> {
            SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            preferences.edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putBoolean("darkMode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_account);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_account) {
                return true;
            } else if (id == R.id.navigation_orders) {
                startActivity(new Intent(this, RestaurantLandingActivity.class));
            } else if (id == R.id.navigation_new) {
                startActivity(new Intent(this, RestaurantNewActivity.class));
            } else if (id == R.id.navigation_reports) {
                startActivity(new Intent(this, RestaurantReportActivity.class));
            } else if (id == R.id.navigation_manage) {
                startActivity(new Intent(this, RestaurantManageActivity.class));
            }
            return false;
        });
    }
}