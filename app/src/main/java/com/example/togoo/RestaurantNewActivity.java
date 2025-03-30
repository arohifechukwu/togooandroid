package com.example.togoo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.togoo.models.FoodItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class RestaurantNewActivity extends AppCompatActivity {

    private Spinner nodeSelector;
    private EditText foodIdInput, descriptionInput, priceInput, categoryInput;
    private ImageView foodImageView;
    private Button pickImageButton, createFoodButton;
    private Uri imageUri;
    private String restaurantUID;
    private DatabaseReference restaurantRef;

    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_new);

        nodeSelector = findViewById(R.id.nodeSelector);
        foodIdInput = findViewById(R.id.foodIdInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        categoryInput = findViewById(R.id.categoryInput);
        foodImageView = findViewById(R.id.foodImageView);
        pickImageButton = findViewById(R.id.pickImageButton);
        createFoodButton = findViewById(R.id.createFoodButton);

        restaurantUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantUID);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"menu", "Special Offers", "Top Picks"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSelector.setAdapter(adapter);

        // Show/hide category field
        nodeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                categoryInput.setVisibility("menu".equals(selected) ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        pickImageButton.setOnClickListener(v -> openImagePicker());
        createFoodButton.setOnClickListener(v -> validateAndSave());

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Food Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(foodImageView);
        }
    }

    private void validateAndSave() {
        String foodId = foodIdInput.getText().toString().trim();
        String desc = descriptionInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String node = nodeSelector.getSelectedItem().toString();

        if (TextUtils.isEmpty(foodId) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr) || imageUri == null || ("menu".equals(node) && TextUtils.isEmpty(category))) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndSave(node, foodId, desc, price, category);
    }

    private void uploadImageAndSave(String node, String foodId, String desc, double price, String category) {
        String imageName = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("restaurant_menu_images")
                .child(restaurantUID)
                .child(imageName + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    FoodItem foodItem = new FoodItem(foodId, desc, uri.toString(), price);
                    DatabaseReference ref;
                    if ("menu".equals(node)) {
                        ref = restaurantRef.child("menu").child(category).child(foodId);
                    } else {
                        ref = restaurantRef.child(node).child(foodId);
                    }
                    ref.setValue(foodItem).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Food item added", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    });
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void clearInputs() {
        foodIdInput.setText("");
        descriptionInput.setText("");
        priceInput.setText("");
        categoryInput.setText("");
        foodImageView.setImageResource(R.drawable.ic_food_placeholder);
        imageUri = null;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_new);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_new) {
                return true;
            } else if (id == R.id.navigation_manage) {
                startActivity(new Intent(this, RestaurantManageActivity.class));
            } else if (id == R.id.navigation_reports) {
                startActivity(new Intent(this, RestaurantReportActivity.class));
            } else if (id == R.id.navigation_orders) {
                startActivity(new Intent(this, RestaurantLandingActivity.class));
            } else if (id == R.id.navigation_account) {
                startActivity(new Intent(this, RestaurantAccountActivity.class));
            }
            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }
}