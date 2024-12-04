package com.example.hospitaladminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserDashboardActivity extends AppCompatActivity {


    private Button doctorsButton, patientsButton, appointmentsButton, accountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String permissions = intent.getStringExtra("access");

        doctorsButton = findViewById(R.id.doctorsButton);
        patientsButton = findViewById(R.id.patientsButton);
        appointmentsButton = findViewById(R.id.appointmentsButton);
        accountButton = findViewById(R.id.accountButton);

        doctorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                assert permissions != null;
                if (permissions.equals("doctor")) {
                    Intent i = new Intent(UserDashboardActivity.this, ManageDoctorsActivity.class);
                    i.putExtra("id", id);
                    startActivity(i);

                } else if (permissions.equals("patient")){
                    Toast.makeText(UserDashboardActivity.this, "You do not have access to this page", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(UserDashboardActivity.this, "An error occured with your permissions", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(UserDashboardActivity.this, MainActivity.class));
                }
            }
        });

        patientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                assert permissions != null;
                if (permissions.equals("doctor")) {
                    Intent i = new Intent(UserDashboardActivity.this, ManagePatientsActivity.class);
                    i.putExtra("id", id);
                    startActivity(i);

                } else if (permissions.equals("patient")){
                    Toast.makeText(UserDashboardActivity.this, "You do not have access to this page", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(UserDashboardActivity.this, "An error occured with your permissions", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(UserDashboardActivity.this, MainActivity.class));
                }
            }
        });

        appointmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                assert permissions != null;
                if (permissions.equals("doctor")) {
                    Intent i = new Intent(UserDashboardActivity.this, ManageAppointmentsActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("access", "doctor");
                    startActivity(i);

                } else if (permissions.equals("patient")) {
                    Intent i = new Intent(UserDashboardActivity.this, ManageAppointmentsActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("access", "patient");
                    startActivity(i);

                } else {
                    Toast.makeText(UserDashboardActivity.this, "An error occured with your permissions", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(UserDashboardActivity.this, MainActivity.class));
                }
            }
        });


    }
}