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
import android.app.AlertDialog;
import android.app.Application;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;

import com.coincollection.AsyncProgressInterface;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.MainActivity;
import com.spencerpages.collections.NativeAmericanDollars;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import static com.coincollection.DatabaseAdapter.COL_NAME;
import static com.coincollection.MainActivity.EXPORT_COLLECTION_LIST_FILE_EXT;
import static com.coincollection.MainActivity.EXPORT_COLLECTION_LIST_FILE_NAME;
import static com.coincollection.MainActivity.EXPORT_DB_VERSION_FILE;
import static com.coincollection.MainActivity.NUMBER_OF_COLLECTION_LIST_SPACERS;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class ExportImportTests extends TestCase {

    /**
     * Enable storage read/write permission
     */
    public void setEnabledPermissions() {
        Application application = ApplicationProvider.getApplicationContext();
        ShadowApplication app = Shadows.shadowOf(application);
        app.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
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
    void createNewTable(Activity activity, String tableName, String coinType,
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
    void deleteAllCollections(Activity activity) {
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
    ArrayList<String> getCollectionNames(Activity activity) {
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
     * Test exporting one of each collection type
     */
    @Test
    public void test_exportOneOfEachCollection() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    // Set up collections
                    setEnabledPermissions();
                    assertTrue(setupOneOfEachCollectionTypes(activity));
                    activity.updateCollectionListFromDatabase();
                    ArrayList<String> beforeCollectionNames = getCollectionNames(activity);

                    // Export and check output
                    activity.handleExportCollectionsPart1();
                    File exportDir = new File(activity.getExportFolderName());
                    assertTrue(exportDir.exists());
                    File dbVersionFile = new File(activity.getExportFolderName(), EXPORT_DB_VERSION_FILE);
                    File collectionListFile = new File(activity.getExportFolderName(), EXPORT_COLLECTION_LIST_FILE_NAME + EXPORT_COLLECTION_LIST_FILE_EXT);
                    assertTrue(dbVersionFile.exists());
                    assertTrue(collectionListFile.exists());
                    for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
                        assertNotNull(collectionInfo);
                        File collectionFile;
                        if(collectionInfo instanceof NativeAmericanDollars){
                            collectionFile = new File(activity.getExportFolderName(), "Sacagawea_SL_Native American Dollars.csv");
                        } else {
                            collectionFile = new File(activity.getExportFolderName(), collectionInfo.getCoinType() + ".csv");
                        }
                        assertTrue(collectionFile.exists());
                    }

                    // Delete all collections
                    deleteAllCollections(activity);
                    activity.updateCollectionListFromDatabase();
                    assertEquals(getCollectionNames(activity).size(), 0);
                    assertEquals(activity.mNumberOfCollections, 0);
                    assertEquals(activity.mCollectionListEntries.size(), NUMBER_OF_COLLECTION_LIST_SPACERS);

                    // Disable the async task since it isn't reliable in the unit test
                    // This test will instead call both Part1 and Part2 (normally called by
                    // the async task).
                    MainActivity activitySpy = spy(activity);
                    when(activitySpy.kickOffAsyncProgressTask(any(AsyncProgressInterface.class), any(int.class))).thenReturn(null);

                    // Run import and check results
                    activitySpy.handleImportCollectionsPart1();
                    ((AlertDialog) ShadowAlertDialog.getLatestDialog())
                            .getButton(AlertDialog.BUTTON_POSITIVE)
                            .performClick();
                    activitySpy.handleImportCollectionsPart2();
                    ArrayList<String> afterCollectionNames = getCollectionNames(activitySpy);
                    assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                    assertEquals(beforeCollectionNames, afterCollectionNames);
                }
            });
        }
    }

    /**
     * Test importing a v1 database collection
     */
    @Test
    public void test_importV1Collection() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    File v1DbDir = new File("src/test/data/v1-coin-collection-files");
                    setEnabledPermissions();
                    // Disable the async task since it isn't reliable in the unit test
                    // This test will instead call both Part1 and Part2 (normally called by
                    // the async task).
                    MainActivity activitySpy = spy(activity);
                    when(activitySpy.kickOffAsyncProgressTask(any(AsyncProgressInterface.class), any(int.class))).thenReturn(null);
                    when(activitySpy.getExportFolderName()).thenReturn(v1DbDir.getAbsolutePath());

                    // Run import and check results
                    activitySpy.handleImportCollectionsPart1();
                    ((AlertDialog) ShadowAlertDialog.getLatestDialog())
                            .getButton(AlertDialog.BUTTON_POSITIVE)
                            .performClick();
                    activitySpy.handleImportCollectionsPart2();
                    ArrayList<String> afterCollectionNames = getCollectionNames(activitySpy);
                    assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                    //assertEquals(beforeCollectionNames, afterCollectionNames);
                }
            });
        }
    }

    /**
     * Test exporting interesting collection names
     */
    @Test
    public void test_exportCollectionNames() {
        final ArrayList<String> namesToTest = new ArrayList<>(Arrays.asList(
                "Name with Spaces",
                "Name with/backslash",
                "a",
                "0",
                ".",
                "..",
                "fake_SL_slash",
                "!@#$%^&*()",
                "special chars -=_+{",
                "}{<.>?/",
                "893174289347",
                "\\n",
                "$name",
                "collection.csv"
        ));
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    // Set up collections
                    setEnabledPermissions();
                    assertTrue(setupCollectionsWithNames(activity, namesToTest));
                    activity.updateCollectionListFromDatabase();
                    ArrayList<String> beforeCollectionNames = getCollectionNames(activity);

                    // Export and check output
                    activity.handleExportCollectionsPart1();
                    File exportDir = new File(activity.getExportFolderName());
                    assertTrue(exportDir.exists());
                    File dbVersionFile = new File(activity.getExportFolderName(), EXPORT_DB_VERSION_FILE);
                    File collectionListFile = new File(activity.getExportFolderName(), EXPORT_COLLECTION_LIST_FILE_NAME + EXPORT_COLLECTION_LIST_FILE_EXT);
                    assertTrue(dbVersionFile.exists());
                    assertTrue(collectionListFile.exists());
                    for (String inputCollectionName : namesToTest) {
                        File collectionFile;
                        String comparisonName = inputCollectionName.replace("/", "_SL_");
                        collectionFile = new File(activity.getExportFolderName(), comparisonName + ".csv");
                        assertTrue(collectionFile.exists());
                    }

                    // Delete all collections
                    deleteAllCollections(activity);
                    activity.updateCollectionListFromDatabase();
                    assertEquals(getCollectionNames(activity).size(), 0);
                    assertEquals(activity.mNumberOfCollections, 0);
                    assertEquals(activity.mCollectionListEntries.size(), NUMBER_OF_COLLECTION_LIST_SPACERS);

                    // Disable the async task since it isn't reliable in the unit test
                    // This test will instead call both Part1 and Part2 (normally called by
                    // the async task).
                    MainActivity activitySpy = spy(activity);
                    when(activitySpy.kickOffAsyncProgressTask(any(AsyncProgressInterface.class), any(int.class))).thenReturn(null);

                    // Run import and check results
                    activitySpy.handleImportCollectionsPart1();
                    ((AlertDialog) ShadowAlertDialog.getLatestDialog())
                            .getButton(AlertDialog.BUTTON_POSITIVE)
                            .performClick();
                    activitySpy.handleImportCollectionsPart2();
                    ArrayList<String> afterCollectionNames = getCollectionNames(activitySpy);
                    assertEquals(afterCollectionNames.size(), namesToTest.size());
                    assertEquals(beforeCollectionNames, afterCollectionNames);
                }
            });
        }
    }
}
