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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.CollectionPage;
import com.coincollection.DatabaseAdapter;
import com.coincollection.MainActivity;
import com.spencerpages.MainApplication;
import com.spencerpages.SharedTest;

import junit.framework.TestCase;

import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.coincollection.CollectionListInfo.COL_COIN_TYPE;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_TOTAL;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BaseTestCase {

    public final static int VERSION_1_YEAR = 2013;

    /**
     * Gets a minimally populated CollectionListInfo
     * @param name collection name
     * @param collectionInfo associated CollectionInfo object
     * @param coinList (optional) list of coins
     * @return CollectionListInfo object
     */
    public CollectionListInfo getCollectionListInfo(String name, CollectionInfo collectionInfo,
                                                    ArrayList<CoinSlot> coinList) {
        return new CollectionListInfo(
                name,
                (coinList != null ? coinList.size() : 0),
                0,
                MainApplication.getIndexFromCollectionNameStr(collectionInfo.getCoinType()),
                CollectionPage.SIMPLE_DISPLAY,
                0, 0, 0, 0);
    }

    /**
     * Enable storage read/write permission
     * @return true if enabling permissions was successful
     */
    @SuppressWarnings("SameReturnValue")
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
    @SuppressWarnings("SameReturnValue")
    public boolean setupOneOfEachCollectionTypes(MainActivity activity) {
        int displayOrder = 0;
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            HashMap<String, Object> parameters = new HashMap<>();
            collectionInfo.getCreationParameters(parameters);
            ArrayList<CoinSlot> newCoinList = new ArrayList<>();
            collectionInfo.populateCollectionLists(parameters, newCoinList);
            CollectionListInfo collectionListInfo = getCollectionListInfo(
                    collectionInfo.getCoinType(),
                    collectionInfo,
                    newCoinList);
            createNewTable(activity, collectionListInfo, newCoinList, displayOrder++);
        }
        return true;
    }

    /**
     * Populate the database with one collection of each type
     * @param activity activity associated with the collection
     * @return true if successful, otherwise false
     */
    @SuppressWarnings("SameReturnValue")
    public boolean setupCollectionsWithNames(MainActivity activity, ArrayList<String> collectionNames) {
        int displayOrder = 0;
        for (String collectionName : collectionNames) {
            CollectionInfo collectionInfo = COLLECTION_TYPES[displayOrder % COLLECTION_TYPES.length];
            HashMap<String, Object> parameters = new HashMap<>();
            collectionInfo.getCreationParameters(parameters);
            ArrayList<CoinSlot> newCoinList = new ArrayList<>();
            collectionInfo.populateCollectionLists(parameters, newCoinList);
            CollectionListInfo collectionListInfo = getCollectionListInfo(
                    collectionName,
                    collectionInfo,
                    newCoinList);
            createNewTable(activity, collectionListInfo, newCoinList, displayOrder++);
        }
        return true;
    }

    /**
     * Create a database table for a new collection
     * @param collectionListInfo Collection info
     * @param coinList List of coin slots
     * @param displayOrder Display order of the collection
     */
    public void createNewTable(Activity activity, CollectionListInfo collectionListInfo,
                        ArrayList<CoinSlot> coinList, int displayOrder) {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(activity);
        dbAdapter.open();
        dbAdapter.createAndPopulateNewTable(collectionListInfo, displayOrder, coinList);
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
                dbAdapter.dropCollectionTable(resultCursor.getString(resultCursor.getColumnIndex(COL_NAME)));
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
            initialValues.put(COL_COIN_IDENTIFIER, (String) coinInfo[0]);
            initialValues.put(COL_COIN_MINT, (String) coinInfo[1]);
            initialValues.put(COL_IN_COLLECTION, (int) coinInfo[2]);
            db.insert("[" + collectionName + "]", null, initialValues);
        }

        ContentValues values = new ContentValues();
        values.put(COL_NAME, collectionName);
        values.put(COL_COIN_TYPE, coinType);
        values.put(COL_TOTAL, coinList.size());
        db.insert(TBL_COLLECTION_INFO, null, values);
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
                    ArrayList<CoinSlot> dbCoinList = activity.mDbAdapter.getAllIdentifiers(collectionName);
                    assertNotNull(dbCoinList);
                    activity.mDbAdapter.close();

                    // Make sure coin lists match
                    assertEquals(newCoinList.size(), dbCoinList.size());
                    for(int i = 0; i < newCoinList.size(); i++){
                        assertEquals(newCoinList.get(i).getIdentifier(), dbCoinList.get(i).getIdentifier());
                        assertEquals(newCoinList.get(i).getMint(), dbCoinList.get(i).getMint());
                    }

                    // Make sure total matches
                    activity.mDbAdapter.open();
                    ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
                    activity.mDbAdapter.getAllTables(collectionListEntries);
                    assertNotNull(collectionListEntries);
                    boolean foundTable = false;
                    for (CollectionListInfo collectionListInfo : collectionListEntries) {
                        if (collectionName.equals(collectionListInfo.getName())) {
                            foundTable = true;
                            assertEquals(newCoinList.size(), collectionListInfo.getMax());
                            break;
                        }
                    }
                    activity.mDbAdapter.close();
                    assertTrue(foundTable);
                }
            });
        }
    }

    /**
     * Checks that the collection can be recreated based solely on the coin data
     * @param coinList coin list to analyze
     * @param coinClass type of coin
     */
    void checkCreationParamsFromCoinList(ArrayList<CoinSlot> coinList, CollectionInfo coinClass) {
        // Note: Skips configurations resulting in 0 coins, as there's no way to recreate these.
        //       In this case, editing the collection will reset back to the original start/year
        if (coinList.size() != 0) {
            CollectionListInfo collectionListInfo = getCollectionListInfo("X", coinClass, coinList);
            collectionListInfo.setCreationParametersFromCoinData(coinList);
            HashMap<String, Object> checkParameters = CoinPageCreator.getParametersFromCollectionListInfo(collectionListInfo);
            ArrayList<CoinSlot> coinListFromDerivedParams = new ArrayList<>();
            coinClass.populateCollectionLists(checkParameters, coinListFromDerivedParams);
            TestCase.assertEquals(coinList, coinListFromDerivedParams);
        }
    }

    /**
     * Compare two CollectionListInfo classes to ensure they're the same
     * @param base CollectionListInfo
     * @param check CollectionListInfo
     */
    void compareCollectionListInfos(CollectionListInfo base, CollectionListInfo check) {
        assertTrue(SharedTest.compareCollectionListInfos(base, check));
    }
}
