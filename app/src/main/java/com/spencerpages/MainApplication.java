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
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BuffaloNickels;
import com.spencerpages.collections.CollectionInfo;
import com.spencerpages.collections.EisenhowerDollar;
import com.spencerpages.collections.FirstSpouseGoldCoins;
import com.spencerpages.collections.FranklinHalfDollars;
import com.spencerpages.collections.IndianHeadCents;
import com.spencerpages.collections.JeffersonNickels;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LibertyHeadNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.MercuryDimes;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NationalParkQuarters;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.PresidentialDollars;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.WalkingLibertyHalfDollars;
import com.spencerpages.collections.WashingtonQuarters;

import java.util.HashMap;

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

    // Common attribution string
    // https://www.usmint.gov/consumer/indexf8be.html?action=circCoinPolicy
    public static final String DEFAULT_ATTRIBUTION = "United States coin images from the United States Mint";

    // List of all the supported collection types by the app.  New collections
    // should be added here
    public static final CollectionInfo[] COLLECTION_TYPES =
            {
                    new LincolnCents(),
                    new JeffersonNickels(),
                    new RooseveltDimes(),
                    new WashingtonQuarters(),
                    new StateQuarters(),
                    new NationalParkQuarters(),
                    new KennedyHalfDollars(),
                    new EisenhowerDollar(),
                    new SusanBAnthonyDollars(),
                    new NativeAmericanDollars(),
                    new PresidentialDollars(),
                    new IndianHeadCents(),
                    new LibertyHeadNickels(),
                    new BuffaloNickels(),
                    new BarberDimes(),
                    new MercuryDimes(),
                    new BarberQuarters(),
                    new StandingLibertyQuarters(),
                    new BarberHalfDollars(),
                    new WalkingLibertyHalfDollars(),
                    new FranklinHalfDollars(),
                    new MorganDollars(),
                    new PeaceDollars(),
                    new AmericanEagleSilverDollars(),
                    new FirstSpouseGoldCoins(),
            };

    public static final String DATABASE_NAME = "CoinCollection";

    /** DATABASE_VERSION Tracks the current database version, and is essential for periodic
     *                   database updating.  It should be raised anytime we need to insert new
     *                   coins into a user's collections (Ex: yearly coin addition, bug fixes).
     *
     *                   Version 2 - Used in Versions 1 and 1.1 of the app
     *                   Version 3 - Used in Version 1.2 and 1.3 of the app
     *                   Version 4 - Used in Version 1.4, 1.4.1, and 1.5 of the app
     *                   Version 5 - Used in Version 1.6 of the app
     *                   Version 6 - Used in Version 2.0, 2.0.1 of the app
     *                   Version 7 - Used in Version 2.1, 2.1.1 of the app
     *                   Version 8 - Used in Version 2.2.1 of the app
     *                   Version 9 - Used in Version 2.3 of the app
     *                   Version 10 - Used in Version 2.3.1 of the app
     */
    public static final int DATABASE_VERSION = 10;

    /**
     * Performs any database updates that are needed at an application level
     * (Ex: renaming a collection type, adding fields to existing databases
     * as required for new functionality.)  Any collection-specific changes
     * should be performed in that collections onCollectionDatabaseUpgrade
     * instead of here.
     *
     * @param db the SQLiteDatabase db object to use when making updates
     * @param oldVersion the previous database version
     * @param newVersion the new database version
     */
    public static void onDatabaseUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion){

        if (oldVersion <= 5) {

            // We need to add in columns to support the new advanced view
            db.execSQL("ALTER TABLE collection_info ADD COLUMN display INTEGER DEFAULT " + Integer.toString(SIMPLE_DISPLAY));

            // Get all of the created tables
            Cursor resultCursor = db.query("collection_info", new String[]{"name"}, null, null, null, null, "_id");
            if (resultCursor.moveToFirst()) {
                do {

                    String name = resultCursor.getString(resultCursor.getColumnIndex("name"));

                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN advGradeIndex INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN advQuantityIndex INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN advNotes TEXT DEFAULT \"\"");

                    // Move to the next collection
                } while (resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        if (oldVersion <= 7) {
            // Add another column for the display order
            // We have to do this in a try/catch block, because there is one case where the
            // table might already have the column - when importing a backup from a previous
            // version of the app.
            try {
                db.execSQL("ALTER TABLE collection_info ADD COLUMN displayOrder INTEGER");
            } catch (SQLException e) {
                Log.d(MainApplication.APP_NAME, "collection_info already has column displayOrder");
            }

            Cursor resultCursor = db.query("collection_info", new String[]{"name", "coinType"},
                    null, null, null, null, "_id");

            int i = 0;  // Used to set the display order

            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                    String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));

                    ContentValues values = new ContentValues();

                    // Since we added the displayOrder column, populate that.
                    // In the import case this may get done twice (in the case of going from
                    // an imported 7 DB to the latest version.
                    values.put("displayOrder", i);

                    db.update("collection_info", values, "name=? AND coinType=?", new String[]{name, coinType});
                    i++;

                    // Move to the next collection
                } while (resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        if (oldVersion <= 9) {

            ContentValues values = new ContentValues();

            // We changed the name that we use for Sacagawea gold coin collections a while back,
            // but since we now use this name to determine the backing CollectionInfo obj, we
            // need to change it in the database (we should have done this to begin with!)
            values.put("coinType", "Sacagawea/Native American Dollars");
            db.update("collection_info", values, "coinType=?", new String[]{"Sacagawea Dollars"});
            values.clear();

            // Remove the space from mint marks so that this field's value is less confusing

            // Get all of the created tables
            Cursor resultCursor = db.query("collection_info", new String[]{"name"}, null, null, null, null, "_id");
            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndex("name"));

                    values.put("coinMint", "P");
                    db.update("[" + name + "]", values, "coinMint=?", new String[]{" P"});
                    values.clear();

                    values.put("coinMint", "D");
                    db.update("[" + name + "]", values, "coinMint=?", new String[]{" D"});
                    values.clear();

                    values.put("coinMint", "S");
                    db.update("[" + name + "]", values, "coinMint=?", new String[]{" S"});
                    values.clear();

                    values.put("coinMint", "O");
                    db.update("[" + name + "]", values, "coinMint=?", new String[]{" O"});
                    values.clear();

                    values.put("coinMint", "CC");
                    db.update("[" + name + "]", values, "coinMint=?", new String[]{" CC"});
                    values.clear();

                } while (resultCursor.moveToNext());
            }
            resultCursor.close();

            //TODO Change buffalo nickels mint marks to remove space
            //TODO Change indian head cent mint marks to remove space
            //TODO Change walking liberty half dollar mint marks to remove space
        }
        return;
    }
}
