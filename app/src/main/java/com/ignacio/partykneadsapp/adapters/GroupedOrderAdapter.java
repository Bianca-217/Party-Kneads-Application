package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.GroupedOrder;

import java.util.List;

public class GroupedOrderAdapter extends RecyclerView.Adapter<GroupedOrderAdapter.GroupedOrderViewHolder> {
    private List<GroupedOrder> groupedOrders;

    public GroupedOrderAdapter(List<GroupedOrder> groupedOrders) {
        this.groupedOrders = groupedOrders;
    }

    @NonNull
    @Override
    public GroupedOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_orders_items, parent, false);
        return new GroupedOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupedOrderViewHolder holder, int position) {
        GroupedOrder groupedOrder = groupedOrders.get(position);

        // Set user info
        holder.txtUserName.setText(groupedOrder.getUserName());
        holder.contactNum.setText(groupedOrder.getContactNum());
        holder.location.setText(groupedOrder.getLocation());

        // Setup inner RecyclerView
        InnerOrderAdapter innerAdapter = new InnerOrderAdapter(groupedOrder.getOrderItems());
        holder.toShipRecycler.setAdapter(innerAdapter);

        holder.btnAcceptOrder.setOnClickListener(v -> {
            // Handle accept order action
            // You can implement the logic for what happens when the order is accepted
        });
    }

    @Override
    public int getItemCount() {
        return groupedOrders.size();
    }

    public static class GroupedOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, contactNum, location;
        RecyclerView toShipRecycler;
        Button btnAcceptOrder;

        public GroupedOrderViewHolder(View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            contactNum = itemView.findViewById(R.id.contactNum);
            location = itemView.findViewById(R.id.location);
            toShipRecycler = itemView.findViewById(R.id.toShipRecycler);
            btnAcceptOrder = itemView.findViewById(R.id.btnAcceptOrder);
        }
    }
}
