package com.example.demo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText userNameEditText, userPhoneEditText, userDobEditText, userAddressEditText;
    private TextView userEmailTextView;
    private ImageView profileImageView;
    private BottomNavigationView bottomNavigationView;
    private Button saveButton, logoutButton, deleteAccountButton;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        userNameEditText = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        userPhoneEditText = findViewById(R.id.userPhone);
        userDobEditText = findViewById(R.id.userDob);
        userAddressEditText = findViewById(R.id.userAddress);
        profileImageView = findViewById(R.id.profileImage);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Initialize calendar for date picker
        calendar = Calendar.getInstance();

        // Highlight current menu item
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup buttons
        saveButton.setOnClickListener(v -> saveProfileChanges());
        logoutButton.setOnClickListener(v -> logoutUser());
        deleteAccountButton.setOnClickListener(v -> deleteAccount());

        // Setup date picker for DOB
        userDobEditText.setOnClickListener(v -> showDatePickerDialog());

        // Check authentication state
        checkAuthentication();
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user found");
            Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        } else {
            Log.d(TAG, "Authenticated user found - UID: " + currentUser.getUid());
            Toast.makeText(this, "Loading profile...", Toast.LENGTH_SHORT).show();
            loadUserProfile(currentUser.getUid());
            userEmailTextView.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email available");
        }
    }

    private void loadUserProfile(String userId) {
        Log.d(TAG, "Attempting to load profile for UID: " + userId);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String emailKey = email.replace(".", ",").replace("@", "_");
            DatabaseReference userRef = mDatabase.child(emailKey);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "Profile data found: " + dataSnapshot.toString());
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Log.d(TAG, "Key: " + child.getKey() + ", Value: " + child.getValue());
                        }

                        String name = dataSnapshot.child("name").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        String dob = dataSnapshot.child("dob").getValue(String.class);
                        String address = dataSnapshot.child("address").getValue(String.class);

                        userNameEditText.setText(name != null ? name : "");
                        userEmailTextView.setText(email != null ? email : "No email");
                        userPhoneEditText.setText(phone != null ? phone : "");
                        userDobEditText.setText(dob != null ? dob : "");
                        userAddressEditText.setText(address != null ? address : "");

                        Toast.makeText(ProfileActivity.this,
                                "Profile loaded successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "No profile data exists at: Users/" + emailKey);
                        Toast.makeText(ProfileActivity.this,
                                "Profile not found. Please complete registration.",
                                Toast.LENGTH_LONG).show();
                        redirectToRegister();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    Toast.makeText(ProfileActivity.this,
                            "Error loading profile: " + databaseError.getMessage(),
                            Toast.LENGTH_LONG).show();
                    userNameEditText.setText("Error Loading Profile");
                }
            });
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    userDobEditText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveProfileChanges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String emailKey = email.replace(".", ",").replace("@", "_");
            DatabaseReference userRef = mDatabase.child(emailKey);

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", userNameEditText.getText().toString().trim());
            updates.put("email", email);
            updates.put("phone", userPhoneEditText.getText().toString().trim());
            updates.put("dob", userDobEditText.getText().toString().trim());
            updates.put("address", userAddressEditText.getText().toString().trim());

            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
        overridePendingTransition(0, 0);
    }

    private void deleteAccount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String emailKey = email.replace(".", ",").replace("@", "_");
            DatabaseReference userRef = mDatabase.child(emailKey);

            userRef.removeValue().addOnCompleteListener(dbTask -> {
                if (dbTask.isSuccessful()) {
                    currentUser.delete().addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            redirectToLogin();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to delete account: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to delete profile data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent homeIntent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                overridePendingTransition(0,0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_settings) {
//                Intent settingsIntent = new Intent(ProfileActivity.this, SettingsActivity.class);
//                startActivity(settingsIntent);
//                finish();
                return true;
            } else if (itemId == R.id.nav_reminders) {
//                Intent remindersIntent = new Intent(ProfileActivity.this, RemindersActivity.class);
//                startActivity(remindersIntent);
//                finish();
                return true;
            } else if (itemId == R.id.nav_pass) {
//                Intent passIntent = new Intent(ProfileActivity.this, PassActivity.class);
//                startActivity(passIntent);
//                finish();
                return true;
            }
            return false;
        });
    }

    private void redirectToLogin() {
        Log.d(TAG, "Redirecting to LoginActivity");
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToRegister() {
        Log.d(TAG, "Redirecting to RegisterActivity due to missing profile data");
        Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "User session expired or logged out");
            redirectToLogin();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
    }
}
