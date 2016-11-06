package com.tankbell.naloxone;

/**
 * Copyright 2016. TankBell Technologies.
 * All Rights Reserved.
 */


public class UserDetailsCache {
    private static String userName;
    private static int age;
    private static int quantity;
    private static String phoneNo;
    private static Double latitude;
    private static Double longitude;
    private static String givenname;
    private static String emailAddr;
    private static String contactA;
    private static String contactB;
    private static String contactC;

    public static String getContactA() {
        return contactA;
    }

    public static void setContactA(String contactA) {
        UserDetailsCache.contactA = contactA;
    }

    public static String getContactB() {
        return contactB;
    }

    public static void setContactB(String contactB) {
        UserDetailsCache.contactB = contactB;
    }

    public static String getContactC() {
        return contactC;
    }

    public static void setContactC(String contactC) {
        UserDetailsCache.contactC = contactC;
    }

    public static String getEmailAddr() {
        return emailAddr;
    }

    public static void setEmailAddr(String ea) {
        UserDetailsCache.emailAddr = ea;
    }

    public static String getGivenname() {
        return givenname;
    }

    public static void setGivenname(String gn) {
        givenname = gn;
    }

    public static Double getLatitude() {
        return latitude;
    }

    public static void setLatitude(Double lat) {
        latitude = lat;
    }

    public static Double getLongitude() {
        return longitude;
    }

    public static void setLongitude(Double lng) {
        longitude = lng;
    }

    public static int getAge() {
        return age;
    }

    public static void setAge(int a) { age = a; }

    public static int getQuantity() {
        return quantity;
    }

    public static void setQuantity(int qt) {
        quantity = qt;
    }

    public static String getPhoneNo() {
        return phoneNo;
    }

    public static void setPhoneNo(String ph) {
        phoneNo = ph;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String un) {
        userName = un;
    }
}
