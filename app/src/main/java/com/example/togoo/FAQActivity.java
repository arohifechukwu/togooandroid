package com.example.togoo;

import android.os.Bundle;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FAQActivity extends AppCompatActivity {

    private ExpandableListView faqExpandableListView;
    private com.example.togoo.FAQExpandableListAdapter adapter;
    private List<String> faqQuestions;
    private HashMap<String, List<String>> faqData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqactivity);

        faqExpandableListView = findViewById(R.id.faqExpandableListView);
        prepareFAQData();
        adapter = new com.example.togoo.FAQExpandableListAdapter(this, faqQuestions, faqData);
        faqExpandableListView.setAdapter(adapter);
    }

    private void prepareFAQData() {
        faqQuestions = new ArrayList<>();
        faqData = new HashMap<>();

        // Add FAQ questions
        faqQuestions.add("What is Togoo?");
        faqQuestions.add("How do I create an account?");
        faqQuestions.add("How do I place an order?");
        faqQuestions.add("What payment methods are available?");
        faqQuestions.add("How is my total calculated?");
        faqQuestions.add("Can I track my order?");
        faqQuestions.add("What is your refund or cancellation policy?");
        faqQuestions.add("How do I update my profile information?");
        faqQuestions.add("Who do I contact for support?");

        // Add corresponding answers for each question
        List<String> answer1 = new ArrayList<>();
        answer1.add("Togoo is a user-friendly application designed to streamline your ordering experience. Whether you’re ordering food for delivery or planning to pick it up, Togoo offers a seamless interface for managing your orders.");

        List<String> answer2 = new ArrayList<>();
        answer2.add("To create an account, tap on the 'Sign Up' button on the welcome screen and fill in your details. You can also sign in using your social media accounts.");

        List<String> answer3 = new ArrayList<>();
        answer3.add("Browse the available items, add your chosen products to the cart, and proceed to checkout. A summary of your order including taxes and total will be displayed.");

        List<String> answer4 = new ArrayList<>();
        answer4.add("Togoo supports payment via credit card through our secure Stripe integration, or you can opt for the pick-up option.");

        List<String> answer5 = new ArrayList<>();
        answer5.add("Your total is calculated by adding the subtotal, GST (5%), and QST (9.975%). The final total is displayed on the checkout screen.");

        List<String> answer6 = new ArrayList<>();
        answer6.add("Yes, after placing your order, you can track its progress through the 'Orders' section and receive notifications.");

        List<String> answer7 = new ArrayList<>();
        answer7.add("If you encounter any issues, contact our support within 24 hours. Details are provided in our Terms & Conditions.");

        List<String> answer8 = new ArrayList<>();
        answer8.add("Update your profile by navigating to the 'Profile' section where you can modify personal and payment information.");

        List<String> answer9 = new ArrayList<>();
        answer9.add("For support, visit the 'Help' section in the app or email support@togoo.com.");

        faqData.put(faqQuestions.get(0), answer1);
        faqData.put(faqQuestions.get(1), answer2);
        faqData.put(faqQuestions.get(2), answer3);
        faqData.put(faqQuestions.get(3), answer4);
        faqData.put(faqQuestions.get(4), answer5);
        faqData.put(faqQuestions.get(5), answer6);
        faqData.put(faqQuestions.get(6), answer7);
        faqData.put(faqQuestions.get(7), answer8);
        faqData.put(faqQuestions.get(8), answer9);
    }
}
