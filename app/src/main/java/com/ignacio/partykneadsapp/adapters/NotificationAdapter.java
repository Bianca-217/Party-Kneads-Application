package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<CartItemModel> itemsList;

    public NotificationAdapter(List<CartItemModel> itemsList) {
        this.itemsList = itemsList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.toshipitems, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        CartItemModel item = itemsList.get(position);

        // Set the values in the toshipitems.xml layout
        holder.productName.setText(item.getProductName());
        holder.cakeSize.setText(item.getCakeSize());
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.totalPrice.setText("₱" + item.getTotalPrice());
        // You can also load the image dynamically using an image loading library like Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .into(holder.cakeImage);

        // Set the status (this can be dynamic depending on the order status)
        holder.txtStatus.setText("Seller is preparing your order.");
    }

    @Override
    public int getItemCount() {
        return itemsList != null ? itemsList.size() : 0;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView cakeImage;
        TextView productName;
        TextView cakeSize;
        TextView quantity;
        TextView totalPrice;
        TextView txtStatus;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}

