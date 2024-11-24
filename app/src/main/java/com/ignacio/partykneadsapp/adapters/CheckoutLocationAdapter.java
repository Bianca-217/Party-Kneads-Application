package com.ignacio.partykneadsapp.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.ChooseAddressFragment;
import com.ignacio.partykneadsapp.R;

import java.util.List;

public class CheckoutLocationAdapter extends RecyclerView.Adapter<CheckoutLocationAdapter.LocationViewHolder> {
    private List<String> locations;
    private String userName; // Store the user's name

    public CheckoutLocationAdapter(List<String> locations) {
        this.locations = locations;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adress_list, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String locationInfo = locations.get(position); // This will be location + phoneNumber
        holder.bind(locationInfo);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    // Method to set the user's name in the adapter
    public void setUserName(String userName) {
        this.userName = userName;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView locationTextView;
        private TextView userNameTextView;
        private TextView contactNumber;
        private TextView btnEdit;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.location);
            userNameTextView = itemView.findViewById(R.id.txtUserName);
            contactNumber = itemView.findViewById(R.id.contactNum);
            btnEdit = itemView.findViewById(R.id.btnEdit);

            // Set OnClickListener for the Edit button
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    String selectedLocation = locations.get(position);

                    // Create a new instance of ChooseAddressFragment
                    ChooseAddressFragment chooseAddressFragment = new ChooseAddressFragment();

                    // Pass data to the fragment via a Bundle
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedLocation", selectedLocation);
                    chooseAddressFragment.setArguments(bundle);

                    // Open the ChooseAddressFragment as a DialogFragment
                    chooseAddressFragment.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "ChooseAddressFragment");

                    // Optional: Show a toast with the selected location
                    Toast.makeText(v.getContext(), "Selected Location: " + selectedLocation, Toast.LENGTH_SHORT).show();
                }
            });
        }




    public void bind(String locationInfo) {
            String[] parts = locationInfo.split(" - "); // Split into location and phone number
            locationTextView.setText(parts[0]); // Set the location text
            if (parts.length > 1) {
                contactNumber.setText(parts[1]); // Set the phone number text
            }
            if (userName != null) {
                userNameTextView.setText(userName); // Set the user name if available
            }
        }
    }
}
