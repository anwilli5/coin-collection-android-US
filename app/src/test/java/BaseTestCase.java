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

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.coincollection.CollectionListInfo.COL_COIN_TYPE;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_TOTAL;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.coincollection.CollectionPage.ADVANCED_DISPLAY;
import static com.coincollection.CollectionPage.SIMPLE_DISPLAY;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.BaseActivity;
import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.MainApplication;
import com.spencerpages.R;
import com.spencerpages.SharedTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

public class BaseTestCase {

    public final static int VERSION_1_YEAR = 2013;
    public static final Random random = new Random(98320498);

    private ArrayList<String> mPreviousRandCollectionNames;

    /**
     * Class containing all collection details
     */
    static class FullCollection {
        final CollectionListInfo mCollectionListInfo;
        final ArrayList<CoinSlot> mCoinList;
        final int mDisplayOrder;

        FullCollection(CollectionListInfo collectionListInfo, ArrayList<CoinSlot> coinList,
                       int displayOrder) {
            mCollectionListInfo = collectionListInfo;
            mCoinList = coinList;
            mDisplayOrder = displayOrder;
        }
    }

    /**
     * Enables VM policy checking (override to disable)
     * @return true if the tests support VM policy checking, otherwise false
     */
    protected boolean enableVmPolicyChecking() {
        return true;
    }

