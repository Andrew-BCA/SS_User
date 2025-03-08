package com.example.ss_user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    private EditText usernameEditText, passwordEditText;
    private TextView loginButton,ForgetPass,RegisterHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Adjust system bars (for immersive UI)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.logbtn);
        ForgetPass = findViewById(R.id.forget_pass);


        // Set onClickListener for login button
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else {
                // Dummy login logic (Replace with real authentication)
                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, expense_history_edit.class);
                startActivity(i);
            }
        });
        // Navigate to Forget Password Page
        ForgetPass.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, Forget__Pass.class);
            startActivity(intent);
        });

    }
}