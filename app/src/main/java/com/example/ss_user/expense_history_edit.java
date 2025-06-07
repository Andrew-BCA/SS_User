package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class expense_history_edit extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView EDIT;
    private TableLayout expensesTable, agenciesTable, currencyTable, financialSummaryTable;
    private Button Request;
    private TextView currencyDropdown, financialSummaryDropdown,totalExpenseTextView,totalAgencyExpenseTextView;
    private DatabaseReference databaseRef, agenciesRef;
    private int serialCounter = 0; // Global counter for serial numbers
    private int serialCounterExpenses = 0; // Counter for expenses
    private int serialCounterAgencies = 0; // Counter for agencies

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_history_edit);


        // Retrieve SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        String type = getIntent().getStringExtra("type");

        Button fabChat = findViewById(R.id.fab_chat);

        fabChat.setOnClickListener(v -> {
            showChatPopup();
        });

        SharedPreferences prefs = getSharedPreferences("SSAppPrefs", MODE_PRIVATE);
        String branch = prefs.getString("selected_branch", "Default");

        // Initialize Firebase Database
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        databaseRef = FirebaseDatabase.getInstance().getReference(userType);
        agenciesRef = databaseRef.child("Agencies").child("ExpenseDetails").child(todayDate);


        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(userType);

        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        totalAgencyExpenseTextView = findViewById(R.id.totalAgencyExpenseTextView);
       Request = findViewById(R.id.button_dang);
        Request.setOnClickListener(view -> {
            fetchEmailsFromUserType("Admin", new EmailFetchCallback() {
                @Override
                public void onEmailsFetched(List<String> emails) {
                    // Now you have the list of emails. For example:
                    sendEmail(emails);  // Use your existing sendEmail() method
                }
            });

            Toast.makeText(expense_history_edit.this, "Request Sent", Toast.LENGTH_SHORT).show();
        });

        EDIT = findViewById(R.id.toolbar_menu1);
        EDIT.setOnClickListener(view -> {
            Intent intent = new Intent(expense_history_edit.this, expense_history_save.class);
            startActivity(intent);
        });

        // Initialize UI components
        initializeUIComponents();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load existing expenses from Firebase
        loadExpensesFromDatabase();
        loadAgenciesExpensesFromDatabase();

        addHeaderRow();

        getNextSerialNumberFromDB();
        reorderSerialsAndUpdateFirebase();

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
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create Hamburger Menu Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        currencyDropdown.setOnClickListener(v -> toggleVisibility(currencyTable, currencyDropdown, "Currency Denomination"));
        financialSummaryDropdown.setOnClickListener(v -> toggleVisibility(financialSummaryTable, financialSummaryDropdown, "Financial Summary"));

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
            message.setSubject("💼 Daily Expenses - " + username + " (" + userType + ") - " + todayDate);

            // ✉️ Email body with fallback plain-text
            String htmlContent = "<h3>Hello,</h3>" +
                    "<p>Please review today's submitted expenses:</p>" +
                    "<ul><li><strong>From:</strong> " + username + "</li>" +
                    "<li><strong>For:</strong> " + userType + "</li>" +
                    "<li><strong>Date:</strong> " + todayDate + "</li></ul>" +
                    "<p><a href=\"" + link + "\">👉 Click here to open the app</a></p>" +
                    "<br><p>Regards,<br>SS Groups Kolathur</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            new Thread(() -> {
                try {
                    Transport.send(message);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "✅ Email sent successfully", Toast.LENGTH_SHORT).show());
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
    private void sendrevisionmail(List<String> recipientEmails) {
        final String senderEmail = "ssgroupskolathur@gmail.com";
        final String senderPassword = "ipvh jrfj mgzr jxyr";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        String link = "https://bit.ly/4dySExY";
        //String link = "Hello";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "Guest");
            String userType = sharedPreferences.getString("userType", "Standard");


            // ✅ Convert list of recipient emails into InternetAddress array
            InternetAddress[] recipientAddresses = new InternetAddress[recipientEmails.size()];
            for (int i = 0; i < recipientEmails.size(); i++) {
                recipientAddresses[i] = new InternetAddress(recipientEmails.get(i).trim());
            }
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);

            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            message.setSubject("Message From "+ username + " for " + userType + " on " + todayDate);

            String htmlContent = "<h3>Kindly check and clear the query:</h3>"+
                    "<p>From : " + username + "</p>" + "<p>For : " + userType + "</p>"
                    + "<p><a href=\"" + link + "\">Click here to open the app</a></p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            /*new Thread(() -> {
                try {
                    Transport.send(message);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Email sent successfully", Toast.LENGTH_SHORT).show());
                } catch (MessagingException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show());
                }
            }).start();*/

        } catch (MessagingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show();
        }
    }
    private void showChatPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_chat, null);
        builder.setView(dialogView);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        AlertDialog chatDialog = builder.create();
        chatDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // optional: make corners soft
        chatDialog.show();

        // Now set up everything inside the dialog
        EditText editTextMessage = dialogView.findViewById(R.id.editTextMessage);
        ImageButton buttonSend = dialogView.findViewById(R.id.buttonSend);
        RecyclerView recyclerViewChat = dialogView.findViewById(R.id.recyclerViewChat);

        List<ChatMessage> chatList = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(chatList,userType);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("chat_remarks").child(userType)
                .child(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        // Load chat history
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        chatList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(chatList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Sending message
        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                String id = chatReference.push().getKey();
                ChatMessage message = new ChatMessage(id, text, userType, System.currentTimeMillis());

                if (id != null) {
                    chatReference.child(id).setValue(message);
                    fetchEmailsFromUserType("Admin", new EmailFetchCallback() {
                        @Override
                        public void onEmailsFetched(List<String> emails) {
                            // Now you have the list of emails. For example:
                            sendrevisionmail(emails);  // Use your existing sendEmail() method
                        }
                    });
                }
                editTextMessage.setText("");
            }
        });
    }
    private void initializeUIComponents() {
        // Initialize Toolbar & Save Button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove the title from the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Removes title from support action bar
        toolbar.setTitle(""); // Removes title from the toolbar
        toolbar.setSubtitle(""); // Removes any subtitle
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
    private void addExpenseRow() {
        serialCounterExpenses++;
        addExpenseRow(serialCounterExpenses, "", 0);
    }
    private void addExpenseRow(int serial, String details, int amount) {

        if (serial <= 0) {
            serial = 1;
        }

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
        DatabaseReference newExpenseRef = databaseRef.child("Expenses").child("ExpenseDetails").child(todayDate).child(String.valueOf(serial));
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("serial", serial);
        expenseData.put("details", details);
        expenseData.put("amount", amount);
        newExpenseRef.setValue(expenseData);


        newRow.addView(serialNo);
        newRow.addView(particulars);
        newRow.addView(amountField);

        expensesTable.addView(newRow);
    }
    private void loadExpensesFromDatabase() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference expenseRef = databaseRef.child("Expenses").child("ExpenseDetails").child(todayDate);

        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expensesTable.removeAllViews(); // Clear existing rows
                int totalAmount = 0; // Initialize total

                if (!dataSnapshot.exists()) {
                    // If no data exists, add an empty row with serial 1
                    addExpenseRow(1, "", 0);
                } else {
                    for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                        Integer serialObj = expenseSnapshot.child("serial").getValue(Integer.class);
                        Integer amountObj = expenseSnapshot.child("amount").getValue(Integer.class);
                        String details = expenseSnapshot.child("details").getValue(String.class);

                        int serial = (serialObj != null) ? serialObj : 0;
                        // Skip entries with serial 0 or negative
                        if (serial <= 0) {
                            continue;
                        }

                        int amount = (amountObj != null) ? amountObj : 0;
                        if (details == null) details = "";

                        addExpenseRow(serial, details, amount);
                        totalAmount += amount;

                        // Update the serial counter
                        if (serial > serialCounterExpenses) {
                            serialCounterExpenses = serial;
                        }
                    }

                    // If no valid rows were added, add one default row
                    if (expensesTable.getChildCount() <= 1) { // 1 for header
                        addExpenseRow(1, "", 0);
                    }
                }
                addTotalRow(totalAmount);
                totalExpenseTextView.setText("Total Expense: ₹" + totalAmount);

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
    private void addAgenciesRow() {
        if (agenciesTable.getChildCount() == 0) {
            addagenciesHeaderRow();
        }
        serialCounterAgencies++;
        addAgenciesRow(serialCounterAgencies, "", 0);
    }
    private void addAgenciesRow(int serial, String details, int amount) {

        if (serial <= 0) {
            serial = 1;
        }

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
        DatabaseReference newExpenseRef = databaseRef.child("Agencies").child("ExpenseDetails").child(todayDate).child(String.valueOf(serial));
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

        agenciesTable.addView(newRow);
    }
    private void getNextSerialNumberFromDB() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference expenseRef = databaseRef.child("Expenses").child("ExpenseDetails").child(todayDate);

        expenseRef.orderByChild("serial").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxSerial = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Integer serial = child.child("serial").getValue(Integer.class);
                    if (serial != null && serial > maxSerial) {
                        maxSerial = serial;
                    }
                }
                serialCounter = maxSerial; // Update global serialCounter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(expense_history_edit.this, "Error getting max serial", Toast.LENGTH_SHORT).show();
            }
        });
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
        saveAgenciesExpensesToDatabase();

        Toast.makeText(this, "Expenses saved successfully!", Toast.LENGTH_SHORT).show();
    }
    private void saveAgenciesExpensesToDatabase() {
        DatabaseReference expenseRef = databaseRef.child("Agencies").child("ExpenseDetails");

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int rowCount = agenciesTable.getChildCount();
        if (rowCount <= 0) {
            Toast.makeText(this, "No expenses to save", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 1; i < rowCount; i++) {
            TableRow row = (TableRow) agenciesTable.getChildAt(i);

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
    private void loadFinancialSummaryData() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference summaryRef = databaseRef.child("Expenses").child("FinancialSummary").child(todayDate);

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
        DatabaseReference currencyRef = databaseRef.child("Expenses").child("CurrencyDenomination").child(todayDate);

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
        DatabaseReference newExpenseRef = databaseRef.child("Agencies").child("ExpenseDetails").child(todayDate).child(String.valueOf(serial));
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("serial", serial);
        expenseData.put("details", details);
        expenseData.put("amount", amount);
        newExpenseRef.setValue(expenseData);


        newRow.addView(serialNo);
        newRow.addView(particulars);
        newRow.addView(amountField);
        agenciesTable.addView(newRow); // Add to agenciesTable instead of expensesTable

    }
    private void loadAgenciesExpensesFromDatabase() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference expenseRef = databaseRef.child("Agencies").child("ExpenseDetails").child(todayDate);

        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                agenciesTable.removeAllViews(); // Clear existing rows
                int totalAmount = 0; // Initialize total

                if (!dataSnapshot.exists()) {
                    addAgencyExpenseRow(1, "", 0); // Start with serial 1
                } else {
                    for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Integer serial = expenseSnapshot.child("serial").getValue(Integer.class);
                            String details = expenseSnapshot.child("details").getValue(String.class);
                            Integer amount = expenseSnapshot.child("amount").getValue(Integer.class);

                            // Skip entries with serial 0 or negative
                            if (serial == null || serial <= 0) {
                                continue;
                            }

                            String safeDetails = details != null ? details : "";
                            int safeAmount = amount != null ? amount : 0;

                            addAgencyExpenseRow(serial, safeDetails, safeAmount);
                            totalAmount += safeAmount;

                            // Update the serial counter
                            if (serial > serialCounterAgencies) {
                                serialCounterAgencies = serial;
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseError", "Error parsing expense data: " + e.getMessage());
                        }
                    }

                    // If no valid rows were added, add one default row
                    if (agenciesTable.getChildCount() <= 1) { // 1 for header
                        addAgencyExpenseRow(1, "", 0);
                    }
                }
                addTotalAgencies(totalAmount);
                totalAgencyExpenseTextView.setText("Total Agency Expense: ₹" + totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database read failed: " + error.getMessage());
                Toast.makeText(expense_history_edit.this, "Failed to load agency expenses", Toast.LENGTH_SHORT).show();
            }
        });
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
    private void reorderSerialsAndUpdateFirebase() {
        TableLayout tableLayout = findViewById(R.id.expenses);
        int count = tableLayout.getChildCount();

        if (count <= 1) return; // Only header exists

        int serialNumber = 1;
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference expenseRef = databaseRef.child("Agencies").child("ExpenseDetails").child(todayDate);

        Map<String, Object> updatedEntries = new HashMap<>();
        List<String> oldSerialsToDelete = new ArrayList<>();

        for (int i = 1; i < count; i++) { // Skip header
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            EditText serialField = (EditText) row.getChildAt(0);
            EditText detailsField = (EditText) row.getChildAt(1);
            EditText amountField = (EditText) row.getChildAt(2);

            String oldSerial = serialField.getText().toString().trim();

            // UI update
            serialField.setText(String.valueOf(serialNumber));

            String detailText = detailsField.getText().toString().trim();
            int amount = amountField.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(amountField.getText().toString().trim());

            Map<String, Object> entry = new HashMap<>();
            entry.put("serial", serialNumber);
            entry.put("details", detailText);
            entry.put("amount", amount);
            updatedEntries.put(String.valueOf(serialNumber), entry);

            if (!oldSerial.equals(String.valueOf(serialNumber))) {
                oldSerialsToDelete.add(oldSerial);
            }

            serialNumber++;
        }

        // Update serial counter
        serialCounter = serialNumber - 1;

        // Remove old serials (those that changed position)
        for (String oldSerial : oldSerialsToDelete) {
            expenseRef.child(oldSerial).removeValue();
        }

        // Write updated ones
        expenseRef.updateChildren(updatedEntries).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Serials synced with DB", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getTextValue(int id) {
        TextView editText = findViewById(id);
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
        } else if (id == R.id.action_add_row_agencies) {
            addAgenciesRow();

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