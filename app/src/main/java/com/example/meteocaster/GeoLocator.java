package com.example.meteocaster;

import java.io.Serializable;

public class GeoLocator implements Serializable {
    private double lat;
    private double lon;
    private String name;
    private String country;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
