/*
 * Taken From MaterialFBook - ZeeRooo Thanks
 */

package com.proximadev.flyso.utils;

import android.os.AsyncTask;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.MainActivity;
import com.proximadev.flyso.others.Helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.lang.ref.WeakReference;

public class UserInfo extends AsyncTask<Void, Void, String> {

    private final WeakReference<MainActivity> mActivityRef;
    private String name, cover;

    public UserInfo(MainActivity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    protected String doInBackground(Void[] params) {
        try {
            Element e = Jsoup.connect("https://www.facebook.com/me").cookie(("https://m.facebook.com"), CookieManager.getInstance().getCookie(("https://m.facebook.com"))).get().body();
            name = e.select("input[name=q]").attr("value");
            cover = Helpers.decodeImg(e.toString().split("<img class=\"coverPhotoImg photo img\" src=\"")[1].split("\"")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        catch (Error error){
            error.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        try {
           MainActivity activity = mActivityRef.get();
           if (activity != null && !activity.isFinishing()) {
               if (name != null) {
                   TextView mName = activity.findViewById(R.id.profile_name);
                   mName.setVisibility(View.VISIBLE);
                   mName.setText(name);
               }
               if (cover != null) {
                   ImageView mCover = activity.findViewById(R.id.cover);
                   TextView tv = activity.findViewById(R.id.greet);
                   tv.setVisibility(View.GONE);
                   mCover.setVisibility(View.VISIBLE);
                   Glide.with(activity)
                           .load(cover)
                           .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                           .into(mCover);
               }
               if (Helpers.getCookie("https://m.facebook.com/", "c_user") != null) {
                   ImageView profile = activity.findViewById(R.id.profile_picture);
                   TextView tv = activity.findViewById(R.id.greet);
                   tv.setVisibility(View.GONE);
                   profile.setVisibility(View.VISIBLE);
                   Glide.with(activity)
                           .load("https://graph.facebook.com/" + Helpers.getCookie("https://m.facebook.com/", "c_user") + "/picture?type=large")
                           .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop())
                           .into(profile);
               }
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}