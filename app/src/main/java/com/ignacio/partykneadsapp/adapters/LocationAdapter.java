package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.LocationModel;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationModel> locationList;
    private OnEditClickListener onEditClickListener;
    private String userName; // Add userName field

    public LocationAdapter(List<LocationModel> locationList, OnEditClickListener onEditClickListener) {
        this.locationList = locationList;
        this.onEditClickListener = onEditClickListener;
    }

    public void setUserName(String userName) { // Method to set the user name
        this.userName = userName;
    }


    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adress_list, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationModel location = locationList.get(position);

        // Concatenate address and set to TextView
        String fullAddress = location.getFullAddress();
        holder.locationTextView.setText(fullAddress);
        holder.phoneNumberTextView.setText(location.getPhoneNumber());
        holder.userNameTextView.setText(userName); // Set the user name here

        // Set click listener for btnEdit
        holder.btnEdit.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        TextView phoneNumberTextView;
        TextView userNameTextView; // TextView for user name
        TextView btnEdit;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.location);
            phoneNumberTextView = itemView.findViewById(R.id.contactNum);
            userNameTextView = itemView.findViewById(R.id.txtUserName); // Finding the user name TextView
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
