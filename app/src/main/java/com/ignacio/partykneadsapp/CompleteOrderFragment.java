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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.CompleteOrderAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentCompleteOrderBinding;
import com.ignacio.partykneadsapp.model.CompleteOrderModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompleteOrderFragment extends Fragment {

    private FragmentCompleteOrderBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CompleteOrderAdapter adapter;
    private List<CompleteOrderModel> completeOrdersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCompleteOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize list and adapter
        completeOrdersList = new ArrayList<>();
        adapter = new CompleteOrderAdapter(getContext(), completeOrdersList);

        // Set up RecyclerView
        binding.completedRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.completedRecyclerview.setAdapter(adapter);

        // Fetch data
        fetchCompleteOrdersFromFirestore();
    }

    private void fetchCompleteOrdersFromFirestore() {
        String uid = auth.getCurrentUser().getUid(); // Get current user ID
        CollectionReference ordersRef = db.collection("Users").document(uid).collection("Orders");

        ordersRef.whereEqualTo("status", "Complete Order").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                completeOrdersList.clear(); // Clear the list before adding new data

                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");

                    if (items != null && !items.isEmpty()) {
                        Map<String, Object> firstItem = items.get(0); // Get the first item
                        processOrder(firstItem, document); // Process and add to the list
                    } else {
                        Log.d("CompleteOrderFragment", "No items found for order " + document.getId());
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter about data changes
            } else {
                Log.e("CompleteOrderFragment", "Error fetching complete orders", task.getException());
                Toast.makeText(getContext(), "Failed to load complete orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrder(Map<String, Object> item, QueryDocumentSnapshot doc) {
        String userName = doc.getString("userName");
        String contactNum = doc.getString("phoneNumber");
        String location = doc.getString("location");
        String productName = (String) item.get("productName");
        String cakeSize = (String) item.get("cakeSize");
        long quantity = (long) item.get("quantity");
        String totalPrice = (String) item.get("totalPrice");
        String imageUrl = (String) item.get("imageUrl");

        // Format totalPrice with ₱ sign and convert to numeric value if needed
        String formattedPrice = "₱" + cleanTotalPrice(totalPrice);

        // Create and add CompleteOrderModel to the list
        CompleteOrderModel order = new CompleteOrderModel(
                userName,
                contactNum,
                location,
                productName,
                cakeSize,
                (int) quantity,
                formattedPrice,
                "Complete Order",
                imageUrl
        );
        completeOrdersList.add(order);
    }

    private String cleanTotalPrice(String price) {
        if (price != null) {
            price = price.replaceAll("[^\\d.]", ""); // Remove non-numeric characters
            try {
                double numericPrice = Double.parseDouble(price);
                return (numericPrice % 1 == 0) ? String.valueOf((int) numericPrice) : String.valueOf(numericPrice); // Return whole number if no decimals
            } catch (NumberFormatException e) {
                Log.e("CompleteOrderFragment", "Error parsing totalPrice: " + price);
            }
        }
        return "0"; // Default value if parsing fails
    }
}
