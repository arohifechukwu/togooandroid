package com.example.togoo.models;

public class Admin {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String status;
    private String imageURL;

    public Admin() {}

    public Admin(String id, String name, String email, String phone, String address,
                 String role, String status, String imageURL) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.status = status;
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

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}