package com.example.togoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.togoo.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        // Initialize Toolbar and enable back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("ProfilePictures");

        profileImage = findViewById(R.id.profileImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        btnSave = findViewById(R.id.btnSave);

        emailInput.setEnabled(false);

        findUserRoleAndLoadData();

        btnUploadImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> uploadImageToFirebase());
    }

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

                        if (!TextUtils.isEmpty(user.getImageURL())) {
                            Glide.with(ProfileActivity.this)
                                    .load(user.getImageURL())
                                    .placeholder(R.drawable.ic_account2)
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

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

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
                    updateUserData();
                } else {
                    Toast.makeText(ProfileActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateUserData();
        }
    }

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

    // Handle toolbar back navigation
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}