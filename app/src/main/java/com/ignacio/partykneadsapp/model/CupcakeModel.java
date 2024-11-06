package com.ignacio.partykneadsapp.model;

public class CupcakeModel {
    private String size;
    private String pieces;
    private String price;

    // Constructor to initialize size, pieces, and price
    public CupcakeModel(String size, String pieces, String price) {
        this.size = size;
        this.pieces = pieces;
        this.price = price;
    }

    // Getter methods
    public String getSize() {
        return size;
    }

    public String getPieces() {
        return pieces;
    }

    public String getPrice() {
        return price;
    }
}

