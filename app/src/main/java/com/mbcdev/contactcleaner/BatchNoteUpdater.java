package com.mbcdev.contactcleaner;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Updates notes in a batch.
 *
 * Created by barry on 14/02/2016.
 */
class BatchNoteUpdater {

    private static final String LOG_TAG = "BatchNoteUpdater";

    private final ContentResolver contentResolver;
    private final List<Pair<String, String>> notes;

    /**
     *
     * @param contentResolver A content resolver, see {@link Context#getContentResolver()}
     * @param notes The notes to process. First is the ID, second is the note content
     */
    public BatchNoteUpdater(ContentResolver contentResolver, List<Pair<String, String>> notes) {
        this.contentResolver = contentResolver;
        this.notes = notes;
    }

    /**
     * Updates the notes in batch
     */
    public void updateNotes() {
        ArrayList<ContentProviderOperation> noteUpdateOperations = new ArrayList<>(notes.size());

        for (Pair<String, String> note : notes) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
            builder.withSelection(ContactsContract.Data._ID + "=?", new String[]{ note.first });
            builder.withValue(ContactsContract.CommonDataKinds.Note.NOTE, note.second);
            noteUpdateOperations.add(builder.build());
        }

        Log.d(LOG_TAG, "Updating " + noteUpdateOperations.size() + " notes.");

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, noteUpdateOperations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

}
