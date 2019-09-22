package com.proximadev.flyso.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.proximadev.flyso.R;

public class CustomizeSettingsFragment extends PreferenceFragment{

    private SwitchPreference FacebookThemeSwitch;
    private SwitchPreference FacebookProfileDetails;
    private SwitchPreference FacebookSaveData;
    private SwitchPreference FacebookFab;
    private SwitchPreference FacebookDefaultPlayer;
    private SwitchPreference FacebookCenterText;
    private SwitchPreference FacebookCopyText;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    private SharedPreferences mSharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the customize_settings from an XML resource
        addPreferencesFromResource(R.xml.customize_settings);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        FacebookSaveData = (SwitchPreference) findPreference("save_data");
        FacebookThemeSwitch = (SwitchPreference) findPreference("facebookSwitch");
        FacebookProfileDetails = (SwitchPreference) findPreference("FacebookDetails");
        FacebookFab = (SwitchPreference) findPreference("facebookFab");
        FacebookDefaultPlayer = (SwitchPreference) findPreference("DefaultPlayer");
        FacebookCenterText = (SwitchPreference) findPreference("facebook_center_text");
        FacebookCopyText = (SwitchPreference) findPreference("facebook_copy_text");

        mListener = (prefs, key) -> {
            switch (key) {
                case "facebookSwitch":
                    if (prefs.getBoolean("facebookSwitch", false)) {
                        FacebookSaveData.setChecked(false);
                        FacebookSaveData.setEnabled(false);
                        FacebookProfileDetails.setEnabled(true);
                        FacebookFab.setEnabled(true);
                        FacebookDefaultPlayer.setEnabled(true);
                        FacebookCenterText.setEnabled(true);
                        FacebookCopyText.setEnabled(true);
                    } else {
                        FacebookSaveData.setEnabled(true);
                        FacebookProfileDetails.setChecked(false);
                        FacebookProfileDetails.setEnabled(false);
                        FacebookFab.setChecked(false);
                        FacebookFab.setEnabled(false);
                        FacebookDefaultPlayer.setChecked(false);
                        FacebookDefaultPlayer.setEnabled(false);
                        FacebookCenterText.setChecked(false);
                        FacebookCenterText.setEnabled(false);
                        FacebookCopyText.setChecked(false);
                        FacebookCopyText.setEnabled(false);
                    }
                    break;
                case "save_data":
                    if (prefs.getBoolean("save_data",false)) {
                        FacebookThemeSwitch.setChecked(false);
                        FacebookThemeSwitch.setEnabled(false);
                        FacebookProfileDetails.setChecked(false);
                        FacebookProfileDetails.setEnabled(false);
                        FacebookFab.setChecked(false);
                        FacebookFab.setEnabled(false);
                        FacebookDefaultPlayer.setChecked(false);
                        FacebookDefaultPlayer.setEnabled(false);
                        FacebookCenterText.setChecked(false);
                        FacebookCenterText.setEnabled(false);
                        FacebookCopyText.setChecked(false);
                        FacebookCopyText.setEnabled(false);
                    }
                    else {
                        FacebookThemeSwitch.setEnabled(true);
                        FacebookProfileDetails.setEnabled(true);
                        FacebookFab.setEnabled(true);
                        FacebookDefaultPlayer.setEnabled(true);
                        FacebookCenterText.setEnabled(true);
                        FacebookCopyText.setEnabled(true);
                    }
                    break;
                default:
                    break;
            }
        };
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