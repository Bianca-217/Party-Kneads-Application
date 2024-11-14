package com.ignacio.partykneadsapp.customermenus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.NotificationAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentNotificationBinding;
import com.ignacio.partykneadsapp.model.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationViewModel> notificationList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        notificationRecyclerView = binding.notificationRecyclerView;
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationRecyclerView.setAdapter(notificationAdapter);

        // Call a method to load notifications
        loadNotifications();
    }

    // Getter for notificationList
    public List<NotificationViewModel> getNotificationList() {
        return notificationList;
    }

    // Getter for notificationAdapter
    public NotificationAdapter getNotificationAdapter() {
        return notificationAdapter;
    }

    public void addNotification(NotificationViewModel notification) {
        notificationList.add(notification);
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();  // Force update of the RecyclerView
        }
    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Assuming you have a "Notifications" collection in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(uid)
                .collection("Notifications") // Collection of notifications
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the current notifications list before adding new ones
                        notificationList.clear();

                        // Iterate over the query results and create NotificationViewModels dynamically
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String orderStatus = document.getString("orderStatus");
                            String userRateComment = document.getString("userRateComment");
                            String imageUrl = document.getString("imageUrl");

                            // Create a new NotificationViewModel with the fetched data
                            NotificationViewModel notification = new NotificationViewModel(orderStatus, userRateComment, imageUrl);

                            // Add the notification to the list
                            notificationList.add(notification);
                        }

                        // Notify the adapter of the data change
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("NotificationFragment", "Error getting notifications: ", task.getException());
                    }
                });
    }
}

