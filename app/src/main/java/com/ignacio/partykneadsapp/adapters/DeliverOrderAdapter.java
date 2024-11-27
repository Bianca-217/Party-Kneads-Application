package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.DeliverOrderModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DeliverOrderAdapter extends RecyclerView.Adapter<DeliverOrderAdapter.DeliverOrderViewHolder> {

    private Context context;
    private List<DeliverOrderModel> ordersList;

    // Constructor
    public DeliverOrderAdapter(Context context, List<DeliverOrderModel> ordersList) {
        this.context = context;
        this.ordersList = ordersList != null ? ordersList : new ArrayList<>();
    }

    @NonNull
    @Override
    public DeliverOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the deliver order item layout
        View view = LayoutInflater.from(context).inflate(R.layout.deliver_orders_items, parent, false);
        return new DeliverOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliverOrderViewHolder holder, int position) {
        DeliverOrderModel order = ordersList.get(position);

        // Bind data to the views
        holder.txtUserName.setText(order.getUserName());
        holder.contactNum.setText(order.getContactNum());
        holder.location.setText(order.getLocation());
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());

        // Use Glide to load the cake image
        Glide.with(context)
                .load(order.getImageURL())
                .into(holder.cakeImage);

        // Button click listener (e.g., "Deliver" action)
        holder.btnDeliverOrder.setOnClickListener(v -> {
            // Handle order delivery action
            // You can call an update function here to change the order status or trigger the delivery process
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    // ViewHolder class for binding deliver order data to views
    public static class DeliverOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, contactNum, location, productName, cakeSize, quantity, totalPrice;
        ImageView cakeImage;
        Button btnDeliverOrder;

        public DeliverOrderViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            txtUserName = itemView.findViewById(R.id.txtUserName);
            contactNum = itemView.findViewById(R.id.contactNum);
            location = itemView.findViewById(R.id.location);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            btnDeliverOrder = itemView.findViewById(R.id.btnDeliverOrder);
        }
    }
}
