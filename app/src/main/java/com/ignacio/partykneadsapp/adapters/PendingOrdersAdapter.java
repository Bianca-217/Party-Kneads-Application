package com.ignacio.partykneadsapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.PendingOrdersViewHolder> {

    private Context context;
    private List<PendingOrdersModel> ordersList;
    private String isDeliverMode;

    public PendingOrdersAdapter(Context context, List<PendingOrdersModel> ordersList) {
        this.context = context;
        this.ordersList = ordersList != null ? ordersList : new ArrayList<>();
    }

    @NonNull
    @Override
    public PendingOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_orders_items, parent, false);
        return new PendingOrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrdersViewHolder holder, int position) {
        PendingOrdersModel order = ordersList.get(position);

        // Bind order details to views
        holder.txtUserName.setText(order.getUserName());
        holder.contactNum.setText(order.getContactNum());
        holder.location.setText(order.getLocation());
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());

        Glide.with(context)
                .load(order.getImageURL())
                .into(holder.cakeImage);

        // Set initial button text based on mode
        holder.btnAcceptOrder.setText("Deliver".equals(isDeliverMode) ? "Deliver" : "Accept");

        holder.btnAcceptOrder.setOnClickListener(v -> {
            String newStatus = holder.btnAcceptOrder.getText().toString().equals("Accept")
                    ? "preparing order" : "Out for Delivery";
            updateOrderStatus(order, newStatus, position);
        });

        // Set OnClickListener to show detailed order dialog
        holder.itemView.setOnClickListener(v -> showOrderDetailsDialog(order));
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class PendingOrdersViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, contactNum, location, productName, cakeSize, quantity, totalPrice;
        ImageView cakeImage;
        Button btnAcceptOrder;

        public PendingOrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            contactNum = itemView.findViewById(R.id.contactNum);
            location = itemView.findViewById(R.id.location);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            btnAcceptOrder = itemView.findViewById(R.id.btnAcceptOrder);
        }
    }

    // Method to update the mode
    public void toggleMode(String status) {
        isDeliverMode = status;
        notifyDataSetChanged();
    }

    // Method to update order status in Firestore
    private void updateOrderStatus(PendingOrdersModel order, String newStatus, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference ordersRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Orders");

        String orderId = order.getOrderId();

        ordersRef.document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    removeItem(position);
                    showToast("Order status updated to " + newStatus);
                })
                .addOnFailureListener(e -> showToast("Failed to update order status"));
    }

    // Helper method to show Toast messages
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }

    // Method to remove item from the list
    private void removeItem(int position) {
        ordersList.remove(position);
        notifyItemRemoved(position);
    }

    // Dialog to show order details
    private void showOrderDetailsDialog(PendingOrdersModel order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Orders")
                .document(order.getOrderId())
                .get()
                .addOnSuccessListener(orderSnapshot -> {
                    if (orderSnapshot.exists()) {
                        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) orderSnapshot.get("items");
                        if (itemsData == null) itemsData = new ArrayList<>();
                        showOrderDetailsDialog(itemsData, order.getOrderId());
                    } else {
                        showToast("Order not found");
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to fetch order details"));
    }

    // Method to show order details in a dialog
    private void showOrderDetailsDialog(List<Map<String, Object>> itemsData, String orderId) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_order_details);

        TextView orderIdTextView = dialog.findViewById(R.id.OrderID);
        TextView itemTotalTextView = dialog.findViewById(R.id.itemTotal);
        RecyclerView productsRecyclerView = dialog.findViewById(R.id.recyclerViewCart);

        productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        List<OrderItemModel> items = new ArrayList<>();
        double totalPrice = 0;

        for (Map<String, Object> itemData : itemsData) {
            OrderItemModel item = new OrderItemModel();
            item.setProductName((String) itemData.get("productName"));
            item.setCakeSize((String) itemData.get("cakeSize"));
            item.setImageUrl((String) itemData.get("imageUrl"));
            item.setQuantity(((Long) itemData.get("quantity")).intValue());
            item.setPrice((String) itemData.get("totalPrice"));

            items.add(item);

            String price = item.getPrice();
            if (price != null && !price.isEmpty()) {
                try {
                    double itemPrice = Double.parseDouble(price.replace("₱", "").trim());
                    totalPrice += itemPrice * item.getQuantity();
                } catch (NumberFormatException ignored) {}
            }
        }

        orderIdTextView.setText(orderId);
        itemTotalTextView.setText("₱" + String.format("%.2f", totalPrice));

        productsRecyclerView.setAdapter(new OrderItemsAdapter(items, context));

        dialog.show();
    }
}
