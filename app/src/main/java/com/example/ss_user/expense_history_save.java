package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.util.TypedValue;
import android.widget.Toast;
import android.view.Gravity;
import android.text.InputType;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import androidx.core.content.ContextCompat;

public class expense_history_save extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView saveButton;
    private TableLayout expensesTable, currencyTable, financialSummaryTable;
    private TextView currencyDropdown, financialSummaryDropdown;
    private DatabaseReference databaseRef;
    private int serialCounter = 0; // Global counter for serial numbers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_history_save);
        // Retrieve SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        // Use the retrieved data
      //  Toast.makeText(this, "Logged in as: " + username + " (" + userType + ")", Toast.LENGTH_LONG).show();


        // Initialize Firebase Database
        databaseRef = FirebaseDatabase.getInstance().getReference(userType).child("Expenses");

        // Initialize UI components
        initializeUIComponents();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Make sure this is called!

        // Load existing expenses from Firebase
        loadExpensesFromDatabase();

        // Add the header row
        //addHeaderRow();
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
        setSupportActionBar(toolbar);
        // Remove the title from the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Removes title from support action bar
        toolbar.setTitle(""); // Removes title from the toolbar
        toolbar.setSubtitle(""); // Removes any subtitle
        saveButton = findViewById(R.id.toolbar_menu1);
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

        // Save Expenses
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveExpensesToDatabase());
        } else {
            Log.e("ERROR", "toolbar_menu1 not found! Check XML.");
        }


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

        // Create the ADD ImageView
        ImageView addImageView = new ImageView(this);
        addImageView.setId(View.generateViewId()); // Generate a unique ID for the ImageView
        addImageView.setImageResource(R.drawable.plus);
        addImageView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        addImageView.setPadding(8, 8, 8, 8);

        // Set OnClickListener for the addImageView
        addImageView.setOnClickListener(v -> addExpenseRow());

        // Add all views to the TableRow
        tableRow.addView(serialTextView);
        tableRow.addView(particularsTextView);
        tableRow.addView(amountTextView);
        tableRow.addView(addImageView);

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
    private void addExpenseRow() {
        // If only the header row exists, reset counter
        if (expensesTable.getChildCount() == 1) {
            serialCounter = 0;
        }
        addExpenseRow(++serialCounter, "", 0);
    }
    private void addExpenseRow(int serial, String details, int amount) {

            if (expensesTable.getChildCount() == 0) { // Ensure header exists
                addHeaderRow();
            }


            // Create a new TableRow programmatically
        TableRow newRow = new TableRow(this);

        EditText serialNo = new EditText(this);
        serialNo.setText(String.valueOf(serial));  // Set incremented serial number
        serialNo.setHintTextColor(ContextCompat.getColor(this, R.color.black));
        serialNo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        serialNo.setPadding(8, 8, 8, 8);
        serialNo.setTextColor(ContextCompat.getColor(this, R.color.black));
        serialNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        serialNo.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));

        EditText particulars = new EditText(this);
        particulars.setHint("Enter details");
        particulars.setHintTextColor(ContextCompat.getColor(this, R.color.black));
        particulars.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        particulars.setPadding(8, 8, 8, 8);
        particulars.setTextColor(ContextCompat.getColor(this, R.color.black));
        particulars.setInputType(InputType.TYPE_CLASS_TEXT);
        particulars.setGravity(Gravity.CENTER);
        particulars.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 7));
        particulars.setText(details); // Set details

        EditText amountField = new EditText(this);
        amountField.setHint("0.00");
        amountField.setHintTextColor(ContextCompat.getColor(this, R.color.black));
        amountField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        amountField.setPadding(8, 8, 8, 8);
        amountField.setTextColor(ContextCompat.getColor(this, R.color.black));
        amountField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountField.setGravity(Gravity.END);
        amountField.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        amountField.setText(String.valueOf(amount)); // Set amount

        ImageView deleteButton = new ImageView(this);
        deleteButton.setImageResource(R.drawable.delete);
        deleteButton.setPadding(8, 8, 8, 8);
        deleteButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        deleteButton.setPadding(0, 10, 0, 0);

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

        // DELETE FUNCTIONALITY
        // DELETE FUNCTIONALITY
        deleteButton.setOnClickListener(v -> {
            expensesTable.removeView(newRow); // Remove row from UI
            newExpenseRef.removeValue(); // Remove from Firebase
            updateSerialNumbers(); // Update serial numbers dynamically

            // If all rows (except header) are deleted, reset counter
            if (expensesTable.getChildCount() == 1) { // Only header exists
                serialCounter = 0;
            }
        });


        newRow.addView(serialNo);
        newRow.addView(particulars);
        newRow.addView(amountField);
        newRow.addView(deleteButton);

        expensesTable.addView(newRow);
    }
    private void updateSerialNumbers() {
        TableLayout tableLayout = findViewById(R.id.expenses);
        int count = tableLayout.getChildCount();

        if (count <= 1) return; // If only the header exists, no update needed

        int newSerial = 1; // Start numbering from 1
        for (int i = 1; i < count; i++) { // Skip header row
            View row = tableLayout.getChildAt(i);
            if (row instanceof TableRow) {
                View firstChild = ((TableRow) row).getChildAt(0);
                if (firstChild instanceof EditText) { // Ensure it's an EditText before casting
                    ((EditText) firstChild).setText(String.valueOf(newSerial++));
                }
            }
        }
    }
    private void saveExpensesToDatabase() {
        DatabaseReference expenseRef = databaseRef.child("ExpenseDetails");

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int rowCount = expensesTable.getChildCount();
        if (rowCount <= 0) {
            Toast.makeText(this, "No expenses to save", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 1; i < rowCount; i++) {
            TableRow row = (TableRow) expensesTable.getChildAt(i);

            EditText serialNo = (EditText) row.getChildAt(0);
            EditText particulars = (EditText) row.getChildAt(1);
            EditText amount = (EditText) row.getChildAt(2);

            try {
                int serial = Integer.parseInt(serialNo.getText().toString().trim());
                int price = Integer.parseInt(amount.getText().toString().trim());
                String details = particulars.getText().toString().trim();

                if (!details.isEmpty()) {
                    Map<String, Object> expenseData = new HashMap<>();
                    expenseData.put("serial", serial);
                    expenseData.put("details", details);
                    expenseData.put("amount", price);

                    // Store under "expenses -> serialNo -> data"
                    expenseRef.child(todayDate).child(String.valueOf(serial)).setValue(expenseData);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input: Serial No & Amount must be numbers", Toast.LENGTH_SHORT).show();
            }
        }

        saveCurrencyData();
        saveFinancialSummaryData();

        Toast.makeText(this, "Expenses saved successfully!", Toast.LENGTH_SHORT).show();
    }
    private void saveCurrencyData() {
        DatabaseReference currencyRef = databaseRef.child("CurrencyDenomination");

        Map<String, Object> currencyData = new HashMap<>();
        currencyData.put("500", getTextValue(R.id.fivehundred));
        currencyData.put("200", getTextValue(R.id.twohundred));
        currencyData.put("100", getTextValue(R.id.hundred));
        currencyData.put("50", getTextValue(R.id.fifty));
        currencyData.put("20", getTextValue(R.id.twenty));
        currencyData.put("10", getTextValue(R.id.ten));
        currencyData.put("Coins", getTextValue(R.id.coins));
        currencyData.put("ExtraCoins", getTextValue(R.id.Extracoins));
        currencyData.put("Others", getTextValue(R.id.others));
        currencyData.put("TotalSales", getTextValue(R.id.totalsales));

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        currencyRef.child(todayDate).setValue(currencyData);
    }
    private void saveFinancialSummaryData() {
        DatabaseReference summaryRef = databaseRef.child("FinancialSummary");

        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("OpeningCashInHand", getTextValue(R.id.opening_cash_in_hand));
        summaryData.put("TotalSales", getTextValue(R.id.totalsalesFS));
        summaryData.put("ReceivedFromOwner", getTextValue(R.id.receivedfromowner));
        summaryData.put("CreditReceived", getTextValue(R.id.creditreceived));
        summaryData.put("Expenses", getTextValue(R.id.expensesfs));
        summaryData.put("UPIPayments", getTextValue(R.id.upipayments));
        summaryData.put("CreditSales", getTextValue(R.id.creditsales));
        summaryData.put("AmountInHand", getTextValue(R.id.amountinhand));
        summaryData.put("EndCash", getTextValue(R.id.endcash));

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        summaryRef.child(todayDate).setValue(summaryData);
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
                Toast.makeText(expense_history_save.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(expense_history_save.this, "Failed to load financial summary.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(expense_history_save.this, "Failed to load currency data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setTextValue(int id, String value) {
        EditText editText = findViewById(id);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            // Handle Save button click
            saveExpensesToDatabase();
            return true;
        } else if (id == R.id.action_add_row) {
            // Handle Add Row button click
            addExpenseRow();
            return true;
        }

        return super.onOptionsItemSelected(item);
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