//package com.example.togoo.models;
//
//public class LocationCoordinates {
//    private String latitude;
//    private String longitude;
//
//    public LocationCoordinates() {}
//
//    public String getLatitude() {
//        return latitude;
//    }
//
//    public String getLongitude() {
//        return longitude;
//    }
//
//    public void setLatitude(String latitude) {
//        this.latitude = latitude;
//    }
//
//    public void setLongitude(String longitude) {
//        this.longitude = longitude;
//    }
//
//    public double getLatitudeAsDouble() {
//        try {
//            return Double.parseDouble(latitude);
//        } catch (Exception e) {
//            return 0.0;
//        }
//    }
//
//    public double getLongitudeAsDouble() {
//        try {
//            return Double.parseDouble(longitude);
//        } catch (Exception e) {
//            return 0.0;
//        }
//    }
//}




package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationCoordinates implements Parcelable {
    private String latitude;
    private String longitude;

    public LocationCoordinates() {}

    protected LocationCoordinates(Parcel in) {
        latitude = in.readString();
        longitude = in.readString();
    }

    public static final Creator<LocationCoordinates> CREATOR = new Creator<LocationCoordinates>() {
        @Override
        public LocationCoordinates createFromParcel(Parcel in) {
            return new LocationCoordinates(in);
        }

        @Override
        public LocationCoordinates[] newArray(int size) {
            return new LocationCoordinates[size];
        }
    };

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getLatitudeAsDouble() {
        try {
            return Double.parseDouble(latitude);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getLongitudeAsDouble() {
        try {
            return Double.parseDouble(longitude);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(latitude);
        dest.writeString(longitude);
    }
}