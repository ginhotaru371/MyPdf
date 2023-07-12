package com.gin371.mypdf;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FAVORITE_FILE_NAME = "favorite.json";
    private static final String RECENT_FILE_NAME = "recent.json";
    private DrawerLayout drawerLayout;
    HomeFragment homeFragment = new HomeFragment();
    RecentFragment recentFragment = new RecentFragment();
    FavoriteFragment favoriteFragment = new FavoriteFragment();
    boolean fileChecker = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        createDataFile();

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbarTop = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setTitle(R.string.all_documents);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    getSupportActionBar().setTitle(R.string.all_documents);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).addToBackStack(null).commit();
                    break;
                case R.id.nav_recent:
                    getSupportActionBar().setTitle(R.string.recents);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, recentFragment).addToBackStack(null).commit();
                    break;
                case R.id.nav_favorite:
                    getSupportActionBar().setTitle(R.string.favorites);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, favoriteFragment).addToBackStack(null).commit();
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbarTop, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_setting:

                break;
            case R.id.nav_about:
                openAboutDialog();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_recent:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, recentFragment).commit();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStackImmediate();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void openAboutDialog() {
        final Dialog aboutDialog = new Dialog(this);
        aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aboutDialog.setContentView(R.layout.about_dialog_layout);

        Window window = aboutDialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        aboutDialog.show();
    }

    public void createDataFile() {

        File fileFavorite = new File(this.getFilesDir(), FAVORITE_FILE_NAME);
        File fileRecent = new File(this.getFilesDir(), RECENT_FILE_NAME);

        if (!fileRecent.exists() && !fileFavorite.exists()) {
            try {
                FileOutputStream favoriteFile = openFileOutput(FAVORITE_FILE_NAME, MODE_PRIVATE);
                FileOutputStream recentFile = openFileOutput(RECENT_FILE_NAME, MODE_PRIVATE);

                favoriteFile.close();
                recentFile.close();

            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Can't Create data", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}