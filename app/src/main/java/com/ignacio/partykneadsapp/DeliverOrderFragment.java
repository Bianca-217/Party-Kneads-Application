package com.ignacio.partykneadsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.DeliverOrderAdapter;  // Updated adapter import
import com.ignacio.partykneadsapp.databinding.FragmentDeliverOrderBinding;  // Correct binding class
import com.ignacio.partykneadsapp.model.DeliverOrderModel;  // Updated model import

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeliverOrderFragment extends Fragment {
    private FragmentDeliverOrderBinding binding;  // Correct binding declaration for the current fragment
    private DeliverOrderAdapter adapter;  // Updated adapter type
    private List<DeliverOrderModel> ordersList = new ArrayList<>();  // Use DeliverOrderModel here
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeliverOrderBinding.inflate(getLayoutInflater());  // Inflate using the correct binding class
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize the adapter and RecyclerView
        adapter = new DeliverOrderAdapter(getContext(), ordersList);  // Updated adapter to DeliverOrderAdapter
        RecyclerView recyclerView = binding.deliverRecyclerview;  // Assuming you have a RecyclerView in your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));  // Set LayoutManager
        recyclerView.setAdapter(adapter);  // Set the adapter to the RecyclerView

        // Call the method to fetch orders when the view is created
        fetchOrdersWithPreparingStatus();
    }

    // Function to fetch orders with "preparing order" status
    private void fetchOrdersWithPreparingStatus() {
        if (auth.getCurrentUser() == null) {
            Log.e("DeliverOrderFragment", "User is not authenticated");
            return;
        }

        String uid = auth.getCurrentUser().getUid(); // Get the current user's UID
        CollectionReference ordersRef = db.collection("Users").document(uid).collection("Orders");

        ordersRef.whereEqualTo("status", "preparing order").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ordersList.clear();  // Clear the existing orders in the list
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");

                    if (items != null && !items.isEmpty()) {
                        Map<String, Object> firstItem = items.get(0); // Get the first item
                        processOrder(firstItem, document); // Process the first item
                    } else {
                        Log.d("DeliverOrderFragment", "No items found for order " + document.getId());
                    }
                }
                adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
            } else {
                Log.e("DeliverOrderFragment", "Error fetching orders", task.getException());
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

        // Create DeliverOrderModel instance and add to list
        DeliverOrderModel order = new DeliverOrderModel(userName, contactNum, location,
                productName, cakeSize, (int) quantity, String.valueOf(cleanTotalPrice), imageUrl);
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
