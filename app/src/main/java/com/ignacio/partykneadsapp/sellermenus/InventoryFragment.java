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
                            String sold = getSoldValue(document);

                            // Store product in the list
                            productList.add(new Product(productName, stocks, category, sold));

                            // Add all products to the table initially
                            addTableRow(productName, stocks, sold);
                        }
                    } else {
                        // Handle failure if needed
                    }
                });
    }

    private String getSoldValue(QueryDocumentSnapshot document) {
        if (document.contains("sold")) {
            Object soldField = document.get("sold");
            if (soldField instanceof Number) {
                return String.valueOf(((Number) soldField).longValue());
            } else if (soldField instanceof String) {
                return (String) soldField;
            }
        }
        return "0"; // Default value if no "sold" field
    }


    private String getStockValue(QueryDocumentSnapshot document) {
        String stocks = "N/A";  // Default value

        // Check if the product belongs to the specified categories
        if (document.contains("categories")) {
            String category = document.getString("categories");
            if ("Cakes".equals(category) ||  "Dessert - Cupcakes".equals(category) ||
                    "Dessert - Donuts".equals(category) ||
                    "Dessert - Cookies".equals(category)) {
                return "Made-to-order";
            }
        }

        // Handle other stock values if "stock" exists
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
                addTableRow(product.getName(), product.getStock(), product.getSold());
            }
        }
}

    private void addTableRow(String productName, String stocks, String sold) {
        TableRow row = new TableRow(getContext());

        // Add product name column (left-aligned)
        row.addView(createTableCell(productName, 6, false));

        // Add stocks column (center-aligned)
        row.addView(createTableCell(stocks, 4, true));

        // Add sold column (center-aligned)
        row.addView(createTableCell(sold, 3, true));

        binding.tableLayout.addView(row);
    }


    private TextView createTableCell(String text, int weight, boolean isCentered) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(12);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_bold));
        textView.setTextColor(getResources().getColor(R.color.semiblack));
        textView.setPadding(10, 10, 10, 10);

        // Set alignment based on the parameter
        textView.setGravity(isCentered ? android.view.Gravity.CENTER : android.view.Gravity.START);

        // Layout parameters with optional gravity
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, weight);
        if (isCentered) {
            params.gravity = android.view.Gravity.CENTER;
        }
        textView.setLayoutParams(params);

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

    private static class Product {
        private final String name;
        private final String stock;
        private final String category;
        private final String sold;

        public Product(String name, String stock, String category, String sold) {
            this.name = name;
            this.stock = stock;
            this.category = category;
            this.sold = sold;
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

        public String getSold() {
            return sold;
        }
    }

}
