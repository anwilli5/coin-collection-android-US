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

import static com.spencerpages.MainApplication.ADVANCED_COLLECTIONS;
import static com.spencerpages.MainApplication.BASIC_COLLECTIONS;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.MainApplication.MORE_COLLECTIONS;
import static com.spencerpages.MainApplication.getIndexFromCollectionClass;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.CollectionPage;
import com.coincollection.helper.ParcelableHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class CollectionPageActivityTests extends BaseTestCase {

    private final ArrayList<FullCollection> mCollectionList = new ArrayList<>();

    @Before
    public void databaseSetup() {
        try (ActivityScenario<CoinPageCreator> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class)
                        .putExtra(CoinPageCreator.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS) {
                    activity.mCoinList = new ArrayList<>();
                    ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                    int index = info.getCollectionTypeIndex();
                    activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                    activity.createOrUpdateCoinListForAsyncThread();
                    // Create the collection in the database
                    activity.mDbAdapter.createAndPopulateNewTable(info, 0, activity.mCoinList);
                    mCollectionList.add(new FullCollection(info, activity.mCoinList, 0));
                }
            });
        }
    }

    /**
     * Test updating coin details
     */
    @Test
    public void test_coinActions() {
        for (FullCollection collection : mCollectionList) {
            String collectionName = collection.mCollectionListInfo.getName();
            int coinTypeIdx = collection.mCollectionListInfo.getCollectionTypeIndex();
            try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(
                    new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                            .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, coinTypeIdx)
                            .putExtra(CollectionPage.COLLECTION_NAME, collectionName))) {
                scenario.onActivity(activity -> {

                    if (!activity.mCoinList.isEmpty()) {

                        // Make copy of coins
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);

                        int lastIndex = activity.mCoinList.size() - 1;
                        activity.copyCoinSlot(activity.mCoinList.get(lastIndex), lastIndex + 1);

                        // Update coin names
                        activity.updateCoinDetails(activity.mCoinList.get(0), "First Coin", "First", -1);
                        activity.updateCoinDetails(activity.mCoinList.get(1), "Second Coin", "Second", -1);
                        activity.updateCoinDetails(activity.mCoinList.get(2), "Third Coin", "Third", -1);

                        lastIndex = activity.mCoinList.size() - 1;
                        activity.updateCoinDetails(activity.mCoinList.get(lastIndex), "Last Coin", "Last", -1);

                        // Delete coins
                        activity.deleteCoinSlotAtPosition(0);
                        activity.deleteCoinSlotAtPosition(2);
                        int secondToLastIndex = activity.mCoinList.size() - 2;
                        activity.deleteCoinSlotAtPosition(secondToLastIndex);

                        // Add coins
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);
                        activity.updateCoinDetails(activity.mCoinList.get(0), "First Coin2", "First2", -1);
                        activity.addNewCoin("New Coin", "Coin Mint", -1);
                    }

                    // Check that the copied collection was made correctly in the database
                    ArrayList<CoinSlot> checkCoinList = activity.mDbAdapter.getCoinList(collectionName, true);
                    compareCoinSlotLists(activity.mCoinList, checkCoinList, true, true);
                    checkCoinSortOrdersUnique(activity.mCoinList);
                    assertEquals(getSortOrderList(activity.mCoinList), getSortOrderList(checkCoinList));
                });
            }
        }
    }

    /**
     * Test that all classes listed in COLLECTION_TYPES are included in
     * BASIC_COLLECTIONS and ADVANCED_COLLECTIONS
     */
    @Test
    public void test_collectionTypes() {
        assertEquals(COLLECTION_TYPES.length, (BASIC_COLLECTIONS.length + ADVANCED_COLLECTIONS.length + MORE_COLLECTIONS.length));
        for (Class<?> collectionClass : BASIC_COLLECTIONS) {
            // All basic collections should be in the collection types list
            assertNotEquals(-1, getIndexFromCollectionClass(collectionClass));
        }
        for (Class<?> collectionClass : ADVANCED_COLLECTIONS) {
            // All advanced collections should be in the collection types list
            assertNotEquals(-1, getIndexFromCollectionClass(collectionClass));
        }
        for (Class<?> collectionClass : MORE_COLLECTIONS) {
            // All advanced collections should be in the collection types list
            assertNotEquals(-1, getIndexFromCollectionClass(collectionClass));
        }
        // Each class should be in the collection types list at least and only once
        for (CollectionInfo collectionType : COLLECTION_TYPES) {
            int numFound = 0;
            for (Class<?> collectionClass : BASIC_COLLECTIONS) {
                if (collectionType.getClass() == collectionClass) {
                    numFound++;
                }
            }
            for (Class<?> collectionClass : ADVANCED_COLLECTIONS) {
                if (collectionType.getClass() == collectionClass) {
                    numFound++;
                }
            }
            for (Class<?> collectionClass : MORE_COLLECTIONS) {
                if (collectionType.getClass() == collectionClass) {
                    numFound++;
                }
            }
            assertEquals(1, numFound);
        }
    }

    /**
     * Test coin filter toggle functionality
     */
    @Test
    public void test_coinFilterToggle() {
        // Test with the first collection that has coins
        FullCollection testCollection = mCollectionList.get(0);
        String collectionName = testCollection.mCollectionListInfo.getName();
        int coinTypeIdx = testCollection.mCollectionListInfo.getCollectionTypeIndex();
        
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                        .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, coinTypeIdx)
                        .putExtra(CollectionPage.COLLECTION_NAME, collectionName))) {
            scenario.onActivity(activity -> {
                
                if (!activity.mCoinList.isEmpty()) {
                    int originalSize = activity.mCoinList.size();
                    
                    // Initially, filter should be SHOW_ALL
                    assertEquals(CollectionPage.FILTER_SHOW_ALL, activity.mCoinFilter);
                    assertEquals(originalSize, activity.mCoinList.size());
                    
                    // Toggle some coins to collected status
                    if (originalSize > 2) {
                        activity.mCoinList.get(0).setInCollection(true);
                        activity.mCoinList.get(1).setInCollection(true);
                        activity.mCoinList.get(2).setInCollection(false);
                        
                        // Test SHOW_COLLECTED filter
                        activity.mCoinFilter = CollectionPage.FILTER_SHOW_COLLECTED;
                        activity.applyCurrentFilter();
                        
                        // Should only show collected coins
                        int collectedCount = 0;
                        for (CoinSlot coin : activity.mCoinList) {
                            if (coin.isInCollection()) {
                                collectedCount++;
                            }
                        }
                        assertEquals(collectedCount, activity.mCoinList.size());
                        
                        // Test SHOW_MISSING filter
                        activity.mCoinFilter = CollectionPage.FILTER_SHOW_MISSING;
                        activity.applyCurrentFilter();
                        
                        // Should only show missing coins
                        int missingCount = 0;
                        for (CoinSlot coin : activity.mCoinList) {
                            if (!coin.isInCollection()) {
                                missingCount++;
                            }
                        }
                        assertEquals(missingCount, activity.mCoinList.size());
                        
                        // Test back to SHOW_ALL
                        activity.mCoinFilter = CollectionPage.FILTER_SHOW_ALL;
                        activity.applyCurrentFilter();
                        assertEquals(originalSize, activity.mCoinList.size());
                    }
                }
            });
        }
    }
}
