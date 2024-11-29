package com.ignacio.partykneadsapp.adapters;


import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.PopularModel;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    private Context context;
    private List<PopularModel> products;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Constructor for PopularAdapter
    public PopularAdapter(Context context, List<PopularModel> products) {
        this.context = context;
        this.products = products;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopularModel product = products.get(position);

        // Set text for product details
        holder.productName.setText(product.getName());
        holder.itemPrice.setText(product.getPrice());
        holder.itemSold.setText(product.getSold());

        // Use Glide to load the image from the URL
        Glide.with(context)
                .load(product.getImageUrl()) // Load the image URL from the PopularModel
                .placeholder(R.drawable.placeholder) // Placeholder image while loading
                .error(R.drawable.placeholder) // Fallback image on error
                .into(holder.productImage);

        // Set up the "Like" button state based on whether the user has liked the product
        setupLikeButton(holder, product);
    }

    private void setupLikeButton(ViewHolder holder, PopularModel product) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // If the user is logged in, check if the product is liked
        if (userId != null) {
            db.collection("Users")
                    .document(userId)
                    .collection("Likes")
                    .document(product.getName())  // Assuming product name is the unique identifier for likes
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Product is liked, set the filled heart icon
                            holder.btnLike.setBackgroundResource(R.drawable.pink_heart_filled);
                        } else {
                            // Product is not liked, set the outline heart icon
                            holder.btnLike.setBackgroundResource(R.drawable.heart_pink);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors (e.g., failed to check if liked)
                        Log.e("Firestore", "Error checking like status: " + e.getMessage());
                    });
        } else {
            // For guests, show an unfilled heart by default
            holder.btnLike.setBackgroundResource(R.drawable.heart_pink);
        }

        // Handle the like button click
        holder.btnLike.setOnClickListener(v -> {
            if (userId != null) {
                toggleLikeStatus(userId, product, holder);
            } else {
                Toast.makeText(v.getContext(), "Please sign in to like products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLikeStatus(String userId, PopularModel product, ViewHolder holder) {
        db.collection("Users")
                .document(userId)
                .collection("Likes")
                .document(product.getName())  // Using product name as the document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Product is already liked, so remove it from likes
                        db.collection("Users")
                                .document(userId)
                                .collection("Likes")
                                .document(product.getName())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Change the heart icon to outline
                                    holder.btnLike.setBackgroundResource(R.drawable.heart_pink);
                                    Toast.makeText(holder.itemView.getContext(), "Item removed from favorites", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "Failed to remove like", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Product is not liked, so add it to likes
                        db.collection("Users")
                                .document(userId)
                                .collection("Likes")
                                .document(product.getName())
                                .set(product)  // Store the product details in the "Likes" collection
                                .addOnSuccessListener(aVoid -> {
                                    // Change the heart icon to filled
                                    holder.btnLike.setBackgroundResource(R.drawable.pink_heart_filled);
                                    Toast.makeText(holder.itemView.getContext(), "Item added to favorites", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "Failed to like the product", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // ViewHolder class to hold product item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView itemPrice;
        TextView itemSold;
        ImageView btnLike; // Like button (heart icon)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemSold = itemView.findViewById(R.id.itemSold);
            btnLike = itemView.findViewById(R.id.btnLike);  // Initialize the Like button
        }
    }
}
