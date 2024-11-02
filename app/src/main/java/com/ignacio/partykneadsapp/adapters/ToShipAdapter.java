package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.List;

public class ToShipAdapter extends RecyclerView.Adapter<ToShipAdapter.OrderViewHolder> {
    private List<ToShipModel> orderList;
    private Context context;

    // Constructor to initialize the order list and context
    public ToShipAdapter(List<ToShipModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.toshipitems, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        ToShipModel order = orderList.get(position);
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());

        // Load image using Glide
        Glide.with(context)
                .load(order.getImageUrl())
                .into(holder.cakeImage);

        // Set the status text to "Waiting for seller to confirm"
        holder.orderStatus.setText(order.getStatus()); // Set the status from ToShipModel
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, cakeSize, quantity, totalPrice, orderStatus; // Added orderStatus
        ImageView cakeImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            orderStatus = itemView.findViewById(R.id.txtStatus); // Initialize orderStatus
        }
    }
}
