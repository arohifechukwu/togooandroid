//package com.example.togoo.adapters;
//
//import android.content.Context;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.togoo.CheckoutActivity;
//import com.example.togoo.R;
//import com.example.togoo.models.CartItem;
//import com.example.togoo.models.FoodItem;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
//
//    private Context context;
//    private List<FoodItem> foodList;
//    private OnFoodClickListener listener;
//
//    public interface OnFoodClickListener {
//        void onFoodClick(FoodItem foodItem);
//    }
//
//    public FoodAdapter(Context context, List<FoodItem> foodList, OnFoodClickListener listener) {
//        this.context = context;
//        this.foodList = foodList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        FoodItem food = foodList.get(position);
//
//        // Display data – using food.getId() as the displayed name
//        holder.foodName.setText(food.getId());
//        holder.foodDescription.setText(food.getDescription());
//        holder.foodPrice.setText("$" + food.getPrice());
//
//        Glide.with(context)
//                .load(food.getImageURL() != null ? food.getImageURL() : R.drawable.ic_food_placeholder)
//                .into(holder.foodImage);
//
//        // Item click listener
//        holder.itemView.setOnClickListener(v -> listener.onFoodClick(food));
//
//        // Add to Cart button
//        holder.btnAddToCart.setOnClickListener(v -> addToCart(food));
//
//        // Buy Now button – immediately move item to CheckoutActivity
//        holder.btnBuy.setOnClickListener(v -> buyNow(food));
//    }
//
//    @Override
//    public int getItemCount() {
//        return foodList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView foodImage, btnAddToCart, btnBuy;
//        TextView foodName, foodDescription, foodPrice;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            foodImage = itemView.findViewById(R.id.foodImage);
//            foodName = itemView.findViewById(R.id.foodName);
//            foodDescription = itemView.findViewById(R.id.foodDescription);
//            foodPrice = itemView.findViewById(R.id.foodPrice);
//            btnAddToCart = itemView.findViewById(R.id.addToCart);
//            btnBuy = itemView.findViewById(R.id.btnBuy);
//        }
//    }
//
//
//    public void updateData(List<FoodItem> newData) {
//        this.foodList.clear();
//        this.foodList.addAll(newData);
//        notifyDataSetChanged();
//    }
//
//
//    // Add Food Item to Firebase Cart
//    // 🔹 Add Food Item to Firebase Cart using push()
//    private void addToCart(FoodItem food) {
//        DatabaseReference cartRef = FirebaseDatabase.getInstance()
//                .getReference("cart")
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//
//        // Create a new CartItem (quantity initially 1)
//        CartItem cartItem = new CartItem(food.getId(), food.getDescription(), food.getImageURL(), food.getPrice(), 1);
//
//        // Use push() to add a new entry so each duplicate gets its own unique key
//        DatabaseReference newItemRef = cartRef.push();
//        newItemRef.setValue(cartItem).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Failed to Add to Cart", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // Buy Now: move the item directly to CheckoutActivity
//    private void buyNow(FoodItem food) {
//        List<CartItem> checkoutItems = new ArrayList<>();
//        checkoutItems.add(new CartItem(food.getId(), food.getDescription(), food.getImageURL(), food.getPrice(), 1));
//
//        Intent intent = new Intent(context, CheckoutActivity.class);
//        intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(checkoutItems));
//        context.startActivity(intent);
//    }
//}




package com.example.togoo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.togoo.CheckoutActivity;
import com.example.togoo.R;
import com.example.togoo.models.CartItem;
import com.example.togoo.models.FoodItem;
import com.example.togoo.models.Restaurant;
import com.example.togoo.utils.RestaurantHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private Context context;
    private List<FoodItem> foodList;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem foodItem);
    }

    public FoodAdapter(Context context, List<FoodItem> foodList, Restaurant unusedRestaurant, OnFoodClickListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem food = foodList.get(position);

        holder.foodName.setText(food.getId());
        holder.foodDescription.setText(food.getDescription());
        holder.foodPrice.setText("$" + food.getPrice());

        Glide.with(context)
                .load(food.getImageURL() != null ? food.getImageURL() : R.drawable.ic_food_placeholder)
                .into(holder.foodImage);

        holder.itemView.setOnClickListener(v -> listener.onFoodClick(food));
        holder.btnAddToCart.setOnClickListener(v -> addToCart(food));
        holder.btnBuy.setOnClickListener(v -> buyNow(food));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage, btnAddToCart, btnBuy;
        TextView foodName, foodDescription, foodPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            btnAddToCart = itemView.findViewById(R.id.addToCart);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }

    public void updateData(List<FoodItem> newData) {
        this.foodList.clear();
        this.foodList.addAll(newData);
        notifyDataSetChanged();
    }

    private void addToCart(FoodItem food) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        CartItem cartItem = new CartItem(food.getId(), food.getDescription(), food.getImageURL(), food.getPrice(), 1);

        DatabaseReference newItemRef = cartRef.push();
        newItemRef.setValue(cartItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to Add to Cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buyNow(FoodItem food) {
        List<CartItem> checkoutItems = new ArrayList<>();
        checkoutItems.add(new CartItem(food.getId(), food.getDescription(), food.getImageURL(), food.getPrice(), 1));

        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(checkoutItems));
        intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
        context.startActivity(intent);
    }
}
