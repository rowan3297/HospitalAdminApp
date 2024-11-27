package com.example.hospitaladminapp;

import android.content.Intent;
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

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends AppCompatActivity {

    private TextView testTextView;
    private DBHandler dbHandler;
    private EditText fullnameEditText, passwordEditText, dobEditText;
    private Button loginButtonPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loginButtonPress = findViewById(R.id.loginButton);
        fullnameEditText = findViewById(R.id.fullnameInput);
        passwordEditText = findViewById(R.id.passwordInput);
        dobEditText = findViewById(R.id.dobInput);
        testTextView = findViewById(R.id.textView3);



        //Log the user in
        loginButtonPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = fullnameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String stringDob = dobEditText.getText().toString().trim();
                Integer dob = Integer.parseInt(stringDob);

                String hashedPassword = hashPassword(password);

                //Check if input is valid
                if (validateInput(fullname, password,dob)) {
                    //Check if user is a doctor
                    if (dbHandler.authenticateDoctor(fullname, hashedPassword, dob) != -1) {
                        int userID = dbHandler.authenticateDoctor(fullname, hashedPassword, dob);
                        Intent i = new Intent(MainActivity.this, UserDashboardActivity.class);
                        i.putExtra("id",userID);
                        i.putExtra("permissions","doctor");
                        startActivity(i);
                    } else if (dbHandler.authenticatePatient(fullname, hashedPassword, dob) != -1) {
                        int userID = dbHandler.authenticatePatient(fullname, hashedPassword, dob);
                        Intent i = new Intent(MainActivity.this, UserDashboardActivity.class);
                        i.putExtra("id",userID);
                        i.putExtra("permissions","patient");
                        startActivity(i);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "User doesnt exist", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
                testTextView.setText(String.format("%s %s %s", fullname, password, dob));
            }
        });
    }


    //Method to validate input fields
    private boolean validateInput(String fullname, String password, Integer dob) {
        // Check if any field is empty
        return !fullname.isEmpty() && !password.isEmpty() && !String.valueOf(dob).isEmpty() &&
                password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$"); // Password must meet criteria
    }
    private String hashPassword(String password) {
        try {
            // Generate a random salt
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            // Specify the key specifications for PBKDF2
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
            Log.e("SECURE_LOGIN", "Password hashing failed", e);
            return null; // Return null if an exception occurs
        }
        return password;
    }

    public void register(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}