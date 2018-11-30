package com.example.angel.earthquake;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class EarthquakeViewModel extends AndroidViewModel {

    private static final String TAG = "EarthquakeUpdate";

    private LiveData<List<Earthquake>> earthquakes;

    public EarthquakeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Earthquake>> getEarthquakes() {
        if(earthquakes == null) {
            earthquakes = EarthquakeDatabaseAccessor.getInstance(getApplication())
                    .earthquakeDAO().loadAllEarthquakes();
            loadEarthquakes();
        }
        return earthquakes;
    }

    void loadEarthquakes() {
        EarthquakeUpdateJobService.scheduleUpdateJob(getApplication());
    }
}
