package com.example.hospitaladminapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ManagePatientsActivity extends AppCompatActivity {

    private DBHandler dbHandler;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_patients);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHandler = new DBHandler(this);

        ArrayList<String> patientList = dbHandler.getPatients();
        lv = (ListView) findViewById(R.id.patientListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patientList);
        // Set the adapter for the ListView
        lv.setAdapter(adapter);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Create the PopupMenu and show it
                PopupMenu popupMenu = new PopupMenu(ManagePatientsActivity.this, view);
                // Inflate the menu from a resource file
                getMenuInflater().inflate(R.menu.context_menu, popupMenu.getMenu());

                int itemPosition = lv.getPositionForView(view);
                String itemValue = (String) lv.getItemAtPosition(itemPosition);

                // Set an item click listener for the menu
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle option 1
                        //Code to delete doctor
                        boolean doctorDeleted = dbHandler.deletePatient(itemValue);
                        if(doctorDeleted) {
                            Toast.makeText(ManagePatientsActivity.this, "Patient deleted", Toast.LENGTH_LONG).show();
                            ArrayList<String> updatedPatientList = dbHandler.getPatients();

                            // Update the adapter's data and refresh the ListView
                            adapter.clear();
                            adapter.addAll(updatedPatientList);
                            adapter.notifyDataSetChanged();
                        }
                        return item.getItemId() == R.id.menu_option1;
                    }
                });
                // Show the menu
                popupMenu.show();
                return true; // Returning true consumes the long click event
            }
        });
    }

    public void settingsPressed(View view){
        Toast.makeText(this, "Settings are still being developed...", Toast.LENGTH_LONG).show();
    }
}