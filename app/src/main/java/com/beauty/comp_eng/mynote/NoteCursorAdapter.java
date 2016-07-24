package com.beauty.comp_eng.mynote;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class NoteCursorAdapter extends CursorAdapter {

    public NoteCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // makes new view to hold data pointed to by cursor
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    // binds existing view with data pointed to by cursor
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // get the body of the note
        String noteBody = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY));
        // get the position of the line feed
        int pos = noteBody.indexOf(10);
        // if line feed found, truncate text and add ellipsis
        if (pos != -1) {
            noteBody = noteBody.substring(0, pos) + " ...";
        }
        TextView tv = (TextView) view.findViewById(R.id.tvNote);
        tv.setText(noteBody);
    }
}
