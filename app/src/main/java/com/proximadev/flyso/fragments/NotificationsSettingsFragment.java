package com.proximadev.flyso.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.proximadev.flyso.R;
import com.proximadev.flyso.notifications.Scheduler;
import com.proximadev.flyso.utils.BlackListH;
import com.proximadev.flyso.utils.BlacklistAdapter;
import com.proximadev.flyso.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationsSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private SharedPreferences preferences;
    private DatabaseHelper DBHelper;
    private BlackListH blh;
    private BlacklistAdapter adapter;
    private List<BlackListH> blacklist;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notifications_settings);
        if (getActivity() != null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            DBHelper = new DatabaseHelper(getActivity());
            cursor = DBHelper.getReadableDatabase().rawQuery("SELECT BL FROM flyso_table", null);
            blacklist = new ArrayList<>();
            while (cursor != null && cursor.moveToNext()) {
                if (cursor.getString(0) != null) {
                    blh = new BlackListH(cursor.getString(0));
                    blacklist.add(blh);
                }
            }

            findPreference("BlackList").setOnPreferenceClickListener(this);

            preferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
                switch (key) {
                    case "notif_interval":
                        reschedule(prefs);
                        break;
                    case "notif_exact":
                        reschedule(prefs);
                        break;
                    default:
                        break;
                }
            });
        }
    }

    private void reschedule(SharedPreferences prefs) {
        Scheduler mScheduler = new Scheduler(getActivity());
        mScheduler.cancel();
        mScheduler.schedule(Integer.parseInt(prefs.getString("notif_interval", "1800000")), true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!cursor.isClosed()) {
            DBHelper.close();
            cursor.close();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("BlackList".equals(preference.getKey())) {
            AlertDialog.Builder BlacklistDialog = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.blacklist, null);
            final EditText blword = view.findViewById(R.id.blword_new);
            BlacklistDialog.setView(view);
            BlacklistDialog.setTitle(R.string.blacklist_title);
            adapter = new BlacklistAdapter(getActivity(), blacklist, DBHelper);
            ListView BlackListView = view.findViewById(R.id.BlackListView);
            BlackListView.setAdapter(adapter);

            BlacklistDialog.setPositiveButton(android.R.string.ok, (dialog, id) -> {
                String word = blword.getText().toString();
                if (!word.equals("")) {
                    blh = new BlackListH(word);
                    DBHelper.addData(blh.getWord());
                    blacklist.add(blh);
                    adapter.notifyDataSetChanged();
                }
            });
            BlacklistDialog.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

            BlacklistDialog.setCancelable(false);
            BlacklistDialog.show();
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update notification ringtone preference summary
        String ringtoneString = preferences.getString("ringtone", "content://settings/system/notification_sound");
        Uri ringtoneUri = Uri.parse(ringtoneString);
        String name;

        try {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            name = ringtone.getTitle(getActivity());
        } catch (Exception ex) {
            ex.printStackTrace();
            name = "Default";
        }

        if ("".equals(ringtoneString))
            name = getString(R.string.silent);

        RingtonePreference rpn = (RingtonePreference) findPreference("ringtone");
        rpn.setSummary(getString(R.string.notification_sound_description) + name);

        // update message ringtone preference summary
        ringtoneString = preferences.getString("ringtone_msg", "content://settings/system/notification_sound");
        ringtoneUri = Uri.parse(ringtoneString);

        try {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            name = ringtone.getTitle(getActivity());
        } catch (Exception ex) {
            ex.printStackTrace();
            name = "Default";
        }

        if ("".equals(ringtoneString))
            name = getString(R.string.silent);

        RingtonePreference rpm = (RingtonePreference) findPreference("ringtone_msg");
        rpm.setSummary(getString(R.string.notification_sound_description) + name);
    }
}