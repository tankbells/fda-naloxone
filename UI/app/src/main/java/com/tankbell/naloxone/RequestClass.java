package com.tankbell.naloxone;

/**
 * Copyright 2016. TankBell Technologies.
 * All Rights Reserved .
 */

public class RequestClass {
    String firstName;
    String lastName;
    String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public RequestClass(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public RequestClass(String u) {
        this.userName = u;
    }

    public RequestClass() {
    }
}
