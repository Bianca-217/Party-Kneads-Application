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
            onItemSelectedListener.onItemSelected(); // Notify the listener
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




    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, quantity, totalPrice, ratePercent, numReviews, cakeSize;
        private ImageView cakeImage;
        private Button btnDelete;
        private CheckBox checkBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            ratePercent = itemView.findViewById(R.id.ratePercent);
            numReviews = itemView.findViewById(R.id.numReviews);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            checkBox = itemView.findViewById(R.id.checkbox);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Add delete button reference

        }

        public void bind(CartItemModel item, int position) {
            Log.d("CartAdapter", "Binding item: " + item.getProductName() + ", docId: " + item.getDocId());
            productName.setText(item.getProductName());
            quantity.setText(String.valueOf(item.getQuantity()));
            totalPrice.setText(item.getTotalPrice()); // Updated method call
//            ratePercent.setText(item.getRate() != null ? item.getRate() : "test");
//            numReviews.setText(item.getNumReviews() != null ? item.getNumReviews() : "test");
            cakeSize.setText(item.getCakeSize());

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .into(cakeImage);
        }
    }
}
