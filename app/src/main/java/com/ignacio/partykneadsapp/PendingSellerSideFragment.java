package com.ignacio.partykneadsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.PendingOrdersAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentPendingSellerSideBinding;
import com.ignacio.partykneadsapp.databinding.FragmentSellerOrderBinding;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingSellerSideFragment extends Fragment {

    private FragmentPendingSellerSideBinding binding;
    private PendingOrdersAdapter adapter;
    private List<PendingOrdersModel> ordersList;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // Declare FirebaseAuth

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPendingSellerSideBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // Initialize list and adapter
        ordersList = new ArrayList<>();
        adapter = new PendingOrdersAdapter(getContext(), ordersList);

        // Set up RecyclerView
        binding.pendingRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pendingRecyclerview.setAdapter(adapter);

//        binding.btnPending.setOnClickListener(view1 -> {
//            fetchOrdersFromFirestore(); // Fetch orders with placed status
//            adapter.toggleMode("Accept"); // Toggle mode for adapter (if applicable)
//        });

        // Initial fetch of orders (defaults to placed orders)
        fetchOrdersFromFirestore();
    }

    // Function to fetch orders with "placed" status
    private void fetchOrdersFromFirestore() {
        String uid = auth.getCurrentUser().getUid(); // Get the current user's UID
        CollectionReference ordersRef = db.collection("Users").document(uid).collection("Orders");

        ordersRef.whereEqualTo("status", "placed").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ordersList.clear(); // Clear the existing list
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");

                    if (items != null && !items.isEmpty()) {
                        Map<String, Object> firstItem = items.get(0); // Get the first item in the order
                        processOrder(firstItem, document); // Process the first item
                    } else {
                        Log.d("SellerOrderFragment", "No items found for order " + document.getId());
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("SellerOrderFragment", "Error fetching orders", task.getException());
                Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to process the order data and add to the list
    private void processOrder(Map<String, Object> item, QueryDocumentSnapshot doc) {
        String userName = doc.getString("userName");
        String contactNum = doc.getString("phoneNumber");
        String location = doc.getString("location");
        String refID = doc.getString("referenceId");
        String userEmail = doc.getString("email");  // Assuming email is stored in the document

        String productName = (String) item.get("productName");
        String cakeSize = (String) item.get("cakeSize");
        long quantity = (long) item.get("quantity");
        String totalPrice = (String) item.get("totalPrice");
        String imageUrl = (String) item.get("imageUrl");

        // Clean up the totalPrice and convert to numeric value
        String cleanTotalPrice = cleanTotalPrice(totalPrice);

        // Create PendingOrdersModel instance and add to list
        PendingOrdersModel order = new PendingOrdersModel(userName, contactNum, location,
                productName, cakeSize, (int) quantity, String.valueOf(cleanTotalPrice), imageUrl, "Waiting for seller to confirm", refID, userEmail);
        ordersList.add(order); // Add to orders list
    }

    // Helper function to clean and parse totalPrice string
    private String cleanTotalPrice(String price) {
        if (price != null) {
            price = price.replaceAll("[^\\d.]", ""); // Remove non-numeric characters
            try {
                double parsedPrice = Double.parseDouble(price); // Parse as double
                // Check if the parsed price is an integer (has no fractional part)
                if (parsedPrice == Math.floor(parsedPrice)) {
                    return "₱" + (int) parsedPrice; // Return as whole number with peso sign
                } else {
                    return "₱" + parsedPrice; // Return as double with peso sign
                }
            } catch (NumberFormatException e) {
                Log.e("SellerOrderFragment", "Error parsing totalPrice: " + price);
            }
        }
        return "₱0"; // Return default value with peso sign if parsing fails
    }
}