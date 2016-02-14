package com.mbcdev.contactcleaner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSIONS_WRITE_CONTACTS = 1;

    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.main_root);
        View cleanButton = findViewById(R.id.main_btn_clean);

        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_WRITE_CONTACTS);
                } else {
                    cleanContacts();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_WRITE_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(rootView, R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                    cleanContacts();
                } else {
                    Snackbar.make(rootView, R.string.permission_denied, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Kicks off a few queries and updates which will remove all of the HTC junk in your contact's
     * notes
     */
    private void cleanContacts() {

        Snackbar.make(rootView, R.string.processing_contacts, Snackbar.LENGTH_SHORT).show();

        AllContactIdsHandler handler = new AllContactIdsHandler(getContentResolver());

        handler.getAllContactIds(new ResultCallback<List<String>>() {
            @Override
            public void onResult(List<String> result) {
                AllNotesWithJunkHandler allNotesWithJunkHandler = new AllNotesWithJunkHandler(getContentResolver(), result, "<HTCData>.*?</HTCData>");
                allNotesWithJunkHandler.getAllNotesWithJunk(new ResultCallback<List<Pair<String, String>>>() {
                    @Override
                    public void onResult(List<Pair<String, String>> result) {
                        BatchNoteUpdater batchNoteUpdater = new BatchNoteUpdater(getContentResolver(), result);
                        batchNoteUpdater.updateNotes();
                        Snackbar.make(rootView, R.string.done, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
