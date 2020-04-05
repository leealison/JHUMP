package com.example.jhump;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Build;
import android.view.MenuItem;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment allListings;
    private Fragment createListings;
    private Fragment myListings;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        allListings = new AllListings();
        createListings = new CreateListings();
        myListings = new MyListings();


        getSupportFragmentManager().beginTransaction()
                .add(R.id.nav_host_fragment, allListings).commit();

        NavigationView navigationView = findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        //TextView name = header.findViewById(R.id.myName);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.allListings) {
            getSupportActionBar().setTitle("All Listings");
            transaction.replace(R.id.nav_host_fragment, allListings);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.createListings) {
            getSupportActionBar().setTitle("Create Listing");
            transaction.replace(R.id.nav_host_fragment, createListings);
            transaction.addToBackStack(null);
            transaction.commit();

        } else if (id == R.id.myListings) {
            getSupportActionBar().setTitle("My Listings");
            transaction.replace(R.id.nav_host_fragment, myListings);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.profile) {

        } else if (id == R.id.signOut) {
            SharedPreferences userInfo = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
            userInfo.edit().putBoolean("logged", false).apply();
            //upload all settings to database
            //clear all saved info up to this point
            Intent backToMain = new Intent(NavigationDrawer.this, MainActivity.class);
            startActivity(backToMain);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}