package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodItem;
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

        categoryRecyclerView = findViewById(R.id.featuredCategoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbReference = FirebaseDatabase.getInstance().getReference("restaurant");

        // Retrieve selected category from intent
        selectedCategory = getIntent().getStringExtra("selectedCategory");

        if (selectedCategory != null) {
            fetchCategoryItems(selectedCategory);
        } else {
            Toast.makeText(this, "Invalid Category Selection", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 🔹 Fetch Category Items from Firebase
    private void fetchCategoryItems(String category) {
        List<String> restaurantNames = getRestaurantsForCategory(category);
        foodItemList.clear();

        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    String restaurantName = restaurant.child("name").getValue(String.class);

                    if (restaurantName != null && restaurantNames.contains(restaurantName)) {
                        DataSnapshot menuSnapshot = restaurant.child("menu").child(category);
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

    // 🔹 Set Up RecyclerView
    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(this, foodItemList, foodItem -> {
            Intent intent = new Intent(FeaturedCategoryActivity.this, FoodDetailActivity.class);
            intent.putExtra("foodId", foodItem.getId());
            intent.putExtra("foodDescription", foodItem.getDescription());
            intent.putExtra("foodImage", foodItem.getImageUrl());
            intent.putExtra("foodPrice", foodItem.getPrice());
            startActivity(intent);
        });
        categoryRecyclerView.setAdapter(foodAdapter);
    }

    // 🔹 Get Restaurant Names for Category
    private List<String> getRestaurantsForCategory(String category) {
        List<String> restaurantNames = new ArrayList<>();
        switch (category) {
            case "Pizza":
            case "Pasta":
                restaurantNames.add("American Cuisine");
                restaurantNames.add("Italian Cuisine");
                break;
            case "Burgers":
            case "Seafood":
            case "Salads":
                restaurantNames.add("American Cuisine");
                break;
            case "Sushi":
                restaurantNames.add("Japanese Cuisine");
                restaurantNames.add("American Cuisine");
                break;
            case "Tacos":
                restaurantNames.add("Mexican Cuisine");
                break;
            case "Desserts":
                restaurantNames.add("Canadian Dishes");
                break;
        }
        return restaurantNames;
    }
}