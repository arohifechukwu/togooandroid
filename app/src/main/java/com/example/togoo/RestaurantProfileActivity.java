package com.example.togoo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantProfileActivity extends AppCompatActivity {

    private EditText nameInput, addressInput, phoneInput;
    private TextView emailText;
    private ImageView profileImage;
    private LinearLayout hoursContainer;
    private Button updateButton, changeImageButton;

    private DatabaseReference restaurantRef;
    private String uid;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_profile);

        nameInput = findViewById(R.id.nameEditText);
        addressInput = findViewById(R.id.addressEditText);
        phoneInput = findViewById(R.id.phoneEditText);
        emailText = findViewById(R.id.emailEditText);
        profileImage = findViewById(R.id.profileImageView);
        hoursContainer = findViewById(R.id.operatingHoursContainer);
        updateButton = findViewById(R.id.saveChangesButton);
        changeImageButton = findViewById(R.id.changeImageButton);
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(uid);

        loadProfileData();

        changeImageButton.setOnClickListener(v -> chooseImage());

        updateButton.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String imageURL = snapshot.child("imageURL").getValue(String.class);

                nameInput.setText(name);
                addressInput.setText(address);
                phoneInput.setText(phone);
                emailText.setText(email);

                if (!TextUtils.isEmpty(imageURL)) {
                    Glide.with(RestaurantProfileActivity.this).load(imageURL).into(profileImage);
                }

                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                hoursContainer.removeAllViews();

                for (String day : days) {
                    View view = getLayoutInflater().inflate(R.layout.item_operating_hour, null);
                    TextView dayLabel = view.findViewById(R.id.dayLabel);
                    EditText openInput = view.findViewById(R.id.openTime);
                    EditText closeInput = view.findViewById(R.id.closeTime);

                    dayLabel.setText(day);
                    view.setTag(day); // Used during save

                    if (snapshot.child("operatingHours").hasChild(day)) {
                        openInput.setText(snapshot.child("operatingHours").child(day).child("open").getValue(String.class));
                        closeInput.setText(snapshot.child("operatingHours").child(day).child("close").getValue(String.class));
                    }

                    hoursContainer.addView(view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData() {
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        restaurantRef.child("name").setValue(name);
        restaurantRef.child("address").setValue(address);
        restaurantRef.child("phone").setValue(phone);

        // Upload image if selected
        if (imageUri != null) {
            uploadImage();
        }

        for (int i = 0; i < hoursContainer.getChildCount(); i++) {
            View v = hoursContainer.getChildAt(i);
            String day = (String) v.getTag();
            String open = ((EditText) v.findViewById(R.id.openTime)).getText().toString();
            String close = ((EditText) v.findViewById(R.id.closeTime)).getText().toString();

            DatabaseReference dayRef = restaurantRef.child("operatingHours").child(day);
            dayRef.child("open").setValue(open);
            dayRef.child("close").setValue(close);
        }

        updateCoordinatesFromAddress(address);
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
    }

    private void updateCoordinatesFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                restaurantRef.child("location").child("latitude").setValue(lat);
                restaurantRef.child("location").child("longitude").setValue(lon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("restaurant_profile_images").child(uid + ".jpg");
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                restaurantRef.child("imageURL").setValue(uri.toString());
                                Toast.makeText(RestaurantProfileActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> Toast.makeText(RestaurantProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
        }
    }
}
