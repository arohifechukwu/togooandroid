package com.example.togoo.models;

public class Driver {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String status;
    private String driverLicense;
    private String vehicleRegistration;
    private String imageURL;

    public Driver() {}

    public Driver(String id, String name, String email, String phone, String address,
                  String role, String status, String driverLicense, String vehicleRegistration, String imageURL) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.status = status;
        this.driverLicense = driverLicense;
        this.vehicleRegistration = vehicleRegistration;
        this.imageURL = imageURL;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDriverLicense() { return driverLicense; }
    public void setDriverLicense(String driverLicense) { this.driverLicense = driverLicense; }

    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}