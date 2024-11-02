package com.ignacio.partykneadsapp.model;

public class OrderItem {
    private String productName;
    private int quantity;
    private String totalPrice;

    public OrderItem(String productName, int quantity, String totalPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTotalPrice() {
        return totalPrice;
    }
}
