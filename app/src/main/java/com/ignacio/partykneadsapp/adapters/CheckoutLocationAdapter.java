package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        String location = locations.get(position);
        holder.bind(location);
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
        private TextView userNameTextView; // TextView for displaying user name

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.location);
            userNameTextView = itemView.findViewById(R.id.txtUserName); // Assuming you have a TextView for the user name
        }

        public void bind(String location) {
            locationTextView.setText(location);
            if (userName != null) {
                userNameTextView.setText(userName); // Set the user's name
            } else {
                userNameTextView.setText(""); // Clear the user name if not set
            }
        }
    }
}
