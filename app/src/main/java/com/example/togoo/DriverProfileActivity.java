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

public class DriverProfileActivity extends AppCompatActivity {

    private ImageView profileImageView, carImageView;
    private EditText nameInput, addressInput, phoneInput, emailInput;
    private EditText carBrandInput, carModelInput, licensePlateInput;
    private Button btnUploadProfileImage, btnUploadCarImage, btnSave;
    private DatabaseReference driverRef;
    private FirebaseUser currentUser;
    private StorageReference storageRefProfile, storageRefCar;
    private Uri profileImageUri, carImageUri;
    private String profileImageURL, carImageURL;
    private final int REQUEST_CODE_PROFILE_IMAGE = 101;
    private final int REQUEST_CODE_CAR_IMAGE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Driver Profile");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        profileImageView = findViewById(R.id.profileImage);
        carImageView = findViewById(R.id.carImage);
        btnUploadProfileImage = findViewById(R.id.btnUploadProfileImage);
        btnUploadCarImage = findViewById(R.id.btnUploadCarImage);
        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        carBrandInput = findViewById(R.id.carBrandInput);
        carModelInput = findViewById(R.id.carModelInput);
        licensePlateInput = findViewById(R.id.licensePlateInput);
        btnSave = findViewById(R.id.btnSave);

        // Email is read-only.
        emailInput.setEnabled(false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Assuming driver profiles are stored under "driver" node.
        driverRef = FirebaseDatabase.getInstance().getReference("driver").child(currentUser.getUid());
        // Create separate StorageReferences for profile and car images.
        storageRefProfile = FirebaseStorage.getInstance().getReference("DriverProfilePictures");
        storageRefCar = FirebaseStorage.getInstance().getReference("DriverCarPictures");

        loadDriverData();

        btnUploadProfileImage.setOnClickListener(v -> selectImage(REQUEST_CODE_PROFILE_IMAGE));
        btnUploadCarImage.setOnClickListener(v -> selectImage(REQUEST_CODE_CAR_IMAGE));

        btnSave.setOnClickListener(v -> saveDriverProfile());
    }

    private void loadDriverData() {
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String carBrand = snapshot.child("carBrand").getValue(String.class);
                    String carModel = snapshot.child("carModel").getValue(String.class);
                    String licensePlate = snapshot.child("licensePlate").getValue(String.class);
                    profileImageURL = snapshot.child("imageURL").getValue(String.class);
                    carImageURL = snapshot.child("carPicture").getValue(String.class);

                    if (name != null) nameInput.setText(name);
                    if (address != null) addressInput.setText(address);
                    if (phone != null) phoneInput.setText(phone);
                    if (email != null) emailInput.setText(email);
                    if (carBrand != null) carBrandInput.setText(carBrand);
                    if (carModel != null) carModelInput.setText(carModel);
                    if (licensePlate != null) licensePlateInput.setText(licensePlate);

                    if (!TextUtils.isEmpty(profileImageURL)) {
                        Glide.with(DriverProfileActivity.this)
                                .load(profileImageURL)
                                .placeholder(R.drawable.ic_account2)
                                .into(profileImageView);
                    }
                    // Always display car image: if field exists use it, otherwise use placeholder.
                    if (!TextUtils.isEmpty(carImageURL)) {
                        Glide.with(DriverProfileActivity.this)
                                .load(carImageURL)
                                .placeholder(R.drawable.ic_car_placeholder)
                                .into(carImageView);
                    } else {
                        carImageView.setImageResource(R.drawable.ic_car_placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverProfileActivity.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == REQUEST_CODE_PROFILE_IMAGE) {
                profileImageUri = data.getData();
                profileImageView.setImageURI(profileImageUri);
            } else if (requestCode == REQUEST_CODE_CAR_IMAGE) {
                carImageUri = data.getData();
                carImageView.setImageURI(carImageUri);
            }
        }
    }

    private void saveDriverProfile() {
        // Check which images need to be uploaded.
        boolean profileChanged = (profileImageUri != null);
        boolean carChanged = (carImageUri != null);

        // If both images have been updated, chain uploads sequentially.
        if (profileChanged && carChanged) {
            uploadImage(profileImageUri, storageRefProfile, "imageURL", () -> {
                uploadImage(carImageUri, storageRefCar, "carPicture", () -> updateDriverData());
            });
        } else if (profileChanged) {
            uploadImage(profileImageUri, storageRefProfile, "imageURL", () -> updateDriverData());
        } else if (carChanged) {
            uploadImage(carImageUri, storageRefCar, "carPicture", () -> updateDriverData());
        } else {
            updateDriverData();
        }
    }

    private void uploadImage(Uri imageUri, StorageReference storageReference, String field, Runnable onComplete) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        StorageReference fileRef = storageReference.child(currentUser.getUid() + "_" + field + ".jpg");
        fileRef.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return fileRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                String url = task.getResult().toString();
                driverRef.child(field).setValue(url);
                onComplete.run();
            } else {
                Toast.makeText(DriverProfileActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDriverData() {
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String carBrand = carBrandInput.getText().toString().trim();
        String carModel = carModelInput.getText().toString().trim();
        String licensePlate = licensePlateInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(carBrand) || TextUtils.isEmpty(carModel) || TextUtils.isEmpty(licensePlate)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);
        updates.put("phone", phone);
        updates.put("carBrand", carBrand);
        updates.put("carModel", carModel);
        updates.put("licensePlate", licensePlate);
        // imageURL and carPicture are already updated via uploadImage() calls.

        driverRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DriverProfileActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DriverProfileActivity.this, "Update Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}