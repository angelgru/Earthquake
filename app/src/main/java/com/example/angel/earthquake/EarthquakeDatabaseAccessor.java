package com.example.angel.earthquake;

import android.arch.persistence.room.Room;
import android.content.Context;

public class EarthquakeDatabaseAccessor {
    private static final String EARTHQUAKE_DB_NAME = "earthquake_db";
    private static EarthquakeDatabase earthquakeDatabase;

    public static EarthquakeDatabase getInstance(Context context) {
        if(earthquakeDatabase == null) {
            earthquakeDatabase = Room.databaseBuilder(context,
                    EarthquakeDatabase.class,
                    EARTHQUAKE_DB_NAME).build();
        }

        return earthquakeDatabase;
    }
}
