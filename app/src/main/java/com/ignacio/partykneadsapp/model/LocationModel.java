package com.ignacio.partykneadsapp.model;

public class LocationModel {
    private String houseNum;
    private String barangay;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private String location; // Keep location field for future use

    // Constructor
    public LocationModel(String houseNum, String barangay, String city, String postalCode, String phoneNumber) {
        this.houseNum = houseNum;
        this.barangay = barangay;
        this.city = city;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.location = null; // Initialize location as null
    }

    public LocationModel(String location) {
        this.location = location;
        this.houseNum = null;
        this.barangay = null;
        this.city = null;
        this.postalCode = null;
        this.phoneNumber = null;
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

    public String getLocation() {
        return location; // Add getter for location
    }

    // Method to concatenate address components
    public String getFullAddress() {
        if (houseNum != null && barangay != null && city != null && postalCode != null) {
            return houseNum + ", " + barangay + ", " + city + ", " + postalCode;
        } else if (location != null) {
            return location; // Return the location string if other components are not set
        }
        return "Address not available"; // Fallback if no address components are set
    }
}