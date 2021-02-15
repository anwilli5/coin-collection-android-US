package com.coincollection;

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

import static com.coincollection.CoinSlot.COL_ADV_GRADE_INDEX;
import static com.coincollection.CoinSlot.COL_ADV_NOTES;
import static com.coincollection.CoinSlot.COL_ADV_QUANTITY_INDEX;
import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.coincollection.CollectionListInfo.COL_COIN_TYPE;
import static com.coincollection.CollectionListInfo.COL_DISPLAY;
import static com.coincollection.CollectionListInfo.COL_DISPLAY_ORDER;
import static com.coincollection.CollectionListInfo.COL_END_YEAR;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_SHOW_CHECKBOXES;
import static com.coincollection.CollectionListInfo.COL_SHOW_MINT_MARKS;
import static com.coincollection.CollectionListInfo.COL_START_YEAR;
import static com.coincollection.CollectionListInfo.COL_TOTAL;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.coincollection.CollectionPage.SIMPLE_DISPLAY;
import static com.spencerpages.MainApplication.APP_NAME;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static com.spencerpages.MainApplication.DATABASE_VERSION;

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
     * @param db database to add to
     * @throws SQLException if an error occurs
     */
    public static void createCollectionInfoTable(SQLiteDatabase db) throws SQLException {

        // v2.2.1 - Until this version all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
        String makeCollectionInfoTable = "CREATE TABLE " + TBL_COLLECTION_INFO + " (_id integer primary key,"
                + " " + COL_NAME + " text not null,"
                + " " + COL_COIN_TYPE + " text not null,"
                + " " + COL_TOTAL + " integer,"
                + " " + COL_DISPLAY + " integer default " + SIMPLE_DISPLAY + ","
                + " " + COL_DISPLAY_ORDER + " integer,"
                + " " + COL_START_YEAR + " integer default 0,"
                + " " + COL_END_YEAR + " integer default 0,"
                + " " + COL_SHOW_MINT_MARKS + " integer default 0,"
                + " " + COL_SHOW_CHECKBOXES + " integer default 0"
                + ");";

        db.execSQL(makeCollectionInfoTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DatabaseHelper.upgradeDb(db, oldVersion, newVersion, false);
    }

    /**
     * Upgrades the database
     *
     * @param db the database to upgrade
     * @param oldVersion the database's current version
     * @param newVersion the version to upgrade to
     * @param fromImport if true, indicates that the upgrade is part of a collection import
     */
    public static void upgradeDb(SQLiteDatabase db, int oldVersion, int newVersion, boolean fromImport) {

        if(BuildConfig.DEBUG) {
            Log.i(APP_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion);
        }

        // First call the MainApplication's onDatabaseUpgrade to ensure that any changes necessary
        // for the app to work are done.
        MainApplication.onDatabaseUpgrade(db, oldVersion, newVersion, fromImport);

        // Now get a list of the collections and call each one's onCollectionDatabaseUpgrade method
        ArrayList<CollectionListInfo> collectionList = new ArrayList<>();
        getAllTables(db, collectionList);
        for (CollectionListInfo collectionListInfo : collectionList) {
            String tableName = collectionListInfo.getName();
            int numCoinsAdded = collectionListInfo.getCollectionObj().onCollectionDatabaseUpgrade (
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
     * @param db database
     * @param oldName The original collection name
     * @param newName The new collection name
     * @throws SQLException if the database update was not successful
     */
    static void updateCollectionName(SQLiteDatabase db, String oldName, String newName) throws SQLException {
        String alterDbSqlStr = "ALTER TABLE [" + oldName + "] RENAME TO [" + newName + "]";
        db.execSQL(alterDbSqlStr);
        ContentValues args = new ContentValues();
        args.put(COL_NAME, newName);
        runSqlUpdate(db, TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[] { oldName });
    }

    /**
     * Adds new coins to the collection based on collection creation parameters
     * @param collectionListInfo the collection info
     * @param values the ArrayList containing coinIdentifier values to add
     * @return number of rows added
     */
    public static int addFromArrayList(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                       ArrayList<String> values) {
        int total = 0;
        String tableName = collectionListInfo.getName();
        for (int i = 0; i < values.size(); i++) {
            if (collectionListInfo.hasMintMarks()) {
                for (String flagStr : CollectionListInfo.MINT_STRING_TO_FLAGS.keySet()) {
                    Integer mintFlag = CollectionListInfo.MINT_STRING_TO_FLAGS.get(flagStr);
                    if (mintFlag != null && ((collectionListInfo.getMintMarkFlags() & mintFlag) != 0)) {
                        ContentValues insertValues = new ContentValues();
                        insertValues.put(COL_COIN_IDENTIFIER, values.get(i));
                        insertValues.put(COL_IN_COLLECTION, 0);
                        insertValues.put(COL_COIN_MINT, flagStr);
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
                if (db.insert("[" + tableName + "]", null, insertValues) != -1) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Add coins for the new year for the "P", "D", and "" mint marks
     * @param db database
     * @param collectionListInfo the collection info
     * @param previousYear previous year to look for to know if this coin should be added
     * @param year coin year
     * @param identifier identifier of the coin to add
     * @return total number of coins added
     */
    public static int addFromYear(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                  int previousYear, int year, String identifier) {
        ArrayList<String> mintList = new ArrayList<>(Arrays.asList("P", "D"));
        return addFromYear(db, collectionListInfo, previousYear, year, identifier, mintList);
    }

    /**
     * Add coins for the new year for the "P", "D", and "" mint marks
     * @param db database
     * @param collectionListInfo the collection info
     * @param year coin year
     * @return total number of coins added
     */
    public static int addFromYear(SQLiteDatabase db, CollectionListInfo collectionListInfo, int year) {
        ArrayList<String> mintList = new ArrayList<>(Arrays.asList("P", "D"));
        return addFromYear(db, collectionListInfo, year-1, year, String.valueOf(year), mintList);
    }

    /**
     * Add coins for the new year, based on the collection parameters
     * @param db database
     * @param collectionListInfo the collection info
     * @param previousYear previous year to look for to know if this coin should be added
     * @param year coin year
     * @param identifier identifier of the coin to add
     * @param mintsToAdd list of the mint marks to add
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
        if (collectionListInfo.hasMintMarks()) {
            for (String flagStr : CollectionListInfo.MINT_STRING_TO_FLAGS.keySet()) {
                Integer mintFlag = CollectionListInfo.MINT_STRING_TO_FLAGS.get(flagStr);
                if (!mintsToAdd.contains(flagStr)) {
                    continue;
                }
                if (mintFlag != null && ((collectionListInfo.getMintMarkFlags() & mintFlag) != 0)) {
                    ContentValues insertValues = new ContentValues();
                    insertValues.put(COL_COIN_IDENTIFIER, identifier);
                    insertValues.put(COL_IN_COLLECTION, 0);
                    insertValues.put(COL_COIN_MINT, flagStr);
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
            if (db.insert("[" + tableName + "]", null, insertValues) != -1) {
                total++;
            }
        }

        // Update the collection's end year
        ContentValues updateValues = new ContentValues();
        updateValues.put(COL_END_YEAR, year);
        runSqlUpdate(db, TBL_COLLECTION_INFO, updateValues, COL_NAME + "=?", new String[] { tableName });
        collectionListInfo.setEndYear(year);

        return total;
    }

    /**
     * Get the basic coin information
     * @param db database
     * @param tableName The name of the collection
     * @param populateAdvInfo If true, includes advanced attributes
     * @return CoinSlot list
     */
    static ArrayList<CoinSlot> getCoinList(SQLiteDatabase db, String tableName, boolean populateAdvInfo) {

        String[] dbColumns;
        if (populateAdvInfo) {
            dbColumns = new String[] {COL_COIN_IDENTIFIER, COL_COIN_MINT, COL_IN_COLLECTION,
                    COL_ADV_GRADE_INDEX, COL_ADV_QUANTITY_INDEX, COL_ADV_NOTES};
        } else {
            dbColumns = new String[] {COL_COIN_IDENTIFIER, COL_COIN_MINT, COL_IN_COLLECTION};
        }

        ArrayList<CoinSlot> coinList = new ArrayList<>();
        Cursor cursor = db.query("[" + tableName + "]", dbColumns,
                null, null, null, null, "_id");
        if (cursor.moveToFirst()) {
            do {
                CoinSlot coinSlot = new CoinSlot(
                        cursor.getString(cursor.getColumnIndex(COL_COIN_IDENTIFIER)),
                        cursor.getString(cursor.getColumnIndex(COL_COIN_MINT)),
                        (cursor.getInt(cursor.getColumnIndex(COL_IN_COLLECTION)) == 1)
                );
                if (populateAdvInfo) {
                    coinSlot.setAdvancedGrades(cursor.getInt(cursor.getColumnIndex(COL_ADV_GRADE_INDEX)));
                    coinSlot.setAdvancedQuantities(cursor.getInt(cursor.getColumnIndex(COL_ADV_QUANTITY_INDEX)));
                    coinSlot.setAdvancedNotes(cursor.getString(cursor.getColumnIndex(COL_ADV_NOTES)));
                }
                coinList.add(coinSlot);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return coinList;
    }

    /**
     * Get the total number of coins in the collection
     * @param db database
     * @param tableName String that identifiers which table to query
     * @return int with the total number of coins in the collection
     * @throws SQLException if an error occurs
     */
    public static int fetchTotalCollected(SQLiteDatabase db, String tableName) throws SQLException {
        String sqlCmd = "SELECT COUNT(_id) FROM [" + tableName + "] WHERE " + COL_IN_COLLECTION + "=1 LIMIT 1";
        SQLiteStatement compiledStatement = db.compileStatement(sqlCmd);
        int result = simpleQueryForLong(compiledStatement);
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Returns a list of all collections in the database
     * @param db database
     * @param collectionListEntries List of CollectionListInfo to populate
     * @throws SQLException if a database error occurs
     */
    public static void getAllTables(SQLiteDatabase db, ArrayList<CollectionListInfo> collectionListEntries) throws SQLException {

        // Get rid of the other items in the list (if any)
        collectionListEntries.clear();
        Cursor cursor = db.query(TBL_COLLECTION_INFO,
                new String[] {COL_NAME, COL_COIN_TYPE, COL_TOTAL, COL_DISPLAY, COL_START_YEAR,
                        COL_END_YEAR, COL_SHOW_MINT_MARKS, COL_SHOW_CHECKBOXES},
                null, null, null, null, COL_DISPLAY_ORDER);
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(cursor.getColumnIndex(COL_NAME));
                String coinType = cursor.getString(cursor.getColumnIndex(COL_COIN_TYPE));
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
                        cursor.getInt(cursor.getColumnIndex(COL_TOTAL)),
                        collected,
                        index,
                        cursor.getInt(cursor.getColumnIndex(COL_DISPLAY)),
                        cursor.getInt(cursor.getColumnIndex(COL_START_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(COL_END_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(COL_SHOW_MINT_MARKS)),
                        cursor.getInt(cursor.getColumnIndex(COL_SHOW_CHECKBOXES))));
            } while(cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Gets the collection parameters based on the collection contents,
     * which is needed for database upgrade.
     * @param db database
     * @return List of CollectionListInfo populated based on contents
     */
    public static ArrayList<CollectionListInfo> getLegacyCollectionParams(SQLiteDatabase db) {

        ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
        getAllTables(db, collectionListEntries);
        for (CollectionListInfo collectionListEntry : collectionListEntries) {
            ArrayList<CoinSlot> coinList = getCoinList(db, collectionListEntry.getName(), false);
            collectionListEntry.setCreationParametersFromCoinData(coinList);
        }
        return collectionListEntries;
    }

    /**
     * Update database info for an existing collection
     * @param db database
     * @param oldTableName the original collection name
     * @param collectionListInfo new collection info
     * @param coinData new coin data
     * @throws SQLException if a database error occurs
     */
    public static void updateExistingCollection(SQLiteDatabase db, String oldTableName, CollectionListInfo collectionListInfo, ArrayList<CoinSlot> coinData) throws SQLException {

        // Update the coin data
        if (coinData != null) {
            runSqlDelete(db, oldTableName, "1", null);
            for (CoinSlot coinSlot : coinData) {
                ContentValues values = new ContentValues();
                values.put(COL_COIN_IDENTIFIER, coinSlot.getIdentifier());
                values.put(COL_COIN_MINT, coinSlot.getMint());
                values.put(COL_IN_COLLECTION, coinSlot.isInCollectionInt());
                values.put(COL_ADV_GRADE_INDEX, coinSlot.getAdvancedGrades());
                values.put(COL_ADV_QUANTITY_INDEX, coinSlot.getAdvancedQuantities());
                values.put(COL_ADV_NOTES, coinSlot.getAdvancedNotes());
                runSqlInsert(db, oldTableName, values);
            }
        }

        // Update the collection info
        ContentValues values = new ContentValues();
        values.put(COL_COIN_TYPE, collectionListInfo.getType());
        values.put(COL_TOTAL, collectionListInfo.getMax());
        values.put(COL_DISPLAY, collectionListInfo.getDisplayType());
        values.put(COL_START_YEAR, collectionListInfo.getStartYear());
        values.put(COL_END_YEAR, collectionListInfo.getEndYear());
        values.put(COL_SHOW_MINT_MARKS, collectionListInfo.getMintMarkFlags());
        values.put(COL_SHOW_CHECKBOXES, collectionListInfo.getCheckboxFlags());
        runSqlUpdate(db, TBL_COLLECTION_INFO, values, COL_NAME + "=?", new String[] { oldTableName });

        // Rename the collection if needed
        if (!oldTableName.equals(collectionListInfo.getName())) {
            updateCollectionName(db, oldTableName, collectionListInfo.getName());
        }
    }

    /**
     * Executes the SQL insert command and returns false if an error occurs
     * @param db The database
     * @param tableName The table to insert into
     * @param values Values to insert into the table
     * @throws SQLException if an insert error occurred
     */
    public static void runSqlInsert(SQLiteDatabase db, String tableName, ContentValues values) throws SQLException {
        if (db.insert("[" + tableName + "]", null, values) == -1) {
            throw new SQLException();
        }
    }

    /**
     * Executes the SQL update command and returns false if an error occurs
     * @param db The database
     * @param tableName Table to update
     * @param values Values to update
     * @param whereClause Where clause
     * @param whereArgs Where args
     * @return the number of rows impacted
     */
    public static int runSqlUpdate(SQLiteDatabase db, String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update("[" + tableName + "]", values, whereClause, whereArgs);
    }

    /**
     * Wrapper for simpleQueryForLong
     * @param compiledStatement statement to execute
     * @return int query result
     * @throws SQLException if a database exception occurs
     */
    public static int simpleQueryForLong (SQLiteStatement compiledStatement) throws SQLException {
        try {
            return (int) compiledStatement.simpleQueryForLong();
        } catch (SQLiteDoneException e) {
            throw new SQLException();
        }
    }

    /**
     * Wrapper for delete
     * @param db The database
     * @param tableName Table to update
     * @param whereClause Where clause
     * @param whereArgs Where args
     * @return the number of rows impacted
     */
    public static int runSqlDelete(SQLiteDatabase db, String tableName, String whereClause, String[] whereArgs) {
        return db.delete("[" + tableName + "]", whereClause, whereArgs);
    }
}
