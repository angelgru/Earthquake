package com.example.angel.earthquake;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class EarthquakeMainActivity extends AppCompatActivity
        implements EarthquakeListFragment.OnListFragmentInteractionListener{

    private static final String TAG_LIST_FRAGMENT = "TAG_LIST_FRAGMENT";
    private static final int SHOW_PREFERENCES = 1;
    EarthquakeListFragment earthquakeListFragment;
    EarthquakeViewModel earthquakeViewModel;
    ViewPager viewPager;
    EarthquakeTabsPagerAdapter earthquakeTabsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        if(viewPager != null) {
            earthquakeTabsPagerAdapter = new EarthquakeTabsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(earthquakeTabsPagerAdapter);
            TabLayout tabLayout = findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(viewPager);
        }

        earthquakeViewModel = ViewModelProviders.of(this).get(EarthquakeViewModel.class);
    }

    @Override
    public void onListFragmentRefreshRequested() {
        updateEarthquakes();
    }

    private void updateEarthquakes() {
        earthquakeViewModel.loadEarthquakes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_preference:
                Intent intent = new Intent(this, PreferencesActivity.class);
                PackageManager pm = getPackageManager();
                if(intent.resolveActivity(pm) != null)
                    startActivityForResult(intent, SHOW_PREFERENCES);
                return true;
        }
        return false;
    }

    private class EarthquakeTabsPagerAdapter extends FragmentPagerAdapter {

        public EarthquakeTabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new EarthquakeListFragment();
                case 1:
                    return new EarthquakeMapFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.tab_list);
                case 1:
                    return getString(R.string.tab_map);
                default:
                    return null;
            }
        }
    }
}
