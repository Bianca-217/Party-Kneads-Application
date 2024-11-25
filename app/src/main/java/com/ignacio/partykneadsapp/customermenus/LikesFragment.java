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

import com.google.firebase.auth.FirebaseAuth;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.CategoriesAdapter;
import com.ignacio.partykneadsapp.adapters.ProductShopAdapter;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.databinding.FragmentLikesBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LikesFragment extends Fragment {

    private LinearLayout cl;
    private RecyclerView categories;
    private List<CategoriesModel> categoriesModelList;
    private CategoriesAdapter categoriesAdapter;
    private RecyclerView recyclerView;
    private ProductShopAdapter productShopAdapter;
    private List<ProductShopModel> likedProductList;
    private FirebaseFirestore db;
    private FragmentLikesBinding binding;
    private SearchView likesSearchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLikesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        likesSearchView = binding.likesearchView;  // Assuming your layout has a SearchView with this ID
        likesSearchView.setQueryHint("Search for products...");

        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter for liked products
        recyclerView = binding.likerecyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2 columns

        likedProductList = new ArrayList<>();
        productShopAdapter = new ProductShopAdapter(requireActivity(), likedProductList);
        recyclerView.setAdapter(productShopAdapter);

        // Setup categories
        setupCategories();

        // Fetch liked products based on a default category
        fetchLikedProducts("All Items");

        // Setup SearchView for liked products
        // Setup SearchView
        likesSearchView = binding.likesearchView;  // Assuming your layout has a SearchView with this ID
        likesSearchView.setQueryHint("Search for products...");

        likesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        binding.btnCart.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_cont);
            navController.navigate(R.id.action_homePageFragment_to_cartFragment);
        });

    }



    private void setupCategories() {
        categories = binding.categories;
        categoriesModelList = new ArrayList<>();

        // Add categories as done in the ShopFragment
        categoriesModelList.add(new CategoriesModel(R.drawable.all, "All Items"));
        categoriesModelList.add(new CategoriesModel(R.drawable.cake, "Cakes"));
        categoriesModelList.add(new CategoriesModel(R.drawable.desserts, "Dessert"));
        categoriesModelList.add(new CategoriesModel(R.drawable.balloons, "Balloons"));
        categoriesModelList.add(new CategoriesModel(R.drawable.party_hats, "Party Hats"));
        categoriesModelList.add(new CategoriesModel(R.drawable.banners, "Banners"));
        categoriesModelList.add(new CategoriesModel(R.drawable.customized, "Customize"));

        // Initialize adapter and layout manager for categories
        categoriesAdapter = new CategoriesAdapter(requireActivity(), categoriesModelList, category -> {
            fetchLikedProducts(category); // Fetch liked products for the selected category
        });

        categories.setAdapter(categoriesAdapter);
        categories.setLayoutManager(new GridLayoutManager(requireActivity(), 1, RecyclerView.HORIZONTAL, false));
        categories.setHasFixedSize(true);
        categories.setNestedScrollingEnabled(false);

        // Programmatically select "All Items" category
        fetchLikedProducts("All Items");
    }

    // Function to search products based on the keyword
    private void searchProducts(String keyword) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllLikedProducts(uid);
            return;// If no search term, load all products

        }

        String normalizedKeyword = keyword.trim().toUpperCase();
        Log.d("ShopFragment", "Searching for products with keyword: " + normalizedKeyword);

        db.collection("Users")
                .document(uid)  // Navigate to the current user's document
                .collection("Likes")  // Fetch liked products from the "Likes" collection
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
                        productShopAdapter.updateData(filteredList); // Update adapter with all liked products
                    } else {
                        Log.d("Firestore", "Error loading liked products: ", task.getException());
                    }

                });
    }


    private void fetchLikedProducts(String categoryFilter) {
        // Fetch the current user's UID (You can retrieve this from FirebaseAuth)
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if ("All Items".equals(categoryFilter)) {
            loadAllLikedProducts(uid); // Load all liked products if "All Items" category is selected
            return;
        }

        // Filter liked products based on the selected category
        String startAt = categoryFilter;
        String endAt = categoryFilter + "\uf8ff";

        db.collection("Users")
                .document(uid)  // Navigate to the current user's document
                .collection("Likes")  // Fetch liked products from the "Likes" collection
                .whereGreaterThanOrEqualTo("category", startAt)
                .whereLessThan("category", endAt)
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
                            String category = document.getString("category");

                            productsList.add(new ProductShopModel(id, imageUrl, name, price, description, rate, numreviews, category));
                        }
                        productShopAdapter.updateData(productsList);  // Update adapter with filtered liked products
                    } else {
                        Log.d("Firestore", "Error fetching liked products: ", task.getException());
                    }
                });
    }

    private void loadAllLikedProducts(String uid) {
        db.collection("Users")
                .document(uid)  // Navigate to the current user's document
                .collection("Likes")  // Fetch liked products from the "Likes" collection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ProductShopModel> allLikedProductsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String imageUrl = document.getString("imageUrl");
                            String name = document.getString("name");
                            String price = document.getString("price");
                            String description = document.getString("description");
                            String rate = document.getString("rate");
                            String numreviews = document.getString("numreviews");
                            String category = document.getString("category");

                            allLikedProductsList.add(new ProductShopModel(id, imageUrl, name, price, description, rate, numreviews, category));
                        }
                        productShopAdapter.updateData(allLikedProductsList); // Update adapter with all liked products
                    } else {
                        Log.d("Firestore", "Error loading liked products: ", task.getException());
                    }
                });
    }
}