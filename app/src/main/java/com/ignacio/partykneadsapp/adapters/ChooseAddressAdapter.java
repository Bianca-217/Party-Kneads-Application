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

public class ChooseAddressAdapter extends RecyclerView.Adapter<ChooseAddressAdapter.LocationViewHolder> {

    private List<LocationModel> locationList;
    private String userName; // Add userName field

    public ChooseAddressAdapter(List<LocationModel> locationList, OnEditClickListener onEditClickListener) {
        this.locationList = locationList;
    }

    public void setUserName(String userName) { // Method to set the user name
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
        LocationModel location = locationList.get(position);

        // Concatenate address and set to TextView
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
