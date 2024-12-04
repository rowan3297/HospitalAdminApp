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

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String permissions = intent.getStringExtra("access");

        dbHandler = new DBHandler(this);

        timeEditText = findViewById(R.id.timeEditText);
        dateEditText = findViewById(R.id.dateEditText);
        patientNameEditText = findViewById(R.id.patientNameEditText);

        createAppointmentButton = findViewById(R.id.createAppointmentButton);

        createAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissions.equals("patient") || permissions == null) {
                    Toast.makeText(CreateAppointment.this, "Permissions error - signing out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAppointment.this, MainActivity.class);
                    startActivity(intent);
                }
                String time = timeEditText.getText().toString();
                String date = dateEditText.getText().toString();
                String patientName = patientNameEditText.getText().toString();

                int patientID = dbHandler.getPatientId(patientName);

                boolean appointmentCreated = dbHandler.createAppointment(patientID, id, date, time);
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