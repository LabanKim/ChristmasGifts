package com.fusion.kim.askproject.Models;

public class Gift {

    private String giftName, description;
    private double giftPrice;
    private boolean bought;
    private String imageOne, ImageTwo, imageThree;

    // Empty constructor required by firebaseui
    public Gift() {
    }

    //constructor to initialize gift data
    public Gift(String giftName, String description, double giftPrice, boolean bought, String imageOne, String imageTwo, String imageThree) {
        this.giftName = giftName;
        this.description = description;
        this.giftPrice = giftPrice;
        this.bought = bought;
        this.imageOne = imageOne;
        ImageTwo = imageTwo;
        this.imageThree = imageThree;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getGiftPrice() {
        return giftPrice;
    }

    public void setGiftPrice(double giftPrice) {
        this.giftPrice = giftPrice;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public String getImageOne() {
        return imageOne;
    }

    public void setImageOne(String imageOne) {
        this.imageOne = imageOne;
    }

    public String getImageTwo() {
        return ImageTwo;
    }

    public void setImageTwo(String imageTwo) {
        ImageTwo = imageTwo;
    }

    public String getImageThree() {
        return imageThree;
    }

    public void setImageThree(String imageThree) {
        this.imageThree = imageThree;
    }
}
