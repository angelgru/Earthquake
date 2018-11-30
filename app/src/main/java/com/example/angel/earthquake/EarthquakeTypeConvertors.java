package com.example.angel.earthquake;

import android.arch.persistence.room.TypeConverter;
import android.location.Location;

import java.util.Date;

public class EarthquakeTypeConvertors {

    @TypeConverter
    public static Date dateFromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter()
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Location locationFromString(String location) {
        if(location != null){
            Location result = new Location("loc");
            String[] locationStrings = location.split(",");
            result.setLatitude(Double.parseDouble(locationStrings[0]));
            result.setLongitude(Double.parseDouble(locationStrings[1]));
            return result;
        }

        return null;
    }

    @TypeConverter
    public static String locationToString(Location location) {
        return location == null ? null : location.getLatitude() + "," + location.getLongitude();
    }
}
