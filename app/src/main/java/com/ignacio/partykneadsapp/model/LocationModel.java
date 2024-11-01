package com.ignacio.partykneadsapp.model;

public class LocationModel {
    private String houseNum;
    private String barangay;
    private String city;
    private String postalCode;
    private String phoneNumber;

    // Constructor
    public LocationModel(String houseNum, String barangay, String city, String postalCode, String phoneNumber) {
        this.houseNum = houseNum;
        this.barangay = barangay;
        this.city = city;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getHouseNum() {
        return houseNum;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Method to concatenate address components
    public String getFullAddress() {
        return houseNum + ", " + barangay + ", " + city + ", " + postalCode;
    }
}
