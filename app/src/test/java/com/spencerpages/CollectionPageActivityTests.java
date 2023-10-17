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

import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
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
                    activity.setInternalStateFromCollectionIndex(index, parameters);
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

                    if (activity.mCoinList.size() > 0) {

                        // Make copy of coins
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);

                        int lastIndex = activity.mCoinList.size() - 1;
                        activity.copyCoinSlot(activity.mCoinList.get(lastIndex), lastIndex + 1);

                        // Update coin names
                        activity.updateCoinDetails(activity.mCoinList.get(0), "First Coin", "First");
                        activity.updateCoinDetails(activity.mCoinList.get(1), "Second Coin", "Second");
                        activity.updateCoinDetails(activity.mCoinList.get(2), "Third Coin", "Third");

                        lastIndex = activity.mCoinList.size() - 1;
                        activity.updateCoinDetails(activity.mCoinList.get(lastIndex), "Last Coin", "Last");

                        // Delete coins
                        activity.deleteCoinSlotAtPosition(0);
                        activity.deleteCoinSlotAtPosition(2);
                        int secondToLastIndex = activity.mCoinList.size() - 2;
                        activity.deleteCoinSlotAtPosition(secondToLastIndex);

                        // Add coins
                        activity.copyCoinSlot(activity.mCoinList.get(0), 1);
                        activity.updateCoinDetails(activity.mCoinList.get(0), "First Coin2", "First2");
                        activity.addNewCoin("New Coin", "Coin Mint");
                    }

                    // Check that the copied collection was made correctly in the database
                    ArrayList<CoinSlot> checkCoinList = activity.mDbAdapter.getCoinList(collectionName, true);
                    compareCoinSlotLists(activity.mCoinList, checkCoinList, true);
                    checkCoinSortOrdersUnique(activity.mCoinList);
                    assertEquals(getSortOrderList(activity.mCoinList), getSortOrderList(checkCoinList));
                });
            }
        }
    }
}
