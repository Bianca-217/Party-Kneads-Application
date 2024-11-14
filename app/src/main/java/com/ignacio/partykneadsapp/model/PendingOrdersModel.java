package com.ignacio.partykneadsapp.model;

public class PendingOrdersModel {
    private String userName;
    private String contactNum;
    private String location;
    private String productName;
    private String cakeSize;
    private int quantity;
    private String totalPrice;
    private String imageURL;
    private String orderId; // Add this field for the order ID
    private String status; // Add this field for the order status
    private String userEmail;

    // Constructor
    public PendingOrdersModel(String userName, String contactNum, String location, String productName,
                              String cakeSize, int quantity, String totalPrice, String imageURL, String status, String orderId, String userEmail) {
        this.userName = userName;
        this.contactNum = contactNum;
        this.location = location;
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.imageURL = imageURL;
        this.status = status;
        this.userEmail = userEmail;
        this.orderId = orderId; // Set the order ID in the constructor
    }

    // Getters and Setters
    public String getUserName() { return userName; }
    public String getContactNum() { return contactNum; }
    public String getLocation() { return location; }
    public String getProductName() { return productName; }
    public String getCakeSize() { return cakeSize; }
    public int getQuantity() { return quantity; }
    public String getTotalPrice() { return totalPrice; }
    public String getImageURL() { return imageURL; }
    public String getOrderId() { return orderId; } // Getter for orderId
    public void setStatus(String status) { this.status = status; } // Setter for status
    public String getUserEmail() {
        return userEmail;
    }
}


