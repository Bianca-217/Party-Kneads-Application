package com.ignacio.partykneadsapp.model;

public class CompleteOrderModel {
    private String userName;
    private String contactNum;
    private String location;
    private String productName;
    private String cakeSize;
    private int quantity;
    private String totalPrice;
    private String status;
    private String imageUrl;

    // Constructor
    public CompleteOrderModel(String userName, String contactNum, String location,
                              String productName, String cakeSize, int quantity,
                              String totalPrice, String status, String imageUrl) {
        this.userName = userName;
        this.contactNum = contactNum;
        this.location = location;
        this.productName = productName;
        this.cakeSize = cakeSize;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContactNum() { return contactNum; }
    public void setContactNum(String contactNum) { this.contactNum = contactNum; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCakeSize() { return cakeSize; }
    public void setCakeSize(String cakeSize) { this.cakeSize = cakeSize; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getTotalPrice() { return totalPrice; }
    public void setTotalPrice(String totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
