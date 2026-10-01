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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import org.robolectric.ParameterizedRobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

/**
 * Upgrade tests from every database version that shipped since 10, driven by frozen fixtures
 * of the collections each version created (see GenerateUpgradeFixtures): every collection
 * type under its default options, everything on, everything off, and a pairwise set of
 * option combinations.
 *
 * <p>Each fixture is written into a raw database with that version's schema, then
 * MainActivity opens it so the app's real onUpgrade() runs from that version to the current
 * one - the path a user's database takes when they update the app. Every upgraded collection
 * must then:
 * <ul>
 *   <li>hold exactly the coins, in the same order and with the same images, that a
 *       collection created today with its stored options would hold - and if the user took
 *       the whole series (no custom date range), the whole series as it stands today;</li>
 *   <li>keep the coins the user had collected;</li>
 *   <li>for versions that stored the creation options (15 onward), keep every mint and
 *       checkbox option the user chose; and</li>
 *   <li>leave options added since that version, which the user never saw, as new
 *       collections have them.</li>
 * </ul>
 * Differences are collected across all collections and checked against {@link #KNOWN}, the
 * list of differences that are understood. An unexpected difference fails the test, and so
 * does a known one that no longer occurs, so that fixing a difference means deleting its
 * entry.
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class UpgradePathTests extends BaseTestCase {

    /** A difference between an upgraded collection and a new one that is understood */
    private static final class KnownDifference {
        final int fromVersion;
        final int toVersion;
        final String reason;
        final Predicate<FixtureCollection> matches;

        /**
         * @param fromVersion the first database version upgrades from which show it
         * @param toVersion   the last database version upgrades from which show it
         * @param reason      why it happens; "BUG:" if it is a defect still to be fixed
         * @param matches     the fixture collections it applies to
         */
        KnownDifference(int fromVersion, int toVersion, String reason, Predicate<FixtureCollection> matches) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.reason = reason;
            this.matches = matches;
        }

        boolean appliesTo(FixtureCollection collection) {
            return collection.databaseVersion >= fromVersion && collection.databaseVersion <= toVersion
                    && matches.test(collection);
        }
    }

    /**
     * Understood differences. Every collection that differs must match an entry, and each
     * entry must match a collection that differs in every version it covers.
     */
    private static final List<KnownDifference> KNOWN = Arrays.asList(
            new KnownDifference(18, 20,
                    "BUG: upgrades add the 2023-2025 quarters in MINT_STRING_TO_FLAGS order "
                            + "(P, S, D) while new collections use P, D, S",
                    c -> c.coinType.equals("American Women Quarters") && c.isOn("ShowMintMarks")
                            && c.isOn("ShowMintMark2") && c.isOn("ShowMintMark3")),
            new KnownDifference(23, 24,
                    "Accepted (#441): before V27 clad-only collections wrongly held the "
                            + "1932-1964 silver quarters. The V27 upgrade deletes them, collected or not",
                    c -> c.coinType.equals("Washington Quarters") && c.isOn("ShowCheckbox2")
                            && !c.isOn("ShowCheckbox1")),
            new KnownDifference(10, 14,
                    "Accepted: before V15 the creation options weren't stored, so the V15 upgrade "
                            + "infers them from the coins. Philadelphia Morgan dollars have no mint "
                            + "mark, so only the 1878 8 Feathers coin shows that P was chosen; a "
                            + "P-only collection starting later is taken as one without mint marks "
                            + "(which would include the proof-only 1895). The coins are unchanged",
                    c -> c.coinType.equals("Morgan Dollars") && c.isOn("ShowMintMarks")
                            && c.isOn("ShowMintMark1") && !c.isOn("ShowMintMark2")
                            && !c.isOn("ShowMintMark3") && !c.isOn("ShowMintMark4")
                            && !c.isOn("ShowMintMark5") && !Integer.valueOf(1878).equals(c.options.get("StartYear")))
    );

    private final int mDatabaseVersion;

    public UpgradePathTests(int databaseVersion) {
        mDatabaseVersion = databaseVersion;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "fromV{0}")
    public static List<Object[]> getDatabaseVersions() {
        List<Object[]> versions = new ArrayList<>();
        for (GenerateUpgradeFixtures.Snapshot snapshot : GenerateUpgradeFixtures.SNAPSHOTS) {
            versions.add(new Object[]{snapshot.databaseVersion});
        }
        return versions;
    }

    /** A collection from an upgrade-path fixture */
    static final class FixtureCollection {
        final int databaseVersion;
        final String name;
        final String coinType;
        final Map<String, Object> options = new LinkedHashMap<>();
        final Map<String, String> labels = new LinkedHashMap<>();
        final JSONObject stored;
        final List<Object[]> coins = new ArrayList<>();

        FixtureCollection(int databaseVersion, JSONObject json) throws JSONException {
            this.databaseVersion = databaseVersion;
            name = json.getString("name");
            coinType = json.getString("coinType");
            JSONObject optionsJson = json.getJSONObject("options");
            for (java.util.Iterator<String> it = optionsJson.keys(); it.hasNext(); ) {
                String key = it.next();
                options.put(key, optionsJson.get(key));
            }
            JSONObject labelsJson = json.getJSONObject("labels");
            for (java.util.Iterator<String> it = labelsJson.keys(); it.hasNext(); ) {
                String key = it.next();
                labels.put(key, labelsJson.getString(key));
            }
            stored = json.optJSONObject("stored");
            JSONArray coinArray = json.getJSONArray("coins");
            for (int i = 0; i < coinArray.length(); i++) {
                JSONArray coin = coinArray.getJSONArray(i);
                coins.add(new Object[]{coin.getString(0), coin.getString(1), coin.getInt(2), coin.getInt(3)});
            }
        }

        boolean isOn(String option) {
            return Boolean.TRUE.equals(options.get(option));
        }
    }

    private List<FixtureCollection> readFixture() {
        String path = GenerateUpgradeFixtures.OUTPUT_DIR + "/v" + mDatabaseVersion + ".json.gz";
        try (InputStream in = openTestData(path)) {
            assertNotNull("Fixture not found: " + path, in);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream gzip = new GZIPInputStream(in)) {
                byte[] buffer = new byte[65536];
                int read;
                while ((read = gzip.read(buffer)) > 0) {
                    bytes.write(buffer, 0, read);
                }
            }
            JSONObject root = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            assertEquals(mDatabaseVersion, root.getInt("databaseVersion"));
            JSONArray collectionArray = root.getJSONArray("collections");
            List<FixtureCollection> collections = new ArrayList<>();
            for (int i = 0; i < collectionArray.length(); i++) {
                collections.add(new FixtureCollection(mDatabaseVersion, collectionArray.getJSONObject(i)));
            }
            return collections;
        } catch (IOException | JSONException e) {
            throw new RuntimeException("Failed to read fixture: " + path, e);
        }
    }

    /**
     * Opens the database at the fixture's version, leaving creation of the schema to the
     * caller
     */
    private static class FixtureDatabaseHelper extends SQLiteOpenHelper {
        FixtureDatabaseHelper(Context context, int version) {
            super(context, DATABASE_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    /**
     * Every third coin starts out collected, so the test can check that upgrades keep it
     */
    private static boolean isSeededAsCollected(int coinIndex) {
        return coinIndex % 3 == 0;
    }

    /**
     * Writes the fixture's collections into a database the way that version stored them.
     * The schema follows that version's DatabaseHelper.createCollectionInfoTable() and
     * DatabaseAdapter's collection table creation.
     */
    private void createFixtureDatabase(List<FixtureCollection> collections) {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DATABASE_NAME);
        FixtureDatabaseHelper helper = new FixtureDatabaseHelper(context, mDatabaseVersion);
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean hasYearsAndFlags = mDatabaseVersion >= GenerateUpgradeFixtures.FIRST_VERSION_WITH_STORED_OPTIONS;
        boolean hasFlagStrings = mDatabaseVersion >= 22;
        boolean hasSortOrder = mDatabaseVersion >= 17;
        boolean hasImageId = mDatabaseVersion >= 21;

        String infoTable = "CREATE TABLE collection_info (_id integer primary key,"
                + " name text not null,"
                + " coinType text not null,"
                + " total integer,"
                + " display integer default 0,"
                + " displayOrder integer";
        if (hasFlagStrings) {
            infoTable += ", startYear integer default 0, endYear integer default 0,"
                    + " showMintMarksStr text not null default '',"
                    + " showCheckboxesStr text not null default ''";
        } else if (hasYearsAndFlags) {
            infoTable += ", startYear integer default 0, endYear integer default 0,"
                    + " showMintMarks integer default 0, showCheckboxes integer default 0";
        }
        db.execSQL(infoTable + ");");

        db.beginTransaction();
        try {
            for (int i = 0; i < collections.size(); i++) {
                FixtureCollection collection = collections.get(i);
                String coinTable = "CREATE TABLE [" + collection.name + "] (_id integer primary key,"
                        + " coinIdentifier text not null,"
                        + " coinMint text,"
                        + " inCollection integer,"
                        + " advGradeIndex integer default 0,"
                        + " advQuantityIndex integer default 0,"
                        + " advNotes text default \"\"";
                if (hasSortOrder) {
                    coinTable += ", sortOrder integer not null, customCoin integer default 0";
                }
                if (hasImageId) {
                    coinTable += ", imageId integer default -1";
                }
                db.execSQL(coinTable + ");");

                for (int j = 0; j < collection.coins.size(); j++) {
                    Object[] coin = collection.coins.get(j);
                    ContentValues values = new ContentValues();
                    values.put("coinIdentifier", (String) coin[0]);
                    values.put("coinMint", (String) coin[1]);
                    values.put("inCollection", isSeededAsCollected(j) ? 1 : 0);
                    if (hasSortOrder) {
                        values.put("sortOrder", (Integer) coin[3]);
                        values.put("customCoin", 0);
                    }
                    if (hasImageId) {
                        values.put("imageId", (Integer) coin[2]);
                    }
                    assertTrue(db.insert("[" + collection.name + "]", null, values) != -1);
                }

                ContentValues values = new ContentValues();
                values.put("name", collection.name);
                values.put("coinType", collection.coinType);
                values.put("total", collection.coins.size());
                values.put("display", 0);
                values.put("displayOrder", i);
                if (hasYearsAndFlags) {
                    values.put("startYear", collection.stored.optInt("startYear"));
                    values.put("endYear", collection.stored.optInt("endYear"));
                    long mintMarkFlags = collection.stored.optLong("mintMarkFlags");
                    long checkboxFlags = collection.stored.optLong("checkboxFlags");
                    if (hasFlagStrings) {
                        values.put("showMintMarksStr", Long.toString(mintMarkFlags));
                        values.put("showCheckboxesStr", Long.toString(checkboxFlags));
                    } else {
                        values.put("showMintMarks", (int) mintMarkFlags);
                        values.put("showCheckboxes", (int) checkboxFlags);
                    }
                }
                assertTrue(db.insert("collection_info", null, values) != -1);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
        helper.close();
    }

    @Test
    public void test_upgradedCollectionsMatchNewOnes() {
        List<FixtureCollection> fixtureCollections = readFixture();
        createFixtureDatabase(fixtureCollections);

        Map<FixtureCollection, String> differences = new LinkedHashMap<>();

        // Opening the app's database runs onUpgrade() from the fixture's version
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                ArrayList<CollectionListInfo> upgradedInfos = new ArrayList<>();
                ArrayList<String> skippedNames = new ArrayList<>();
                activity.mDbAdapter.getAllTables(upgradedInfos, skippedNames);
                assertTrue("Collections skipped as unrecognized: " + skippedNames, skippedNames.isEmpty());
                assertEquals(fixtureCollections.size(), upgradedInfos.size());

                for (int i = 0; i < fixtureCollections.size(); i++) {
                    FixtureCollection fixture = fixtureCollections.get(i);
                    CollectionListInfo upgradedInfo = upgradedInfos.get(i);
                    assertEquals(fixture.name, upgradedInfo.getName());
                    ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(fixture.name, true);
                    String difference = findDifference(activity.getResources(), fixture, upgradedInfo, dbCoins);
                    if (difference != null) {
                        differences.put(fixture, difference);
                    }
                }
            });
        }

        checkAgainstKnownDifferences(differences);
    }

    /**
     * Compares an upgraded collection with a new one made with its stored options
     *
     * @return a description of the first difference, or null if there is none
     */
    private static String findDifference(Resources res, FixtureCollection fixture,
                                         CollectionListInfo upgradedInfo, ArrayList<CoinSlot> dbCoins) {
        ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(upgradedInfo);
        ParcelableHashMap defaults = new ParcelableHashMap();
        upgradedInfo.getCollectionObj().getCreationParameters(defaults);

        // Options added after the fixture's version: the user never saw them, so the upgrade
        // must leave them as a new collection has them. They're matched by label, since a
        // collection type can reassign its numbered option slots between versions.
        for (String key : defaults.keySet()) {
            Object labelId = defaults.get(key + "StringId");
            if (!(defaults.get(key) instanceof Boolean) || !(labelId instanceof Integer)) {
                continue;
            }
            String label = res.getResourceEntryName((Integer) labelId);
            if (!fixture.labels.containsValue(label) && !defaults.get(key).equals(parameters.get(key))) {
                return "option " + label + ", added after V" + fixture.databaseVersion + ", is "
                        + parameters.get(key) + "; new collections default to " + defaults.get(key);
            }
        }

        // A user who didn't choose a date range took the whole series, so they should have
        // the whole series as it stands today. Comparing against the stored years alone would
        // miss an upgrade that skipped a year's coins, since it would skip updating the
        // stored end year too. Before V15 the years weren't stored: the upgrade infers them
        // from the first and last coins, which is narrower when the chosen mints didn't strike
        // those years (e.g. D-only Peace dollars, 1922-1934), so only the coins are compared.
        if (parameters.containsKey(CoinPageCreator.OPT_STOP_YEAR) && !fixture.isOn(CoinPageCreator.OPT_EDIT_DATE_RANGE)) {
            Object startYear = defaults.get(CoinPageCreator.OPT_START_YEAR);
            Object stopYear = defaults.get(CoinPageCreator.OPT_STOP_YEAR);
            if (fixture.stored != null && (!stopYear.equals(upgradedInfo.getEndYear())
                    || !startYear.equals(upgradedInfo.getStartYear()))) {
                return "stored years " + upgradedInfo.getStartYear() + "-" + upgradedInfo.getEndYear()
                        + ", the whole series is " + startYear + "-" + stopYear;
            }
            parameters.put(CoinPageCreator.OPT_START_YEAR, startYear);
            parameters.put(CoinPageCreator.OPT_STOP_YEAR, stopYear);
        }

        ArrayList<CoinSlot> expected = new ArrayList<>();
        upgradedInfo.getCollectionObj().populateCollectionLists(parameters, expected);

        int common = Math.min(expected.size(), dbCoins.size());
        for (int i = 0; i < common; i++) {
            CoinSlot want = expected.get(i);
            CoinSlot got = dbCoins.get(i);
            if (!want.getIdentifier().equals(got.getIdentifier()) || !want.getMint().equals(got.getMint())
                    || want.getImageId() != got.getImageId()) {
                return "coin " + i + " is " + describe(got) + ", a new collection has " + describe(want);
            }
        }
        if (expected.size() != dbCoins.size()) {
            return dbCoins.size() + " coins, a new collection has " + expected.size()
                    + (dbCoins.size() > common ? "; first extra " + describe(dbCoins.get(common))
                    : "; first missing " + describe(expected.get(common)));
        }
        if (upgradedInfo.getMax() != dbCoins.size()) {
            return "stored total " + upgradedInfo.getMax() + " for " + dbCoins.size() + " coins";
        }

        // Collected status. Rows keep their _id through an upgrade, and the fixture's rows
        // were inserted as _id 1, 2, ... An original row keeps its status, except that a
        // row an upgrade merged duplicates into may take a removed duplicate's status. A
        // collected row may only be removed if such a merge kept it, and rows the upgrade
        // added start out not collected.
        Map<String, Boolean> survivingCollected = new HashMap<>();
        Map<Long, CoinSlot> surviving = new HashMap<>();
        for (CoinSlot coin : dbCoins) {
            surviving.put(coin.getDatabaseId(), coin);
            survivingCollected.merge(coinKey(coin.getIdentifier(), coin.getMint()), coin.isInCollection(),
                    Boolean::logicalOr);
        }
        Map<String, Boolean> removedCollected = new HashMap<>();
        for (int j = 0; j < fixture.coins.size(); j++) {
            Object[] coin = fixture.coins.get(j);
            if (!surviving.containsKey((long) (j + 1)) && isSeededAsCollected(j)) {
                String key = coinKey((String) coin[0], (String) coin[1]);
                if (!Boolean.TRUE.equals(survivingCollected.get(key))) {
                    return "the upgrade removed collected coin " + key;
                }
                removedCollected.put(key, true);
            }
        }
        for (CoinSlot coin : dbCoins) {
            int index = (int) coin.getDatabaseId() - 1;
            boolean original = index < fixture.coins.size();
            boolean wasCollected = original && isSeededAsCollected(index);
            boolean mayBeCollected = wasCollected || (original
                    && removedCollected.containsKey(coinKey(coin.getIdentifier(), coin.getMint())));
            if (coin.isInCollection() ? !mayBeCollected : wasCollected) {
                return describe(coin) + (original ? "" : ", added by the upgrade,") + " is "
                        + (coin.isInCollection() ? "" : "not ") + "collected after the upgrade";
            }
        }

        if (fixture.stored != null) {
            long mintMarkFlags = fixture.stored.optLong("mintMarkFlags");
            long checkboxFlags = fixture.stored.optLong("checkboxFlags");
            if ((upgradedInfo.getMintMarkFlagsAsLong() & mintMarkFlags) != mintMarkFlags) {
                return "mint mark flags " + upgradedInfo.getMintMarkFlagsAsLong()
                        + " lost some of the stored " + mintMarkFlags;
            }
            if ((upgradedInfo.getCheckboxFlagsAsLong() & checkboxFlags) != checkboxFlags) {
                return "checkbox flags " + upgradedInfo.getCheckboxFlagsAsLong()
                        + " lost some of the stored " + checkboxFlags;
            }
        }
        return null;
    }

    private static String coinKey(String identifier, String mint) {
        return "\"" + identifier + "\" \"" + mint + "\"";
    }

    private static String describe(CoinSlot coin) {
        return "\"" + coin.getIdentifier() + "\" \"" + coin.getMint() + "\" image " + coin.getImageId();
    }

    private void checkAgainstKnownDifferences(Map<FixtureCollection, String> differences) {
        StringBuilder problems = new StringBuilder();
        Map<KnownDifference, Integer> matchCounts = new HashMap<>();
        for (Map.Entry<FixtureCollection, String> entry : differences.entrySet()) {
            KnownDifference known = null;
            for (KnownDifference candidate : KNOWN) {
                if (candidate.appliesTo(entry.getKey())) {
                    known = candidate;
                    break;
                }
            }
            if (known != null) {
                matchCounts.merge(known, 1, Integer::sum);
            } else {
                problems.append("\n  Unexpected: ").append(entry.getKey().name).append(" ")
                        .append(entry.getKey().options).append(": ").append(entry.getValue());
            }
        }
        for (KnownDifference known : KNOWN) {
            if (mDatabaseVersion >= known.fromVersion && mDatabaseVersion <= known.toVersion
                    && !matchCounts.containsKey(known)) {
                problems.append("\n  No longer occurs, remove it from KNOWN: ").append(known.reason);
            }
        }
        if (problems.length() > 0) {
            fail("Upgrading from database version " + mDatabaseVersion + ":" + problems);
        }
    }
}
