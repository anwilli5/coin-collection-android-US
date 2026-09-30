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

package com.spencerpages;

import static com.coincollection.ExportImportHelper.JSON_CHARSET;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_DB_VERSION_FILE;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_FOLDER_NAME;
import static com.coincollection.CollectionPage.SIMPLE_DISPLAY;
import static com.coincollection.MainActivity.NUMBER_OF_COLLECTION_LIST_SPACERS;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.ExportImportHelper;
import com.coincollection.ImportFormatException;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.BuffaloNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.NativeAmericanDollars;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

@RunWith(RobolectricTestRunner.class)
public class ExportImportTests extends BaseTestCase {

    @Rule
    public final TemporaryFolder mTempFolder = new TemporaryFolder();

    /**
     * Get a temporary file with a given name
     *
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
     * Find a coin in a collection, failing the test if it isn't there
     *
     * @param activity       activity whose database to search
     * @param collectionName collection to search
     * @param identifier     coin identifier
     * @param mint           coin mint, compared ignoring surrounding whitespace
     * @return the coin
     */
    private static CoinSlot findCoin(MainActivity activity, String collectionName,
                                     String identifier, String mint) {
        for (CoinSlot coin : activity.mDbAdapter.getCoinList(collectionName, true)) {
            if (coin.getIdentifier().equals(identifier) && coin.getMint().trim().equals(mint)) {
                return coin;
            }
        }
        throw new AssertionError("No coin " + identifier + " " + mint + " in " + collectionName);
    }

