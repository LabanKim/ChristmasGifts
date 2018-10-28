package com.fusion.kim.askproject.Models;

public class Person {

    private String personName, deadline;
    private boolean bought;

    public Person() {
    }

    public Person(String personName, String deadline, boolean bought) {
        this.personName = personName;
        this.deadline = deadline;
        this.bought = bought;
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
}
