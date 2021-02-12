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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;

import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.MainActivity;
import com.spencerpages.collections.NativeAmericanDollars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import static com.coincollection.MainActivity.EXPORT_COLLECTION_LIST_FILE_EXT;
import static com.coincollection.MainActivity.EXPORT_COLLECTION_LIST_FILE_NAME;
import static com.coincollection.MainActivity.EXPORT_DB_VERSION_FILE;
import static com.coincollection.MainActivity.NUMBER_OF_COLLECTION_LIST_SPACERS;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class ExportImportTests extends BaseTestCase {

    /**
     * Test exporting one of each collection type
     */
    @Test
    public void test_exportOneOfEachCollection() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    // Set up collections
                    //assertTrue(waitForMainActivitySetup(activity));
                    assertTrue(setEnabledPermissions(activity));
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

                    // Run import and check results
                    activity.handleImportCollectionsPart1();
                    ((AlertDialog) ShadowAlertDialog.getLatestDialog())
                            .getButton(AlertDialog.BUTTON_POSITIVE)
                            .performClick();
                    ArrayList<String> afterCollectionNames = getCollectionNames(activity);
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
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    File v1DbDir = new File("src/test/data/v1-coin-collection-files");
                    assertTrue(setEnabledPermissions(activity));
                    // Disable the async task since it isn't reliable in the unit test
                    // This test will instead call both Part1 and Part2 (normally called by
                    // the async task).
                    MainActivity activitySpy = spy(activity);
                    when(activitySpy.getExportFolderName()).thenReturn(v1DbDir.getAbsolutePath());

                    // Run import and check results
                    activitySpy.handleImportCollectionsPart1();
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
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    // Set up collections
                    assertTrue(setEnabledPermissions(activity));
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

                    // Run import and check results
                    activity.handleImportCollectionsPart1();
                    ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                    assertEquals(afterCollectionNames.size(), namesToTest.size());
                    assertEquals(beforeCollectionNames, afterCollectionNames);
                }
            });
        }
    }

    /**
     * Test that running the collection list info export -> import work
     */
    @Test
    public void test_exportImportMethods() {
        for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS){
            DatabaseAdapter fakeDbAdapter = mock(DatabaseAdapter.class);
            when(fakeDbAdapter.fetchTableDisplay(anyString())).thenReturn(info.getDisplayType());
            String[] export = info.getCsvExportProperties(fakeDbAdapter);
            CollectionListInfo checkInfo = new CollectionListInfo(export);
            compareCollectionListInfos(info, checkInfo);
        }
    }
}
