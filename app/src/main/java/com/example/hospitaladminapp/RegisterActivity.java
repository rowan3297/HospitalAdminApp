package com.example.hospitaladminapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {

    EditText fullnameEditText, passwordEditText, dobEditText,rPasswordEditText, refnumEditText;
    DBHandler dbHandler;
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullnameEditText = findViewById(R.id.nameInputField);
        passwordEditText = findViewById(R.id.passwordInputField);
        rPasswordEditText = findViewById(R.id.rPasswordInputField);
        refnumEditText = findViewById(R.id.refnumInputField);
        dobEditText = findViewById(R.id.dobInputField);



        String fullname = fullnameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String rPassword = rPasswordEditText.getText().toString().trim();
        String stringDob = dobEditText.getText().toString().trim();
        Integer dob = Integer.parseInt(stringDob);
        String refnum = refnumEditText.getText().toString().trim();
        Integer ref = Integer.parseInt(refnum);

        String hashedPassword = hashPassword(password);
        String hashedRPassword = hashPassword(rPassword);

        register = findViewById(R.id.registerButton);

        //When register button is clicked
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate inputs
                if (validateInput(fullname, hashedPassword, dob) && hashedPassword == hashedRPassword) {

                    //Check if user exists
                    if (dbHandler.userExists(fullname, dob)) {
                        Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    } else {

                        //If user doesnt exist, add them to the database
                        int userID = Math.toIntExact(dbHandler.addUser(fullname, hashedPassword, dob, ref));
                        Toast.makeText(RegisterActivity.this, "User added", Toast.LENGTH_SHORT).show();

                        //Reference can be given by the hospital to ensure only valid people sign up
                        if (ref > 10000) {
                            Intent i = new Intent(RegisterActivity.this, UserDashboardActivity.class);
                            i.putExtra("id",userID);
                            startActivity(i);
                        } else {
                            startActivity(new Intent(RegisterActivity.this, UserDashboardActivity.class));
                        }
                    }
                }
            }
        });

    }

    private boolean validateInput(String fullname, String password, Integer dob) {
        // Check if any field is emp
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

    public void login(View v) {
        startActivity(new Intent(this, MainActivity.class));
    }
}