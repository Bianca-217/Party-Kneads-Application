package com.ignacio.partykneadsapp.model;

public class CancelCustomerModel {
    private String productName;
    private String cakeSize;
    private int quantity;
    private String totalPrice;
    private String imageUrl;
    private String status;  // This will be "Order has been Cancelled"
    private String reason;  // This will be "Order has been Cancelled"

    public CancelCustomerModel(String productName, String cakeSize, int quantity, String totalPrice, String imageUrl, String status, String reason) {
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.imageUrl = imageUrl;
        this.status = status;
        this.reason = reason;
    }

    public String getProductName() {
        return productName;
    }

    public String getCakeSize() {
        return cakeSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
