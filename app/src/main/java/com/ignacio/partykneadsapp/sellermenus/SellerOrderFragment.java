package com.ignacio.partykneadsapp.sellermenus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.PendingOrdersAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentSellerOrderBinding;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;
import com.ignacio.partykneadsapp.model.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellerOrderFragment extends Fragment {

    private FragmentSellerOrderBinding binding;
    private PendingOrdersAdapter adapter;
    private List<PendingOrdersModel> ordersList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private NotificationViewModel notificationViewModel; // Add ViewModel for notifications

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSellerOrderBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and ViewModel
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);

        // Initialize list and adapter
        ordersList = new ArrayList<>();
        adapter = new PendingOrdersAdapter(getContext(), ordersList, notificationViewModel); // Pass ViewModel to Adapter

        // Set up RecyclerView
        binding.pendingOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pendingOrders.setAdapter(adapter);

        // Fetch orders with "placed" status initially
        fetchOrdersFromFirestore();

        // Set up button listeners
        binding.btnToDeliver.setOnClickListener(view1 -> {
            fetchOrdersWithPreparingStatus(); // Fetch orders with "preparing" status
            adapter.toggleMode("Deliver"); // Toggle mode to deliver
        });

        binding.btnPending.setOnClickListener(view1 -> {
            fetchOrdersFromFirestore(); // Fetch orders with "placed" status
            adapter.toggleMode("Accept"); // Toggle mode to accept
        });

        // Listen for when an admin accepts an order (handled in the adapter directly)
        // Since you've already updated the order status inside the adapter, no need for another listener
    }

    // Function to fetch orders with "placed" status
    private void fetchOrdersFromFirestore() {
        String uid = auth.getCurrentUser().getUid();
        CollectionReference ordersRef = db.collection("Users").document(uid).collection("Orders");

        ordersRef.whereEqualTo("status", "placed").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ordersList.clear();  // Clear the existing orders
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

    // Function to fetch orders with "preparing order" status
    private void fetchOrdersWithPreparingStatus() {
        String uid = auth.getCurrentUser().getUid();
        CollectionReference ordersRef = db.collection("Users").document(uid).collection("Orders");

        ordersRef.whereEqualTo("status", "preparing order").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ordersList.clear();  // Clear the existing orders
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

        String productName = (String) item.get("productName");
        String cakeSize = (String) item.get("cakeSize");
        long quantity = (long) item.get("quantity");
        String totalPrice = (String) item.get("totalPrice");
        String imageUrl = (String) item.get("imageUrl");

        // Clean up the totalPrice and convert to numeric value
        double cleanTotalPrice = cleanTotalPrice(totalPrice);

        // Create PendingOrdersModel instance and add to list
        PendingOrdersModel order = new PendingOrdersModel(userName, contactNum, location,
                productName, cakeSize, (int) quantity, String.valueOf(cleanTotalPrice), imageUrl, "Waiting for seller to confirm", refID);
        ordersList.add(order);
    }

    // Helper function to clean and parse totalPrice string
    private double cleanTotalPrice(String price) {
        if (price != null) {
            price = price.replaceAll("[^\\d.]", ""); // Remove non-numeric characters
            try {
                return Double.parseDouble(price); // Parse to double
            } catch (NumberFormatException e) {
                Log.e("SellerOrderFragment", "Error parsing totalPrice: " + price);
            }
        }
        return 0.0; // Return default value if parsing fails
    }

    // Function to accept the order
    private void acceptOrder(String orderRefId) {
        // Get the customer userId associated with the order (you might need to fetch this from the order data)
        String customerUserId = getCustomerUserIdForOrder(orderRefId);

        // Update the order status to "accepted"
        db.collection("Users").document(auth.getCurrentUser().getUid())
                .collection("Orders")
                .document(orderRefId)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    Log.d("SellerOrderFragment", "Order accepted: " + orderRefId);

                    // Send notification to the customer that the order is now "to ship"
                    sendNotificationToCustomer(orderRefId, customerUserId);
                })
                .addOnFailureListener(e -> {
                    Log.w("SellerOrderFragment", "Error accepting order", e);
                    Toast.makeText(getContext(), "Failed to accept order", Toast.LENGTH_SHORT).show();
                });
    }

    // Function to send notification to the customer
    private void sendNotificationToCustomer(String orderRefId, String customerUserId) {
        // Set the status to "to ship" for the customer
        db.collection("Users").document(customerUserId)
                .collection("Orders")
                .document(orderRefId)
                .update("status", "to ship")
                .addOnSuccessListener(aVoid -> {
                    Log.d("SellerOrderFragment", "Customer's order status updated to 'to ship'");
                    // Notify the customer by updating the NotificationFragment
                    notificationViewModel.setOrderReferenceId(orderRefId);
                })
                .addOnFailureListener(e -> {
                    Log.w("SellerOrderFragment", "Error updating customer's order status", e);
                });
    }

    // Helper function to get the customer userId for an order (this may vary depending on your order structure)
    private String getCustomerUserIdForOrder(String orderRefId) {
        // You may need to implement this based on how your order data is structured.
        // For example, fetching the customer userId from Firestore based on the order reference.
        return "customerUserId"; // Replace with actual logic to get the customer user ID
    }
}