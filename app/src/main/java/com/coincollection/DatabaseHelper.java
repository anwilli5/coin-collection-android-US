package com.coincollection;

import static com.coincollection.CoinSlot.COL_ADV_GRADE_INDEX;
import static com.coincollection.CoinSlot.COL_ADV_NOTES;
import static com.coincollection.CoinSlot.COL_ADV_QUANTITY_INDEX;
import static com.coincollection.CoinSlot.COL_COIN_ID;
import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_CUSTOM_COIN;
import static com.coincollection.CoinSlot.COL_IMAGE_ID;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.coincollection.CoinSlot.COL_SORT_ORDER;
import static com.coincollection.CollectionListInfo.COL_COIN_TYPE;
import static com.coincollection.CollectionListInfo.COL_DISPLAY;
import static com.coincollection.CollectionListInfo.COL_DISPLAY_ORDER;
import static com.coincollection.CollectionListInfo.COL_END_YEAR;
import static com.coincollection.CollectionListInfo.COL_ID;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_SHOW_CHECKBOXES;
import static com.coincollection.CollectionListInfo.COL_SHOW_CHECKBOXES_LEGACY;
import static com.coincollection.CollectionListInfo.COL_SHOW_MINT_MARKS;
import static com.coincollection.CollectionListInfo.COL_SHOW_MINT_MARKS_LEGACY;
import static com.coincollection.CollectionListInfo.COL_START_YEAR;
import static com.coincollection.CollectionListInfo.COL_TOTAL;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.coincollection.CollectionPage.SIMPLE_DISPLAY;
import static com.spencerpages.MainApplication.APP_NAME;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static com.spencerpages.MainApplication.DATABASE_VERSION;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;

