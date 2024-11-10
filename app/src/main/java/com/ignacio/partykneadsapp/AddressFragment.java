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

            // Fetch all locations without filtering by status
            db.collection("Users").document(userId).collection("Locations")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            activeLocations.clear(); // Clear existing locations
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("AddressFragment", "Fetched document: " + document.getId());

                                // Fetch fields using the updated names
                                String houseNum = document.getString("houseNumber");
                                String barangay = document.getString("barangay");
                                String city = document.getString("city");
                                String postalCode = document.getString("postalCode");
                                String phoneNumber = document.getString("contactNumber");
                                String userNameInLocation = document.getString("fullName");

                                // Debugging: Print out all the fields fetched
                                Log.d("AddressFragment", "HouseNum: " + houseNum);
                                Log.d("AddressFragment", "Barangay: " + barangay);
                                Log.d("AddressFragment", "City: " + city);
                                Log.d("AddressFragment", "PostalCode: " + postalCode);
                                Log.d("AddressFragment", "PhoneNumber: " + phoneNumber);
                                Log.d("AddressFragment", "UserNameInLocation: " + userNameInLocation);

                                // Check if the location entry has a userName, else fetch from the main user profile
                                if (userNameInLocation == null || userNameInLocation.isEmpty()) {
                                    fetchUserName(userId, () -> {
                                        LocationModel location = new LocationModel(houseNum, barangay, city + ", Laguna", postalCode, phoneNumber, userName);
                                        activeLocations.add(location);
                                        locationAdapter.setUserName(userName);
                                        locationAdapter.notifyDataSetChanged();
                                    });
                                } else {
                                    LocationModel location = new LocationModel(houseNum, barangay, city + ", Laguna", postalCode, phoneNumber, userNameInLocation);
                                    locationAdapter.setUserName(userNameInLocation);
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

    private void fetchUserName(String userId, Runnable onComplete) {
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Fetch full name from the updated field
                            String fullName = document.getString("fullName"); // Assuming the full name is stored in 'fullName'

                            // If fullName exists, use it, otherwise, fallback to empty string
                            userName = (fullName != null && !fullName.isEmpty()) ? fullName : "";

                            // Pass the fetched userName to the adapter
                            locationAdapter.setUserName(userName);
                        }
                    } else {
                        Log.w("AddressFragment", "Error fetching user name", task.getException());
                    }
                    // Ensure onComplete callback runs after the task is done
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.w("AddressFragment", "Error fetching user name", e);
                    // Ensure onComplete callback runs on failure as well
                    onComplete.run();
                });
    }
}