package com.ignacio.partykneadsapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.NotificationViewModel;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.PendingOrdersViewHolder> {

    private Context context;
    private List<PendingOrdersModel> ordersList;
    private String isDeliverMode;
    private NotificationViewModel notificationViewModel;

    public PendingOrdersAdapter(Context context, List<PendingOrdersModel> ordersList, NotificationViewModel notificationViewModel) {
        this.context = context;
        this.ordersList = ordersList != null ? ordersList : new ArrayList<>();
        this.notificationViewModel = notificationViewModel;
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

        // Set button text based on delivery mode
        holder.btnAcceptOrder.setText("Deliver".equals(isDeliverMode) ? "Deliver" : "Accept");

        holder.btnAcceptOrder.setOnClickListener(v -> {
            String newStatus = holder.btnAcceptOrder.getText().toString().equals("Accept")
                    ? "preparing order" : "Out for Delivery";

            // Update the order status
            updateOrderStatus(order, newStatus, position);

            // Assuming you have a way to retrieve userId based on the order
            String userId = getUserIdFromOrder(order); // You need to define this method

            // Send notification and show corresponding layout
            if ("Accept".equals(holder.btnAcceptOrder.getText().toString())) {
                // When the admin clicks "Accept", show the "toship" layout
                notificationViewModel.sendToShipNotification(order.getOrderId(), userId);
                showOrderDetailsDialog(order, "toshipitems"); // Show toship layout
            } else if ("Deliver".equals(holder.btnAcceptOrder.getText().toString())) {
                // When the admin clicks "Deliver", show the "toreceive" layout
                notificationViewModel.sendToReceiveNotification(order.getOrderId(), userId);
                showOrderDetailsDialog(order, "toreceiveitems"); // Show toreceive layout
            }
        });

        // Set OnClickListener to show detailed order dialog
        holder.itemView.setOnClickListener(v -> showOrderDetailsDialog(order, "toreceive"));
    }

    // This method can retrieve the userId based on the order, you can customize it.
    private String getUserIdFromOrder(PendingOrdersModel order) {
        return "someUserId"; // Replace with actual logic to get the userId
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
                    if ("Accept".equals(newStatus)) {
                        moveOrderToDelivery(order, orderId);
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to update order status"));
    }

    // Helper method to show Toast messages
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    // Method to remove item from the list
    private void removeItem(int position) {
        ordersList.remove(position);
        notifyItemRemoved(position);
    }

    // Method to move order to the Delivery collection and notify the customer
    private void moveOrderToDelivery(PendingOrdersModel order, String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getUserIdFromOrder(order); // Retrieve the userId

        // Fetch the order details
        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(orderSnapshot -> {
                    if (orderSnapshot.exists()) {
                        // Move the order to the Delivery collection
                        db.collection("Users").document(userId)
                                .collection("Delivery")
                                .document(orderId)
                                .set(orderSnapshot.getData())
                                .addOnSuccessListener(aVoid -> {
                                    // Delete the order from the Orders collection
                                    db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Orders")
                                            .document(orderId)
                                            .delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                showToast("Order moved to Delivery!");
                                                sendNotificationToCustomer(orderId, userId); // Send notification
                                            })
                                            .addOnFailureListener(e -> showToast("Failed to delete order from Orders"));
                                })
                                .addOnFailureListener(e -> showToast("Failed to move order to Delivery"));
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to fetch order details"));
    }

    // Function to send notification to the customer
    private void sendNotificationToCustomer(String orderId, String userId) {
        notificationViewModel.sendToShipNotification(orderId, userId); // Notify customer that their order is preparing
    }

    // Modified dialog to show "toship" or "toreceive" layout based on status
    private void showOrderDetailsDialog(PendingOrdersModel order, String layoutType) {
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
                        showOrderDetailsDialog(itemsData, order.getOrderId(), layoutType);
                    } else {
                        showToast("Order not found");
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to fetch order details"));
    }

    // Show order details in the dialog with dynamic layout
    private void showOrderDetailsDialog(List<Map<String, Object>> itemsData, String orderId, String layoutType) {
        Dialog dialog = new Dialog(context);

        // Dynamically choose the layout for the dialog (toship or toreceive)
        String layout = layoutType.equals("toshipitems") ? "toshipitems" : "toreceiveitems";
        int layoutResId = context.getResources().getIdentifier(layout, "layout", context.getPackageName());

        // Check if the layout exists
        if (layoutResId != 0) {
            dialog.setContentView(layoutResId);  // Set the layout resource for the dialog

            // Find the RecyclerView by the correct ID
            RecyclerView notificationRecyclerView = dialog.findViewById(R.id.notificationRecyclerView);

            if (notificationRecyclerView != null) {
                // Convert List<Map<String, Object>> to List<OrderItemModel>
                List<OrderItemModel> items = new ArrayList<>();
                double totalPrice = 0;

                // Iterate through the Firestore data and create OrderItemModels
                for (Map<String, Object> itemData : itemsData) {
                    OrderItemModel item = new OrderItemModel();
                    item.setProductName((String) itemData.get("productName"));
                    item.setCakeSize((String) itemData.get("cakeSize"));
                    item.setImageUrl((String) itemData.get("imageUrl"));
                    item.setQuantity(((Long) itemData.get("quantity")).intValue());
                    item.setPrice((String) itemData.get("totalPrice"));

                    // Calculate the total price
                    String price = item.getPrice();
                    if (price != null && !price.isEmpty()) {
                        try {
                            double itemPrice = Double.parseDouble(price.replace("₱", "").trim());
                            totalPrice += itemPrice * item.getQuantity();
                        } catch (NumberFormatException ignored) {}
                    }

                    items.add(item);
                }

                // Set the RecyclerView layout manager
                notificationRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                // Pass the items to the OrderItemsAdapter
                notificationRecyclerView.setAdapter(new OrderItemsAdapter(items, context));

                // Initialize other views inside the dialog
                TextView orderIdTextView = dialog.findViewById(R.id.OrderID);
                TextView itemTotalTextView = dialog.findViewById(R.id.itemTotal);

                // Set the order ID
                orderIdTextView.setText(orderId);

                // Set the total price
                itemTotalTextView.setText("₱" + String.format("%.2f", totalPrice));

                // Show the dialog
                dialog.show();
            } else {
                Log.e("PendingOrdersAdapter", "RecyclerView not found in the layout");
            }
        } else {
            Log.e("PendingOrdersAdapter", "Layout resource not found: " + layout);
        }
    }
}
