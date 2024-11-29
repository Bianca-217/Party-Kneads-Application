package com.ignacio.partykneadsapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.ToShipModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ToShipModel> orderList;
    private Context context;

    // Constants for view types
    private static final int VIEW_TYPE_PENDING = 0;
    private static final int VIEW_TYPE_CANCELED = 1;

    // Constructor to initialize the order list and context
    public PendingItemAdapter(List<ToShipModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        // Check the status and return the appropriate view type
        if (orderList.get(position).getStatus().equals("Cancelled")) {
            return VIEW_TYPE_CANCELED;
        } else {
            return VIEW_TYPE_PENDING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PENDING) {
            // Inflate the layout for pending orders
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topending_items, parent, false);
            return new OrderViewHolder(view);
        }
        // If the viewType is not VIEW_TYPE_PENDING, you need to return a ViewHolder for that type
        // For example, if you have another view type, handle it here and return the appropriate ViewHolder
        // For now, just return null or handle other cases appropriately.
        return new RecyclerView.ViewHolder(new View(parent.getContext())) {};  // Placeholder for other view types
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ToShipModel order = orderList.get(position);

        if (holder instanceof OrderViewHolder) {
            // Bind data for pending order
            OrderViewHolder orderViewHolder = (OrderViewHolder) holder;
            orderViewHolder.productName.setText(order.getProductName());
            orderViewHolder.cakeSize.setText(order.getCakeSize());
            orderViewHolder.quantity.setText(String.valueOf(order.getQuantity()));
            orderViewHolder.totalPrice.setText(order.getTotalPrice());

            // Load image using Glide
            Glide.with(context)
                    .load(order.getImageUrl())
                    .into(orderViewHolder.cakeImage);

            // Set the status text
            orderViewHolder.orderStatus.setText(order.getStatus());

            // Handle item click to open dialog
            orderViewHolder.itemView.setOnClickListener(v -> showOrderDetailsDialog(order.getReferenceId()));

            // Set up the Cancel Order button
            orderViewHolder.cancelOrderButton.setOnClickListener(v -> showCancelDialog(order.getReferenceId(), position));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // ViewHolder class for pending orders
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, cakeSize, quantity, totalPrice, orderStatus;
        ImageView cakeImage;
        Button cancelOrderButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            cancelOrderButton = itemView.findViewById(R.id.btnCancelOrder);
        }
    }

    private void showCancelDialog(String referenceId, int position) {
        // Create the Dialog
        Dialog cancelDialog = new Dialog(context);
        cancelDialog.setContentView(R.layout.cancel_dialog_customer); // Your custom cancel dialog layout

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

        // Initialize the RadioGroup and Confirm Button
        RadioGroup radioGroupCancelReasons = cancelDialog.findViewById(R.id.radioGroupCancelReasons);
        Button btnConfirmCancel = cancelDialog.findViewById(R.id.btnConfirmCancel);

        // Confirm cancellation button logic
        btnConfirmCancel.setOnClickListener(v -> {
            // Get the selected RadioButton's ID
            int selectedId = radioGroupCancelReasons.getCheckedRadioButtonId();
            if (selectedId != -1) { // Ensure a RadioButton is selected
                RadioButton selectedRadioButton = cancelDialog.findViewById(selectedId);
                String cancellationReason = selectedRadioButton.getText().toString();

                // Proceed with cancellation
                proceedWithCancellation(referenceId, cancellationReason, cancelDialog, position);

                // Close the dialog
                cancelDialog.dismiss();
            } else {
                // Show a toast message prompting the user to select a reason
                Toast.makeText(context, "Please select a cancellation reason before proceeding.", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        cancelDialog.show();
    }




    // Method to handle the cancellation process and update Firestore
    private void proceedWithCancellation(String referenceId, String cancellationReason, Dialog dialog, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming user email is used to find the user document (can be replaced with appropriate logic)
        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com") // Example user email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDocument = querySnapshot.getDocuments().get(0);

                        // Update the order status and cancellation reason in Firestore
                        db.collection("Users")
                                .document(userDocument.getId())
                                .collection("Orders")
                                .document(referenceId)
                                .update("status", "Cancelled", "cancellationReason", cancellationReason)
                                .addOnSuccessListener(aVoid -> {
                                    // Show success Toast
                                    Toast.makeText(context, "Order has been canceled for reason: " + cancellationReason, Toast.LENGTH_SHORT).show();

                                    // Remove the canceled order from the list
                                    removeCanceledOrder(referenceId, position);

                                    // Dismiss the dialog after successful cancellation
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    // Show failure Toast
                                    Toast.makeText(context, "Failed to cancel order", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Show error Toast if user is not found
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to remove the canceled order from the list
    private void removeCanceledOrder(String referenceId, int position) {
        int pos = findOrderPosition(referenceId);
        if (pos != -1) {
            // Update the order status to "Cancelled"
            ToShipModel canceledOrder = orderList.get(pos);
            canceledOrder.setStatus("Cancelled");
            canceledOrder.setCancellationReason("Reason for cancellation"); // Optional: set reason

            // Remove the canceled order from the list
            orderList.remove(pos);
            orderList.add(pos, canceledOrder);  // This ensures RecyclerView gets updated
            notifyItemChanged(pos);
        }
    }

    // Helper method to find the position of the order in the list
    private int findOrderPosition(String referenceId) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getReferenceId().equals(referenceId)) {
                return i;
            }
        }
        return -1;  // Return -1 if not found
    }

    private void showOrderDetailsDialog(String referenceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the 'Users' collection for the document with the specified email
        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the user document
                        DocumentSnapshot userDocument = querySnapshot.getDocuments().get(0);

                        // Access the Orders collection and the specific order by referenceId
                        db.collection("Users")
                                .document(userDocument.getId())
                                .collection("Orders")
                                .document(referenceId)
                                .get()
                                .addOnSuccessListener(orderSnapshot -> {
                                    if (orderSnapshot.exists()) {
                                        // Retrieve the 'items' field from the order document
                                        List<Map<String, Object>> items = (List<Map<String, Object>>) orderSnapshot.get("items");

                                        // Initialize the dialog
                                        Dialog dialog = new Dialog(context);
                                        dialog.setContentView(R.layout.view_order_details);

                                        // Set the dialog background to transparent
                                        if (dialog.getWindow() != null) {
                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        }

                                        // Set margin of 20dp horizontally and center the dialog
                                        Window window = dialog.getWindow();
                                        if (window != null) {
                                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                                            layoutParams.copyFrom(window.getAttributes());

                                            // Convert dp to pixels
                                            int margin = (int) (20 * context.getResources().getDisplayMetrics().density);

                                            // Set width to match parent with 20dp margin on each side
                                            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels - margin * 2); // 20dp margin on each side
                                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Height to wrap content

                                            // Set the gravity to center the dialog
                                            layoutParams.gravity = Gravity.CENTER;

                                            // Apply the layout parameters
                                            window.setAttributes(layoutParams);
                                        }

                                        // Get references to the TextViews in the dialog
                                        TextView orderIdTextView = dialog.findViewById(R.id.OrderID);
                                        TextView itemTotalTextView = dialog.findViewById(R.id.itemTotal);
                                        TextView totalCosttTextView = dialog.findViewById(R.id.totalCost);
                                        RecyclerView productsRecyclerView = dialog.findViewById(R.id.recyclerViewCart);

                                        productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                                        // Adapter for the RecyclerView
                                        List<OrderItemModel> productList = new ArrayList<>();
                                        OrderItemsAdapter adapter = new OrderItemsAdapter(productList, context);
                                        productsRecyclerView.setAdapter(adapter);

                                        // Set the OrderID TextView to the referenceId
                                        orderIdTextView.setText(referenceId);

                                        // Variable to hold the total price sum
                                        double totalPrice = 0;

                                        // Check if 'items' is null or empty
                                        if (items == null || items.isEmpty()) {
                                            Log.d("OrderDetailsDialog", "No items found or empty items list.");
                                            Toast.makeText(context, "No items found in the order.", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss(); // Close dialog if no items
                                        } else {
                                            // Iterate through items and calculate the total price
                                            for (Map<String, Object> item : items) {
                                                String productName = (String) item.get("productName");
                                                String cakeSize = (String) item.get("cakeSize");
                                                long quantity = item.containsKey("quantity") ? (long) item.get("quantity") : 0;
                                                String imageUrl = (String) item.get("imageUrl");
                                                String price = (String) item.get("totalPrice");

                                                // Add item to the list if the required fields are not null
                                                if (productName != null && price != null) {
                                                    productList.add(new OrderItemModel(productName, cakeSize, imageUrl, (int) quantity, price));

                                                    // Sum the totalPrice (assuming the price is stored with the "₱" symbol)
                                                    try {
                                                        String priceWithoutSymbol = price.replace("₱", "").trim();  // Remove "₱" symbol and spaces
                                                        double itemPrice = Double.parseDouble(priceWithoutSymbol);  // Convert to double
                                                        totalPrice += itemPrice * quantity;  // Multiply by quantity and add to totalPrice
                                                    } catch (NumberFormatException e) {
                                                        Log.e("ParseError", "Invalid price format: " + price);
                                                    }
                                                }
                                            }

                                            // Notify adapter of the new data
                                            adapter.notifyDataSetChanged();

                                            // Set the total price to the itemTotal TextView
                                            itemTotalTextView.setText("₱" + String.format("%.2f", totalPrice)); // Format to 2 decimal places
                                            totalCosttTextView.setText(itemTotalTextView.getText());
                                        }

                                        // Show the dialog after all data is set
                                        dialog.show();
                                    } else {
                                        Log.d("OrderDetailsDialog", "Order document not found.");
                                        Toast.makeText(context, "Order not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore Error", e.getMessage());
                                    Toast.makeText(context, "Failed to fetch order details.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.d("OrderDetailsDialog", "User document not found.");
                        Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    Toast.makeText(context, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                });
    }
}
