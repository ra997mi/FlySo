package com.proximadev.flyso.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.proximadev.flyso.R;

import java.util.Calendar;

public final class Utility {

    public static AlertDialog.Builder showGuideDialog(Context activity, SharedPreferences prefs, String title, String message, CharSequence sequence){
        int dialogTheme = R.style.Dialog;

        switch (prefs.getInt("ThemeKey", 0)) {
            case 0://Dark
            case 1://Black
                dialogTheme = R.style.DialogDark;
                break;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity , dialogTheme);
        dialog.setTitle(title);
        if (message != null)
            dialog.setMessage(message);
        else
            dialog.setMessage(sequence);
        dialog.setCancelable(false);
        return dialog;
    }

    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "x";
        }
    }

    public static int getCurrentYear(){
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String getVideoName(@NonNull String mUrl) {
        try {
            String img = "FlySo-VID-";
            String[] separated = mUrl.split("/");
            String myFileName = separated[separated.length - 1];

            if(myFileName.contains(".mp4"))
                myFileName = img + myFileName.substring(0, 8) + ".mp4";

            else if(myFileName.contains(".m3u8"))
                myFileName = img + myFileName.substring(0, 8) + ".m3u8";

            else
                myFileName = img + myFileName.substring(0, 8) + ".mp4";

            return myFileName;
        } catch (Exception ex){
            ex.printStackTrace();
            return "FlySo-Video.mp4";
        }
    }

    public static String getPictureName(@NonNull String mUrl) {
        try {
            String img = "FlySo-IMG-";
            String[] separated = mUrl.split("/");
            String myFileName = separated[separated.length - 1];

            if(myFileName.contains(".png"))
                myFileName = img + myFileName.substring(0, 8) + ".png";

            else if(myFileName.contains(".gif"))
                myFileName = img + myFileName.substring(0, 8) + ".gif";

            else if (myFileName.contains(".jpg"))
                myFileName = img + myFileName.substring(0, 8) + ".jpg";

            else
                myFileName = img + myFileName.substring(0, 8) + ".jpg";

            return myFileName;
        } catch (Exception ex){
            ex.printStackTrace();
            return "FlySo-Picture.jpg";
        }
    }
}




