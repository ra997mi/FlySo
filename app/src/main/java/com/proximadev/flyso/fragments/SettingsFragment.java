package com.proximadev.flyso.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import android.text.Html;

import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.CustomPinActivity;
import com.proximadev.flyso.notifications.Scheduler;
import com.proximadev.flyso.others.Helpers;
import com.proximadev.flyso.pins.managers.AppLock;
import com.proximadev.flyso.utils.Utility;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{

    private Scheduler mScheduler;
    private SharedPreferences mSharedPref;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the settings_preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preferences);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mScheduler = new Scheduler(getActivity());

        findPreference("notifications_settings").setOnPreferenceClickListener(this);
        findPreference("customize_settings").setOnPreferenceClickListener(this);
        findPreference("location").setOnPreferenceClickListener(this);
        findPreference("notif").setOnPreferenceClickListener(this);
        findPreference("licenses").setOnPreferenceClickListener(this);
        findPreference("sendfeedback").setOnPreferenceClickListener(this);
        findPreference("FlySoThemeColor").setOnPreferenceClickListener(this);

        mListener = (prefs, key) -> {
            if ("FlysoLocker".equals(key)) {
                final Intent intent = new Intent(getActivity(), CustomPinActivity.class);
                if (prefs.getBoolean("FlysoLocker", false)) {
                    Utility.showGuideDialog(getActivity(), prefs, getString(R.string.pin_code_forgot_text), getString(R.string.forget_pincode), null)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                                startActivity(intent);
                            }).show();
                }
            }
        };
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case "notifications_settings":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_content_frame,
                        new NotificationsSettingsFragment()).commit();
                return true;
            case "customize_settings":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_content_frame,
                        new CustomizeSettingsFragment()).commit();
                return true;
            case "location":
                getLocationPermission();
                return true;
            case "notif":
                setScheduler();
                return true;
            case "licenses":
                String text = getString(R.string.license_message);
                CharSequence styledText;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    styledText = Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
                else
                    styledText = Html.fromHtml(text);

                Utility.showGuideDialog(getActivity(),mSharedPref , getString(R.string.license_title),null , styledText)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
                return true;
            case "sendfeedback":
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"proximadevteam@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "FlySo");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.from)+"\n" + Helpers.getDeviceInfo(getActivity()) + "\n\n (Write your feedback here)");
                startActivity(Intent.createChooser(intent, getString(R.string.send)));
                return true;

            case "FlySoThemeColor":
                final ColorPicker colorPicker = new ColorPicker(getActivity());
                ArrayList<String>colors = new ArrayList<>();
                colors.add("#242424");//0 Dark
                colors.add("#000000");//1 Black
                colors.add("#3b5998");//2 Blue
                colors.add("#03A9F4");//3 LightBlue
                colors.add("#4CAF50");//4 Green
                colors.add("#109c56");//5 PlayGreen
                colors.add("#F44336");//6 Red
                colors.add("#FFEB3B");//7 Yellow
                colors.add("#CDDC39");//8 Lime
                colors.add("#9C27B0");//9 Purple
                colors.add("#E91E63");//10 Pink
                colors.add("#607D8B");//11 Grey
                colors.add("#FF5722");//12 Orange
                colorPicker.setColors(colors);
                colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position,int color) {
                        mSharedPref.edit().putInt("ThemeKey",position).apply();
                    }

                    @Override
                    public void onCancel(){
                        colorPicker.dismissDialog();
                    }
                }).setRoundColorButton(true).setColumns(5).show();
                return true;
        }
        return false;
    }

    private void getLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                RequestStoragePermission();
            }
        }
    }

    private void setScheduler() {
        if (mSharedPref.getBoolean("notif", false) && !mSharedPref.getBoolean("save_data", false))
            mScheduler.schedule(Integer.parseInt(mSharedPref.getString("notif_interval", "1800000")), true);
        else
            mScheduler.cancel();
    }

    private void RequestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSharedPref.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSharedPref.unregisterOnSharedPreferenceChangeListener(mListener);
    }
}
