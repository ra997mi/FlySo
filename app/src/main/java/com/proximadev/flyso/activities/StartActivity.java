package com.proximadev.flyso.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.proximadev.flyso.R;
import com.proximadev.flyso.services.Connectivity;
import com.proximadev.flyso.utils.Utility;

public class StartActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("RotationLock",true))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus();
        }
        setContentView(R.layout.activity_start);
        setStartElements();
        showScreen();
    }

    private void showScreen() {
        new Handler().postDelayed(() -> {
            if (!Connectivity.isConnected(StartActivity.this)){
                Intent intent = new Intent(StartActivity.this, ConnectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            else {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, (long) 800);
    }

    private void setStartElements() {
        TextView version = findViewById(R.id.versionName);
        version.setText(getString(R.string.version, Utility.getAppVersion(this)));
        TextView copyright = findViewById(R.id.copyrightText);
        copyright.setText(getString(R.string.proximadev ,Utility.getCurrentYear()));
    }

    @TargetApi(19)
    private void setTranslucentStatus() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }
}
