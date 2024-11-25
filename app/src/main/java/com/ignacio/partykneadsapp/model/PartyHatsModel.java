package com.ignacio.partykneadsapp.model;

public class PartyHatsModel {
    private int image;
    private String themeName;

    public PartyHatsModel(int image, String themeName) {
        this.image = image;
        this.themeName = themeName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getThemeName() { // getColorName
        return themeName;
    }

    public void setColorName(String colorName) {
        this.themeName = colorName;
    }
}
