package com.example.angel.earthquake;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Earthquake.class}, version = 1)
@TypeConverters({EarthquakeTypeConvertors.class})
public abstract class EarthquakeDatabase extends RoomDatabase {

    public abstract EarthquakeDAO earthquakeDAO();

}
