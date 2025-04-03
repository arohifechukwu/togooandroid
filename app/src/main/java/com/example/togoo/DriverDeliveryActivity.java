package com.example.togoo;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;

public class DriverDeliveryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnStartTrip, btnMarkArrived;
    private String orderId;
    private String driverId;
    private String driverAddress, restaurantAddress, customerAddress;
    private DatabaseReference ordersRef, driversRef;
    private LatLng driverLatLng, restaurantLatLng, customerLatLng;
    private Polyline routePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivery);

        btnStartTrip = findViewById(R.id.btnStartTrip);
        btnMarkArrived = findViewById(R.id.btnMarkArrived);
//        setupBottomNavigation();

        // Get order ID from intent extras.
        orderId = getIntent().getStringExtra("orderId");
        if(orderId == null){
            Toast.makeText(this, "Order ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");

        // Fetch order details (restaurant and customer addresses).
        ordersRef.child(orderId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    restaurantAddress = snapshot.child("restaurant/address").getValue(String.class);
                    customerAddress = snapshot.child("customer/address").getValue(String.class);
                }
                // Initialize the map once order details are loaded.
                initializeMap();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Fetch driver address.
        driversRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverAddress = snapshot.child("address").getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        btnStartTrip.setOnClickListener(v -> {
            drawRoute();
            btnStartTrip.setVisibility(Button.GONE);
            btnMarkArrived.setVisibility(Button.VISIBLE);
        });

        btnMarkArrived.setOnClickListener(v -> markOrderAsDelivered());
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null){
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Convert addresses to coordinates.
        driverLatLng = getLatLngFromAddress(driverAddress);
        restaurantLatLng = getLatLngFromAddress(restaurantAddress);
        customerLatLng = getLatLngFromAddress(customerAddress);

        if(driverLatLng != null && restaurantLatLng != null && customerLatLng != null){
            // Add markers with different colors.
            mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.addMarker(new MarkerOptions().position(restaurantLatLng).title("Restaurant")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            // Adjust camera to show all markers.
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(driverLatLng);
            builder.include(restaurantLatLng);
            builder.include(customerLatLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        } else {
            Toast.makeText(this, "Failed to obtain location coordinates.", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to convert an address string into a LatLng coordinate.
    private LatLng getLatLngFromAddress(String address) {
        if(address == null || address.isEmpty()) return null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if(addresses != null && !addresses.isEmpty()){
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Draw a simple polyline route connecting driver -> restaurant -> customer.
    private void drawRoute() {
        if(mMap == null || driverLatLng == null || restaurantLatLng == null || customerLatLng == null){
            Toast.makeText(this, "Cannot draw route: missing coordinates.", Toast.LENGTH_SHORT).show();
            return;
        }
        PolylineOptions options = new PolylineOptions()
                .add(driverLatLng)
                .add(restaurantLatLng)
                .add(customerLatLng)
                .width(10)
                .color(0xFF0000FF); // Blue color.
        routePolyline = mMap.addPolyline(options);
    }

    // Mark the order as delivered.
    private void markOrderAsDelivered() {
        if(orderId == null) return;
        DatabaseReference orderRef = ordersRef.child(orderId);
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("status", "delivered");
        updates.put("timestamps/delivered", now);
        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(DriverDeliveryActivity.this, "Order marked as delivered.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(DriverDeliveryActivity.this, "Failed to update order status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void setupBottomNavigation(){
//        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
//        bottomNav.setSelectedItemId(R.id.navigation_orders); // Adjust as needed.
//        bottomNav.setOnNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//            // Handle navigation if needed.
//            return false;
//        });
//    }
}