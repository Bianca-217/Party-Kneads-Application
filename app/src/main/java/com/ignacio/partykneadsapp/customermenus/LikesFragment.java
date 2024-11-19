package com.ignacio.partykneadsapp.customermenus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.LikedProductAdapter;
import com.ignacio.partykneadsapp.model.LikedProductModel;

import java.util.ArrayList;
import java.util.List;

public class LikesFragment extends Fragment {

    private RecyclerView likerecyclerView;
    private LikedProductAdapter adapter;
    private List<LikedProductModel> likedProducts;
    private List<LikedProductModel> filteredProducts; // This list will hold the filtered products
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId;
    private SearchView likeSearchView;  // Declare the SearchView
    private Button btnCart; // Declare the Cart Button

    public LikesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_likes, container, false);

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up the RecyclerView
        likerecyclerView = view.findViewById(R.id.likerecyclerView);
        likerecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        likedProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>(likedProducts);  // Initialize filteredProducts as a copy of likedProducts
        adapter = new LikedProductAdapter(filteredProducts, product -> {
            // Handle product click (e.g., navigate to product details)
            Toast.makeText(getContext(), "Clicked on " + product.getName(), Toast.LENGTH_SHORT).show();
        });
        likerecyclerView.setAdapter(adapter);

        // Initialize the SearchView
        likeSearchView = view.findViewById(R.id.likesearchView);
        likeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // You can handle submission of the search query here if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the liked products list based on the search query
                filterLikedProducts(newText);
                return false;
            }
        });

        // Initialize the Cart Button
        btnCart = view.findViewById(R.id.btnCart); // Reference to the Cart button
        btnCart.setOnClickListener(v -> {
            // Create a bundle if you need to pass data (you can add items or anything else)
            Bundle bundle = new Bundle();
            bundle.putString("some_key", "some_value"); // Replace with your actual key-value pair

            // Navigate to CartFragment using Navigation Component and pass the bundle
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_likes_to_cart, bundle);
        });

        // Load liked products from Firebase
        loadLikedProducts();

        return view;
    }

    private void loadLikedProducts() {
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId != null) {
            firestore.collection("Users")
                    .document(userId)
                    .collection("Likes") // Assuming "Likes" collection holds liked products
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            likedProducts.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Convert the Firestore document into a LikedProductModel object
                                LikedProductModel product = document.toObject(LikedProductModel.class);
                                likedProducts.add(product);
                            }
                            // After loading the products, we should update the filtered list
                            filteredProducts.clear();
                            filteredProducts.addAll(likedProducts);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Failed to load liked products", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void filterLikedProducts(String query) {
        // Clear the filtered list and only add products that match the query
        filteredProducts.clear();
        if (query.isEmpty()) {
            // If the query is empty, show all liked products
            filteredProducts.addAll(likedProducts);
        } else {
            // Loop through the liked products and check if their name or description matches the query
            for (LikedProductModel product : likedProducts) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredProducts.add(product);
                }
            }
        }
        // Notify the adapter that the data has changed so the RecyclerView can update
        adapter.notifyDataSetChanged();
    }
}
