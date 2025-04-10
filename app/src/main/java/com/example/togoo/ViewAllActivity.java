package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodItem;
import com.example.togoo.utils.RestaurantHelper;

import java.util.ArrayList;
import java.util.List;

public class ViewAllActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView viewAllRecyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);

        // Set up custom toolbar with back navigation
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        viewAllRecyclerView = findViewById(R.id.viewAllRecyclerView);
        viewAllRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve the list of food items passed via intent
//        foodItems = getIntent().getParcelableArrayListExtra("searchResults");
//        if (foodItems == null) {
//            foodItems = new ArrayList<>();
//        }

        foodItems = getIntent().getParcelableArrayListExtra("searchResults");
        if (foodItems == null) {
            Log.e("ViewAllActivity", "FoodItems is null");
            foodItems = new ArrayList<>();
        } else {
            Log.d("ViewAllActivity", "Received " + foodItems.size() + " items");
            for (FoodItem item : foodItems) {
                Log.d("ViewAllActivity", "Item: id=" + item.getId() + ", restaurantId=" + item.getRestaurantId() +
                        ", parentNode=" + item.getParentNode() + ", category=" + item.getCategory());
            }
        }

        foodAdapter = new FoodAdapter(
                this,
                foodItems,
                RestaurantHelper.getCurrentRestaurant(), // ✅ Use centralized Restaurant
                foodItem -> {
                    Intent intent = new Intent(ViewAllActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
                    startActivity(intent);
                }
        );

        viewAllRecyclerView.setAdapter(foodAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Back button behavior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}