    /**
     * Test exporting one of each collection type using legacy CSV format
     */
    @Test
    public void test_legacyCsvExportOneOfEachCollection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                // Set up collections
                assertTrue(setEnabledPermissions(activity));
                assertTrue(setupOneOfEachCollectionTypes(activity));
                activity.updateCollectionListFromDatabase();
                ArrayList<String> beforeCollectionNames = getCollectionNames(activity);
                ArrayList<ArrayList<CoinSlot>> beforeCoinLists = getCoinSlotListsFromCollectionNames(activity.mDbAdapter, beforeCollectionNames);

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
                    File collectionFile = getCollectionFile(activity, collectionInfo);
                    assertTrue(collectionFile.exists());
                }

                // Delete all collections
                deleteAllCollections(activity);
                activity.updateCollectionListFromDatabase();
                assertEquals(0, getCollectionNames(activity).size());
                assertEquals(0, activity.mNumberOfCollections);
                assertEquals(NUMBER_OF_COLLECTION_LIST_SPACERS, activity.mCollectionListEntries.size());

                // Run import and check results
                assertEquals("", helper.importCollectionsFromLegacyCSV(activity.getLegacyExportFolderName()));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                ArrayList<ArrayList<CoinSlot>> afterCoinLists = getCoinSlotListsFromCollectionNames(activity.mDbAdapter, afterCollectionNames);
                assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                assertEquals(beforeCollectionNames, afterCollectionNames);
                compareListOfCoinSlotLists(beforeCoinLists, afterCoinLists, false);
            });
        }
    }

    /**
     * Get the collection file for a given collection info object
     *
     * @param activity        MainActivity instance
     * @param collectionInfo  CollectionInfo object
     * @return File for the collection
     */
    @NonNull
    private static File getCollectionFile(MainActivity activity, CollectionInfo collectionInfo) {
        File collectionFile;
        if (collectionInfo instanceof NativeAmericanDollars) {
            collectionFile = new File(activity.getLegacyExportFolderName(), "Sacagawea_SL_Native American Dollars.csv");
        } else if (collectionInfo instanceof AmericanInnovationDollars) {
            collectionFile = new File(activity.getLegacyExportFolderName(), "American Innovation Dollars w_SL_ Proofs.csv");
        } else{
            collectionFile = new File(activity.getLegacyExportFolderName(), collectionInfo.getCoinType() + ".csv");
        }
        return collectionFile;
    }

    /**
     * Test exporting one of each collection type using JSON file format
     */
    @Test
    public void test_jsonExportOneOfEachCollection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                // Set up collections
                assertTrue(setEnabledPermissions(activity));
                assertTrue(setupOneOfEachCollectionTypes(activity));
                activity.updateCollectionListFromDatabase();
                ArrayList<String> beforeCollectionNames = getCollectionNames(activity);
                ArrayList<ArrayList<CoinSlot>> beforeCoinLists = getCoinSlotListsFromCollectionNames(activity.mDbAdapter, beforeCollectionNames);

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
                assertEquals(0, getCollectionNames(activity).size());
                assertEquals(0, activity.mNumberOfCollections);
                assertEquals(NUMBER_OF_COLLECTION_LIST_SPACERS, activity.mCollectionListEntries.size());

                // Run import and check results
                InputStream inputStream = openInputStream(exportFile);
                assertEquals("", helper.importCollectionsFromJson(inputStream));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                ArrayList<ArrayList<CoinSlot>> afterCoinLists = getCoinSlotListsFromCollectionNames(activity.mDbAdapter, afterCollectionNames);
                assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                assertEquals(beforeCollectionNames, afterCollectionNames);
                compareListOfCoinSlotLists(beforeCoinLists, afterCoinLists, true);
                closeStream(inputStream);
            });
        }
    }

    /**
     * Test exporting one of each collection type using single-file CSV format
     */
    @Test
    public void test_csvExportOneOfEachCollection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                // Set up collections
                assertTrue(setEnabledPermissions(activity));
                assertTrue(setupOneOfEachCollectionTypes(activity));
                activity.updateCollectionListFromDatabase();
                ArrayList<String> beforeCollectionNames = getCollectionNames(activity);
                ArrayList<ArrayList<CoinSlot>> beforeCoinLists = getCoinSlotListsFromCollectionNames(activity.mDbAdapter, beforeCollectionNames);

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
                assertEquals(0, getCollectionNames(activity).size());
                assertEquals(0, activity.mNumberOfCollections);
                assertEquals(NUMBER_OF_COLLECTION_LIST_SPACERS, activity.mCollectionListEntries.size());

                // Run import and check results
                InputStream inputStream = openInputStream(exportFile);
                assertEquals("", helper.importCollectionsFromSingleCSV(inputStream));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                ArrayList<ArrayList<CoinSlot>> afterCoinLists = getCoinSlotListsFromCollectionNames(activity.mDbAdapter, afterCollectionNames);
                assertEquals(afterCollectionNames.size(), COLLECTION_TYPES.length);
                assertEquals(beforeCollectionNames, afterCollectionNames);
                compareListOfCoinSlotLists(beforeCoinLists, afterCoinLists, true);
                closeStream(inputStream);
            });
        }
    }

    /**
     * Strings that are valid user input but hard on the export formats: CSV delimiters,
     * quotes, line breaks, backslashes (the CSV reader has escaping turned off), surrounding
     * whitespace, non-BMP characters, and text that looks like an export section separator.
     *
     * <p>Carriage returns are left out on purpose: CSV import reads "\r\n" and "\r" as "\n".
     * Keeping them would leave a stray "\r" at the end of every row of a CSV that was re-saved
     * with Windows line endings, which would then fail to import.
     */
    private static final String[] HOSTILE_STRINGS = {
            "a,b\"c\"\nd",
            "\uD83D\uDE01, \"quoted\"",
            "\"",
            "\"\"",
            ",",
            "'; DROP TABLE collection_info; --",
            "C:\\new\\table \\n \\\"",
            "  padded  ",
            "tab\there",
            "trailing newline\n",
            "\nleading newline",
            "\u00D1and\u00FA caf\u00E9 \u2615 \uD834\uDD1E \uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67",
            "=1+2",
            ExportImportHelper.CSV_SEPARATOR,
            ExportImportHelper.JSON_COIN_LIST,
            new String(new char[500]).replace("\0", "x,\"y\"\n"),
    };

    /** Collection name with the characters a user can type (no [ or ], and none that
     * Windows forbids in the legacy export's file names) */
    private static final String HOSTILE_COLLECTION_NAME =
            "Grandpa's Wheat Cents, 1909\u20132025 \uD83D\uDE01";

    /**
     * Gets the default creation parameters for a collection type
     *
     * @param coinType collection type
     * @return the default parameters
     */
    private static ParcelableHashMap getDefaultParameters(String coinType) {
        ParcelableHashMap parameters = new ParcelableHashMap();
        COLLECTION_TYPES[MainApplication.getIndexFromCollectionNameStr(coinType)]
                .getCreationParameters(parameters);
        return parameters;
    }

    /**
     * Gets the coins a new collection of a type gets with the default parameters
     *
     * @param coinType collection type
     * @return the coins
     */
    private static ArrayList<CoinSlot> getDefaultCoins(String coinType) {
        ArrayList<CoinSlot> coinList = new ArrayList<>();
        COLLECTION_TYPES[MainApplication.getIndexFromCollectionNameStr(coinType)]
                .populateCollectionLists(getDefaultParameters(coinType), coinList);
        return coinList;
    }

    /**
     * Creates a collection in the database with the metadata the collection creator stores
     * for the type's default parameters
     *
     * @param activity     activity whose database to populate
     * @param name         collection name
     * @param coinType     collection type
     * @param coinList     coins to store
     * @param displayOrder display order
     */
    private void createCollection(MainActivity activity, String name, String coinType,
                                  ArrayList<CoinSlot> coinList, int displayOrder) {
        ParcelableHashMap parameters = getDefaultParameters(coinType);
        int collected = 0;
        for (CoinSlot coin : coinList) {
            collected += coin.isInCollection() ? 1 : 0;
        }
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        CollectionListInfo collectionListInfo = new CollectionListInfo(
                name,
                coinList.size(),
                collected,
                MainApplication.getIndexFromCollectionNameStr(coinType),
                SIMPLE_DISPLAY,
                (startYear != null) ? startYear : 0,
                (stopYear != null) ? stopYear : 0,
                Long.toString(CoinPageCreator.getMintMarkFlagsFromParameters(parameters)),
                Long.toString(CoinPageCreator.getCheckboxFlagsFromParameters(parameters)));
        createNewTable(activity, collectionListInfo, coinList, displayOrder);
    }

    /**
     * Creates a Lincoln Cents collection whose notes and custom coins carry HOSTILE_STRINGS,
     * followed by an ordinary Buffalo Nickels collection, so that a parser that loses its
     * place in the hostile rows also corrupts the collection after them
     *
     * @param activity activity whose database to populate
     */
    private void setupHostileCollections(MainActivity activity) {
        ArrayList<CoinSlot> coinList = getDefaultCoins(LincolnCents.COLLECTION_TYPE);

        // Hostile notes on the generated coins, with varied advanced-view values
        for (int i = 0; i < HOSTILE_STRINGS.length; i++) {
            CoinSlot coin = coinList.get(i);
            coin.setAdvancedNotes(HOSTILE_STRINGS[i]);
            coin.setInCollection(i % 2 == 0);
            coin.setAdvancedGrades(i % 5);
            coin.setAdvancedQuantities(i % 7);
        }

        // Custom coins (as added with "Add Coin") with hostile identifiers, mints and notes
        int sortOrder = coinList.size();
        int imageId = coinList.get(0).getImageId();
        for (int i = 0; i < HOSTILE_STRINGS.length; i++) {
            CoinSlot coin = new CoinSlot(HOSTILE_STRINGS[i],
                    HOSTILE_STRINGS[HOSTILE_STRINGS.length - 1 - i], sortOrder++, imageId, true);
            coin.setAdvancedNotes(HOSTILE_STRINGS[(i + 1) % HOSTILE_STRINGS.length]);
            coin.setInCollection(i % 3 == 0);
            coinList.add(coin);
        }
        // A row that starts like a section separator: "-----", "coinList", ...
        coinList.add(new CoinSlot(ExportImportHelper.CSV_SEPARATOR, ExportImportHelper.JSON_COIN_LIST,
                sortOrder, imageId, true));

        createCollection(activity, HOSTILE_COLLECTION_NAME, LincolnCents.COLLECTION_TYPE, coinList, 0);
        createCollection(activity, BuffaloNickels.COLLECTION_TYPE, BuffaloNickels.COLLECTION_TYPE,
                getDefaultCoins(BuffaloNickels.COLLECTION_TYPE), 1);
        activity.updateCollectionListFromDatabase();
    }

    /**
     * Asserts that two coin lists match field by field, so a failure names the coin and field
     *
     * @param collectionName   collection being compared
     * @param expected         coins before the round trip
     * @param actual           coins after the round trip
     * @param compareNewFields true to compare the fields the legacy CSV format lacks (sort
     *                         order, custom coin flag, image id)
     */
    private static void assertCoinListsEqual(String collectionName, ArrayList<CoinSlot> expected,
                                             ArrayList<CoinSlot> actual, boolean compareNewFields) {
        assertEquals("Coin count for " + collectionName, expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            CoinSlot want = expected.get(i);
            CoinSlot got = actual.get(i);
            String label = collectionName + " coin " + i + " ";
            assertEquals(label + "identifier", want.getIdentifier(), got.getIdentifier());
            assertEquals(label + "mint", want.getMint(), got.getMint());
            assertEquals(label + "in collection", want.isInCollection(), got.isInCollection());
            assertEquals(label + "grade", want.getAdvancedGrades(), got.getAdvancedGrades());
            assertEquals(label + "quantity", want.getAdvancedQuantities(), got.getAdvancedQuantities());
            assertEquals(label + "notes", want.getAdvancedNotes(), got.getAdvancedNotes());
            if (compareNewFields) {
                assertEquals(label + "sort order", want.getSortOrder(), got.getSortOrder());
                assertEquals(label + "custom coin", want.isCustomCoin(), got.isCustomCoin());
                assertEquals(label + "image id", want.getImageId(), got.getImageId());
            }
        }
    }

    /**
     * Deletes every collection, runs the import, and checks the collections came back intact
     *
     * @param activity         activity whose database to use
     * @param runImport        runs the import, returning its error message ("" on success)
     * @param compareNewFields false for the legacy CSV format, which lacks some fields
     */
    private void deleteImportAndCompare(MainActivity activity, Supplier<String> runImport,
                                        boolean compareNewFields) {
        ArrayList<String> beforeNames = getCollectionNames(activity);
        ArrayList<ArrayList<CoinSlot>> beforeCoinLists =
                getCoinSlotListsFromCollectionNames(activity.mDbAdapter, beforeNames);
        assertEquals(Arrays.asList(HOSTILE_COLLECTION_NAME, BuffaloNickels.COLLECTION_TYPE), beforeNames);

        deleteAllCollections(activity);
        activity.updateCollectionListFromDatabase();
        assertEquals(0, getCollectionNames(activity).size());

        assertEquals("", runImport.get());
        ArrayList<String> afterNames = getCollectionNames(activity);
        assertEquals(beforeNames, afterNames);
        ArrayList<ArrayList<CoinSlot>> afterCoinLists =
                getCoinSlotListsFromCollectionNames(activity.mDbAdapter, afterNames);
        for (int i = 0; i < beforeNames.size(); i++) {
            assertCoinListsEqual(beforeNames.get(i), beforeCoinLists.get(i), afterCoinLists.get(i),
                    compareNewFields);
        }
    }

    /**
     * Hostile-but-valid notes, identifiers, mints and collection names survive a
     * single-file CSV export and import, and exporting again gives the same bytes
     */
    @Test
    public void test_csvRoundTripHostileData() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupHostileCollections(activity);
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);

                File exportFile = getTempFile("hostile-export.csv");
                OutputStream outputStream = openOutputStream(exportFile);
                assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                        helper.exportCollectionsToSingleCSV(outputStream, LEGACY_EXPORT_FOLDER_NAME));
                closeStream(outputStream);

                deleteImportAndCompare(activity, () -> {
                    InputStream inputStream = openInputStream(exportFile);
                    String result = helper.importCollectionsFromSingleCSV(inputStream);
                    closeStream(inputStream);
                    return result;
                }, true);

                File reExportFile = getTempFile("hostile-re-export.csv");
                outputStream = openOutputStream(reExportFile);
                helper.exportCollectionsToSingleCSV(outputStream, LEGACY_EXPORT_FOLDER_NAME);
                closeStream(outputStream);
                assertArrayEquals(readAllBytes(exportFile), readAllBytes(reExportFile));
            });
        }
    }

    /**
     * Hostile-but-valid notes, identifiers, mints and collection names survive a JSON
     * export and import, and exporting again gives the same bytes
     */
    @Test
    public void test_jsonRoundTripHostileData() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupHostileCollections(activity);
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);

                File exportFile = getTempFile("hostile-export.json");
                OutputStream outputStream = openOutputStream(exportFile);
                assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                        helper.exportCollectionsToJson(outputStream, LEGACY_EXPORT_FOLDER_NAME));
                closeStream(outputStream);

                deleteImportAndCompare(activity, () -> {
                    InputStream inputStream = openInputStream(exportFile);
                    String result = helper.importCollectionsFromJson(inputStream);
                    closeStream(inputStream);
                    return result;
                }, true);

                File reExportFile = getTempFile("hostile-re-export.json");
                outputStream = openOutputStream(reExportFile);
                helper.exportCollectionsToJson(outputStream, LEGACY_EXPORT_FOLDER_NAME);
                closeStream(outputStream);
                assertArrayEquals(readAllBytes(exportFile), readAllBytes(reExportFile));
            });
        }
    }

    /**
     * Hostile-but-valid notes, identifiers, mints and collection names survive a legacy
     * (one file per collection) CSV export and import
     */
    @Test
    public void test_legacyCsvRoundTripHostileData() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                assertTrue(setEnabledPermissions(activity));
                setupHostileCollections(activity);
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);

                assertEquals(activity.mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME),
                        helper.exportCollectionsToLegacyCSV(activity.getLegacyExportFolderName()));

                deleteImportAndCompare(activity,
                        () -> helper.importCollectionsFromLegacyCSV(activity.getLegacyExportFolderName()),
                        false);
            });
        }
    }

    private static byte[] readAllBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Test importing a v1 database collection
     */
    @Test
    public void test_csvImportV1Collection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                File v1DbDir = getTestDataDir("v1-coin-collection-files");
                assertTrue(setEnabledPermissions(activity));

                // Run import and check results
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                assertEquals("", helper.importCollectionsFromLegacyCSV(v1DbDir.getAbsolutePath()));
                ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                assertEquals(26, afterCollectionNames.size());

                // Spot-check names and order, including a name whose '/' was
                // encoded as _SL_ in the file name
                assertEquals("Pennies", afterCollectionNames.get(0));
                assertEquals("Sacagawea/Native American Dollars", afterCollectionNames.get(9));
                assertEquals("American Innovation Dollars", afterCollectionNames.get(25));

                // Spot-check that the collected state came across
                assertTrue(findCoin(activity, "National Park Quarters", "Hot Springs", "D").isInCollection());
                assertTrue(findCoin(activity, "Franklin Half Dollars", "1948", "").isInCollection());
                assertFalse(findCoin(activity, "Pennies", "1909 V.D.B", "").isInCollection());
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
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
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
                assertEquals(0, getCollectionNames(activity).size());
                assertEquals(0, activity.mNumberOfCollections);
                assertEquals(NUMBER_OF_COLLECTION_LIST_SPACERS, activity.mCollectionListEntries.size());

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
    public void test_csvExportImportMethods() throws ImportFormatException {
        for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS) {
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
        for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS) {
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

            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * Test importing a saved collection
     */
    @Test
    public void test_importSavedFiles() {
        Object[][] testFiles = {
                {"coin-collection-010822-16.json", 11},
                {"coin-collection-010822-17.csv", 11},
                {"coin-collection-010822-17-excel.csv", 11},
        };
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                for (Object[] testFile : testFiles) {
                    ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                    String filePath = (String) testFile[0];
                    InputStream inputStream = openTestData(filePath);
                    assertNotNull("Fixture not found: " + filePath, inputStream);
                    if (filePath.endsWith(".csv")) {
                        assertEquals("", helper.importCollectionsFromSingleCSV(inputStream));
                    } else {
                        assertEquals("", helper.importCollectionsFromJson(inputStream));
                    }
                    ArrayList<String> afterCollectionNames = getCollectionNames(activity);
                    assertEquals((int) testFile[1], afterCollectionNames.size());
                    closeStream(inputStream);
                }
            });
        }
    }
}
