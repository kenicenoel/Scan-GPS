package com.shipwebsource.scangps;

import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ScanGPS" ;
    private LocationBuddy buddy;
    private DatabaseAdaptor helper;
    private EditText scanField;
    private String hawb;
    private Location location;
    private String currentLocation;
    private Marker scannedLocation;
    private GoogleMap map;
    TextView exportData;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        buddy = new LocationBuddy(this, 1000);
        exportData = (TextView) findViewById(R.id.exportData);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(getResources().getColor(R.color.primaryTextColor));
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);

        final LinearLayout mapContainer = (LinearLayout) findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.INVISIBLE);



        // Creating a database adaptor
        helper = new DatabaseAdaptor(this);

        final MediaPlayer scanErrorSound = MediaPlayer.create(this, R.raw.scan_error);
        final MediaPlayer scanSuccessSound = MediaPlayer.create(this, R.raw.scan_success);
        final TextView prev = (TextView) findViewById(R.id.previouslyScanned);


        Button saveButton = (Button) findViewById(R.id.save);

        exportData.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Cursor export = helper.getAllData();
                String str = "Package # \t\t\t Location\t\t\t Timestamp\n\n";

                while (export.moveToNext())
                {
                    int index1 = export.getColumnIndex(DatabaseAdaptor.DatabaseHelper.COLUMN_PACKAGENUMBER);
                    int index2 = export.getColumnIndex(DatabaseAdaptor.DatabaseHelper.COLUMN_LATITUDE_LONGITUDE);
                    int index3 = export.getColumnIndex(DatabaseAdaptor.DatabaseHelper.COLUMN_TIMESTAMP);

                    String packageNumber = export.getString(index1);
                    String location = "\t\t\t"+export.getString(index2)+"\t\t\t";
                    String timestamp = export.getString(index3);
                    String newLine = "\n";
                    str = str+packageNumber+location+timestamp+newLine;


                } //End while

                String diskState = Environment.getExternalStorageState();
                if (diskState.equals(Environment.MEDIA_MOUNTED))
                {
                    try
                    {
                        // The disk is mounted and we can continue
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File newFile = new File(path, "listOfScannedLocations.txt");
                        FileWriter writer = new FileWriter(newFile, false);

                        writer.write(str);
                        writer.flush();
                        writer.close();

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                MainActivity.this);
                        builder.setTitle("Export data");
                        builder.setMessage("The file has been saved in the Downloads folder of the device with the name listOfScannedLocations.txt");

                        builder.setPositiveButton("Okay",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {

                                    }
                                });
                        builder.show();

                        export.close();


                    }

                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }



                }



        });

        scanField = (EditText) findViewById(R.id.scanEditTextView);
        scanField.requestFocus();
        scanField.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    //Get the scanned HAWB string
                    hawb = scanField.getText().toString();
                    Log.d(TAG, "'" + hawb + "' was entered");




                    Log.d(TAG, "Is Location Updated? " +String.valueOf(buddy.isLocationUpdated()));
                    Log.d(TAG, "Is client connected? "+String.valueOf(buddy.isClientConnected()));
                    if (buddy.isLocationUpdated() && buddy.isClientConnected())
                    {
                        Log.d(TAG, buddy.getUpdatedLocation().toString());
                        location = buddy.getUpdatedLocation();
                        currentLocation = (String.valueOf(location.getLatitude()+","+String.valueOf(location.getLongitude())));
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        if (map != null)
                        {
                            scannedLocation = map.addMarker(new MarkerOptions().position(latLng).title("Current position"));
                            //Move the camera instantly with a zoom of 15.
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                            // Zoom in, animating the camera.
                            map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);


                        }
                        mapContainer.setVisibility(View.VISIBLE);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "We've updated your location to: "+currentLocation, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                    prev.setText("You just scanned "+hawb);

                    //Clear the EditText. This way it is ready for another scanned HAWB
                    scanField.getText().clear();
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date());
                    helper.saveLocation(hawb, currentLocation, date);
                    scanSuccessSound.start();


                    return true;
                }
                return false;
            }
        });



        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        Log.d(TAG, "Is Location Updated? " +String.valueOf(buddy.isLocationUpdated()));
        Log.d(TAG, "Is client connected? "+String.valueOf(buddy.isClientConnected()));



    }



//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        buddy.disconnectFromGoogleApiClient();
//    }
//
//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        buddy.initialize();
//    }
//
//    @Override
//    protected void onStop()
//    {
//        super.onStop();
//        buddy.disconnectFromGoogleApiClient();
//    }
}
