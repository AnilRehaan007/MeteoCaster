package com.example.meteocaster.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ClimateInformation.class},exportSchema = false,version = 1)
public abstract class DataBase extends RoomDatabase {

    private static DataBase dataBase;

    public static synchronized DataBase getInstance(Context context)
    {

        if(dataBase==null)
        {
            dataBase= Room.databaseBuilder(context.getApplicationContext(),DataBase.class,
                    "MeteoCaster_Database").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }

        return dataBase;

    }
    public abstract MyDao dao();


}
