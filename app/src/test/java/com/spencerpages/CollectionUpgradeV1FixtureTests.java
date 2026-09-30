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

import static com.spencerpages.MainApplication.DATABASE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Upgrade tests driven by frozen V1 fixtures: the coin lists that the first version of this
 * codebase (app 2.2.1, database version 8) created for each of its 25 collection types, under
 * several creation-option sets. See GenerateV1Fixtures for how they were produced.
 *
 * <p>Each fixture is written into a raw database with the V1 schema, then MainActivity opens
 * it so the app's real onUpgrade() chain runs from that version to the current one — the path
 * a V1 user's database takes when they update the app. Every upgraded collection must then
 * match a freshly created one:
 * <ul>
 *   <li>against the parameters the upgrade inferred for it (like the V23 fixture tests),</li>
 *   <li>against those parameters with the date range the V1 user chose, so an upgrade that
 *       skips new coins can't hide behind a matching (but wrongly inferred) end year, and</li>
 *   <li>for collections made with the V1 defaults, against the current defaults too.</li>
 * </ul>
 * Coins the user had collected must still be collected afterwards.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionUpgradeV1FixtureTests extends BaseTestCase {

    // Under app/src/test/data
    private static final String FIXTURE_DIR = "v1-upgrades";

    /** A collection from a V1 fixture file */
    private static class V1Collection {
        final String name;
        final String coinType;
        final boolean editDateRange;
        final int startYear;
        final int stopYear;
        final List<String[]> coins = new ArrayList<>();

        V1Collection(JSONObject json) throws JSONException {
            name = json.getString("name");
            coinType = json.getString("coinType");
            JSONObject v1Options = json.getJSONObject("v1Options");
            editDateRange = v1Options.getBoolean("editDateRange");
            startYear = v1Options.getInt("startYear");
            stopYear = v1Options.getInt("stopYear");
            JSONArray coinArray = json.getJSONArray("coins");
            for (int i = 0; i < coinArray.length(); i++) {
                JSONArray coin = coinArray.getJSONArray(i);
                coins.add(new String[]{coin.getString(0), coin.getString(1)});
            }
        }
    }

    /**
     * Opens the database at the V1 version, leaving creation of the schema to the caller
     */
    private static class V1DatabaseHelper extends SQLiteOpenHelper {
        V1DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, GenerateV1Fixtures.V1_DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private static List<V1Collection> readFixture(String fixtureFilename) {
        String fixturePath = FIXTURE_DIR + "/" + fixtureFilename;
        try {
            String contents = readTestData(fixturePath);
            assertNotNull("Fixture not found: " + fixturePath, contents);
            JSONObject root = new JSONObject(contents);
            assertEquals(GenerateV1Fixtures.V1_DATABASE_VERSION, root.getInt("databaseVersion"));
            JSONArray collectionArray = root.getJSONArray("collections");
            List<V1Collection> collections = new ArrayList<>();
            for (int i = 0; i < collectionArray.length(); i++) {
                collections.add(new V1Collection(collectionArray.getJSONObject(i)));
            }
            return collections;
        } catch (IOException | JSONException e) {
            throw new RuntimeException("Failed to read fixture: " + fixturePath, e);
        }
    }

    /**
     * Every third coin starts out collected, so the tests can check that upgrades keep it
     */
    private static boolean isSeededAsCollected(int coinIndex) {
        return coinIndex % 3 == 0;
    }

    /**
     * Writes the fixture's collections into a database the way V1 would have stored them
     */
    private static void createV1Database(List<V1Collection> collections) {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DATABASE_NAME);
        V1DatabaseHelper helper = new V1DatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        // From V1 DatabaseAdapter.createCollectionInfoTable()
        db.execSQL("CREATE TABLE collection_info (_id integer primary key,"
                + " name text not null,"
                + " coinType text not null,"
                + " total integer,"
                + " display integer default 0,"
                + " displayOrder integer"
                + ");");

        int displayOrder = 0;
        for (V1Collection collection : collections) {
            // From V1 DatabaseAdapter.createNewTable(String)
            db.execSQL("CREATE TABLE [" + collection.name + "] (_id integer primary key,"
                    + " coinIdentifier text not null,"
                    + " coinMint text,"
                    + " inCollection integer,"
                    + " advGradeIndex integer default 0,"
                    + " advQuantityIndex integer default 0,"
                    + " advNotes text default \"\");");
            for (int i = 0; i < collection.coins.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("coinIdentifier", collection.coins.get(i)[0]);
                values.put("coinMint", collection.coins.get(i)[1]);
                values.put("inCollection", isSeededAsCollected(i) ? 1 : 0);
                db.insert("[" + collection.name + "]", null, values);
            }

            // From V1 DatabaseAdapter.addEntryToCollectionInfoTable()
            ContentValues values = new ContentValues();
            values.put("name", collection.name);
            values.put("coinType", collection.coinType);
            values.put("total", collection.coins.size());
            values.put("displayOrder", displayOrder++);
            values.put("display", 0);
            db.insert("collection_info", null, values);
        }
        db.close();
        helper.close();
    }

    /**
     * Upgrades the fixture from a V1 database and validates every collection
     *
     * @param fixtureFilename fixture to load
     * @param matchesDefaults true if every collection was made with the V1 defaults
     */
    private void upgradeAndValidateFixture(String fixtureFilename, boolean matchesDefaults) {
        List<V1Collection> fixtureCollections = readFixture(fixtureFilename);
        createV1Database(fixtureCollections);

        // Opening the app's database runs onUpgrade() from the old version
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(collectionListEntries);
                assertEquals(fixtureCollections.size(), collectionListEntries.size());

                for (int i = 0; i < fixtureCollections.size(); i++) {
                    V1Collection fixtureCollection = fixtureCollections.get(i);
                    CollectionListInfo upgradedInfo = collectionListEntries.get(i);
                    String name = fixtureCollection.name;
                    assertEquals(name, upgradedInfo.getName());

                    ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(name, true);
                    assertNotNull("Coin list null for " + name, dbCoins);
                    assertEquals("Total mismatch for " + name, dbCoins.size(), upgradedInfo.getMax());

                    // V1 let a collection be made with only mints that never struck the
                    // series, leaving it empty. There's nothing to infer parameters from, and
                    // it must stay empty rather than gain coins the user never chose.
                    if (fixtureCollection.coins.isEmpty()) {
                        assertEquals("Empty collection gained coins: " + name, 0, dbCoins.size());
                        continue;
                    }

                    // Against the parameters the upgrade inferred
                    ParcelableHashMap inferredParameters =
                            CoinPageCreator.getParametersFromCollectionListInfo(upgradedInfo);
                    assertCoinsMatch(name + " (inferred parameters)", upgradedInfo, inferredParameters, dbCoins);

                    ParcelableHashMap defaultParameters = new ParcelableHashMap();
                    upgradedInfo.getCollectionObj().getCreationParameters(defaultParameters);

                    // Against today's defaults
                    if (matchesDefaults) {
                        assertCoinsMatch(name + " (default parameters)", upgradedInfo, defaultParameters, dbCoins);
                    }

                    // Against the date range the user chose - the whole series (as it stands
                    // today) unless they narrowed it. The upgrade infers the range from the
                    // coins present, which can be narrower when the chosen mints skipped the
                    // first or last years, but it must give the same coins. This catches
                    // upgrades that skip new coins because of an inferred end year.
                    if (defaultParameters.containsKey(CoinPageCreator.OPT_START_YEAR)) {
                        ParcelableHashMap chosenParameters =
                                CoinPageCreator.getParametersFromCollectionListInfo(upgradedInfo);
                        chosenParameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE,
                                fixtureCollection.editDateRange);
                        chosenParameters.put(CoinPageCreator.OPT_START_YEAR, fixtureCollection.editDateRange
                                ? fixtureCollection.startYear
                                : defaultParameters.get(CoinPageCreator.OPT_START_YEAR));
                        chosenParameters.put(CoinPageCreator.OPT_STOP_YEAR, fixtureCollection.editDateRange
                                ? fixtureCollection.stopYear
                                : defaultParameters.get(CoinPageCreator.OPT_STOP_YEAR));
                        assertCoinsMatch(name + " (chosen date range)", upgradedInfo, chosenParameters, dbCoins);
                    }

                    // Collected coins survive the upgrade
                    int expectedCollected = 0;
                    for (int j = 0; j < fixtureCollection.coins.size(); j++) {
                        if (isSeededAsCollected(j)) {
                            expectedCollected++;
                        }
                    }
                    int actualCollected = 0;
                    for (CoinSlot coin : dbCoins) {
                        if (coin.isInCollection()) {
                            actualCollected++;
                        }
                    }
                    assertEquals("Collected count mismatch for " + name, expectedCollected, actualCollected);
                }
            });
        }
    }

    private static void assertCoinsMatch(String label, CollectionListInfo upgradedInfo,
                                         ParcelableHashMap parameters, ArrayList<CoinSlot> dbCoins) {
        ArrayList<CoinSlot> expectedCoins = new ArrayList<>();
        upgradedInfo.getCollectionObj().populateCollectionLists(parameters, expectedCoins);
        assertEquals("Coin count mismatch for " + label, expectedCoins.size(), dbCoins.size());
        for (int i = 0; i < expectedCoins.size(); i++) {
            assertEquals("Identifier mismatch at index " + i + " for " + label,
                    expectedCoins.get(i).getIdentifier(), dbCoins.get(i).getIdentifier());
            assertEquals("Mint mismatch at index " + i + " for " + label,
                    expectedCoins.get(i).getMint(), dbCoins.get(i).getMint());
            assertEquals("ImageId mismatch at index " + i + " for " + label,
                    expectedCoins.get(i).getImageId(), dbCoins.get(i).getImageId());
        }
    }

    @Test
    public void test_upgradeFromV1Defaults() {
        upgradeAndValidateFixture("v1-default.json", true);
    }

    @Test
    public void test_upgradeFromV1AllOptions() {
        upgradeAndValidateFixture("v1-all-options.json", false);
    }

    @Test
    public void test_upgradeFromV1SplitA() {
        upgradeAndValidateFixture("v1-split-A.json", false);
    }

    @Test
    public void test_upgradeFromV1SplitB() {
        upgradeAndValidateFixture("v1-split-B.json", false);
    }

    @Test
    public void test_upgradeFromV1DateRange() {
        upgradeAndValidateFixture("v1-date-range.json", false);
    }
}
