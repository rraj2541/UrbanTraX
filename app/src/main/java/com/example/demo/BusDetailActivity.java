package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class BusDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bus_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView busNumberTitle = findViewById(R.id.busNumberTitle);
        TextView destinationText = findViewById(R.id.destinationText);
        TextView originText = findViewById(R.id.originText);
        TextView routeNumberText = findViewById(R.id.routeNumberText);
        TextView viaText = findViewById(R.id.viaText);
        TableLayout scheduleTable = findViewById(R.id.scheduleTable); // Reference to the TableLayout

        Intent intent = getIntent();
        String busNumber = intent.getStringExtra("busNumber");
        String destination = intent.getStringExtra("destination");
        String origin = intent.getStringExtra("origin");
        String routeNumber = intent.getStringExtra("routeNumber");
        String via = intent.getStringExtra("via");
        String schedule = intent.getStringExtra("schedule");

        busNumberTitle.setText("Bus " + busNumber);
        destinationText.setText("Destination: " + destination);
        originText.setText("Origin: " + origin);
        routeNumberText.setText("Route Number: " + routeNumber);

        try {
            // Format Via
            JSONArray viaArray = new JSONArray(via);
            StringBuilder viaString = new StringBuilder("Via: ");
            for (int i = 0; i < viaArray.length(); i++) {
                viaString.append(viaArray.getString(i));
                if (i < viaArray.length() - 1) viaString.append(", ");
            }
            viaText.setText(viaString.toString());

            // Clear existing rows in the TableLayout
            scheduleTable.removeAllViews();

            // Add header row
            TableRow headerRow = new TableRow(this);
            TextView departureHeader = new TextView(this);
            departureHeader.setText("Departure");
            departureHeader.setTextColor(getResources().getColor(R.color.black));
            headerRow.addView(departureHeader);

            TextView arrivalHeader = new TextView(this);
            arrivalHeader.setText("Arrival");
            arrivalHeader.setTextColor(getResources().getColor(R.color.black));
            headerRow.addView(arrivalHeader);

            scheduleTable.addView(headerRow);

            // Format Schedule
            JSONArray scheduleArray = new JSONArray(schedule);
            for (int i = 0; i < scheduleArray.length(); i++) {
                JSONObject scheduleObj = scheduleArray.getJSONObject(i);

                // Extracting the correct keys for departure and arrival
                String departure = "N/A";
                String arrival = "N/A";

                // Check for bus number and set keys accordingly
                if (busNumber.equals("15")) {
                    departure = scheduleObj.has("departure_station") ? scheduleObj.getString("departure_station") : "N/A";
                    arrival = scheduleObj.has("arrival_harni") ? scheduleObj.getString("arrival_harni") : "N/A"; // Assuming destination is "Harni Bypass"
                } else if (busNumber.equals("16")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_radhe") ? scheduleObj.getString("arrival_radhe") : "N/A";
                } else if (busNumber.equals("25")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_panchvati") ? scheduleObj.getString("arrival_panchvati") : "N/A";
                } else if (busNumber.equals("26")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_sunpharma") ? scheduleObj.getString("arrival_sunpharma") : "N/A";
                } else if (busNumber.equals("27")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_gokulnagar") ? scheduleObj.getString("arrival_gokulnagar") : "N/A";
                } else if (busNumber.equals("30")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_lakshmipura") ? scheduleObj.getString("arrival_lakshmipura") : "N/A";
                } else if (busNumber.equals("17-17A")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_makarpura") ? scheduleObj.getString("arrival_makarpura") : "N/A";
                } else if (busNumber.equals("25A")) {
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    arrival = scheduleObj.has("arrival_narmdeshwar") ? scheduleObj.getString("arrival_narmdeshwar") : "N/A";
                } else {
                    // For other buses, use the default keys
                    departure = scheduleObj.has("departure_janmahal") ? scheduleObj.getString("departure_janmahal") : "N/A";
                    String arrivalKey = "arrival_" + destination.toLowerCase().replace(" ", "_"); // Constructing the arrival key based on destination
                    arrival = scheduleObj.has(arrivalKey) ? scheduleObj.getString(arrivalKey) : "N/A";
                }

                TableRow scheduleRow = new TableRow(this);

                // Departure TextView
                TextView departureText = new TextView(this);
                departureText.setText(departure);
                departureText.setPadding(16, 16, 16, 16); // Add padding
                departureText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                departureText.setGravity(android.view.Gravity.CENTER); // Center align
                scheduleRow.addView(departureText);

                // Arrival TextView
                TextView arrivalText = new TextView(this);
                arrivalText.setText(arrival);
                arrivalText.setPadding(16, 16, 16, 16); // Add padding
                arrivalText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                arrivalText.setGravity(android.view.Gravity.CENTER); // Center align
                scheduleRow.addView(arrivalText);

                scheduleTable.addView(scheduleRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            viaText.setText("Via: Error parsing data");
            // Handle error for schedule display
        }
    }
}