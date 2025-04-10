package com.example.togoo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        Log.d("FoodAdapter", "FoodItem: id=" + food.getId() + ", desc=" + food.getDescription() +
                ", image=" + food.getImageURL() + ", price=" + food.getPrice() +
                ", restaurantId=" + food.getRestaurantId());

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to add items to cart.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(food.getRestaurantId());
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                if (restaurant != null) {
                    RestaurantHelper.setCurrentRestaurant(restaurant);
                    DatabaseReference cartRef = FirebaseDatabase.getInstance()
                            .getReference("cart")
                            .child(currentUser.getUid());

                    CartItem cartItem = new CartItem(
                            food.getId(),
                            food.getDescription(),
                            food.getImageURL(),
                            restaurant.getId(), // Ensure this is not null
                            food.getPrice(),
                            1
                    );
                    Log.d("AddToCart", "CartItem: id=" + cartItem.getFoodId() +
                            ", desc=" + cartItem.getFoodDescription() +
                            ", image=" + cartItem.getFoodImage() +
                            ", price=" + cartItem.getFoodPrice() +
                            ", restaurantId=" + cartItem.getRestaurantId());

                    cartRef.push().setValue(cartItem).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to Add to Cart", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Restaurant info missing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load restaurant data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void buyNow(FoodItem food) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to buy items.", Toast.LENGTH_SHORT).show();
            return;
        }

        String restaurantId = food.getRestaurantId();
        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(context, "Invalid restaurant ID for this item.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantId);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                if (restaurant != null) {
                    if (restaurant.getId() == null || restaurant.getId().isEmpty()) {
                        restaurant.setId(snapshot.getKey());
                    }
                    RestaurantHelper.setCurrentRestaurant(restaurant);
                    List<CartItem> checkoutItems = new ArrayList<>();
                    CartItem cartItem = new CartItem(
                            food.getId(),
                            food.getDescription(),
                            food.getImageURL(),
                            restaurant.getId(),
                            food.getPrice(),
                            1
                    );
                    checkoutItems.add(cartItem);
                    Log.d("BuyNow", "CartItem: id=" + cartItem.getFoodId() +
                            ", desc=" + cartItem.getFoodDescription() +
                            ", image=" + cartItem.getFoodImage() +
                            ", price=" + cartItem.getFoodPrice() +
                            ", restaurantId=" + cartItem.getRestaurantId());

                    Intent intent = new Intent(context, CheckoutActivity.class);
                    intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(checkoutItems));
                    intent.putExtra("selectedRestaurant", restaurant);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Restaurant info missing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load restaurant data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
