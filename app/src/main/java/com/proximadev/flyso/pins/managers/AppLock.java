package com.proximadev.flyso.pins.managers;

import android.app.Activity;

import java.util.HashSet;

public abstract class AppLock {

    public static final int ENABLE_PINLOCK = 0;

    public static final int DISABLE_PINLOCK = 1;

    public static final int CHANGE_PIN = 2;

    public static final int CONFIRM_PIN = 3;

    public static final int UNLOCK_PIN = 4;

    public static final int LOGO_ID_NONE = -1;

    public static final String EXTRA_TYPE = "type";

    public static final long DEFAULT_TIMEOUT = 1000 * 10; // 10sec

    protected HashSet<String> mIgnoredActivities;

    public AppLock() {
        mIgnoredActivities = new HashSet<String>();
    }

    public void addIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.mIgnoredActivities.add(clazzName);
    }

    public void removeIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.mIgnoredActivities.remove(clazzName);
    }

    public abstract long getTimeout();

    public abstract void setTimeout(long timeout);

    public abstract int getLogoId();

    public abstract void setLogoId(int logoId);

    public abstract boolean shouldShowForgot(int appLockType);

    public abstract void setShouldShowForgot(boolean showForgot);

    public abstract boolean pinChallengeCancelled();

    public abstract void setPinChallengeCancelled(boolean cancelled);

    public abstract boolean onlyBackgroundTimeout();

    public abstract void setOnlyBackgroundTimeout(boolean onlyBackgroundTimeout);

    public abstract void enable();

    public abstract void disable();

    public abstract void disableAndRemoveConfiguration();

    public abstract long getLastActiveMillis();

    public abstract void setLastActiveMillis();

    public abstract boolean setPasscode(String passcode);

    public abstract boolean isFingerprintAuthEnabled();

    public abstract void setFingerprintAuthEnabled(boolean enabled);

    public abstract boolean checkPasscode(String passcode);

    public abstract boolean isPasscodeSet();

    public abstract boolean isIgnoredActivity(Activity activity);

    public abstract boolean shouldLockSceen(Activity activity);
}
