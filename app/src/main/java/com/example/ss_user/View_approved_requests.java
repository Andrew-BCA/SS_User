package com.example.ss_user;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class View_approved_requests extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    ApprovalRequestAdapter adapter;
    List<ApprovalRequest> requestList = new ArrayList<>();
    TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_approved_requests);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(userType);

        Toolbar toolbar = findViewById(R.id.toolbar);

        recyclerView = findViewById(R.id.requestsRecyclerView);
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApprovalRequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);

        loadRequests();

        // Create Hamburger Menu Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void loadRequests() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        String userType = sharedPreferences.getString("userType", "Standard");

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Approved").child(userType);

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();

                for (DataSnapshot dateSnap : snapshot.getChildren()) {
                    DataSnapshot userSnap = dateSnap.child(username);
                    if (userSnap.exists()) {
                        ApprovalRequest request = userSnap.getValue(ApprovalRequest.class);
                        if (request != null) {
                            requestList.add(request);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_approved_requests.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
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

}
