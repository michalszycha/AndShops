package com.example.newapp2;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//public class Data implements Parcelable {
public class Data {

    @SerializedName("type")
    @Expose
    public String type;

    //public String getType() {
    //    return type;
    //}

    //elements of address
    @SerializedName("address")
    @Expose
    public Address address = new Address();

    @SerializedName("lat")
    @Expose
    public String lat;

    @SerializedName("lon")
    @Expose
    public String lon;

    public static class Address {
        @SerializedName("road")
        @Expose
        public String road;

        @SerializedName("house_number")
        @Expose
        public String house_number;
    }

    //elements of namedatails
    @SerializedName("namedetails")
    @Expose
    public Namedetails namedetails = new Namedetails();

    public static class Namedetails {
        @SerializedName("name")
        @Expose
        public String name;
    }

    //elements of extratags
    @SerializedName("extratags")
    @Expose
    public Extratags extratags = new Extratags();

    public static class Extratags {
        @SerializedName("opening_hours")
        @Expose
        public String opening_hours;
    }

}
