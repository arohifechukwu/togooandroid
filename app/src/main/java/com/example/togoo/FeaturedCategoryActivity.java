//package com.example.togoo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.togoo.adapters.FoodAdapter;
//import com.example.togoo.models.FoodItem;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class FeaturedCategoryActivity extends AppCompatActivity {
//
//    private RecyclerView categoryRecyclerView;
//    private DatabaseReference dbReference;
//    private FoodAdapter foodAdapter;
//    private List<FoodItem> foodItemList = new ArrayList<>();
//    private String selectedCategory;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_featured_category);
//
//        // Setup Toolbar with custom back icon
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if(getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            // Optionally, set a custom icon if needed:
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
//        }
//
//        // Initialize RecyclerView and Database reference
//        categoryRecyclerView = findViewById(R.id.featuredCategoryRecyclerView);
//        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        dbReference = FirebaseDatabase.getInstance().getReference("restaurant");
//
//        // Retrieve selected category from intent
//        selectedCategory = getIntent().getStringExtra("selectedCategory");
//
//        if (selectedCategory != null) {
//            fetchCategoryItems(selectedCategory);
//        } else {
//            Toast.makeText(this, "Invalid Category Selection", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }
//
//
//    private void fetchCategoryItems(String category) {
//        foodItemList.clear();
//        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot restaurant : snapshot.getChildren()) {
//                    DataSnapshot menuSnapshot = restaurant.child("menu").child(category);
//                    if (menuSnapshot.exists()) {
//                        for (DataSnapshot foodSnapshot : menuSnapshot.getChildren()) {
//                            String id = foodSnapshot.getKey();
//                            String description = foodSnapshot.child("description").getValue(String.class);
//                            String imageUrl = foodSnapshot.child("imageURL").getValue(String.class);
//                            Double price = foodSnapshot.child("price").getValue(Double.class);
//                            if (id != null && description != null && imageUrl != null && price != null) {
//                                foodItemList.add(new FoodItem(id, description, imageUrl, price));
//                            }
//                        }
//                    }
//                }
//
//                if (foodItemList.isEmpty()) {
//                    Toast.makeText(FeaturedCategoryActivity.this, "No items found for " + category, Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    setupRecyclerView();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(FeaturedCategoryActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // 🔹 Set Up RecyclerView
//    private void setupRecyclerView() {
//        foodAdapter = new FoodAdapter(this, foodItemList, foodItem -> {
//            Intent intent = new Intent(FeaturedCategoryActivity.this, FoodDetailActivity.class);
//            intent.putExtra("foodId", foodItem.getId());
//            intent.putExtra("foodDescription", foodItem.getDescription());
//            intent.putExtra("foodImage", foodItem.getImageURL());
//            intent.putExtra("foodPrice", foodItem.getPrice());
//            startActivity(intent);
//        });
//        categoryRecyclerView.setAdapter(foodAdapter);
//    }
//
//    // 🔹 Get Restaurant Names for a Given Category
//    private List<String> getRestaurantsForCategory(String category) {
//        List<String> restaurantNames = new ArrayList<>();
//        switch (category) {
//            case "Pizza":
//            case "Pasta":
//                restaurantNames.add("American Cuisine");
//                restaurantNames.add("Italian Cuisine");
//                break;
//            case "Burgers":
//            case "Seafood":
//            case "Salads":
//                restaurantNames.add("American Cuisine");
//                break;
//            case "Sushi":
//                restaurantNames.add("Japanese Cuisine");
//                restaurantNames.add("American Cuisine");
//                break;
//            case "Tacos":
//                restaurantNames.add("Mexican Cuisine");
//                break;
//            case "Desserts":
//                restaurantNames.add("Canadian Dishes");
//                break;
//        }
//        return restaurantNames;
//    }
//
//    // Handle custom back navigation from the Toolbar
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if(item.getItemId() == android.R.id.home) {
//            finish(); // Finish the activity and go back
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}



package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodItem;
import com.example.togoo.utils.RestaurantHelper;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FeaturedCategoryActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;
    private DatabaseReference dbReference;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItemList = new ArrayList<>();
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured_category);

        // Setup Toolbar with custom back icon
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        categoryRecyclerView = findViewById(R.id.featuredCategoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbReference = FirebaseDatabase.getInstance().getReference("restaurant");

        selectedCategory = getIntent().getStringExtra("selectedCategory");

        if (selectedCategory != null) {
            fetchCategoryItems(selectedCategory);
        } else {
            Toast.makeText(this, "Invalid Category Selection", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchCategoryItems(String category) {
        foodItemList.clear();
        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    DataSnapshot menuSnapshot = restaurant.child("menu").child(category);
                    if (menuSnapshot.exists()) {
                        for (DataSnapshot foodSnapshot : menuSnapshot.getChildren()) {
                            String id = foodSnapshot.getKey();
                            String description = foodSnapshot.child("description").getValue(String.class);
                            String imageUrl = foodSnapshot.child("imageURL").getValue(String.class);
                            Double price = foodSnapshot.child("price").getValue(Double.class);
                            if (id != null && description != null && imageUrl != null && price != null) {
                                foodItemList.add(new FoodItem(id, description, imageUrl, price));
                            }
                        }
                    }
                }

                if (foodItemList.isEmpty()) {
                    Toast.makeText(FeaturedCategoryActivity.this, "No items found for " + category, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    setupRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FeaturedCategoryActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(
                this,
                foodItemList,
                RestaurantHelper.getCurrentRestaurant(), // ✅ Use shared restaurant
                foodItem -> {
                    Intent intent = new Intent(FeaturedCategoryActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
                    startActivity(intent);
                }
        );
        categoryRecyclerView.setAdapter(foodAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}