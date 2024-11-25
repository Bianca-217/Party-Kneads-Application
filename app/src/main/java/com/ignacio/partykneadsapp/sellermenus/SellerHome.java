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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentSellerHomeBinding;

public class SellerHome extends Fragment {

    private FragmentSellerHomeBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        binding.myproduct.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_seller_HomePageFragment_to_myProductFragment);
        });

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadUserProfilePicture(userId);
        }


        // Check if binding is correctly initialized
        if (binding == null) {
            Log.e("SellerHome", "Binding is null");
            return;
        }

        // Navigate to MyProductFragment
        binding.btnmyProduct1.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView2);
            navController.navigate(R.id.action_seller_HomePageFragment_to_myProductFragment);
        });

        // Fetch total number of products from Firestore
        fetchTotalProductCount();
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
}
