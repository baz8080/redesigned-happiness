package com.mbcdev.contactcleaner;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Gets all of the Notes with 'junk' content
 *
 * Created by barry on 14/02/2016.
 */
class AllNotesWithJunkHandler extends AsyncQueryHandler {

    private static final String LOG_TAG = "AllNotesWithJunkHandler";

    private final String[] regularExpressionsToRemove;
    private final List<String> contactIds;
    private List<Pair<String, String>> notesList;
    private AtomicInteger totalQueryCount;
    private ResultCallback<List<Pair<String, String>>> callback;

    /**
     * Constructor which takes the given content resolver and passes it to super
     *
     * @param contentResolver A content resolver, see {@link Context#getContentResolver()}
     * @param contactIds The contact IDs to search when removing junk notes
     * @param regularExpressionsToRemove The regular expressions to search for and remove in the notes
     */
    public AllNotesWithJunkHandler(ContentResolver contentResolver, List<String> contactIds,  String... regularExpressionsToRemove) {
        super(contentResolver);
        this.contactIds = contactIds;
        this.regularExpressionsToRemove = regularExpressionsToRemove;

        this.notesList = Collections.synchronizedList(new ArrayList<Pair<String, String>>());
    }

    public void getAllNotesWithJunk(ResultCallback<List<Pair<String, String>>> callback) {

        if (contactIds == null || contactIds.size() == 0) {
            Log.d(LOG_TAG, "There are no contacts to process");

            if (callback != null) {
                callback.onResult(new ArrayList<Pair<String, String>>());
            }

            return;
        }

        if (regularExpressionsToRemove == null || regularExpressionsToRemove.length == 0) {
            Log.d(LOG_TAG, "There are no regular expressions to process");

            if (callback != null) {
                callback.onResult(new ArrayList<Pair<String, String>>());
            }

            return;
        }

        this.callback = callback;
        this.totalQueryCount = new AtomicInteger(contactIds.size());

        for (String contactId : contactIds) {

            String[] projection = new String[] { ContactsContract.CommonDataKinds.Note.NOTE, ContactsContract.Data._ID };
            String selection = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " =?";
            String[] selectionArgs = new String[] { contactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };

            startQuery(-1, null, ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
        }

    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

        if (cursor != null && cursor.moveToNext()) {

            String originalNote = cursor.getString(0);
            String noteId = cursor.getString(1);

            if (originalNote != null && originalNote.length() > 0) {

                String cleanedNote = originalNote;

                for (String regex : regularExpressionsToRemove) {
                    cleanedNote = cleanedNote.replaceAll(regex, "");

                    if (!originalNote.equals(cleanedNote)) {
                        notesList.add(new Pair<>(noteId, cleanedNote));
                    }
                }
            }
        }

        int remaining = totalQueryCount.decrementAndGet();

        if (remaining == 0) {

            Log.d(LOG_TAG, "All queries complete.");

            if (callback != null) {
                Log.d(LOG_TAG, "Calling back with list of notes");
                callback.onResult(notesList);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }
}
