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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import androidx.core.view.GravityCompat;
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

public class expense_history_edit extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView edit;
    private TableLayout expensesTable, currencyTable, financialSummaryTable;
    private TextView currencyDropdown, financialSummaryDropdown;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_history_edit);

        // Retrieve SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        // Use the retrieved data
        Toast.makeText(this, "Logged in as: " + username + " (" + userType + ")", Toast.LENGTH_LONG).show();


        databaseRef = FirebaseDatabase.getInstance().getReference(userType).child("Expenses");

        // Initialize UI components
        initializeUIComponents();

        // Load existing expenses from Firebase
        loadExpensesFromDatabase();

        // Add the header row
      //  addHeaderRow();

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        edit = findViewById(R.id.toolbar_menu1);

        edit.setOnClickListener(v -> {
            Intent i = new Intent(expense_history_edit.this, expense_history_save.class);
            startActivity(i);
        });





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
        currencyTable = findViewById(R.id.currencyTable);
        financialSummaryTable = findViewById(R.id.financialSummaryTable);
        currencyDropdown = findViewById(R.id.currencyDropdown);
        financialSummaryDropdown = findViewById(R.id.financialSummaryDropdown);


        // Toggle Dropdowns
        currencyDropdown.setOnClickListener(v -> toggleVisibility(currencyTable, currencyDropdown, "Currency Denomination"));
        financialSummaryDropdown.setOnClickListener(v -> toggleVisibility(financialSummaryTable, financialSummaryDropdown, "Financial Summary"));



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
    private void toggleVisibility(TableLayout table, TextView dropdown, String label) {
        if (table.getVisibility() == View.GONE) {
            table.setVisibility(View.VISIBLE);
            dropdown.setText(label + " ▲");
        } else {
            table.setVisibility(View.GONE);
            dropdown.setText(label + " ▼");
        }
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
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference newExpenseRef = databaseRef.child("ExpenseDetails").child(todayDate).child(String.valueOf(serial));
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
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference expenseRef = databaseRef.child("ExpenseDetails").child(todayDate);

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
                loadFinancialSummaryData();

                // Load currency data
                loadCurrencyData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(expense_history_edit.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFinancialSummaryData() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference summaryRef = databaseRef.child("FinancialSummary").child(todayDate);

        summaryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming you have EditText fields to display these values
                    setTextValue(R.id.opening_cash_in_hand, dataSnapshot.child("OpeningCashInHand").getValue(String.class));
                    setTextValue(R.id.totalsalesFS, dataSnapshot.child("TotalSales").getValue(String.class));
                    setTextValue(R.id.receivedfromowner, dataSnapshot.child("ReceivedFromOwner").getValue(String.class));
                    setTextValue(R.id.creditreceived, dataSnapshot.child("CreditReceived").getValue(String.class));
                    setTextValue(R.id.expensesfs, dataSnapshot.child("Expenses").getValue(String.class));
                    setTextValue(R.id.upipayments, dataSnapshot.child("UPIPayments").getValue(String.class));
                    setTextValue(R.id.creditsales, dataSnapshot.child("CreditSales").getValue(String.class));
                    setTextValue(R.id.amountinhand, dataSnapshot.child("AmountInHand").getValue(String.class));
                    setTextValue(R.id.endcash, dataSnapshot.child("EndCash").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(expense_history_edit.this, "Failed to load financial summary.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrencyData() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference currencyRef = databaseRef.child("CurrencyDenomination").child(todayDate);

        currencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming you have EditText fields to display these values
                    setTextValue(R.id.fivehundred, dataSnapshot.child("500").getValue(String.class));
                    setTextValue(R.id.twohundred, dataSnapshot.child("200").getValue(String.class));
                    setTextValue(R.id.hundred, dataSnapshot.child("100").getValue(String.class));
                    setTextValue(R.id.fifty, dataSnapshot.child("50").getValue(String.class));
                    setTextValue(R.id.twenty, dataSnapshot.child("20").getValue(String.class));
                    setTextValue(R.id.ten, dataSnapshot.child("10").getValue(String.class));
                    setTextValue(R.id.coins, dataSnapshot.child("Coins").getValue(String.class));
                    setTextValue(R.id.Extracoins, dataSnapshot.child("ExtraCoins").getValue(String.class));
                    setTextValue(R.id.others, dataSnapshot.child("Others").getValue(String.class));
                    setTextValue(R.id.totalsales, dataSnapshot.child("TotalSales").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(expense_history_edit.this, "Failed to load currency data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setTextValue(int id, String value) {
        TextView editText = findViewById(id);
        if (value != null) {
            editText.setText(value);
        } else {
            editText.setText(""); // Clear the field if value is null
        }
    }
    private String getTextValue(int id) {
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }




  @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

      if (id == R.id.nav_home) {
          Intent i = new Intent(expense_history_edit.this, expense_history_edit.class);
          startActivity(i);
      } else if (id == R.id.nav_create_user) {
        //  Toast.makeText(this, "Create User Clicked", Toast.LENGTH_SHORT).show();
          Intent i = new Intent(expense_history_edit.this, expense_history_date_selection.class);
          startActivity(i);
      }else if (id == R.id.nav_manage_user) {
          // Handle Manage User navigation (e.g., start a new activity or fragment)
          // Example:
          Intent i = new Intent(expense_history_edit.this, agencies_history_edit.class);
          startActivity(i);
      }
      // Close drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
