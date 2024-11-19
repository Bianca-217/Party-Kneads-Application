package com.ignacio.partykneadsapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.ProductShopModel;

import java.util.List;

public class ProductShopAdapter extends RecyclerView.Adapter<ProductShopAdapter.ProductViewHolder> {

    private List<ProductShopModel> productList;
    private Context context;

    public ProductShopAdapter(Context context, List<ProductShopModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.products_shop, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ProductShopModel currentProduct = productList.get(position);

        // Bind data to the views
        holder.productName.setText(currentProduct.getName());
        holder.itemPrice.setText("₱ " + currentProduct.getPrice());

        // Load product image using Glide
        Glide.with(holder.itemView.getContext())
                .load(currentProduct.getimageUrl())
                .into(holder.productImage);

        // Handle Like Button click
        holder.btnLike.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser().getUid();  // Get user's UID (not email)

            if (userId != null) {
                // Reference to the "Users" collection and the specific user's document
                db.collection("Users")
                        .document(userId)               // Use user UID as the document ID
                        .collection("Likes")             // Create the "Likes" sub-collection inside the user's document
                        .document(currentProduct.getId())  // Use the product ID as the document ID
                        .set(currentProduct)             // Store the product details in that document
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(v.getContext(), "Liked " + currentProduct.getName(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(v.getContext(), "Failed to like the product", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(v.getContext(), "Please sign in to like products", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle item click (navigate to product details)
        holder.itemView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("detailed", productList.get(position)); // Pass the product details

            String category = currentProduct.getCategory();
            if ("Cakes".equals(category)) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homePageFragment_to_cake_Description, bundle);
            } else if ("Dessert - Cupcakes".equals(category) || "Dessert - Cookies".equals(category) || "Dessert - Donuts".equals(category)) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homePageFragment_to_cupcake_Description, bundle);
            } else if ("Balloons - Classic".equals(category) || "Balloons - Latex".equals(category) || "Balloons - LED".equals(category) || "Balloons - Number".equals(category) || "Balloons - Letter".equals(category)) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homePageFragment_to_balloonClassicDescription, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, itemPrice;
        Button btnLike;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            btnLike = itemView.findViewById(R.id.btnLike); // Initialize Like button
        }
    }

    public void updateData(List<ProductShopModel> newProductsList) {
        this.productList.clear();
        this.productList.addAll(newProductsList);
        notifyDataSetChanged();
    }
}
