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

import static com.coincollection.CollectionListInfo.COL_COIN_TYPE;
import static com.coincollection.CollectionListInfo.COL_DISPLAY;
import static com.coincollection.CollectionListInfo.COL_END_YEAR;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_SHOW_CHECKBOXES;
import static com.coincollection.CollectionListInfo.COL_SHOW_MINT_MARKS;
import static com.coincollection.CollectionListInfo.COL_START_YEAR;
import static com.coincollection.CollectionListInfo.COL_TOTAL;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.coincollection.ExportImportHelper.CSV_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseAdapter;
import com.coincollection.DatabaseHelper;
import com.coincollection.ExportImportHelper;
import com.coincollection.MainActivity;
import com.spencerpages.collections.LincolnCents;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Negative-path tests for import. Every case must return a non-empty error string
 * and leave the pre-existing database completely untouched.
 */
@RunWith(RobolectricTestRunner.class)
public class ExportImportErrorTests extends BaseTestCase {

    private static final String COLLECTION_A = "Collection A";
    private static final String COLLECTION_B = "Collection B";

    /**
     * Snapshot of the database contents, used to prove that a failed import changed nothing
     */
    private static class DbSnapshot {
        final ArrayList<String> names;
        final ArrayList<ArrayList<CoinSlot>> coinLists;

        DbSnapshot(ArrayList<String> names, ArrayList<ArrayList<CoinSlot>> coinLists) {
            this.names = names;
            this.coinLists = coinLists;
        }
    }

    /**
     * Populate the database with two known collections
     *
     * @param activity test activity
     */
    private void setupTwoCollections(MainActivity activity) {
        ArrayList<String> names = new ArrayList<>(Arrays.asList(COLLECTION_A, COLLECTION_B));
        assertTrue(setupCollectionsWithNames(activity, names));
    }

    /**
     * Take a snapshot of every collection and its coins
     *
     * @param activity test activity
     * @return snapshot of the database contents
     */
    private DbSnapshot snapshotDb(MainActivity activity) {
        ArrayList<String> names = getCollectionNames(activity);
        return new DbSnapshot(names, getCoinSlotListsFromCollectionNames(activity.mDbAdapter, names));
    }

    /**
     * Assert that the database still matches a previously taken snapshot
     *
     * @param activity test activity
     * @param before   snapshot taken before the failed import
     */
    private void assertDbUnchanged(MainActivity activity, DbSnapshot before) {
        DbSnapshot after = snapshotDb(activity);
        assertEquals(before.names, after.names);
        compareListOfCoinSlotLists(before.coinLists, after.coinLists, true);
    }

    /**
     * Run a single-file CSV import from an in-memory string
     *
     * @param activity test activity
     * @param contents CSV file contents
     * @return the import result string
     */
    private String importCsv(MainActivity activity, String contents) {
        ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
        InputStream inputStream = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
        String result = helper.importCollectionsFromSingleCSV(inputStream);
        closeStream(inputStream);
        return result;
    }

    /**
     * Run a JSON import from an in-memory string
     *
     * @param activity test activity
     * @param contents JSON file contents
     * @return the import result string
     */
    private String importJson(MainActivity activity, String contents) {
        ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
        InputStream inputStream = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
        String result = helper.importCollectionsFromJson(inputStream);
        closeStream(inputStream);
        return result;
    }

    /**
     * Build the header rows shared by every valid CSV import file
     *
     * @param databaseVersion database version to declare in the file
     * @return the start of a CSV import file
     */
    private String csvHeader(int databaseVersion) {
        return CSV_SEPARATOR + "," + ExportImportHelper.JSON_DB_VERSION + "\n"
                + databaseVersion + "\n";
    }

    /**
     * Build a collection section for a CSV import file
     *
     * @param name     collection name
     * @param coinType collection type string
     * @param total    total coin count cell
     * @return CSV collection section
     */
    private String csvCollectionSection(String name, String coinType, String total) {
        return CSV_SEPARATOR + "," + ExportImportHelper.JSON_COLLECTIONS + "\n"
                + String.join(",", CollectionListInfo.getCsvExportHeader()) + "\n"
                + name + "," + coinType + ",0," + total + ",0,1909,2020,0,0,15,1\n";
    }

    /**
     * Build a coin list section for a CSV import file
     *
     * @param coinRows raw coin rows to include
     * @return CSV coin list section
     */
    private String csvCoinSection(String... coinRows) {
        StringBuilder builder = new StringBuilder();
        builder.append(CSV_SEPARATOR).append(",").append(ExportImportHelper.JSON_COIN_LIST).append("\n");
        builder.append(String.join(",", CoinSlot.getCsvExportHeader())).append("\n");
        for (String coinRow : coinRows) {
            builder.append(coinRow).append("\n");
        }
        return builder.toString();
    }

