package com.spencerpages.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import com.spencerpages.MainApplication;

public class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
        super(context, MainApplication.DATABASE_NAME, null, MainApplication.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This is called if the DB doesn't exist (A fresh installation)
        _createCollectionInfoTable(db);
    }

    public void _createCollectionInfoTable(SQLiteDatabase db) {

        // v2.2.1 - Until this version all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
        String makeCollectionInfoTable = "CREATE TABLE collection_info (_id integer primary key,"
                + " name text not null,"
                + " coinType text not null,"
                + " total integer,"
                + " display integer default " + Integer.toString(MainApplication.SIMPLE_DISPLAY) + ","
                + " displayOrder integer"
                + ");";

        db.execSQL(makeCollectionInfoTable);
    }

    // We could implement this now that our targeted API level is >= 11, but not necessary
    //@Override
    //public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
    //}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(MainApplication.APP_NAME, "Upgrading database from version " + oldVersion + " to "
                + newVersion);


        // First call the MainApplication's onDatabaseUpgrade to ensure that any changes necessary
        // for the app to work are done.

        MainApplication.onDatabaseUpgrade(db, oldVersion, newVersion);

        // Now get a list of the collections and call each one's onCollectionDatabaseUpgrade method

        Cursor resultCursor = db.query("collection_info", new String[]{"name", "coinType",
                "total"}, null, null, null, null, "_id");

        if (resultCursor.moveToFirst()) {

            do {
                String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
                int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));

                int originalTotal = total;

                for (CollectionInfo collectionInfo : MainApplication.COLLECTION_TYPES) {

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
                    values.put("total", total);
                    db.update("collection_info", values, "name=? AND coinType=?", new String[]{name, coinType});
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
        Cursor coinMintCursor = db.query(true, tableName, new String[]{"coinMint"}, null, null, null, null, "_id", null);

        // Now add the new coins
        for (int j = 0; j < values.size(); j++) {
            ContentValues initialValues = new ContentValues();
            initialValues.put("coinIdentifier", values.get(j));
            initialValues.put("inCollection", 0);
            // For each mint mark in the collection, add in a new Coin
            if (coinMintCursor.moveToFirst()) {
                do {
                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));

                    initialValues.remove("coinMint");
                    initialValues.put("coinMint", coinMint);
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
     * @param db
     * @param tableName
     * @param year
     * @return
     */
    public static int addFromYear(SQLiteDatabase db, String tableName, String year) {

        int total = 0;

        // First, we need to determine whether we should add the year to this album
        Cursor lastYearCursor = db.query(tableName, new String[]{"coinIdentifier"}, null, null, null, null, "_id DESC", "1");
        boolean shouldAddNextYear = false;

        if (lastYearCursor.moveToFirst()) {
            String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex("coinIdentifier"));
            if (lastYear.equals(String.valueOf(Integer.valueOf(year) - 1))) {
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
            Cursor coinMintCursor = db.query(true, tableName, new String[]{"coinMint"}, null, null, null, null, "_id", null);

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
                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));

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
                newYearPCoinValues.put("coinIdentifier", year);
                newYearPCoinValues.put("coinMint", "P");
                if (db.insert(tableName, null, newYearPCoinValues) != -1) {
                    total++;
                }
            } else {

                if (foundBlank) {
                    ContentValues newYearBlankCoinValues = new ContentValues();
                    newYearBlankCoinValues.put("coinIdentifier", year);
                    newYearBlankCoinValues.put("coinMint", "");
                    if (db.insert(tableName, null, newYearBlankCoinValues) != -1) {
                        total++;
                    }
                }
            }

            if (shouldAddD) {
                ContentValues newYearDCoinValues = new ContentValues();
                newYearDCoinValues.put("coinIdentifier", year);
                newYearDCoinValues.put("coinMint", "D");
                if (db.insert(tableName, null, newYearDCoinValues) != -1) {
                    total++;
                }
            }
        }
        return total;
    }
}
