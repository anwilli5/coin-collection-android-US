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
import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.CollectionPage;
import com.coincollection.DatabaseAdapter;
import com.coincollection.DatabaseHelper;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BuffaloNickels;
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

import static com.coincollection.CoinSlot.COL_ADV_GRADE_INDEX;
import static com.coincollection.CoinSlot.COL_ADV_NOTES;
import static com.coincollection.CoinSlot.COL_ADV_QUANTITY_INDEX;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CollectionListInfo.COL_COIN_TYPE;
import static com.coincollection.CollectionListInfo.COL_DISPLAY;
import static com.coincollection.CollectionListInfo.COL_DISPLAY_ORDER;
import static com.coincollection.CollectionListInfo.COL_END_YEAR;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_SHOW_CHECKBOXES;
import static com.coincollection.CollectionListInfo.COL_SHOW_MINT_MARKS;
import static com.coincollection.CollectionListInfo.COL_START_YEAR;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.coincollection.DatabaseHelper.runSqlUpdate;

public class MainApplication extends Application {

    // App name string, used when printing log messages
    public static final String APP_NAME = "CoinCollection";

    // String used to indicate the SharedPreference store to use.
    // SharedPreferences are used to store information like whether the help
    // dialogs have been seen before.
    public static final String PREFS = "mainPreferences";

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
                    new AmericanInnovationDollars(),
            };

    public static final String DATABASE_NAME = "CoinCollection";

    private final DatabaseAdapter mDbAdapter = new DatabaseAdapter(this);

    public DatabaseAdapter getDbAdapter() {
        return mDbAdapter;
    }

    /**
     *  DATABASE_VERSION Tracks the current database version, and is essential for periodic
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
     *                   Version 11 - Used in Version 2.3.2 of the app
     *                   Version 12 - Used in Version 2.3.3 of the app
     *                   Version 13 - Used in Version 2.3.4 of the app
     *                   Version 14 - Used in Version 2.3.5 of the app
     *                   Version 15 - Used in Version 3.0.0 of the app
     *                   Version 16 - Used in Version 3.1.0 of the app
     */
    public static final int DATABASE_VERSION = 16;

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
     * @param fromImport true if the upgrade is part of a database import
     */
    public static void onDatabaseUpgrade (
            SQLiteDatabase db,
            int oldVersion,
            int newVersion,
            boolean fromImport){

        // Skip if importing, since the database will be created with the latest structure
        if (oldVersion <= 5 && !fromImport) {

            // We need to add in columns to support the new advanced view
            db.execSQL("ALTER TABLE " + TBL_COLLECTION_INFO + " ADD COLUMN " + COL_DISPLAY + " INTEGER DEFAULT " + CollectionPage.SIMPLE_DISPLAY);

            // Get all of the created tables
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME}, null, null, null, null, "_id");
            if (resultCursor.moveToFirst()) {
                do {

                    String name = resultCursor.getString(resultCursor.getColumnIndex(COL_NAME));
                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN " + COL_ADV_GRADE_INDEX + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN " + COL_ADV_QUANTITY_INDEX + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN " + COL_ADV_NOTES + " TEXT DEFAULT \"\"");

                    // Move to the next collection
                } while (resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        if (oldVersion <= 7) {

            if (!fromImport) {
                // Add another column for the display order
                db.execSQL("ALTER TABLE " + TBL_COLLECTION_INFO + " ADD COLUMN " + COL_DISPLAY_ORDER + " INTEGER");
            }

            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME, COL_COIN_TYPE},
                    null, null, null, null, "_id");

            int i = 0;  // Used to set the display order

            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndex(COL_NAME));
                    String coinType = resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_TYPE));

                    ContentValues values = new ContentValues();

                    // Since we added the displayOrder column, populate that.
                    // In the import case this may get done twice (in the case of going from
                    // an imported 7 DB to the latest version.
                    values.put(COL_DISPLAY_ORDER, i);

                    runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_NAME + "=? AND " + COL_COIN_TYPE + "=?", new String[]{name, coinType});
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
            values.put(COL_COIN_TYPE, "Sacagawea/Native American Dollars");
            runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_COIN_TYPE + "=?", new String[]{"Sacagawea Dollars"});
            values.clear();

            // Remove the space from mint marks so that this field's value is less confusing

            // Get all of the created tables
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME}, null, null, null, null, "_id");
            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndex(COL_NAME));

                    values.put(COL_COIN_MINT, "P");
                    runSqlUpdate(db, name, values, COL_COIN_MINT + "=?", new String[]{" P"});
                    values.clear();

                    values.put(COL_COIN_MINT, "D");
                    runSqlUpdate(db, name, values, COL_COIN_MINT + "=?", new String[]{" D"});
                    values.clear();

                    values.put(COL_COIN_MINT, "S");
                    runSqlUpdate(db, name, values, COL_COIN_MINT + "=?", new String[]{" S"});
                    values.clear();

                    values.put(COL_COIN_MINT, "O");
                    runSqlUpdate(db, name, values, COL_COIN_MINT + "=?", new String[]{" O"});
                    values.clear();

                    values.put(COL_COIN_MINT, "CC");
                    runSqlUpdate(db, name, values, COL_COIN_MINT + "=?", new String[]{" CC"});
                    values.clear();

                } while (resultCursor.moveToNext());
            }
            resultCursor.close();

            //TODO Change buffalo nickels mint marks to remove space
            //TODO Change indian head cent mint marks to remove space
            //TODO Change walking liberty half dollar mint marks to remove space
        }

        if (oldVersion <= 14) {

            if (!fromImport) {
                // Add columns that keep track of the creation parameters (so these can be changed later)
                db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_START_YEAR + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_END_YEAR + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_SHOW_MINT_MARKS + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_SHOW_CHECKBOXES + " INTEGER DEFAULT 0");
            }

            // Determine the collection parameters for each existing collection
            for (CollectionListInfo collectionListInfo : DatabaseHelper.getLegacyCollectionParams(db)) {
                DatabaseHelper.updateExistingCollection(db, collectionListInfo.getName(), collectionListInfo, null);
            }
        }
    }

    /**
     * Get the collection index from collection type name
     * @param collectionTypeName collection name
     * @return int index or -1 if not found
     */
    public static int getIndexFromCollectionNameStr (String collectionTypeName) {
        for (int i = 0; i < COLLECTION_TYPES.length; i++) {
            if (COLLECTION_TYPES[i].getCoinType().equals(collectionTypeName)) {
                return i;
            }
        }
        return -1;
    }
}