import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This is called if the DB doesn't exist (A fresh installation)
        createCollectionInfoTable(db);
    }

    /**
     * Creates the collection info database table
     *
     * @param db database to add to
     * @throws SQLException if an error occurs
     */
    public static void createCollectionInfoTable(SQLiteDatabase db) throws SQLException {

        // v2.2.1 - Until this version all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
        String makeCollectionInfoTable = "CREATE TABLE " + TBL_COLLECTION_INFO + " ("
                + " " + COL_ID + " integer primary key,"
                + " " + COL_NAME + " text not null,"
                + " " + COL_COIN_TYPE + " text not null,"
                + " " + COL_TOTAL + " integer,"
                + " " + COL_DISPLAY + " integer default " + SIMPLE_DISPLAY + ","
                + " " + COL_DISPLAY_ORDER + " integer,"
                + " " + COL_START_YEAR + " integer default 0,"
                + " " + COL_END_YEAR + " integer default 0,"
                + " " + COL_SHOW_MINT_MARKS + " text not null default '',"
                + " " + COL_SHOW_CHECKBOXES + " text not null default ''"
                + ");";

        db.execSQL(makeCollectionInfoTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DatabaseHelper.upgradeDb(db, oldVersion, newVersion, false);
    }

    /**
     * Performs any database updates that are needed at an application level
     * (Ex: renaming a collection type, adding fields to existing databases
     * as required for new functionality.)  Any collection-specific changes
     * should be performed in that collections onCollectionDatabaseUpgrade
     * instead of here.
     *
     * @param db         the SQLiteDatabase db object to use when making updates
     * @param oldVersion the previous database version
     * @param newVersion the new database version
     * @param fromImport true if the upgrade is part of a database import
     */
    private static void upgradeDbStructure(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion,
            boolean fromImport) {

        // Skip if importing, since the database will be created with the latest structure
        if (oldVersion <= 5 && !fromImport) {

            // We need to add in columns to support the new advanced view
            db.execSQL("ALTER TABLE " + TBL_COLLECTION_INFO + " ADD COLUMN " + COL_DISPLAY + " INTEGER DEFAULT " + CollectionPage.SIMPLE_DISPLAY);

            // Get all of the created tables
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME}, null, null, null, null, COL_COIN_ID);
            if (resultCursor.moveToFirst()) {
                do {

                    String name = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_NAME));
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
                    null, null, null, null, COL_COIN_ID);

            int i = 0;  // Used to set the display order

            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_NAME));
                    String coinType = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_COIN_TYPE));

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
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME}, null, null, null, null, COL_COIN_ID);
            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_NAME));

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
                db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_SHOW_MINT_MARKS_LEGACY + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_SHOW_CHECKBOXES_LEGACY + " INTEGER DEFAULT 0");
            }

            // Determine the collection parameters for each existing collection
            for (CollectionListInfo collectionListInfo : DatabaseHelper.getLegacyCollectionParams(db, !fromImport)) {
                // Imported collections will have the updated columns names
                DatabaseHelper.updateExistingCollection(db, collectionListInfo.getName(), collectionListInfo, null, !fromImport);
            }
        }

        // Add sort order to coins in each collection
        // - Skip if importing, since the database will be created with the latest structure
        if (oldVersion <= 16 && !fromImport) {

            // Get all of the created tables
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME}, null, null, null, null, COL_DISPLAY_ORDER);
            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_NAME));

                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN " + COL_SORT_ORDER + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN " + COL_CUSTOM_COIN + " INTEGER DEFAULT 0");

                    // Set the sort order to the IDs, as a starting point
                    db.execSQL("UPDATE [" + name + "] SET " + COL_SORT_ORDER + " = " + COL_COIN_ID);

                    // Move to the next collection
                } while (resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        // Add image id to coins in each collection
        // - Skip if importing, since the database will be created with the latest structure
        if (oldVersion <= 20 && !fromImport) {

            // Get all of the created tables
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME}, null, null, null, null, COL_DISPLAY_ORDER);
            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_NAME));

                    db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN " + COL_IMAGE_ID + " INTEGER DEFAULT -1");

                    // Move to the next collection
                } while (resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        // Add new mint mark/checkbox columns of string type to support any number of options
        // Note: Leaving the existing columns in existing databases since SQLite doesn't support
        // changing column types.
        if (oldVersion <= 21 && !fromImport) {
            db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_SHOW_MINT_MARKS + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE [" + TBL_COLLECTION_INFO + "] ADD COLUMN " + COL_SHOW_CHECKBOXES + " TEXT NOT NULL DEFAULT ''");

            // Set the new columns to the value of the old columns
            Cursor resultCursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME, COL_SHOW_MINT_MARKS_LEGACY, COL_SHOW_CHECKBOXES_LEGACY}, null, null, null, null, COL_DISPLAY_ORDER);
            if (resultCursor.moveToFirst()) {
                do {
                    String name = resultCursor.getString(resultCursor.getColumnIndexOrThrow(COL_NAME));
                    int mintMarks = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(COL_SHOW_MINT_MARKS_LEGACY));
                    int checkboxes = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(COL_SHOW_CHECKBOXES_LEGACY));
                    ContentValues values = new ContentValues();
                    values.put(COL_SHOW_MINT_MARKS, Integer.toString(mintMarks));
                    values.put(COL_SHOW_CHECKBOXES, Integer.toString(checkboxes));
                    runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_NAME + "=?", new String[]{name});
                } while (resultCursor.moveToNext());
            }
            resultCursor.close();
        }
    }

    /**
     * Get the next sort order for a new coin
     *
     * @param db        the database to access
     * @param tableName the collection name to access
     * @return The next display order to use
     * @throws SQLException if a database error occurred
     */
    public static int getNextCoinSortOrder(SQLiteDatabase db, String tableName) throws SQLException {
        String sqlCmd = "SELECT MAX(" + COL_SORT_ORDER + ") FROM [" + DatabaseAdapter.removeBrackets(tableName) + "]";
        SQLiteStatement compiledStatement = db.compileStatement(sqlCmd);
        int result = simpleQueryForLong(compiledStatement);
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result + 1;
    }

    /**
     * Upgrades the database
     *
     * @param db         the database to upgrade
     * @param oldVersion the database's current version
     * @param newVersion the version to upgrade to
     * @param fromImport if true, indicates that the upgrade is part of a collection import
     */
    public static void upgradeDb(SQLiteDatabase db, int oldVersion, int newVersion, boolean fromImport) {

        if (BuildConfig.DEBUG) {
            Log.i(APP_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion);
        }

        // First call the MainApplication's onDatabaseUpgrade to ensure that any changes necessary
        // for the app to work are done.
        upgradeDbStructure(db, oldVersion, newVersion, fromImport);

        // Now get a list of the collections and call each one's onCollectionDatabaseUpgrade method
        ArrayList<CollectionListInfo> collectionList = new ArrayList<>();
        getAllTables(db, collectionList, false);
        for (CollectionListInfo collectionListInfo : collectionList) {
            String tableName = collectionListInfo.getName();
            int numCoinsAdded = collectionListInfo.getCollectionObj().onCollectionDatabaseUpgrade(
                    db, collectionListInfo, oldVersion, newVersion);
            // Update the collection total if coins were added or removed
            if (numCoinsAdded != 0) {
                int newTotal = collectionListInfo.getMax() + numCoinsAdded;
                collectionListInfo.setMax(newTotal);
                ContentValues values = new ContentValues();
                values.put(COL_TOTAL, newTotal);
                runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_NAME + "=?", new String[]{tableName});
            }
        }
    }

    /**
     * Helper function to rename a collection
     *
     * @param db      database
     * @param oldName The original collection name
     * @param newName The new collection name
     * @throws SQLException if the database update was not successful
     */
    static void updateCollectionName(SQLiteDatabase db, String oldName, String newName) throws SQLException {
        String alterDbSqlStr = "ALTER TABLE [" + DatabaseAdapter.removeBrackets(oldName) + "] RENAME TO [" + DatabaseAdapter.removeBrackets(newName) + "]";
        db.execSQL(alterDbSqlStr);
        ContentValues args = new ContentValues();
        args.put(COL_NAME, newName);
        runSqlUpdate(db, TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[]{oldName});
    }

    /**
     * Adds new coins to the collection based on collection creation parameters
     *
     * @param collectionListInfo the collection info
     * @param values             the ArrayList containing coinIdentifier values to add
     * @return number of rows added
     */
    public static int addFromArrayList(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                       ArrayList<String> values) {
        int total = 0;
        String tableName = collectionListInfo.getName();
        int newSortOrder = getNextCoinSortOrder(db, tableName);
        for (int i = 0; i < values.size(); i++) {
            if (collectionListInfo.hasMintMarks()) {
                for (String flagStr : CollectionListInfo.MINT_STRING_TO_FLAGS.keySet()) {
                    Long mintFlag = CollectionListInfo.MINT_STRING_TO_FLAGS.get(flagStr);
                    if (mintFlag != null && ((collectionListInfo.getMintMarkFlagsAsLong() & mintFlag) != 0)) {
                        ContentValues insertValues = new ContentValues();
                        insertValues.put(COL_COIN_IDENTIFIER, values.get(i));
                        insertValues.put(COL_IN_COLLECTION, 0);
                        insertValues.put(COL_COIN_MINT, flagStr);
                        insertValues.put(COL_SORT_ORDER, newSortOrder++);
                        if (db.insert("[" + tableName + "]", null, insertValues) != -1) {
                            total++;
                        }
                    }
                }
            } else {
                ContentValues insertValues = new ContentValues();
                insertValues.put(COL_COIN_IDENTIFIER, values.get(i));
                insertValues.put(COL_IN_COLLECTION, 0);
                insertValues.put(COL_COIN_MINT, "");
                insertValues.put(COL_SORT_ORDER, newSortOrder++);
                if (db.insert("[" + tableName + "]", null, insertValues) != -1) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Add coins for the new year for the "P", "D", and "" mint marks
     *
     * @param db                 database
     * @param collectionListInfo the collection info
     * @param previousYear       previous year to look for to know if this coin should be added
     * @param year               coin year
     * @param identifier         identifier of the coin to add
     * @return total number of coins added
     */
    public static int addFromYear(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                  int previousYear, int year, String identifier) {
        ArrayList<String> mintList = new ArrayList<>(Arrays.asList("P", "D"));
        return addFromYear(db, collectionListInfo, previousYear, year, identifier, mintList);
    }

    /**
     * Add coins for the new year for the "P", "D", and "" mint marks
     *
     * @param db                 database
     * @param collectionListInfo the collection info
     * @param year               coin year
     * @return total number of coins added
     */
    public static int addFromYear(SQLiteDatabase db, CollectionListInfo collectionListInfo, int year) {
        ArrayList<String> mintList = new ArrayList<>(Arrays.asList("P", "D"));
        return addFromYear(db, collectionListInfo, year - 1, year, String.valueOf(year), mintList);
    }

    /**
     * Add coins for the new year, based on the collection parameters
     *
     * @param db                 database
     * @param collectionListInfo the collection info
     * @param previousYear       previous year to look for to know if this coin should be added
     * @param year               coin year
     * @param identifier         identifier of the coin to add
     * @param mintsToAdd         list of the mint marks to add
     * @return total number of coins added
     */
    public static int addFromYear(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                  int previousYear, int year, String identifier,
                                  ArrayList<String> mintsToAdd) {
        int total = 0;

        // Skip adding if the collection has an earlier end date
        if (previousYear != collectionListInfo.getEndYear()) {
            return total;
        }

        // Add the new coin entries
        String tableName = collectionListInfo.getName();
        int newSortOrder = getNextCoinSortOrder(db, tableName);
        if (collectionListInfo.hasMintMarks()) {
            for (String flagStr : CollectionListInfo.MINT_STRING_TO_FLAGS.keySet()) {
                Long mintFlag = CollectionListInfo.MINT_STRING_TO_FLAGS.get(flagStr);
                if (!mintsToAdd.contains(flagStr)) {
                    continue;
                }
                if (mintFlag != null && ((collectionListInfo.getMintMarkFlagsAsLong() & mintFlag) != 0)) {
                    ContentValues insertValues = new ContentValues();
                    insertValues.put(COL_COIN_IDENTIFIER, identifier);
                    insertValues.put(COL_IN_COLLECTION, 0);
                    insertValues.put(COL_COIN_MINT, flagStr);
                    insertValues.put(COL_SORT_ORDER, newSortOrder++);
                    if (db.insert("[" + tableName + "]", null, insertValues) != -1) {
                        total++;
                    }
                }
            }
        } else {
            ContentValues insertValues = new ContentValues();
            insertValues.put(COL_COIN_IDENTIFIER, identifier);
            insertValues.put(COL_IN_COLLECTION, 0);
            insertValues.put(COL_COIN_MINT, "");
            insertValues.put(COL_SORT_ORDER, newSortOrder++);
            if (db.insert("[" + tableName + "]", null, insertValues) != -1) {
                total++;
            }
        }

        // Update the collection's end year
        ContentValues updateValues = new ContentValues();
        updateValues.put(COL_END_YEAR, year);
        runSqlUpdate(db, TBL_COLLECTION_INFO, updateValues, COL_NAME + "=?", new String[]{tableName});
        collectionListInfo.setEndYear(year);

        return total;
    }

    /**
     * Get the basic coin information
     *
     * @param db              database
     * @param tableName       The name of the collection
     * @param populateAdvInfo If true, includes advanced attributes
     * @param useSortOrder    If true, includes sort order and uses it for sorting
     * @return CoinSlot list
     */
    static ArrayList<CoinSlot> getCoinList(SQLiteDatabase db, String tableName, boolean populateAdvInfo, boolean useSortOrder) {

        ArrayList<String> dbColumns = new ArrayList<>(
                Arrays.asList(COL_COIN_ID, COL_COIN_IDENTIFIER, COL_COIN_MINT, COL_IN_COLLECTION,
                        COL_SORT_ORDER, COL_CUSTOM_COIN, COL_IMAGE_ID));
        if (populateAdvInfo) {
            dbColumns.addAll(
                    Arrays.asList(COL_ADV_GRADE_INDEX, COL_ADV_QUANTITY_INDEX, COL_ADV_NOTES));
        }

        ArrayList<CoinSlot> coinList = new ArrayList<>();
        String sortColumn = useSortOrder ? COL_SORT_ORDER : COL_COIN_ID;
        Cursor cursor = db.query("[" + tableName + "]", dbColumns.toArray(new String[0]),
                null, null, null, null, sortColumn);
        if (cursor.moveToFirst()) {
            do {
                int sortOrder = useSortOrder ? cursor.getInt(cursor.getColumnIndexOrThrow(COL_SORT_ORDER))
                        : (int) cursor.getLong(cursor.getColumnIndexOrThrow(COL_COIN_ID));
                if (populateAdvInfo) {
                    coinList.add(new CoinSlot(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COL_COIN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_IDENTIFIER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_MINT)),
                            (cursor.getInt(cursor.getColumnIndexOrThrow(COL_IN_COLLECTION)) != 0),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ADV_GRADE_INDEX)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ADV_QUANTITY_INDEX)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_ADV_NOTES)),
                            sortOrder,
                            (cursor.getInt(cursor.getColumnIndexOrThrow(COL_CUSTOM_COIN)) != 0),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_IMAGE_ID))));
                } else {
                    coinList.add(new CoinSlot(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COL_COIN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_IDENTIFIER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_MINT)),
                            (cursor.getInt(cursor.getColumnIndexOrThrow(COL_IN_COLLECTION)) != 0),
                            sortOrder,
                            (cursor.getInt(cursor.getColumnIndexOrThrow(COL_CUSTOM_COIN)) != 0),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_IMAGE_ID))));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return coinList;
    }

    /**
     * Get the basic coin information used by the legacy code to determine collection params
     * This function should not be updated past DB version 16
     *
     * @param db        database
     * @param tableName The name of the collection
     * @return CoinSlot list
     */
    static ArrayList<CoinSlot> getCoinListForLegacyCollectionParams(SQLiteDatabase db, String tableName) {

        ArrayList<String> dbColumns = new ArrayList<>(
                Arrays.asList(COL_COIN_ID, COL_COIN_IDENTIFIER, COL_COIN_MINT, COL_IN_COLLECTION));

        ArrayList<CoinSlot> coinList = new ArrayList<>();
        Cursor cursor = db.query("[" + tableName + "]", dbColumns.toArray(new String[0]),
                null, null, null, null, COL_COIN_ID);
        if (cursor.moveToFirst()) {
            do {
                coinList.add(new CoinSlot(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_COIN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_IDENTIFIER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_MINT)),
                        (cursor.getInt(cursor.getColumnIndexOrThrow(COL_IN_COLLECTION)) == 1),
                        (int) cursor.getLong(cursor.getColumnIndexOrThrow(COL_COIN_ID)),
                        false,
                        -1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return coinList;
    }

    /**
     * Get the total number of coins in the collection
     *
     * @param db        database
     * @param tableName String that identifiers which table to query
     * @return int with the total number of coins in the collection
     * @throws SQLException if an error occurs
     */
    public static int fetchTotalCollected(SQLiteDatabase db, String tableName) throws SQLException {
        String sqlCmd = "SELECT COUNT(" + COL_COIN_ID + ") FROM [" + DatabaseAdapter.removeBrackets(tableName) + "] WHERE " + COL_IN_COLLECTION + "=1 LIMIT 1";
        SQLiteStatement compiledStatement = db.compileStatement(sqlCmd);
        int result = simpleQueryForLong(compiledStatement);
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Returns a list of all collections in the database
     *
     * @param db                    database
     * @param collectionListEntries List of CollectionListInfo to populate
     * @param legacyOptions         if true, uses the legacy mint marks / checkbox columns
     * @throws SQLException if a database error occurs
     */
    public static void getAllTables(SQLiteDatabase db, ArrayList<CollectionListInfo> collectionListEntries, boolean legacyOptions) throws SQLException {

        // Get rid of the other items in the list (if any)
        collectionListEntries.clear();
        String colShowMintMarks = legacyOptions ? COL_SHOW_MINT_MARKS_LEGACY : COL_SHOW_MINT_MARKS;
        String colShowCheckboxes = legacyOptions ? COL_SHOW_CHECKBOXES_LEGACY : COL_SHOW_CHECKBOXES;
        Cursor cursor = db.query(TBL_COLLECTION_INFO,
                new String[]{COL_NAME, COL_COIN_TYPE, COL_TOTAL, COL_DISPLAY, COL_START_YEAR,
                        COL_END_YEAR, colShowMintMarks, colShowCheckboxes},
                null, null, null, null, COL_DISPLAY_ORDER);
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String coinType = cursor.getString(cursor.getColumnIndexOrThrow(COL_COIN_TYPE));
                // Figure out what collection type maps to this
                int index = MainApplication.getIndexFromCollectionNameStr(coinType);
                if (index == -1) {
                    cursor.close();
                    throw new SQLException();
                }
                // Get the number of coins collected
                int collected = fetchTotalCollected(db, tableName);
                if (collected == -1) {
                    cursor.close();
                    throw new SQLException();
                }
                // Add it to the list of collections
                collectionListEntries.add(new CollectionListInfo(
                        tableName,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL)),
                        collected,
                        index,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_DISPLAY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_START_YEAR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_END_YEAR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(colShowMintMarks)),
                        cursor.getString(cursor.getColumnIndexOrThrow(colShowCheckboxes))));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Gets the collection parameters based on the collection contents,
     * which is needed for database upgrade.
     *
     * @param db            database
     * @param legacyOptions if true, uses the legacy mint marks / checkbox columns
     * @return List of CollectionListInfo populated based on contents
     */
    public static ArrayList<CollectionListInfo> getLegacyCollectionParams(SQLiteDatabase db, boolean legacyOptions) throws SQLException {

        ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
        getAllTables(db, collectionListEntries, legacyOptions);
        for (CollectionListInfo collectionListEntry : collectionListEntries) {
            ArrayList<CoinSlot> coinList = getCoinListForLegacyCollectionParams(db, collectionListEntry.getName());
            collectionListEntry.setCreationParametersFromCoinData(coinList);
        }
        return collectionListEntries;
    }

    /**
     * Updates an existing coin list
     *
     * @param db          database
     * @param tableName   the collection name
     * @param coinData    coin data to use for updates
     * @param updateTotal if true, updates the collection info total
     * @throws SQLException if a database error occurs
     */
    public static void updateCoinList(SQLiteDatabase db, String tableName, ArrayList<CoinSlot> coinData, boolean updateTotal) throws SQLException {
        runSqlDelete(db, tableName, "1", null);
        for (CoinSlot coinSlot : coinData) {
            ContentValues values = new ContentValues();
            values.put(COL_COIN_IDENTIFIER, coinSlot.getIdentifier());
            values.put(COL_COIN_MINT, coinSlot.getMint());
            values.put(COL_IN_COLLECTION, coinSlot.isInCollectionInt());
            values.put(COL_ADV_GRADE_INDEX, coinSlot.getAdvancedGrades());
            values.put(COL_ADV_QUANTITY_INDEX, coinSlot.getAdvancedQuantities());
            values.put(COL_ADV_NOTES, coinSlot.getAdvancedNotes());
            values.put(COL_SORT_ORDER, coinSlot.getSortOrder());
            values.put(COL_CUSTOM_COIN, coinSlot.isCustomCoin());
            values.put(COL_IMAGE_ID, coinSlot.getImageId());
            coinSlot.setDatabaseId(runSqlInsert(db, tableName, values));
        }

        // Update the collection total if needed
        if (updateTotal) {
            ContentValues values = new ContentValues();
            values.put(COL_TOTAL, coinData.size());
            runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_NAME + "=?", new String[]{tableName});
        }
    }

    /**
     * Update database info for an existing collection
     *
     * @param db                 database
     * @param oldTableName       the original collection name
     * @param collectionListInfo new collection info
     * @param coinData           new coin data
     * @param legacyOptions      if true, uses the legacy mint marks / checkbox columns
     * @throws SQLException if a database error occurs
     */
    public static void updateExistingCollection(SQLiteDatabase db, String oldTableName, CollectionListInfo collectionListInfo,
                                                ArrayList<CoinSlot> coinData, boolean legacyOptions) throws SQLException {

        // Update the coin data
        if (coinData != null) {
            updateCoinList(db, oldTableName, coinData, false);
        }

        String colShowMintMarks = legacyOptions ? COL_SHOW_MINT_MARKS_LEGACY : COL_SHOW_MINT_MARKS;
        String colShowCheckboxes = legacyOptions ? COL_SHOW_CHECKBOXES_LEGACY : COL_SHOW_CHECKBOXES;

        // Update the collection info
        ContentValues values = new ContentValues();
        values.put(COL_COIN_TYPE, collectionListInfo.getType());
        values.put(COL_TOTAL, collectionListInfo.getMax());
        values.put(COL_DISPLAY, collectionListInfo.getDisplayType());
        values.put(COL_START_YEAR, collectionListInfo.getStartYear());
        values.put(COL_END_YEAR, collectionListInfo.getEndYear());
        values.put(colShowMintMarks, collectionListInfo.getMintMarkFlags());
        values.put(colShowCheckboxes, collectionListInfo.getCheckboxFlags());
        runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_NAME + "=?", new String[]{oldTableName});

        // Rename the collection if needed
        if (!oldTableName.equals(collectionListInfo.getName())) {
            updateCollectionName(db, oldTableName, collectionListInfo.getName());
        }
    }

    /**
     * Executes the SQL insert command and returns false if an error occurs
     *
     * @param db        The database
     * @param tableName The table to insert into
     * @param values    Values to insert into the table
     * @throws SQLException if an insert error occurred
     */
    public static long runSqlInsert(SQLiteDatabase db, String tableName, ContentValues values) throws SQLException {
        return db.insertOrThrow("[" + tableName + "]", null, values);
    }

    /**
     * Executes the SQL update command and returns false if an error occurs
     *
     * @param db          The database
     * @param tableName   Table to update
     * @param values      Values to update
     * @param whereClause Where clause
     * @param whereArgs   Where args
     * @return the number of rows impacted
     */
    public static int runSqlUpdate(SQLiteDatabase db, String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update("[" + tableName + "]", values, whereClause, whereArgs);
    }

    /**
     * Wrapper for simpleQueryForLong
     *
     * @param compiledStatement statement to execute
     * @return int query result
     * @throws SQLException if a database exception occurs
     */
    public static int simpleQueryForLong(SQLiteStatement compiledStatement) throws SQLException {
        try {
            return (int) compiledStatement.simpleQueryForLong();
        } catch (SQLiteDoneException e) {
            throw new SQLException();
        }
    }

    /**
     * Wrapper for delete
     *
     * @param db          The database
     * @param tableName   Table to update
     * @param whereClause Where clause
     * @param whereArgs   Where args
     * @return the number of rows impacted
     */
    public static int runSqlDelete(SQLiteDatabase db, String tableName, String whereClause, String[] whereArgs) {
        return db.delete("[" + tableName + "]", whereClause, whereArgs);
    }
}
