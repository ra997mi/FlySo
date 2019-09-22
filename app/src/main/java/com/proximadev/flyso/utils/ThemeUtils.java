/*
 * Taken From MaterialFBook - ZeeRooo Thanks
 */

package com.proximadev.flyso.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;

import com.proximadev.flyso.R;

public final class ThemeUtils {

    public static int getColor(final Context context) {
        int attr = R.attr.colorPrimary;
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static void setTheme(Context context, SharedPreferences prefs) {
        switch (prefs.getInt("ThemeKey", 0)) {
            case 0:
                context.setTheme(R.style.Dark);
                break;
            case 1:
                context.setTheme(R.style.Black);
                break;
            case 2:
                context.setTheme(R.style.Blue);
                break;
            case 3:
                context.setTheme(R.style.LightBlue);
                break;
            case 4:
                context.setTheme(R.style.Green);
                break;
            case 5:
                context.setTheme(R.style.PlayStore);
                break;
            case 6:
                context.setTheme(R.style.Red);
                break;
            case 7:
                context.setTheme(R.style.Yellow);
                break;
            case 8:
                context.setTheme(R.style.Lime);
                break;
            case 9:
                context.setTheme(R.style.Purple);
                break;
            case 10:
                context.setTheme(R.style.Pink);
                break;
            case 11:
                context.setTheme(R.style.Grey);
                break;
            case 12:
                context.setTheme(R.style.Orange);
                break;
        }
    }
}
