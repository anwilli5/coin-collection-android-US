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

import static com.coincollection.CollectionListInfo.COL_DISPLAY_ORDER;
import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.coincollection.MainActivity;
import com.coincollection.ReorderAdapter;
import com.coincollection.ReorderCollections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTests extends BaseTestCase {

    /**
     * Test building the attribution string
     */
    @Test
    public void test_buildAttributions() {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> assertNotEquals("", activity.buildInfoText()));
        }
    }

    /**
     * Test copying collections
     */
    @Test
    public void test_copyCollections() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                for (CollectionInfo coinType : MainApplication.COLLECTION_TYPES) {
                    for (FullCollection collection : getRandomTestScenarios(coinType, 2)) {
                        String collectionName = collection.mCollectionListInfo.getName();

                        // Create the collection in the database
                        activity.mDbAdapter.createAndPopulateNewTable(collection.mCollectionListInfo,
                                collection.mDisplayOrder, collection.mCoinList);
                        activity.updateCollectionListFromDatabase();

                        // Make a copy
                        String newDbName = collectionName + " Copy";
                        activity.startCopyCollectionTask(collectionName);
                        CollectionListInfo copiedCollectionListInfo = collection.mCollectionListInfo.copy(newDbName);

                        // Drop the original
                        activity.mDbAdapter.dropCollectionTable(collectionName);

                        // Check that the copied collection was made correctly in the database
                        compareCollectionWithDb(activity, copiedCollectionListInfo,
                                collection.mCoinList, collection.mDisplayOrder);

                        // Delete the collection from the database
                        activity.mDbAdapter.dropCollectionTable(newDbName);
                    }
                }
            });
        }
    }

    /**
     * Test that a copy is placed right after its source, that a copy of the copy is named
     * from the base name and placed after it, and that the display orders are renumbered
     */
    @Test
    public void test_copyCollectionPlacedAfterSource() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupCollectionsWithNames(activity, new ArrayList<>(Arrays.asList("A", "B", "C")));

                activity.startCopyCollectionTask("A");
                assertEquals(Arrays.asList("A", "A Copy", "B", "C"), getCheckedDisplayOrder(activity));

                activity.startCopyCollectionTask("A Copy");
                assertEquals(Arrays.asList("A", "A Copy", "A Copy1", "B", "C"),
                        getCheckedDisplayOrder(activity));

                // The list shown to the user was refreshed once the task finished
                assertFalse(activity.isDatabaseWriteInProgress());
                assertEquals(5, activity.mNumberOfCollections);
                assertEquals("A Copy1", activity.mCollectionListEntries.get(2).getName());
            });
        }
    }

    /**
     * Test that deleting a collection drops it and renumbers the ones after it
     */
    @Test
    public void test_deleteCollectionRenumbersTheRest() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupCollectionsWithNames(activity, new ArrayList<>(Arrays.asList("A", "B", "C")));

                activity.startDeleteCollectionTask("B");
                assertEquals(Arrays.asList("A", "C"), getCheckedDisplayOrder(activity));
                assertEquals(-1, activity.mDbAdapter.checkCollectionName("B"));

                assertFalse(activity.isDatabaseWriteInProgress());
                assertEquals(2, activity.mNumberOfCollections);
            });
        }
    }

    /**
     * Test that a delete that fails changes nothing, and that the list can be read
     * again afterward
     */
    @Test
    public void test_failedDeleteLeavesCollectionsUnchanged() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                setupCollectionsWithNames(activity, new ArrayList<>(Arrays.asList("A", "B")));

                activity.startDeleteCollectionTask("Missing");
                assertEquals(Arrays.asList("A", "B"), getCheckedDisplayOrder(activity));
                assertFalse(activity.isDatabaseWriteInProgress());
            });
        }
    }

    /**
     * Gets the collection names in display order, checking that the display orders run
     * from 0 with no gaps or repeats
     *
     * @param activity activity that can be used to access the database
     * @return collection names in display order
     */
    private List<String> getCheckedDisplayOrder(MainActivity activity) {
        ArrayList<String> names = new ArrayList<>();
        try (SQLiteDatabase db = new DatabaseHelper(activity).getReadableDatabase();
             Cursor cursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_NAME, COL_DISPLAY_ORDER},
                     null, null, null, null, COL_DISPLAY_ORDER)) {
            while (cursor.moveToNext()) {
                assertEquals("Display order of " + cursor.getString(0), names.size(), cursor.getInt(1));
                names.add(cursor.getString(0));
            }
        }
        return names;
    }

    /**
     * Launch the reorder fragment
     */
    @Test
    public void test_reorderFragment() {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                // Add collections to the database
                ArrayList<Integer> indexPositions = new ArrayList<>();
                for (int i = 0; i < COLLECTION_LIST_INFO_SCENARIOS.length; i++) {
                    CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[i];
                    activity.mDbAdapter.createAndPopulateNewTable(info, i, null);
                    indexPositions.add(i);
                }
                activity.updateCollectionListFromDatabase();

                // Launch the reorder fragment
                ReorderCollections reorderFragment = activity.launchReorderFragment();
                assertNotNull(reorderFragment);
                activity.getSupportFragmentManager().executePendingTransactions();

                // Perform a few reorders
                ReorderAdapter adapter = reorderFragment.mAdapter;
                assertNotNull(adapter);
                for (int i = 0; i < 20; i++) {
                    int fromIndex = random.nextInt(COLLECTION_LIST_INFO_SCENARIOS.length);
                    int toIndex = random.nextInt(COLLECTION_LIST_INFO_SCENARIOS.length);
                    adapter.onItemMove(fromIndex, toIndex);
                    Collections.swap(indexPositions, fromIndex, toIndex);
                }
                activity.handleCollectionsReordered(adapter.mItems);

                // Check the order
                ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(collectionListEntries);
                assertNotNull(collectionListEntries);
                assertEquals(collectionListEntries.size(), COLLECTION_LIST_INFO_SCENARIOS.length);
                for (int i = 0; i < collectionListEntries.size(); i++) {
                    compareCollectionListInfos(collectionListEntries.get(i),
                            COLLECTION_LIST_INFO_SCENARIOS[indexPositions.get(i)]);
                }
            });
        }
    }

    /**
     * Make sure the doesCollectionTypeUseDates method works correctly
     */
    @Test
    public void test_verifyDoesCollectionTypeUseDates() {
        for (CollectionInfo coinType : MainApplication.COLLECTION_TYPES) {
            if (CollectionListInfo.doesCollectionTypeUseDates(coinType.getCoinType())) {
                assertNotEquals(0, coinType.getStartYear());
                assertNotEquals(0, coinType.getStopYear());
            } else {
                assertEquals(0, coinType.getStartYear());
                assertEquals(0, coinType.getStopYear());
            }
        }
    }
}
