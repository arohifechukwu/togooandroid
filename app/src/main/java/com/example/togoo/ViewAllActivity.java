package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodItem;
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
        // Enable back button and set a custom icon
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Set custom back icon (ensure ic_back exists and is tinted appropriately)
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        viewAllRecyclerView = findViewById(R.id.viewAllRecyclerView);
        viewAllRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve the list of food items passed via intent
        foodItems = getIntent().getParcelableArrayListExtra("searchResults");
        if (foodItems == null) {
            foodItems = new ArrayList<>();
        }

        // Create and set the adapter (using your existing FoodAdapter with the "item_food" layout)
        foodAdapter = new FoodAdapter(this, foodItems, new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(FoodItem foodItem) {
                // When an item is clicked, open FoodDetailActivity with the item details
                Intent intent = new Intent(ViewAllActivity.this, FoodDetailActivity.class);
                intent.putExtra("foodId", foodItem.getId());
                intent.putExtra("foodDescription", foodItem.getDescription());
                intent.putExtra("foodImage", foodItem.getImageURL());
                intent.putExtra("foodPrice", foodItem.getPrice());
                startActivity(intent);
            }
        }); // Using the standard layout here
        viewAllRecyclerView.setAdapter(foodAdapter);
    }

    // Handle the custom back button press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Navigate back when the back button is clicked
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}