package com.beauty.comp_eng.mynote;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditNoteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1002;
    private String action;
    private EditText noteEditor;
    private EditText titleEditor;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteEditor = (EditText) findViewById(R.id.editText);
        noteEditor.requestFocus();
        titleEditor = (EditText) findViewById(R.id.editTitle);
        Intent intent = getIntent();
        // Allows you to pass complex object as an intent extra
        Uri uri = intent.getParcelableExtra(NoteProvider.CONTENT_ITEM_TYPE);

        // null when uri isn't passed in, else, request to edit note

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();     // where clause
            // cursor gives you access to one record that matched requested primary key value
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY));  // gets text of selected note
            noteEditor.setText(oldText);    // sets the text in EditNoteActivity activity to the old text for editing
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
            titleEditor.setText(oldTitle);
            noteEditor.requestFocus();      // sends cursor to end of text
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            // When toolbar back button is pressed
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
            case R.id.action_attach_image:
                attachImage();
                break;
        }

        return true;    // true means you always handled the menu selection
    }

    private void attachImage() {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
//        Intent intent = new Intent();
//        // Show only images, no videos or anything else
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_PICK);
//        // Always show the chooser (if there are multiple options available)
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    } // end attachImage method

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // end onActivityResult method

    private void finishEditing() {
        String newText = noteEditor.getText().toString().trim();
        String newTitle = titleEditor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    /* Sends message to MainActivity saying that operation that was requested is
                    cancelled if the length of the new note is 0.
                     */
                    setResult(RESULT_CANCELED);
                } else {
                    if (newText.length() == 0) {
                        insertNote("", newTitle);
                    } else if (newTitle.length() == 0) {
                        insertNote(newText, "");
                    } else {
                        insertNote(newText, newTitle);
                    }
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    deleteNote();
                } else if (oldText.equals(newText) && oldTitle.equals(newTitle)) {
                    setResult(RESULT_CANCELED);     // if there are no changes, send back the RESULT_CANCELLED value
                } else {
                    if (newText.length() == 0) {
                        updateNote("", newTitle);
                    } else if (newTitle.length() == 0) {
                        updateNote(newText, "");
                    } else {
                        updateNote(newText, newTitle);
                    }
                }
        }
        finish();   // finished with activity, then go to parent activity
    }

    private void deleteNote() {
        getContentResolver().delete(NoteProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void updateNote(String noteText, String titleText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_BODY, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, titleText);
        // reusing noteFilter value to make sure you're only updating 1 selected row
        getContentResolver().update(NoteProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);   // send message to MainActivity saying something has changed, update content in list
    }

    // Code from MainActivity insertNote method
    private void insertNote(String noteText, String titleText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_BODY, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, titleText);
        getContentResolver().insert(NoteProvider.CONTENT_URI, values);  // insert method returns Uri but we can ignore it
        setResult(RESULT_OK);   // means the operation that was requested is completed
    }

    // When Android back button is pressed (toolbar back button in onOptionsItemSelected method
    @Override
    public void onBackPressed() {
        finishEditing();
    }

//    public void sendNote(View view) {
//        Intent i = new Intent(Intent.ACTION_SEND);
//        i.setType("message/rfc822");
//        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"m_tanel@live.ca"});
//        i.putExtra(Intent.EXTRA_SUBJECT, titleEditor.getText().toString());
//        i.putExtra(Intent.EXTRA_TEXT   , noteEditor.getText().toString());
//        try {
//            startActivity(Intent.createChooser(i, "Send mail..."));
//        } catch (android.content.ActivityNotFoundException ex) {
//            Toast.makeText(EditNoteActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//        }
//
//
//    }

    public void sendNote(View view) {
        Resources resources = getResources();

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_TEXT, titleEditor.getText().toString());
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, noteEditor.getText().toString());
        emailIntent.setType("message/rfc822");

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, titleEditor.getText().toString() + ": " + noteEditor.getText().toString());

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if(packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            } else if(packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("android.gm")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if(packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, titleEditor.getText().toString() + ": " + noteEditor.getText().toString());
                } else if(packageName.contains("facebook")) {
                    // Warning: Facebook IGNORES our text. They say "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
                    // One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share. We can also make a custom landing page, and the link
                    // will show the <meta content ="..."> text from that page with our link in Facebook.
                    intent.putExtra(Intent.EXTRA_TEXT, titleEditor.getText().toString() + ": " + noteEditor.getText().toString());
                } else if(packageName.contains("mms")) {
                    intent.putExtra(Intent.EXTRA_TEXT, titleEditor.getText().toString() + ": " + noteEditor.getText().toString());
                } else if(packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    intent.putExtra(Intent.EXTRA_TEXT, noteEditor.getText().toString());
                    intent.putExtra(Intent.EXTRA_SUBJECT, titleEditor.getText().toString());
                    intent.setType("message/rfc822");
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
    }

} // end EditNoteActivity class