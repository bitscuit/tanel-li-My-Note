package com.beauty.comp_eng.mynote;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // get the body of the note
        String noteText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY));
        String noteTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
        TextView tv = (TextView) view.findViewById(R.id.tvNote);

        if (noteTitle.length() != 0 && noteText.length() != 0) {
            // get the position of the line feed
            int textPos = noteText.indexOf(10);
            int titlePos = noteTitle.indexOf(10);
            // if line feed found, truncate text and add ellipsis
            if (textPos != -1 && titlePos != -1) {
                noteText = noteText.substring(0, textPos) + " ...";
                noteTitle = noteTitle.substring(0, titlePos) + " ...";
            } else if (textPos != -1 && titlePos == -1) {
                noteText = noteText.substring(0, textPos) + " ...";
            } else if (textPos == -1 && titlePos != -1) {
                noteTitle = noteTitle.substring(0, titlePos) + " ...";
            }
            noteTitle = noteTitle + ": ";
//            final SpannableStringBuilder str = new SpannableStringBuilder(noteTitle);
//            final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
//            str.setSpan(bold, 0, noteTitle.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//            tv.setText(str + noteText);
            tv.setText(noteTitle + noteText);
        } else if (noteText.length() == 0 && noteTitle.length() != 0) {
            int titlePos = noteTitle.indexOf(10);
            // if line feed found, truncate text and add ellipsis
            if (titlePos != -1) {
                noteTitle = noteTitle.substring(0, titlePos) + " ...";
            }
            noteTitle = noteTitle + ": ";
            tv.setText(noteTitle);
        } else if (noteText.length() != 0 && noteTitle.length() == 0) {
            int textPos = noteText.indexOf(10);
            // if line feed found, truncate text and add ellipsis
            if (textPos != -1) {
                noteText = noteText.substring(0, textPos) + " ...";
            }
            tv.setText(noteText);
        }
    }
}
