package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.util.List;
import java.util.Locale;

public class DriverLandingActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private DatabaseReference ordersRef, driversRef, ordersByDriverRef;
    private String driverId;
    private String driverName, driverPhone, driverAddress;
    private String driverAvailability = "unavailable";  // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_landing);

        ordersContainer = findViewById(R.id.ordersContainer);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");
        ordersByDriverRef = FirebaseDatabase.getInstance().getReference("ordersByDriver");

        // Get current driver UID.
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check driver availability and fetch driver details.
        driversRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverAvailability = snapshot.child("availability").getValue(String.class);
                driverName = snapshot.child("name").getValue(String.class);
                driverPhone = snapshot.child("phone").getValue(String.class);
                driverAddress = snapshot.child("address").getValue(String.class);

                // Instead of finishing, allow access.
                if (TextUtils.isEmpty(driverAvailability) || !driverAvailability.equalsIgnoreCase("available")) {
                    Toast.makeText(DriverLandingActivity.this, "You are currently not available for deliveries.", Toast.LENGTH_LONG).show();
                    showOfflineMessage();
                } else {
                    loadReadyOrders();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverLandingActivity.this, "Failed to fetch driver details.", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void loadReadyOrders() {
        // Query orders with status "ready"
        Query readyOrdersQuery = ordersRef.orderByChild("status").equalTo("ready");
        readyOrdersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersContainer.removeAllViews();
                if (!snapshot.exists()) {
                    showNoOrdersMessage();
                    return;
                }
                // Iterate through orders with status "ready"
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    // Skip orders that already have a driver assigned.
                    if (orderSnap.hasChild("driver")) continue;
                    final String orderId = orderSnap.getKey();
                    displayOrder(orderId, orderSnap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverLandingActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // If driver is not available, show an offline message.
    private void showOfflineMessage() {
        ordersContainer.removeAllViews();
        TextView messageView = new TextView(DriverLandingActivity.this);
        messageView.setText("Orders cannot be assigned while offline. Update your availability.");
        messageView.setTextSize(16);
        messageView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        messageView.setPadding(16, 32, 16, 32);
        messageView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        ordersContainer.addView(messageView);
    }

    private void displayOrder(final String orderId, DataSnapshot orderSnap) {
        // Fetch order details.
        String status = orderSnap.child("status").getValue(String.class);
        String customerName = orderSnap.child("customer/name").getValue(String.class);
        String customerAddress = orderSnap.child("customer/address").getValue(String.class);
        String customerPhone = orderSnap.child("customer/phone").getValue(String.class);
        String customerNotes = orderSnap.child("notes").getValue(String.class);
        String restaurantName = orderSnap.child("restaurant/name").getValue(String.class);
        String restaurantAddress = orderSnap.child("restaurant/address").getValue(String.class);
        String foodId = orderSnap.child("orderDetails/items/0/foodId").getValue(String.class);
        String foodDescription = orderSnap.child("orderDetails/items/0/foodDescription").getValue(String.class);
        String quantity = orderSnap.child("orderDetails/items/0/quantity").getValue() != null ?
                orderSnap.child("orderDetails/items/0/quantity").getValue().toString() : "N/A";
        String deliveryFare = orderSnap.child("payment/deliveryFare").getValue() != null ?
                orderSnap.child("payment/deliveryFare").getValue().toString() : "N/A";
        String tips = orderSnap.child("payment/tips").getValue() != null ?
                orderSnap.child("payment/tips").getValue().toString() : "N/A";
        String total = orderSnap.child("payment/total").getValue() != null ?
                orderSnap.child("payment/total").getValue().toString() : "N/A";
        String paymentMethod = orderSnap.child("payment/method").getValue(String.class);
        if (paymentMethod == null) paymentMethod = "N/A";
        String paymentStatus = orderSnap.child("payment/status").getValue(String.class);
        if (paymentStatus == null) paymentStatus = "N/A";
        String estimatedDeliveryTime = "Not assigned";

        // Inflate the order item view.
        View orderView = getLayoutInflater().inflate(R.layout.item_driver_order, ordersContainer, false);
        TextView orderDetailsTextView = orderView.findViewById(R.id.orderDetails);
        Button acceptButton = orderView.findViewById(R.id.acceptButton);
        Button declineButton = orderView.findViewById(R.id.declineButton);
        Button startTripButton = orderView.findViewById(R.id.startTripButton);
        startTripButton.setVisibility(View.GONE);

        String html = "<b>Order ID:</b> " + orderId +
                "<br><b>Status:</b> " + status +
                "<br><b>Customer:</b> " + customerName +
                "<br><b>Customer Address:</b> " + customerAddress +
                "<br><b>Customer Phone:</b> " + customerPhone +
                "<br><b>Customer Notes:</b> " + customerNotes +
                "<br><b>Restaurant (Pickup):</b> " + restaurantName +
                "<br><b>Pickup Address:</b> " + restaurantAddress +
                "<br><b>Estimated Delivery Time:</b> " + estimatedDeliveryTime +
                "<br><b>Food ID:</b> " + foodId +
                "<br><b>Food Description:</b> " + foodDescription +
                "<br><b>Quantity:</b> " + quantity +
                "<br><b>Delivery Fare:</b> " + deliveryFare +
                "<br><b>Tips:</b> " + tips +
                "<br><b>Total:</b> " + total +
                "<br><b>Payment Method:</b> " + paymentMethod +
                "<br><b>Payment Status:</b> " + paymentStatus;
        orderDetailsTextView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));

        acceptButton.setOnClickListener(v -> {
            updateOrderOnDriverAccept(orderId);
            acceptButton.setVisibility(View.GONE);
            declineButton.setVisibility(View.GONE);
            startTripButton.setVisibility(View.VISIBLE);
        });
        declineButton.setOnClickListener(v -> updateOrderStatus(orderId, "declined"));
        startTripButton.setOnClickListener(v -> {
            Intent intent = new Intent(DriverLandingActivity.this, DriverDeliveryActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        ordersContainer.addView(orderView);
    }



    private void updateOrderOnDriverAccept(String orderId) {
        DatabaseReference orderRef = ordersRef.child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String restaurantAddress = snapshot.child("restaurant/address").getValue(String.class);
                String customerAddress = snapshot.child("customer/address").getValue(String.class);

                Geocoder geocoder = new Geocoder(DriverLandingActivity.this, Locale.getDefault());
                LatLng driverLatLng = getLatLngFromAddress(driverAddress, geocoder);
                LatLng restaurantLatLng = getLatLngFromAddress(restaurantAddress, geocoder);
                LatLng customerLatLng = getLatLngFromAddress(customerAddress, geocoder);

                if (driverLatLng == null || restaurantLatLng == null || customerLatLng == null) {
                    Toast.makeText(DriverLandingActivity.this, "Failed to calculate estimated delivery time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                float[] result = new float[1];
                Location.distanceBetween(driverLatLng.latitude, driverLatLng.longitude,
                        restaurantLatLng.latitude, restaurantLatLng.longitude, result);
                float distance1 = result[0];

                Location.distanceBetween(restaurantLatLng.latitude, restaurantLatLng.longitude,
                        customerLatLng.latitude, customerLatLng.longitude, result);
                float distance2 = result[0];

                float totalDistanceKm = (distance1 + distance2) / 1000;
                int estimatedTimeMins = Math.round(totalDistanceKm * 2);
                String estimatedTimeString = estimatedTimeMins + " mins";

                // Update order with driver info, estimated delivery, and log
                String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("status", "out for delivery");
                updates.put("timestamps/driverAssigned", now);
                updates.put("estimatedDeliveryTime", estimatedTimeString);

                orderRef.updateChildren(updates);

                HashMap<String, Object> driverInfo = new HashMap<>();
                driverInfo.put("id", driverId);
                driverInfo.put("name", driverName);
                driverInfo.put("phone", driverPhone);
                driverInfo.put("assignmentTimestamp", now);
                orderRef.child("driver").setValue(driverInfo);

                ordersByDriverRef.child(driverId).child(orderId).setValue(true);

                HashMap<String, Object> logEntry = new HashMap<>();
                logEntry.put("timestamp", now);
                logEntry.put("status", "out for delivery");
                logEntry.put("note", "Status updated to out for delivery by driver.");
                orderRef.child("updateLogs").push().setValue(logEntry);

                // Update the driver's notification status
                DatabaseReference driverNotifRef = driversRef.child(driverId).child("notifications");
                Query notifQuery = driverNotifRef.orderByChild("orderId").equalTo(orderId);
                notifQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot notifSnap : snapshot.getChildren()) {
                            notifSnap.getRef().child("status").setValue("order accepted");
                        }

                        Intent intent = new Intent(DriverLandingActivity.this, DriverDeliveryActivity.class);
                        intent.putExtra("orderId", orderId);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DriverLandingActivity.this, "Error updating notification", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverLandingActivity.this, "Failed to fetch order info.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateOrderStatus(String orderId, String newStatus) {
        DatabaseReference orderRef = ordersRef.child(orderId);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        if ("out for delivery".equals(newStatus)) {
            updates.put("timestamps/driverAssigned", now);
        } else if ("declined".equals(newStatus)) {
            updates.put("timestamps/driverDeclined", now);
        }
        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, Object> logEntry = new HashMap<>();
                logEntry.put("timestamp", now);
                logEntry.put("status", newStatus);
                logEntry.put("note", "Status updated to " + newStatus + " by driver.");
                orderRef.child("updateLogs").push().setValue(logEntry);
                Toast.makeText(DriverLandingActivity.this, "Order status: " + newStatus, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DriverLandingActivity.this, "Failed to update order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoOrdersMessage() {
        ordersContainer.removeAllViews();
        TextView noOrdersView = new TextView(DriverLandingActivity.this);
        noOrdersView.setText("No available orders.");
        noOrdersView.setTextSize(16);
        noOrdersView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        noOrdersView.setPadding(16, 32, 16, 32);
        noOrdersView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        ordersContainer.addView(noOrdersView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_orders);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) return true;
            else if (id == R.id.navigation_notification) {
                startActivity(new Intent(this, DriverNotificationsActivity.class));
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


    private LatLng getLatLngFromAddress(String address, Geocoder geocoder) {
        if (address == null || address.isEmpty()) return null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}