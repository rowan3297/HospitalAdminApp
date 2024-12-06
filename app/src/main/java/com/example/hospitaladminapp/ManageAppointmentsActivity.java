package com.example.hospitaladminapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;

public class ManageAppointmentsActivity extends AppCompatActivity {

    private Button btnAddAppointment;
    private ListView appointmentList;
    private DBHandler dbHandler;
    private ArrayList<HashMap<String,String>> appointmentArrayList;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_appointments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHandler = new DBHandler(this);

        //Get critical info from intent
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String permissions = intent.getStringExtra("access");

        //Redirect to create appointment page using create appointment button
        btnAddAppointment = findViewById(R.id.createAppointmentRedirectButton);
        btnAddAppointment.setOnClickListener(v -> {
            if(permissions == "patient"){
                Toast.makeText(this, "You do not have permission to create appointments", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(ManageAppointmentsActivity.this, CreateAppointment.class);
            i.putExtra("id", id);
            i.putExtra("access",permissions);
            startActivity(i);
        });

        //Check permissions havent been faked
        if (permissions == null) {
            Toast.makeText(this, "Permissions error: Logging out", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(ManageAppointmentsActivity.this, MainActivity.class);
            startActivity(intent1);
        }
        //Get appointments from database and check not null
        appointmentArrayList = dbHandler.getAppointments(id, permissions);
        if (appointmentArrayList == null) {
            Toast.makeText(this, "Error getting appointments", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(ManageAppointmentsActivity.this, MainActivity.class);
            startActivity(intent1);
        } else {
            Log.d("Appointments", appointmentArrayList.toString());
        }
        //Set up list view with the list from the database
        appointmentList = findViewById(R.id.appointmentListView);
        adapter = new MyAdapter(this, appointmentArrayList);
        appointmentList.setAdapter(adapter);

        //Long click to delete or update appointment
        appointmentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedAppointment = appointmentArrayList.get(position);

                // Optionally show a confirmation dialog before deleting
                new AlertDialog.Builder(ManageAppointmentsActivity.this)
                        .setMessage("Are you sure you want to delete this appointment?")
                        // Delete the appointment
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Remove the appointment from the list
                                dbHandler.deleteAppointment(Integer.parseInt(selectedAppointment.get("id")));

                                Intent i = new Intent(ManageAppointmentsActivity.this, ManageAppointmentsActivity.class);
                                i.putExtra("id", id);
                                i.putExtra("access",permissions);
                                startActivity(i);
                                finish();

                                // Optionally, show a toast to confirm the deletion
                                Toast.makeText(ManageAppointmentsActivity.this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        // Update appointment
                        .setNeutralButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int appointmentId = Integer.parseInt(selectedAppointment.get("id"));
                                Intent i = new Intent(ManageAppointmentsActivity.this, UpdateAppointment.class);
                                i.putExtra("id", id);
                                i.putExtra("access",permissions);
                                i.putExtra("appointmentId", appointmentId);
                                startActivity(i);
                                finish();
                            }
                        })
                        // Cancel the deletion
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }
        });
    }
}