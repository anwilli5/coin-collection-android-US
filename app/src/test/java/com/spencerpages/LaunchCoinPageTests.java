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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.BaseActivity;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class LaunchCoinPageTests extends BaseTestCase {

    private final CollectionInfo mCoinTypeObj;

    public LaunchCoinPageTests(CollectionInfo coinTypeObj) {
        mCoinTypeObj = coinTypeObj;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static List<?> getCoinTypeObj() {
        return Arrays.asList(MainApplication.COLLECTION_TYPES);
    }

    /**
     * Test launching collections
     */
    @Test
    public void test_launchCoinPage() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                for (FullCollection scenario1 : getRandomTestScenarios(mCoinTypeObj, 1)) {
                    // Create the collection in the database
                    activity.mDbAdapter.createAndPopulateNewTable(scenario1.mCollectionListInfo,
                            scenario1.mDisplayOrder, scenario1.mCoinList);
                    activity.updateCollectionListFromDatabase();

                    // Launch the collection
                    Intent intent = activity.launchCoinPageActivity(scenario1.mCollectionListInfo);
                    assertNotNull(intent);
                    CollectionPage coinActivity = Robolectric.buildActivity(CollectionPage.class, intent).get();
                    assertNotNull(coinActivity);
                    coinActivity.onCreate(null);

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }

    /**
     * Test that recreating a CollectionPage while the database is closed (as
     * happens when the OS kills the app process and the user returns from
     * Recents) does not crash. Per WI-02, onCreate must defer all database
     * access until the asynchronous open completes.
     */
    @Test
    public void test_launchCoinPageWithClosedDatabase() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                for (FullCollection scenario1 : getRandomTestScenarios(mCoinTypeObj, 1)) {
                    // Create the collection in the database
                    activity.mDbAdapter.createAndPopulateNewTable(scenario1.mCollectionListInfo,
                            scenario1.mDisplayOrder, scenario1.mCoinList);
                    activity.updateCollectionListFromDatabase();

                    // Launch the collection
                    Intent intent = activity.launchCoinPageActivity(scenario1.mCollectionListInfo);
                    assertNotNull(intent);

                    // Simulate process death: the shared database is not open when
                    // the activity is recreated
                    activity.mDbAdapter.close();

                    // Use the real (asynchronous) task path for the database open.
                    // The synchronous unit-test seam would reopen the database inside
                    // BaseActivity.onCreate and mask a regression in the deferral.
                    CollectionPage coinActivity;
                    BaseActivity.isUnitTest = false;
                    try {
                        coinActivity = Robolectric.buildActivity(CollectionPage.class, intent).get();
                        assertNotNull(coinActivity);
                        // onCreate must not touch the database directly in this state,
                        // otherwise it crashes with a NullPointerException
                        coinActivity.onCreate(null);
                    } finally {
                        BaseActivity.isUnitTest = true;
                    }

                    // The database-dependent setup must have been deferred, not run
                    // against the closed database
                    assertNull(coinActivity.mCoinSlotAdapter);

                    // Complete the open and deliver the callback as the task runner
                    // would, and verify the deferred setup then runs
                    activity.mDbAdapter.open();
                    coinActivity.asyncProgressOnPostExecute(BaseActivity.TASK_OPEN_DATABASE, "");
                    assertNotNull(coinActivity.mCoinSlotAdapter);

                    // Detach the activity from its task runner so the still-queued
                    // database-open task becomes a no-op when the looper idles
                    coinActivity.onDestroy();

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }
}
