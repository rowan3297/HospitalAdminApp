package com.example.hospitaladminapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "HOSPITAL_DB";
    private static final int DB_VERSION = 1;
    private static final String PATIENT_TABLE_NAME = "PATIENTSTABLE";
    private static final String PATIENT_ID_COL = "PATIENT_ID";
    private static final String PATIENT_NAME_COL = "FULL_NAME";
    private static final String PATIENT_PASSWORD_COL = "PASSWORD";
    private static final String PATIENT_DOB_COL = "PATIENT_DOB";

    //Doctor table information
    private static final String DOCTOR_TABLE_NAME = "DOCTORTABLE";
    private static final String DOCTOR_ID_COL = "DOCTOR_ID";
    private static final String DOCTOR_NAME_COL = "FULL_NAME";
    private static final String DOCTOR_PASSWORD_COL = "PASSWORD";
    private static final String DOCTOR_DOB_COL = "DOCTOR_DOB";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + PATIENT_TABLE_NAME + " ("
                + PATIENT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PATIENT_NAME_COL + " TEXT,"
                + PATIENT_PASSWORD_COL + " TEXT,"
                + PATIENT_DOB_COL + " TEXT)";

        String query2 = "CREATE TABLE " + DOCTOR_TABLE_NAME + " ("
                + DOCTOR_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DOCTOR_NAME_COL + " TEXT,"
                + DOCTOR_PASSWORD_COL + " TEXT,"
                + DOCTOR_DOB_COL + " TEXT)";

        // at last we are calling a exec sql
        // method to execute above sql querys
        db.execSQL(query);
        db.execSQL(query2);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    public Integer authenticatePatient(String userName, String userPassword, Integer DOB) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + PATIENT_TABLE_NAME + " WHERE " +
                PATIENT_NAME_COL + " = ? AND " + PATIENT_PASSWORD_COL + " = ? AND " + PATIENT_DOB_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userName, userPassword, DOB.toString()});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(PATIENT_ID_COL));
            cursor.close();
            return id;
        } else {
            cursor.close();
            return -1;
        }
    }

    public Integer authenticateDoctor(String userName, String userPassword, Integer DOB) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " +
                DOCTOR_NAME_COL + " = ? AND " + DOCTOR_PASSWORD_COL + " = ? AND " + DOCTOR_DOB_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userName, userPassword, DOB.toString()});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(DOCTOR_ID_COL));
            cursor.close();
            return id;
        } else {
            cursor.close();
            return -1;
        }
    }

    public boolean userExists(String userName, Integer DOB) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Check if user is in patient table
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME + " WHERE " +
                PATIENT_NAME_COL + " = ? AND " + PATIENT_DOB_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userName, DOB.toString() });
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }

        //Check if user is in doctor table
        String query2 = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " +
                DOCTOR_NAME_COL + " = ? AND " + DOCTOR_DOB_COL + " = ?";
        Cursor cursor2 = db.rawQuery(query, new String[]{userName, DOB.toString()});
        if (cursor2.getCount() > 0) {
            cursor2.close();
            return true;
        }
        //If the user doesnt exist then return false
        return false;
    }

    //Add a user based on the sign up code given (ref) to the correct table
    public Long addUser(String name, String password, Integer DOB, Integer ref) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (ref > 10000) {
            values.put(DOCTOR_NAME_COL, name);
            values.put(DOCTOR_PASSWORD_COL, password);
            values.put(DOCTOR_DOB_COL, DOB);
            return db.insert(DOCTOR_TABLE_NAME, null, values);
        } else {
            values.put(PATIENT_NAME_COL, name);
            values.put(PATIENT_PASSWORD_COL, password);
            values.put(PATIENT_DOB_COL, DOB);
            return db.insert(PATIENT_TABLE_NAME, null, values);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + PATIENT_TABLE_NAME);
        onCreate(db);
    }
}
