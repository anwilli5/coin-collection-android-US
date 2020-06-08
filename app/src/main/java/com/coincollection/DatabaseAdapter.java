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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.coincollection.CoinSlot.COIN_SLOT_WHERE_CLAUSE;
import static com.coincollection.CoinSlot.COL_ADV_GRADE_INDEX;
import static com.coincollection.CoinSlot.COL_ADV_NOTES;
import static com.coincollection.CoinSlot.COL_ADV_QUANTITY_INDEX;
import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;

/**
 * Adapter based on the Simple Notes Database Access Helper Class on the Android site.
 * 
 * This Adapter is used to get information that the user has entered regarding his or her coin
 * collections (from the backing database.)
 */
public class DatabaseAdapter {

    private final DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    final static String TBL_COLLECTION_INFO = "collection_info";

    final static String COL_NAME = "name";
    final static String COL_COIN_TYPE = "coinType";
    final static String COL_TOTAL = "total";
    final static String COL_DISPLAY_ORDER = "displayOrder";
    final static String COL_DISPLAY = "display";

    /**
     * Record any internal DB names here!
     * Because internal tables and user tables aren't differentiated, we must prohibit
     * users from trying to create database collections that map to internal DB names
     */
    private final List<String> mReservedDbNames = new ArrayList<>(Collections.singletonList(
            TBL_COLLECTION_INFO
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
     *
     * @throws SQLException if the database could be neither opened or created
     */
    public void open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
    }

    // Clean up a bit if we no longer need this DatabaseAdapter
    public void close() {
        mDbHelper.close();
    }

    /**
     * Get the total number of coins in the collection
     *
     * @param tableName String that identifiers which table to query
     * @return int with the total number of coins in the collection
     */
    int fetchTotalCollected(String tableName) {
        String select_sqlStatement = "SELECT COUNT(_id) FROM [" + tableName + "] WHERE " + COL_IN_COLLECTION + "=1 LIMIT 1";
        SQLiteStatement compiledStatement;

        compiledStatement = mDb.compileStatement(select_sqlStatement);
        // TODO May generate a SQLITE_SCHEMA error (17) after just deleting a table, doesn't appear to break things though

        int result = (int) compiledStatement.simpleQueryForLong();

        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Returns whether a coinIdentifier and coinMint has been marked as collected in a given
     * collection.
     *
     * @param tableName The collection of interest
     * @param coinSlot The coin we want to retrieve data for
     * @return 0 if item is in the collection, 1 otherwise
     * @throws SQLException if coin could not be found (Shouldn't happen???)
     */
    // TODO Rename
    // TODO Retrieving the coin information individually (and onScroll) is inefficient... We should
    // instead have one query that returns all of the info.
    private int fetchIsInCollection(String tableName, CoinSlot coinSlot) throws SQLException {
        String select_sqlStatement = "SELECT " + COL_IN_COLLECTION + " FROM [" + tableName + "] WHERE " + COIN_SLOT_WHERE_CLAUSE + " LIMIT 1";
        SQLiteStatement compiledStatement = mDb.compileStatement(select_sqlStatement);
        compiledStatement.bindString(1, coinSlot.getIdentifier());
        compiledStatement.bindString(2, coinSlot.getMint());
        int result = (int) compiledStatement.simpleQueryForLong();
        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Updates a coins presence in the database.
     *
     * @param tableName The name of the collection of interest
     * @param coinSlot The coin we want to retrieve data for
     * @return true if the value was successfully updated, false otherwise
     */

    boolean toggleInCollection(String tableName, CoinSlot coinSlot) {
        int result = fetchIsInCollection(tableName, coinSlot);
        int newValue = (result + 1) % 2;
        ContentValues args = new ContentValues();
        args.put(COL_IN_COLLECTION, newValue);

        // TODO Should we do something if update fails?
        String[] whereValues = new String[] {coinSlot.getIdentifier(), coinSlot.getMint()};
        return mDb.update("[" + tableName + "]", args, COIN_SLOT_WHERE_CLAUSE, whereValues) > 0;
    }

    /**
     * Returns the display configured for the table (advanced view, simple view, etc.)
     *
     * @param tableName - Used to know which table to query
     * @return which display we should show.  See MainApplication for the types
     * @throws SQLException if an SQL-related error occurs
     */
    int fetchTableDisplay(String tableName) throws SQLException {

        // The database will only be set up this way in this case
        String select_sqlStatement = "SELECT " + COL_DISPLAY + " FROM " + TBL_COLLECTION_INFO + " WHERE name=? LIMIT 1";
        SQLiteStatement compiledStatement = mDb.compileStatement(select_sqlStatement);

        compiledStatement.bindString(1, tableName);
        int result = (int) compiledStatement.simpleQueryForLong();

        compiledStatement.clearBindings();
        compiledStatement.close();
        return result;
    }

    /**
     * Updates the display type associated with a given collection
     *
     * @param tableName - Used to know which table to update
     * @param displayType - New displaytype to store for this table
     * @return 0 on update success, 1 otherwise
     * @throws SQLException if an SQL-related error occurs
     */
    boolean updateTableDisplay(String tableName, int displayType) throws SQLException {

        ContentValues args = new ContentValues();
        args.put(COL_DISPLAY, displayType);

        // TODO Should we do something if update fails?
        return mDb.update(TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[] { tableName }) > 0;
    }

    /**
     * Updates the order in which a collection should appear in the list of collections
     *
     * @param tableName - Used to know which table to update
     * @param displayOrder - New displayOrder to store for this table
     * @return 0 if the update was successful, 1 otherwise
     * @throws SQLException if an SQL-related error occurs
     */
    boolean updateDisplayOrder(String tableName, int displayOrder) throws SQLException {

        ContentValues args = new ContentValues();
        args.put(COL_DISPLAY_ORDER, displayOrder);
        // TODO Should we do something if update fails?
        return mDb.update(TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[] { tableName }) > 0;
    }

    /**
     * Updates the info for the coin in table 'name' where the coin is identified with
     * coinIdentifier and coinMint. This includes the advanced info (coin grade, quantity, and
     * notes) in addition to whether it is inc the collection.
     * @param tableName The collection name
     * @param coinSlot Coin slot
     * @return 1 on success, 0 otherwise
     */
    int updateAdvInfo(String tableName, CoinSlot coinSlot) {

        ContentValues args = new ContentValues();
        args.put(COL_IN_COLLECTION, coinSlot.isInCollection() ? 1 : 0);
        args.put(COL_ADV_GRADE_INDEX, coinSlot.getAdvancedGrades());
        args.put(COL_ADV_QUANTITY_INDEX, coinSlot.getAdvancedQuantities());
        args.put(COL_ADV_NOTES, coinSlot.getAdvancedNotes());
        String[] whereValues = new String[] {coinSlot.getIdentifier(), coinSlot.getMint()};
        return mDb.update("[" + tableName + "]", args, COIN_SLOT_WHERE_CLAUSE, whereValues);
    }

    /**
     * Helper function to issue the SQL needed when creating a new database table for a collection
     * @param tableName The collection name
     */
    private void createNewTable(String tableName){
        // v2.2.1 - Until this point all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
        String DATABASE_CREATE = "CREATE TABLE [" + tableName
        + "] (_id integer primary key,"
        + " " + COL_COIN_IDENTIFIER + " text not null,"
        + " " + COL_COIN_MINT + " text,"
        + " " + COL_IN_COLLECTION + " integer,"
        + " " + COL_ADV_GRADE_INDEX + " integer default 0,"
        + " " + COL_ADV_QUANTITY_INDEX + " integer default 0,"
        + " " + COL_ADV_NOTES + " text default \"\");";

        mDb.execSQL(DATABASE_CREATE);
    }

    /**
     * Helper function to rename a collection
     * @param oldName The original collection name
     * @param newName The new collection name
     * @return true if the value was successfully updated, false otherwise
     */
    boolean updateCollectionName(String oldName, String newName){
        String alterDbSqlStr = "ALTER TABLE [" + oldName + "] RENAME TO [" + newName + "]";
        mDb.execSQL(alterDbSqlStr);

        ContentValues args = new ContentValues();
        args.put(COL_NAME, newName);

        // TODO Should we do something if update fails?
        return mDb.update(TBL_COLLECTION_INFO, args, COL_NAME + "=?", new String[] { oldName }) > 0;
    }

    /**
     * Handles adding everything needed for a collection to store it's data in the database.
     * This is used for creating new collections, and will initialize everything to a blank state.
     *
     * @param tableName The collection tableName
     * @param coinType The collection type
     * @param coinList A list of the identifiers for the coins in this collection (Ex: 2009)
     * @param displayOrder The position in the list of collections in which this should be displayed
     *                     TODO maybe make this not an argument, and determine this internally?
     * @return 1 TODO
     */
    // TODO Rename, since we aren't just creating a new table
    int createNewTable(String tableName, String coinType, ArrayList<CoinSlot> coinList, int displayOrder) {

        // Actually make the table
        createNewTable(tableName);

        // We have the list of identifiers, now set them correctly
        for(int j = 0; j < coinList.size(); j++){
            CoinSlot coinSlot = coinList.get(j);
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_COIN_IDENTIFIER, coinSlot.getIdentifier());
            initialValues.put(COL_COIN_MINT, coinSlot.getMint());
            initialValues.put(COL_IN_COLLECTION, 0);
            // Advanced info gets added automatically, if the columns are there

            long value = mDb.insert("[" + tableName + "]", null, initialValues);
            // TODO Do something if insert fails?
        }

        // We also need to add the table to the list of tables
        addEntryToCollectionInfoTable(tableName, coinType, coinList.size(), CollectionPage.SIMPLE_DISPLAY, displayOrder);

        return 1;
    }

    /**
     * Helper function to add a collection into the global list of collections
     * @param tableName Collection name
     * @param coinType Type of collection
     * @param total Number of coins in collection
     * @param display Type of display
     * @param displayOrder Display order of this collection
     */
    private void addEntryToCollectionInfoTable(String tableName, String coinType, int total, int display, int displayOrder){
        ContentValues values = new ContentValues();
        values.put(COL_NAME, tableName);
        values.put(COL_COIN_TYPE, coinType);
        values.put(COL_TOTAL, total);
        values.put(COL_DISPLAY_ORDER, displayOrder);
        values.put(COL_DISPLAY, display);

        long value = mDb.insert(TBL_COLLECTION_INFO, null, values);
        // TODO Do something if insert fails?
        // TODO It'd be great if there was a way to clear the prepared SQL Statement cache so that
        // we don't get SQL 17 Errors
    }

    /**
     * Handles adding everything needed for a collection to store it's data in the database.
     * This also allows the data to be pre-populated in the database. This is used for importing
     * collections
     * @param tableName The collection name
     * @param coinType The collection type
     * @param total The total number of coins in the collection (TODO I think)
     * @param display The display type of this collection
     * @param displayOrder The order that this collection should appear in the list of collections
     * @param rawData The data that should be put into the backing database once it is created
     * @return 1 TODO
     */
    // TODO Rename, since we aren't just creating a new table
    int createNewTable(String tableName, String coinType, int total, int display, int displayOrder, String[][] rawData) {

        // Actually make the table
        createNewTable(tableName);

        // We have the list of identifiers, now set them correctly
        for (String[] rawRowData : rawData) {

            // coinIdentifier, coinMint, inCollection, advGradeIndex, advQuantityIndex, advNotes
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_COIN_IDENTIFIER, rawRowData[0]);
            initialValues.put(COL_COIN_MINT, rawRowData[1]);
            initialValues.put(COL_IN_COLLECTION, Integer.valueOf(rawRowData[2]));
            initialValues.put(COL_ADV_GRADE_INDEX, Integer.valueOf(rawRowData[3]));
            initialValues.put(COL_ADV_QUANTITY_INDEX, Integer.valueOf(rawRowData[4]));
            initialValues.put(COL_ADV_NOTES, rawRowData[5]);

            long value = mDb.insert("[" + tableName + "]", null, initialValues);
            // TODO Do something if insert fails?
        }

        // We also need to add the table to the list of tables
        addEntryToCollectionInfoTable(tableName, coinType, total, display, displayOrder);
        return 1;
    }

    /**
     * Handles removing a collection from the database
     *
     * @param tableName The collection name
     */
    // TODO Rename, since it does more than just drop a table
    void dropTable(String tableName){
        String DATABASE_DROP = "DROP TABLE [" + tableName + "];";
        mDb.execSQL(DATABASE_DROP);

        int value = mDb.delete(TBL_COLLECTION_INFO, COL_NAME + "=?", new String[] { tableName });
        // TODO Do something if insert fails?
        // TODO It be great if there was a way to clear the prepared SQL Statement cache so that
        // we don't get SQL 17 Errors
        // ^^^ Not sure if this is still an issue
    }

    /**
     * Deletes the table of metadata about all the current collections
     */
    void dropCollectionInfoTable(){

        String DATABASE_DROP = "DROP TABLE [" + TBL_COLLECTION_INFO + "];";
        mDb.execSQL(DATABASE_DROP);
    }

    /**
     * Creates the table of metadata for all the current collections
     */
    void createCollectionInfoTable(){

        // I would put the functionality here and call it from within the mDbHelper,
        // but I couldn't figure out how to get this working.  :(
        mDbHelper._createCollectionInfoTable(mDb);
    }

    /**
     * Return a Cursor that gives the names of all of the defined collections
     *
     * @return Cursor
     */
    public Cursor getAllCollectionNames() {

        return mDb.query(TBL_COLLECTION_INFO, new String[] {COL_NAME}, null, null, null, null, COL_DISPLAY_ORDER);
    }

    /**
     * Return a Cursor that gives information about each of the collections
     *
     * @return Cursor
     */
    public Cursor getAllTables() {

        return mDb.query(TBL_COLLECTION_INFO, new String[] {COL_NAME, COL_COIN_TYPE,
        COL_TOTAL}, null, null, null, null, COL_DISPLAY_ORDER);
    }

    /**
     * Get the list of identifiers for each collection
     *
     * @param tableName The name of the collection
     * @return Cursor over all coins in the collection
     */
    public Cursor getAllIdentifiers(String tableName) {

        return mDb.query("[" + tableName + "]", new String[] {COL_COIN_IDENTIFIER, COL_COIN_MINT},
                null, null, null, null, "_id");
    }

    /**
     * Get the basic coin information
     *
     * @param tableName The name of the collection
     * @return Cursor over identifier, mint, and in collection
     */
    Cursor getBasicCoinInfo(String tableName) {

        return mDb.query("[" + tableName + "]", new String[] {COL_COIN_IDENTIFIER, COL_COIN_MINT, COL_IN_COLLECTION},
                null, null, null, null, "_id");
    }

    /**
     * Get whether each coin is in the collection
     *
     * @return Cursor over all coins in the collection
     */
    Cursor getInCollectionInfo(String tableName) {
        return mDb.query("[" + tableName + "]", new String[] {COL_IN_COLLECTION},
                    null, null, null, null, "_id");
    }


    /**
     * Get the advanced info associated with each coin in the collection
     *
     * @param tableName The collection name
     * @return Cursor over all coins in the collection
     */
    Cursor getAdvInfo(String tableName) {

        return mDb.query("[" + tableName + "]", new String[] {COL_ADV_GRADE_INDEX, COL_ADV_QUANTITY_INDEX, COL_ADV_NOTES},
                null, null, null, null, "_id");
    }

    /**
     * Get all collection info (for exporting)
     *
     * @param tableName The collection name
     * @return Cursor over all coins in the collection
     */
    Cursor getAllCollectionInfo(String tableName) {

        return mDb.query("[" + tableName + "]", new String[] {COL_COIN_IDENTIFIER, COL_COIN_MINT, COL_IN_COLLECTION, COL_ADV_GRADE_INDEX, COL_ADV_QUANTITY_INDEX, COL_ADV_NOTES},
                null, null, null, null, "_id");
    }

    /**
     * Expose the dbHelper's onUpgrade method so we can call it manually when importing collections
     *
     * @param oldVersion the db version to upgrade from
     */
    void upgradeCollections(int oldVersion) {
        mDbHelper.onUpgrade(mDb, oldVersion, MainApplication.DATABASE_VERSION);
    }

    /**
     * Check if a name can be used for a new/renamed collection
     * @param tableName The collection name
     * @return Empty string if name is valid, otherwise a reason why the name can't be used
     */
    String checkCollectionName(String tableName) {

        // Make sure the name isn't in the reserved list
        if (mReservedDbNames.contains(tableName)) {
            return "Collection name is reserved, please choose a different name";
        }

        // By the time the user is able to click this mDbAdapter should not be NULL anymore
        Cursor resultCursor = this.getAllCollectionNames();
        if(resultCursor == null){
            return "Failed to get list of current collections, low on memory perhaps?";
        }
        // THanks! http://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
        if (resultCursor.moveToFirst()){
            do {
                Locale defaultLocale = Locale.getDefault();
                if(resultCursor.getString(resultCursor.getColumnIndex(COL_NAME)).toLowerCase(defaultLocale).equals(tableName.toLowerCase(defaultLocale))){
                    resultCursor.close();
                    return "A collection with this name already exists, please choose a different name";
                }

            } while(resultCursor.moveToNext());
        }
        resultCursor.close();
        return "";
    }

    /**
     * Get the next display order for a new collection
     * @return The next display order to use
     */
    int getNextDisplayOrder() {
        String select_sqlStatement = "SELECT MAX(" + COL_DISPLAY_ORDER + ") FROM " + TBL_COLLECTION_INFO;
        SQLiteStatement compiledStatement;

        compiledStatement = mDb.compileStatement(select_sqlStatement);
        int result = (int) compiledStatement.simpleQueryForLong();

        compiledStatement.clearBindings();
        compiledStatement.close();

        return result + 1;
    }
}
