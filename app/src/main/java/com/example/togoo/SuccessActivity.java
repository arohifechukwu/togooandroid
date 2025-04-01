package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    private Button btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(SuccessActivity.this, CustomerLandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}