package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.contains("username")) {
            // Redirect to expense_history_edit if session exists
            startActivity(new Intent(MainActivity.this, expense_history_edit.class));
            finish();
        }

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        TextView loginButton = findViewById(R.id.logbtn);
        TextView forgetPass = findViewById(R.id.forget_pass);

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else {
                authenticateUser(username, password);
            }
        });

        forgetPass.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Forget__Pass.class);
            startActivity(intent);
        });
    }

    private void authenticateUser(String username, String password) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean userFound = false;

                for (DataSnapshot userTypeSnapshot : snapshot.getChildren()) {  // Loop through user types (Store, Admin, etc.)
                    if (userTypeSnapshot.hasChild(username)) {  // Check if the username exists inside any userType
                        userFound = true;

                        DataSnapshot userSnapshot = userTypeSnapshot.child(username);
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        String userType = userSnapshot.child("userType").getValue(String.class);

                        if (storedPassword != null && storedPassword.equals(password)) {
                            saveUserSession(username, userType);
                            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, expense_history_edit.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid password!", Toast.LENGTH_SHORT).show();
                        }
                        break; // Stop looping once we find the user
                    }
                }

                if (!userFound) {
                    Toast.makeText(MainActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(String username, String userType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("userType", userType);
        editor.apply();
    }
}
