package com.ignacio.partykneadsapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.LocationAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentAddressBinding;

import java.util.ArrayList;
import java.util.List;

public class AddressFragment extends Fragment implements LocationAdapter.OnEditClickListener {

    private RecyclerView locationRecyclerView;
    private LocationAdapter locationAdapter;
    private List<String> activeLocations;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser cUser;

    FragmentAddressBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cUser = mAuth.getCurrentUser();

        // New Address Button Click
        binding.btnNewAddress.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_addressFragment_to_newAddressFragment);
        });

        // Back Button Click
        binding.btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);

            // Navigate to the homepage with the argument to load ShopFragment
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_addressFragment_to_homePageFragment, args1);
        });

        // Initialize RecyclerView for locations
        locationRecyclerView = binding.addressListRecyclerView;
        activeLocations = new ArrayList<>();
        locationAdapter = new LocationAdapter(activeLocations, "", this); // Pass 'this' as the OnEditClickListener
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationRecyclerView.setAdapter(locationAdapter);

        fetchUserNameAndLocations();
    }

    @Override
    public void onEditClick(int position) {
        // Navigate to the EditAddressFragment when edit button is clicked
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_addressFragment_to_editAddressFragment);
    }

    private void fetchUserNameAndLocations() {
        if (cUser != null) {
            String userId = cUser.getUid();
            db.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                String firstName = documentSnapshot.getString("First Name");
                String lastName = documentSnapshot.getString("Last Name");

                // Combine first name and last name to form the user's full name
                String userName = firstName + " " + lastName;

                // Update the location adapter with user's name
                locationAdapter.setUserName(userName);
                locationAdapter.notifyDataSetChanged();

                // Fetch active locations after getting the user's name
                fetchActiveLocations();
            }).addOnFailureListener(e -> {
                Log.w("AddressFragment", "Error fetching user name", e);
            });
        }
    }

    private void fetchActiveLocations() {
        if (cUser != null) {
            String userId = cUser.getUid();
            db.collection("Users").document(userId).collection("Locations")
                    .whereEqualTo("status", "Active")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String location = document.getString("location");
                                if (location != null) {
                                    activeLocations.add(location);
                                }
                            }
                            locationAdapter.notifyDataSetChanged(); // Notify adapter about data change
                        } else {
                            Log.w("AddressFragment", "Error getting active locations.", task.getException());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("AddressFragment", "Error fetching active locations", e);
                    });
        }
    }
}
