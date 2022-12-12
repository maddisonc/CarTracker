package com.example.cartracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity
{
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    // request code
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    // dependency: Google Play Services - needed for maps, FusedLocation (added in gradle)

    // instantiate ui elements
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_locationupdates, sw_gps;

    // var that determines whether or not location is being tracked
    boolean updateOn = false;

    // LocationRequest - class that influences FusedLocationProvider settings
    LocationRequest locationRequest;

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

        // set LocationRequest properties
        locationRequest = new LocationRequest();

        // how often to update location (default val)
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        // fastest - most frequent, less concerned with battery life
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        // accuracy level of location
        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // switch between gps location and cell tower location
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (sw_gps.isChecked())
                {
                    // most accurate - use gps
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Retrieving location from GPS.");
                }
                else
                {
                    // save battery by using cell tower and wifi location (less accurate)
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Retrieving location from cell tower data and wifi connection.");
                }
            } // end gps/cell/wifi switch onClick
        }); // end gps switch listener
      } // end onCreate

    // generated from main activity class, calls method when permissions are granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    updateGPS();
                }
                else
                {
                    Toast.makeText(this, "This app requires location permissions.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void updateGPS()
      {
          // ask user for location permissions
          //get current location from fused client
          // update ui
          fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
          if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
          {
              // user gives permission
              fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                  @Override
                  public void onSuccess(Location location) {
                      // store values of location and put into ui
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

} // end MainActivity