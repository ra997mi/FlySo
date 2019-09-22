package com.proximadev.flyso.activities;

import android.app.Application;
import com.google.android.gms.ads.MobileAds;

public class FlySoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, "ca-app-pub-3836303927954880~1359595585");
    }
}