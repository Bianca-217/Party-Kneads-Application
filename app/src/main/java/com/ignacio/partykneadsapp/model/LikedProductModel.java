package com.ignacio.partykneadsapp.model;

public class LikedProductModel {

    private String id;
    private String name;
    private String price;
    private String rating;
    private int numReviews;
    private String imageUrl;
    private boolean isLiked;

    // No-argument constructor (required by Firebase for deserialization)
    public LikedProductModel() {
        // Firebase requires this constructor to instantiate the object
    }

    // Constructor with parameters
    public LikedProductModel(String id, String name, String price, String rating, int numReviews, String imageUrl, boolean isLiked) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.numReviews = numReviews;
        this.imageUrl = imageUrl;
        this.isLiked = isLiked;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(int numReviews) {
        this.numReviews = numReviews;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}