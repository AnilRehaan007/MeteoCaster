package com.example.meteocaster.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ClimateInformation {

    @PrimaryKey(autoGenerate = true)
    int id;
    byte[] geoLocator;
    byte[] climate_data;


    public ClimateInformation( byte[] geoLocator, byte[] climate_data) {
        this.geoLocator = geoLocator;
        this.climate_data = climate_data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getGeoLocator() {
        return geoLocator;
    }

    public void setGeoLocator(byte[] geoLocator) {
        this.geoLocator = geoLocator;
    }

    public byte[] getClimate_data() {
        return climate_data;
    }

    public void setClimate_data(byte[] climate_data) {
        this.climate_data = climate_data;
    }
}
