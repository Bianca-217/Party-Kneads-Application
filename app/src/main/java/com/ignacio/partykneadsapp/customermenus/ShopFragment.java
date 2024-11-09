package com.ignacio.partykneadsapp.customermenus;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.CategoriesAdapter;
import com.ignacio.partykneadsapp.adapters.ProductShopAdapter;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.databinding.FragmentShopBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    private LinearLayout cl;
    private RecyclerView categories;
    private List<CategoriesModel> categoriesModelList;
    private CategoriesAdapter categoriesAdapter;
    private RecyclerView recyclerView;
    private ProductShopAdapter productShopAdapter;
    private List<ProductShopModel> productList;
    private FirebaseFirestore db;
    private FragmentShopBinding binding;
    CollectionReference productsRef;
    private SearchView shopsearchView;  // SearchView for the ShopFragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");

        // Use GridLayoutManager for 2 items in a row
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2 columns

        // Initialize the adapter with an empty list and the fragment's context
        productShopAdapter = new ProductShopAdapter(requireActivity(), new ArrayList<>());
        recyclerView.setAdapter(productShopAdapter);

        // Setup categories
        setupCategories();

        // Fetch products for the "Cakes" category by default
        fetchProducts("Cakes");

        // Setup SearchView
        shopsearchView = binding.ShopsearchView;  // Assuming your layout has a SearchView with this ID
        shopsearchView.setQueryHint("Search for products...");

        shopsearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No need to handle submit for Firestore query
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProducts(newText); // Call the function to search products
                return true;
            }
        });

        binding.btnAddress.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_cont);
            navController.navigate(R.id.action_homePageFragment_to_addressFragment);
        });

        binding.btnOrders.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_cont);
            navController.navigate(R.id.action_homePageFragment_to_orderFragment);
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
        categoriesModelList.add(new CategoriesModel(R.drawable.cake, "Cakes"));
        categoriesModelList.add(new CategoriesModel(R.drawable.desserts, "Dessert"));
        categoriesModelList.add(new CategoriesModel(R.drawable.balloons, "Balloons"));
        categoriesModelList.add(new CategoriesModel(R.drawable.party_hats, "Party Hats"));
        categoriesModelList.add(new CategoriesModel(R.drawable.banners, "Banners"));
        categoriesModelList.add(new CategoriesModel(R.drawable.toppers, "Toppers"));
        categoriesModelList.add(new CategoriesModel(R.drawable.backdrop, "Backdrop"));
        categoriesModelList.add(new CategoriesModel(R.drawable.customized, "Customize"));

        // Initialize adapter and layout manager for categories
        categoriesAdapter = new CategoriesAdapter(requireActivity(), categoriesModelList, category -> {
            fetchProducts(category); // Fetch products for the selected category
        });
        categories.setAdapter(categoriesAdapter);
        categories.setLayoutManager(new GridLayoutManager(requireActivity(), 1, RecyclerView.HORIZONTAL, false));
        categories.setHasFixedSize(true);
        categories.setNestedScrollingEnabled(false);
    }

    // Function to search products based on the keyword
    private void searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllProducts();  // If no search term, load all products
            return;
        }

        String normalizedKeyword = keyword.trim().toUpperCase();
        Log.d("ShopFragment", "Searching for products with keyword: " + normalizedKeyword);

        productsRef
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ProductShopModel> filteredList = new ArrayList<>();
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

                                filteredList.add(new ProductShopModel(id, imageUrl, name, price, description, rate, numreviews, category));
                            }
                        }
                        productShopAdapter.updateData(filteredList);  // Update the adapter with the filtered list
                    } else {
                        Log.d("Firestore", "Error searching products: ", task.getException());
                    }
                });
    }

    private void loadAllProducts() {
        productsRef
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ProductShopModel> allProductsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String imageUrl = document.getString("imageUrl");
                            String name = document.getString("name");
                            String price = document.getString("price");
                            String description = document.getString("description");
                            String rate = document.getString("rate");
                            String numreviews = document.getString("numreviews");
                            String category = document.getString("categories");

                            allProductsList.add(new ProductShopModel(id, imageUrl, name, price, description, rate, numreviews, category));
                        }
                        productShopAdapter.updateData(allProductsList);  // Update the adapter with all products
                    } else {
                        Log.d("Firestore", "Error loading products: ", task.getException());
                    }
                });
    }

    private void fetchProducts(String categoryFilter) {
        String startAt = categoryFilter;
        String endAt = categoryFilter + "\uf8ff";

        productsRef
                .whereGreaterThanOrEqualTo("categories", startAt)
                .whereLessThan("categories", endAt)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ProductShopModel> productsList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String imageUrl = document.getString("imageUrl");
                            String name = document.getString("name");
                            String price = document.getString("price");
                            String description = document.getString("description");
                            String rate = document.getString("rate");
                            String numreviews = document.getString("numreviews");
                            String category = document.getString("categories");

                            productsList.add(new ProductShopModel(id, imageUrl, name, price, description, rate, numreviews, category));
                        }

                        productShopAdapter.updateData(productsList);
                    } else {
                        Log.d("Firestore", "Error getting products: ", task.getException());
                    }
                });
    }
}
