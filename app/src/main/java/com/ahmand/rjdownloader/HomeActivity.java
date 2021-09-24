package com.ahmand.rjdownloader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawer;
    NavigationView navigationView;
    FrameLayout frameLayout;
    ActionBarDrawerToggle toggle;
    ImageView imageView;
    Toolbar toolbar;
    View header;
    TextView app_version;

    public static boolean isVpnConnectionActive() {
        List<String> networks = new ArrayList<>();

        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp()) {
                    networks.add(networkInterface.getName());
                }
            }
        } catch (Exception ignored) {
        }

        return networks.contains("tun0") || networks.contains("ppp") || networks.contains("pptp");
    }

    private SharedPreferences SavePreference() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager Themes = new ThemeManager();
        Themes.ApplyTheme(this);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        frameLayout = findViewById(R.id.frame);
        header = navigationView.getHeaderView(0);
        imageView = header.findViewById(R.id.thumb);
        app_version = findViewById(R.id.app_version);
        app_version.setText("RJ Downloader : v" + BuildConfig.VERSION_NAME);
        imageView.setOnClickListener(v -> drawer.closeDrawer(GravityCompat.START));
        File AppDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RadioJavan");
        if (!AppDir.exists()) {
            AppDir.mkdirs();
        }
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //Load Default Fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame, new HomeFragment());
        transaction.commit();
        //End of codes
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.nav_downloads) {
                loadFragment(new DownloadsFragment());
            } else if (id == R.id.nav_settings) {
                CharSequence[] Theme = new CharSequence[]{"Light", "Dark"};
                int checked = 0;
                if (Themes.GetCurrent(this).equals("black")) {
                    checked = 1;
                }
                MaterialAlertDialogBuilder Dialog = new MaterialAlertDialogBuilder(this);
                Dialog.setTitle(R.string.theme_title);
                Dialog.setSingleChoiceItems(Theme, checked, (dialog, which) -> {
                    if (which == 0) {
                        SavePreference().edit().putString("theme", "white").apply();
                        setTheme(R.style.Theme_RJDownloader);
                    } else if (which == 1) {
                        SavePreference().edit().putString("theme", "black").apply();
                        setTheme(R.style.Theme_RJDownloader_Dark);

                    }
                });
                Dialog.setPositiveButton(R.string.save_theme, (dialog, which) -> recreate());
                Dialog.show();
            } else if (id == R.id.nav_about) {
                MaterialAlertDialogBuilder Dialog = new MaterialAlertDialogBuilder(this);
                Dialog.setTitle(R.string.about_title);
                Dialog.setMessage(R.string.about_text);
                Dialog.setNeutralButton("VPN Pro", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/xvpnpro")));
                    } catch (Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.telegram_not, Snackbar.LENGTH_SHORT).show();
                    }
                });
                Dialog.setNegativeButton("IC Mods", ((dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/ic_mods")));
                    } catch (Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.telegram_not, Snackbar.LENGTH_SHORT).show();
                    }
                }));

                Dialog.setCancelable(true);
                Dialog.show();
            } else if (id == R.id.nav_exit) {
                this.finishAffinity();
            } else if (id == R.id.nav_help) {
                Intent intent = new Intent(this, HelpActivity.class);
                this.startActivity(intent);
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
        if (!isVpnConnectionActive()) {

            MaterialAlertDialogBuilder Dialog = new MaterialAlertDialogBuilder(this);
            Dialog.setMessage(R.string.vpn_dialog);
            Dialog.setPositiveButton("Okay", null);
            Dialog.show();
        }

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyHavePermission()) {
                requestForSpecificPermission();
            }
        }
        GetShared();
    }

    public String GetShared() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String result = "";
        if (Intent.ACTION_SEND.equals(action) && type != null) if ("text/plain".equals(type)) {
            result = handleSendText(intent);
        }
        return result;
    }

    public String handleSendText(Intent intent) {
        return intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 101) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.telegram) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/ic_mods")));
        } else if (item.getItemId() == R.id.instagram) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/ah_mand")));
        } else if (item.getItemId() == R.id.github) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Incognito-Coder/")));
        } else if (item.getItemId() == R.id.website) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mr-incognito.ir")));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}