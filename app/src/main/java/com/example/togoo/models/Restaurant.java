//package com.example.togoo.models;
//
//public class Restaurant {
//    private String name;
//    private String location;
//    private String imageUrl;
//
//    public Restaurant() {
//        // Default constructor required for Firebase
//    }
//
//    public Restaurant(String name, String location, String imageUrl) {
//        this.name = name;
//        this.location = location;
//        this.imageUrl = imageUrl;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getLocation() {
//        return location;
//    }
//
//    public String getImageUrl() {
//        return imageUrl;
//    }
//}





package com.example.togoo.models;

public class Restaurant {
    private String id;
    private String name;
    private String location;
    private String imageUrl;

    public Restaurant() {
        // Default constructor required for Firebase
    }

    public Restaurant(String id, String name, String location, String imageUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
}