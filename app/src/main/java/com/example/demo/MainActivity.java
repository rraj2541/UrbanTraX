package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText etEmail = findViewById(R.id.emailField);
        EditText etPassword = findViewById(R.id.passwordField);
        Button btnLogin = findViewById(R.id.loginButton);
        TextView tvSignUp = findViewById(R.id.registerText);
        TextView tvForgotPassword = findViewById(R.id.ForgotPasswordText);
        mAuth = FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required!");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required!");
                return;
            }
            if (!isValidPassword(password)) {
                etPassword.setError("Password must be at least 12 characters, include 1 special symbol, and 3 numbers!");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
            finish();
        });
    }
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        Pattern pattern = Pattern.compile("^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?/])(?=(?:.*\\d){3}).{8,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}