package com.mbcdev.contactcleaner;

/**
 * Callback to wrap responses from {@link android.content.AsyncQueryHandler}
 *
 * Created by barry on 14/02/2016.
 */
interface ResultCallback<T> {

    /**
     * Called when a result is available
     *
     * @param result The result
     */
    void onResult(T result);
}
