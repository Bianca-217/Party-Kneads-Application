package com.ignacio.partykneadsapp.model;


import java.io.Serializable;

public class PopularModel implements Serializable {
    private String name;
    private String sold;
    private String price;
    private String imageUrl; // Updated to hold image URL from Firestore

    public PopularModel(String name, String sold, String price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.sold = sold;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getSold() {
        return sold;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
