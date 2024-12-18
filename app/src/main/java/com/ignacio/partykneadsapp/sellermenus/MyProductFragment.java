package com.ignacio.partykneadsapp.sellermenus;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.CategoriesAdapter;
import com.ignacio.partykneadsapp.adapters.ProductShopAdapter;
import com.ignacio.partykneadsapp.adapters.SellerProductAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentMyProductBinding;
import com.ignacio.partykneadsapp.databinding.FragmentShopBinding;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.model.SellerProductModel;

import java.util.ArrayList;
import java.util.List;
public class MyProductFragment extends Fragment {
    private LinearLayout cl;
    private RecyclerView categories;
    private List<CategoriesModel> categoriesModelList;
    private CategoriesAdapter categoriesAdapter;
    private RecyclerView recyclerView;
    private SellerProductAdapter productShopAdapter;
    private List<SellerProductModel> productList;
    private FirebaseFirestore db;
    CollectionReference productsRef;
    private FragmentMyProductBinding binding;
    private SearchView searchView;  // Declare SearchView

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");

        // Initialize SearchView
        searchView = binding.likesearchView;  // Reference the SearchView from the XML

        // Set up SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);  // Perform search when user submits query
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProducts(newText);  // Perform search as text changes
                return false;
            }
        });

        // Use GridLayoutManager for 2 items in a row
        RecyclerView productsRecyclerView = binding.recyclerView;
        productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2 columns

        // Initialize the adapter with an empty list and the fragment's context
        productShopAdapter = new SellerProductAdapter(requireActivity(), new ArrayList<>());
        productsRecyclerView.setAdapter(productShopAdapter);

        // Setup categories
        setupCategories();

        // Fetch products
        fetchProducts("All Items");

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_myProductFragment_to_seller_HomePageFragment);
        });

        binding.vouchers.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_myProductFragment_to_voucherSellerFragment);
        });

        // Set up click listener to hide the keyboard when tapping outside
        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
    }

    private void setupCategories() {
        categories = binding.categories;
        categoriesModelList = new ArrayList<>();

        // Add categories to the list (Images should exist in drawable folder)
        categoriesModelList.add(new CategoriesModel(R.drawable.all, "All Items"));
        categoriesModelList.add(new CategoriesModel(R.drawable.cake, "Cakes"));
        categoriesModelList.add(new CategoriesModel(R.drawable.desserts, "Dessert"));
        categoriesModelList.add(new CategoriesModel(R.drawable.balloons, "Balloons"));
        categoriesModelList.add(new CategoriesModel(R.drawable.party_hats, "Party Hats"));
        categoriesModelList.add(new CategoriesModel(R.drawable.banners, "Banners"));
        categoriesModelList.add(new CategoriesModel(R.drawable.customized, "Customize"));

        categoriesAdapter = new CategoriesAdapter(requireActivity(), categoriesModelList, category -> {
            fetchProducts(category); // Fetch products for the selected category
        });

        categories.setAdapter(categoriesAdapter);
        categories.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        categories.setHasFixedSize(true);
        categories.setNestedScrollingEnabled(false);
    }

    private void fetchProducts(String categoryFilter) {
        if (categoryFilter.equals("All Items")) {
            // Fetch all products without filtering
            productsRef
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<SellerProductModel> productsList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String imageUrl = document.getString("imageUrl");
                                String name = document.getString("name");
                                String price = document.getString("price");
                                String description = document.getString("description");
                                String rate = document.getString("rate");
                                String numreviews = document.getString("numreviews");
                                String category = document.getString("categories");

                                productsList.add(new SellerProductModel(id, imageUrl, name, price, description, rate, numreviews, category));
                            }

                            productShopAdapter.updateData(productsList);
                        } else {
                            Log.d("Firestore", "Error getting products: ", task.getException());
                        }
                    });
        } else {
            // Filter products by category
            String startAt = categoryFilter;
            String endAt = categoryFilter + "\uf8ff"; // Unicode character to include anything starting with the prefix

            productsRef
                    .whereGreaterThanOrEqualTo("categories", startAt)
                    .whereLessThan("categories", endAt)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<SellerProductModel> productsList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String imageUrl = document.getString("imageUrl");
                                String name = document.getString("name");
                                String price = document.getString("price");
                                String description = document.getString("description");
                                String rate = document.getString("rate");
                                String numreviews = document.getString("numreviews");
                                String category = document.getString("categories");

                                productsList.add(new SellerProductModel(id, imageUrl, name, price, description, rate, numreviews, category));
                            }

                            productShopAdapter.updateData(productsList);
                        } else {
                            Log.d("Firestore", "Error getting products: ", task.getException());
                        }
                    });
        }
    }

    private void searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            fetchProducts("All Items");  // If no search term, load all products
            return;
        }

        String normalizedKeyword = keyword.trim().toUpperCase();
        Log.d("MyProductFragment", "Searching for products with keyword: " + normalizedKeyword);

        // Remove default category filtering; just search based on the keyword
        productsRef
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<SellerProductModel> filteredList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("name");
                            if (productName != null && productName.toUpperCase().contains(normalizedKeyword)) {
                                String id = document.getId();
                                String imageUrl = document.getString("imageUrl");
                                String name = document.getString("name");
                                String price = document.getString("price");
                                String description = document.getString("description");
                                String rate = document.getString("rate");
                                String numreviews = document.getString("numreviews");
                                String category = document.getString("categories");

                                filteredList.add(new SellerProductModel(id, imageUrl, name, price, description, rate, numreviews, category));
                            }
                        }
                        productShopAdapter.updateData(filteredList);  // Update the adapter with the filtered list
                    } else {
                        Log.d("Firestore", "Error searching products: ", task.getException());
                    }
                });
    }
}
