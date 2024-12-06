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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class RegisterActivity extends AppCompatActivity {

    EditText fullnameEditText, passwordEditText,rPasswordEditText, refnumEditText;
    DBHandler dbHandler;
    Button register;
    TextView test;

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

        dbHandler = new DBHandler(this);

        test = findViewById(R.id.loginTestRegister);

        EditText fullnameEditText = findViewById(R.id.nameInputField);
        EditText passwordEditText = findViewById(R.id.passwordInputField);
        EditText rPasswordEditText = findViewById(R.id.rPasswordInputField);
        EditText refnumEditText = findViewById(R.id.refnumInputField);

        register = findViewById(R.id.registerButton);

        //When register button is clicked
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Gather inputs
                String fullname = fullnameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String rPassword = rPasswordEditText.getText().toString().trim();
                String refnum = refnumEditText.getText().toString().trim();

                //Validate inputs
                if (validateInput(fullname, password,refnum) && password.equals(rPassword)) {
                    int ref = Integer.parseInt(refnum);

                    byte[] salt = getBytes();
                    String hashedPassword = hashPassword(password,salt);

                    //Check if user exists
                    if (!dbHandler.userExists(fullname)) {

                        Toast.makeText(RegisterActivity.this, Arrays.toString(salt), Toast.LENGTH_SHORT).show();

                        int userID = Math.toIntExact(dbHandler.addUser(fullname, hashedPassword, ref,salt));
                        if(userID == -1){
                            Toast.makeText(RegisterActivity.this, "Error Signing up", Toast.LENGTH_SHORT).show();
                        } else {
                            //Reference can be given by the hospital to ensure only valid people sign up
                            Intent i = new Intent(RegisterActivity.this, UserDashboardActivity.class);
                            i.putExtra("id", userID);
                            //Check if user is a doctor or patient based on their reference
                            if (ref > 1000) {
                                i.putExtra("access", "doctor");
                            } else {
                                i.putExtra("access", "patient");
                                Toast.makeText(RegisterActivity.this, "You are a patient", Toast.LENGTH_SHORT).show();
                            }
                            startActivity(i);
                        }
                    }else {
                        //User already exists
                        Toast.makeText(RegisterActivity.this, "Account exists", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(RegisterActivity.this, "Invalid Input" , Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean validateInput(String fullname, String password, String refnum) {
        // Check if any field is empty
        return !fullname.isEmpty() && !password.isEmpty() && !refnum.isEmpty() &&
                password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$"); // Password must meet criteria
    }
    private String hashPassword(String password, byte[] salt) {
        try {

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

    private static byte[] getBytes() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    public void login(View v) {
        startActivity(new Intent(this, MainActivity.class));
    }
}