package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.NotificationViewModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationViewModel> notificationList;

    public NotificationAdapter(List<NotificationViewModel> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notifications, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        NotificationViewModel notification = notificationList.get(position);
        holder.orderStatusTextView.setText(notification.getOrderStatus());
        holder.userRateCommentTextView.setText(notification.getUserRateComment());

        // Load the image using Glide (or Picasso)
        Glide.with(holder.itemView.getContext())
                .load(notification.getCakeImageUrl())  // Image URL
                .into(holder.cakeImageView);  // ImageView where the cake image will be displayed
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView orderStatusTextView;
        TextView userRateCommentTextView;
        ImageView cakeImageView;  // ImageView for the cake image

        public NotificationViewHolder(View itemView) {
            super(itemView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatus);
            userRateCommentTextView = itemView.findViewById(R.id.userRateComment);
            cakeImageView = itemView.findViewById(R.id.cakeImage);  // Bind the ImageView
        }
    }
}
