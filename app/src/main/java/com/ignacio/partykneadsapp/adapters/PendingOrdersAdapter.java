package com.ignacio.partykneadsapp.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.customermenus.NotificationFragment;
import com.ignacio.partykneadsapp.model.NotificationViewModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;

import java.util.ArrayList;
import java.util.HashMap;
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



        // Set OnClickListener for cancel button
        holder.btnCancelOrder.setOnClickListener(v -> showCancelDialog(order, position));


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
        Button btnAcceptOrder, btnCancelOrder;

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
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }
    }

    private void showCancelDialog(PendingOrdersModel order, int position) {
        // Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate the custom layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.cancel_dialog_seller, null);
        builder.setView(dialogView);

        // Create the AlertDialog
        AlertDialog cancelDialog = builder.create();

        // Make the background fully transparent
        if (cancelDialog.getWindow() != null) {
            cancelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set dialog width and apply margins programmatically
        Window window = cancelDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels - 40 * context.getResources().getDisplayMetrics().density); // 20dp margin on each side
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        // Initialize the RadioGroup
        RadioGroup radioGroupCancelReasons = dialogView.findViewById(R.id.radioGroupCancelReasons);

        // Confirm cancellation button
        Button btnConfirmCancel = dialogView.findViewById(R.id.btnConfirmCancel);
        btnConfirmCancel.setOnClickListener(v -> {
            String cancellationReason = null;

            // Get the selected RadioButton's ID
            int selectedId = radioGroupCancelReasons.getCheckedRadioButtonId();
            if (selectedId != -1) { // Ensure a RadioButton is selected
                RadioButton selectedRadioButton = dialogView.findViewById(selectedId);
                cancellationReason = selectedRadioButton.getText().toString();
            }

            // Validate that a reason is selected
            if (cancellationReason != null) {
                // Proceed with cancellation
                updateOrderStatusToCancelled(order, cancellationReason, position);
                cancelDialog.dismiss(); // Close the dialog
            } else {
                // Show a toast message prompting the user to select a reason
                Toast.makeText(context, "Please select a cancellation reason before proceeding.", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the AlertDialog
        cancelDialog.show();
    }




    // Method to update the order status to "Cancelled" and save the reason
    private void updateOrderStatusToCancelled(PendingOrdersModel order, String cancellationReason, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference ordersRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Orders");

        String orderId = order.getOrderId();

        // Update the Firestore document for the order to set status as "Cancelled" and save the reason
        ordersRef.document(orderId)
                .update("status", "Cancelled", "reason", cancellationReason)
                .addOnSuccessListener(aVoid -> {
                    // Notify user and update UI after cancellation
                    showToast("Order has been cancelled for reason: " + cancellationReason);
                    removeItem(position); // Remove item from list
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update order status.");
                    Log.e("CancelOrder", "Failed to update order status", e);
                });
    }


    // Method to update the mode
    public void toggleMode(String status) {
        isDeliverMode = status;
        notifyDataSetChanged();
    }

    private void updateOrderStatus(PendingOrdersModel order, String newStatus, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference ordersRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Orders");

        String orderId = order.getOrderId();

        // Step 1: Check if the order exists first
        ordersRef.document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Order exists, proceed with status update
                        updateOrderInFirestore(orderId, newStatus, order, position);
                    } else {
                        Log.w("UpdateOrder", "No such order found: " + orderId);
                        showToast("Order not found: " + orderId);  // Show a toast message to the user
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateOrder", "Error fetching order", e);
                    showToast("Failed to fetch order details");
                });
    }

    private void updateOrderInFirestore(String orderId, String newStatus, PendingOrdersModel order, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference ordersRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Orders");

        // Step 2: Update the order status in Firestore
        ordersRef.document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // After status update, fetch productId and send notification
                    fetchCustomerDetails(order, newStatus, position);
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update order status");
                    Log.e("UpdateOrder", "Failed to update order status", e);
                });
    }

    private void fetchCustomerDetails(PendingOrdersModel order, String newStatus, int position) {
        String productId = order.getOrderId();  // Fetch the productId from the order model

        if (productId == null || productId.isEmpty()) {
            Log.e("UpdateOrder", "Product ID is null or empty. Cannot fetch customer details.");
            showToast("Product ID is missing. Cannot fetch customer details.");
            return;  // Exit early if productId is invalid
        }

        // Step 3: Fetch the order document using productId
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document("QqqccLchjigd0C7zf8ewPXY0KZc2")  // Replace this with the actual admin document ID if needed
                .collection("Orders")
                .whereEqualTo("referenceId", productId)  // Query by productId instead of email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Assuming only one document matches the productId
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);

                        if (documentSnapshot.exists()) {
                            // Step 4: Fetch the customer email from the order document
                            String customerEmail = documentSnapshot.getString("userEmail");

                            if (customerEmail != null && !customerEmail.isEmpty()) {
                                // Proceed with sending the notification to the customer
                                NotificationViewModel notification = createNotification(newStatus, order);
                                saveNotificationToUser(customerEmail, notification);  // Send notification to the customer

                                // Step 5: After notification, remove the item from the list
                                removeItem(position);  // Remove the item after processing the order
                            } else {
                                Log.w("UpdateOrder", "Customer email not found in the order document");
                                showToast("Customer email not found in the order document.");
                            }
                        }
                    } else {
                        Log.w("UpdateOrder", "No orders found with productId: " + productId);
                        showToast("Order document not found with productId: " + productId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateOrder", "Error fetching order document", e);
                });
    }

    private NotificationViewModel createNotification(String newStatus, PendingOrdersModel order) {
        String orderStatus = "";
        String userRateComment = "";

        // Define the notification content based on the new status
        switch (newStatus) {
            case "preparing order":
                orderStatus = "Order Accepted by Seller";
                userRateComment = "Your order has been confirmed by the seller and is currently preparing it for delivery.";
                break;
            case "Out for Delivery":
                orderStatus = "Out for Delivery";
                userRateComment = "Your order is on its way! The seller is delivering your cake.";
                break;
            default:
                orderStatus = "Order Update";
                userRateComment = "Your order status has been updated.";
                break;
        }

        // Optionally, use an image URL from the order or default it
        String imageUrl = order.getImageURL();

        // Return the new notification
        return new NotificationViewModel(orderStatus, userRateComment, imageUrl);
    }

    private void saveNotificationToUser(String email, NotificationViewModel notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 5: Save the notification in the "Notifications" subcollection under the user's email
        db.collection("Users")
                .whereEqualTo("email", email)  // Find the user document by their email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the user's document ID
                        String userDocId = querySnapshot.getDocuments().get(0).getId();

                        // Save the notification under the user's "Notifications" subcollection
                        db.collection("Users")
                                .document(userDocId)  // User's document ID
                                .collection("Notifications")  // Notifications subcollection
                                .add(new HashMap<String, Object>() {{
                                    put("orderStatus", notification.getOrderStatus());
                                    put("userRateComment", notification.getUserRateComment());
                                    put("imageUrl", notification.getCakeImageUrl());
                                    put("timestamp", FieldValue.serverTimestamp());  // Add a timestamp
                                }})
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Notification", "Notification saved successfully for email: " + email);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Notification", "Error saving notification for email: " + email, e);
                                });
                    } else {
                        Log.w("Notification", "User with email not found: " + email);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Error fetching user with email: " + email, e);
                });
    }

    // Method to remove item from the list
    private void removeItem(int position) {
        // Ensure the position is within bounds of the list
        if (position >= 0 && position < ordersList.size()) {
            // Remove the item from the list
            ordersList.remove(position);
            // Notify the adapter that an item was removed
            notifyItemRemoved(position);
        } else {
            // Log an error if the position is invalid
            Log.e("PendingOrdersAdapter", "Invalid position for removal: " + position);
        }
    }

    // Helper method to show Toast messages
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
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

    private void showOrderDetailsDialog(List<Map<String, Object>> itemsData, String orderId) {
        // Create a new Dialog instance
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_order_details);

        // Remove background and make it transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set the dialog window width and add horizontal margin (20dp)
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());

            // Convert dp to pixels
            int margin = (int) (20 * context.getResources().getDisplayMetrics().density);

            // Set width to match parent with 20dp margin on each side
            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels - margin * 2); // 20dp margin on each side
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT; // Height to match parent

            // Set the dialog position to center the window both horizontally and vertically
            layoutParams.gravity = Gravity.CENTER; // Ensures the dialog is centered on the screen

            // Apply the layout parameters
            window.setAttributes(layoutParams);
        }

        // Initialize the views
        TextView orderIdTextView = dialog.findViewById(R.id.OrderID);
        TextView itemTotalTextView = dialog.findViewById(R.id.itemTotal);
        RecyclerView productsRecyclerView = dialog.findViewById(R.id.recyclerViewCart);

        // Set RecyclerView layout manager
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Prepare the list of items and calculate total price
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

        // Set order details
        orderIdTextView.setText(orderId);
        itemTotalTextView.setText("₱" + String.format("%.2f", totalPrice));

        // Set the RecyclerView adapter
        productsRecyclerView.setAdapter(new OrderItemsAdapter(items, context));

        // Show the dialog
        dialog.show();
    }
}