package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.togoo.utils.RestaurantHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class RestaurantLandingActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private DatabaseReference ordersRef, driversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_landing);

        ordersContainer = findViewById(R.id.ordersContainer);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");

        fetchOrders();
        setupBottomNavigation();
    }

    private void fetchOrders() {
        ordersContainer.removeAllViews();

        String restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (restaurantId == null) {
            Toast.makeText(this, "Restaurant UID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference restaurantOrdersRef = FirebaseDatabase.getInstance()
                .getReference("ordersByRestaurant").child(restaurantId);

        restaurantOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showNoOrdersMessage();
                    return;
                }

                for (DataSnapshot orderLinkSnap : snapshot.getChildren()) {
                    String orderId = orderLinkSnap.getKey();

                    ordersRef.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot orderSnap) {
                            if (!orderSnap.exists()) return;

                            String status = orderSnap.child("status").getValue(String.class);
                            if (!"placed".equals(status)) return;

                            Map<String, Object> customerMap = (Map<String, Object>) orderSnap.child("customer").getValue();
                            String customerName = (String) customerMap.get("name");
                            String customerPhone = (String) customerMap.get("phone");
                            String customerAddress = (String) customerMap.get("address");

                            View orderView = getLayoutInflater().inflate(R.layout.item_order_request, ordersContainer, false);
                            TextView orderDetails = orderView.findViewById(R.id.orderDetails);
                            Button acceptButton = orderView.findViewById(R.id.acceptButton);
                            Button declineButton = orderView.findViewById(R.id.declineButton);

                            orderDetails.setText("Order ID: " + orderId
                                    + "\nCustomer: " + customerName
                                    + "\nPhone: " + customerPhone
                                    + "\nAddress: " + customerAddress);

                            acceptButton.setOnClickListener(v -> {
                                updateOrderStatus(orderId, "accepted");
                                notifyDrivers(orderId, customerAddress, customerPhone);
                            });

                            declineButton.setOnClickListener(v -> updateOrderStatus(orderId, "declined"));

                            ordersContainer.addView(orderView);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(RestaurantLandingActivity.this, "Failed to load order", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantLandingActivity.this, "Failed to fetch restaurant orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(String orderId, String status) {
        ordersRef.child(orderId).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Order " + status, Toast.LENGTH_SHORT).show();
                        fetchOrders(); // Refresh
                    } else {
                        Toast.makeText(this, "Failed to update order", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void notifyDrivers(String orderId, String address, String phone) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("orderId", orderId);
        notification.put("address", address);
        notification.put("phone", phone);
        notification.put("status", "awaiting_driver");

        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot driver : snapshot.getChildren()) {
                    driversRef.child(driver.getKey()).child("notifications").push().setValue(notification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantLandingActivity.this, "Notification error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoOrdersMessage() {
        TextView noOrdersView = new TextView(RestaurantLandingActivity.this);
        noOrdersView.setText("No available orders.");
        noOrdersView.setTextSize(16);
        noOrdersView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        noOrdersView.setPadding(16, 32, 16, 32);
        noOrdersView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ordersContainer.addView(noOrdersView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_orders);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) return true;
            else if (id == R.id.navigation_new) {
                startActivity(new Intent(this, RestaurantNewActivity.class));
                return true;
            } else if (id == R.id.navigation_reports) {
                startActivity(new Intent(this, RestaurantReportActivity.class));
                return true;
            } else if (id == R.id.navigation_manage) {
                startActivity(new Intent(this, RestaurantManageActivity.class));
                return true;
            } else if (id == R.id.navigation_account) {
                startActivity(new Intent(this, RestaurantAccountActivity.class));
                return true;
            }
            return false;
        });
    }
}