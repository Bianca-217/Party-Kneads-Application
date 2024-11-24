package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.ChooseAddressModel;
import java.util.List;

public class ChooseAddressAdapter extends RecyclerView.Adapter<ChooseAddressAdapter.LocationViewHolder> {

    private List<ChooseAddressModel> locationList;
    private String userName;
    private Context context;

    // Constructor to initialize the adapter with location list and context
    public ChooseAddressAdapter(List<ChooseAddressModel> locationList, Context context) {
        this.locationList = locationList;
        this.context = context;
    }

    // Method to set the user name in the adapter
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chooseaddress_list, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        ChooseAddressModel location = locationList.get(position);

        // Set the full address and phone number
        String fullAddress = location.getFullAddress();
        holder.locationTextView.setText(fullAddress);
        holder.phoneNumberTextView.setText(location.getPhoneNumber());

        // Display userName from LocationModel if available, otherwise use the adapter's userName
        if (location.getUserName() != null && !location.getUserName().isEmpty()) {
            holder.userNameTextView.setText(location.getUserName());
        } else if (userName != null && !userName.isEmpty()) {
            holder.userNameTextView.setText(userName);
        } else {
            holder.userNameTextView.setText("User");
        }

        // Set up the RadioButton to reflect isSelected status
        holder.rdSelect.setChecked(location.isSelected());

        // Handle click on rdSelect (RadioButton)
        holder.rdSelect.setOnClickListener(view -> {
            // Deselect all locations
            for (ChooseAddressModel loc : locationList) {
                loc.setSelected(false);
            }
            // Select the current location
            location.setSelected(true);
            notifyDataSetChanged();

            // Update Firestore to reflect the new selection
            updateSelectedAddressInFirestore(location);
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    // Method to update Firestore when a new address is selected
    private void updateSelectedAddressInFirestore(ChooseAddressModel selectedLocation) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (selectedLocation.getDocumentId() == null || selectedLocation.getDocumentId().isEmpty()) {
            Log.e("ChooseAddressAdapter", "Selected address has no document ID!");
            return;
        }

        // Reference to user's Locations collection
        firestore.collection("Users")
                .document(userId)
                .collection("Locations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        String locationId = document.getId();
                        boolean isCurrentlySelected = locationId.equals(selectedLocation.getDocumentId());

                        // Update status based on whether this is the selected address
                        String newStatus = isCurrentlySelected ? "Active" : "Not Active";

                        firestore.collection("Users")
                                .document(userId)
                                .collection("Locations")
                                .document(locationId)
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    if (isCurrentlySelected) {
                                        Log.d("ChooseAddressAdapter", "Address marked as Active: " + locationId);
                                    } else {
                                        Log.d("ChooseAddressAdapter", "Address marked as Not Active: " + locationId);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("ChooseAddressAdapter", "Error updating address: " + locationId, e));
                    }
                })
                .addOnFailureListener(e -> Log.e("ChooseAddressAdapter", "Error retrieving locations", e));
    }

    // ViewHolder class to hold references to the views for each list item
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        TextView phoneNumberTextView;
        TextView userNameTextView; // TextView for user name
        RadioButton rdSelect; // RadioButton for selection

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.location);
            phoneNumberTextView = itemView.findViewById(R.id.contactNum);
            userNameTextView = itemView.findViewById(R.id.txtUserName);
            rdSelect = itemView.findViewById(R.id.rdSelect); // Initialize RadioButton
        }
    }
}