package com.ignacio.partykneadsapp.model;

import java.io.Serializable;

public class SearchProduct implements Serializable {
    private String name;
    private String description;

    // Default constructor
    public SearchProduct() {
    }

    // Constructor to initialize fields
    public SearchProduct(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}