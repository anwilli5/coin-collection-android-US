/*
 * Coin Collection, an Android app that helps users track the coins that they've collected
 * Copyright (C) 2010-2016 Andrew Williams
 *
 * This file is part of Coin Collection.
 *
 * Coin Collection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coin Collection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coin Collection.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.coincollection;

import static com.coincollection.BaseActivity.TASK_NONE;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class AsyncTaskRunner {
    private WeakReference<AsyncProgressInterface> mListenerRef;
    private final static int NUM_DELAY_HALF_SECONDS = 10;
    private final static ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final static Semaphore sDoInBackgroundSemaphore = new Semaphore(1, true);

    private static int mLatestTaskId = TASK_NONE;

    AsyncTaskRunner(AsyncProgressInterface listener) {
        setListener(listener);
    }

    /**
     * Set the listener for this task.
     * 
     * @param listener an instance of AsyncProgressInterface to handle task events
     */
    protected void setListener(AsyncProgressInterface listener) {
        if (listener != null) {
            mListenerRef = new WeakReference<>(listener);
        } else {
            mListenerRef = null;
        }
    }

    /**
     * Clear the listener reference to avoid memory leaks.
     * This should be called when the task is no longer needed or when the activity is destroyed.
     */
    protected void clearListener() {
        if (mListenerRef != null) {
            mListenerRef.clear();
        }
    }

    /**
     * Get the latest task ID that this listener is handling.
     *
     * @return the latest task ID, or TASK_NONE if no task is currently being handled
     */
    protected int getLatestTaskId() {
        return mLatestTaskId;
    }

    /**
     * Execute the task with the given task ID.
     *
     * @param taskId an integer representing the task ID
     */
    protected void execute(int taskId) {
        mMainHandler.post(() -> {
            mLatestTaskId = taskId;
            // Execute pre-execute on main thread
            onPreExecute(taskId);
            mExecutorService.execute(() -> {
                // Execute background task in a separate thread
                String resultString = doInBackground(taskId);
                // Execute post-execute on main thread
                mMainHandler.post(() -> {
                    onPostExecute(taskId, resultString);
                    // Clear the latest task ID if it is still the latest
                    // Note: Both set and clear are done on the main thread
                    if (mLatestTaskId == taskId) {
                        mLatestTaskId = TASK_NONE;
                    }
                });
            });
        });
    }

    /**
     * Perform the background task.
     * This method uses a semaphore to ensure that background tasks are atomic
     * 
     * @param taskId an integer representing the task ID
     * @return a string result to display, or "" if no result
     */
    protected String doInBackground(int taskId) {
        try {
            sDoInBackgroundSemaphore.acquire();
            for (int i = 0; i < NUM_DELAY_HALF_SECONDS; i++) {
                if (mListenerRef != null && mListenerRef.get() != null) {
                    return mListenerRef.get().asyncProgressDoInBackground(taskId);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break; // Return empty string (no error) if interrupted
                }
            }
            return "";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } finally {
            sDoInBackgroundSemaphore.release();
        }
    }

    /**
     * Method to perform on the UI thread before the async task starts.
     * @param taskId an integer representing the task ID
     */
    protected void onPreExecute(int taskId) {
        for (int i = 0; i < NUM_DELAY_HALF_SECONDS; i++) {
            if (mListenerRef != null && mListenerRef.get() != null) {
                mListenerRef.get().asyncProgressOnPreExecute(taskId);
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Method to perform on the UI thread after the async task completes.
     * @param taskId an integer representing the task ID
     * @param resultString a string result to display, or "" if no result
     */
    protected void onPostExecute(int taskId, String resultString) {
        for (int i = 0; i < NUM_DELAY_HALF_SECONDS; i++) {
            if (mListenerRef != null && mListenerRef.get() != null) {
                mListenerRef.get().asyncProgressOnPostExecute(taskId, resultString);
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}