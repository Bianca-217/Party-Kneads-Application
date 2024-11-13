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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.LocationAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentAddressBinding;
import com.ignacio.partykneadsapp.model.LocationModel;

import java.util.ArrayList;
import java.util.List;

public class AddressFragment extends Fragment implements LocationAdapter.OnEditClickListener {

    private RecyclerView locationRecyclerView;
    private LocationAdapter locationAdapter;
    private List<LocationModel> activeLocations; // Change this to List<LocationModel>
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser cUser;

    FragmentAddressBinding binding;
    String userName;
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
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_addressFragment_to_homePageFragment, args1);
        });

        // Initialize RecyclerView for locations
        locationRecyclerView = binding.addressListRecyclerView;
        activeLocations = new ArrayList<>(); // List of LocationModel
        locationAdapter = new LocationAdapter(activeLocations, this); // Pass the correct type
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationRecyclerView.setAdapter(locationAdapter);

        fetchActiveLocations();
    }

    @Override
    public void onEditClick(int position) {
        // Navigate to the EditAddressFragment when edit button is clicked
        LocationModel location = activeLocations.get(position); // Get LocationModel object

        // Create a bundle to pass userName and location to EditAddressFragment
        Bundle bundle = new Bundle();
        bundle.putString("location", location.getFullAddress()); // Pass the full address
        bundle.putString("username", userName); // Pass the full address
        bundle.putString("phonenumber", location.getPhoneNumber()); // Pass the full address


        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_addressFragment_to_editAddressFragment, bundle);
    }

    private void fetchActiveLocations() {
        if (cUser != null) {
            String userId = cUser.getUid();

            // Fetch the userName first
            fetchUserName(userId, () -> {
                // After fetching the userName, proceed with fetching locations
                db.collection("Users").document(userId).collection("Locations")
                        .whereEqualTo("status", "Active")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                activeLocations.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String houseNum = document.getString("houseNum");
                                    String barangay = document.getString("barangay");
                                    String city = document.getString("city");
                                    String postalCode = document.getString("postalCode");
                                    String phoneNumber = document.getString("phoneNumber");
                                    String userNameInLocation = document.getString("userName");
                                    String location1 = document.getString("location");

                                    // If all address components are null, use 'location1'
                                    if (houseNum == null && barangay == null && city == null && postalCode == null && phoneNumber == null) {
                                        activeLocations.add(new LocationModel(location1));
                                    } else {
                                        // If userName in location is empty, use fetched userName
                                        if (userNameInLocation == null || userNameInLocation.isEmpty()) {
                                            activeLocations.add(new LocationModel(houseNum, barangay, city + ", Laguna", postalCode, phoneNumber, userName));
                                        } else {
                                            activeLocations.add(new LocationModel(houseNum, barangay, city + ", Laguna", postalCode, phoneNumber, userNameInLocation));
                                        }
                                    }
                                }
                                // Set the userName in adapter and notify changes after locations are loaded
                                locationAdapter.setUserName(userName);
                                locationAdapter.notifyDataSetChanged();
                            } else {
                                Log.w("AddressFragment", "Error getting active locations.", task.getException());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w("AddressFragment", "Error fetching active locations", e);
                        });
            });
        }
    }

    private void fetchUserName(String userId, Runnable onComplete) {
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String firstName = document.getString("First Name");
                            String lastName = document.getString("Last Name");
                            userName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        }
                    } else {
                        Log.w("AddressFragment", "Error fetching user name", task.getException());
                    }
                    onComplete.run(); // Run the onComplete callback
                })
                .addOnFailureListener(e -> {
                    Log.w("AddressFragment", "Error fetching user name", e);
                    onComplete.run(); // Ensure to run the callback even on failure
                });
    }

}