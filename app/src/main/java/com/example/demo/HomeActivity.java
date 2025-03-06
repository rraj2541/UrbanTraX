package com.example.demo;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private JSONObject busData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(TAG, "HomeActivity started");

        // Load JSON data
        loadBusData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_home) {
                return true;
            }
            return false;
        });

        // Bus Timing Option Click Listener
        LinearLayout busTimingOption = findViewById(R.id.busTimingOption);
        if (busTimingOption == null) {
            Log.e(TAG, "busTimingOption LinearLayout not found!");
            Toast.makeText(this, "Bus Timing option not found in layout", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Setting click listener for busTimingOption");
        busTimingOption.setOnClickListener(v -> {
            Log.d(TAG, "Bus Timing clicked");
            showBusListDialog();
        });
    }

    private void loadBusData() {
        try {
            InputStream is = getAssets().open("bus_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            is.close();
            if (bytesRead == -1) {
                Log.e(TAG, "Failed to read bus_data.json");
                return;
            }
            String json = new String(buffer, StandardCharsets.UTF_8);
            busData = new JSONObject(json).getJSONObject("buses");
            Log.d(TAG, "Bus data loaded successfully: " + busData.keys().next());
        } catch (Exception e) {
            Log.e(TAG, "Error loading bus data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading bus data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showBusListDialog() {
        if (busData == null) {
            Log.e(TAG, "Bus data is null, cannot show dialog");
            Toast.makeText(this, "Bus data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> busNumbers = new ArrayList<>();
        Iterator<String> keys = busData.keys();
        while (keys.hasNext()) {
            String busNumber = keys.next();
            busNumbers.add(busNumber);
            Log.d(TAG, "Added bus number: " + busNumber);
        }

        if (busNumbers.isEmpty()) {
            Log.w(TAG, "No bus numbers found in data");
            Toast.makeText(this, "No bus data available", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Bus Number");
        builder.setItems(busNumbers.toArray(new String[0]), (dialog, which) -> {
            String selectedBus = busNumbers.get(which);
            Log.d(TAG, "Selected bus: " + selectedBus);
            try {
                JSONObject busInfo = busData.getJSONObject(selectedBus);
                Intent intent = new Intent(HomeActivity.this, BusDetailActivity.class);
                intent.putExtra("busNumber", selectedBus);
                intent.putExtra("destination", busInfo.getString("destination"));
                intent.putExtra("origin", busInfo.getString("origin"));
                intent.putExtra("routeNumber", busInfo.getString("route_number"));
                intent.putExtra("via", busInfo.getJSONArray("via").toString());
                intent.putExtra("schedule", busInfo.getJSONArray("schedule").toString());
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error processing bus selection: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading bus details", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> Log.d(TAG, "Dialog cancelled"));
        builder.show();
        Log.d(TAG, "Bus list dialog shown");
    }
}