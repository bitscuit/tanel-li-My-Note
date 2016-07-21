package com.beauty.comp_eng.mynote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;

// THIS IS TO TEST THE UPDATE PROJECT OPTIONS

public class MainActivity extends AppCompatActivity {

    private static final int EDITOR_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: remove this later
        insertNote("New note");

        // cursor represents the data in the database
        Cursor cursor = getContentResolver().query(NoteProvider.CONTENT_URI,
                DBOpenHelper.ALL_COLUMNS, null, null, null, null);

        String[] from = {DBOpenHelper.NOTE_BODY};
        // text view to store note body in
        int[] to = {android.R.id.text1};

        // cursorAdapter exposes data in cursor to list view
        CursorAdapter cursorAdapter = new android.widget.SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, cursor, from, to, 0);

        // list view to show all notes in database
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
    }

    private void insertNote(String noteBody) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_BODY, noteBody);
        Uri noteUri = getContentResolver().insert(NoteProvider.CONTENT_URI, values);
        Log.d("MainActivity", "Inserted note " + noteUri.getLastPathSegment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addNewNote(View view) {
        Intent openEditNote = new Intent(this, EditNote.class);
        startActivityForResult(openEditNote, EDITOR_REQUEST_CODE);
    }
}
