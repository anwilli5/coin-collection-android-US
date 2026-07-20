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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.BaseActivity;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowDialog;

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

    /**
     * Test that stopping a CollectionPage while the asynchronous database open
     * is still pending (so setContentView() and the coin list load haven't run
     * yet) does not crash in onSaveInstanceState(), and that restoring from the
     * resulting bundle loads the coin list from the database instead of the
     * (empty) saved state.
     */
    @Test
    public void test_saveInstanceStateBeforeDatabaseOpenCompletes() {
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

                    // Recreate the activity with the database closed so the
                    // database-dependent setup (including setContentView) is deferred
                    CollectionPage coinActivity;
                    Bundle outState = new Bundle();
                    BaseActivity.isUnitTest = false;
                    try {
                        coinActivity = Robolectric.buildActivity(CollectionPage.class, intent).get();
                        assertNotNull(coinActivity);
                        coinActivity.onCreate(null);
                        assertNull(coinActivity.mCoinSlotAdapter);

                        // Stop the activity before the async database open completes.
                        // The list layout hasn't been inflated, so this used to crash
                        // with a NullPointerException in getAbsListViewPosition()
                        coinActivity.onSaveInstanceState(outState);
                    } finally {
                        BaseActivity.isUnitTest = true;
                    }
                    coinActivity.onDestroy();

                    // Restore a new activity from the saved state. The bundle has no
                    // coin list (it was never loaded), so the restore path must fall
                    // back to loading it from the database
                    activity.mDbAdapter.open();
                    CollectionPage restoredActivity = Robolectric.buildActivity(CollectionPage.class, intent).get();
                    assertNotNull(restoredActivity);
                    restoredActivity.onCreate(outState);
                    assertNotNull(restoredActivity.mCoinSlotAdapter);
                    assertEquals(scenario1.mCoinList.size(), restoredActivity.mOriginalCoinList.size());
                    restoredActivity.onDestroy();

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }

    /**
     * Test that deleteCoinSlotAtPosition() ignores positions outside the
     * current coin list instead of crashing with an IndexOutOfBoundsException
     * (a stale position can arrive from an actions dialog that was opened
     * before the list changed).
     */
    @Test
    public void test_deleteCoinSlotAtPositionIgnoresStalePosition() {
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
                    assertNotNull(coinActivity.mCoinSlotAdapter);

                    // Out-of-range positions must be ignored, not crash
                    int coinCount = coinActivity.mCoinList.size();
                    coinActivity.deleteCoinSlotAtPosition(coinCount);
                    coinActivity.deleteCoinSlotAtPosition(-1);
                    assertEquals(coinCount, coinActivity.mCoinList.size());
                    assertEquals(coinCount, coinActivity.mOriginalCoinList.size());

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }

    /**
     * Test that selecting an action from the long-press actions dialog after
     * the coin list has changed (e.g. the coin was deleted while the dialog
     * was open) is ignored instead of crashing or acting on the wrong coin.
     */
    @Test
    public void test_coinSlotActionDialogIgnoresStaleSelection() {
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
                    assertNotNull(coinActivity.mCoinSlotAdapter);
                    int coinCount = coinActivity.mCoinList.size();
                    if (coinCount == 0) {
                        // Some random scenarios produce empty collections -
                        // there is no coin to open the actions dialog on
                        activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                        continue;
                    }
                    int lastPosition = coinCount - 1;

                    // Open the actions dialog for the last coin. Alerts are
                    // suppressed while isUnitTest is set, so clear it to get a
                    // real dialog to click on
                    AlertDialog dialog;
                    BaseActivity.isUnitTest = false;
                    try {
                        coinActivity.promptCoinSlotActions(lastPosition);
                        dialog = (AlertDialog) ShadowDialog.getLatestDialog();
                    } finally {
                        BaseActivity.isUnitTest = true;
                    }
                    assertNotNull(dialog);

                    // The list shrinks while the dialog is still open
                    coinActivity.deleteCoinSlotAtPosition(lastPosition);
                    assertEquals(coinCount - 1, coinActivity.mCoinList.size());

                    // Selecting delete (item 3) used to crash in
                    // ArrayList.remove with the stale position - it must now be
                    // ignored since the coin is no longer in the list
                    dialog.getListView().performItemClick(null, 3, 3);
                    assertEquals(coinCount - 1, coinActivity.mCoinList.size());
                    assertEquals(coinCount - 1, coinActivity.mOriginalCoinList.size());
                    coinActivity.onDestroy();

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }

    /**
     * Test that deleting a coin via the actions dialog acts on the exact
     * tapped instance even when an equal-by-equals duplicate exists (as the
     * copy action creates, since it reuses the identifier and mint). An
     * equals-based position or removal lookup would act on the wrong
     * duplicate, so the delete must be resolved by object identity.
     */
    @Test
    public void test_coinSlotActionDialogDeletesTappedDuplicate() {
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
                    assertNotNull(coinActivity.mCoinSlotAdapter);
                    if (coinActivity.mCoinList.isEmpty()) {
                        // Some random scenarios produce empty collections - there
                        // is no coin to copy and delete
                        activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                        continue;
                    }

                    // Duplicate the first coin so two equal-by-equals coins sit
                    // next to each other (the original at index 0, the copy at
                    // index 1)
                    CoinSlot original = coinActivity.mCoinList.get(0);
                    coinActivity.copyCoinSlot(original, 1);
                    int sizeAfterCopy = coinActivity.mCoinList.size();
                    CoinSlot copy = coinActivity.mCoinList.get(1);
                    assertSame(original, coinActivity.mCoinList.get(0));
                    assertNotSame(original, copy);
                    assertEquals(original, copy);

                    // Open the actions dialog on the second duplicate (the copy)
                    AlertDialog dialog;
                    BaseActivity.isUnitTest = false;
                    try {
                        coinActivity.promptCoinSlotActions(1);
                        dialog = (AlertDialog) ShadowDialog.getLatestDialog();
                    } finally {
                        BaseActivity.isUnitTest = true;
                    }
                    assertNotNull(dialog);

                    // Select delete (item 3) - the tapped copy must be removed,
                    // leaving the original in place at index 0
                    dialog.getListView().performItemClick(null, 3, 3);
                    assertEquals(sizeAfterCopy - 1, coinActivity.mCoinList.size());
                    assertEquals(sizeAfterCopy - 1, coinActivity.mOriginalCoinList.size());
                    assertSame(original, coinActivity.mCoinList.get(0));
                    assertFalse(containsByIdentity(coinActivity.mCoinList, copy));
                    assertFalse(containsByIdentity(coinActivity.mOriginalCoinList, copy));
                    assertTrue(containsByIdentity(coinActivity.mOriginalCoinList, original));
                    coinActivity.onDestroy();

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }

    /**
     * Returns true if the exact CoinSlot instance is present in the list
     * (identity, not equals)
     */
    private static boolean containsByIdentity(List<CoinSlot> coinList, CoinSlot coinSlot) {
        for (CoinSlot currCoinSlot : coinList) {
            if (currCoinSlot == coinSlot) {
                return true;
            }
        }
        return false;
    }
}
