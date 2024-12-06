package com.example.hospitaladminapp;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends AppCompatActivity {

    private TextView testTextView;
    private DBHandler dbHandler;
    private EditText fullnameEditText, passwordEditText;
    private Button loginButtonPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Initialise the views
        loginButtonPress = findViewById(R.id.loginButton);
        fullnameEditText = findViewById(R.id.fullnameInput);
        passwordEditText = findViewById(R.id.passwordInput);
        testTextView = findViewById(R.id.textView3);

        dbHandler = new DBHandler(this);


        //Log the user in
        loginButtonPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = fullnameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                //Check if input is valid
                if (validateInput(fullname, password)) {

                    //Get the users salt
                    byte[] salt = dbHandler.getSalt(fullname);
                    if (salt == null) {
                        Toast.makeText(MainActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    Log.d("SALT", "Got the salt");

                    //Hash the password
                    String hashedPassword = hashPassword(password, salt);
//                    Log.d("SALT", "Hashed the password");

                    //Check if user is a doctor
                    if (dbHandler.authenticateDoctor(fullname, hashedPassword) != -1) {
                        int userID = dbHandler.authenticateDoctor(fullname, hashedPassword);
                        Intent i = new Intent(MainActivity.this, UserDashboardActivity.class);
                        i.putExtra("id",userID);
                        i.putExtra("access","doctor");
                        startActivity(i);
                        Log.d("SALT", "Its a doctor");

                    //Check if user is a patient
                    } else if (dbHandler.authenticatePatient(fullname, hashedPassword) != -1) {
                        int userID = dbHandler.authenticatePatient(fullname, hashedPassword);
                        Intent i = new Intent(MainActivity.this, UserDashboardActivity.class);
                        i.putExtra("id",userID);
                        i.putExtra("access","patient");
                        startActivity(i);
//                        Log.d("SALT", "its a patient");

                        //If the user is not found notify the user
                    } else {
                        Toast.makeText(MainActivity.this, "User doesnt exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //Method to validate input fields
    private boolean validateInput(String fullname, String password) {
        // Check if any field is empty
        return !fullname.isEmpty() && !password.isEmpty() &&
                password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$"); // Password must meet criteria
    }
    private String hashPassword(String password, byte[] salt) {
        try {
            // Specify the key specifications for PBKDF2 with sha 1
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            // Generate the hashed password
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Encode the hash as a Base64 string and return it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(hash);
            }
        } catch (Exception e) {
            // Log an error if hashing fails
//            Log.e("SECURE_LOGIN", "Password hashing failed", e);
            return null; // Return null if an exception occurs
        }
        return password;
    }

    public void register(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }


}