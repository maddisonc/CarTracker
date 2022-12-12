package com.example.cartracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    // request code
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    // dependency: Google Play Services - needed for maps, FusedLocation (added in gradle)

    // instantiate ui elements
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_wayPointCounts;
    Button btn_newWayPoint, btn_showWayPointList, btn_showMap;
    Switch sw_locationupdates, sw_gps;

    // var that determines whether or not location is being tracked
    boolean updateOn = false;

    // current location
    Location currentLocation;

    // list of saved locations (waypoints)
    List<Location> savedLocations;

    // LocationRequest - class that influences FusedLocationProvider settings
    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    // three sources of location data: gps, cell tower (triangulation used to find dist from each tower), wifi (connected to wifi)
    // FusedLocationProvider: fuses all three sources - allows choice of very accurate data or less accurate data to save battery
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // assign values to ui elements
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);

        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        tv_wayPointCounts = findViewById(R.id.tv_countofCrumbs);
        btn_showMap = findViewById(R.id.btn_showMap);

        // set LocationRequest properties
        locationRequest = new LocationRequest();

        // how often to update location (default val)
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        // fastest - most frequent, less concerned with battery life
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        // accuracy level of location
        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // triggered whenever update interval is met (5 sec, 30 sec)
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save location
                updateUIValues(locationResult.getLastLocation());
            }
        }; // end locationCallback

        // clicking new waypoint
        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // get gps location


                // add location to list
                // gets access to singleton class
                MyApplication myApplication = (MyApplication)getApplicationContext();
                // gets list from singleton
                savedLocations  = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }
        }); // end clicked new waypoint

        // click show waypoints
        btn_showWayPointList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);
            }
        }); // end show waypoints in String list

        // click show waypoints on map
        btn_showMap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        }); // end show waypoints on map

        // switch between gps location and cell tower location
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    // most accurate - use gps
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Location from GPS.");
                } else {
                    // save battery by using cell tower and wifi location (less accurate)
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Location from cell tower data and wifi connection.");
                }
            } // end gps/cell/wifi switch onClick
        }); // end gps switch listener

        // on/off switch for location updates
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    // turn on location tracking
                    startLocationUpdates();
                } else {
                    // turn off location tracking
                    stopLocationUpdates();
                }
            }
        }); // end location updates switch

        // calls gps update - updates values to send to ui
        updateGPS();
    } // end onCreate

    // if location updates is set to off
    private void stopLocationUpdates() {
        tv_updates.setText("Location is not being tracked.");
        tv_lat.setText("Not tracking location.");
        tv_lon.setText("Not tracking location.");
        tv_speed.setText("Not tracking location.");
        tv_address.setText("Not tracking location.");
        tv_accuracy.setText("Not tracking location.");
        tv_altitude.setText("Not tracking location.");
        tv_sensor.setText("Not tracking location.");

        // turns off gps tracking
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    } // end stopLocationUpdates()

    // if location updates is set to on
    private void startLocationUpdates()
    {
        // needed for error resolve
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        tv_updates.setText("Location is being tracked.");
        // reuqests location to send to updateGPS method
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        // calls updates to send to ui
        updateGPS();
    } // end startLocationUpdates()

    // generated from main activity class, calls method when permissions are granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code = 99, if code is correct
        switch (requestCode)
        {
            case PERMISSIONS_FINE_LOCATION:
                // permissions are granted
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    updateGPS();
                }
                // no permissions
                else
                {
                    Toast.makeText(this, "This app requires location permissions.", Toast.LENGTH_SHORT).show();
                }
        } // end switch
    } // end check for permissions

    // calls updateUIValues if permissions are met
    private void updateGPS()
    {
        // ask user for location permissions
        //get current location from fused client
        // update ui
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // user gives permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>()
            {
                @Override
                public void onSuccess(Location location)
                {
                    // store values of location and put into ui
                    updateUIValues(location);
                    // saves current location to var
                    currentLocation = location;
                }
            });
        } // end if permissions given
        else
        {
            // no permissions yet
            // check if android build is recent enough (at least 23: M)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                  requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        } // end else no permissions
    } // end updateGPS method

    // updates text/UI values passed in
    private void updateUIValues(Location location)
    {
        // have to parse to String because values are doubles
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        // not all phones have the ability to check altitude
        if (location.hasAltitude())
        {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else
        {
            tv_altitude.setText("Altitude not available.");
        }

        // not all phones have the ability to check speed
        if (location.hasSpeed())
        {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else
        {
            tv_speed.setText("Speed not available.");
        }

        // used to translate location data into street address
        // can adjust location in emulator settings (set location of phone)
        Geocoder geocoder = new Geocoder(MainActivity.this);

        // catches exception in the case where the geocoder can not find the street address
        try
        {
            // top 1 address from lat and lon data
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e)
        {
            tv_address.setText("Unable to retrieve street address.");
        } // end catch exception

        // gets access to singleton class
        MyApplication myApplication = (MyApplication)getApplicationContext();
        // gets list from singleton
        savedLocations  = myApplication.getMyLocations();

        // output the number of waypoints saved in list (after every interval)
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));

    } // end updateUIValues method

} // end MainActivity