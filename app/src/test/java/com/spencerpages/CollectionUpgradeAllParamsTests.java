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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.ExportImportHelper;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.collections.LincolnCents;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Upgrade tests using static V23 JSON fixture files. Each fixture contains all
 * collection types exported from a real V23 database with specific parameter
 * configurations. On import, the V23→V24 upgrade runs automatically. The test
 * validates each upgraded collection against a fresh populateCollectionLists.
 *
 * <p>Fixture files are in app/src/test/data/v23-upgrades/ and were generated
 * by running GenerateV23Fixtures on the main branch (DATABASE_VERSION == 23).
 *
 * <p>This approach is fully independent of upgrade code — the fixtures are
 * frozen V23 snapshots that never change.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionUpgradeAllParamsTests extends BaseTestCase {

    private static final String FIXTURE_DIR = "src/test/data/v23-upgrades";

    // Collections with known upgrade vs. fresh-creation mismatches.
    // LincolnCents: addFromYear uses "P" for Philadelphia, but pennies use "" since
    // the P mint mark was never on any pennies. This is a pre-existing bug across all
    // upgrade versions (V4–V24). See https://github.com/anwilli5/coin-collection-android-US/issues/366
    private static final Set<String> KNOWN_UPGRADE_MISMATCHES = new HashSet<>();
    static {
        KNOWN_UPGRADE_MISMATCHES.add(LincolnCents.COLLECTION_TYPE);
    }

    /**
     * Import a V23 fixture, validate each collection after upgrade.
     * Reconstructs the original parameters from stored flags using
     * getParametersFromCollectionListInfo, then generates a fresh coin list
     * to compare against the upgraded database.
     */
    private void importAndValidateFixture(String fixtureFilename) {
        // Delete any leftover database to ensure clean state
        ApplicationProvider.getApplicationContext().deleteDatabase(MainApplication.DATABASE_NAME);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                // Import the V23 fixture (triggers V23→V24 upgrade automatically)
                ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
                File fixtureFile = new File(FIXTURE_DIR, fixtureFilename);
                InputStream inputStream;
                try {
                    inputStream = new FileInputStream(fixtureFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Fixture not found: " + fixtureFile, e);
                }
                String importResult = helper.importCollectionsFromJson(inputStream);
                assertEquals("", importResult);

                // Validate each imported collection
                ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(collectionListEntries);
                assertNotNull(collectionListEntries);
                assertTrue("No collections imported", collectionListEntries.size() > 0);

                for (CollectionListInfo importedInfo : collectionListEntries) {
                    // Skip collections with known pre-existing upgrade mismatches
                    if (KNOWN_UPGRADE_MISMATCHES.contains(importedInfo.getType())) {
                        continue;
                    }

                    // Reconstruct the parameters used to create this collection
                    ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(importedInfo);

                    // Generate a fresh coin list using the reconstructed parameters
                    ArrayList<CoinSlot> expectedCoins = new ArrayList<>();
                    importedInfo.getCollectionObj().populateCollectionLists(parameters, expectedCoins);

                    // Get the upgraded coin list from the database
                    ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(importedInfo.getName(), true);
                    assertNotNull("Coin list null for " + importedInfo.getName(), dbCoins);

                    // Compare
                    assertEquals("Coin count mismatch for " + importedInfo.getName(),
                            expectedCoins.size(), dbCoins.size());
                    for (int i = 0; i < expectedCoins.size(); i++) {
                        assertEquals("Identifier mismatch at index " + i + " for " + importedInfo.getName(),
                                expectedCoins.get(i).getIdentifier(), dbCoins.get(i).getIdentifier());
                        assertEquals("Mint mismatch at index " + i + " for " + importedInfo.getName(),
                                expectedCoins.get(i).getMint(), dbCoins.get(i).getMint());
                        assertEquals("ImageId mismatch at index " + i + " for " + importedInfo.getName(),
                                expectedCoins.get(i).getImageId(), dbCoins.get(i).getImageId());
                    }
                }
            });
        }
    }

    @Test
    public void test_upgradeFromV23DefaultParams() {
        importAndValidateFixture("v23-default-params.json");
    }

    @Test
    public void test_upgradeFromV23AllParamsEnabled() {
        importAndValidateFixture("v23-all-params-enabled.json");
    }

    @Test
    public void test_upgradeFromV23AlternatingA() {
        importAndValidateFixture("v23-alternating-A.json");
    }

    @Test
    public void test_upgradeFromV23AlternatingB() {
        importAndValidateFixture("v23-alternating-B.json");
    }
}
