package com.example.cartracker;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application
{
    // set up as singleton - only one instance
    // sets up list of locations for breadcrumbs and saved locations

    private static MyApplication singleton;

    private List<Location> myLocations;

    public List<Location> getMyLocations()
    {
        return myLocations;
    }

    public void setMyLocations(List<Location> myLocations)
    {
        this.myLocations = myLocations;
    }

    public MyApplication getInstance()
    {
        return singleton;
    }

    public void onCreate()
    {
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
    } // end onCreate
} // end MyApplication class