    /**
     * Rewrite a collection's stored coin type to a value this app doesn't recognize
     *
     * @param activity       test activity
     * @param collectionName collection to corrupt
     */
    private void makeCollectionTypeUnknown(MainActivity activity, String collectionName) {
        SQLiteDatabase db = new DatabaseHelper(activity).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COIN_TYPE, "Martian Doubloons");
        assertEquals(1, DatabaseHelper.runSqlUpdate(db, TBL_COLLECTION_INFO, values,
                COL_NAME + "=?", new String[]{collectionName}));
        db.close();
    }

    /**
     * A completely empty import file has no database version, so it must be rejected
     */
    @Test
    public void test_csvImportEmptyStream() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                assertFalse(importCsv(activity, "").isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * A collection row that's missing required columns must be rejected
     */
    @Test
    public void test_csvImportTooFewColumns() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + CSV_SEPARATOR + "," + ExportImportHelper.JSON_COLLECTIONS + "\n"
                        + String.join(",", CollectionListInfo.getCsvExportHeader()) + "\n"
                        + "Imported," + LincolnCents.COLLECTION_TYPE + "\n";

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * A non-numeric total cell must be rejected rather than crashing with NumberFormatException
     */
    @Test
    public void test_csvImportNonNumericTotal() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection("Imported", LincolnCents.COLLECTION_TYPE, "not-a-number");

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * An unknown coin type must abort the import instead of silently becoming the
     * collection type at index 0
     */
    @Test
    public void test_csvImportUnknownCoinType() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection("Imported", "Martian Doubloons", "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);

                // The bogus collection must not have been imported as Lincoln Cents
                assertFalse(getCollectionNames(activity).contains("Imported"));
            });
        }
    }

    /**
     * A coin row that appears before any collection header has nowhere to go
     */
    @Test
    public void test_csvImportCoinWithoutCollection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * Two collections with the same name can't both be created, so this must be caught
     * during validation - before anything is dropped
     */
    @Test
    public void test_csvImportDuplicateNamesWithinFile() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection("Imported", LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1")
                        + csvCollectionSection("imported", LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2020,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * A collection using an internal table name must be rejected
     */
    @Test
    public void test_csvImportReservedName() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection(TBL_COLLECTION_INFO, LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * Atomicity proof: the first collection in the file is perfectly valid and the second
     * is not. The whole import must be rejected, leaving the original database intact.
     */
    @Test
    public void test_csvImportSecondCollectionInvalidIsAtomic() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);
                assertEquals(2, before.names.size());

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection("Good Collection", LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1")
                        + csvCollectionSection("Bad Collection", "Martian Doubloons", "1")
                        + csvCoinSection("2020,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());

                // Neither the valid nor the invalid collection may have landed
                assertDbUnchanged(activity, before);
                assertFalse(getCollectionNames(activity).contains("Good Collection"));
                assertFalse(getCollectionNames(activity).contains("Bad Collection"));
            });
        }
    }

    /**
     * A truncated JSON document must produce an error rather than a crash
     */
    @Test
    public void test_jsonImportTruncated() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = "{\"" + ExportImportHelper.JSON_DB_VERSION + "\":"
                        + MainApplication.DATABASE_VERSION + ",\""
                        + ExportImportHelper.JSON_COLLECTIONS + "\":[{\"" + COL_NAME + "\":\"Imp";

                assertFalse(importJson(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * A file written by a newer version of the app can't be imported safely
     */
    @Test
    public void test_jsonImportFutureDatabaseVersion() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = "{\"" + ExportImportHelper.JSON_DB_VERSION + "\":"
                        + (MainApplication.DATABASE_VERSION + 1) + ",\""
                        + ExportImportHelper.JSON_COLLECTIONS + "\":[]}";

                assertFalse(importJson(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * An unknown coin type in a JSON file must abort the import
     */
    @Test
    public void test_jsonImportUnknownCoinType() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = "{\"" + ExportImportHelper.JSON_DB_VERSION + "\":"
                        + MainApplication.DATABASE_VERSION + ",\""
                        + ExportImportHelper.JSON_COLLECTIONS + "\":[{"
                        + "\"" + COL_NAME + "\":\"Imported\","
                        + "\"" + COL_COIN_TYPE + "\":\"Martian Doubloons\","
                        + "\"" + COL_TOTAL + "\":0,"
                        + "\"" + COL_DISPLAY + "\":0,"
                        + "\"" + COL_START_YEAR + "\":0,"
                        + "\"" + COL_END_YEAR + "\":0,"
                        + "\"" + COL_SHOW_MINT_MARKS + "\":\"0\","
                        + "\"" + COL_SHOW_CHECKBOXES + "\":\"0\","
                        + "\"" + ExportImportHelper.JSON_COIN_LIST + "\":[]}]}";

                assertFalse(importJson(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
                assertFalse(getCollectionNames(activity).contains("Imported"));
            });
        }
    }

    /**
     * A JSON collection with no coinType at all must be rejected too
     */
    @Test
    public void test_jsonImportMissingCoinType() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = "{\"" + ExportImportHelper.JSON_DB_VERSION + "\":"
                        + MainApplication.DATABASE_VERSION + ",\""
                        + ExportImportHelper.JSON_COLLECTIONS + "\":[{"
                        + "\"" + COL_NAME + "\":\"Imported\","
                        + "\"" + COL_TOTAL + "\":0,"
                        + "\"" + ExportImportHelper.JSON_COIN_LIST + "\":[]}]}";

                assertFalse(importJson(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * A valid import file that replaces collections with the same names must still work -
     * this is the normal export/import round trip and must not be broken by validation
     */
    @Test
    public void test_csvImportOverExistingNamesSucceeds() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection(COLLECTION_A, LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertEquals("", importCsv(activity, contents));

                ArrayList<String> names = getCollectionNames(activity);
                assertEquals(1, names.size());
                assertEquals(COLLECTION_A, names.get(0));
                assertEquals(1, activity.mDbAdapter.getCoinList(COLLECTION_A, true).size());
            });
        }
    }

    /**
     * Importing a file from an older database version must run the upgrade inside the same
     * transaction and still succeed
     */
    @Test
    public void test_csvImportOlderDatabaseVersionSucceeds() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION - 1)
                        + csvCollectionSection("Old Version Collection", LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertEquals("", importCsv(activity, contents));

                ArrayList<String> names = getCollectionNames(activity);
                assertEquals(1, names.size());
                assertEquals("Old Version Collection", names.get(0));
            });
        }
    }

    /**
     * A collection stored with an unrecognized coin type must be skipped and reported
     * rather than throwing, which used to crash-loop getWritableDatabase()
     */
    @Test
    public void test_getAllTablesSkipsUnknownCoinType() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                // Insert a collection_info row referencing a coin type this app doesn't know
                DatabaseAdapter dbAdapter = new DatabaseAdapter(activity);
                dbAdapter.open();
                CollectionListInfo bogusInfo = new CollectionListInfo("Future Collection", 0, 0,
                        MainApplication.getIndexFromCollectionNameStr(LincolnCents.COLLECTION_TYPE),
                        0, 0, 0, "0", "0");
                dbAdapter.createAndPopulateNewTable(bogusInfo, 2, new ArrayList<>());
                dbAdapter.close();

                makeCollectionTypeUnknown(activity, "Future Collection");

                // The other collections must still load, and the bad one must be reported
                ArrayList<CollectionListInfo> entries = new ArrayList<>();
                ArrayList<String> skipped = new ArrayList<>();
                activity.mDbAdapter.getAllTables(entries, skipped);

                assertEquals(2, entries.size());
                assertEquals(COLLECTION_A, entries.get(0).getName());
                assertEquals(COLLECTION_B, entries.get(1).getName());
                assertEquals(1, skipped.size());
                assertEquals("Future Collection", skipped.get(0));
            });
        }
    }

    /**
     * Rollback proof: force a failure inside the import transaction itself (rather than
     * during parsing) by leaving an orphan table behind that isn't listed in
     * collection_info, so the CREATE TABLE for the imported collection fails after the
     * existing tables have already been dropped. The original database must come back.
     */
    @Test
    public void test_csvImportRollsBackFailureInsideTransaction() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                // Create a table that collection_info doesn't know about, so it survives the
                // drop loop and collides with the imported collection's CREATE TABLE
                SQLiteDatabase db = new DatabaseHelper(activity).getWritableDatabase();
                db.execSQL("CREATE TABLE [Orphan Table] (_id integer primary key);");
                db.close();

                DbSnapshot before = snapshotDb(activity);
                assertEquals(2, before.names.size());

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection("Orphan Table", LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());

                // Both original collections and all of their coins must still be there
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * A successful import must drop every collection listed in collection_info, including
     * one whose coin type this app doesn't recognize - otherwise its table would be
     * orphaned when the collection_info row goes away
     */
    @Test
    public void test_csvImportDropsCollectionWithUnknownCoinType() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                makeCollectionTypeUnknown(activity, COLLECTION_B);

                // Import a collection reusing the unrecognized collection's name. This only
                // works if the old table was dropped despite its unknown type.
                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection(COLLECTION_B, LincolnCents.COLLECTION_TYPE, "1")
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertEquals("", importCsv(activity, contents));

                ArrayList<String> names = getCollectionNames(activity);
                assertEquals(1, names.size());
                assertEquals(COLLECTION_B, names.get(0));
                assertEquals(1, activity.mDbAdapter.getCoinList(COLLECTION_B, true).size());
            });
        }
    }

    /**
     * A flag value that can't be recovered must abort the import rather than silently
     * becoming 0, which would wipe the collection's mint mark configuration
     */
    @Test
    public void test_csvImportUnrecoverableFlagValue() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                // Trailing cell 9 is showMintMarks - "not a number" is unrecoverable
                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + CSV_SEPARATOR + "," + ExportImportHelper.JSON_COLLECTIONS + "\n"
                        + String.join(",", CollectionListInfo.getCsvExportHeader()) + "\n"
                        + "Imported," + LincolnCents.COLLECTION_TYPE
                        + ",0,1,0,1909,2020,0,0,not a number,1\n"
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertFalse(importCsv(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * The same check must apply to JSON imports
     */
    @Test
    public void test_jsonImportUnrecoverableFlagValue() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);
                DbSnapshot before = snapshotDb(activity);

                String contents = "{\"" + ExportImportHelper.JSON_DB_VERSION + "\":"
                        + MainApplication.DATABASE_VERSION + ",\""
                        + ExportImportHelper.JSON_COLLECTIONS + "\":[{"
                        + "\"" + COL_NAME + "\":\"Imported\","
                        + "\"" + COL_COIN_TYPE + "\":\"" + LincolnCents.COLLECTION_TYPE + "\","
                        + "\"" + COL_TOTAL + "\":0,"
                        + "\"" + COL_DISPLAY + "\":0,"
                        + "\"" + COL_START_YEAR + "\":0,"
                        + "\"" + COL_END_YEAR + "\":0,"
                        + "\"" + COL_SHOW_MINT_MARKS + "\":\"not a number\","
                        + "\"" + COL_SHOW_CHECKBOXES + "\":\"0\","
                        + "\"" + ExportImportHelper.JSON_COIN_LIST + "\":[]}]}";

                assertFalse(importJson(activity, contents).isEmpty());
                assertDbUnchanged(activity, before);
            });
        }
    }

    /**
     * Spreadsheet-mangled flag values must still be recovered rather than rejected -
     * tightening the import check must not break the issue #406 recovery path
     */
    @Test
    public void test_csvImportRecoversSpreadsheetMangledFlags() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                // Cell 9 uses scientific notation, cell 10 a trailing decimal
                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + CSV_SEPARATOR + "," + ExportImportHelper.JSON_COLLECTIONS + "\n"
                        + String.join(",", CollectionListInfo.getCsvExportHeader()) + "\n"
                        + "Imported," + LincolnCents.COLLECTION_TYPE
                        + ",0,1,0,1909,2020,0,0,2.68435E+8,268435456.0\n"
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertEquals("", importCsv(activity, contents));

                ArrayList<CollectionListInfo> entries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(entries);
                assertEquals(1, entries.size());
                assertEquals(268435000L, entries.get(0).getMintMarkFlagsAsLong());
                assertEquals(268435456L, entries.get(0).getCheckboxFlagsAsLong());
            });
        }
    }

    /**
     * An empty flag cell is the legitimate "no flags set" encoding and must still import
     */
    @Test
    public void test_csvImportEmptyFlagValueIsZero() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + CSV_SEPARATOR + "," + ExportImportHelper.JSON_COLLECTIONS + "\n"
                        + String.join(",", CollectionListInfo.getCsvExportHeader()) + "\n"
                        + "Imported," + LincolnCents.COLLECTION_TYPE + ",0,1,0,1909,2020,0,0,,\n"
                        + csvCoinSection("2019,P,1,0,0,,0,0,-1");

                assertEquals("", importCsv(activity, contents));

                ArrayList<CollectionListInfo> entries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(entries);
                assertEquals(1, entries.size());
                assertEquals(0L, entries.get(0).getMintMarkFlagsAsLong());
                assertEquals(0L, entries.get(0).getCheckboxFlagsAsLong());
            });
        }
    }

    /**
     * A data row starting with the separator token must be treated as data, not silently
     * dropped as a malformed section header
     */
    @Test
    public void test_csvImportSeparatorLookalikeRowIsData() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupTwoCollections(activity);

                // A coin whose identifier happens to be the separator token
                String contents = csvHeader(MainApplication.DATABASE_VERSION)
                        + csvCollectionSection("Separator Test", LincolnCents.COLLECTION_TYPE, "2")
                        + csvCoinSection(CSV_SEPARATOR + ",P,1,0,0,,0,1,-1",
                        "2019,P,1,0,0,,1,0,-1");

                assertEquals("", importCsv(activity, contents));

                ArrayList<CoinSlot> coinList = activity.mDbAdapter.getCoinList("Separator Test", true);
                assertEquals(2, coinList.size());
                assertEquals(CSV_SEPARATOR, coinList.get(0).getIdentifier());
                assertEquals("2019", coinList.get(1).getIdentifier());
            });
        }
    }
}
