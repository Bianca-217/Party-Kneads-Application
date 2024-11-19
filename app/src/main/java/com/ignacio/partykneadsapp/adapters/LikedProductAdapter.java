package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.LikedProductModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LikedProductAdapter extends RecyclerView.Adapter<LikedProductAdapter.LikedProductViewHolder> {

    private List<LikedProductModel> likedProducts;
    private OnItemClickListener listener;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public interface OnItemClickListener {
        void onItemClick(LikedProductModel product);
    }

    public LikedProductAdapter(List<LikedProductModel> likedProducts, OnItemClickListener listener) {
        this.likedProducts = likedProducts;
        this.listener = listener;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public LikedProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.likes_products, parent, false);
        return new LikedProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LikedProductViewHolder holder, int position) {
        LikedProductModel product = likedProducts.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("₱ " + product.getPrice());
        holder.itemRating.setText(product.getRating() + " (" + product.getNumReviews() + ")");
        Glide.with(holder.itemView.getContext()).load(product.getImageUrl()).into(holder.productImage);

        // Handle like button visibility
        if (product.isLiked()) {
            holder.btnLike.setBackgroundResource(R.drawable.pink_heart_filled);  // Filled heart icon
        } else {
            holder.btnLike.setBackgroundResource(R.drawable.heart_pink); // Empty heart icon
        }

        // Handle like button click to toggle like/unlike
        holder.btnLike.setOnClickListener(v -> {
            // Show a confirmation dialog if the user clicks on the heart button
            showUnlikeConfirmationDialog(product, position, holder.itemView.getContext());
        });

        // Handle add to cart button action
        holder.btnAddToCart.setOnClickListener(v -> {
            // Pass context to moveProductToCart
            moveProductToCart(product, position, holder.itemView.getContext());
        });

        // Set click listener for product card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });
    }

    private void showUnlikeConfirmationDialog(LikedProductModel product, int position, Context context) {
        try {
            // Ensure the context is not null and is valid
            if (context != null) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Unlike Product")
                        .setMessage("Are you sure you want to unlike this product?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            unlikeProduct(product, position, context);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            } else {
                Log.e("DialogError", "Context is null, unable to show dialog.");
            }
        } catch (Exception e) {
            Log.e("DialogError", "Error showing dialog: ", e);
        }

}

    private void unlikeProduct(LikedProductModel product, int position, Context context) {
        // Get the current user ID
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Ensure the userId is not null
        if (userId == null) {
            // Handle the error (maybe show a toast or log it)
            Toast.makeText(context, "User is not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure product id is not null
        String productId = product.getId(); // Assuming 'id' is the identifier
        if (productId == null) {
            // Handle the error (maybe show a toast or log it)
            Toast.makeText(context, "Product ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed to unlike product if userId and productId are valid
        firestore.collection("Users")
                .document(userId)
                .collection("Likes")
                .document(productId) // Use the product's 'id' field for the document reference
                .delete() // Delete the product from the Likes collection
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed from Likes, now update the UI
                    likedProducts.remove(position);  // Remove product from the displayed list
                    notifyItemRemoved(position);  // Refresh the RecyclerView
                    Toast.makeText(context, "Product unliked and removed from Likes.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure to remove from Likes
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to remove product from Likes.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return likedProducts.size();
    }

    private void moveProductToCart(LikedProductModel product, int position, Context context) {
        // Get the current user ID
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Ensure the userId is not null
        if (userId == null) {
            // Handle the error (maybe show a toast or log it)
            Toast.makeText(context, "User is not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure product id is not null
        String productId = product.getId(); // Assuming 'id' is the identifier
        if (productId == null) {
            // Handle the error (maybe show a toast or log it)
            Toast.makeText(context, "Product ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed to move product to cart if userId and productId are valid
        firestore.collection("Users")
                .document(userId)
                .collection("Likes")
                .document(productId) // Use the product's 'id' field for the document reference
                .delete() // Delete the product from the Likes collection
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed from Likes, now add it to CartItems
                    firestore.collection("Users")
                            .document(userId)
                            .collection("cartItems")
                            .document(productId) // Add product with the same 'id' to CartItems
                            .set(product) // Store the product data in the CartItems collection
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(context, "Product added to cart.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to add to CartItems
                                e.printStackTrace();
                                Toast.makeText(context, "Failed to add product to cart.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure to remove from Likes
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to remove product from Likes.", Toast.LENGTH_SHORT).show();
                });
    }


    public static class LikedProductViewHolder extends RecyclerView.ViewHolder {

        TextView productName, productPrice, itemRating;
        ImageView btnLike;  // Button for the heart
        ImageView productImage;
        Button btnAddToCart;

        public LikedProductViewHolder(View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.itemPrice);
            itemRating = itemView.findViewById(R.id.itemRating);
            productImage = itemView.findViewById(R.id.productImage);
            btnLike = itemView.findViewById(R.id.btnLike);  // This is a Button
            btnAddToCart = itemView.findViewById(R.id.btnaddToCart);
        }
    }



}