package com.example.hospitaladminapp;

import android.app.LauncherActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManageDoctorsActivity extends AppCompatActivity {

    private DBHandler dbHandler;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_doctors);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHandler = new DBHandler(this);
        //Get all the doctors from the database and display them in the listview
        ArrayList<String> doctorList = dbHandler.getDoctors();
        lv = (ListView) findViewById(R.id.doctorListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorList);
        // Set the adapter for the ListView
        lv.setAdapter(adapter);

        //Long click to delete doctor
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Create the PopupMenu and show it
                PopupMenu popupMenu = new PopupMenu(ManageDoctorsActivity.this, view);
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
                        boolean doctorDeleted = false;
                        try {
                            doctorDeleted = dbHandler.deleteDoctor(itemValue);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        if(doctorDeleted) {
                            Toast.makeText(ManageDoctorsActivity.this, "Doctor deleted", Toast.LENGTH_LONG).show();
                            ArrayList<String> doctorList = dbHandler.getDoctors();

                            // Update the adapter's data and refresh the ListView
                            adapter.clear();
                            adapter.addAll(doctorList);
                            adapter.notifyDataSetChanged();
                        } else{
                            Toast.makeText(ManageDoctorsActivity.this, "Doctor not deleted", Toast.LENGTH_LONG).show();
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
}