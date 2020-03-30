package com.example.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Station implements Serializable, Parcelable {

    private String code;
    private double lat;
    private double lng;
    private String fullName;

    public Station(String code, double lat, double lng, String fullName) {
        this.code = code;
        this.lat = lat;
        this.lng = lng;
        this.fullName = fullName;
    }

    protected Station(Parcel in) {
        code = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        fullName = in.readString();
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(code);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(fullName);
    }
}
