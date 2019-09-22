package com.proximadev.flyso.pins.interfaces;

import android.app.Activity;

public interface LifeCycleInterface {

    void onActivityResumed(Activity activity);

    void onActivityUserInteraction(Activity activity);

    void onActivityPaused(Activity activity);
}
