package com.proximadev.flyso.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.proximadev.flyso.R;
import com.proximadev.flyso.fragments.FacebookFragment;
import com.proximadev.flyso.fragments.InstagramFragment;
import com.proximadev.flyso.fragments.TwitterFragment;
import com.proximadev.flyso.others.CustomViewPager;
import com.proximadev.flyso.others.Helpers;
import com.proximadev.flyso.others.ViewPagerAdapter;
import com.proximadev.flyso.pins.PinCompatActivity;
import com.proximadev.flyso.pins.managers.AppLock;
import com.proximadev.flyso.utils.ThemeUtils;
import com.proximadev.flyso.utils.UserInfo;
import com.proximadev.flyso.utils.Utility;

import java.io.File;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;

public class MainActivity extends PinCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private TabLayout mTabLayout;
    private CustomViewPager mViewPager;
    private ViewPagerAdapter adapter;
    private SharedPreferences prefs;

    // Milliseconds, desired time passed between two back presses
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ThemeUtils.setTheme(this, prefs);

        if (prefs.getBoolean("RotationLock", true))
            // Make screen Portrait to disable Landscape orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (prefs.getBoolean("FullScreen",false))
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus();
        }

        switch (prefs.getString("TabsPosition", "Bottom")) {
            case "Top":
                setContentView(R.layout.activity_main_top_tab);
                break;
            case "Hide":
                setContentView(R.layout.activity_main_bottom_tab);
                findViewById(R.id.appbar).setVisibility(View.GONE);
                break;
            case "Bottom":
            default:
                    setContentView(R.layout.activity_main_bottom_tab);
                    break;
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//remove the keyboard issue

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView greeting = navigationView.getHeaderView(0).findViewById(R.id.greet);
        greeting.setText(getGreeting());

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.viewpager);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
        setupTabs();

        if (!prefs.getBoolean("Swipe", false))
            mViewPager.disableScroll();

        noNetworksTabs();

        if (prefs.getBoolean("FlysoLocker",false)) {
            Intent intent = new Intent(MainActivity.this, CustomPinActivity.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("FacebookDetails", false)) {
            if (Helpers.getCookie("https://m.facebook.com/", "c_user") != null && !prefs.getBoolean("save_data", false)) {
                new UserInfo(MainActivity.this).execute();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prefs.getBoolean("ClearCache", false))
            deleteCache(getCacheDir());
    }

    private void noNetworksTabs() {

        if (!prefs.getBoolean("Facebook", true)
                && !prefs.getBoolean("Instagram", true)
                && !prefs.getBoolean("Twitter", true)
                && !prefs.getBoolean("Google+", true)){

            Utility.showGuideDialog(MainActivity.this, prefs, getString(R.string.notab), getString(R.string.notab_text), null)
                    .setCancelable(false)
                    .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        finish();
                    })
                    .show();
        }
    }

    private void setupTabs() {
        // Set the right color the the tabs on start
        setColor(0);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int mTabPosition = tab.getPosition();
                mViewPager.setCurrentItem(mTabPosition);
                setColor(mTabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                // No needed method
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                // No needed method

            }
        });

    }

    @SuppressWarnings("ConstantConditions")
    private void setColor(int mTabPosition) {
        for (int i = 0; i< adapter.getFragmentList().size(); i++) {
            if (i != mTabPosition) {
                mTabLayout.getTabAt(i).setIcon(R.drawable.ic_small_dot);
            }
            else {
                mTabLayout.getTabAt(i).setIcon(R.drawable.ic_dot);
            }
        }
        changeColor(ThemeUtils.getColor(this));
    }

    private void changeColor(int color) {
        // Change status bar color to match theme on 21 and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter.clearList();

        if (prefs.getBoolean("Facebook",true))
            adapter.addFragment(new FacebookFragment());
        if (prefs.getBoolean("Instagram",true))
            adapter.addFragment(new InstagramFragment());
        if (prefs.getBoolean("Twitter",true))
            adapter.addFragment(new TwitterFragment());
        adapter.notifyDataSetChanged();

        // Lock the fragments in memory
        viewPager.setOffscreenPageLimit(adapter.getCount());
        viewPager.setAdapter(adapter);
    }

    public boolean isPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                RequestStoragePermission();
                return false;
            }
        } else {
            // Permission is automatically granted on sdk < 23 upon installation
            return true;
        }
    }

    private void RequestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);

        else if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            super.onBackPressed();
        else {
            Toasty.info(this, getString(R.string.exit_app), 0, false).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteCache(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteCache(child);

        fileOrDirectory.delete();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                finish();
                break;
            case R.id.nav_feedback:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"proximadevteam@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "FlySo");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.from)+"\n" + Helpers.getDeviceInfo(this) + "\n\n (Write your feedback here)");
                startActivity(Intent.createChooser(intent, getString(R.string.send)));
                break;
            case R.id.nav_facebook:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/proximadev")));
            case R.id.nav_close:
                MainActivity.super.onBackPressed();
                break;
            case R.id.nav_support:
                Utility.showGuideDialog(MainActivity.this, prefs, getString(R.string.paypal_support), getString(R.string.paypal_support_message),null)
                        .setPositiveButton(getString(R.string.paypal_support), (dialog, id) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/RamiSaadGhani"))))
                        .setNegativeButton(R.string.no_thanks, (dialog, id) -> dialog.dismiss())
                        .show();
                break;
            case R.id.nav_playstore:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6002093717124717681")));
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getGreeting(){
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay <= 12){
            return getString(R.string.morning);
        }else if(timeOfDay <= 16){
            return getString(R.string.afternoon);
        }else if(timeOfDay <= 21){
            return getString(R.string.evening);
        }else {
            return getString(R.string.night);
        }
    }
}