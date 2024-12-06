package com.example.hospitaladminapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MyAdapter extends ArrayAdapter<HashMap<String, String>> {
    private Context context;
    private ArrayList<HashMap<String, String>> data;

    public MyAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        super(context, R.layout.appointment_row, data);  // R.layout.list_item_layout is your custom list item layout
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse or create a new view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.appointment_row, parent, false);
        }

        // Get the current HashMap object from the data list
        HashMap<String, String> currentItem = data.get(position);

        // Find the TextViews in the layout
        TextView doctorTextView = convertView.findViewById(R.id.doctor);
        TextView patientTextView = convertView.findViewById(R.id.patient);
        TextView dateTextView = convertView.findViewById(R.id.date);
        TextView timeTextView = convertView.findViewById(R.id.time);

        // Set the values from the HashMap to the TextViews
        doctorTextView.setText(currentItem.get("doctor"));
        patientTextView.setText(currentItem.get("patient"));
        dateTextView.setText(currentItem.get("date"));
        timeTextView.setText(currentItem.get("time"));

        return convertView;
    }
}