    /**
     * Setup run before every test
     */
    @Before
    public void testSetup() {
        // This list keeps tracked of previously used random collection names, to prevent duplicates
        mPreviousRandCollectionNames = new ArrayList<>();
    }

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
                SIMPLE_DISPLAY,
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
            ParcelableHashMap parameters = new ParcelableHashMap();
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
            ParcelableHashMap parameters = new ParcelableHashMap();
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
        ParcelableHashMap parameters = new ParcelableHashMap();
        validateUpdatedDb(collectionInfo, collectionName, parameters);
    }

    /**
     * Validate updated database
     * @param collectionInfo collection info
     * @param collectionName collection name
     * @param parameters setup parameters
     */
    public void validateUpdatedDb(final CollectionInfo collectionInfo, final String collectionName,
                                  final ParcelableHashMap parameters){
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {

                // Create a new database from scratch
                collectionInfo.getCreationParameters(parameters);
                ArrayList<CoinSlot> newCoinList = new ArrayList<>();
                collectionInfo.populateCollectionLists(parameters, newCoinList);

                // Get coins from the updated database
                ArrayList<CoinSlot> dbCoinList = activity.mDbAdapter.getAllIdentifiers(collectionName);
                assertNotNull(dbCoinList);

                // Make sure coin lists match
                assertEquals(newCoinList.size(), dbCoinList.size());
                for(int i = 0; i < newCoinList.size(); i++){
                    assertEquals(newCoinList.get(i).getIdentifier(), dbCoinList.get(i).getIdentifier());
                    assertEquals(newCoinList.get(i).getMint(), dbCoinList.get(i).getMint());
                }

                // Make sure total matches
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
                assertTrue(foundTable);
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
            ParcelableHashMap checkParameters = CoinPageCreator.getParametersFromCollectionListInfo(collectionListInfo);
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

    /**
     * Compare the collection against what's stored in the database
     * @param activity test activity
     * @param collectionListInfo CollectionListInfo to compare
     * @param coinList CoinSlot list to compare
     */
    void compareCollectionWithDb(BaseActivity activity, CollectionListInfo collectionListInfo,
                                 ArrayList<CoinSlot> coinList, int displayOrder) {
        String tableName = collectionListInfo.getName();
        // Make sure the collection list info is correct in the database
        ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
        activity.mDbAdapter.getAllTables(collectionListEntries);
        assertEquals(1, collectionListEntries.size());
        compareCollectionListInfos(collectionListEntries.get(0), collectionListInfo);

        // Test table display database methods
        int originalDisplayType = collectionListInfo.getDisplayType();
        assertEquals(activity.mDbAdapter.fetchTableDisplay(tableName), originalDisplayType);
        int toggledDisplayType = (originalDisplayType == SIMPLE_DISPLAY) ? ADVANCED_DISPLAY : SIMPLE_DISPLAY;
        activity.mDbAdapter.updateTableDisplay(tableName, toggledDisplayType);
        assertEquals(activity.mDbAdapter.fetchTableDisplay(tableName), toggledDisplayType);
        activity.mDbAdapter.updateTableDisplay(tableName, originalDisplayType);
        assertEquals(activity.mDbAdapter.fetchTableDisplay(tableName), originalDisplayType);

        // Test display order database methods
        activity.mDbAdapter.updateDisplayOrder(tableName, displayOrder);
        assertEquals(displayOrder + 1, activity.mDbAdapter.getNextDisplayOrder());

        // Test updating collection name
        assertEquals(R.string.collection_name_exists, activity.mDbAdapter.checkCollectionName(tableName));
        activity.mDbAdapter.updateCollectionName(tableName, "New Name");
        assertEquals(R.string.collection_name_exists, activity.mDbAdapter.checkCollectionName("New Name"));
        assertEquals(-1, activity.mDbAdapter.checkCollectionName(tableName));
        activity.mDbAdapter.updateCollectionName("New Name", tableName);
        assertEquals(R.string.collection_name_exists, activity.mDbAdapter.checkCollectionName(tableName));

        // Make sure the coin list is correct in the database
        if (coinList != null) {
            boolean populateAdvInfo = (collectionListInfo.getDisplayType() == ADVANCED_DISPLAY);
            ArrayList<CoinSlot> checkCoinList = activity.mDbAdapter.getCoinList(tableName, populateAdvInfo);
            assertEquals(coinList, checkCoinList);

            // Test coin slot database methods
            for (CoinSlot coinSlot : coinList) {
                assertEquals(activity.mDbAdapter.fetchIsInCollection(tableName, coinSlot),
                        (coinSlot.isInCollection() ? 1 : 0));
                activity.mDbAdapter.toggleInCollection(tableName, coinSlot);
                assertEquals(activity.mDbAdapter.fetchIsInCollection(tableName, coinSlot),
                        (coinSlot.isInCollection() ? 0 : 1));

            }
        }
    }

    /**
     * Generate test scenarios for each collection
     * @param coinClass collection type
     * @param numScenarios Number of scenarios to generate
     * @return scenario list containing [start, end] dates (if dates are used)
     */
    ArrayList<Integer[]> getTestScenarios(CollectionInfo coinClass, int numScenarios) {
        ArrayList<Integer[]> scenarioList = new ArrayList<>();
        if (CollectionListInfo.doesCollectionTypeUseDates(coinClass.getCoinType())) {
            // Add some coin date scenarios to test
            int startYear = coinClass.getStartYear();
            int endYear = coinClass.getStopYear();
            // Add these interesting scenarios for all collections
            scenarioList.add(new Integer[]{startYear, endYear});
            scenarioList.add(new Integer[]{startYear, startYear});
            scenarioList.add(new Integer[]{endYear, endYear});
            scenarioList.add(new Integer[]{startYear, startYear + 1});
            scenarioList.add(new Integer[]{endYear - 1, endYear});
            // Choose some random date ranges
            for (int i = 0; i < numScenarios; i++) {
                int randStartYear = startYear + DatabaseAccessTests.random.nextInt(endYear - startYear + 1);
                int randEndYear = randStartYear + random.nextInt(endYear - randStartYear + 1);
                scenarioList.add(new Integer[]{randStartYear, randEndYear});
            }
        } else {
            // Add a fixed number of scenarios if dates aren't used
            for (int i = 0; i < numScenarios; i++) {
                scenarioList.add(new Integer[]{0, 0});
            }
        }
        return scenarioList;
    }

    /**
     * Get collections with random information filled in
     * @param activity activity needed to access some resources
     * @param coinType type of collection to make
     * @param numScenarios number of collections to make
     * @return list of generated collections
     */
    ArrayList<FullCollection> getRandomTestScenarios(BaseActivity activity, CollectionInfo coinType, int numScenarios) {
        String[] grades = activity.getResources().getStringArray(R.array.coin_grades);
        String[] quantities = activity.getResources().getStringArray(R.array.coin_quantities);
        ArrayList<Integer[]> scenarioList = getTestScenarios(coinType, numScenarios);
        ArrayList<FullCollection> testCollections = new ArrayList<>();

        for (int i = 0; i < scenarioList.size(); i++) {
            int startDate = scenarioList.get(i)[0];
            int endDate = scenarioList.get(i)[1];
            int displayOrder = random.nextInt(100000);

            // Select a unique name
            String collectionName;
            do {
                collectionName = getRandCollectionName();
            } while (mPreviousRandCollectionNames.contains(collectionName));
            mPreviousRandCollectionNames.add(collectionName);

            // Create CollectionListInfo
            int displayType = ((random.nextInt() % 2) == 1 ? SIMPLE_DISPLAY : ADVANCED_DISPLAY);
            CollectionListInfo collectionListInfo = new CollectionListInfo(
                    collectionName,
                    0,
                    0,
                    MainApplication.getIndexFromCollectionNameStr(coinType.getCoinType()),
                    displayType,
                    startDate,
                    endDate,
                    random.nextInt() & CollectionListInfo.ALL_MINT_MASK,
                    random.nextInt() & CollectionListInfo.ALL_CHECKBOXES_MASK);

            // Populate coin list
            ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(collectionListInfo);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinType.populateCollectionLists(parameters, coinList);
            int numCollected = 0;
            for (CoinSlot coinSlot : coinList) {
                boolean collected = (random.nextInt() % 2) == 1;
                coinSlot.setInCollection(collected);
                coinSlot.setAdvancedGrades(random.nextInt() % grades.length);
                coinSlot.setAdvancedQuantities(random.nextInt() % quantities.length);
                coinSlot.setAdvancedNotes(Integer.toString(random.nextInt()));
                numCollected += collected ? 1 : 0;
            }
            collectionListInfo.setMax(coinList.size());
            collectionListInfo.setCollected(numCollected);

            testCollections.add(new FullCollection(collectionListInfo, coinList, displayOrder));
        }
        return testCollections;
    }

    /**
     * Generate a random collection name
     * @return string name
     */
    String getRandCollectionName(){
        String chars = "ABCDEFGHIJHLMNOPqrstuvwxyz0983746~!@#$%^&*() \\/<>?:\"{}'";
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < 1 + random.nextInt(50); i++) {
            output.append(chars.charAt(random.nextInt(chars.length())));
        }
        return output.toString();
    }

    /**
     * Open an output stream from file
     * @param file file to open
     * @return output stream
     */
    OutputStream openOutputStream(File file) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            fail();
        }
        return outputStream;
    }

    /**
     * Open an input stream from file
     * @param file file to open
     * @return input stream
     */
    InputStream openInputStream(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            fail();
        }
        return inputStream;
    }

    /**
     * Close the output stream
     * @param stream output stream
     */
    void closeStream(OutputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Close the input stream
     * @param stream input stream
     */
    void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            fail();
        }
    }
}
