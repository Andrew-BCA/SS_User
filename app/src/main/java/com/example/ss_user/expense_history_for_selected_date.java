package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class expense_history_for_selected_date extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout drawerLayout;
    private TableLayout expensesTable, agenciesTable, currencyTable, financialSummaryTable;
    private TextView currencyDropdown, financialSummaryDropdown,totalExpenseTextView,totalAgencyExpenseTextView,toolbar_title;
    private DatabaseReference databaseRef;
    private DatabaseReference requestRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_history_for_selected_date);

        // Retrieve SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        // Use the retrieved data
     //   Toast.makeText(this, "Logged in as: " + username + " (" + userType + ")", Toast.LENGTH_LONG).show();
        databaseRef = FirebaseDatabase.getInstance().getReference(userType);
        toolbar_title = findViewById(R.id.toolbar_title);
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        totalAgencyExpenseTextView = findViewById(R.id.totalAgencyExpenseTextView);

        toolbar_title.setText(userType);
        requestRef = FirebaseDatabase.getInstance().getReference(userType).child(username).child("Requests");

        // Initialize UI components
        initializeUIComponents();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Make sure this is called!

        // Load existing expenses from Firebase
        loadExpensesFromDatabase();
        loadAgenciesExpensesFromDatabase();
        loadCurrencyData();
        loadFinancialSummaryData();

        TextView currencyDropdown = findViewById(R.id.currencyDropdown);
        final TableLayout currencyTable = findViewById(R.id.currencyTable);

        currencyDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currencyTable.getVisibility() == View.GONE) {
                    currencyTable.setVisibility(View.VISIBLE);
                    currencyDropdown.setText("Currency Denomination ▲");
                } else {
                    currencyTable.setVisibility(View.GONE);
                    currencyDropdown.setText("Currency Denomination ▼");
                }
            }
        });

        TextView expenseDropdown = findViewById(R.id.expenseDropdown);
        LinearLayout expenseLayout = findViewById(R.id.expenseLayout);

        expenseDropdown.setOnClickListener(v -> {
            if (expenseLayout.getVisibility() == View.VISIBLE) {
                expenseLayout.setVisibility(View.GONE);
                expenseDropdown.setText("Expense Details ▼");
            } else {
                expenseLayout.setVisibility(View.VISIBLE);
                expenseDropdown.setText("Expense Details ▲");
            }
        });

        TextView agenciesDropdown = findViewById(R.id.agenciesDropdown);
        LinearLayout agenciesLayout = findViewById(R.id.agenciesLayout);

        agenciesDropdown.setOnClickListener(v -> {
            if (agenciesLayout.getVisibility() == View.VISIBLE) {
                agenciesLayout.setVisibility(View.GONE);
                agenciesDropdown.setText("Agencies ▼");
            } else {
                agenciesLayout.setVisibility(View.VISIBLE);
                agenciesDropdown.setText("Agencies ▲");
            }
        });



        TextView financialSummaryDropdown = findViewById(R.id.financialSummaryDropdown);
        final TableLayout financialSummaryTable = findViewById(R.id.financialSummaryTable);

        financialSummaryDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (financialSummaryTable.getVisibility() == View.GONE) {
                    financialSummaryTable.setVisibility(View.VISIBLE);
                    financialSummaryDropdown.setText("Financial Summary ▲");
                } else {
                    financialSummaryTable.setVisibility(View.GONE);
                    financialSummaryDropdown.setText("Financial Summary ▼");
                }
            }
        });

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        agenciesTable = findViewById(R.id.agenciesexpenses);
        expensesTable = findViewById(R.id.expenses);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Remove the title from the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Removes title from support action bar
        toolbar.setTitle(""); // Removes title from the toolbar
        toolbar.setSubtitle(""); // Removes any subtitle
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        totalAgencyExpenseTextView = findViewById(R.id.totalAgencyExpenseTextView);


        // Create Hamburger Menu Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        currencyDropdown.setOnClickListener(v -> toggleVisibility(currencyTable, currencyDropdown, "Currency Denomination"));
        financialSummaryDropdown.setOnClickListener(v -> toggleVisibility(financialSummaryTable, financialSummaryDropdown, "Financial Summary"));
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
    private void addagenciesHeaderRow(){

        // Create a new TableRow for agenciesTable
        TableRow agenciesHeaderRow = new TableRow(this);
        // Recreate the TextViews for the agencies header row
        TextView agenciesSerialTextView = new TextView(this);
        agenciesSerialTextView.setText("S.NO");
        agenciesSerialTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        agenciesSerialTextView.setPadding(8, 8, 8, 8);
        agenciesSerialTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        agenciesSerialTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        TextView agenciesParticularsTextView = new TextView(this);
        agenciesParticularsTextView.setText("PARTICULARS");
        agenciesParticularsTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 7));
        agenciesParticularsTextView.setPadding(8, 8, 8, 8);
        agenciesParticularsTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        agenciesParticularsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        agenciesParticularsTextView.setGravity(Gravity.CENTER);

        TextView agenciesAmountTextView = new TextView(this);
        agenciesAmountTextView.setText("AMOUNT");
        agenciesAmountTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        agenciesAmountTextView.setPadding(8, 8, 8, 8);
        agenciesAmountTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        agenciesAmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        agenciesAmountTextView.setGravity(Gravity.END);

        // Add all views to the TableRow for agencies
        agenciesHeaderRow.addView(agenciesSerialTextView);
        agenciesHeaderRow.addView(agenciesParticularsTextView);
        agenciesHeaderRow.addView(agenciesAmountTextView);

        // Finally, add the TableRow to your agenciesTable
        agenciesTable.addView(agenciesHeaderRow);
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
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");
        DatabaseReference expenseRef = databaseRef.child("Expenses").child("ExpenseDetails").child(savedDate);

        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expensesTable.removeAllViews(); // Clear existing rows
                int totalAmount = 0;

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
                        totalAmount += amount;
                    }
                }

                // Load financial summary data
                loadFinancialSummaryData();

                addTotalRow(totalAmount);
                totalExpenseTextView.setText("Total Expense: ₹" + totalAmount);

                // Load currency data
                loadCurrencyData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(expense_history_for_selected_date.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addTotalRow(int totalAmount) {
        TableRow totalRow = new TableRow(this);

        TextView label = new TextView(this);
        label.setText("Total");
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        label.setTextColor(ContextCompat.getColor(this, R.color.black));
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(8, 8, 8, 8);
        label.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 12));
        label.setGravity(Gravity.END);

        TextView totalText = new TextView(this);
        totalText.setText("₹" + totalAmount);
        totalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        totalText.setTextColor(ContextCompat.getColor(this, R.color.black));
        totalText.setTypeface(null, Typeface.BOLD);
        totalText.setPadding(8, 8, 8, 8);
        totalText.setGravity(Gravity.END);
        totalText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));

        // Empty cell to match S.NO column space
        TextView emptyCell = new TextView(this);
        emptyCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));

        totalRow.addView(emptyCell);   // for S.NO column
        totalRow.addView(label);       // for PARTICULARS column
        totalRow.addView(totalText);   // for AMOUNT column

        expensesTable.addView(totalRow);
    }
    private void loadFinancialSummaryData() {
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");
        DatabaseReference summaryRef = databaseRef.child("Expenses").child("FinancialSummary").child(savedDate);

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
                Toast.makeText(expense_history_for_selected_date.this, "Failed to load financial summary.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadAgenciesExpensesFromDatabase() {
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");
        DatabaseReference expenseRef = databaseRef.child("Agencies").child("ExpenseDetails").child(savedDate);

        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                agenciesTable.removeAllViews(); // Clear existing rows
                int totalAmount = 0; // Initialize total

                if (!dataSnapshot.exists()) {
                    addAgencyExpenseRow(1, "", 0); // Ensure header is added even if no data exists
                } else {
                    for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                        int serial = expenseSnapshot.child("serial").getValue(Integer.class);
                        String details = expenseSnapshot.child("details").getValue(String.class);
                        int amount = expenseSnapshot.child("amount").getValue(Integer.class);
                        addAgencyExpenseRow(serial, details, amount);
                        totalAmount += amount;
                    }
                    addTotalAgencies(totalAmount);
                    totalAgencyExpenseTextView.setText("Total Agency Expense: ₹" + totalAmount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(expense_history_for_selected_date.this, "Failed to load agency expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addAgencyExpenseRow(int serial, String details, int amount) {
        if (agenciesTable.getChildCount() == 0) { // Ensure header exists
            addagenciesHeaderRow();
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
        agenciesTable.addView(newRow); // Add to agenciesTable instead of expensesTable
    }
    private void addTotalAgencies(int totalAmount) {
        TableRow totalRow = new TableRow(this);

        TextView label = new TextView(this);
        label.setText("Total");
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        label.setTextColor(ContextCompat.getColor(this, R.color.black));
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(8, 8, 8, 8);
        label.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 12));
        label.setGravity(Gravity.END);

        TextView totalText = new TextView(this);
        totalText.setText("₹" + totalAmount);
        totalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        totalText.setTextColor(ContextCompat.getColor(this, R.color.black));
        totalText.setTypeface(null, Typeface.BOLD);
        totalText.setPadding(8, 8, 8, 8);
        totalText.setGravity(Gravity.END);
        totalText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));

        // Empty cell to match S.NO column space
        TextView emptyCell = new TextView(this);
        emptyCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));

        totalRow.addView(emptyCell);   // for S.NO column
        totalRow.addView(label);       // for PARTICULARS column
        totalRow.addView(totalText);   // for AMOUNT column

        agenciesTable.addView(totalRow);
    }
    private void loadCurrencyData() {
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");
        DatabaseReference currencyRef = databaseRef.child("Expenses").child("CurrencyDenomination").child(savedDate);

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
                Toast.makeText(expense_history_for_selected_date.this, "Failed to load currency data.", Toast.LENGTH_SHORT).show();
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

    private void performEmailRequest() {
        fetchEmailsFromUserType("Admin", new EmailFetchCallback() {
            @Override
            public void onEmailsFetched(List<String> emails) {
                sendEmail(emails); // Your existing email sending logic
            }
        });
    }

    private void fetchEmailsFromUserType(String userType, EmailFetchCallback callback) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("users").child("Admin");

        List<String> emailList = new ArrayList<>();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        emailList.add(email);
                    }
                }
                callback.onEmailsFetched(emailList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to fetch emails", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public interface EmailFetchCallback {
        void onEmailsFetched(List<String> emails);
    }
    private void sendEmail(List<String> recipientEmails) {
        final String senderEmail = "ssgroupskolathur@gmail.com";
        final String senderPassword = "ipvh jrfj mgzr jxyr"; // App password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedDate = sharedPreferencess.getString("selected_date", "No Date Selected");

        // Enable debug to see SMTP logs
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
        session.setDebug(true); // 🔍 enables debug output in Logcat

        String link = "https://bit.ly/4dySExY";
        //String link = "hello";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail, "SS Groups Kolathur")); // better name

            // 🧠 Load user data
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "Guest");
            String userType = sharedPreferences.getString("userType", "Standard");
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // 📧 Set recipients
            InternetAddress[] recipientAddresses = new InternetAddress[recipientEmails.size()];
            for (int i = 0; i < recipientEmails.size(); i++) {
                recipientAddresses[i] = new InternetAddress(recipientEmails.get(i).trim());
            }
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);

            // 📝 Subject
            message.setSubject("💼 Requesting access - " + username + " (" + userType + ") - " + savedDate);

            // ✉️ Email body with fallback plain-text
            String htmlContent = "<h3>Hello,</h3>" +
                    "<p>Please review and Approve the access for the requested date :</p>" +
                    "<ul><li><strong>From:</strong> " + username + "</li>" +
                    "<li><strong>For:</strong> " + userType + "</li>" +
                    "<li><strong>For the Date:</strong> " + savedDate + "</li>" +
                    "<li><strong>Date:</strong> " + todayDate + "</li></ul>" +
                    "<p><a href=\"" + link + "\">👉 Click here to open the app</a></p>" +
                    "<br><p>Regards,<br>"+ username +"</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            new Thread(() -> {
                try {
                    Transport.send(message);
                   // runOnUiThread(() -> Toast.makeText(getApplicationContext(), "✅ Email sent successfully", Toast.LENGTH_SHORT).show());
                } catch (MessagingException e) {
                    e.printStackTrace();
                    Log.e("EMAIL_ERROR", "Error sending email: " + e.getMessage());
                    Log.e("EMAIL_ERROR", "Full error: ", e); // Full stack trace!

                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "❌ Failed to send email", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "❌ Error setting up email", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendApprovalRequest() {
        // Debugging: Check if method is called


        Toast.makeText(this, "Request Access clicked!", Toast.LENGTH_SHORT).show();

        // Retrieve values from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");
        String savedDate = sharedPreferences.getString("selected_date", "No Date Selected");

        // Create a reference to the 'Requests' node in Firebase
        requestRef = FirebaseDatabase.getInstance().getReference("Requests").child(userType).child(savedDate).child(username);

        // Create the data to be stored in Firebase
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("userId", username);
        requestData.put("userType", userType);
        requestData.put("requestedDate", savedDate);
        requestData.put("reason", "Requesting permission to edit old data");
        requestData.put("status", "pending");
        requestData.put("timestamp", ServerValue.TIMESTAMP);

        // Save the data in Firebase under the generated request ID
        requestRef.setValue(requestData)
                .addOnSuccessListener(aVoid -> {
                    // Show success message when data is stored successfully
                    Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show error message if data could not be stored
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        }else if (id == R.id.nav_log_out) {
            logoutUser();
        }else if (id == R.id.nav_View_approved_requests) {
            Intent i = new Intent(this, View_approved_requests.class);
            startActivity(i);
        }

        // Close drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.Req_Approval) {
            performEmailRequest();
            sendApprovalRequest();
            Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.req_approval, menu);
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