package com.ignacio.partykneadsapp.model;

public class VoucherModel {
    private String id;        // Firestore document ID
    private String discount;  // Discount description
    private String status;    // Status of the voucher (e.g., "available", "claimed")

    public VoucherModel() {
        // Required for Firestore
    }

    public VoucherModel(String id, String discount, String status) {
        this.id = id;
        this.discount = discount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
