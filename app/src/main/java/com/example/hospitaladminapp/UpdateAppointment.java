package com.example.hospitaladminapp;

import android.content.Intent;
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

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class UpdateAppointment extends AppCompatActivity {

    private EditText editTextDate,editTextTime,editTextDoctor;
    private TextView textViewPatient;
    private Button updateButton;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHandler = new DBHandler(this);

        //Initialise views
        textViewPatient = findViewById(R.id.patientNameTextView);
        editTextDate = findViewById(R.id.updateDateEditText);
        editTextTime = findViewById(R.id.updateTimeEditText);
        editTextDoctor = findViewById(R.id.updateDoctorEditText);


        //Get intent data
        Intent i = getIntent();
        int id = i.getIntExtra("id", 0);
        String permission = i.getStringExtra("access");
        int appointmentId = i.getIntExtra("appointmentId", 0);

        //Stop patients editing appointments
        if(permission.equals("patient")){
            Toast.makeText(this, "Error with permisisons - logging out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateAppointment.this, MainActivity.class);
            startActivity(intent);
        }

        //Get all appointments and check not null
        HashMap<String,String> appointment = dbHandler.getAppointmentById(appointmentId);
        if(appointment == null){
            Toast.makeText(this, "Error getting appointment", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateAppointment.this, MainActivity.class);
            startActivity(intent);
        }
        Log.d("Appointment", appointment.toString());

        editTextDate.setText(appointment.get("date"));
        editTextTime.setText(appointment.get("time"));
        editTextDoctor.setText(appointment.get("doctor"));

        textViewPatient.setText(appointment.get("patient"));

        updateButton = findViewById(R.id.updateAppointmentButton);
        updateButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    String date = editTextDate.getText().toString();
                    String time = editTextTime.getText().toString();
                    String doctor = editTextDoctor.getText().toString();

                    //Validate the inputs and update the database
                    if(validateInput(date,time,doctor)){
                        boolean updated = dbHandler.updateAppointment(appointmentId,date,time,doctor);
                        if(!updated){
                            Toast.makeText(UpdateAppointment.this, "Error updating appointment", Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(UpdateAppointment.this, "Appointment updated", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(UpdateAppointment.this, UserDashboardActivity.class);
                        intent.putExtra("access",permission);
                        intent.putExtra("id",id);
                        startActivity(intent);

                    } else {
                        //Or notfiy the user something went wrong
                        Toast.makeText(UpdateAppointment.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    }

                }
        });


    }

    private boolean validateInput(String date, String time, String doctor) {
//        String timePattern = "^[0-2][0-9][0-5][0-9]$";
//        String datePattern = "^[0-3][0-9][0-1][0-9]$";
        if (date.isEmpty() || time.isEmpty() || doctor.isEmpty()) {
            return false;
        }
        return true;
    }
}