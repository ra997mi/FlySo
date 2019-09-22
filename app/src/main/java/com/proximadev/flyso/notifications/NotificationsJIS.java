/*
 * Taken From MaterialFBook - ZeeRooo Thanks
 */

package com.proximadev.flyso.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.webkit.CookieManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.MainActivity;
import com.proximadev.flyso.others.Helpers;
import com.proximadev.flyso.utils.DatabaseHelper;
import com.proximadev.flyso.utils.ThemeUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class NotificationsJIS extends JobIntentService {
    private boolean msg_notAWhiteList = false, notif_notAWhiteList = false;
    private String pictureNotif, pictureMsg, e = "";
    private Bitmap picprofile;
    private String[] picMsg, picNotif;
    private Spanned emoji;
    private int mode = 0;
    private List<String> blist;
    private DatabaseHelper db;
    private Cursor cursor;
    private SharedPreferences prefs;
    private final static int FACEBOOKID = 122, MESSAGESID =133;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, NotificationsJIS.class, 2, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        db = new DatabaseHelper(this);
        Log.i("JobIntentService_FlySo", "Started");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        blist = new ArrayList<>();

        try {
            cursor = db.getReadableDatabase().rawQuery("SELECT BL FROM flyso_table", null);
            while (cursor != null && cursor.moveToNext()) {
                if (cursor.getString(0) != null)
                    blist.add(cursor.getString(0));
            }
            if (prefs.getBoolean("facebook_messages", false))
                SyncMessages();
            if (prefs.getBoolean("facebook_notifications", false))
                SyncNotifications();
        } catch (Exception e) {
            e.getStackTrace();
        }
        catch (Error error){
         error.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            Log.i("JobIntentService_FlySo", "Stopped");
            if (pictureMsg != null)
                pictureMsg = "";
            if (pictureNotif != null)
                pictureNotif = "";
            if (!cursor.isClosed()) {
                db.close();
                cursor.close();
            }
            if (msg_notAWhiteList)
                msg_notAWhiteList = false;
            if (notif_notAWhiteList)
                notif_notAWhiteList = false;
            super.onDestroy();
        } catch (Exception ignore){}
    }

    // Sync the notifications
    private void SyncNotifications() throws Exception {
        Log.i("JobIntentService_FlySo", "Trying: " + "https://m.facebook.com/notifications.php");
        Document doc = Jsoup.connect("https://m.facebook.com/notifications.php").cookie(("https://m.facebook.com"), CookieManager.getInstance().getCookie(("https://m.facebook.com"))).timeout(300000).get();
        Element notifications = doc.selectFirst("div.aclb > div.touchable-notification > a.touchable");

        final String time = notifications.select("span.mfss.fcg").text();

        final String content = notifications.select("div.c").text().replace(time, "");
        if (!blist.isEmpty())
            for (int listCount = 0; listCount < blist.size(); listCount++) {
                if (content.contains(blist.get(listCount)))
                    notif_notAWhiteList = true;
            }
        if (!notif_notAWhiteList) {
            final String text = content.replace(time, "");
            pictureNotif = notifications.select("i.img.l.profpic").attr("style");

            if (pictureNotif != null)
                picNotif = pictureNotif.split("('*')");

            if (!prefs.getString("last_notification_text", "").contains(text))
                notifier(content, getString(R.string.app_name), picNotif[1], FACEBOOKID);

            prefs.edit().putString("last_notification_text", text).apply();
        }
    }

    private void SyncMessages() throws Exception {
        Log.i("JobIntentService_FlySo", "Trying: " + "https://m.facebook.com/messages?soft=messages");
        Document doc = Jsoup.connect("https://m.facebook.com/messages?soft=messages").cookie(("https://m.facebook.com"), CookieManager.getInstance().getCookie(("https://m.facebook.com"))).timeout(300000).get();
        Element result = doc.getElementsByClass("item messages-flyout-item aclb abt").select("a.touchable.primary").first();
        if (result != null) {
            final String content = result.select("div.oneLine.preview.mfss.fcg").text();
            if (!blist.isEmpty())
                for (String s : blist) {
                    if (content.contains(s))
                        msg_notAWhiteList = true;
                }
            if (!msg_notAWhiteList) {
                final String text = result.text().replace(result.select("div.time.r.nowrap.mfss.fcl").text(), "");
                final String name = result.select("div.title.thread-title.mfsl.fcb").text();
                pictureMsg = result.select("i.img.profpic").attr("style");
                String CtoDisplay = content;

                if (pictureMsg != null)
                    picMsg = pictureMsg.split("('*')");

                Elements e_iemoji = result.select("._47e3._3kkw");
                if (!e_iemoji.isEmpty())
                    for (Element em : e_iemoji) {
                        String emojiUrl = em.attr("style");
                        String[] emoji_sp = emojiUrl.split("/");
                        String emoji_unicode = "0x" + emoji_sp[9].replace(".png)", "");
                        int i = Integer.parseInt(emoji_unicode.substring(2), 16);
                        String emoji_char = new String(Character.toChars(i));
                        e = e + emoji_char;
                        emoji = Html.fromHtml(e);
                        mode = 1;
                    }
                Elements e_emoji = result.select("._1ift._2560.img");
                if (!e_emoji.isEmpty())
                    for (Element em : e_emoji) {
                        String emojiUrl = em.attr("src");
                        String[] emoji_sp = emojiUrl.split("/");
                        String emoji_unicode = "0x" + emoji_sp[9].replace(".png", "");
                        int i = Integer.parseInt(emoji_unicode.substring(2), 16);
                        String emoji_char = new String(Character.toChars(i));
                        e = e + emoji_char;
                        emoji = Html.fromHtml(e);
                        mode = 2;
                    }

                if (mode != 0)
                    CtoDisplay += " " + emoji;

                if (!prefs.getString("last_message", "").equals(text))
                    notifier(CtoDisplay, name, picMsg[1], MESSAGESID);

                // save as shown (or ignored) to avoid showing it again
                prefs.edit().putString("last_message", text).apply();
            }
        }
    }

    // create a notification and display it
    private void notifier(@Nullable final String content, final String title, final String image_url, int id) {

        try {
            picprofile = Glide.with(this).asBitmap().load(Helpers.decodeImg(image_url)).apply(RequestOptions.circleCropTransform()).submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (Exception e) {
            e.getStackTrace();
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create all channels at once so users can see/configure them all with the first notification
            NotificationChannel messagesChannel = createNotificationChannel(mNotificationManager, "com.proximadev.flyso.notif.messages", getString(R.string.facebook_message), "vibrate_msg", "vibrate_double_msg", "led_msj");
            NotificationChannel facebookChannel = createNotificationChannel(mNotificationManager, "com.proximadev.flyso.notif.facebook", getString(R.string.facebook_notifications), "vibrate_notif", "vibrate_double_notif", "led_notif");

            if (id == FACEBOOKID)
                channelId = facebookChannel.getId();
            else
                channelId = messagesChannel.getId();
        }
        else {
            channelId = "com.proximadev.flyso.notif";
        }

        String ringtoneKey, vibrate_, vibrate_double_, led_;

        if (id == FACEBOOKID){
            ringtoneKey = "ringtone";
            vibrate_ = "vibrate_notif";
            vibrate_double_ = "vibrate_double_notif";
            led_ = "led_notif";
        }

        else {
            ringtoneKey = "ringtone_msg";
            vibrate_ = "vibrate_msg";
            vibrate_double_ = "vibrate_double_msg";
            led_ = "led_msj";
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setColor(ThemeUtils.getColor(this))
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_web)
                .setLargeIcon(picprofile)
                .setAutoCancel(true);

        if (prefs.getBoolean(vibrate_, false)) {
            mBuilder.setVibrate(new long[]{500, 500});
            if (prefs.getBoolean(vibrate_double_, false))
                mBuilder.setVibrate(new long[]{500, 500, 500, 500});
        }

        if (prefs.getBoolean(led_, false))
            mBuilder.setLights(Color.BLUE, 1000, 1000);

        // priority for Heads-up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder.setCategory(Notification.CATEGORY_MESSAGE);

        Uri ringtoneUri = Uri.parse(prefs.getString(ringtoneKey, "content://settings/system/notification_sound"));
        mBuilder.setSound(ringtoneUri);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setOngoing(false);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);

        if (mNotificationManager != null) {
            if (id == FACEBOOKID) {
                mBuilder.setSmallIcon(R.drawable.ic_facebook);
                mNotificationManager.notify(id, mBuilder.build());
            }
            else{
                mBuilder.setSmallIcon(R.drawable.ic_messages);
                mNotificationManager.notify(id, mBuilder.build());
            }
        }
    }

    @TargetApi(26)
    private NotificationChannel createNotificationChannel(NotificationManager notificationManager, String id, String name, String vibratePref, String doubleVibratePref, String ledPref) {
        NotificationChannel notificationChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.enableVibration(prefs.getBoolean(vibratePref, false));
        notificationChannel.enableLights(prefs.getBoolean(ledPref, false));
        if (prefs.getBoolean(vibratePref, false)) {
            notificationChannel.setVibrationPattern(new long[]{500, 500});
            if (prefs.getBoolean(doubleVibratePref, false))
                notificationChannel.setVibrationPattern(new long[]{500, 500, 500, 500});
        }
        if (prefs.getBoolean(ledPref, false))
            notificationChannel.setLightColor(Color.BLUE);
        notificationManager.createNotificationChannel(notificationChannel);

        return notificationChannel;
    }
}
