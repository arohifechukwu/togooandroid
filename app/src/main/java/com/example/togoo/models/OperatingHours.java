package com.example.togoo.models;

public class OperatingHours {
    private String open;
    private String close;

    public OperatingHours() {} // required for Firebase

    public OperatingHours(String open, String close) {
        this.open = open;
        this.close = close;
    }

    public String getOpen() { return open; }
    public String getClose() { return close; }
}