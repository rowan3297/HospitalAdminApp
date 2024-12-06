package com.example.hospitaladminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public class CreateAppointment extends AppCompatActivity {

    EditText timeEditText, dateEditText,patientNameEditText;
    Button createAppointmentButton;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the id and permissions from the intent
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String permissions = intent.getStringExtra("access");

        dbHandler = new DBHandler(this);

        // Initialize the views
        timeEditText = findViewById(R.id.timeEditText);
        dateEditText = findViewById(R.id.dateEditText);
        patientNameEditText = findViewById(R.id.patientNameEditText);
        createAppointmentButton = findViewById(R.id.createAppointmentButton);

        // Set the click listener for the create appointment button
        createAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user has the correct permissions
                if (permissions.equals("patient") || permissions == null) {
                    Toast.makeText(CreateAppointment.this, "Permissions error - signing out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAppointment.this, MainActivity.class);
                    startActivity(intent);
                }
                // Get the input values
                String time = timeEditText.getText().toString();
                String date = dateEditText.getText().toString();
                String patientName = patientNameEditText.getText().toString();

                // Check if the input values are empty
                if (time.isEmpty() || date.isEmpty() || patientName.isEmpty()) {
                    Toast.makeText(CreateAppointment.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get the patientID from the patient name
                int patientId = dbHandler.getPatientId(patientName);

                // Create the appointment and then feed back to the user if it was successful or not
                boolean appointmentCreated = dbHandler.createAppointment(patientId, id, date, time);
                if (appointmentCreated) {
                    Toast.makeText(CreateAppointment.this, "Appointment created", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAppointment.this, UserDashboardActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("access", "doctor");
                    startActivity(intent);
                } else {
                    Toast.makeText(CreateAppointment.this, "Error creating appointment, there could be a conflicting appointment ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}