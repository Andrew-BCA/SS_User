package com.example.ss_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class expense_history_edit extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_history_edit);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        edit = findViewById(R.id.toolbar_menu1);

        edit.setOnClickListener(v -> {
            Intent i = new Intent(expense_history_edit.this, expense_history_save.class);
            startActivity(i);
        });
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
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create Hamburger Menu Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }





  @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

      if (id == R.id.nav_home) {
          Intent i = new Intent(expense_history_edit.this, expense_history_edit.class);
          startActivity(i);
      } else if (id == R.id.nav_create_user) {
          Toast.makeText(this, "Create User Clicked", Toast.LENGTH_SHORT).show();
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
