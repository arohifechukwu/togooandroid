package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class LocationCoordinates implements Parcelable {
    private Double latitude;
    private Double longitude;

    public LocationCoordinates() {}

    protected LocationCoordinates(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
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

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(Object latitude) {  // Changed to Object to handle String or Double
        if (latitude instanceof String) {
            try {
                this.latitude = Double.parseDouble((String) latitude);
            } catch (NumberFormatException e) {
                this.latitude = 0.0;
                Log.e("LocationCoordinates", "Invalid latitude format: " + latitude);
            }
        } else if (latitude instanceof Double) {
            this.latitude = (Double) latitude;
        } else {
            this.latitude = 0.0;
        }
    }

    public void setLongitude(Object longitude) {  // Changed to Object to handle String or Double
        if (longitude instanceof String) {
            try {
                this.longitude = Double.parseDouble((String) longitude);
            } catch (NumberFormatException e) {
                this.longitude = 0.0;
                Log.e("LocationCoordinates", "Invalid longitude format: " + longitude);
            }
        } else if (longitude instanceof Double) {
            this.longitude = (Double) longitude;
        } else {
            this.longitude = 0.0;
        }
    }

    public double getLatitudeAsDouble() {
        return latitude != null ? latitude : 0.0;
    }

    public double getLongitudeAsDouble() {
        return longitude != null ? longitude : 0.0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude != null ? latitude : 0.0);
        dest.writeDouble(longitude != null ? longitude : 0.0);
    }
}