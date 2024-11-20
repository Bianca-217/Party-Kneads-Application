package com.ignacio.partykneadsapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;

public class CartItemModel implements Parcelable {
    private String productName;
    private String productId;
    private String cakeSize;
    private int quantity;
    private String totalPrice;
    private String rate;
    private long numReviews;  // Keep this as long to match Firestore Long
    private String imageUrl;
    private boolean isSelected;
    private String originalPrice;
    private String docId; // Firestore document ID

    // Required empty constructor for Firestore
    public CartItemModel() {
        this.productId = "";
        this.productName = "";
        this.cakeSize = "Bento Cake"; // Default cake size
        this.quantity = 1; // Default quantity
        this.totalPrice = "₱0"; // Default price
        this.rate = "No rating"; // Default rate
        this.numReviews = 0; // Default reviews count
        this.imageUrl = "";
        this.isSelected = false;
    }

    // Constructor with parameters for ease of use
    public CartItemModel(String id, String name, String size, int quantity, String totalPrice, String imageUrl) {
        this.productId = id;
        this.productName = name;
        this.cakeSize = size;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.imageUrl = imageUrl;
        this.rate = "No rating"; // Optional: you can modify this
        this.numReviews = 0; // Optional: you can modify this
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
        numReviews = in.readLong(); // Change to readLong to match long type
        imageUrl = in.readString();
    }
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
        dest.writeLong(numReviews); // Use writeLong for long type
        dest.writeString(imageUrl);
    }

    public String getDocId() {
        return docId;
    }

    // Getters and setters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getCakeSize() { return cakeSize; }
    public int getQuantity() { return quantity; }
    public String getTotalPrice() { return totalPrice; }
    public String getRate() { return rate; }
    public long getNumReviews() { return numReviews; } // Return as long
    public String getImageUrl() { return imageUrl; }
    public String getOriginalPrice() { return originalPrice; }
    public boolean isSelected() { return isSelected; }

    // Setters
    public void setNumReviews(long numReviews) { this.numReviews = numReviews; } // Accept long type
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }

    public void setDocId(String docId) {
        this.docId = docId;
    }



    // Optional: You can add any extra methods if needed
}
