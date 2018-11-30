package com.example.angel.earthquake;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.JsonReader;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EarthquakeUpdateJobService extends JobService {

    private static final String TAG = "EarthquakeUpdateJob ";
    private static final String UPDATE_JOB_TAG = "update_job";
    private static final String PERIODIC_JOB_TAG = "periodic_job";
    private static final String NOTIFICATION_CHANNEL = "earthquake";
    public static final int NOTIFICATION_ID = 1;

    private AsyncTask<Void, Void, Boolean> task = null;

    public static void scheduleUpdateJob(Context context) {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job job = dispatcher.newJobBuilder().setService(EarthquakeUpdateJobService.class)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK).setTag(UPDATE_JOB_TAG).build();
        dispatcher.schedule(job);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(JobParameters job) {
        Context context = this;

        task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                List<Earthquake> earthquakes = new ArrayList<>(0);

                try{
                    URL url = new URL("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson");
                    URLConnection urlConnection = url.openConnection();
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        InputStream in = httpURLConnection.getInputStream();
                        earthquakes = parseJson(in);
                    }

                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(job.getTag().equals(PERIODIC_JOB_TAG)) {
                    Earthquake largestEarthquake = findLargestNewEarthquake(earthquakes);
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    int magnitude = Integer.parseInt(
                            sharedPreferences.getString(PreferencesActivity.PREF_MIN_MAG, "3")
                    );
                    if(largestEarthquake != null && largestEarthquake.getMagnitude() >= magnitude) {
                        broadcastNotification(largestEarthquake);
                    }
                }

                EarthquakeDatabaseAccessor.getInstance(getApplication())
                        .earthquakeDAO().insertEarthquakes(earthquakes);

                scheduleNextUpdate(getApplicationContext(), job);

                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                jobFinished(job, !aBoolean);
            }
        };

        task.execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if(task != null)
            task.cancel(true);
        return true;
    }

    private void scheduleNextUpdate(Context context, JobParameters jobParameters) {
        if(jobParameters.getTag().equals(UPDATE_JOB_TAG)) {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            int updateFreg = Integer.parseInt(
                    preferences.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
            boolean autoUpdateChecked = preferences.getBoolean(
                    PreferencesActivity.PREF_AUTO_UPDATE, false
            );

            if(autoUpdateChecked) {
                FirebaseJobDispatcher firebaseJobDispatcher =
                        new FirebaseJobDispatcher(new GooglePlayDriver(context));

                Job job = firebaseJobDispatcher.newJobBuilder()
                        .setTag(PERIODIC_JOB_TAG)
                        .setService(EarthquakeUpdateJobService.class)
                        .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                        .setReplaceCurrent(true)
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(
                                updateFreg*60/2,
                                updateFreg*60
                        ))
                        .setLifetime(Lifetime.FOREVER)
                        .build();
                firebaseJobDispatcher.schedule(job);
            }
        }
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.earthquake_channel_name);

            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            NOTIFICATION_CHANNEL,
                            name,
                            NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void broadcastNotification(Earthquake earthquake) {
        createNotificationChannel();

        Intent startActivityIntent = new Intent(this, EarthquakeMainActivity.class);
        PendingIntent launchIntent = PendingIntent.getActivity(
                this,
                0,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);

        builder.setSmallIcon(R.drawable.ic_vibration_black_24dp)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(launchIntent)
                .setAutoCancel(true)
                .setShowWhen(true);
        builder.setWhen(earthquake.getDate().getTime())
                .setContentTitle("M:" + earthquake.getMagnitude())
                .setContentText(earthquake.getDetails())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(earthquake.getDetails()));

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private Earthquake findLargestNewEarthquake(List<Earthquake> newEarthquakes) {
        List<Earthquake> earthquakes = EarthquakeDatabaseAccessor.getInstance(this)
                .earthquakeDAO().loadAllEarthquakesBlocking();

        Earthquake largestEarthquake = null;

        for(Earthquake earthquake: newEarthquakes) {
            if(earthquakes.contains(earthquake))
                continue;
            if(largestEarthquake == null || largestEarthquake.getMagnitude() < earthquake.getMagnitude()) {
                largestEarthquake = earthquake;
            }
        }
        return largestEarthquake;
    }

    private List<Earthquake> parseJson(InputStream in) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        List<Earthquake> earthquakes = null;
        try{
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("features"))
                    earthquakes = readEarthquakeArray(reader);
                else
                    reader.skipValue();
            }
            reader.endObject();
        } finally {
            reader.close();
        }
        return earthquakes;
    }

    private List<Earthquake> readEarthquakeArray(JsonReader reader) throws IOException {
        List<Earthquake> earthquakes = new ArrayList<>();
        reader.beginArray();
        while(reader.hasNext()) {
            earthquakes.add(readEarthquake(reader));
        }
        reader.endArray();
        return earthquakes;
    }

    private Earthquake readEarthquake(JsonReader reader) throws IOException {
        String id = null;
        Location location = null;
        Earthquake earthquakeProperties = null;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if(name.equals("id"))
                id = reader.nextString();
            else if(name.equals("geometry"))
                location = readLocation(reader);
            else if(name.equals("properties"))
                earthquakeProperties = readEarthquakeProperties(reader);
            else
                reader.skipValue();
        }
        reader.endObject();

        return new Earthquake(id, earthquakeProperties.getDate(),
                earthquakeProperties.getDetails(),
                location,
                earthquakeProperties.getMagnitude(),
                earthquakeProperties.getLink());
    }

    private Location readLocation(JsonReader reader) throws IOException {
        Location location = null;
        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();
            if(name.equals("coordinates")) {
                List<Double> coords = readDoublesArray(reader);
                location = new Location("dd");
                location.setLatitude(coords.get(0));
                location.setLongitude(coords.get(1));
            } else
                reader.skipValue();
        }
        reader.endObject();
        return location;
    }

    private List<Double> readDoublesArray(JsonReader reader) throws IOException {
        List<Double> doubles = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            doubles.add(reader.nextDouble());
        }
        reader.endArray();
        return doubles;
    }

    private Earthquake readEarthquakeProperties(JsonReader reader) throws IOException {
        Date date = null;
        String details = null;
        double magnitude = -1;
        String link = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if(name.equals("time")) {
                long time = reader.nextLong();
                date = new Date(time);
            } else if(name.equals("place"))
                details = reader.nextString();
            else if (name.equals("url"))
                link = reader.nextString();
            else if(name.equals("mag"))
                magnitude = reader.nextDouble();
            else
                reader.skipValue();
        }
        reader.endObject();
        return new Earthquake(null, date, details, null, magnitude, link);
    }
}
