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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.PendingOrdersAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentSellerOrderBinding;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellerOrderFragment extends Fragment {

    private FragmentSellerOrderBinding binding;
    private PendingOrdersAdapter adapter;
    private List<PendingOrdersModel> ordersList;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // Declare FirebaseAuth

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSellerOrderBinding.inflate(getLayoutInflater());
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
        binding.pendingOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pendingOrders.setAdapter(adapter);

        // Fetch data from Firestore
        fetchOrdersFromFirestore();
    }

    private void fetchOrdersFromFirestore() {
        // Get the current user's UID
        String uid = auth.getCurrentUser().getUid(); // Get the current user's UID
        CollectionReference ordersRef = db.collection("Users").document(uid).collection("Orders");

        // Fetch orders with status "placed"
        ordersRef.whereEqualTo("status", "placed").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    processOrder(document); // Process each order document
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("SellerOrderFragment", "Error fetching orders", task.getException());
                Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void processOrder(QueryDocumentSnapshot doc) {
        String userName = doc.getString("userName"); // Assuming userName is in the document
        String contactNum = doc.getString("phoneNumber"); // Assuming contactNum is in the document
        String location = doc.getString("location");
        String refID = doc.getString("referenceId");

        // Retrieve the 'items' array from the document
        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
        if (items != null) {
            Log.d("SellerOrderFragment", "Number of items: " + items.size()); // Log the number of items
            for (Map<String, Object> item : items) {
                // Assuming location is in the document
                String productName = (String) item.get("productName");
                String cakeSize = (String) item.get("cakeSize");
                long quantity = (long) item.get("quantity"); // Assuming quantity is stored as long
                String totalPrice = (String) item.get("totalPrice");
                String imageUrl = (String) item.get("imageUrl");
                String status = "Waiting for seller to confirm"; // Set the status message

                // Create a new PendingOrdersModel instance
                PendingOrdersModel order = new PendingOrdersModel(userName, contactNum, location,
                        productName, cakeSize, (int) quantity, totalPrice, imageUrl, status, refID);
                ordersList.add(order); // Add order to list
            }
        } else {
            Log.w("SellerOrderFragment", "No items found in the order document");
        }
    }


}
