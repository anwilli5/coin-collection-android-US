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

import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;
import com.coincollection.ReorderAdapter;
import com.coincollection.ReorderCollections;
import com.spencerpages.MainApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;

import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class MainActivityTests extends BaseTestCase {

    /**
     * Test building the attribution string
     */
    @Test
    public void test_buildAttributions() {

        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    assertNotEquals("", activity.buildInfoText());
                }
            });
        }
    }

    /**
     * Test copying collections
     */
    @Test
    public void test_copyCollections() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    for (CollectionInfo coinType : MainApplication.COLLECTION_TYPES) {
                        for (FullCollection scenario : getRandomTestScenarios(activity, coinType, 2)) {
                            String collectionName = scenario.mCollectionListInfo.getName();

                            // Create the collection in the database
                            activity.mDbAdapter.createAndPopulateNewTable(scenario.mCollectionListInfo,
                                    scenario.mDisplayOrder, scenario.mCoinList);
                            activity.updateCollectionListFromDatabase();

                            // Make a copy
                            String newDbName = collectionName + " Copy";
                            activity.copyCollection(collectionName);
                            CollectionListInfo copiedCollectionListInfo = scenario.mCollectionListInfo.copy(newDbName);

                            // Drop the original
                            activity.mDbAdapter.dropCollectionTable(collectionName);

                            // Check that the copied collection was made correctly in the database
                            compareCollectionWithDb(activity, copiedCollectionListInfo,
                                    scenario.mCoinList, scenario.mDisplayOrder);

                            // Delete the collection from the database
                            activity.mDbAdapter.dropCollectionTable(newDbName);
                        }
                    }
                }
            });
        }
    }

    /**
     * Launch the reorder fragment
     */
    @Test
    public void test_reorderFragment() {

        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
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
                }
            });
        }
    }

    /**
     * Test launching collections
     */
    @Test
    public void test_launchCoinPage() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    // Create some collections
                    ArrayList<CollectionListInfo> allCollections = new ArrayList<>();
                    for (CollectionInfo coinType : MainApplication.COLLECTION_TYPES) {
                        for (FullCollection scenario : getRandomTestScenarios(activity, coinType, 1)) {
                            // Create the collection in the database
                            activity.mDbAdapter.createAndPopulateNewTable(scenario.mCollectionListInfo,
                                    scenario.mDisplayOrder, scenario.mCoinList);
                            allCollections.add(scenario.mCollectionListInfo);
                        }
                    }
                    activity.updateCollectionListFromDatabase();

                    // Launch the collections
                    for (CollectionListInfo collectionListInfo : allCollections) {
                        Intent intent = activity.launchCoinPageActivity(collectionListInfo);
                        assertNotNull(intent);
                        CollectionPage coinActivity = Robolectric.buildActivity(CollectionPage.class, intent).get();
                        assertNotNull(coinActivity);
                        coinActivity.onCreate(null);
                    }
                }
            });
        }
    }
}
