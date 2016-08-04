package com.beauty.comp_eng.mynote;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

// this class is the content provider
// creates a standardized mechanism to get the app's data
public class NoteProvider extends ContentProvider {

    // AUTHORITY is a globally unique string that identifies the content provider
    // to the Android framework.
    private static final String AUTHORITY = "com.beauty.comp_eng.mynote.noteprovider";

    // represents the entire data set. Only one table, so "notes"
    private static final String BASE_PATH = "notes";

    // uniform resource identifier to identify the content provider
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // constants to identify requested operation
    private static final int NOTES = 1;             // give data
    private static final int NOTES_ID = 2;          // give a single row in database

    // Uri matcher to parse a URI and then determine which operation has been requested
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // figure out purpose of this
    public static final String CONTENT_ITEM_TYPE = "note";

    // static initializer for UriMatcher
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
        // "#" is a wildcard
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTES_ID);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        db = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    // strings is projection. s is selection to filter data, strings1 is selection args, s1 is sort order
    // changed s to selection
    public Cursor query(Uri uri, String[] strings, String selection, String[] strings1, String s1) {

        // Tells you if the uri matches the NOTES_ID uri, you only want a single row from the database
        if (uriMatcher.match(uri) == NOTES_ID) {
            selection = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();
        }

        return db.query(DBOpenHelper.TABLE_NOTES, DBOpenHelper.ALL_COLUMNS,
                selection, null, null, null, DBOpenHelper.NOTE_CREATED + " DESC");
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id = db.insert(DBOpenHelper.TABLE_NOTES,
                null, contentValues);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return db.delete(DBOpenHelper.TABLE_NOTES, s, strings);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return db.update(DBOpenHelper.TABLE_NOTES, contentValues, s, strings);
    }
}
