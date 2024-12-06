package com.example.hospitaladminapp;

import static android.app.PendingIntent.getActivity;
import static android.provider.Settings.System.getString;
import static java.util.Collections.emptyList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "HOSPITAL_DB";
    private static final int DB_VERSION = 1;

    //Patient table information
    private static final String PATIENT_TABLE_NAME = "PATIENTSTABLE";
    private static final String PATIENT_ID_COL = "PATIENT_ID";
    private static final String PATIENT_NAME_COL = "FULL_NAME";
    private static final String PATIENT_PASSWORD_COL = "PASSWORD";
    private static final String PATIENT_SALT_COL = "SALT";


    //Doctor table information
    private static final String DOCTOR_TABLE_NAME = "DOCTORTABLE";
    private static final String DOCTOR_ID_COL = "DOCTOR_ID";
    private static final String DOCTOR_NAME_COL = "FULL_NAME";
    private static final String DOCTOR_PASSWORD_COL = "PASSWORD";
    private static final String DOCTOR_SALT_COL = "SALT";

    //Appointment table information
    private static final String APPOINTMENT_TABLE_NAME = "APPOINTMENTSTABLE";
    private static final String APPOINTMENT_ID_COL = "APPOINTMENT_ID";
    private static final String APPOINTMENT_PATIENT_ID_COL = "PATIENT_ID";
    private static final String APPOINTMENT_DOCTOR_ID_COL = "DOCTOR_ID";
    private static final String APPOINTMENT_DATE_COL = "DATE";
    private static final String APPOINTMENT_TIME_COL = "TIME";
    private static final String APPOINTMENT_SIGNATURE_COL = "SIGNATURE";


    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // sql query to create the tables
        String query = "CREATE TABLE " + PATIENT_TABLE_NAME + " ("
                + PATIENT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PATIENT_NAME_COL + " TEXT,"
                + PATIENT_PASSWORD_COL + " TEXT,"
                + PATIENT_SALT_COL + " BLOB)";

        String query2 = "CREATE TABLE " + DOCTOR_TABLE_NAME + " ("
                + DOCTOR_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DOCTOR_NAME_COL + " TEXT,"
                + DOCTOR_PASSWORD_COL + " TEXT,"
                + DOCTOR_SALT_COL + " BLOB)";

        String query3 = "CREATE TABLE " + APPOINTMENT_TABLE_NAME + " ("
            + APPOINTMENT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + APPOINTMENT_PATIENT_ID_COL + " INTEGER,"
            + APPOINTMENT_DOCTOR_ID_COL + " INTEGER,"
            + APPOINTMENT_DATE_COL + " TEXT,"
            + APPOINTMENT_TIME_COL + " TEXT,"
            + APPOINTMENT_SIGNATURE_COL + " BLOB,"
            + "FOREIGN KEY(" + APPOINTMENT_PATIENT_ID_COL + ") REFERENCES " + PATIENT_TABLE_NAME + "(" + PATIENT_ID_COL + "),"
            + "FOREIGN KEY(" + APPOINTMENT_DOCTOR_ID_COL + ") REFERENCES " + DOCTOR_TABLE_NAME + "(" + DOCTOR_ID_COL + "))";

        // methods to execute above sql querys
        db.execSQL(query);
        db.execSQL(query2);
        db.execSQL(query3);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    //Authenticate a user based on their username and password
    public Integer authenticatePatient(String userName, String userPassword) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query to select the user based on their username and password
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME + " WHERE " + PATIENT_PASSWORD_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userPassword});
        String username;
        int userid = -1;
        //Check if the username matches the username in the database but decoded first
        while (cursor.moveToNext()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME_COL));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(PATIENT_ID_COL));
            try {
                username = AESHandler.decrypt(username);
                if (username.equals(userName)) {
                    cursor.close();
                    userid = id;
                }
            } catch (Exception e) {
                return -1;
            }
        }
        cursor.close();
        return userid;
    }

    //Authenticate a doctor based on their username and password
    public Integer authenticateDoctor(String userName, String userPassword) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " +
                DOCTOR_NAME_COL + " = ? AND " + DOCTOR_PASSWORD_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userName, userPassword});
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


    //Add a user based on the sign up code given (ref) to the correct table
    public Long addUser(String name, String password, Integer ref, byte[] salt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //Check the sign in code and add the user to the correct table
        if (ref > 10000) {
            values.put(DOCTOR_NAME_COL, name);
            values.put(DOCTOR_PASSWORD_COL, password);
            values.put(DOCTOR_SALT_COL, salt);
            return db.insert(DOCTOR_TABLE_NAME, null, values);
        } else {
            //Encrypt the username before adding it to the database
            String encryptedUsername;
            try {
                encryptedUsername = AESHandler.encrypt(name);
            } catch (Exception e) {
                return Long.valueOf(-1);
            }
            values.put(PATIENT_NAME_COL, encryptedUsername);
            values.put(PATIENT_PASSWORD_COL, password);
            values.put(PATIENT_SALT_COL, salt);
            return db.insert(PATIENT_TABLE_NAME, null, values);
        }
    }

    //Basic user exists check
    public boolean userExists(String userName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME ;
        Cursor cursor = db.rawQuery(query,null);
        boolean found = false;
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME_COL));
            try {
                username = AESHandler.decrypt(username);
                if (username.equals(userName)) {
                    cursor.close();
                    found = true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        cursor.close();
        String query2 = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " +
                DOCTOR_NAME_COL + " = ?";
        Cursor cursor2 = db.rawQuery(query2, new String[]{userName});
        if (cursor2.getCount() > 0) {
            cursor2.close();
            found = true;
        }
        return found;
    }

    //Return the salt for a given user to be able to encrypt their pssword correctly
    public byte[] getSalt(String userName){
        SQLiteDatabase db = this.getReadableDatabase();
        //Check if a user is a patient and return the correct salt
        int userId = getPatientId(userName);
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME + " WHERE " + PATIENT_ID_COL + " = " + userId;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            byte[] salt = cursor.getBlob(cursor.getColumnIndexOrThrow(PATIENT_SALT_COL));
            cursor.close();
            return salt;
        }
        //Check if a user is a doctor and return the correct salt
        String query2 = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " + DOCTOR_NAME_COL + " = ?";
        Cursor cursor2 = db.rawQuery(query2, new String[]{userName});
        if(cursor2.getCount() > 0){
            cursor2.moveToFirst();
            byte[] salt = cursor2.getBlob(cursor2.getColumnIndexOrThrow(DOCTOR_SALT_COL));
            cursor2.close();
            return salt;
        }
        return null;
    }

    //Get a list of all doctors names
    public ArrayList<String> getDoctors() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> doctors = new ArrayList<>();
        String query = "SELECT * FROM " + DOCTOR_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                doctors.add(cursor.getString(cursor.getColumnIndexOrThrow(DOCTOR_NAME_COL)));
            } while (cursor.moveToNext());
        }
        return doctors;
    }

    //Delete a specific doctor by their name and return if any rows were deleted
    public boolean deleteDoctor(String doctorName){
        SQLiteDatabase db = this.getWritableDatabase();
        int id = getDoctorId(doctorName);
        if(id == -1){
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT * FROM " + APPOINTMENT_TABLE_NAME + " WHERE " + APPOINTMENT_DOCTOR_ID_COL + " = ?", new String[]{String.valueOf(id)});
        if(cursor.getCount() > 0){
            return false;
        }

        String whereClause = DOCTOR_NAME_COL + " = ?";
        String[] whereArgs = new String[]{doctorName};

        // Delete the rows and check how many were deleted
        int rowsDeleted = db.delete(DOCTOR_TABLE_NAME, whereClause, whereArgs);
        return rowsDeleted > 0;
    }

    //Delete a specific patient by their name and return if any rows were deleted
    public boolean deletePatient(String patientName){
        SQLiteDatabase db = this.getWritableDatabase();
        int id = getPatientId(patientName);
        if (id == -1){
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT * FROM " + APPOINTMENT_TABLE_NAME + " WHERE " + APPOINTMENT_PATIENT_ID_COL + " = ?", new String[]{String.valueOf(id)});
        if(cursor.getCount() > 0){
            return false;
        }


        String whereClause = PATIENT_ID_COL + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        // Delete the rows and check how many were deleted
        int rowsDeleted = db.delete(PATIENT_TABLE_NAME, whereClause, whereArgs);
        return rowsDeleted > 0;
    }

    //Gets a list of all patients decrypted usernames
    public ArrayList<String> getPatients() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> patients = new ArrayList<>();
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    patients.add(AESHandler.decrypt(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME_COL))));
                } catch (Exception e) {
                    return null;
                }
            } while (cursor.moveToNext());
        }
        return patients;
    }

    //Gets a list of appointments
    public ArrayList<HashMap<String,String>> getAppointments(int id, String access){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String,String>> appointments = new ArrayList<>();
        Cursor cursor;
        //Check if the user is a doctor or a patient and get the correct appointments depending on their ID
        if (access.equals("doctor")){
            String query = "SELECT * FROM " + APPOINTMENT_TABLE_NAME + " WHERE " + APPOINTMENT_DOCTOR_ID_COL + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        } else if (access.equals("patient")) {
            String query = "SELECT * FROM " + APPOINTMENT_TABLE_NAME + " WHERE " + APPOINTMENT_PATIENT_ID_COL + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        } else {
            return appointments;
        }
        //Check if there are any appointments and get their patient usernames
        if (cursor.moveToFirst()) {
            do {
                String plainTextPatientName;
                try {
                    plainTextPatientName = getPatientName(cursor.getInt(cursor.getColumnIndexOrThrow(APPOINTMENT_PATIENT_ID_COL)));
                } catch (Exception e) {
                    plainTextPatientName = "decryption error";
                }

                //Add all the values to the appointment hashmap and add it to the array list
                HashMap<String,String> appointment = new HashMap<>();
                appointment.put("id", cursor.getString(cursor.getColumnIndexOrThrow(APPOINTMENT_ID_COL)));
                appointment.put("date", cursor.getString(cursor.getColumnIndexOrThrow(APPOINTMENT_DATE_COL)));
                appointment.put("time", cursor.getString(cursor.getColumnIndexOrThrow(APPOINTMENT_TIME_COL)));
                appointment.put("doctor", getDoctorName(cursor.getInt(cursor.getColumnIndexOrThrow(APPOINTMENT_DOCTOR_ID_COL))));
                appointment.put("patient", plainTextPatientName);
                appointments.add(appointment);
            } while (cursor.moveToNext());
            return appointments;
        }else{
            return appointments;
        }
    }

    //Get doctors name from their ID
    public String getDoctorName(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " + DOCTOR_ID_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        if(cursor.moveToFirst()){
            return cursor.getString(cursor.getColumnIndexOrThrow(DOCTOR_NAME_COL));
        } else {
            return null;
        }
    }

    //Get doctors name from their ID
    public String getPatientName(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME + " WHERE " + PATIENT_ID_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        if(cursor.moveToFirst()){
            try{
                return AESHandler.decrypt(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME_COL)));
            } catch (Exception e){
                return null;
            }
        } else {
            return null;
        }
    }

    //Get a patients ID from their name
    public int getPatientId(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        //Get all patients
        String query = "SELECT * FROM " + PATIENT_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        String username;
        int userid = -1;
        //Check if the username matches the username in the database but decoded first
        while (cursor.moveToNext()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME_COL));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(PATIENT_ID_COL));
            try {
                username = AESHandler.decrypt(username);
                if (username.equals(name)) {
                    cursor.close();
                    userid = id;
                }
            } catch (Exception e) {
                return -1;
            }
        }
        cursor.close();
        return userid;
    }

    //Get a doctors ID from their name
    public int getDoctorId(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DOCTOR_TABLE_NAME + " WHERE " + DOCTOR_NAME_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        if(cursor.moveToFirst()){
            return cursor.getInt(cursor.getColumnIndexOrThrow(DOCTOR_ID_COL));
        } else {
            return -1;
        }
    }

    //Create an appointment
    public boolean createAppointment(int patientId, int doctorId, String date, String time){
        //Check if there is already an appointment with the same date, time, patient and doctor
        String query = "SELECT * FROM " + APPOINTMENT_TABLE_NAME + " WHERE " + APPOINTMENT_DATE_COL + " = ? AND " + APPOINTMENT_TIME_COL + " = ? AND " + APPOINTMENT_PATIENT_ID_COL + " = ? AND " + APPOINTMENT_DOCTOR_ID_COL + " = ?";
        String[] args = new String[]{date, time, String.valueOf(patientId), String.valueOf(doctorId)};
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, args);
        if(cursor.getCount() > 0){
            return false;
        }
        //Create a signature for who edited an appointment
        String signature;
        try {
            signature = AESHandler.encrypt(String.valueOf(doctorId));
        } catch (Exception e) {
            return false;
        }

        //Add all the values, create the appointment and check if it was successful
        ContentValues values = new ContentValues();

        values.put(APPOINTMENT_PATIENT_ID_COL, patientId);
        values.put(APPOINTMENT_DOCTOR_ID_COL, doctorId);
        values.put(APPOINTMENT_DATE_COL, date);
        values.put(APPOINTMENT_TIME_COL, time);
        values.put(APPOINTMENT_SIGNATURE_COL, signature);

        long result = db.insert(APPOINTMENT_TABLE_NAME, null, values);
        return result != -1;
    }

    //Delete an appointment from the ID
    public boolean deleteAppointment(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = APPOINTMENT_ID_COL + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        int rowsDeleted = db.delete(APPOINTMENT_TABLE_NAME, whereClause, whereArgs);
        return rowsDeleted > 0;
    }

    //Update an appointment
    public boolean updateAppointment(int id, String date, String time, String doctorName){
        //Check if the entered doctor exists and if not return no changes were made
        int doctorId = getDoctorId(doctorName);
        if (doctorId == -1){
            return false;
        }
        //create a signature to update the doctor who was editing
        String signature;
        try {
            signature = AESHandler.encrypt(String.valueOf(doctorId));
        } catch (Exception e) {
            return false;
        }

        //Add all the values and update the row
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = APPOINTMENT_ID_COL + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        ContentValues values = new ContentValues();
        values.put(APPOINTMENT_DATE_COL, date);
        values.put(APPOINTMENT_TIME_COL, time);
        values.put(APPOINTMENT_DOCTOR_ID_COL, doctorId);
        values.put(APPOINTMENT_SIGNATURE_COL, signature);
        int rowsUpdated = db.update(APPOINTMENT_TABLE_NAME, values, whereClause, whereArgs);
        return rowsUpdated > 0;

    }

    // get a specific appointment from its ID
    public HashMap<String,String> getAppointmentById(int id){
        //find appointments with that id
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + APPOINTMENT_TABLE_NAME + " WHERE " + APPOINTMENT_ID_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        //If one is found save and return all the values
        if(cursor.moveToFirst()){
            String patientName;
            patientName = getPatientName(cursor.getInt(cursor.getColumnIndexOrThrow(APPOINTMENT_PATIENT_ID_COL)));
            HashMap<String,String> appointment = new HashMap<>();
            appointment.put("id", cursor.getString(cursor.getColumnIndexOrThrow(APPOINTMENT_ID_COL)));
            appointment.put("date", cursor.getString(cursor.getColumnIndexOrThrow(APPOINTMENT_DATE_COL)));
            appointment.put("time", cursor.getString(cursor.getColumnIndexOrThrow(APPOINTMENT_TIME_COL)));
            appointment.put("doctor", getDoctorName(cursor.getInt(cursor.getColumnIndexOrThrow(APPOINTMENT_DOCTOR_ID_COL))));
            appointment.put("patient", patientName);
            return appointment;
        } else{
            return null;
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + PATIENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DOCTOR_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + APPOINTMENT_TABLE_NAME);
        onCreate(db);
    }
}
