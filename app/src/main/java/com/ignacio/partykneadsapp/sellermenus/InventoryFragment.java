package com.ignacio.partykneadsapp.sellermenus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentInventoryBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryFragment extends Fragment {

    private FragmentInventoryBinding binding;
    private FirebaseFirestore db;
    private List<Product> productList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        setupCategoryDropdown();
        fetchProductData(); // Initial fetch

        return binding.getRoot();
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.Categories,
                android.R.layout.simple_dropdown_item_1line
        );
        binding.categories.setAdapter(adapter);

        // Handle category selection
        binding.categories.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = parent.getItemAtPosition(position).toString();
            filterProductsByCategory(selectedCategory);
        });
    }

    private void fetchProductData() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.tableLayout.removeViews(1, binding.tableLayout.getChildCount() - 1); // Clear existing rows (except headers)

                        // Clear previous product list
                        productList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("name");
                            String stocks = getStockValue(document);
                            String category = document.contains("categories") ? document.getString("categories") : "Unknown";

                            // Store product in the list
                            productList.add(new Product(productName, stocks, category));

                            // Add all products to the table initially
                            addTableRow(productName, stocks);
                        }
                    } else {
                        // Handle failure if needed
                    }
                });
    }

    private String getStockValue(QueryDocumentSnapshot document) {
        String stocks = "N/A";  // Default value
        if (document.contains("stock")) {
            Object stockField = document.get("stock");
            if (stockField instanceof Number) {
                stocks = String.valueOf(((Number) stockField).longValue());
            } else if (stockField instanceof String) {
                stocks = (String) stockField;
            }
        }
        return stocks;
    }

    private void filterProductsByCategory(String selectedCategory) {
        binding.tableLayout.removeViews(1, binding.tableLayout.getChildCount() - 1); // Clear existing rows (except headers)

        for (Product product : productList) {
            if (product.getCategory().equals(selectedCategory)) {
                addTableRow(product.getName(), product.getStock());
            }
        }
    }

    private void addTableRow(String productName, String stocks) {
        TableRow row = new TableRow(getContext());
        row.addView(createTableCell(productName, 6));
        row.addView(createTableCell(stocks, 4));
        row.addView(createTableCell("N/A", 3)); // Placeholder for sold column

        binding.tableLayout.addView(row);
    }

    private TextView createTableCell(String text, int weight) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(12);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_bold));
        textView.setTextColor(getResources().getColor(R.color.semiblack));
        textView.setPadding(10, 10, 10, 10);
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, weight));
        return textView;
    }

    // Back button navigation
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_inventoryFragment_to_seller_HomePageFragment);
        });
    }

    // Product model class
    private static class Product {
        private final String name;
        private final String stock;
        private final String category;

        public Product(String name, String stock, String category) {
            this.name = name;
            this.stock = stock;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public String getStock() {
            return stock;
        }

        public String getCategory() {
            return category;
        }
    }
}
