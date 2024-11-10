// SellerTransactions.java
package com.ignacio.partykneadsapp.sellermenus;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.TransactionCompletedAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentSellerTransactionsBinding;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.OrderItemWithReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellerTransactions extends Fragment {
    List<OrderItemWithReference> orderListWithRefID = new ArrayList<>();
    FragmentSellerTransactionsBinding binding;
    FirebaseFirestore db;
    String adminEmail = "sweetkatrinabiancaignacio@gmail.com"; // Admin email

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerTransactionsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        TransactionCompletedAdapter adapter = new TransactionCompletedAdapter(orderListWithRefID, getContext());

        // Set up RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        // Fetch complete orders
        fetchCompleteOrders(adapter);
    }

    private void fetchCompleteOrders(TransactionCompletedAdapter adapter) {
        db.collection("Users")
                .whereEqualTo("email", adminEmail) // Query by email field
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the user document
                        DocumentSnapshot userDocument = querySnapshot.getDocuments().get(0);

                        // Access the Orders collection
                        db.collection("Users")
                                .document(userDocument.getId()) // Get the user document ID
                                .collection("Orders")
                                .whereEqualTo("status", "Complete Order") // Filter by complete status
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    // Clear previous list to prevent data duplication
                                    orderListWithRefID.clear();

                                    // Loop through orders and extract items, but only take the first item from each order
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        // Fetch the items from the order document
                                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");

                                        if (items != null && !items.isEmpty()) {
                                            // Only take the first item from the list of items in the order
                                            Map<String, Object> firstItem = items.get(0); // Get the first item

                                            String productName = (String) firstItem.get("productName");
                                            String cakeSize = (String) firstItem.get("cakeSize");
                                            long quantity = (long) firstItem.get("quantity");
                                            String totalPrice = (String) firstItem.get("totalPrice");
                                            String imageUrl = (String) firstItem.get("imageUrl");

                                            // Create a new OrderItemModel for the first item
                                            OrderItemModel orderItem = new OrderItemModel(
                                                    productName, cakeSize, imageUrl, (int) quantity, totalPrice
                                            );

                                            String referenceID = doc.getId();  // Get the reference ID

                                            // Wrap the OrderItemModel and referenceID into OrderItemWithReference
                                            orderListWithRefID.add(new OrderItemWithReference(orderItem, referenceID));
                                        }
                                    }

                                    // Notify the adapter to update the RecyclerView
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SellerTransactions", "Error fetching orders: ", e);
                                });
                    } else {
                        Log.d("SellerTransactions", "No user found with this email.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SellerTransactions", "Error fetching user: ", e);
                });
    }
}
