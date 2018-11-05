package com.fusion.kim.askproject.Models;

public class Person {

    private String personName, deadline;
    private boolean bought;
    private double totalAmount;

    // Empty constructor required by firebaseui
    public Person() {
    }

    //constructor to initialize person data
    public Person(String personName, String deadline, boolean bought, double totalAmount) {
        this.personName = personName;
        this.deadline = deadline;
        this.bought = bought;
        this.totalAmount = totalAmount;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
