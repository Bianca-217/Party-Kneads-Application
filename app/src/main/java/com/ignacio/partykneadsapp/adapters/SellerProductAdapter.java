package com.ignacio.partykneadsapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.Cake_Description;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.model.SellerProductModel;

import java.util.List;

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.ProductViewHolder> {

    private List<SellerProductModel> productList;
    private Context context; // Add context reference

    public SellerProductAdapter(Context context, List<SellerProductModel> productList) {
        this.context = context; // Initialize context
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
        SellerProductModel currentProduct = productList.get(position);

        // Bind data to the views
        holder.productName.setText(currentProduct.getName());
        holder.itemPrice.setText("₱ " + currentProduct.getPrice());

        // Load product image using Glide
        Glide.with(holder.itemView.getContext())
                .load(currentProduct.getimageUrl())
                .into(holder.productImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new instance of the appropriate fragment based on the category

                Bundle bundle = new Bundle();
                bundle.putSerializable("detailed", productList.get(position)); // Pass the product details

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_myProductFragment_to_sellerCakeDescription, bundle);


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

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
        }
    }

    public void updateData(List<SellerProductModel> newProductsList) {
        this.productList.clear();
        this.productList.addAll(newProductsList);
        notifyDataSetChanged();
    }
}
