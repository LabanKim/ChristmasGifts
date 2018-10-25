package com.fusion.kim.askproject.Models;

public class Person {

    private String personName, deadline;

    public Person() {
    }

    public Person(String personName, String deadline) {
        this.personName = personName;
        this.deadline = deadline;
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
}
