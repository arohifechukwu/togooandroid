package com.example.togoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.togoo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText nameInput, addressInput, phoneInput, emailInput;
    private Button btnUploadImage, btnSave;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private Uri imageUri;
    private String imageURL;
    private String userRole;
    private boolean userFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("ProfilePictures");

        // Initialize UI components
        profileImage = findViewById(R.id.profileImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        btnSave = findViewById(R.id.btnSave);

        // Disable email editing
        emailInput.setEnabled(false);

        // 🔹 Search all user roles for logged-in user
        findUserRoleAndLoadData();

        // Handle Image Upload
        btnUploadImage.setOnClickListener(v -> selectImage());

        // Save Updated User Data
        btnSave.setOnClickListener(v -> uploadImageToFirebase());
    }

    // 🔹 Search all roles (driver, customer, admin, restaurant) to find user
    private void findUserRoleAndLoadData() {
        String[] roles = {"driver", "customer", "admin", "restaurant"};
        for (String role : roles) {
            DatabaseReference roleRef = FirebaseDatabase.getInstance().getReference(role).child(currentUser.getUid());
            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && !userFound) {
                        userFound = true;
                        userRole = role;
                        userRef = roleRef;
                        loadUserData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 🔹 Load user data from Firebase
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        nameInput.setText(user.getName());
                        addressInput.setText(user.getAddress());
                        phoneInput.setText(user.getPhone());
                        emailInput.setText(user.getEmail());

                        // Load profile image using Glide
                        if (!TextUtils.isEmpty(user.getImageURL())) {
                            Glide.with(ProfileActivity.this)
                                    .load(user.getImageURL())
                                    .placeholder(R.drawable.ic_account2) // Default image
                                    .into(profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Select an image from the device
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    // 🔹 Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    // 🔹 Upload Image to Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    imageURL = task.getResult().toString();
                    updateUserData(); // Update profile data with new image
                } else {
                    Toast.makeText(ProfileActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateUserData(); // Save data even if image isn't changed
        }
    }

    // 🔹 Update user data in Firebase
    private void updateUserData() {
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);
        updates.put("phone", phone);
        if (imageURL != null) updates.put("imageURL", imageURL);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Update Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}