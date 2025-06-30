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

import android.os.AsyncTask;

/**
 * sub-class of AsyncTask
 * See: <a href="http://stackoverflow.com/questions/6450275/android-how-to-work-with-asynctasks-progressdialog">...</a>
 */
// TODO For passing the AsyncTask between Activity instances, see this post:
// http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
// Our method is subject to the race conditions described therein :O
class AsyncProgressTask extends AsyncTask<Void, Void, Void> {
    AsyncProgressInterface mListener;
    int mAsyncTaskId = 0;
    private final static int NUM_DELAY_HALF_SECONDS = 10;
    String mResultString;

    AsyncProgressTask(AsyncProgressInterface listener) {
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int i = 0; i < NUM_DELAY_HALF_SECONDS; i++) {
            if (mListener != null) {
                mResultString = mListener.asyncProgressDoInBackground();
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        for (int i = 0; i < NUM_DELAY_HALF_SECONDS; i++) {
            if (mListener != null) {
                mListener.asyncProgressOnPreExecute();
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        for (int i = 0; i < NUM_DELAY_HALF_SECONDS; i++) {
            if (mListener != null) {
                mListener.asyncProgressOnPostExecute(mResultString);
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