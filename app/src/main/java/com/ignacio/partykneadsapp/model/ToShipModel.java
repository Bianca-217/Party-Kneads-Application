package com.ignacio.partykneadsapp.model;

import java.util.List;

public class ToShipModel {
    private String referenceId;
    private String status;
    private String totalPrice;
    private String productName;
    private String cakeSize;
    private String imageUrl;
    private int quantity;
    private List<OrderItemModel> items; // Nested list of individual items

    // Default constructor required for Firestore
    public ToShipModel() {
    }

    // Constructor with all fields
    public ToShipModel(String referenceId, String status, String totalPrice, String productName,
                       String cakeSize, String imageUrl, int quantity, List<OrderItemModel> items) {
        this.referenceId = referenceId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.items = items;
    }

    // Getters and Setters
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
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

    public List<OrderItemModel> getItems() {
        return items;
    }

    public void setItems(List<OrderItemModel> items) {
        this.items = items;
    }
}
