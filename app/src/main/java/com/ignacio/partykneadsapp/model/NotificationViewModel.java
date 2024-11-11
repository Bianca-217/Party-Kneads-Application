package com.ignacio.partykneadsapp.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationViewModel extends ViewModel {

    private final MutableLiveData<String> orderReferenceId = new MutableLiveData<>();
    private final MutableLiveData<String> notificationMessage = new MutableLiveData<>();

    // Set the orderReferenceId
    public void setOrderReferenceId(String orderRefId) {
        orderReferenceId.setValue(orderRefId);
    }

    // Get the orderReferenceId
    public LiveData<String> getOrderReferenceId() {
        return orderReferenceId;
    }

    // Set the notification message
    public void setNotificationMessage(String message) {
        notificationMessage.setValue(message);
    }

    // Get the notification message
    public LiveData<String> getNotificationMessage() {
        return notificationMessage;
    }

    // Method to send a notification when the admin clicks "Accept" and the order is prepared for shipping
    public void sendToShipNotification(String orderId, String userId) {
        String notificationText = "Your order with ID: " + orderId + " is now being prepared for shipping.";

        // Update LiveData with notification details
        setNotificationMessage(notificationText);

        // Notify the Fragment (or any listener) about the new order ID
        setOrderReferenceId(orderId);
    }


    // Method to send a notification when the admin clicks "Deliver" and the order is out for delivery
    public void sendToReceiveNotification(String orderId, String userId) {
        // This could be a Firebase Cloud Messaging (FCM) push notification or a local notification
        String notificationText = "Your order with ID: " + orderId + " is now out for delivery.";

        // Set notification message
        setNotificationMessage(notificationText);

        // Here you would implement the FCM or Firestore logic to actually notify the user.
        // For now, we just update the LiveData.
        sendNotification(orderId, notificationText);
    }

    // Private helper method to simulate sending a notification
    private void sendNotification(String orderId, String notificationText) {
        // For now, we'll simply print the notification message for debugging purposes
        // You can replace this with logic to push the notification using Firebase or other services
        System.out.println("Sending notification: " + notificationText);

        // Also, updating the notification message LiveData
        setNotificationMessage(notificationText);
    }
}
