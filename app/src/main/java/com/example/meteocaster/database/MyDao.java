package com.example.meteocaster.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MyDao {


    @Insert
    void insertion(ClimateInformation climateInformation);

    @Query("delete from ClimateInformation")
    void delete();

    @Query("select count(*) from ClimateInformation")
    int get_information();

    @Query("select * from ClimateInformation")
    List<ClimateInformation> getClimateData();

    @Query("select count(id) from ClimateInformation")

    int totalData();
}
