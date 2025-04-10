package com.example.togoo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DriverDeliveryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnStartTrip, btnMarkArrived;
    private String orderId, driverId;
    private String driverAddress, restaurantAddress, customerAddress;
    private LatLng driverLatLng, restaurantLatLng, customerLatLng;
    private DatabaseReference ordersRef, driversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivery);

        btnStartTrip = findViewById(R.id.btnStartTrip);
        btnMarkArrived = findViewById(R.id.btnMarkArrived);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        driversRef = FirebaseDatabase.getInstance().getReference("driver");

        fetchAddressesAndInitializeMap();

        btnStartTrip.setOnClickListener(v -> {
            drawRoute();
            btnStartTrip.setVisibility(Button.GONE);
            btnMarkArrived.setVisibility(Button.VISIBLE);
        });

        btnMarkArrived.setOnClickListener(v -> markOrderAsDelivered());
    }

    private void fetchAddressesAndInitializeMap() {
        driversRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot driverSnap) {
                driverAddress = driverSnap.child("address").getValue(String.class);
                ordersRef.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot orderSnap) {
                        restaurantAddress = orderSnap.child("restaurant/address").getValue(String.class);
                        customerAddress = orderSnap.child("customer/address").getValue(String.class);

                        Log.d("DriverDelivery", "Driver: " + driverAddress);
                        Log.d("DriverDelivery", "Restaurant: " + restaurantAddress);
                        Log.d("DriverDelivery", "Customer: " + customerAddress);

                        if (driverAddress != null && restaurantAddress != null && customerAddress != null) {
                            initializeMap();
                        } else {
                            Toast.makeText(DriverDeliveryActivity.this, "One or more addresses missing.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DriverDeliveryActivity.this, "Error loading order.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverDeliveryActivity.this, "Error loading driver info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        driverLatLng = getLatLngFromAddress(driverAddress);
        restaurantLatLng = getLatLngFromAddress(restaurantAddress);
        customerLatLng = getLatLngFromAddress(customerAddress);

        if (driverLatLng != null && restaurantLatLng != null && customerLatLng != null) {
            mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.addMarker(new MarkerOptions().position(restaurantLatLng).title("Restaurant")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(driverLatLng);
            builder.include(restaurantLatLng);
            builder.include(customerLatLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        } else {
            Toast.makeText(this, "Failed to obtain location coordinates.", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng getLatLngFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            Log.e("Geocoder", "Address is null or empty.");
            return null;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                Log.e("Geocoder", "No address found for: " + address);
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Geocoding failed: " + e.getMessage());
        }
        return null;
    }

    private void drawRoute() {
        if (mMap == null || driverLatLng == null || restaurantLatLng == null || customerLatLng == null) {
            Toast.makeText(this, "Cannot draw route: missing coordinates.", Toast.LENGTH_SHORT).show();
            return;
        }

        mMap.clear(); // Clear existing markers/polylines for a clean redraw

        // Add updated markers with more descriptive titles
        mMap.addMarker(new MarkerOptions()
                .position(driverLatLng)
                .title("Start: Driver")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addMarker(new MarkerOptions()
                .position(restaurantLatLng)
                .title("First Stop: Restaurant")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mMap.addMarker(new MarkerOptions()
                .position(customerLatLng)
                .title("Final Stop: Customer")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Draw route: driver → restaurant → customer
        PolylineOptions route = new PolylineOptions()
                .add(driverLatLng)
                .add(restaurantLatLng)
                .add(customerLatLng)
                .width(10)
                .color(0xFF0000FF); // Blue polyline

        mMap.addPolyline(route);

        // Zoom camera to show all points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(driverLatLng);
        builder.include(restaurantLatLng);
        builder.include(customerLatLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private void markOrderAsDelivered() {
        if (orderId == null) return;

        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

        if (driverLatLng == null || restaurantLatLng == null || customerLatLng == null) {
            Toast.makeText(this, "Missing coordinate data", Toast.LENGTH_SHORT).show();
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
        String estimatedTime = estimatedTimeMins + " mins";

        DatabaseReference orderRef = ordersRef.child(orderId);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("status", "delivered");
        updates.put("timestamps/delivered", now);
        updates.put("estimatedDeliveryTime", estimatedTime);

        HashMap<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", now);
        logEntry.put("status", "delivered");
        logEntry.put("note", "Status updated to delivered by driver.");

        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                orderRef.child("updateLogs").push().setValue(logEntry);
                Toast.makeText(this, "Order marked as delivered.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DriverLandingActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to update order status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}