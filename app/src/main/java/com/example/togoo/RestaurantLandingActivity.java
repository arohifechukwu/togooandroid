package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RestaurantLandingActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private DatabaseReference ordersRef, driversRef;
    // Map to keep track of order views by orderId for real-time updates.
    private HashMap<String, View> orderViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_landing);

        ordersContainer = findViewById(R.id.ordersContainer);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");

        TextView viewOngoingLink = findViewById(R.id.viewOngoingOrdersLink);
        viewOngoingLink.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantLandingActivity.this, RestaurantOngoingOrdersActivity.class);
            startActivity(intent);
        });

        listenToOrders();
        setupBottomNavigation();
    }

    // Attach a continuous (real-time) listener to the ordersByRestaurant node.
    private void listenToOrders() {
        String restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference restaurantOrdersRef = FirebaseDatabase.getInstance()
                .getReference("ordersByRestaurant").child(restaurantId);

        // Clear container and any previous mappings.
        ordersContainer.removeAllViews();
        orderViews.clear();

        restaurantOrdersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showNoOrdersMessage();
                    return;
                }
                // For each order ID in this restaurant's list, attach a real-time listener.
                for (DataSnapshot orderLinkSnap : snapshot.getChildren()) {
                    String orderId = orderLinkSnap.getKey();
                    attachOrderListener(orderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantLandingActivity.this, "Failed to fetch restaurant orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Attach a real-time listener to a single order.
    private void attachOrderListener(String orderId) {
        ordersRef.child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot orderSnap) {
                if (!orderSnap.exists()) {
                    // Order no longer exists – remove its view if present.
                    if (orderViews.containsKey(orderId)) {
                        ordersContainer.removeView(orderViews.get(orderId));
                        orderViews.remove(orderId);
                    }
                    return;
                }
                String status = orderSnap.child("status").getValue(String.class);
                // Only display orders that are still "placed"
                if ("placed".equals(status)) {
                    // Retrieve customer details.
                    Map<String, Object> customerMap = (Map<String, Object>) orderSnap.child("customer").getValue();
                    if (customerMap == null) return;
                    String customerName = (String) customerMap.get("name");
                    String customerPhone = (String) customerMap.get("phone");
                    String customerAddress = (String) customerMap.get("address");

                    // Retrieve food details from orderDetails/items/0.
                    String foodDescription = "N/A";
                    String foodId = "N/A";
                    String foodPrice = "N/A";
                    String quantity = "N/A";
                    DataSnapshot itemZeroSnap = orderSnap.child("orderDetails").child("items").child("0");
                    if (itemZeroSnap.exists()) {
                        foodDescription = itemZeroSnap.child("foodDescription").getValue(String.class);
                        foodId = itemZeroSnap.child("foodId").getValue(String.class);
                        Object priceObj = itemZeroSnap.child("foodPrice").getValue();
                        foodPrice = priceObj != null ? priceObj.toString() : "N/A";
                        Object quantityObj = itemZeroSnap.child("quantity").getValue();
                        quantity = quantityObj != null ? quantityObj.toString() : "N/A";
                    }

                    // Retrieve payment details from the payment node (which is a sibling of orderDetails).
                    String total = "N/A";
                    String method = "N/A";
                    DataSnapshot paymentSnapshot = orderSnap.child("payment");
                    if (paymentSnapshot.exists()) {
                        Object totalObj = paymentSnapshot.child("total").getValue();
                        total = totalObj != null ? totalObj.toString() : "N/A";
                        Object methodObj = paymentSnapshot.child("method").getValue();
                        method = methodObj != null ? methodObj.toString() : "N/A";
                    }

                    // Retrieve customer notes.
                    String notes = orderSnap.child("notes").getValue(String.class);

                    // Create or update the order view.
                    View orderView = orderViews.get(orderId);
                    if (orderView == null) {
                        orderView = getLayoutInflater().inflate(R.layout.item_order_request, ordersContainer, false);
                        orderViews.put(orderId, orderView);
                    }
                    TextView orderDetailsTextView = orderView.findViewById(R.id.orderDetails);
                    Button acceptButton = orderView.findViewById(R.id.acceptButton);
                    Button declineButton = orderView.findViewById(R.id.declineButton);

                    // Compose the full order details text with bold labels using HTML.
                    String html = "<b>Order ID:</b> " + orderId +
                            "<br><b>Customer:</b> " + customerName +
                            "<br><b>Phone:</b> " + customerPhone +
                            "<br><b>Address:</b> " + customerAddress +
                            "<br><b>Food ID:</b> " + foodId +
                            "<br><b>Food Description:</b> " + foodDescription +
                            "<br><b>Food Price:</b> " + foodPrice +
                            "<br><b>Quantity:</b> " + quantity +
                            "<br><b>Total:</b> " + total +
                            "<br><b>Payment Method:</b> " + method +
                            "<br><b>Customer Notes:</b> " + notes +
                            "<br><b>Status:</b> " + status;
                    orderDetailsTextView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));

                    // Show only the Accept and Decline buttons.
                    acceptButton.setVisibility(View.VISIBLE);
                    declineButton.setVisibility(View.VISIBLE);
                    acceptButton.setOnClickListener(v -> {
                        updateOrderStatus(orderId, "accepted");
                        notifyDrivers(orderId, customerAddress, customerPhone);
                    });
                    declineButton.setOnClickListener(v -> updateOrderStatus(orderId, "declined"));

                    // Add the view to the container if it's not already added.
                    if (orderView.getParent() == null) {
                        ordersContainer.addView(orderView);
                    }
                } else {
                    // If the order's status is not "placed" (for example, "accepted"), remove its view.
                    View viewToRemove = orderViews.get(orderId);
                    if (viewToRemove != null) {
                        ordersContainer.removeView(viewToRemove);
                        orderViews.remove(orderId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantLandingActivity.this, "Failed to load order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        DatabaseReference orderRef = ordersRef.child(orderId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        if ("accepted".equals(newStatus))
            updates.put("timestamps/restaurantAccepted", now);
        else if ("declined".equals(newStatus))
            updates.put("timestamps/restaurantDeclined", now);
        else if ("preparing".equals(newStatus))
            updates.put("timestamps/preparing", now);
        else if ("ready".equals(newStatus))
            updates.put("timestamps/readyForPickup", now);

        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> logEntry = new HashMap<>();
                logEntry.put("timestamp", now);
                logEntry.put("status", newStatus);
                logEntry.put("note", "Status updated to " + newStatus + " by restaurant.");
                orderRef.child("updateLogs").push().setValue(logEntry);
                Toast.makeText(this, "Order status: " + newStatus, Toast.LENGTH_SHORT).show();
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
                    String availability = driver.child("availability").getValue(String.class);
                    if (availability != null && availability.equalsIgnoreCase("available")) {
                        driversRef.child(driver.getKey()).child("notifications").push().setValue(notification);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantLandingActivity.this, "Notification error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoOrdersMessage() {
        ordersContainer.removeAllViews();
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