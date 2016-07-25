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

package com.spencerpages;

import android.app.Application;

public class MainApplication extends Application {

    // App name string, used when printing log messages
	public static final String APP_NAME = "CoinCollection";

    // String used to indicate the SharedPreference store to use.
    // SharedPreferences are used to store information like whether the help
    // dialogs have been seen before.
	public static final String PREFS = "mainPreferences";

	// Global "enum" values
	public static final int SIMPLE_DISPLAY = 0;
	public static final int ADVANCED_DISPLAY = 1;

}
