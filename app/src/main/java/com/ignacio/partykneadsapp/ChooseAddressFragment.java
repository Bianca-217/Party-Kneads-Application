package com.ignacio.partykneadsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.ChooseAddressAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentChooseAddressBinding;
import com.ignacio.partykneadsapp.model.ChooseAddressModel;

import java.util.ArrayList;
import java.util.List;

public class ChooseAddressFragment extends DialogFragment {

    FragmentChooseAddressBinding binding;
    private String userName; // To store user name fetched
    private List<ChooseAddressModel> activeLocations = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChooseAddressBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set dialog width and height
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,  // Set to MATCH_PARENT for full width
                    ViewGroup.LayoutParams.WRAP_CONTENT  // Set to WRAP_CONTENT for dynamic height
            );

            // Remove gray background and make it transparent
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cUser = mAuth.getCurrentUser();

        // Fetch the active locations once the fragment is created
        if (cUser != null) {
            fetchActiveLocations();
        }

        // Set up the close button
        binding.btnClose.setOnClickListener(v -> {
            // Check if any address is selected
            boolean isAnyAddressSelected = false;
            for (ChooseAddressModel address : activeLocations) {
                if (address.isSelected()) {
                    isAnyAddressSelected = true;
                    break;
                }
            }

            // If no address is selected, show the dialog
            if (!isAnyAddressSelected) {
                // Inflate the custom dialog layout
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.close_dialog, null);

                // Create and configure the AlertDialog
                AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

                // Make the background of the dialog transparent
                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                // Set up the Cancel button
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(cancelView -> {
                    alertDialog.dismiss(); // Close the custom dialog
                });

                // Set up the Discard button
                dialogView.findViewById(R.id.btnDiscard).setOnClickListener(discardView -> {
                    alertDialog.dismiss(); // Close the custom dialog
                    dismiss(); // Close the DialogFragment
                });

                // Show the dialog
                alertDialog.show();
            } else {
                // If an address is selected, simply close the DialogFragment without showing the dialog
                dismiss(); // Close the DialogFragment
            }
        });

        // Set up RecyclerView with ChooseAddressAdapter
        ChooseAddressAdapter adapter = new ChooseAddressAdapter(activeLocations, getContext());
        binding.addressListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.addressListRecyclerView.setAdapter(adapter);
    }


    private void fetchActiveLocations() {
        if (cUser != null) {
            String userId = cUser.getUid();

            // Fetch the userName first
            fetchUserName(userId, () -> {
                // After fetching the userName, proceed with fetching locations
                db.collection("Users").document(userId).collection("Locations")
                        .whereIn("status", List.of("Active", "Not Active")) // Fetch both Active and Not Active locations
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                activeLocations.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentId = document.getId(); // Get the document ID
                                    String houseNum = document.getString("houseNum");
                                    String barangay = document.getString("barangay");
                                    String city = document.getString("city");
                                    String postalCode = document.getString("postalCode");
                                    String phoneNumber = document.getString("phoneNumber");
                                    String userNameInLocation = document.getString("userName");
                                    String location1 = document.getString("location");

                                    // Create ChooseAddressModel with documentId
                                    ChooseAddressModel addressModel;

                                    // If all address components are null, use 'location1'
                                    if (houseNum == null && barangay == null && city == null && postalCode == null && phoneNumber == null) {
                                        addressModel = new ChooseAddressModel(location1);
                                    } else {
                                        // If userName in location is empty, use fetched userName
                                        if (userNameInLocation == null || userNameInLocation.isEmpty()) {
                                            addressModel = new ChooseAddressModel(houseNum, barangay, city + ", Laguna", postalCode, phoneNumber, userName);
                                        } else {
                                            addressModel = new ChooseAddressModel(houseNum, barangay, city + ", Laguna", postalCode, phoneNumber, userNameInLocation);
                                        }
                                    }

                                    // Set the document ID
                                    addressModel.setDocumentId(documentId);

                                    // Add the address model to the list
                                    activeLocations.add(addressModel);
                                }

                                // Notify the adapter that data has changed
                                if (getContext() != null && binding.addressListRecyclerView.getAdapter() != null) {
                                    ((ChooseAddressAdapter) binding.addressListRecyclerView.getAdapter()).notifyDataSetChanged();
                                }
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
