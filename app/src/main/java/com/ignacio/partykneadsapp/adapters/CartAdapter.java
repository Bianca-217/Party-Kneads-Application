package com.ignacio.partykneadsapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.CartItemModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItemModel> cartItems;
    private HashMap<Integer, Boolean> selectedItems = new HashMap<>();
    private OnItemSelectedListener onItemSelectedListener;

    // Interface for item selection change
    public interface OnItemSelectedListener {
        void onItemSelected();
    }

    // Callback interface for fetching stock
    public interface OnStockFetchedListener {
        void onStockFetched(int stock);
    }

    // Callback interface for fetching product category
    public interface OnCategoryFetchedListener {
        void onCategoryFetched(String category);
    }

    public CartAdapter(List<CartItemModel> cartItems, OnItemSelectedListener listener) {
        this.cartItems = cartItems;
        this.onItemSelectedListener = listener;
        for (int i = 0; i < cartItems.size(); i++) {
            selectedItems.put(i, false); // Initialize all items as unchecked
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemModel item = cartItems.get(position);
        holder.bind(item, position);

        // Set checkbox state
        holder.checkBox.setChecked(selectedItems.getOrDefault(position, false));

        // Checkbox listener
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedItems.put(position, isChecked); // Update selection state
            onItemSelectedListener.onItemSelected(); // Notify the listener to update total price
        });

        // Delete button listener
        holder.btnDelete.setOnClickListener(v -> {
            String docId = item.getDocId(); // Get the Firestore document ID
            removeItemFromFirestore(docId, position, v); // Pass the document ID as a parameter
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void selectAll(boolean isSelected) {
        for (int i = 0; i < cartItems.size(); i++) {
            selectedItems.put(i, isSelected); // Update selection state for all items
        }
        notifyDataSetChanged(); // Notify adapter of changes
    }

    public List<CartItemModel> getSelectedItems() {
        List<CartItemModel> selected = new ArrayList<>();
        for (int i = 0; i < cartItems.size(); i++) {
            if (selectedItems.getOrDefault(i, false)) {
                selected.add(cartItems.get(i));
            }
        }
        return selected;
    }

    private void removeItemFromFirestore(String docId, int position, View view) {
        if (docId == null || docId.isEmpty()) {
            Toast.makeText(view.getContext(), "Error: Unable to delete item, missing document ID.", Toast.LENGTH_SHORT).show();
            Log.e("CartAdapter", "Cannot delete item at position " + position + " because docId is null or empty.");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference cartItemRef = firestore
                .collection("Users")
                .document(uid)
                .collection("cartItems")
                .document(docId);

        cartItemRef.delete()
                .addOnSuccessListener(aVoid -> {
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItems.size());
                    Toast.makeText(view.getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("CartAdapter", "Error deleting item", e);
                    Toast.makeText(view.getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProductPrice(String productId, OnPriceFetchedListener listener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String price = documentSnapshot.getString("price");
                        listener.onPriceFetched(price);
                    } else {
                        listener.onPriceFetched("0"); // Default if price not found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartAdapter", "Error fetching price for productId: " + productId, e);
                    listener.onPriceFetched("0");
                });
    }

    // Callback interface for fetching prices
    public interface OnPriceFetchedListener {
        void onPriceFetched(String price);
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, quantity, totalPrice, cakeSize, btnIncrease, btnDecrease;
        private ImageView cakeImage;
        private Button btnDelete; // Added buttons for quantity adjustment
        private CheckBox checkBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            checkBox = itemView.findViewById(R.id.checkbox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }

        public void bind(CartItemModel item, int position) {
            productName.setText(item.getProductName());
            quantity.setText(String.valueOf(item.getQuantity()));
            totalPrice.setText(item.getTotalPrice());
            cakeSize.setText(item.getCakeSize());

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .into(cakeImage);

            btnIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                updateQuantityAndPrice(item, newQuantity);
            });

            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    updateQuantityAndPrice(item, newQuantity);
                }
            });
        }


        private void updateCartItemInFirestore(CartItemModel item) {
            // Get Firestore instance
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Reference to the specific cart item document in Firestore
            DocumentReference cartItemRef = firestore
                    .collection("Users")
                    .document(uid)
                    .collection("cartItems")
                    .document(item.getDocId());

            // Create a map of the fields you want to update
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("quantity", item.getQuantity());
            cartItemData.put("totalPrice", item.getTotalPrice());

            // Update the Firestore document with the new values
            cartItemRef.update(cartItemData);
        }

        private void fetchProductStock(String productId, OnStockFetchedListener listener) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("products")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get the stock value
                            Long stockValue = documentSnapshot.getLong("stock");
                            String category = documentSnapshot.getString("categories"); // Fetch the category of the product

                            // Check if the product belongs to an exempt category
                            boolean isExemptCategory = isExemptCategory(category);

                            // If the category is exempt, set stock to Integer.MAX_VALUE (or another value to bypass stock check)
                            if (isExemptCategory) {
                                listener.onStockFetched(Integer.MAX_VALUE); // No quantity limitation
                            } else {
                                // If stock is null, set it to 0 (or handle as needed)
                                int stock = (stockValue != null) ? stockValue.intValue() : 0;
                                listener.onStockFetched(stock);
                            }
                        } else {
                            // If the product doesn't exist in the Firestore document, default to 0 stock
                            listener.onStockFetched(0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CartAdapter", "Error fetching stock for productId: " + productId, e);
                        listener.onStockFetched(0); // Return 0 stock in case of failure
                    });
        }

        private void updateQuantityAndPrice(CartItemModel item, int newQuantity) {
            // Fetch product category first before checking stock
            fetchProductCategory(item.getProductId(), category -> {
                // Check if the item belongs to the exempt categories
                if (isExemptCategory(category)) {
                    // Item belongs to an exempt category, no stock limitation applied
                    applyQuantityChange(item, newQuantity);
                } else {
                    // Fetch stock and apply stock limit logic for non-exempt items
                    fetchProductStock(item.getProductId(), stock -> {
                        if (newQuantity > stock) {
                            // Show a message if quantity exceeds stock
                            Toast.makeText(itemView.getContext(), "Cannot exceed available stock (" + stock + ")", Toast.LENGTH_SHORT).show();
                            return; // Prevent updating if the new quantity exceeds available stock
                        }

                        // Calculate unit price dynamically
                        double unitPrice = item.getTotalPriceAsDouble() / item.getQuantity(); // Derive unit price

                        // Calculate new total price based on the new quantity
                        double newTotalPrice = unitPrice * newQuantity;

                        // Format total price based on whether it has decimal places or not
                        String formattedPrice;
                        if (newTotalPrice == (long) newTotalPrice) {
                            formattedPrice = "₱" + (long) newTotalPrice; // Whole number
                        } else {
                            formattedPrice = "₱" + String.format("%.2f", newTotalPrice); // Decimal format
                        }

                        // Update the cart item
                        item.setQuantity(newQuantity);
                        item.setTotalPrice(formattedPrice);

                        // Update views
                        quantity.setText(String.valueOf(newQuantity));
                        totalPrice.setText(formattedPrice);

                        // Update Firestore
                        updateCartItemInFirestore(item);

                        // Notify listener to update the total price in the cart
                        onItemSelectedListener.onItemSelected();
                    });
                }
            });
        }

        private void applyQuantityChange(CartItemModel item, int newQuantity) {
            // Calculate unit price dynamically
            double unitPrice = item.getTotalPriceAsDouble() / item.getQuantity(); // Derive unit price from the current total price

            // Calculate new total price based on the new quantity
            double newTotalPrice = unitPrice * newQuantity;

            // Format the total price based on whether it has decimal places or not
            String formattedPrice;
            if (newTotalPrice == (long) newTotalPrice) {
                formattedPrice = "₱" + (long) newTotalPrice; // Whole number
            } else {
                formattedPrice = "₱" + String.format("%.2f", newTotalPrice); // Decimal format
            }

            // Update the cart item
            item.setQuantity(newQuantity);
            item.setTotalPrice(formattedPrice);

            // Update the quantity and total price displayed in the view
            quantity.setText(String.valueOf(newQuantity));
            totalPrice.setText(formattedPrice);

            // Update Firestore with the new quantity and total price
            updateCartItemInFirestore(item);

            // Notify listener to update the total price in the cart
            onItemSelectedListener.onItemSelected();
        }


        // Helper method to check if the product belongs to one of the exempt categories
        private boolean isExemptCategory(String category) {
            // Convert category to lowercase for case-insensitive comparison
            category = category.toLowerCase();

            // Check if the category is one of the exempt categories
            return category.equals("dessert - donuts") ||
                    category.equals("dessert - cookies") ||
                    category.equals("dessert - cupcakes") ||
                    category.equals("cakes");
        }

        // Fetch product category from Firestore based on productId
        private void fetchProductCategory(String productId, OnCategoryFetchedListener listener) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("products")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String category = documentSnapshot.getString("categories"); // Assuming "category" is the field name
                            listener.onCategoryFetched(category != null ? category : ""); // Handle null category if not found
                        } else {
                            listener.onCategoryFetched(""); // Default to empty if product not found
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CartAdapter", "Error fetching category for productId: " + productId, e);
                        listener.onCategoryFetched(""); // Default to empty if failed
                    });
        }
    }
}