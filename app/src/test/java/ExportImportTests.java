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

import static com.coincollection.ExportImportHelper.JSON_CHARSET;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_DB_VERSION_FILE;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_FOLDER_NAME;
import static com.coincollection.MainActivity.NUMBER_OF_COLLECTION_LIST_SPACERS;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.ExportImportHelper;
import com.coincollection.MainActivity;
import com.spencerpages.R;
import com.spencerpages.collections.NativeAmericanDollars;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class ExportImportTests extends BaseTestCase {

    @Rule
    public final TemporaryFolder mTempFolder = new TemporaryFolder();

    /**
     * Get a temporary file with a given name
     * @param filename file name
     * @return file
     */
    File getTempFile(String filename) {
        try {
            return mTempFolder.newFile(filename);
        } catch (IOException e) {
            fail();
        }
        return null;
    }

    /**
     * Test exporting one of each collection type using legacy CSV format
     */
    @Test
    public void test_legacyCsvExportOneOfEachCollection() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                // Set up collections
                //assertTrue(waitForMainActivitySetup(activity));
                assertTrue(setEnabledPermissions(activity));
                assertTrue(setupOneOfEachCollectionTypes(activity));
                activity.updateCollectionListFromDatabase();
                ArrayList<String> beforeCollectionNames = getCollectionNames(activity);

                // Export and check output
                File exportDir = new File(activity.getLegacyExportFolderName());
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                        helper.exportCollectionsToLegacyCSV(activity.getLegacyExportFolderName()));
                assertTrue(exportDir.exists());
                File dbVersionFile = new File(activity.getLegacyExportFolderName(), LEGACY_EXPORT_DB_VERSION_FILE);
                File collectionListFile = new File(activity.getLegacyExportFolderName(), LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME + LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT);
                assertTrue(dbVersionFile.exists());
                assertTrue(collectionListFile.exists());
                for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
                    assertNotNull(collectionInfo);
                    File collectionFile;
                    if(collectionInfo instanceof NativeAmericanDollars){
                        collectionFile = new File(activity.getLegacyExportFolderName(), "Sacagawea_SL_Native American Dollars.csv");
                    } else {
                        collectionFile = new File(activity.getLegacyExportFolderName(), collectionInfo.getCoinType() + ".csv");
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
                assertEquals("", helper.importCollectionsFromLegacyCSV(activity.getLegacyExportFolderName()));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                assertEquals(beforeCollectionNames, afterCollectionNames);
            });
        }
    }

    /**
     * Test exporting one of each collection type using JSON file format
     */
    @Test
    public void test_jsonExportOneOfEachCollection() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                // Set up collections
                //assertTrue(waitForMainActivitySetup(activity));
                assertTrue(setEnabledPermissions(activity));
                assertTrue(setupOneOfEachCollectionTypes(activity));
                activity.updateCollectionListFromDatabase();
                ArrayList<String> beforeCollectionNames = getCollectionNames(activity);

                // Export and check output
                File exportFile = getTempFile("json-export.json");
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                OutputStream outputStream = openOutputStream(exportFile);
                assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                        helper.exportCollectionsToJson(outputStream, LEGACY_EXPORT_FOLDER_NAME));
                assertTrue(exportFile.exists());
                closeStream(outputStream);

                // Delete all collections
                deleteAllCollections(activity);
                activity.updateCollectionListFromDatabase();
                assertEquals(getCollectionNames(activity).size(), 0);
                assertEquals(activity.mNumberOfCollections, 0);
                assertEquals(activity.mCollectionListEntries.size(), NUMBER_OF_COLLECTION_LIST_SPACERS);

                // Run import and check results
                InputStream inputStream = openInputStream(exportFile);
                assertEquals("", helper.importCollectionsFromJson(inputStream));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                assertEquals(beforeCollectionNames, afterCollectionNames);
                closeStream(inputStream);
            });
        }
    }

    /**
     * Test exporting one of each collection type using single-file CSV format
     */
    @Test
    public void test_csvExportOneOfEachCollection() {
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
                    File exportFile = getTempFile("csv-export.csv");
                    ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                    OutputStream outputStream = openOutputStream(exportFile);
                    assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                            helper.exportCollectionsToSingleCSV(outputStream, LEGACY_EXPORT_FOLDER_NAME));
                    assertTrue(exportFile.exists());
                    closeStream(outputStream);

                    // Delete all collections
                    deleteAllCollections(activity);
                    activity.updateCollectionListFromDatabase();
                    assertEquals(getCollectionNames(activity).size(), 0);
                    assertEquals(activity.mNumberOfCollections, 0);
                    assertEquals(activity.mCollectionListEntries.size(), NUMBER_OF_COLLECTION_LIST_SPACERS);

                    // Run import and check results
                    InputStream inputStream = openInputStream(exportFile);
                    assertEquals("", helper.importCollectionsFromSingleCSV(inputStream));
                    ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                    assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                    assertEquals(beforeCollectionNames, afterCollectionNames);
                    closeStream(inputStream);
                }
            });
        }
    }

    /**
     * Test importing a v1 database collection
     */
    @Test
    public void test_csvImportV1Collection() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                File v1DbDir = new File("src/test/data/v1-coin-collection-files");
                assertTrue(setEnabledPermissions(activity));

                // Run import and check results
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                assertEquals("", helper.importCollectionsFromLegacyCSV(v1DbDir.getAbsolutePath()));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                //assertEquals(beforeCollectionNames, afterCollectionNames);
            });
        }
    }

    /**
     * Test exporting interesting collection names
     */
    @Test
    public void test_csvExportCollectionNames() {
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
            scenario.onActivity(activity -> {
                // Set up collections
                assertTrue(setEnabledPermissions(activity));
                assertTrue(setupCollectionsWithNames(activity, namesToTest));
                activity.updateCollectionListFromDatabase();
                ArrayList<String> beforeCollectionNames = getCollectionNames(activity);

                // Export and check output
                File exportDir = new File(activity.getLegacyExportFolderName());
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                        helper.exportCollectionsToLegacyCSV(activity.getLegacyExportFolderName()));
                assertTrue(exportDir.exists());
                File dbVersionFile = new File(activity.getLegacyExportFolderName(), LEGACY_EXPORT_DB_VERSION_FILE);
                File collectionListFile = new File(activity.getLegacyExportFolderName(), LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME + LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT);
                assertTrue(dbVersionFile.exists());
                assertTrue(collectionListFile.exists());
                for (String inputCollectionName : namesToTest) {
                    File collectionFile;
                    String comparisonName = inputCollectionName.replace("/", "_SL_");
                    collectionFile = new File(activity.getLegacyExportFolderName(), comparisonName + ".csv");
                    assertTrue(collectionFile.exists());
                }

                // Delete all collections
                deleteAllCollections(activity);
                activity.updateCollectionListFromDatabase();
                assertEquals(getCollectionNames(activity).size(), 0);
                assertEquals(activity.mNumberOfCollections, 0);
                assertEquals(activity.mCollectionListEntries.size(), NUMBER_OF_COLLECTION_LIST_SPACERS);

                // Run import and check results
                assertEquals("", helper.importCollectionsFromLegacyCSV(activity.getLegacyExportFolderName()));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                assertEquals(afterCollectionNames.size(), namesToTest.size());
                assertEquals(beforeCollectionNames, afterCollectionNames);
            });
        }
    }

    /**
     * Test that running the collection list info export -> import work
     */
    @Test
    public void test_csvExportImportMethods() {
        for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS){
            DatabaseAdapter fakeDbAdapter = mock(DatabaseAdapter.class);
            when(fakeDbAdapter.fetchTableDisplay(anyString())).thenReturn(info.getDisplayType());
            String[] export = info.getCsvExportProperties(fakeDbAdapter);
            CollectionListInfo checkInfo = new CollectionListInfo(export);
            compareCollectionListInfos(info, checkInfo);
        }
    }

    /**
     * Test that running the collection list info export -> import work
     */
    @Test
    public void test_jsonExportImportMethods() {
        int testNum = 0;
        for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS){
            DatabaseAdapter fakeDbAdapter = mock(DatabaseAdapter.class);
            when(fakeDbAdapter.fetchTableDisplay(anyString())).thenReturn(info.getDisplayType());
            File exportFile = getTempFile("test-file" + testNum + ".json");
            OutputStream outputStream = openOutputStream(exportFile);
            try {
                // Write the JSON file
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, JSON_CHARSET));
                info.writeToJson(writer, fakeDbAdapter, new ArrayList<>());
                writer.close();
                closeStream(outputStream);

                // Read the JSON file
                InputStream inputStream = openInputStream(exportFile);
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, JSON_CHARSET));
                CollectionListInfo checkInfo = new CollectionListInfo(reader, new ArrayList<>());
                reader.close();
                closeStream(inputStream);

                // Compare the results
                compareCollectionListInfos(info, checkInfo);
                testNum++;

            } catch (Exception ignored) {
                fail();
            }
        }
    }
}
