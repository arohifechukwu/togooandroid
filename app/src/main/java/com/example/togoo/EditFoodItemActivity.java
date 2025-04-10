package com.example.togoo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditFoodItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView foodImage;
    private EditText foodName, foodDescription, foodPrice;
    private Button saveButton, chooseImageButton;

    private String foodId, nodeType, restaurantId, category;
    private String imageUrl;
    private Uri selectedImageUri;

    private DatabaseReference foodRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food_item);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.foodDetailsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // UI elements
        foodImage = findViewById(R.id.editFoodImage);
        foodName = findViewById(R.id.editFoodName);
        foodDescription = findViewById(R.id.editFoodDescription);
        foodPrice = findViewById(R.id.editFoodPrice);
        chooseImageButton = findViewById(R.id.pickImageButton);
        saveButton = findViewById(R.id.saveFoodButton);

        // Get passed data
        Intent intent = getIntent();
        foodId = intent.getStringExtra("foodId");
        nodeType = intent.getStringExtra("parentNode"); // Fix: correct key
        restaurantId = intent.getStringExtra("restaurantUID"); // Fix: correct key
        category = intent.getStringExtra("category");

        if (restaurantId == null || nodeType == null || foodId == null) {
            Toast.makeText(this, "Missing food reference info.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String name = intent.getStringExtra("foodName");
        String description = intent.getStringExtra("foodDescription");
        double price = intent.getDoubleExtra("price", 0.0);
        imageUrl = intent.getStringExtra("imageURL");

      // Pre-fill fields
        if (name != null) foodName.setText(name);
        if (description != null) foodDescription.setText(description);
        foodPrice.setText(String.valueOf(price));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.burger).into(foodImage);
        }

       // Safe Firebase reference setup
        if (restaurantId != null && foodId != null && nodeType != null) {
            if ("menu".equals(nodeType) && category != null) {
                foodRef = FirebaseDatabase.getInstance().getReference("restaurant")
                        .child(restaurantId)
                        .child("menu")
                        .child(category)
                        .child(foodId);
            } else {
                foodRef = FirebaseDatabase.getInstance().getReference("restaurant")
                        .child(restaurantId)
                        .child(nodeType)
                        .child(foodId);
            }
        } else {
            Toast.makeText(this, "Missing food reference info.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Actions
        chooseImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void saveChanges() {
        String name = foodName.getText().toString().trim();
        String desc = foodDescription.getText().toString().trim();
        String priceText = foodPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceText)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceText);

        // Update exact node
        foodRef.child("id").setValue(name);
        foodRef.child("description").setValue(desc);
        foodRef.child("price").setValue(price);

        if (selectedImageUri != null) {
            // Just update URI if changed; for Firebase Storage integration, change this logic
            foodRef.child("imageURL").setValue(selectedImageUri.toString());
        }

        Toast.makeText(this, "Food item updated successfully", Toast.LENGTH_SHORT).show();
        finish(); // Return to manage screen
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            foodImage.setImageURI(selectedImageUri);
        }
    }
}