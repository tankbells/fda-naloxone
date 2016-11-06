package com.tankbell.naloxone;

/*
 * Copyright 2016. TankBell Technologies.
 * All Rights Reserved.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.Toast;

/*
 * Tracks the device location.
 * Background Service.
 */
public class GPSTracker extends Service {

    public static final String TAG = "GPSTracker";

    private static final long MIN_TIME = 1000;
    private static final long MIN_DIST = 0; // In meters


    private LocationListener listener;
    private LocationManager locationManager;

    boolean gpsEnabled = false;
    boolean networkEnabled = false;

    private UserDetailsCache cache;

    private DynamoDBManager.UserDetails userDetails = new DynamoDBManager.UserDetails();
    private DynamoDBManager.UserQuant userQuant = new DynamoDBManager.UserQuant();
    private DynamoDBManager.EmergencyContacts userEmerContacts = new DynamoDBManager.EmergencyContacts();

    public static AmazonClientManager clientManager = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        clientManager = new AmazonClientManager(this);
        Log.i("GPSTrackerV2.0", "ServiceCreated");
        //new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USER_DETAILS);
        // Implement the Location Listener CallBack .
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("GPSTrackerV2.0", "Latitude Changed: " + Double.toString(location.getLatitude()));
                Log.i("GPSTrackerV2.0", "Longitude Changed: " + Double.toString(location.getLongitude()));
                // Update location in DynamoDB
                cache.setLatitude(location.getLatitude());
                cache.setLongitude(location.getLongitude());
                cache.setUserName(AppHelper.getCurrUser());
                //new DynamoDBManagerTask()
                //        .execute(DynamoDBManagerType.UPDATE_USER);
                // Cascade DynamoDB calls starting with READ_USER_DETAILS
                new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USER_DETAILS);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Get the Location Manager.
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !networkEnabled) {
            // If location services are not turned on
            // take the user to the settings page.
            // The user has to turn on location services for this app.
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            // The location can now be updated only the
            // next time the location changes .
            // TODO : This interaction can be made better.
        } else {
            try {
                if (gpsEnabled) {
                    // Request the Location .
                    // Even small movements will be tracked as of now .
                    // Change MIN_DIST and MIN_TIME to update these values .
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, listener);
                    if (locationManager != null) {
                        // The first time get the location using the last known location .
                        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null) {
                            Double latitude = loc.getLatitude();
                            Double longitude = loc.getLongitude();
                            Log.i("GPSTrackerv2.0", "Latitude: " + Double.toString(latitude));
                            Log.i("GPSTrackerv2.0", "Longitude: " + Double.toString(longitude));
                            // Update location in DynamoDB
                            cache.setLatitude(latitude);
                            cache.setLongitude(longitude);
                            cache.setUserName(AppHelper.getCurrUser());
                            //new DynamoDBManagerTask()
                            //        .execute(DynamoDBManagerType.UPDATE_USER);
                            new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USER_DETAILS);
                        }
                    }
                } else if (networkEnabled) {
                    // Request the Location .
                    // Even small movements will be tracked as of now .
                    // Change MIN_DIST and MIN_TIME to update these values .
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, listener);
                    if (locationManager != null) {
                        // The first time get the location using the last known location .
                        Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (loc != null) {
                            Double latitude = loc.getLatitude();
                            Double longitude = loc.getLongitude();
                            Log.i("GPSTrackerv2.0", "Latitude: " + Double.toString(latitude));
                            Log.i("GPSTrackerv2.0", "Longitude: " + Double.toString(longitude));
                            // Update location in DynamoDB
                            cache.setLatitude(latitude);
                            cache.setLongitude(longitude);
                            cache.setUserName(AppHelper.getCurrUser());
                            //new DynamoDBManagerTask()
                            //        .execute(DynamoDBManagerType.UPDATE_USER);
                            new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USER_DETAILS);
                        }
                    }
                }
            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }

    private class DynamoDBManagerTask extends
            AsyncTask<GPSTracker.DynamoDBManagerType, Void, GPSTracker.DynamoDBManagerTaskResult> {

        protected GPSTracker.DynamoDBManagerTaskResult doInBackground(
                GPSTracker.DynamoDBManagerType... types) {

            GPSTracker.DynamoDBManagerTaskResult result = new GPSTracker.DynamoDBManagerTaskResult();

            if (types[0] == GPSTracker.DynamoDBManagerType.UPDATE_USER) {
                String tableStatus = DynamoDBManager.getTableStatusGPSService();

                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.updateUserDetailsGPS(cache);
                }
            } else if (types[0] == DynamoDBManagerType.READ_USER_DETAILS) {
                String tableStatus = DynamoDBManager.getNLXUserDetailTableStatusFromGPSService();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userDetails = DynamoDBManager.getUserDetailsFromGPSService(AppHelper.getCurrUser());
                    Log.d("Test " , "Test " + userDetails);
                    Log.d("Test " , "Test " + AppHelper.getCurrUser());
                }
            } else if (types[0] == DynamoDBManagerType.READ_USER_QUANT) {
                String tableStatus = DynamoDBManager.getNLXQuantTableStatusFromGPSService();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userQuant = DynamoDBManager.getUserQuantFromGPSService(AppHelper.getCurrUser());
                    if (userQuant == null) {
                        //user has probably not updated the age and quantity yet.
                        // initialize them to 18 and 1 respectively.
                        userQuant = new DynamoDBManager.UserQuant();
                        userQuant.setAge(18);
                        userQuant.setQuantity(1);
                    }
                }
            } else if (types[0] == DynamoDBManagerType.READ_USER_EMERGENCY) {
                String tableStatus = DynamoDBManager.getNLXEmerContactTableStatusFromGPSService();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userEmerContacts = DynamoDBManager.getUserEmerContactFromGPSService(AppHelper.getCurrUser());
                    if (userEmerContacts == null) {
                        //user has probably not updated the age and quantity yet.
                        // initialize them to 18 and 1 respectively.
                        userEmerContacts = new DynamoDBManager.EmergencyContacts();
                        userEmerContacts.setContactA("+00000000000");
                        userEmerContacts.setContactB("+00000000000");
                        userEmerContacts.setContactC("+00000000000");
                    }
                }
            }

            return result;
        }

        protected void onPostExecute(GPSTracker.DynamoDBManagerTaskResult result) {

            if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == GPSTracker.DynamoDBManagerType.UPDATE_USER) {
                //Toast.makeText(GPSTracker.this,
                //        "Users inserted successfully!", Toast.LENGTH_SHORT).show();
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.READ_USER_DETAILS) {
                //Log.d("Location","Location " + cache.getLatitude());
                new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USER_EMERGENCY);
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                        && result.getTaskType() == DynamoDBManagerType.READ_USER_EMERGENCY) {
                    new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USER_QUANT);
                } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                        && result.getTaskType() == DynamoDBManagerType.READ_USER_QUANT) {
                    //Log.d("Location","Location " + cache.getLatitude());
                    cache.setGivenname(userDetails.getGivenname());
                    cache.setPhoneNo(userDetails.getPhoneNumber());
                    cache.setEmailAddr(userDetails.getEmailId());
                    cache.setAge(userQuant.getAge());
                    cache.setQuantity(userQuant.getQuantity());
                    cache.setContactA(userEmerContacts.getContactA());
                    cache.setContactB(userEmerContacts.getContactB());
                    cache.setContactC(userEmerContacts.getContactC());
                new DynamoDBManagerTask().execute(DynamoDBManagerType.UPDATE_USER);
            }
        }
    }

    private enum DynamoDBManagerType {
        UPDATE_USER,READ_USER_DETAILS,READ_USER_QUANT,READ_USER_EMERGENCY
    }

    private class DynamoDBManagerTaskResult {
        private GPSTracker.DynamoDBManagerType taskType;
        private String tableStatus;

        public GPSTracker.DynamoDBManagerType getTaskType() {
            return taskType;
        }

        public void setTaskType(GPSTracker.DynamoDBManagerType taskType) {
            this.taskType = taskType;
        }

        public String getTableStatus() {
            return tableStatus;
        }

        public void setTableStatus(String tableStatus) {
            this.tableStatus = tableStatus;
        }
    }
}