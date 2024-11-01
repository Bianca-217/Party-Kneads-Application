package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<String> locationList;
    private String userName;
    private OnEditClickListener onEditClickListener;

    public LocationAdapter(List<String> locationList, String userName, OnEditClickListener onEditClickListener) {
        this.locationList = locationList;
        this.userName = userName;
        this.onEditClickListener = onEditClickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adress_list, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String location = locationList.get(position);
        holder.locationTextView.setText(location);
        holder.userNameTextView.setText(userName);

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

    public void setUserName(String userName) {
        this.userName = userName;
        notifyDataSetChanged();
    }

    // Interface for edit button click
    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        TextView userNameTextView;
        TextView btnEdit;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.location);
            userNameTextView = itemView.findViewById(R.id.txtUserName);
            btnEdit = itemView.findViewById(R.id.btnEdit); // Find btnEdit
        }
    }
}
