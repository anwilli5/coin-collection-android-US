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

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.coincollection.AsyncProgressInterface;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.MainActivity;

import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.DatabaseAdapter.COL_NAME;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class BaseTestCase {

    public final static int VERSION_1_YEAR = 2013;

    /**
     * Enable storage read/write permission
     * returns true if enabling permissions was successful
     */
    public boolean setEnabledPermissions(MainActivity activity) {
        ShadowApplication app = Shadows.shadowOf(activity.getApplication());
        app.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        return true;
    }

    /**
     * Populate the database with one collection of each type
     * @param activity activity associated with the collection
     * @return true if successful, otherwise false
     */
    public boolean setupOneOfEachCollectionTypes(MainActivity activity) {
        int displayOrder = 0;
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            HashMap<String, Object> parameters = new HashMap<>();
            collectionInfo.getCreationParameters(parameters);
            ArrayList<CoinSlot> newCoinList = new ArrayList<>();
            collectionInfo.populateCollectionLists(parameters, newCoinList);
            createNewTable(activity, collectionInfo.getCoinType(), collectionInfo.getCoinType(), newCoinList, displayOrder++);
        }
        return true;
    }

    /**
     * Populate the database with one collection of each type
     * @param activity activity associated with the collection
     * @return true if successful, otherwise false
     */
    public boolean setupCollectionsWithNames(MainActivity activity, ArrayList<String> collectionNames) {
        int displayOrder = 0;
        for (String collectionName : collectionNames) {
            CollectionInfo collectionInfo = COLLECTION_TYPES[displayOrder % COLLECTION_TYPES.length];
            HashMap<String, Object> parameters = new HashMap<>();
            collectionInfo.getCreationParameters(parameters);
            ArrayList<CoinSlot> newCoinList = new ArrayList<>();
            collectionInfo.populateCollectionLists(parameters, newCoinList);
            createNewTable(activity, collectionName, collectionInfo.getCoinType(), newCoinList, displayOrder++);
        }
        return true;
    }

    /**
     * Create a database table for a new collection
     * @param tableName Name of the table
     * @param coinType Type of coin
     * @param coinList List of coin slots
     * @param displayOrder Display order of the collection
     */
    public void createNewTable(Activity activity, String tableName, String coinType,
                        ArrayList<CoinSlot> coinList, int displayOrder) {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(activity);
        dbAdapter.open();
        dbAdapter.createNewTable(tableName, coinType, coinList, displayOrder);
        dbAdapter.close();
    }

    /**
     * Delete all collections from the database
     * @param activity activity that can be used to access the database
     */
    public void deleteAllCollections(Activity activity) {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(activity);
        dbAdapter.open();
        Cursor resultCursor = dbAdapter.getAllCollectionNames();
        assertNotNull(resultCursor);
        if (resultCursor.moveToFirst()){
            do{
                dbAdapter.dropTable(resultCursor.getString(resultCursor.getColumnIndex(COL_NAME)));
            } while(resultCursor.moveToNext());
        }
        resultCursor.close();
        dbAdapter.close();
    }

    /**
     * Get all collections from the database
     * @param activity activity that can be used to access the database
     */
    public ArrayList<String> getCollectionNames(Activity activity) {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(activity);
        dbAdapter.open();
        ArrayList<String> nameList = new ArrayList<>();
        Cursor resultCursor = dbAdapter.getAllCollectionNames();
        assertNotNull(resultCursor);
        if (resultCursor.moveToFirst()){
            do{
                nameList.add(resultCursor.getString(resultCursor.getColumnIndex(COL_NAME)));
            } while(resultCursor.moveToNext());
        }
        resultCursor.close();
        dbAdapter.close();
        return nameList;
    }

    /**
     * DB Helper for setting up test databases
     */
    public static class TestDatabaseHelper extends SQLiteOpenHelper {

        TestDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE collection_info (_id integer primary key,"
                    + " name text not null,"
                    + " coinType text not null,"
                    + " total integer"
                    + ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    /**
     * Adds a collection to the database that looks like version 1 of the app's DB scheme
     * @param db database to populate
     * @param collectionName collection name
     * @param coinType coin type
     * @param coinList list of coins
     */
    public void createV1Collection(SQLiteDatabase db, String collectionName, String coinType, ArrayList<Object[]> coinList) {

        db.execSQL("CREATE TABLE [" + collectionName
                + "] (_id integer primary key,"
                + " coinIdentifier text not null,"
                + " coinMint text,"
                + " inCollection integer);");

        for(Object[] coinInfo : coinList){
            ContentValues initialValues = new ContentValues();
            initialValues.put("coinIdentifier", (String) coinInfo[0]);
            initialValues.put("coinMint", (String) coinInfo[1]);
            initialValues.put("inCollection", (int) coinInfo[2]);
            db.insert("[" + collectionName + "]", null, initialValues);
        }

        ContentValues values = new ContentValues();
        values.put("name", collectionName);
        values.put("coinType", coinType);
        values.put("total", coinList.size());
        db.insert("collection_info", null, values);
    }

    /**
     * Validate updated database
     * @param collectionInfo collection info
     * @param collectionName collection name
     */
    public void validateUpdatedDb(final CollectionInfo collectionInfo, final String collectionName){
        HashMap<String, Object> parameters = new HashMap<>();
        validateUpdatedDb(collectionInfo, collectionName, parameters);
    }

    /**
     * Validate updated database
     * @param collectionInfo collection info
     * @param collectionName collection name
     * @param parameters setup parameters
     */
    public void validateUpdatedDb(final CollectionInfo collectionInfo, final String collectionName,
                                  final HashMap<String, Object> parameters){
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {

                    // Create a new database from scratch
                    collectionInfo.getCreationParameters(parameters);
                    ArrayList<CoinSlot> newCoinList = new ArrayList<>();
                    collectionInfo.populateCollectionLists(parameters, newCoinList);

                    // Get coins from the updated database
                    activity.mDbAdapter.open();
                    Cursor resultCursor = activity.mDbAdapter.getAllIdentifiers(collectionName);
                    assertNotNull(resultCursor);
                    ArrayList<CoinSlot> dbCoinList = new ArrayList<>();
                    if (resultCursor.moveToFirst()){
                        do {
                            dbCoinList.add(new CoinSlot(
                                    resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_IDENTIFIER)),
                                    resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_MINT)),
                                    false));
                        } while(resultCursor.moveToNext());
                    }
                    resultCursor.close();
                    activity.mDbAdapter.close();

                    // Make sure coin lists match
                    assertEquals(newCoinList.size(), dbCoinList.size());
                    for(int i = 0; i < newCoinList.size(); i++){
                        assertEquals(newCoinList.get(i).getIdentifier(), dbCoinList.get(i).getIdentifier());
                        assertEquals(newCoinList.get(i).getMint(), dbCoinList.get(i).getMint());
                    }

                    // Make sure total matches
                    activity.mDbAdapter.open();
                    resultCursor = activity.mDbAdapter.getAllTables();
                    boolean foundTable = false;
                    if (resultCursor.moveToFirst()){
                        do{
                            if(collectionName.equals(resultCursor.getString(resultCursor.getColumnIndex("name")))){
                                foundTable = true;
                                assertEquals(newCoinList.size(), resultCursor.getInt(resultCursor.getColumnIndex("total")));
                            }
                        } while(resultCursor.moveToNext());
                    }
                    resultCursor.close();
                    activity.mDbAdapter.close();
                    assertTrue(foundTable);
                }
            });
        }
    }
}
