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


        private void updateQuantityAndPrice(CartItemModel item, int newQuantity) {
            fetchProductPrice(item.getProductId(), price -> {
                double priceValue = Double.parseDouble(price);
                double newTotalPrice = priceValue * newQuantity;

                // Format total price based on whether it has decimal places or not
                String formattedPrice;
                if (newTotalPrice == (long) newTotalPrice) {
                    // If price is a whole number, show it as an integer
                    formattedPrice = "₱" + (long) newTotalPrice;
                } else {
                    // If price has decimals, show it with 2 decimal places
                    formattedPrice = "₱" + String.format("%.2f", newTotalPrice);
                }

                // Update model directly
                item.setQuantity(newQuantity);
                item.setTotalPrice(formattedPrice);

                // Update only the necessary views (quantity and totalPrice)
                quantity.setText(String.valueOf(newQuantity));
                totalPrice.setText(formattedPrice);

                // Update the cartItems list dynamically
                cartItems.set(getAdapterPosition(), item);

                // Save the updated cart item to Firestore
                updateCartItemInFirestore(item);

                // Notify listener to update the total price of the cart
                onItemSelectedListener.onItemSelected();
            });
        }
    }
}
