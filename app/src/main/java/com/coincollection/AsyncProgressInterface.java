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

/**
 * Interface used for AsyncProgressTask to communicate with activity
 */
public interface AsyncProgressInterface {
    /**
     * Method to perform on the async thread
     *
     * @return a string result to display, or "" if no result
     */
    String asyncProgressDoInBackground();

    /**
     * Method to perform on the UI thread ahead of the async task
     */
    void asyncProgressOnPreExecute();

    /**
     * Method to perform on the UI thread after of the async task
     * This method should check the
     *
     * @param resultStr a string result to display, or "" if no result
     */
    void asyncProgressOnPostExecute(String resultStr);
}
