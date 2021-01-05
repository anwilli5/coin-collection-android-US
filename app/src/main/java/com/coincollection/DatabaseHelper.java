package com.coincollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.coincollection.CollectionPage.SIMPLE_DISPLAY;
import static com.coincollection.DatabaseAdapter.COL_COIN_TYPE;
import static com.coincollection.DatabaseAdapter.COL_DISPLAY;
import static com.coincollection.DatabaseAdapter.COL_DISPLAY_ORDER;
import static com.coincollection.DatabaseAdapter.COL_NAME;
import static com.coincollection.DatabaseAdapter.COL_TOTAL;
import static com.coincollection.DatabaseAdapter.TBL_COLLECTION_INFO;
import static com.spencerpages.MainApplication.APP_NAME;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static com.spencerpages.MainApplication.DATABASE_VERSION;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This is called if the DB doesn't exist (A fresh installation)
        _createCollectionInfoTable(db);
    }

    void _createCollectionInfoTable(SQLiteDatabase db) {

        // v2.2.1 - Until this version all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
        String makeCollectionInfoTable = "CREATE TABLE " + TBL_COLLECTION_INFO + " (_id integer primary key,"
                + " " + COL_NAME + " text not null,"
                + " " + COL_COIN_TYPE + " text not null,"
                + " " + COL_TOTAL + " integer,"
                + " " + COL_DISPLAY + " integer default " + SIMPLE_DISPLAY + ","
                + " " + COL_DISPLAY_ORDER + " integer"
                + ");";

        db.execSQL(makeCollectionInfoTable);
    }

    // We could implement this now that our targeted API level is >= 11, but not necessary
    //@Override
    //public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
    //}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.upgradeDb(db, oldVersion, newVersion, false);
    }

    /**
     * Upgrades the database
     *
     * @param db the database to upgrade
     * @param oldVersion the database's current version
     * @param newVersion the version to upgrade to
     * @param fromImport if true, indicates that the upgrade is part of a collection import
     */
    public void upgradeDb(SQLiteDatabase db, int oldVersion, int newVersion, boolean fromImport) {

        if(BuildConfig.DEBUG) {
            Log.i(APP_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion);
        }

        // First call the MainApplication's onDatabaseUpgrade to ensure that any changes necessary
        // for the app to work are done.

        MainApplication.onDatabaseUpgrade(db, oldVersion, newVersion, fromImport);

        // Now get a list of the collections and call each one's onCollectionDatabaseUpgrade method

        Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME, COL_COIN_TYPE,
                COL_TOTAL}, null, null, null, null, "_id");

        if (resultCursor.moveToFirst()) {

            do {
                String name = resultCursor.getString(resultCursor.getColumnIndex(COL_NAME));
                String coinType = resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_TYPE));
                int total = resultCursor.getInt(resultCursor.getColumnIndex(COL_TOTAL));

                int originalTotal = total;

                for (CollectionInfo collectionInfo : COLLECTION_TYPES) {

                    if (collectionInfo.getCoinType().equals(coinType)) {

                        // We pass the tableName as "[" + name + "]" just to ensure that
                        // a table name can't corrupt the SQL request.

                        int newRowCount = collectionInfo.onCollectionDatabaseUpgrade(db,
                                "[" + name + "]", oldVersion, newVersion);

                        // Adjust the total number of coins in the collection based
                        // on how many coins were added/removed
                        total += newRowCount;
                        break;
                    }
                }

                // For each collection, if there were changes then update the total
                if (originalTotal != total) {
                    // Update the total
                    ContentValues values = new ContentValues();
                    values.put(COL_TOTAL, total);
                    db.update(TBL_COLLECTION_INFO, values, COL_NAME + "=? AND " + COL_COIN_TYPE + "=?", new String[]{name, coinType});
                }

            } while (resultCursor.moveToNext());
        }
        resultCursor.close();
    }

    /**
     * Attempts to guess the coin mints available in a given collection and then
     * adds a row per coinIdentifier specified in values (per mint)
     *
     * TODO The logic for guessing coin mints is prone to breaking... We should
     * really just store the mint information in the database and store that.
     *
     * @param tableName the collection name
     * @param values    the ArrayList containing coinIdentifier values to add as rows
     * @return number of rows added
     */

    public static int addFromArrayList(SQLiteDatabase db, String tableName, ArrayList<String> values) {

        int total = 0;
        // Get the distinct columns from the coinMint field
        Cursor coinMintCursor = db.query(true, tableName, new String[]{COL_COIN_MINT}, null, null, null, null, "_id", null);

        // Now add the new coins
        for (int j = 0; j < values.size(); j++) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_COIN_IDENTIFIER, values.get(j));
            initialValues.put(COL_IN_COLLECTION, 0);
            // For each mint mark in the collection, add in a new Coin
            if (coinMintCursor.moveToFirst()) {
                do {
                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex(COL_COIN_MINT));

                    initialValues.remove(COL_COIN_MINT);
                    initialValues.put(COL_COIN_MINT, coinMint);
                    if (db.insert(tableName, null, initialValues) != -1) {
                        total++;
                    }

                } while (coinMintCursor.moveToNext());
            }
        }

        // All done with the coin mint information
        coinMintCursor.close();

        return total;
    }

    /**
     * Attempts to guess whether coinIdentifiers should be added for the given
     * year.  If this is the case, this method tries to determine the coin
     * mints available in the given collection and then adds row(s) for the
     * given year.
     *
     * NOTE: The only mints that are added are the 'P' mark, 'D' mint mark,
     * and no mint mark ('')
     *
     * TODO The logic for guessing coin mints is prone to breaking... We should
     * really just store the mint information in the database and store that.
     *
     * * Consider moving to MainApplication?
     * @param db database
     * @param tableName database table name
     * @param year coin year
     * @return total number of coins added
     */
    public static int addFromYear(SQLiteDatabase db, String tableName, String year) {

        int total = 0;

        // First, we need to determine whether we should add the year to this album
        Cursor lastYearCursor = db.query(tableName, new String[]{COL_COIN_IDENTIFIER}, null, null, null, null, "_id DESC", "1");
        boolean shouldAddNextYear = false;

        if (lastYearCursor.moveToFirst()) {
            String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex(COL_COIN_IDENTIFIER));
            if (lastYear.equals(String.valueOf(Integer.parseInt(year) - 1))) {
                // If the collection included last year's coins, it's likely
                // they want this year's in there too
                //
                // Also, in earlier versions it was possible to make a collection end after 2012...
                // If they already have year in their collection, don't add
                shouldAddNextYear = true;
            }
        }
        lastYearCursor.close();

        if (shouldAddNextYear) {
            // For each collection, we need to know which mint marks are present
            // Get the distinct columns from the coinMint field
            Cursor coinMintCursor = db.query(true, tableName, new String[]{COL_COIN_MINT}, null, null, null, null, "_id", null);

            // We need to determine which mint marks to add.  The way we do this is
            // a bit complicated, but is as follows:
            // - If we see P's, we should add another P
            // - If we see D's, we should add another D
            // - If we see a blank mint mark, there are three cases that could be
            //   be occurring:
            //       - The collection has no mint marks displayed
            //       - It's a collection type where coins from the P mint don't
            //         have a mint mark (Ex: Lincoln Cents)
            //       - It's a collection type where coins from the P mint mark
            //         used to not have a mint mark but now do.
            //
            //   We only want to include a blank mark in the first two cases
            //   above, so, if we see a blank mint mark and not a P mint mark.
            //
            //   A final note - we want to add the P mint mark before the D mint
            //   mark (our convention.)

            boolean shouldAddP = false;

            boolean shouldAddD = false;

            boolean foundBlank = false;

            if (coinMintCursor.moveToFirst()) {
                do {
                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex(COL_COIN_MINT));

                    switch (coinMint) {
                        case "":
                            foundBlank = true;
                            break;
                        case "P":
                            shouldAddP = true;
                            break;
                        case "D":
                            shouldAddD = true;
                            break;
                        default:
                            // Don't want to add in any " S"s
                            break;
                    }
                } while (coinMintCursor.moveToNext());
            }

            // All done with the coin mint information
            coinMintCursor.close();

            if (shouldAddP) {
                ContentValues newYearPCoinValues = new ContentValues();
                newYearPCoinValues.put(COL_COIN_IDENTIFIER, year);
                newYearPCoinValues.put(COL_COIN_MINT, "P");
                if (db.insert(tableName, null, newYearPCoinValues) != -1) {
                    total++;
                }
            } else {

                if (foundBlank) {
                    ContentValues newYearBlankCoinValues = new ContentValues();
                    newYearBlankCoinValues.put(COL_COIN_IDENTIFIER, year);
                    newYearBlankCoinValues.put(COL_COIN_MINT, "");
                    if (db.insert(tableName, null, newYearBlankCoinValues) != -1) {
                        total++;
                    }
                }
            }

            if (shouldAddD) {
                ContentValues newYearDCoinValues = new ContentValues();
                newYearDCoinValues.put(COL_COIN_IDENTIFIER, year);
                newYearDCoinValues.put(COL_COIN_MINT, "D");
                if (db.insert(tableName, null, newYearDCoinValues) != -1) {
                    total++;
                }
            }
        }
        return total;
    }
}
