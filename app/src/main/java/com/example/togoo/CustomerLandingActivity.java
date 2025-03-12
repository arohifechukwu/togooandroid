//package com.example.togoo;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CustomerLandingActivity extends AppCompatActivity {
//
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
//
//    private TextView locationText;
//    private ImageButton cartButton;
//    private EditText searchBar;
//    private RecyclerView foodCategoriesList, featuredList, placesList, offersList, restaurantsList;
//    private FusedLocationProviderClient fusedLocationClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_customer_landing);
//
//        // Initialize UI Components
//        locationText = findViewById(R.id.locationText);
//        cartButton = findViewById(R.id.cartButton);
//        searchBar = findViewById(R.id.searchBar);
//        foodCategoriesList = findViewById(R.id.foodCategoriesList);
//        featuredList = findViewById(R.id.featuredList);
//        placesList = findViewById(R.id.placesList);
//        offersList = findViewById(R.id.offersList);
//        restaurantsList = findViewById(R.id.restaurantsList);
//
//        // Initialize Google Location Services
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        fetchCustomerLocation();
//
//        // Cart Button Click → Open CartActivity
//        cartButton.setOnClickListener(v -> {
//            Intent intent = new Intent(CustomerLandingActivity.this, CartActivity.class);
//            startActivity(intent);
//        });
//
//        // Search Bar Click → Open FoodSearchActivity
//        searchBar.setOnClickListener(v -> {
//            Intent intent = new Intent(CustomerLandingActivity.this, FoodSearchActivity.class);
//            startActivity(intent);
//        });
//
//        // Set up RecyclerViews
//        setupRecyclerViews();
//
//        // Bottom Navigation Bar Setup
//        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
//        bottomNav.setSelectedItemId(R.id.nav_home);
//        bottomNav.setOnNavigationItemSelectedListener(item -> onNavigationItemSelected(item.getItemId()));
//    }
//
//    private void fetchCustomerLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // Request permissions if not granted
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//            return;
//        }
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location != null) {
//                            String locationTextStr = "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
//                            locationText.setText(locationTextStr);
//                        } else {
//                            locationText.setText("Location Unavailable");
//                        }
//                    }
//                });
//
//        locationText.setOnClickListener(v -> {
//            Intent intent = new Intent(CustomerLandingActivity.this, LocationSelectionActivity.class);
//            startActivity(intent);
//        });
//    }
//
//    private void setupRecyclerViews() {
//        // Set Layout Managers
//        foodCategoriesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        featuredList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        placesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        offersList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        restaurantsList.setLayoutManager(new LinearLayoutManager(this));
//
//        // Load Data (Mock Data for now)
//        foodCategoriesList.setAdapter(new FoodCategoryAdapter(getMockFoodCategories()));
//        featuredList.setAdapter(new FoodAdapter(getMockFeaturedFoods()));
//        placesList.setAdapter(new RestaurantAdapter(getMockPlaces()));
//        offersList.setAdapter(new FoodAdapter(getMockOffers()));
//        restaurantsList.setAdapter(new RestaurantAdapter(getMockRestaurants()));
//    }
//
//    private boolean onNavigationItemSelected(int itemId) {
//        switch (itemId) {
//            case R.id.nav_home:
//                return true;
//            case R.id.nav_restaurants:
//                startActivity(new Intent(this, RestaurantsActivity.class));
//                return true;
//            case R.id.nav_browse:
//                startActivity(new Intent(this, BrowseActivity.class));
//                return true;
//            case R.id.nav_orders:
//                startActivity(new Intent(this, OrderActivity.class));
//                return true;
//            case R.id.nav_account:
//                startActivity(new Intent(this, AccountActivity.class));
//                return true;
//        }
//        return false;
//    }
//
//    private List<String> getMockFoodCategories() {
//        List<String> categories = new ArrayList<>();
//        for (int i = 1; i <= 15; i++) categories.add("Category " + i);
//        return categories;
//    }
//
//    private List<String> getMockFeaturedFoods() {
//        List<String> featured = new ArrayList<>();
//        for (int i = 1; i <= 10; i++) featured.add("Featured Food " + i);
//        return featured;
//    }
//
//    private List<String> getMockPlaces() {
//        List<String> places = new ArrayList<>();
//        for (int i = 1; i <= 10; i++) places.add("Restaurant " + i);
//        return places;
//    }
//
//    private List<String> getMockOffers() {
//        List<String> offers = new ArrayList<>();
//        for (int i = 1; i <= 10; i++) offers.add("Offer " + i);
//        return offers;
//    }
//
//    private List<String> getMockRestaurants() {
//        List<String> restaurants = new ArrayList<>();
//        for (int i = 1; i <= 20; i++) restaurants.add("Restaurant " + i);
//        return restaurants;
//    }
//
//    // Handle Permission Request Result
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            fetchCustomerLocation();
//        }
//    }
//}


package com.example.togoo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerLandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_landing);
    }
}