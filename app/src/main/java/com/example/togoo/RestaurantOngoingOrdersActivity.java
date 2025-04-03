package com.example.togoo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RestaurantOngoingOrdersActivity extends AppCompatActivity {

    private LinearLayout ongoingOrdersContainer;
    private Spinner filterSpinner;
    private EditText searchBar;
    private DatabaseReference ordersRef, driversRef;
    private String restaurantId;
    private List<Map<String, Object>> allOrders = new ArrayList<>();
    private int availableDriverCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_ongoing_orders);

        setupToolbar();

        ongoingOrdersContainer = findViewById(R.id.ongoingOrdersContainer);
        filterSpinner = findViewById(R.id.filterSpinner);
        searchBar = findViewById(R.id.searchBar);

        restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");

        setupFilterSpinner();
        setupSearchListener();
        fetchAvailableDriverCount();
        loadOngoingOrders();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ongoing Orders");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                java.util.Arrays.asList("Order ID", "Customer", "Status", "Timestamp"));
        filterSpinner.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });
    }

    // This method fetches the number of drivers with availability "available" in real time.
    private void fetchAvailableDriverCount() {
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot driverSnap : snapshot.getChildren()) {
                    String availability = driverSnap.child("availability").getValue(String.class);
                    if (!TextUtils.isEmpty(availability) && availability.equalsIgnoreCase("available")) {
                        count++;
                    }
                }
                availableDriverCount = count;
                // Refresh the order display so that the available count is updated.
                displayOrders(allOrders);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadOngoingOrders() {
        DatabaseReference restaurantOrdersRef = FirebaseDatabase.getInstance()
                .getReference("ordersByRestaurant").child(restaurantId);

        // Use a real-time listener for orders-by-restaurant.
        restaurantOrdersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                // For each order link, attach a real-time listener.
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String orderId = orderSnap.getKey();
                    ordersRef.child(orderId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String status = dataSnapshot.child("status").getValue(String.class);
                                // Exclude orders that have been declined or delivered.
                                if (!"declined".equals(status) && !"delivered".equals(status)) {
                                    Map<String, Object> orderMap = (Map<String, Object>) dataSnapshot.getValue();
                                    orderMap.put("orderId", orderId);
                                    removeOrderIfExists(orderId);
                                    allOrders.add(orderMap);
                                    displayOrders(allOrders);
                                } else {
                                    removeOrderIfExists(orderId);
                                    displayOrders(allOrders);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * Helper method to remove an order from allOrders by orderId.
     */
    private void removeOrderIfExists(String orderId) {
        for (Iterator<Map<String, Object>> iterator = allOrders.iterator(); iterator.hasNext();) {
            Map<String, Object> order = iterator.next();
            if (orderId.equals(order.get("orderId"))) {
                iterator.remove();
            }
        }
    }

    private void filterOrders(String query) {
        String filterBy = filterSpinner.getSelectedItem().toString();
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> order : allOrders) {
            String field = "";
            switch (filterBy) {
                case "Order ID":
                    field = (String) order.get("orderId");
                    break;
                case "Customer":
                    Map<String, Object> customer = (Map<String, Object>) order.get("customer");
                    field = (String) customer.get("name");
                    break;
                case "Status":
                    field = (String) order.get("status");
                    break;
                case "Timestamp":
                    Map<String, Object> ts = (Map<String, Object>) order.get("timestamps");
                    field = (String) ts.get("restaurantAccepted");
                    break;
            }
            if (field != null) {
                String fieldLower = field.toLowerCase();
                String queryLower = query.toLowerCase();
                if (fieldLower.contains(queryLower)) {
                    filtered.add(order);
                }
            }
        }
        displayOrders(filtered);
    }

    private void displayOrders(List<Map<String, Object>> orders) {
        ongoingOrdersContainer.removeAllViews();
        for (Map<String, Object> order : orders) {
            View card = getLayoutInflater().inflate(R.layout.item_in_progress_order, ongoingOrdersContainer, false);
            TextView textView = card.findViewById(R.id.inProgressOrderDetails);
            TextView driverStatus = card.findViewById(R.id.driverStatus);
            Button btnPreparing = card.findViewById(R.id.btnPreparing);
            Button btnReady = card.findViewById(R.id.btnReady);

            String orderId = (String) order.get("orderId");
            String customerName = ((Map<String, Object>) order.get("customer")).get("name").toString();
            String status = order.get("status").toString();

            textView.setText("Order: " + orderId + "\nCustomer: " + customerName + "\nStatus: " + status);

            // Instead of "Driver: Awaiting", display count of available drivers if no driver is assigned.
            if (order.containsKey("driver")) {
                Map<String, Object> driver = (Map<String, Object>) order.get("driver");
                if (driver != null && driver.containsKey("name")) {
                    String driverName = (String) driver.get("name");
                    driverStatus.setText("Driver: " + driverName);
                }
            } else {
                driverStatus.setText("Driver: " + availableDriverCount + " drivers available");
            }

            // Update button UI/logic based on order status.
            if ("accepted".equals(status)) {
                btnPreparing.setVisibility(View.VISIBLE);
                btnReady.setVisibility(View.VISIBLE);
                btnPreparing.setText("Mark as Preparing");
                btnPreparing.setEnabled(true);
                btnReady.setText("Ready for Pickup");
                btnReady.setEnabled(true);
                btnPreparing.setOnClickListener(v -> {
                    updateOrderStatus(orderId, "preparing");
                    btnPreparing.setText("Preparing ✅");
                    btnPreparing.setEnabled(false);
                });
                btnReady.setOnClickListener(v -> {
                    updateOrderStatus(orderId, "ready");
                    btnPreparing.setVisibility(View.GONE);
                    btnReady.setText("Ready ✅");
                    btnReady.setEnabled(false);
                });
            } else if ("preparing".equals(status)) {
                btnPreparing.setVisibility(View.VISIBLE);
                btnPreparing.setText("Preparing ✅");
                btnPreparing.setEnabled(false);
                btnReady.setVisibility(View.VISIBLE);
                btnReady.setText("Ready for Pickup");
                btnReady.setEnabled(true);
                btnReady.setOnClickListener(v -> {
                    updateOrderStatus(orderId, "ready");
                    btnPreparing.setVisibility(View.GONE);
                    btnReady.setText("Ready ✅");
                    btnReady.setEnabled(false);
                });
            } else if ("ready".equals(status)) {
                btnPreparing.setVisibility(View.GONE);
                btnReady.setVisibility(View.VISIBLE);
                btnReady.setText("Ready ✅");
                btnReady.setEnabled(false);
            }

            ongoingOrdersContainer.addView(card);
        }
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        DatabaseReference orderRef = ordersRef.child(orderId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        switch (newStatus) {
            case "preparing":
                updates.put("timestamps/preparing", now);
                break;
            case "ready":
                updates.put("timestamps/readyForPickup", now);
                break;
        }
        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> logEntry = new HashMap<>();
                logEntry.put("timestamp", now);
                logEntry.put("status", newStatus);
                logEntry.put("note", "Status updated to " + newStatus + " by restaurant.");
                orderRef.child("updateLogs").push().setValue(logEntry);
                Toast.makeText(this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}