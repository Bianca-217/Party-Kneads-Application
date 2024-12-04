package com.ignacio.partykneadsapp.model;


public class OrderItemModel {
    private String productId;   // Field for productId
    private String productName;
    private String cakeSize;
    private String imageUrl;
    private int quantity;
    private String price;
    private String referenceID; // Field for referenceID

    // Default constructor required for Firestore
    public OrderItemModel() {
    }

    // Constructor with all fields
    public OrderItemModel(String productId, String productName, String cakeSize, String imageUrl, int quantity, String price, String referenceID) {
        this.productId = productId; // Initialize productId
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
        this.referenceID = referenceID; // Initialize referenceID
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCakeSize() {
        return cakeSize;
    }

    public void setCakeSize(String cakeSize) {
        this.cakeSize = cakeSize;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }
}
