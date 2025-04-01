package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Customer implements Parcelable {
    private String id;
    private String name;
    private String phone;
    private String address;

    // Required empty constructor for Firebase
    public Customer() {}

    public Customer(String id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Parcelable constructor
    protected Customer(Parcel in) {
        id = in.readString();
        name = in.readString();
        phone = in.readString();
        address = in.readString();
    }

    public static final Creator<Customer> CREATOR = new Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}