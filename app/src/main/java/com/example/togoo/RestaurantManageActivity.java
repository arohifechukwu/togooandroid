package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.togoo.models.FoodItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantManageActivity extends AppCompatActivity {

    private LinearLayout manageContainer;
    private DatabaseReference restaurantRef;
    private String restaurantUID;
    private EditText searchInput;
    private final List<FoodItem> allFoodItems = new ArrayList<>();
    private TextView resultCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_manage);

        searchInput = findViewById(R.id.searchInput);
        manageContainer = findViewById(R.id.manageContainer);
        restaurantUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantUID);
        resultCount = findViewById(R.id.resultCount);
        setupBottomNavigation();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoodItems(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchSectionItems("Special Offers");
        fetchSectionItems("Top Picks");
        fetchMenuCategories();
    }

    private void fetchSectionItems(String sectionName) {
        restaurantRef.child(sectionName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                removeSectionViews(sectionName);

                List<FoodItem> sectionItems = new ArrayList<>();

                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String id = itemSnap.getKey();
                    FoodItem item = itemSnap.getValue(FoodItem.class);
                    if (item != null && id != null) {
                        item.setId(id);
                        item.setCategory(null);
                        item.setParentNode(sectionName);
                        sectionItems.add(item);
                    }
                }

                allFoodItems.addAll(sectionItems);
                filterFoodItems(searchInput.getText().toString().trim());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantManageActivity.this, "Failed to load " + sectionName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMenuCategories() {
        restaurantRef.child("menu").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                removeMenuViews();

                List<FoodItem> menuItems = new ArrayList<>();

                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    String category = categorySnap.getKey();
                    for (DataSnapshot itemSnap : categorySnap.getChildren()) {
                        String id = itemSnap.getKey();
                        FoodItem item = itemSnap.getValue(FoodItem.class);
                        if (item != null && id != null) {
                            item.setId(id);
                            item.setCategory(category);
                            item.setParentNode("menu");
                            menuItems.add(item);
                        }
                    }
                }

                allFoodItems.addAll(menuItems);
                filterFoodItems(searchInput.getText().toString().trim());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantManageActivity.this, "Failed to load menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSectionHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(18);
        header.setPadding(8, 16, 8, 8);
        manageContainer.addView(header);
    }

    private void displayFoodItem(String parentNode, String category, String id, FoodItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_manage_food, manageContainer, false);

        TextView foodName = view.findViewById(R.id.foodName);
        TextView foodDescription = view.findViewById(R.id.foodDescription);
        TextView foodPrice = view.findViewById(R.id.foodPrice);
        Button editBtn = view.findViewById(R.id.editButton);
        Button deleteBtn = view.findViewById(R.id.deleteButton);
        ImageView foodImage = view.findViewById(R.id.foodImage);

        Glide.with(this)
                .load(item.getImageURL())
                .placeholder(R.drawable.ic_manage_food)
                .into(foodImage);

        foodName.setText(item.getId());
        foodDescription.setText(item.getDescription());
        foodPrice.setText("Price: $" + item.getPrice());

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFoodItemActivity.class);
            intent.putExtra("restaurantUID", restaurantUID);
            intent.putExtra("parentNode", parentNode);
            intent.putExtra("category", category);
            intent.putExtra("foodId", id);
            intent.putExtra("foodName", item.getId());
            intent.putExtra("foodDescription", item.getDescription());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("imageURL", item.getImageURL());
            startActivity(intent);
        });

        deleteBtn.setOnClickListener(v -> {
            DatabaseReference itemRef = "menu".equals(parentNode)
                    ? restaurantRef.child("menu").child(category).child(id)
                    : restaurantRef.child(parentNode).child(id);

            itemRef.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                manageContainer.removeView(view);
            });
        });

        manageContainer.addView(view);
    }

    private void filterFoodItems(String query) {
        manageContainer.removeAllViews();

        if (TextUtils.isEmpty(query)) {
            resultCount.setText("Food items found: " + allFoodItems.size() + " result" + (allFoodItems.size() == 1 ? "" : "s"));

            List<String> sectionOrder = List.of("Special Offers", "Top Picks", "menu");
            for (String section : sectionOrder) {
                boolean headerAdded = false;
                for (FoodItem item : allFoodItems) {
                    boolean isMatch = section.equals("menu")
                            ? item.getParentNode().equals("menu")
                            : item.getParentNode().equals(section);

                    if (isMatch) {
                        if (!headerAdded) {
                            String header = section.equals("menu") ? "Menu - " + item.getCategory() : section;
                            addSectionHeader(header);
                            headerAdded = true;
                        }
                        displayFoodItem(item.getParentNode(), item.getCategory(), item.getId(), item);
                    }
                }
            }
            return;
        }

        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            boolean matchByName = item.getId().toLowerCase().contains(query.toLowerCase());
            boolean matchBySection = item.getParentNode().toLowerCase().contains(query.toLowerCase());
            boolean matchByCategory = item.getCategory() != null && item.getCategory().toLowerCase().contains(query.toLowerCase());

            if (matchByName || matchBySection || matchByCategory) {
                filtered.add(item);
            }
        }

        // Sort: prioritize prefix matches
        filtered.sort((a, b) -> {
            boolean aStarts = a.getId().toLowerCase().startsWith(query.toLowerCase());
            boolean bStarts = b.getId().toLowerCase().startsWith(query.toLowerCase());
            return Boolean.compare(bStarts, aStarts);
        });

        resultCount.setText("Food items found: " + filtered.size() + " result" + (filtered.size() == 1 ? "" : "s"));

        for (FoodItem item : filtered) {
            String header = item.getParentNode().equals("menu") ?
                    "Menu - " + item.getCategory() :
                    item.getParentNode();

            addSectionHeader(header);
            displayFoodItem(item.getParentNode(), item.getCategory(), item.getId(), item);
        }

        //show result count
        if (filtered.isEmpty()) {
            TextView noResult = new TextView(this);
            noResult.setText("No matching food items.");
            noResult.setPadding(24, 40, 24, 40);
            noResult.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            manageContainer.addView(noResult);
        }
    }

    private void removeSectionViews(String sectionName) {
        for (int i = 0; i < manageContainer.getChildCount(); i++) {
            View child = manageContainer.getChildAt(i);
            if (child instanceof TextView && ((TextView) child).getText().toString().equals(sectionName)) {
                manageContainer.removeViewAt(i);
                break;
            }
        }
    }

    private void removeMenuViews() {
        manageContainer.removeAllViews();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_manage);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_manage) {
                    return true;
                } else if (id == R.id.navigation_new) {
                    startActivity(new Intent(RestaurantManageActivity.this, RestaurantNewActivity.class));
                    return true;
                } else if (id == R.id.navigation_reports) {
                    startActivity(new Intent(RestaurantManageActivity.this, RestaurantReportActivity.class));
                    return true;
                } else if (id == R.id.navigation_orders) {
                    startActivity(new Intent(RestaurantManageActivity.this, RestaurantLandingActivity.class));
                    return true;
                } else if (id == R.id.navigation_account) {
                    startActivity(new Intent(RestaurantManageActivity.this, RestaurantAccountActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
}
