/*
 * Copyright (C) 2008 Google Inc.
 * Modified by Andrew Williams
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.coincollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.coincollection.CoinSlot.COIN_SLOT_WHERE_CLAUSE;
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
import static com.coincollection.DatabaseHelper.simpleQueryForLong;
import static com.coincollection.MainActivity.EXPORT_COLLECTION_LIST_FILE_NAME;

/**
 * Adapter based on the Simple Notes Database Access Helper Class on the Android site.
 * 
 * This Adapter is used to get information that the user has entered regarding his or her coin
 * collections (from the backing database.)
 */
public class DatabaseAdapter {

    private final DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb = null;

    /**
     * Record any internal DB names here!
     * Because internal tables and user tables aren't differentiated, we must prohibit
     * users from trying to create database collections that map to internal DB names
     * Must also include the collection export file name, so that import/exports work
     */
    private final List<String> mReservedDbNames = new ArrayList<>(Arrays.asList(
            TBL_COLLECTION_INFO,
            EXPORT_COLLECTION_LIST_FILE_NAME
    ));

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param context the Context within which to work
     */
    public DatabaseAdapter(Context context) {
        mDbHelper = new DatabaseHelper(context);
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * @throws SQLException if the database cannot be opened
     */
    public void open() throws SQLException {
        if (mDb == null || !mDb.isOpen()) {
            mDb = mDbHelper.getWritableDatabase();
        }
    }

    /**
     * Close the current database connection
     */
    public void close() {
        if (mDb != null && mDb.isOpen()) {
            mDb.close();
            mDb = null;
        }
    }

    /**
     * Returns whether a coinIdentifier and coinMint has been marked as collected in a given
     * collection.
     * @param tableName The collection of interest
     * @param coinSlot The coin we want to retrieve data for
     * @return 0 if item is in the collection, 1 otherwise
     * @throws SQLException if coin could not be found (shouldn't happen)
     */
    // TODO Retrieving the coin information individually (and onScroll) is inefficient... We should
    // instead have one query that returns all of the info.
    public int fetchIsInCollection(String tableName, CoinSlot coinSlot) throws SQLException {
        String sqlCmd = "SELECT " + COL_IN_COLLECTION + " FROM [" + tableName + "] WHERE " + COIN_SLOT_WHERE_CLAUSE + " LIMIT 1";
        SQLiteStatement compiledStatement = mDb.compileStatement(sqlCmd);
        compiledStatement.bindString(1, coinSlot.getIdentifier());
        compiledStatement.bindString(2, coinSlot.getMint());
        int result = simpleQueryForLong(compiledStatement);
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Updates a coins presence in the database.
     *
     * @param tableName The name of the collection of interest
     * @param coinSlot The coin we want to retrieve data for
     * @throws SQLException if the database update was not successful
     */
    public void toggleInCollection(String tableName, CoinSlot coinSlot) throws SQLException {
        int result = fetchIsInCollection(tableName, coinSlot);
        int newValue = (result + 1) % 2;
        ContentValues args = new ContentValues();
        args.put(COL_IN_COLLECTION, newValue);
        String[] whereValues = new String[] {coinSlot.getIdentifier(), coinSlot.getMint()};
        runSqlUpdateAndCheck(tableName, args, COIN_SLOT_WHERE_CLAUSE, whereValues);
    }

    /**
     * Returns the display configured for the table (advanced view, simple view, etc.)
     *
     * @param tableName - Used to know which table to query
     * @return which display we should show.  See MainApplication for the types
     * @throws SQLException if an SQL-related error occurs
     */
    public int fetchTableDisplay(String tableName) throws SQLException {
        // The database will only be set up this way in this case
        String sqlCmd = "SELECT " + COL_DISPLAY + " FROM " + TBL_COLLECTION_INFO + " WHERE name=? LIMIT 1";
        SQLiteStatement compiledStatement = mDb.compileStatement(sqlCmd);
        compiledStatement.bindString(1, tableName);
        int result = simpleQueryForLong(compiledStatement);
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Updates the display type associated with a given collection
     *
     * @param tableName - Used to know which table to update
     * @param displayType - New display type to store for this table
     * @throws SQLException if the database update was not successful
     */
    public void updateTableDisplay(String tableName, int displayType) throws SQLException {
        ContentValues args = new ContentValues();
        args.put(COL_DISPLAY, displayType);
        runSqlUpdateAndCheck(TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[] { tableName });
    }

    /**
     * Updates the order in which a collection should appear in the list of collections
     *
     * @param tableName - Used to know which table to update
     * @param displayOrder - New displayOrder to store for this table
     * @throws SQLException if the database update was not successful
     */
    public void updateDisplayOrder(String tableName, int displayOrder) throws SQLException {
        ContentValues args = new ContentValues();
        args.put(COL_DISPLAY_ORDER, displayOrder);
        runSqlUpdateAndCheck(TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[] { tableName });
    }

    /**
     * Updates the info for the coin in table 'name' where the coin is identified with
     * coinIdentifier and coinMint. This includes the advanced info (coin grade, quantity, and
     * notes) in addition to whether it is inc the collection.
     * @param tableName The collection name
     * @param coinSlot Coin slot
     * @throws SQLException if the database update was not successful
     */
    void updateAdvInfo(String tableName, CoinSlot coinSlot) throws SQLException {
        ContentValues args = new ContentValues();
        args.put(COL_IN_COLLECTION, coinSlot.isInCollectionInt());
        args.put(COL_ADV_GRADE_INDEX, coinSlot.getAdvancedGrades());
        args.put(COL_ADV_QUANTITY_INDEX, coinSlot.getAdvancedQuantities());
        args.put(COL_ADV_NOTES, coinSlot.getAdvancedNotes());
        String[] whereValues = new String[] {coinSlot.getIdentifier(), coinSlot.getMint()};
        runSqlUpdateAndCheck(tableName, args, COIN_SLOT_WHERE_CLAUSE, whereValues);
    }

    /**
     * Helper function to issue the SQL needed when creating a new database table for a collection
     * @param tableName The collection name
     * @throws SQLException if the database error occurs
     */
    private void createCollectionTable(String tableName) throws SQLException {
        // v2.2.1 - Until this point all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
        String sqlCmd = "CREATE TABLE [" + tableName
        + "] (_id integer primary key,"
        + " " + COL_COIN_IDENTIFIER + " text not null,"
        + " " + COL_COIN_MINT + " text,"
        + " " + COL_IN_COLLECTION + " integer,"
        + " " + COL_ADV_GRADE_INDEX + " integer default 0,"
        + " " + COL_ADV_QUANTITY_INDEX + " integer default 0,"
        + " " + COL_ADV_NOTES + " text default \"\");";
        mDb.execSQL(sqlCmd);
    }

    /**
     * Handles adding everything needed for a collection to store it's data in the database.
     * This also allows the data to be pre-populated in the database.
     * @param collectionListInfo The collection info
     * @param coinData The data that should be put into the backing database once it is created
     * @throws SQLException if the database update was not successful
     */
    public void createAndPopulateNewTable(CollectionListInfo collectionListInfo, int displayOrder, ArrayList<CoinSlot> coinData) throws SQLException {

        // Actually make the table
        String tableName = collectionListInfo.getName();
        createCollectionTable(tableName);

        // We have the list of identifiers, now set them correctly
        if (coinData != null) {
            for (CoinSlot coinSlot : coinData) {
                ContentValues values = new ContentValues();
                values.put(COL_COIN_IDENTIFIER, coinSlot.getIdentifier());
                values.put(COL_COIN_MINT, coinSlot.getMint());
                values.put(COL_IN_COLLECTION, coinSlot.isInCollectionInt());
                values.put(COL_ADV_GRADE_INDEX, coinSlot.getAdvancedGrades());
                values.put(COL_ADV_QUANTITY_INDEX, coinSlot.getAdvancedQuantities());
                values.put(COL_ADV_NOTES, coinSlot.getAdvancedNotes());
                runSqlInsert(tableName, values);
            }
        }

        // We also need to add the table to the list of tables
        ContentValues values = new ContentValues();
        values.put(COL_NAME, collectionListInfo.getName());
        values.put(COL_COIN_TYPE, collectionListInfo.getType());
        values.put(COL_TOTAL, collectionListInfo.getMax());
        values.put(COL_DISPLAY_ORDER, displayOrder);
        values.put(COL_DISPLAY, collectionListInfo.getDisplayType());
        values.put(COL_START_YEAR, collectionListInfo.getStartYear());
        values.put(COL_END_YEAR, collectionListInfo.getEndYear());
        values.put(COL_SHOW_MINT_MARKS, collectionListInfo.getMintMarkFlags());
        values.put(COL_SHOW_CHECKBOXES, collectionListInfo.getCheckboxFlags());
        runSqlInsert(TBL_COLLECTION_INFO, values);
    }

    /**
     * Handles removing a collection from the database
     * @param tableName The collection name
     * @throws SQLException if a database error occurs
     */
    public void dropCollectionTable(String tableName) throws SQLException {
        String dropTableCmd = "DROP TABLE [" + tableName + "];";
        mDb.execSQL(dropTableCmd);
        runSqlDeleteAndCheck(TBL_COLLECTION_INFO, COL_NAME + "=?", new String[] { tableName });
    }

    /**
     * Deletes the table of metadata about all the current collections
     * @throws SQLException if a database error occurs
     */
    void dropCollectionInfoTable() throws SQLException {
        String dropTableCmd = "DROP TABLE [" + TBL_COLLECTION_INFO + "];";
        mDb.execSQL(dropTableCmd);
    }

    /**
     * Return a Cursor that gives the names of all of the defined collections
     * @return Cursor to iterate over
     */
    public Cursor getAllCollectionNames() {
        return mDb.query(TBL_COLLECTION_INFO, new String[] {COL_NAME}, null, null, null, null, COL_DISPLAY_ORDER);
    }

    /**
     * Get the list of identifiers for each collection
     *
     * @param tableName The name of the collection
     * @return List of all coins in the collection
     */
    public ArrayList<CoinSlot> getAllIdentifiers(String tableName) {

        ArrayList<CoinSlot> coinList = new ArrayList<>();
        Cursor cursor = mDb.query("[" + tableName + "]",
                new String[] {COL_COIN_IDENTIFIER, COL_COIN_MINT},
                null, null, null, null, "_id");
        if (cursor.moveToFirst()){
            do {
                coinList.add(new CoinSlot(
                        cursor.getString(cursor.getColumnIndex(COL_COIN_IDENTIFIER)),
                        cursor.getString(cursor.getColumnIndex(COL_COIN_MINT)),
                        false));
            } while(cursor.moveToNext());
        }
        cursor.close();
        return coinList;
    }

    /**
     * Expose the dbHelper's onUpgrade method so we can call it manually when importing collections
     *
     * @param oldVersion the db version to upgrade from
     * @param fromImport true if the upgrade is part of a database import
     */
    void upgradeCollections(int oldVersion, boolean fromImport) {
        DatabaseHelper.upgradeDb(mDb, oldVersion, MainApplication.DATABASE_VERSION, fromImport);
    }

    /**
     * Check if a name can be used for a new/renamed collection
     * @param tableName The collection name
     * @return -1 if successful otherwise a resource id corresponding to an error message
     */
    public int checkCollectionName(String tableName) {

        // Make sure the name isn't in the reserved list
        if (mReservedDbNames.contains(tableName)) {
            return R.string.collection_name_reserved;
        }

        // By the time the user is able to click this mDbAdapter should not be NULL anymore
        Cursor cursor = this.getAllCollectionNames();
        if (cursor.moveToFirst()){
            do {
                Locale defaultLocale = Locale.getDefault();
                if(cursor.getString(cursor.getColumnIndex(COL_NAME)).toLowerCase(defaultLocale).equals(tableName.toLowerCase(defaultLocale))){
                    cursor.close();
                    return R.string.collection_name_exists;
                }

            } while(cursor.moveToNext());
        }
        cursor.close();
        return -1;
    }

    /**
     * Get the next display order for a new collection
     * @return The next display order to use
     * @throws SQLException if a database error occurred
     */
    public int getNextDisplayOrder() throws SQLException {
        String sqlCmd = "SELECT MAX(" + COL_DISPLAY_ORDER + ") FROM " + TBL_COLLECTION_INFO;
        SQLiteStatement compiledStatement = mDb.compileStatement(sqlCmd);
        int result = simpleQueryForLong(compiledStatement);
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result + 1;
    }

    /**
     * Copy collection
     * @param sourceCollectionListInfo Source table info
     * @param newTableName Name of the new table to create
     * @param insertIndex index to place the new collection at
     * @throws SQLException if a database error occurs
     * @return the newly created CollectionListInfo
     */
    CollectionListInfo createCollectionCopy(CollectionListInfo sourceCollectionListInfo, String newTableName, int insertIndex) throws SQLException {

        // Add the new table but don't populate
        CollectionListInfo newCollectionListInfo = sourceCollectionListInfo.copy(newTableName);
        createAndPopulateNewTable(newCollectionListInfo, insertIndex, null);

        // Populate the contents use SQL commands
        String sourceTableName = sourceCollectionListInfo.getName();
        String populateDbCmd = "INSERT INTO [" + newTableName + "] SELECT * FROM [" + sourceTableName + "];";
        mDb.execSQL(populateDbCmd);

        // Return the newly created object
        return newCollectionListInfo;
    }

    /**
     * Helper function to rename a collection
     * @param oldName The original collection name
     * @param newName The new collection name
     * @throws SQLException if the database update was not successful
     */
    public void updateCollectionName(String oldName, String newName) throws SQLException {
        DatabaseHelper.updateCollectionName(mDb, oldName, newName);
    }

    /**
     * Update database info for an existing collection
     * @param oldTableName the original collection name
     * @param collectionListInfo new collection info
     * @param coinData new coin data
     * @throws SQLException if a database error occurs
     */
    public void updateExistingCollection(String oldTableName, CollectionListInfo collectionListInfo, ArrayList<CoinSlot> coinData) throws SQLException {
        DatabaseHelper.updateExistingCollection(mDb, oldTableName, collectionListInfo, coinData);
    }


    /**
     * Creates the table of metadata for all the current collections
     * @throws SQLException if a database error occurs
     */
    void createCollectionInfoTable() throws SQLException {
        DatabaseHelper.createCollectionInfoTable(mDb);
    }

    /**
     * Returns a list of all collections in the database
     * @throws SQLException if a database error occurs
     */
    public void getAllTables(ArrayList<CollectionListInfo> collectionListEntries) throws SQLException {
        DatabaseHelper.getAllTables(mDb, collectionListEntries);
    }

    /**
     * Get the basic coin information
     *
     * @param tableName The name of the collection
     * @param populateAdvInfo If true, includes advanced attributes
     * @return CoinSlot list
     */
    public ArrayList<CoinSlot> getCoinList(String tableName, boolean populateAdvInfo) {
        return DatabaseHelper.getCoinList(mDb, tableName, populateAdvInfo);
    }

    /**
     * Executes the SQL insert command and returns false if an error occurs
     * @param tableName The table to insert into
     * @param values Values to insert into the table
     * @throws SQLException if an insert error occurred
     */
    void runSqlInsert(String tableName, ContentValues values) throws SQLException {
        DatabaseHelper.runSqlInsert(mDb, tableName, values);
    }

    /**
     * Executes the SQL update command and returns false if an error occurs
     * @param tableName Table to update
     * @param values Values to update
     * @param whereClause Where clause
     * @param whereArgs Where args
     * @throws SQLException if the update did not affect any rows
     */
    void runSqlUpdateAndCheck(String tableName, ContentValues values, String whereClause, String[] whereArgs) throws SQLException {
        if (DatabaseHelper.runSqlUpdate(mDb, tableName, values, whereClause, whereArgs) <= 0) {
            throw new SQLException();
        }
    }

    /**
     * Wrapper for delete
     * @param table Table to update
     * @param whereClause Where clause
     * @param whereArgs Where args
     * @throws SQLException if the delete did not affect any rows
     */
    void runSqlDeleteAndCheck(String table, String whereClause, String[] whereArgs) throws SQLException {
        if (DatabaseHelper.runSqlDelete(mDb, table, whereClause, whereArgs) <= 0) {
            throw new SQLException();
        }
    }
}
