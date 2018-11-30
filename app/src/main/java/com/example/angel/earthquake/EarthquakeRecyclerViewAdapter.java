package com.example.angel.earthquake;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.angel.earthquake.databinding.ListItemEarthquakeBinding;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EarthquakeRecyclerViewAdapter
        extends RecyclerView.Adapter<EarthquakeRecyclerViewAdapter.ViewHolder> {

    private final List<Earthquake> earthquakes;
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private static final NumberFormat MAGNITUDE_FORMAT =
            new DecimalFormat("0.0");

    public EarthquakeRecyclerViewAdapter(List<Earthquake> earthquakes) {
        this.earthquakes = earthquakes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ListItemEarthquakeBinding binding = ListItemEarthquakeBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Earthquake earthquake = earthquakes.get(i);
        viewHolder.binding.setEarthquake(earthquake);
        viewHolder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return earthquakes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListItemEarthquakeBinding binding;

        public ViewHolder(ListItemEarthquakeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.setTimeformat(TIME_FORMAT);
            binding.setMagnitudeformat(MAGNITUDE_FORMAT);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + binding.details.getText() + "'";
        }
    }
}
