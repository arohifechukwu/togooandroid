package com.example.togoo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RestaurantprofileFragment extends Fragment {

    private static final String TAG = "RestaurantprofileFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView back_button, profileImage;
    private EditText usernameInput, emailInput, phoneInput;
    private Button updateButton;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private String userId;

    private String initialUsername, initialPhone, profileImageUrl;

    public RestaurantprofileFragment() {
    }

    public static RestaurantprofileFragment newInstance() {
        return new RestaurantprofileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurantprofile, container, false);

        back_button = view.findViewById(R.id.back_button);
        profileImage = view.findViewById(R.id.profile_image);
        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        phoneInput = view.findViewById(R.id.phone_input);
        updateButton = view.findViewById(R.id.button_update);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            fetchUserData();
        } else {
            Log.w(TAG, "User is not authenticated");
        }

        profileImage.setOnClickListener(v -> openImagePicker());
        updateButton.setOnClickListener(v -> updateUserProfile());

        return view;
    }

    private void fetchUserData() {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "Error fetching user data", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                initialUsername = documentSnapshot.getString("username");
                initialPhone = documentSnapshot.getString("phone");
                String email = documentSnapshot.getString("email");
                profileImageUrl = documentSnapshot.getString("profileImageUrl");

                usernameInput.setText(initialUsername);
                phoneInput.setText(initialPhone);
                emailInput.setText(email);
                emailInput.setEnabled(false);

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(getActivity())  // Fixed Context Issue
                            .load(profileImageUrl)
                            .apply(new RequestOptions()
                                    .circleCrop()
                                    .placeholder(com.example.togoo.R.drawable.baseline_person_24)
                                    .error(com.example.togoo.R.drawable.baseline_person_24))
                            .into(profileImage);
                }
            } else {
                Log.w(TAG, "User data does not exist");
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImageToFirebaseStorage();
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(userId + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        profileImageUrl = uri.toString();
                        firestore.collection("users").document(userId).update("profileImageUrl", profileImageUrl);
                    })
            ).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload image", e);
                Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
            });
        }    }

    private void updateUserProfile() {
        String newUsername = usernameInput.getText().toString().trim();
        String newPhone = phoneInput.getText().toString().trim();

        if (newUsername.length() < 8) {
            usernameInput.setError("Username must be at least 8 characters");
            return;
        }

        if (newPhone.length() != 10) {
            phoneInput.setError("Phone number must be 10 digits");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("phone", newPhone);

        firestore.collection("users").document(userId).update(updates).addOnSuccessListener(aVoid ->
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e -> Log.e(TAG, "Error updating profile", e));
    }
}
