package com.shipwebsource.scangps;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class LocationBuddy implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    // Class Variables



    // Private instance variables
    private GoogleApiClient client;
    private Context context;
    private static boolean clientConnected = false;
    private static boolean locationUpdated = false;
    private static int updateInterval;
    private static Location lastLocation;
    private final static String TAG = "LocationBuddy";
    private static LocationRequest locationRequest;


    // Create the LocationBuddy Object and initialize it with the current context
    public LocationBuddy(Context c, int milliseconds)
    {
        this.context = c;
        this.client = null;
        initialize();
        this.updateInterval = milliseconds;
        Log.d(TAG, "LocationBuddy created with an update Interval of " + updateInterval + " milliseconds.");
    }




    public void initialize()
    {
        this.client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        client.connect();
        if (client.isConnected() || client.isConnecting())
        {
            clientConnected = true;
        }


    }



    protected void createLocationRequest()
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void getLastLocation()
    {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (lastLocation != null)
        {
           Log.d(TAG, "Found location successfully");
        }
        else
        {
            Log.d(TAG, "Could not get the location. Is your GPS enabled?");

        }

    }


    protected void startLocationUpdates()
    {

        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

    }


    protected void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }



    @Override
    public void onConnected(Bundle bundle)
    {
        createLocationRequest();
        getLastLocation();
        startLocationUpdates();

    }



    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(TAG, "The Google API client has been suspended");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastLocation = location;
        Log.d(TAG, "The location has been changed to " + location.toString());
        locationUpdated = true;
        Log.d(TAG,  "Quick stats: "+isClientConnected() +"/"+ isLocationUpdated());

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    public void disconnectFromGoogleApiClient()
    {
        stopLocationUpdates();
        client.disconnect();
        clientConnected = false;

    }

    public GoogleApiClient getClient()
    {
        return client;
    }


    public Location getUpdatedLocation()
    {
        return lastLocation;
    }

    public int getUpdateInterval()
    {
        return updateInterval;
    }


    public boolean isLocationUpdated()
    {
        return locationUpdated;
    }

    public boolean isClientConnected()
    {
        return clientConnected;
    }

    // Return a String description of this instance
    public String toString()
    {
        return "LocationBuddy[lastLocation=" + lastLocation + ",interval=" + updateInterval + ", clientConnected=" + clientConnected + ", locationUpdated="
                + locationUpdated + "]";
    }





}
