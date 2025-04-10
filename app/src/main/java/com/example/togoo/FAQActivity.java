package com.example.togoo;

import android.os.Bundle;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.togoo.adapters.FAQExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FAQActivity extends AppCompatActivity {

    private ExpandableListView faqExpandableListView;
    private FAQExpandableListAdapter adapter;
    private List<String> faqQuestions;
    private HashMap<String, List<String>> faqData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqactivity);

        faqExpandableListView = findViewById(R.id.faqExpandableListView);
        prepareFAQData();
        adapter = new FAQExpandableListAdapter(this, faqQuestions, faqData);
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
        faqQuestions.add("How can I update my order?");
        faqQuestions.add("Is my payment secure?");
        faqQuestions.add("How do I apply promo codes?");
        faqQuestions.add("What are your delivery hours?");
        faqQuestions.add("Can I order for someone else?");

        // Add corresponding answers for each question

        // FAQ 1
        List<String> answer1 = new ArrayList<>();
        answer1.add("Togoo is a user-friendly application designed to streamline your ordering experience. Whether you’re ordering food for delivery or planning to pick it up, Togoo offers a seamless interface for managing your orders.");

        // FAQ 2
        List<String> answer2 = new ArrayList<>();
        answer2.add("To create an account, tap on the 'Sign Up' button on the welcome screen and fill in your details. You can also sign in using your social media accounts.");

        // FAQ 3
        List<String> answer3 = new ArrayList<>();
        answer3.add("Browse the available items, add your chosen products to the cart, and proceed to checkout. A summary of your order including taxes and total will be displayed.");

        // FAQ 4
        List<String> answer4 = new ArrayList<>();
        answer4.add("Togoo supports payment via credit card through our secure Stripe integration, or you can opt for the pick-up option.");

        // FAQ 5
        List<String> answer5 = new ArrayList<>();
        answer5.add("Your total is calculated by adding the subtotal, GST (5%), and QST (9.975%). The final total is displayed on the checkout screen.");

        // FAQ 6
        List<String> answer6 = new ArrayList<>();
        answer6.add("Yes, after placing your order, you can track its progress through the 'Orders' section and receive notifications.");

        // FAQ 7
        List<String> answer7 = new ArrayList<>();
        answer7.add("If you encounter any issues, contact our support within 24 hours. Details are provided in our Terms & Conditions.");

        // FAQ 8
        List<String> answer8 = new ArrayList<>();
        answer8.add("Update your profile by navigating to the 'Profile' section where you can modify personal and payment information.");

        // FAQ 9
        List<String> answer9 = new ArrayList<>();
        answer9.add("For support, visit the 'Help' section in the app or email support@togoo.com.");

        // FAQ 10
        List<String> answer10 = new ArrayList<>();
        answer10.add("If you need to update your order after submission, please contact our customer support immediately. Adjustments are possible if your order has not yet been processed.");

        // FAQ 11
        List<String> answer11 = new ArrayList<>();
        answer11.add("Yes, your payment is secure. All transactions are processed using industry-standard encryption and secure payment gateways.");

        // FAQ 12
        List<String> answer12 = new ArrayList<>();
        answer12.add("Promo codes can be applied during the checkout process. Simply enter your promo code in the designated field and tap 'Apply'.");

        // FAQ 13
        List<String> answer13 = new ArrayList<>();
        answer13.add("Our delivery service operates from 10 AM to 10 PM daily. Pick-up orders are available during the same hours.");

        // FAQ 14
        List<String> answer14 = new ArrayList<>();
        answer14.add("Yes, you can place an order on behalf of someone else. Just provide the correct delivery address and contact information.");

        // Map each question to its answer
        faqData.put(faqQuestions.get(0), answer1);
        faqData.put(faqQuestions.get(1), answer2);
        faqData.put(faqQuestions.get(2), answer3);
        faqData.put(faqQuestions.get(3), answer4);
        faqData.put(faqQuestions.get(4), answer5);
        faqData.put(faqQuestions.get(5), answer6);
        faqData.put(faqQuestions.get(6), answer7);
        faqData.put(faqQuestions.get(7), answer8);
        faqData.put(faqQuestions.get(8), answer9);
        faqData.put(faqQuestions.get(9), answer10);
        faqData.put(faqQuestions.get(10), answer11);
        faqData.put(faqQuestions.get(11), answer12);
        faqData.put(faqQuestions.get(12), answer13);
        faqData.put(faqQuestions.get(13), answer14);
    }
}
