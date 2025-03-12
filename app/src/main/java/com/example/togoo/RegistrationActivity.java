package com.example.togoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import android.database.*;
import android.provider.OpenableColumns;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private EditText inputName, inputEmail, inputPhone, inputAddress, inputPassword, inputConfirmPassword;
    private Spinner businessTypeSpinner;
    private LinearLayout driverFields, restaurantFields;
    private EditText inputDriverLicense, inputVehicleRegistration, inputRestaurantLicense, inputRetailLicense;
    private CheckBox termsCheckbox;
    private Button registerButton, uploadDriverLicenseBtn, uploadVehicleRegistrationBtn, uploadRestaurantLicenseBtn, uploadRetailLicenseBtn;
    private Uri driverLicenseUri, vehicleRegistrationUri, restaurantLicenseUri, retailLicenseUri;
    private int currentUploadRequest = -1;
    private ProgressDialog progressDialog;
    private String businessType, userId;
    private DatabaseReference userRef;

    // Atomic counter to track file uploads or URL assignments
    private AtomicInteger uploadCount;
    private int expectedUploads; // will be determined dynamically for each file field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI Elements
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputAddress = findViewById(R.id.inputAddress);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        businessTypeSpinner = findViewById(R.id.businessTypeSpinner);
        driverFields = findViewById(R.id.driverFields);
        restaurantFields = findViewById(R.id.restaurantFields);
        inputDriverLicense = findViewById(R.id.inputDriverLicense);
        inputVehicleRegistration = findViewById(R.id.inputVehicleRegistration);
        inputRestaurantLicense = findViewById(R.id.inputRestaurantLicense);
        inputRetailLicense = findViewById(R.id.inputRetailLicense);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        registerButton = findViewById(R.id.registerButton);
        uploadDriverLicenseBtn = findViewById(R.id.uploadDriverLicense);
        uploadVehicleRegistrationBtn = findViewById(R.id.uploadVehicleRegistration);
        uploadRestaurantLicenseBtn = findViewById(R.id.uploadRestaurantLicense);
        uploadRetailLicenseBtn = findViewById(R.id.uploadRetailLicense);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.business_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        businessTypeSpinner.setAdapter(adapter);

        // Handle Business Type Selection
        businessTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                businessType = businessTypeSpinner.getSelectedItem().toString();
                if (businessType.equalsIgnoreCase("Driver")) {
                    driverFields.setVisibility(android.view.View.VISIBLE);
                    restaurantFields.setVisibility(android.view.View.GONE);
                } else if (businessType.equalsIgnoreCase("Restaurant")) {
                    restaurantFields.setVisibility(android.view.View.VISIBLE);
                    driverFields.setVisibility(android.view.View.GONE);
                } else {
                    driverFields.setVisibility(android.view.View.GONE);
                    restaurantFields.setVisibility(android.view.View.GONE);
                }
                // Reset the counter and determine expectedUploads dynamically below.
                uploadCount = new AtomicInteger(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // File Upload Handlers
        uploadDriverLicenseBtn.setOnClickListener(v -> selectFile(1));
        uploadVehicleRegistrationBtn.setOnClickListener(v -> selectFile(2));
        uploadRestaurantLicenseBtn.setOnClickListener(v -> selectFile(3));
        uploadRetailLicenseBtn.setOnClickListener(v -> selectFile(4));

        // Register Button Click
        registerButton.setOnClickListener(v -> registerBusiness());
    }

    private void selectFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        currentUploadRequest = requestCode;
        filePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        String fileName = getFileName(selectedFileUri);
                        switch (currentUploadRequest) {
                            case 1:
                                driverLicenseUri = selectedFileUri;
                                inputDriverLicense.setText(fileName);
                                break;
                            case 2:
                                vehicleRegistrationUri = selectedFileUri;
                                inputVehicleRegistration.setText(fileName);
                                break;
                            case 3:
                                restaurantLicenseUri = selectedFileUri;
                                inputRestaurantLicense.setText(fileName);
                                break;
                            case 4:
                                retailLicenseUri = selectedFileUri;
                                inputRetailLicense.setText(fileName);
                                break;
                        }
                    }
                }
            });

    private void registerBusiness() {
        // Basic field validation
        if (TextUtils.isEmpty(inputName.getText().toString().trim()) ||
                TextUtils.isEmpty(inputEmail.getText().toString().trim()) ||
                TextUtils.isEmpty(inputPhone.getText().toString().trim()) ||
                TextUtils.isEmpty(inputAddress.getText().toString().trim()) ||
                TextUtils.isEmpty(inputPassword.getText().toString().trim()) ||
                TextUtils.isEmpty(inputConfirmPassword.getText().toString().trim())) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        auth.createUserWithEmailAndPassword(inputEmail.getText().toString().trim(), inputPassword.getText().toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userId = auth.getCurrentUser().getUid();
                        // Store user details under "driver" or "restaurant" node based on businessType
                        userRef = database.getReference(businessType.toLowerCase()).child(userId);

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", inputName.getText().toString().trim());
                        user.put("email", inputEmail.getText().toString().trim());
                        user.put("phone", inputPhone.getText().toString().trim());
                        user.put("address", inputAddress.getText().toString().trim());
                        user.put("role", businessType);

                        // ✅ Set registration status as pending
                        user.put("status", "pending");

                        // For each file field, if a file was selected, expectedUploads will be incremented;
                        // otherwise, we use the input text value and increment expectedUploads.
                        expectedUploads = 0;
                        // For drivers:
                        if (businessType.equalsIgnoreCase("driver")) {
                            // Driver's License
                            if (driverLicenseUri != null) {
                                expectedUploads++;
                                uploadFile(storage.getReference().child("driver").child(userId + "_driverLicense.jpg"),
                                        driverLicenseUri, user, "driverLicense", userRef);
                            } else {
                                user.put("driverLicense", inputDriverLicense.getText().toString().trim());
                                expectedUploads++;
                                if (uploadCount.incrementAndGet() == expectedUploads) {
                                    updateUserRecord(userRef, user);
                                }
                            }
                            // Vehicle Registration
                            if (vehicleRegistrationUri != null) {
                                expectedUploads++;
                                uploadFile(storage.getReference().child("driver").child(userId + "_vehicleRegistration.jpg"),
                                        vehicleRegistrationUri, user, "vehicleRegistration", userRef);
                            } else {
                                user.put("vehicleRegistration", inputVehicleRegistration.getText().toString().trim());
                                expectedUploads++;
                                if (uploadCount.incrementAndGet() == expectedUploads) {
                                    updateUserRecord(userRef, user);
                                }
                            }
                        } else if (businessType.equalsIgnoreCase("restaurant")) {
                            // Restaurant License
                            if (restaurantLicenseUri != null) {
                                expectedUploads++;
                                uploadFile(storage.getReference().child("restaurant").child(userId + "_restaurantLicense.jpg"),
                                        restaurantLicenseUri, user, "restaurantLicense", userRef);
                            } else {
                                user.put("restaurantLicense", inputRestaurantLicense.getText().toString().trim());
                                expectedUploads++;
                                if (uploadCount.incrementAndGet() == expectedUploads) {
                                    updateUserRecord(userRef, user);
                                }
                            }
                            // Retail License
                            if (retailLicenseUri != null) {
                                expectedUploads++;
                                uploadFile(storage.getReference().child("restaurant").child(userId + "_retailLicense.jpg"),
                                        retailLicenseUri, user, "retailLicense", userRef);
                            } else {
                                user.put("retailLicense", inputRetailLicense.getText().toString().trim());
                                expectedUploads++;
                                if (uploadCount.incrementAndGet() == expectedUploads) {
                                    updateUserRecord(userRef, user);
                                }
                            }
                        } else {
                            // If businessType is neither Driver nor Restaurant, no file upload expected.
                            expectedUploads = 0;
                        }

                        // If no uploads are expected, update user record immediately.
                        if (expectedUploads == 0) {
                            updateUserRecord(userRef, user);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void uploadFile(StorageReference ref, Uri fileUri, Map<String, Object> user, String key, DatabaseReference userRef) {
        if (fileUri != null) {
            ProgressDialog uploadProgress = new ProgressDialog(this);
            uploadProgress.setTitle("Uploading " + key);
            uploadProgress.setMessage("Please wait...");
            uploadProgress.setCancelable(false);
            uploadProgress.show();

            Log.d("UPLOAD_PATH", "Uploading to: " + ref.getPath()); // 🔍 Log the full upload path

            ref.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                user.put(key, uri.toString());
                                uploadProgress.dismiss(); // Close progress dialog
                                Log.d("UPLOAD_SUCCESS", "File uploaded successfully: " + uri.toString()); // ✅ Log Success

                                if (uploadCount.incrementAndGet() == expectedUploads) {
                                    updateUserRecord(userRef, user);
                                }
                            }).addOnFailureListener(e -> {
                                uploadProgress.dismiss();
                                Log.e("UPLOAD_ERROR", "Failed to get download URL for " + key, e);
                            })
                    ).addOnFailureListener(e -> {
                        uploadProgress.dismiss();
                        Log.e("UPLOAD_ERROR", "Failed to upload " + key, e);
                    });
        } else {
            Log.w("UPLOAD_WARNING", key + " file URI is null, skipping upload.");
        }
    }

//    private void updateUserRecord(DatabaseReference userRef, Map<String, Object> user) {
//        userRef.setValue(user).addOnCompleteListener(task -> {
//            progressDialog.dismiss();
//            if (task.isSuccessful()) {
//                Toast.makeText(RegistrationActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
//                finish();
//            } else {
//                Toast.makeText(RegistrationActivity.this, "Registration update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    private void updateUserRecord(DatabaseReference userRef, Map<String, Object> user) {
        userRef.setValue(user).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(RegistrationActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegistrationActivity.this, RegistrationStatusActivity.class));
                finish();
            } else {
                Toast.makeText(RegistrationActivity.this, "Registration update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        fileName = cursor.getString(columnIndex);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment(); // Fallback if metadata is unavailable
        }
        return fileName;
    }

}

