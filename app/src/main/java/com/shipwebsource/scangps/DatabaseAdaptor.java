package com.shipwebsource.scangps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseAdaptor
{
    private static final String TAG = "DatabaseAdaptor" ;

    private String LOCATION_TYPE_A = "Last Delivery";
    private String LOCATION_TYPE_B = "Primary Address";
    DatabaseHelper helper;





    public DatabaseAdaptor(Context context)
    {
        helper = new DatabaseHelper(context);


    }

    // These functions are responsible for getting and selecting data and returning them




    public long saveLocation(String HAWB, String location, String timestamp)
    {

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_PACKAGENUMBER, HAWB);
        contentValues.put(DatabaseHelper.COLUMN_LATITUDE_LONGITUDE, location);
        contentValues.put(DatabaseHelper.COLUMN_TIMESTAMP, timestamp);


        long id = db.insert(DatabaseHelper.TABLE_NAME_LOCATION, null, contentValues);

        return id;
    }

    public Cursor getAllData()
    {

        SQLiteDatabase db = helper.getWritableDatabase();
        final String query = "SELECT * "+
                " FROM "+DatabaseHelper.TABLE_NAME_LOCATION+" " +
                "GROUP BY "+DatabaseHelper.COLUMN_ID;

        return db.rawQuery(query, null);


    }





    ////////////////////////////////////////////////////////////////////////////////////




    public class DatabaseHelper extends SQLiteOpenHelper
    {
        // Increment Database Version every time you make changes to trigger onUpgrade
        public static final int DATABASE_VERSION        = 1;
        public static final String DATABASE_NAME        = "scanGPS.db";

        // THE TABLE NAMES DEFINITION
        public static final String TABLE_NAME_LOCATION   = "location";


        // THE TABLES DEFINITION

        // PACKAGE TABLE COLUMNS

        public static final String COLUMN_ID = "_locationId";
        public static final String COLUMN_LATITUDE_LONGITUDE = "coordinates";
        public static final String COLUMN_PACKAGENUMBER      = "packageNumber";
        public static final String COLUMN_TIMESTAMP = "timestamp";



        //CREATE TABLE SQL
        //CREATE PACKAGE TABLE SQL
        private static final String CREATE_LOCATION_TABLE = "create table " + TABLE_NAME_LOCATION + "("
                + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, "
                + COLUMN_PACKAGENUMBER + " VARCHAR(60) not null, "
                + COLUMN_LATITUDE_LONGITUDE + " VARCHAR(50) not null, "
                + COLUMN_TIMESTAMP + " TIMESTAMP not null);";


        // Drop table statement tables
        private static final String DROP_TABLE_PACKAGE = "DROP TABLE " + TABLE_NAME_LOCATION+ " IF EXISTS";




        /* Alter statements for the users who already have the app installed
            ------------------------------------------------------------------
            NB: Alter the create statements that are run inside the onCreate function.
            So, if a column is added as part of an update, add it in the create statement inside the
            onCreate function (for new users) and as an alter statement inside the onUpgrade function
            (for existing users).

            Don't forget to Increment Database Version every time you make changes to trigger onUpgrade
            ---------------------------------------------------------------------------------------- */

            // Example alter statement: (Copy and paste then call inside onUpgrade) copy & modify everything between arrows
                // --> private static final String DATABASE_ALTER = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " VARCHAR(30);"; <--

//        private static final String DATABASE_ALTER_MESSAGES = "ALTER TABLE " + DatabaseHelper.TABLE_NAME_MESSAGES + " ADD COLUMN " + DatabaseHelper.COLUMN_SYNCSTATUS + " VARCHAR(5) DEFAULT 'FALSE';";


        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }


        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try
            {

                db.execSQL(CREATE_LOCATION_TABLE);

            }

            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {


            Log.d(TAG, "Database upgrade in progress");

            // if the user's installed database version is older than the current version number
//            if (oldVersion < newVersion)
//            {
//                db.execSQL(DATABASE_ALTER_MESSAGES);
//            }

        }
    }
}
