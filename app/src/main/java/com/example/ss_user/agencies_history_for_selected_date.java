package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class agencies_history_for_selected_date extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private TableLayout expensesTable;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agencies_history_for_selected_date);
        // Retrieve SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        // Retrieve the saved date from SharedPreferences
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");

// Find the TextView by ID
        TextView tvSelectedDate = findViewById(R.id.tv_selected_date);

// Set the retrieved date to the TextView
        tvSelectedDate.setText("Selected Date: " + savedDate);

        databaseRef = FirebaseDatabase.getInstance().getReference(userType).child("Agencies");


        // Initialize UI components
        initializeUIComponents();

        // Load existing expenses from Firebase
        loadExpensesFromDatabase();

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create Hamburger Menu Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void initializeUIComponents() {
        // Initialize Toolbar & Save Button
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        expensesTable = findViewById(R.id.expenses);


        // Initialize Navigation Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void addHeaderRow() {
        // Create a new TableRow
        TableRow tableRow = new TableRow(this);

        // Create the S.NO TextView
        TextView serialTextView = new TextView(this);
        serialTextView.setText("S.NO");
        serialTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        serialTextView.setPadding(8, 8, 8, 8);
        serialTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        serialTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        // Create the PARTICULARS TextView
        TextView particularsTextView = new TextView(this);
        particularsTextView.setText("PARTICULARS");
        particularsTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 7));
        particularsTextView.setPadding(8, 8, 8, 8);
        particularsTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        particularsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        particularsTextView.setGravity(Gravity.CENTER);

        // Create the AMOUNT TextView
        TextView amountTextView = new TextView(this);
        amountTextView.setText("AMOUNT");
        amountTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        amountTextView.setPadding(8, 8, 8, 8);
        amountTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        amountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        amountTextView.setGravity(Gravity.END);


        // Add all views to the TableRow
        tableRow.addView(serialTextView);
        tableRow.addView(particularsTextView);
        tableRow.addView(amountTextView);

        // Finally, add the TableRow to your TableLayout
        expensesTable.addView(tableRow);
    }
    private void addExpenseRow(int serial, String details, int amount) {
        if (expensesTable.getChildCount() == 0) { // Ensure header exists
            addHeaderRow();
        }

        // Create a new TableRow programmatically
        TableRow newRow = new TableRow(this);

        TextView serialNo = new TextView(this);
        serialNo.setText(String.valueOf(serial));  // Set incremented serial number
        serialNo.setHintTextColor(ContextCompat.getColor(this, R.color.black));
        serialNo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        serialNo.setPadding(8, 8, 8, 8);
        serialNo.setTextColor(ContextCompat.getColor(this, R.color.black));
        serialNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        serialNo.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));

        TextView particulars = new TextView(this);
        particulars.setHint("Enter details");
        particulars.setHintTextColor(ContextCompat.getColor(this, R.color.black));
        particulars.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        particulars.setPadding(8, 8, 8, 8);
        particulars.setTextColor(ContextCompat.getColor(this, R.color.black));
        particulars.setInputType(InputType.TYPE_CLASS_TEXT);
        particulars.setGravity(Gravity.CENTER);
        particulars.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 7));
        particulars.setText(details); // Set details

        TextView amountField = new TextView(this);
        amountField.setHint("0.00");
        amountField.setHintTextColor(ContextCompat.getColor(this, R.color.black));
        amountField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        amountField.setPadding(8, 8, 8, 8);
        amountField.setTextColor(ContextCompat.getColor(this, R.color.black));
        amountField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountField.setGravity(Gravity.END);
        amountField.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        amountField.setText(String.valueOf(amount)); // Set amount


        // Store empty row in Firebase initially
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");
        DatabaseReference newExpenseRef = databaseRef.child("ExpenseDetails").child(savedDate).child(String.valueOf(serial));
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("serial", serial);
        expenseData.put("details", details);
        expenseData.put("amount", amount);
        newExpenseRef.setValue(expenseData);

        // Add TextWatcher to update Firebase in real-time
        particulars.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                newExpenseRef.child("details").setValue(s.toString().trim());
            }
        });

        amountField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString().trim();
                int price = value.isEmpty() ? 0 : Integer.parseInt(value);
                newExpenseRef.child("amount").setValue(price);
            }
        });


        newRow.addView(serialNo);
        newRow.addView(particulars);
        newRow.addView(amountField);
        expensesTable.addView(newRow);
    }
    private void loadExpensesFromDatabase() {
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");

        DatabaseReference expenseRef = databaseRef.child("ExpenseDetails").child(savedDate);

        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expensesTable.removeAllViews(); // Clear existing rows

                if (!dataSnapshot.exists()) {
                    // If no data exists, add an empty row
                    int serialCounter = 1;
                    addExpenseRow(serialCounter++, "", 0);
                } else {
                    for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                        int serial = expenseSnapshot.child("serial").getValue(Integer.class);
                        String details = expenseSnapshot.child("details").getValue(String.class);
                        int amount = expenseSnapshot.child("amount").getValue(Integer.class);

                        // Add a new row with the retrieved data
                        addExpenseRow(serial, details, amount);
                    }
                }

                // Load financial summary data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(agencies_history_for_selected_date.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent i = new Intent(this, expense_history_edit.class);
            startActivity(i);
        } else if (id == R.id.nav_create_user) {
            Intent i = new Intent(this, expense_history_date_selection.class);
            startActivity(i);
        }else if (id == R.id.nav_manage_user) {
            Intent i = new Intent(this, agencies_history_edit.class);
            startActivity(i);
        }else if (id == R.id.nav_log_out) {
             logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear session
        editor.apply();

        // Redirect to Login Screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}