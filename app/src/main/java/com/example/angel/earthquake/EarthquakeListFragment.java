package com.example.angel.earthquake;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EarthquakeListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private List<Earthquake> mEarthquakes = new ArrayList<>();
    private int mMinimumMagnitude = 0;
    private RecyclerView recyclerView;
    private EarthquakeRecyclerViewAdapter recyclerViewAdapter =
            new EarthquakeRecyclerViewAdapter(mEarthquakes);
    private EarthquakeViewModel earthquakeViewModel;
    private SwipeRefreshLayout swipeToRefreshView;
    private OnListFragmentInteractionListener mListener;

    public EarthquakeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof  OnListFragmentInteractionListener)
            mListener = (OnListFragmentInteractionListener) context;
        else
            throw new RuntimeException("Activity doesn't implement OnListFragmentInteractionListener");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_earthquake_list, container, false);
        recyclerView = view.findViewById(R.id.list);
        swipeToRefreshView = view.findViewById(R.id.swiperefresh);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        earthquakeViewModel = ViewModelProviders.of(getActivity()).get(EarthquakeViewModel.class);
        earthquakeViewModel.getEarthquakes()
                .observe(this, (List<Earthquake> earthquakes) -> {
                    if(earthquakes != null)
                        setEarthquakes(earthquakes);
                });
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(recyclerViewAdapter);
        swipeToRefreshView.setOnRefreshListener(() -> {
            updateEarthquakes();
        });
    }


    public void setEarthquakes(List<Earthquake> earthquakes) {
        updateFromPreferences();
        mEarthquakes.clear();
        recyclerViewAdapter.notifyDataSetChanged();

        for(Earthquake earthquake: earthquakes) {
            if(earthquake.getMagnitude() >= mMinimumMagnitude) {
                if (!mEarthquakes.contains(earthquake)) {
                    mEarthquakes.add(earthquake);
                    recyclerViewAdapter.notifyItemInserted(mEarthquakes.indexOf(earthquake));
                }
            }

            if(mEarthquakes != null && mEarthquakes.size() > 0) {
                for (int i = mEarthquakes.size() - 1; i >= 0; i--) {
                    if (mEarthquakes.get(i).getMagnitude() < mMinimumMagnitude) {
                        mEarthquakes.remove(i);
                        recyclerViewAdapter.notifyItemRemoved(i);
                    }
                }
            }
            swipeToRefreshView.setRefreshing(false);
        }
    }

    protected void updateEarthquakes() {
        mListener.onListFragmentRefreshRequested();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(PreferencesActivity.PREF_MIN_MAG.equals(key)) {
            List<Earthquake> earthquakes = earthquakeViewModel.getEarthquakes().getValue();
            if(earthquakes != null)
                setEarthquakes(earthquakes);
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentRefreshRequested();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void updateFromPreferences() {
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getContext());
        mMinimumMagnitude = Integer.parseInt(
                sharedPreferences.getString(PreferencesActivity.PREF_MIN_MAG, "3"));
    }
}
