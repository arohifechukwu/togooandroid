package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.Map;

public class DriverNotificationsActivity extends AppCompatActivity {

    private LinearLayout notificationsContainer;
    private DatabaseReference notificationsRef;
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_notifications);

        // Get the container from the layout
        notificationsContainer = findViewById(R.id.notificationsContainer);
        setupBottomNavigation();

        // Get current driver's UID.
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Reference to the notifications node for this driver.
        notificationsRef = FirebaseDatabase.getInstance().getReference("driver")
                .child(driverId).child("notifications");

        loadNotifications();
    }

    private void loadNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsContainer.removeAllViews();
                boolean found = false;
                if (snapshot.exists()) {
                    for (DataSnapshot notifSnap : snapshot.getChildren()) {
                        Map<String, Object> notif = (Map<String, Object>) notifSnap.getValue();
                        if (notif != null) {
                            String status = (String) notif.get("status");
                            // Skip notifications with status "order accepted".
                            if (status != null && status.equalsIgnoreCase("order accepted")) {
                                continue;
                            }
                            // Inflate a notification item view.
                            View itemView = LayoutInflater.from(DriverNotificationsActivity.this)
                                    .inflate(R.layout.item_driver_notification, notificationsContainer, false);
                            TextView notificationText = itemView.findViewById(R.id.notificationText);

                            // Display notification details – here, Order ID and status.
                            String orderId = (String) notif.get("orderId");
                            String displayText = "Order ID: " + orderId + "\nStatus: " + status;
                            notificationText.setText(displayText);

                            notificationsContainer.addView(itemView);
                            found = true;
                        }
                    }
                }
                if (!found) {
                    showNoNotificationsMessage();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverNotificationsActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoNotificationsMessage() {
        notificationsContainer.removeAllViews();
        TextView noNotifView = new TextView(this);
        noNotifView.setText("No available notifications.");
        noNotifView.setTextSize(16);
        noNotifView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        noNotifView.setPadding(16, 32, 16, 32);
        noNotifView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        notificationsContainer.addView(noNotifView);
    }

    private void setupBottomNavigation(){
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_notification);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) {
                startActivity(new Intent(this, DriverLandingActivity.class));
                return true;
            } else if (id == R.id.navigation_notification) {
                // Already here.
                return true;
            } else if (id == R.id.navigation_reports) {
                startActivity(new Intent(this, DriverReportsActivity.class));
                return true;
            } else if (id == R.id.navigation_account) {
                startActivity(new Intent(this, DriverAccountActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}