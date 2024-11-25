package com.ignacio.partykneadsapp.model;

public class BannerColorModel {
    private int image;
    private String colorName;

    public BannerColorModel(int image, String colorName) {
        this.image = image;
        this.colorName = colorName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }
}
