package com.proximadev.flyso.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.proximadev.flyso.R;
import com.proximadev.flyso.utils.ThemeUtils;

public class ConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ThemeUtils.setTheme(this, prefs);

        if(prefs.getBoolean("RotationLock",true))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (prefs.getBoolean("FullScreen",false))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_connection);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close:
                finish();
                break;
            case R.id.settings:
                Intent Settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivity(Settings);
                finish();
                break;
        }
    }
}
