package com.beauty.comp_eng.mynote;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

        // cursorAdapter exposes data in cursor to list view
        cursorAdapter = new NoteCursorAdapter(this, null, 0);

        // list view to display all notes in database
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
        // video had getLoaderManager..., but the initLoader() had a different third arg
        // which gave an erro when passing "this", so had to use support library
        getSupportLoaderManager().initLoader(0, null, this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // Changed the 3rd parameter to position and 4th parameter to id
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                Uri uri = Uri.parse(NoteProvider.CONTENT_URI + "/" + id);   // returns primary key value
                intent.putExtra(NoteProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, final long id) {
                final AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage("Delete?");
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Uri uri = Uri.parse(NoteProvider.CONTENT_URI + "/" + id);   // returns primary key value
                        getContentResolver().delete(uri, DBOpenHelper.NOTE_ID + "=" + id, null);
                        restartLoader();
                    }
                });
                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                b.show();

                return true;
            }
        });
    }

    private void insertNote(String noteBody) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_BODY, noteBody);
        Uri noteUri = getContentResolver().insert(NoteProvider.CONTENT_URI, values);
        //Log.d("MainActivity", "Inserted note " + noteUri.getLastPathSegment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                return true;
            case R.id.action_delete_all:
                deleteAllNotes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            getContentResolver().delete(
                                    NoteProvider.CONTENT_URI, null, null);
                            restartLoader();
                            Toast.makeText(MainActivity.this, getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    private void insertSampleData() {
        insertNote("Simple note");
        insertNote("Multiline\n note");
        insertNote("Very long text with a lot of text that exceeds the length of the screen");
        restartLoader();
    }

    private void restartLoader() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    public void addNewNote(View view) {
        Intent openEditNote = new Intent(this, EditNoteActivity.class);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();    // reloads and refreshed data from database
        }
    }
}
