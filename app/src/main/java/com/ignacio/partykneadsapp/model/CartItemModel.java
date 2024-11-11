package com.ignacio.partykneadsapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class CartItemModel implements Parcelable {
    private String productName;
    private String productId;
    private String cakeSize; // Add this field
    private int quantity; // Assuming this is an integer
    private String totalPrice; // Assuming price is stored as a string
    private String rate; // Add this field if you want to display the rate
    private String numReviews; // Add this field if you want to display the number of reviews
    private String imageUrl;
    private boolean isSelected;
    private String originalPrice;

    // Required empty constructor for Firestore
    public CartItemModel() {
        // Initialize fields with reasonable defaults
        this.productId = "";
        this.productName = "";
        this.cakeSize = "Bento Cake"; // Default cake size
        this.quantity = 1; // Default quantity
        this.totalPrice = "₱0"; // Default price
        this.rate = "No rating"; // Default rate
        this.numReviews = "0"; // Default reviews count
        this.imageUrl = "";
        this.isSelected = false;
    }

    // Parcelable constructor
    protected CartItemModel(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        cakeSize = in.readString();
        quantity = in.readInt();
        totalPrice = in.readString();
        rate = in.readString();
        numReviews = in.readString();
        imageUrl = in.readString();
    }

    // Parcelable Creator
    public static final Creator<CartItemModel> CREATOR = new Creator<CartItemModel>() {
        @Override
        public CartItemModel createFromParcel(Parcel in) {
            return new CartItemModel(in);
        }

        @Override
        public CartItemModel[] newArray(int size) {
            return new CartItemModel[size];
        }
    };

    // Constructor with parameters for ease of use
    public CartItemModel(String id, String name, String size, int quantity, String totalPrice, String imageUrl) {
        this.productId = id;
        this.productName = name;
        this.cakeSize = size;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.imageUrl = imageUrl;
        this.rate = "No rating"; // Optional: you can modify this
        this.numReviews = "0"; // Optional: you can modify this
        this.isSelected = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeString(cakeSize);
        dest.writeInt(quantity);
        dest.writeString(totalPrice);
        dest.writeString(rate);
        dest.writeString(numReviews);
        dest.writeString(imageUrl);
    }



    // Getters and setters
    public String getProductId() { return productId; }

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

    public String getRate() {
        return rate;
    }

    public String getNumReviews() {
        return numReviews;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    // This method converts totalPrice to a double for calculations
    public double getTotalPriceAsDouble() {
        if (totalPrice != null) {
            try {
                // Remove any non-numeric characters (like "₱" or spaces) before parsing
                String numericValue = totalPrice.replaceAll("[^\\d.]", ""); // Keeps only digits and the decimal point
                return Double.parseDouble(numericValue); // Convert to double
            } catch (NumberFormatException e) {
                Log.e("CartItemModel", "Error parsing totalPrice: " + totalPrice, e);
            }
        }
        return 0.0; // Return 0 if price is null or invalid
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    // Setters to allow modifications
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setCakeSize(String cakeSize) {
        this.cakeSize = cakeSize;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setNumReviews(String numReviews) {
        this.numReviews = numReviews;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Optional: You can add any extra methods if needed
}
