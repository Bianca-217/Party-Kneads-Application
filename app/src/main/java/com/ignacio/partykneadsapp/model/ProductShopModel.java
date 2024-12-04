package com.ignacio.partykneadsapp.model;

import java.io.Serializable;


public class ProductShopModel implements Serializable {
    private String id;
    private String imageUrl;
    private String name;
    private String price;
    private String description;
    private String rate;
    private String numreviews;
    private String category;
    private String productId;
    private long sold;  // Add this field for the 'sold' count

    // Constructor with 'sold' added
    public ProductShopModel(String id, String imageUrl, String name, String price, String description,
                            String rate, String numreviews, String category, long sold) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.description = description;
        this.rate = rate;
        this.numreviews = numreviews;
        this.category = category;
        this.productId = productId;
        this.sold = sold;  // Initialize the 'sold' field
    }

    // Getter for productId
    public String getProductId() {
        return productId;
    }

    // Getter methods
    public String getId() {
        return id;
    }

    public String getimageUrl() {
        return imageUrl;
    }

    public void setimageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getRate() {
        return rate;
    }

    public String getNumreviews() {
        return numreviews;
    }

    // Getter for the 'sold' field
    public long getSold() {
        return sold;
    }

    // Setter for 'sold' field (if needed)
    public void setSold(long sold) {
        this.sold = sold;
    }
}
