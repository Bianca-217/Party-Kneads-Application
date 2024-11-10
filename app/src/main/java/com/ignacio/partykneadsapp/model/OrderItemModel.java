package com.ignacio.partykneadsapp.model;

public class OrderItemModel {
    private String productName;
    private String cakeSize;
    private String imageUrl;
    private int quantity;
    private String price;
    private String referenceID;

    // Default constructor required for Firestore
    public OrderItemModel() {
    }

    // Constructor with all fields
    public OrderItemModel(String productName, String cakeSize, String imageUrl, int quantity, String price) {
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
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

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }
}
