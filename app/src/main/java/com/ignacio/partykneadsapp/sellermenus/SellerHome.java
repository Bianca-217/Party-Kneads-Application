package com.ignacio.partykneadsapp.sellermenus;

import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentSellerHomeBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SellerHome extends Fragment {
    private FragmentSellerHomeBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private PieChart pieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSellerHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        pieChart = binding.pieChart;


        binding.myproduct.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_seller_HomePageFragment_to_myProductFragment);
        });

        binding.revenueView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_seller_HomePageFragment_to_analyticsFragment);
        });

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadUserProfilePicture(userId);
        }


        if (binding == null) {
            Log.e("SellerHome", "Binding is null");
            return;
        }

        binding.btnmyProduct1.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView2);
            navController.navigate(R.id.action_seller_HomePageFragment_to_myProductFragment);
        });

        binding.btnInventory.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView2);
            navController.navigate(R.id.action_seller_HomePageFragment_to_inventoryFragment);
        });

        fetchTotalProductCount();
        fetchPendingOrdersCount();
        fetchTotalRevenue();
        setupPieChart();
    }

    private void loadUserProfilePicture(String userId) {
        firestore.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the profile picture URL from Firestore
                            String profilePictureUrl = document.getString("profilePictureUrl");

                            // Check if the fragment is still attached to avoid IllegalStateException
                            if (isAdded() && getContext() != null) {
                                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                                    // Use Glide to load the profile picture
                                    Glide.with(requireContext())
                                            .load(profilePictureUrl)
                                            .placeholder(R.drawable.round_person_24) // Default placeholder
                                            .error(R.drawable.img_placeholder) // Error placeholder
                                            .into(binding.imgUserProfile);
                                } else {
                                    Toast.makeText(getActivity(), "No profile picture found", Toast.LENGTH_SHORT).show();
                                    binding.imgUserProfile.setImageResource(R.drawable.img_placeholder);
                                }
                            } else {
                                Log.w("SellerHome", "Fragment is not attached to the context.");
                            }
                        } else {
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to load profile picture", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchTotalProductCount() {
        // Reference to 'product' collection
        CollectionReference productsRef = firestore.collection("products");

        // Get all documents in the 'product' collection
        productsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Get the count of documents
                int totalProducts = task.getResult().size();

                // Display the count in the TextView
                binding.numOverallProduct.setText(String.valueOf(totalProducts));

            } else {
                Log.e("Firestore", "Error getting documents: ", task.getException());
                Toast.makeText(requireContext(), "Failed to fetch product count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPendingOrdersCount() {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's 'Orders' collection
        CollectionReference ordersRef = firestore.collection("Users")
                .document(userId)
                .collection("Orders");

        // Query orders where the status is 'placed'
        ordersRef.whereEqualTo("status", "placed").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Get the count of pending orders
                int pendingOrders = task.getResult().size();

                // Display the count in the TextView
                binding.numPendingOrders.setText(String.valueOf(pendingOrders));

            } else {
                Log.e("Firestore", "Error getting pending orders: ", task.getException());
                Toast.makeText(requireContext(), "Failed to fetch pending orders count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTotalRevenue() {
        // Get today's date in the format "MMMM dd, yyyy" (e.g., "December 07, 2024")
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
        String todayDateString = sdf.format(new Date());  // Today's date as a string

        // Reference to the user's 'Orders' collection
        CollectionReference ordersRef = firestore.collection("Users")
                .document("QqqccLchjigd0C7zf8ewPXY0KZc2")
                .collection("Orders");

        // Query all orders (you'll filter later by the timestamp)
        ordersRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            Log.d("Firestore", "No orders found.");
                            Toast.makeText(requireContext(), "No orders for today", Toast.LENGTH_SHORT).show();
                        } else {
                            double totalRevenue = 0.0;

                            // Check if results are returned
                            Log.d("Firestore", "Number of orders found: " + task.getResult().size());

                            // Iterate through the results and sum the totalPrice for today's orders
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the timestamp string and totalPrice for each order
                                String timestamp = document.getString("timestamp");
                                String totalPrice = document.getString("totalPrice");

                                // Debug logging to verify the timestamp and totalPrice
                                Log.d("Firestore", "Order Timestamp: " + timestamp);
                                Log.d("Firestore", "Order TotalPrice: " + totalPrice);

                                if (timestamp != null && totalPrice != null) {
                                    // Extract only the date part from the timestamp string (e.g., "December 07, 2024")
                                    String orderDateString = extractDateFromTimestamp(timestamp);

                                    // Compare the order date with today's date
                                    if (orderDateString.equals(todayDateString)) {
                                        // Remove the currency symbol and parse the price
                                        double totalPriceDouble = Double.parseDouble(totalPrice.replace("₱", ""));
                                        totalRevenue += totalPriceDouble;  // Add the totalPrice to the total revenue
                                    }
                                }
                            }

                            String formattedRevenue = String.format("₱%.2f", totalRevenue);
                            Log.d("Firestore", "Total Revenue for today: " + formattedRevenue);
                            binding.revenue.setText(formattedRevenue);  // Display the total revenue
                        }
                    } else {
                        Log.e("Firestore", "Error getting revenue data: ", task.getException());
                        Toast.makeText(requireContext(), "Failed to fetch total revenue", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String extractDateFromTimestamp(String timestamp) {
        try {
            // Use SimpleDateFormat to parse the timestamp and format it into the desired "MMMM dd, yyyy" format
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);

            // Parse the timestamp string and reformat it to match today's date format
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);  // Return only the date part (e.g., "December 07, 2024")
        } catch (ParseException e) {
            Log.e("Timestamp Parsing", "Error parsing timestamp: " + timestamp, e);
            return "";  // Return an empty string if parsing fails
        }
    }

    private void setupPieChart() {
        CollectionReference productsRef = firestore.collection("products");
        final HashMap<String, Double> productRevenueMap = new HashMap<>();

        // Set up a real-time listener for the "products" collection
        productsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening for updates: ", error);
                Toast.makeText(requireContext(), "Failed to listen for product updates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null && !value.isEmpty()) {
                productRevenueMap.clear();

                for (QueryDocumentSnapshot document : value) {
                    try {
                        String productName = (String) document.get("name");
                        Double productPrice = Double.valueOf(String.valueOf(document.get("price")));
                        Long productSold = Long.valueOf(String.valueOf(document.get("sold"))); // Ensure 'sold' field exists

                        if (productName != null && productPrice != null && productSold != null) {
                            double totalRevenue = productPrice * productSold;
                            productRevenueMap.put(productName, totalRevenue);
                        }
                    } catch (Exception e) {
                        Log.e("Firestore", "Error parsing document: " + e.getMessage());
                    }
                }

                // Sort products by revenue in descending order
                List<Map.Entry<String, Double>> sortedProducts = new ArrayList<>(productRevenueMap.entrySet());
                sortedProducts.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

                // Get the top 5 products
                List<Map.Entry<String, Double>> topProducts = sortedProducts.subList(0, Math.min(5, sortedProducts.size()));

                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                for (Map.Entry<String, Double> entry : topProducts) {
                    String productName = entry.getKey();
                    double totalRevenue = entry.getValue();

                    // Add product name and revenue to the pie entry, but only display the product name inside the slice
                    pieEntries.add(new PieEntry((float) totalRevenue, productName)); // Display only the product name
                }

                if (pieEntries.isEmpty()) {
                    Log.e("PieChart", "No data available for the pie chart");
                    return;
                }

                PieDataSet pieDataSet = new PieDataSet(pieEntries, "Best Sellers");

                // Custom color list to avoid color duplication
                List<Integer> customColors = new ArrayList<>();
                customColors.add(Color.parseColor("#FF5733")); // Orange
                customColors.add(Color.parseColor("#33FF57")); // Green
                customColors.add(Color.parseColor("#3357FF")); // Blue
                customColors.add(Color.parseColor("#FF33A1")); // Pink
                customColors.add(Color.parseColor("#FFC300")); // Yellow

                // Apply the custom color list to the PieDataSet
                pieDataSet.setColors(customColors);

                pieDataSet.setValueTextSize(12f); // Reduced text size for cleaner appearance
                pieDataSet.setValueTextColor(Color.BLACK); // Keep value text visible inside the slice

                PieData pieData = new PieData(pieDataSet);
                pieData.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return "₱" + String.format("%.2f", value); // Display the revenue with currency formatting
                    }
                });

                pieChart.setData(pieData);

                // Configure pie chart appearance
                pieChart.setDrawHoleEnabled(false); // Disable the hole in the center
                pieChart.setTransparentCircleColor(Color.WHITE);
                pieChart.setUsePercentValues(false); // Disable percentage values
                pieChart.setEntryLabelColor(Color.BLACK); // Set label color to black for better contrast
                pieChart.setEntryLabelTextSize(12f); // Adjust label text size

                // Disable the legend completely (this removes the product names below the chart)
                pieChart.getLegend().setEnabled(false);

                // Refresh the pie chart
                pieChart.invalidate();
            } else {
                Log.e("Firestore", "No product data available");
                Toast.makeText(requireContext(), "No product data available", Toast.LENGTH_SHORT).show();
            }
        });
    }
}