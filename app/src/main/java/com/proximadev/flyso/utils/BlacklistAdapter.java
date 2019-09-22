/*
 * Taken From MaterialFBook - ZeeRooo Thanks
 */

package com.proximadev.flyso.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.proximadev.flyso.R;
import java.util.List;
import es.dmoral.toasty.Toasty;

public class BlacklistAdapter extends ArrayAdapter<BlackListH> {
    private final List<BlackListH> BlackLH;
    private final DatabaseHelper DBHelper;

    private static class ViewHolder {
        TextView title;
        ImageButton delete;
    }

    public BlacklistAdapter(Context context, List<BlackListH> BlackLH, DatabaseHelper db) {
        super(context, R.layout.blacklist_listview, BlackLH);
        this.DBHelper = db;
        this.BlackLH = BlackLH;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder viewHolder;
        final BlackListH h = getItem(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.blacklist_listview, parent, false);
            viewHolder.title = convertView.findViewById(R.id.blacklist_word);
            viewHolder.delete = convertView.findViewById(R.id.delete_word);

            viewHolder.delete.setColorFilter(viewHolder.title.getCurrentTextColor());

            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.title.setText(h.getWord());

        viewHolder.delete.setOnClickListener(v -> {
            DBHelper.remove(h.getWord());
            BlackLH.remove(position);
            notifyDataSetChanged();
            Toasty.success(getContext(), getContext().getString(R.string.remove_bookmark) + " " + h.getWord(), 0).show();
        });
        return convertView;
    }
}