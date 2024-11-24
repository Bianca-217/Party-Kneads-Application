package com.ignacio.partykneadsapp.model;

public class ChooseAddressModel {
    private String documentId;
    private String houseNum;
    private String barangay;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private String location; // Location for Firestore use or for full address display
    private String userName;
    private boolean isSelected;

    // Constructor with full address details
    public ChooseAddressModel(String houseNum, String barangay, String city, String postalCode, String phoneNumber, String userName) {
        this.houseNum = houseNum;
        this.barangay = barangay;
        this.city = city;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.location = generateLocation(houseNum, barangay, city, postalCode); // Generate location string
    }

    // Constructor with only 'location' field
    public ChooseAddressModel(String location) {
        this.location = location;
        this.houseNum = null;
        this.barangay = null;
        this.city = null;
        this.postalCode = null;
        this.phoneNumber = null;
        this.userName = null; // Initialize userName as null
        this.isSelected = false;
    }

    // Constructor to use when assigning from Firestore (including documentId)
    public ChooseAddressModel(String documentId, String houseNum, String barangay, String city, String postalCode, String phoneNumber, String location, String userName, boolean isSelected) {
        this.documentId = documentId;
        this.houseNum = houseNum;
        this.barangay = barangay;
        this.city = city;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.userName = userName;
        this.isSelected = isSelected;
    }

    // Getter for location
    public String getLocation() {
        return location != null ? location : getFullAddress();  // Use location if it's set, otherwise fall back to the full address.
    }

    // Getters and Setters
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

    public String getUserName() {
        return userName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Helper method to create a location string from full address components
    private String generateLocation(String houseNum, String barangay, String city, String postalCode) {
        if (houseNum != null && barangay != null && city != null && postalCode != null) {
            return houseNum + ", " + barangay + ", " + city + ", " + postalCode;
        }
        return null;  // If not all address components are provided, return null
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
