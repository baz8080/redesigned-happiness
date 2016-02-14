package com.mbcdev.contactcleaner;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler to fetch all the contact IDs
 *
 * Created by barry on 14/02/2016.
 */
class AllContactIdsHandler extends AsyncQueryHandler {

    private static final String LOG_TAG = "AllContactIdsQuery";

    private static final int QUERY_ALL_CONTACTS = 1;

    private ResultCallback<List<String>> callback;

    /**
     * Constructor which takes the given content resolver and passes it to super
     *
     * @param contentResolver A content resolver, see {@link Context#getContentResolver()}
     */
    public AllContactIdsHandler(ContentResolver contentResolver) {
        super(contentResolver);
    }

    /**
     * Gets all of the contact IDs on the system
     *
     * @param callback The callback called when the IDs have been retrieved.
     */
    public void getAllContactIds(ResultCallback<List<String>> callback) {

        this.callback = callback;

        startQuery(
                QUERY_ALL_CONTACTS, "", ContactsContract.Contacts.CONTENT_URI,
                new String[] { ContactsContract.Contacts._ID },
                null, null, null
        );
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

        switch (token) {
            case QUERY_ALL_CONTACTS: {
                if (cursor != null) {
                    Log.d(LOG_TAG, "Number of contacts queried: " + cursor.getCount());

                    if (callback != null) {

                        List<String> idList = new ArrayList<>(cursor.getCount());

                        while (cursor.moveToNext()) {
                            idList.add(cursor.getString(0));
                        }

                        Log.d(LOG_TAG, "Calling back with list of IDs");
                        callback.onResult(idList);
                    }

                }
                break;
            }
            default: {
                Log.d(LOG_TAG, "Unknown token: " + token);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }
}
