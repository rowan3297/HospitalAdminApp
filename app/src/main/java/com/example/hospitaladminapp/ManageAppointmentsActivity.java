package com.example.hospitaladminapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String permissions = intent.getStringExtra("access");

        btnAddAppointment = findViewById(R.id.createAppointmentRedirectButton);
        btnAddAppointment.setOnClickListener(v -> {
            Intent i = new Intent(ManageAppointmentsActivity.this, CreateAppointment.class);
            i.putExtra("id", id);
            i.putExtra("access",permissions);
            startActivity(i);
        });

        if (permissions == null) {
            Toast.makeText(this, "Permissions error: Logging out", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(ManageAppointmentsActivity.this, MainActivity.class);
            startActivity(intent1);
        }
        appointmentArrayList = dbHandler.getAppointments(id, permissions);
        appointmentList = findViewById(R.id.appointmentListView);
        ListAdapter adapter = new SimpleAdapter(ManageAppointmentsActivity.this, appointmentArrayList, R.layout.appointment_row,
                new String[]{"date","time","doctor","patient"}, new int[]{R.id.date, R.id.time, R.id.doctor, R.id.patient});
        appointmentList.setAdapter(adapter);

        appointmentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedAppointment = appointmentArrayList.get(position);

                // Optionally show a confirmation dialog before deleting
                new AlertDialog.Builder(ManageAppointmentsActivity.this)
                        .setMessage("Are you sure you want to delete this appointment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Remove the appointment from the list
                                dbHandler.deleteAppointment(Integer.parseInt(selectedAppointment.get("id")));

                                // Notify the adapter that the data has changed
                                Intent i = new Intent(ManageAppointmentsActivity.this, ManageAppointmentsActivity.class);
                                i.putExtra("id", id);
                                i.putExtra("access",permissions);
                                startActivity(i);

                                // Optionally, show a toast to confirm the deletion
                                Toast.makeText(ManageAppointmentsActivity.this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true; // Return true to indicate the long-click was handled
            }
        });

    }
}