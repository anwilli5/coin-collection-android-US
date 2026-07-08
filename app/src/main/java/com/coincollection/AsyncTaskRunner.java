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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class AsyncTaskRunner {
    private WeakReference<AsyncProgressInterface> mListenerRef;
    private final Executor mExecutor;
    private final Handler mMainHandler;
    private final static Semaphore sDoInBackgroundSemaphore = new Semaphore(1, true);

    // Default production executor, shared across runners
    private final static Executor sDefaultExecutor = Executors.newCachedThreadPool();

    // The most recent task ID handled by this runner. Kept per-instance (not
    // static) so a task started in one activity can't surface progress UI in an
    // unrelated activity. Only read/written on the main thread.
    private int mLatestTaskId = TASK_NONE;

    // Result of a task that completed while no listener was attached (e.g. during
    // a configuration change). Held here and delivered when the next listener
    // attaches, so no task result is ever dropped. Only accessed on the main thread.
    private boolean mHasPendingResult = false;
    private int mPendingResultTaskId = TASK_NONE;
    private String mPendingResultString = "";

    AsyncTaskRunner(AsyncProgressInterface listener) {
        this(listener, sDefaultExecutor, new Handler(Looper.getMainLooper()));
    }

    /**
     * Constructor allowing the executor and main-thread handler to be injected.
     * Used by tests to run the background work synchronously and drive the main
     * looper deterministically. Production code uses the single-argument
     * constructor with the shared thread pool.
     *
     * @param listener    the initial listener
     * @param executor    executor used to run the background work
     * @param mainHandler handler used to post work to the main thread
     */
    AsyncTaskRunner(AsyncProgressInterface listener, Executor executor, Handler mainHandler) {
        mExecutor = executor;
        mMainHandler = mainHandler;
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
            // A newly attached listener picks up any result that completed while
            // detached, or re-shows progress for a task that is still running.
            deliverPendingToListener();
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
     * @return the currently attached listener, or null if none is attached
     */
    private AsyncProgressInterface getListener() {
        return (mListenerRef != null) ? mListenerRef.get() : null;
    }

    /**
     * Execute the task with the given task ID.
     *
     * @param taskId an integer representing the task ID
     */
    protected void execute(int taskId) {
        mMainHandler.post(() -> {
            mLatestTaskId = taskId;
            // Capture the listener that started the task and hold it strongly for
            // the duration of the background work, so the task always runs to
            // completion even if the activity is torn down (e.g. rotation) before
            // it finishes. The result is delivered to whichever listener is
            // attached when the work completes (see onPostExecute).
            final AsyncProgressInterface listener = getListener();
            if (listener != null) {
                listener.asyncProgressOnPreExecute(taskId);
            }
            mExecutor.execute(() -> {
                final String resultString = doInBackground(taskId, listener);
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
     * Perform the background task using the supplied listener.
     * This method uses a semaphore to ensure that background tasks are atomic.
     *
     * @param taskId   an integer representing the task ID
     * @param listener the listener captured when the task started
     * @return a string result to display, or "" if no result
     */
    protected String doInBackground(int taskId, AsyncProgressInterface listener) {
        if (listener == null) {
            return "";
        }
        try {
            sDoInBackgroundSemaphore.acquire();
            return listener.asyncProgressDoInBackground(taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } finally {
            sDoInBackgroundSemaphore.release();
        }
    }

    /**
     * Deliver a completed task's result on the main thread. If no listener is
     * attached (mid configuration change), the result is stashed and delivered
     * when the next listener attaches.
     *
     * @param taskId       an integer representing the task ID
     * @param resultString a string result to display, or "" if no result
     */
    private void onPostExecute(int taskId, String resultString) {
        AsyncProgressInterface listener = getListener();
        if (listener != null) {
            listener.asyncProgressOnPostExecute(taskId, resultString);
        } else {
            mHasPendingResult = true;
            mPendingResultTaskId = taskId;
            mPendingResultString = resultString;
        }
    }

    /**
     * Called when a listener attaches. Delivers a pending result if one is
     * waiting, otherwise re-shows the progress UI if a task is still running.
     * Must be called on the main thread.
     */
    private void deliverPendingToListener() {
        AsyncProgressInterface listener = getListener();
        if (listener == null) {
            return;
        }
        if (mHasPendingResult) {
            mHasPendingResult = false;
            int taskId = mPendingResultTaskId;
            String resultString = mPendingResultString;
            mPendingResultTaskId = TASK_NONE;
            mPendingResultString = "";
            listener.asyncProgressOnPostExecute(taskId, resultString);
        } else if (mLatestTaskId != TASK_NONE) {
            // A task is still running - re-show its progress UI on the new activity
            listener.asyncProgressOnPreExecute(mLatestTaskId);
        }
    }
}
