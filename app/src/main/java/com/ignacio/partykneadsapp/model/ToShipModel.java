package com.ignacio.partykneadsapp.model;

public class ToShipModel {
    private String productName;
    private String cakeSize;
    private String imageUrl;
    private int quantity;
    private String totalPrice;
    private String status;
    // No-argument constructor
    public ToShipModel() {
    }

    // Constructor with parameters
    public ToShipModel(String productName, String cakeSize, String imageUrl, int quantity, String totalPrice, String status) {
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public String getCakeSize() {
        return cakeSize;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTotalPrice() {
        return totalPrice;
    }
    public String getStatus() {
        return status;
    }

    // Optionally, you can also add setters if needed
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCakeSize(String cakeSize) {
        this.cakeSize = cakeSize;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

}
