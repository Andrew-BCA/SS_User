package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class expense_history_date_selection extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView tvSelectedDate;
    private Button btnSelectDate;
    private String userSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_history_date_selection);

        // Retrieve user source from intent
        userSource = getIntent().getStringExtra("user_source");

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

        // Initialize Date Picker components
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        btnSelectDate = findViewById(R.id.btn_select_date);

        // Set Click Listener for Date Picker Button
        btnSelectDate.setOnClickListener(v -> showMaterialDatePicker(v));
    }

    private void showMaterialDatePicker(View anchorView) {

        // Build Material Date Picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert selected date to formatted String
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(new Date(selection));

            // Save selected date in SharedPreferences
            saveSelectedDate(formattedDate);

            // Set the selected date in TextView
            tvSelectedDate.setText("Selected Date: " + formattedDate);

            // Show Popup only if the user is NOT from "store"
            if (!"store".equals(userSource)) {
                Intent i = new Intent(this, expense_history_for_selected_date.class);
                startActivity(i);
            } else {
                Intent i = new Intent(this, expense_history_edit.class);
                startActivity(i);
            }
        });
    }

    // Method to Save Date in SharedPreferences
    private void saveSelectedDate(String date) {
        SharedPreferences sharedPreferencess = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencess.edit();
        editor.putString("selected_date", date);
        editor.apply();
    }
  /*  private void showPopup(View anchorView) {
        // Inflate the popup layout
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_expenses_or_agencies, null);

        TextView tvPopupMessage = popupView.findViewById(R.id.tvPopupMessage);


        // Get screen width dynamically
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.4); // 40% of screen height

        // Create PopupWindow with dynamic size
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Show popup in the center of the screen
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // Initialize buttons
        Button btnEdit = popupView.findViewById(R.id.btnexpenses);
        Button btnDelete = popupView.findViewById(R.id.btnagencies);

        // Set button click listeners
        btnEdit.setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Expenses Clicked", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(expense_history_date_selection.this, expense_history_for_selected_date.class);
            startActivity(i);
        });

        btnDelete.setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Agencies Clicked", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(expense_history_date_selection.this, agencies_history_for_selected_date.class);
            startActivity(i);
        });
    }*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent i = new Intent(this, expense_history_edit.class);
            startActivity(i);
        } else if (id == R.id.nav_create_user) {
            Toast.makeText(this, "Create User Clicked", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, expense_history_date_selection.class);
            startActivity(i);
        }else if (id == R.id.nav_log_out) {
            logoutUser();
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
