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

import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.ExportImportHelper;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * Generator for V23 JSON fixture files used by CollectionUpgradeAllParamsTests.
 *
 * <p>This test is @Ignore'd — it is only run manually when fixture files need
 * to be regenerated (e.g., after a new database version is released). It must
 * be run on the branch whose DATABASE_VERSION matches the target fixture version.
 * For example, to generate V23 fixtures, run this on the main branch where
 * DATABASE_VERSION == 23.
 *
 * <p>Output directory: app/src/test/data/v23-upgrades/
 *
 * <p>Generated files:
 * <ul>
 *   <li>v23-default-params.json — all collection types with default parameters</li>
 *   <li>v23-all-params-enabled.json — all types with all Boolean params TRUE</li>
 *   <li>v23-alternating-A.json — all types with alternating FALSE,TRUE,FALSE,...</li>
 *   <li>v23-alternating-B.json — all types with alternating TRUE,FALSE,TRUE,...</li>
 *   <li>v23-semiq-proofs.json — 9 SemiQ collections with proof marks enabled</li>
 * </ul>
 */
@Ignore("Run manually to regenerate V23 fixture files")
@RunWith(RobolectricTestRunner.class)
public class GenerateV23Fixtures extends BaseTestCase {

    // Gradle test runner CWD is the app/ directory
    private static final String OUTPUT_DIR = "src/test/data/v23-upgrades";

    /**
     * Generates all V23 fixture files. Run this single test to regenerate everything.
     */
    @Test
    public void generateAllFixtures() {
        new File(OUTPUT_DIR).mkdirs();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                assertTrue(setEnabledPermissions(activity));

                generateFixture(activity, "v23-default-params.json", this::applyDefaultParams);
                generateFixture(activity, "v23-all-params-enabled.json", this::applyAllEnabled);
                generateFixture(activity, "v23-alternating-A.json",
                        (collection, params) -> applyAlternating(params, false));
                generateFixture(activity, "v23-alternating-B.json",
                        (collection, params) -> applyAlternating(params, true));
            });
        }
    }

    @FunctionalInterface
    interface ParamApplier {
        void apply(CollectionInfo collection, ParcelableHashMap parameters);
    }

    /**
     * Creates all collections using the given parameter strategy and exports to JSON.
     */
    private void generateFixture(MainActivity activity, String filename, ParamApplier applier) {
        // Delete any existing collections
        deleteAllCollections(activity);
        activity.updateCollectionListFromDatabase();

        // Create one collection of each type with the specified params
        int displayOrder = 0;
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            ParcelableHashMap parameters = new ParcelableHashMap();
            collectionInfo.getCreationParameters(parameters);
            applier.apply(collectionInfo, parameters);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            collectionInfo.populateCollectionLists(parameters, coinList);

            // Build CollectionListInfo with proper flags from params
            long mintMarkFlags = CoinPageCreator.getMintMarkFlagsFromParameters(parameters);
            long checkboxFlags = CoinPageCreator.getCheckboxFlagsFromParameters(parameters);
            Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
            Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);

            CollectionListInfo collectionListInfo = new CollectionListInfo(
                    collectionInfo.getCoinType(),
                    coinList.size(),
                    0,
                    MainApplication.getIndexFromCollectionNameStr(collectionInfo.getCoinType()),
                    0,
                    (startYear != null) ? startYear : 0,
                    (stopYear != null) ? stopYear : 0,
                    Long.toString(mintMarkFlags),
                    Long.toString(checkboxFlags));

            createNewTable(activity, collectionListInfo, coinList, displayOrder++);
        }
        activity.updateCollectionListFromDatabase();

        // Export to JSON
        File outputFile = new File(OUTPUT_DIR, filename);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            ExportImportHelper helper = new ExportImportHelper(activity.mRes, activity.mDbAdapter);
            helper.exportCollectionsToJson(outputStream, OUTPUT_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write fixture: " + filename, e);
        }
    }

    // --- Parameter strategies ---

    private void applyDefaultParams(CollectionInfo collection, ParcelableHashMap parameters) {
        // Use defaults from getCreationParameters — no changes needed
    }

    private void applyAllEnabled(CollectionInfo collection, ParcelableHashMap parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                entry.setValue(Boolean.TRUE);
            }
        }
    }

    private void applyAlternating(ParcelableHashMap parameters, boolean startWithTrue) {
        boolean nextValue = startWithTrue;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                entry.setValue(nextValue);
                nextValue = !nextValue;
            }
        }
    }
}
