package com.fusion.kim.askproject.Models;

public class Gift {

    private String giftName, description;
    private double giftPrice;
    private boolean bought;

    public Gift() {
    }

    public Gift(String giftName, String description, double giftPrice, boolean bought) {
        this.giftName = giftName;
        this.description = description;
        this.giftPrice = giftPrice;
        this.bought = bought;
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
}
