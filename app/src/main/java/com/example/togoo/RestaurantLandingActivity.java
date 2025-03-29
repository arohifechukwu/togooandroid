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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RestaurantLandingActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private DatabaseReference ordersRef, driversRef, customersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_landing);

        ordersContainer = findViewById(R.id.ordersContainer);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");
        customersRef = FirebaseDatabase.getInstance().getReference("customer");


        fetchOrders();
        setupBottomNavigation();
    }

    private void fetchOrders() {
        ordersContainer.removeAllViews();
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasPendingOrders = false;

                for (DataSnapshot customerSnap : snapshot.getChildren()) {
                    String customerId = customerSnap.getKey();
                    for (DataSnapshot orderSnap : customerSnap.getChildren()) {
                        String orderId = orderSnap.getKey();
                        String status = orderSnap.child("status").getValue(String.class);

                        if ("pending".equals(status)) {
                            hasPendingOrders = true;

                            String foodId = orderSnap.child("foodId").getValue(String.class);
                            String foodName = orderSnap.child("foodName").getValue(String.class);
                            String description = orderSnap.child("description").getValue(String.class);
                            Double price = orderSnap.child("price").getValue(Double.class);
                            String imageUrl = orderSnap.child("imageURL").getValue(String.class);
                            String customerName = orderSnap.child("customerName").getValue(String.class);
                            String customerEmail = orderSnap.child("customerEmail").getValue(String.class);
                            String customerAddress = orderSnap.child("customerAddress").getValue(String.class);
                            String customerPhone = orderSnap.child("customerPhone").getValue(String.class);

                            View orderView = getLayoutInflater().inflate(R.layout.item_order_request, ordersContainer, false);
                            TextView orderDetails = orderView.findViewById(R.id.orderDetails);
                            Button acceptButton = orderView.findViewById(R.id.acceptButton);
                            Button declineButton = orderView.findViewById(R.id.declineButton);

                            orderDetails.setText("Order ID: " + orderId +
                                    "\nFood ID: " + foodId +
                                    "\nFood Name: " + foodName +
                                    "\nDescription: " + description +
                                    "\nPrice: $" + price +
                                    "\nCustomer Name: " + customerName +
                                    "\nEmail: " + customerEmail +
                                    "\nPhone: " + customerPhone +
                                    "\nAddress: " + customerAddress);

                            acceptButton.setOnClickListener(v -> {
                                updateOrderStatus(customerId, orderId, "accepted");
                                notifyDrivers(orderId, customerId, customerAddress, customerPhone, foodName, description, price);
                            });

                            declineButton.setOnClickListener(v -> {
                                updateOrderStatus(customerId, orderId, "declined");
                            });

                            ordersContainer.addView(orderView);
                        }
                    }
                }

                // Show message if no pending orders
                if (!hasPendingOrders) {
                    TextView noOrdersView = new TextView(RestaurantLandingActivity.this);
                    noOrdersView.setText("No available orders.");
                    noOrdersView.setTextSize(16);
                    noOrdersView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    noOrdersView.setPadding(16, 32, 16, 32);
                    noOrdersView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    ordersContainer.addView(noOrdersView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantLandingActivity.this, "Failed to fetch orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_orders);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_orders) {
                    return true;
                } else if (id == R.id.navigation_new) {
                    startActivity(new Intent(RestaurantLandingActivity.this, RestaurantNewActivity.class));
                    return true;
                } else if (id == R.id.navigation_reports) {
                    startActivity(new Intent(RestaurantLandingActivity.this, RestaurantReportActivity.class));
                    return true;
                } else if (id == R.id.navigation_manage) {
                    startActivity(new Intent(RestaurantLandingActivity.this, RestaurantManageActivity.class));
                    return true;
                } else if (id == R.id.navigation_account) {
                    startActivity(new Intent(RestaurantLandingActivity.this, AccountActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void updateOrderStatus(String customerId, String orderId, String status) {
        ordersRef.child(customerId).child(orderId).child("status").setValue(status);
        Toast.makeText(this, "Order " + status, Toast.LENGTH_SHORT).show();
        fetchOrders(); // Refresh list
    }

    private void notifyDrivers(String orderId, String customerId, String address, String phone, String food, String desc, Double price) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("orderId", orderId);
        notification.put("customerId", customerId);
        notification.put("address", address);
        notification.put("phone", phone);
        notification.put("foodName", food);
        notification.put("description", desc);
        notification.put("price", price);

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
}
