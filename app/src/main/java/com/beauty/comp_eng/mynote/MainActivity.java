package com.beauty.comp_eng.mynote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;

// THIS IS TO TEST THE UPDATE PROJECT OPTIONS

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDITOR_REQUEST_CODE = 1001;

    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: remove this later
        insertNote("New note");

        String[] from = {DBOpenHelper.NOTE_BODY};
        // text view to store note body in
        int[] to = {android.R.id.text1};

        // cursorAdapter exposes data in cursor to list view
        cursorAdapter = new android.widget.SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null, from, to, 0);

        // list view to display all notes in database
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
        // video had getLoaderManager..., but the initLoader() had a different third arg
        // which gave an erro when passing "this", so had to use support library
        getSupportLoaderManager().initLoader(0, null, this);
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

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NoteProvider.CONTENT_URI, null, null, null, null);
    }

    // check api for description. auto-generated java doc had and error
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
