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
package com.coincollection;

import static com.coincollection.dialog.DialogRequests.TAG_LIST_CHOICE;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.dialog.ListChoiceDialogFragment;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.BaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end tests for the coin actions dialog wiring on
 * {@link CollectionPage}: the real {@link ListChoiceDialogFragment} is shown
 * via {@code promptCoinSlotActions()}, an item on its real AlertDialog is
 * clicked, and the selection travels back through the fragment result API to
 * {@code applyCoinSlotAction()}. This covers the payload construction, the
 * fragment arguments, {@code sendResult()}, the result listener registration,
 * and the resolution of the tapped coin by its database id - none of which is
 * exercised by tests that hand-build the result bundle.
 * <p>
 * Uses a single collection type (Lincoln Cents) rather than the full
 * parameterized matrix - the wiring under test is type-independent.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionPageDialogWiringTests extends BaseTestCase {

    // Order of the entries in the coin actions dialog, mirroring the private
    // constants in CollectionPage (TOGGLE, EDIT, COPY, DELETE)
    private static final int ACTION_TOGGLE = 0;
    private static final int ACTION_DELETE = 3;

    // Coin position to open the actions dialog on - not the first coin, so an
    // always-acts-on-index-zero regression would be caught
    private static final int TAP_POSITION = 2;

    /**
     * Runs an action with dialog suppression disabled, so a real dialog is
     * created instead of being skipped for unit tests
     *
     * @param action the action to run
     */
    private static void withDialogsEnabled(Runnable action) {
        boolean wasUnitTest = BaseActivity.isUnitTest;
        BaseActivity.isUnitTest = false;
        try {
            action.run();
        } finally {
            BaseActivity.isUnitTest = wasUnitTest;
        }
    }

    /**
     * Creates the first shared collection scenario (Lincoln Cents) in the
     * database so CollectionPage can load it
     *
     * @return an intent that launches CollectionPage on that collection
     */
    private Intent createCollectionAndGetLaunchIntent() {
        CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
        try (ActivityScenario<CoinPageCreator> creatorScenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class))) {
            creatorScenario.onActivity(activity -> {
                activity.mCoinList = new ArrayList<>();
                ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                int index = info.getCollectionTypeIndex();
                activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                activity.createOrUpdateCoinListForAsyncThread();
                activity.mDbAdapter.createAndPopulateNewTable(info, 0, activity.mCoinList);
            });
        }
        return new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, info.getCollectionTypeIndex())
                .putExtra(CollectionPage.COLLECTION_NAME, info.getName());
    }

    /**
     * Shows the coin actions dialog for {@link #TAP_POSITION} and clicks the
     * given action on the real AlertDialog
     *
     * @param scenario    the CollectionPage scenario
     * @param actionIndex index of the action to click in the list
     */
    private static void openActionsDialogAndClick(ActivityScenario<CollectionPage> scenario,
                                                  int actionIndex) {
        scenario.onActivity(activity ->
                withDialogsEnabled(() -> activity.promptCoinSlotActions(TAP_POSITION)));
        shadowOf(Looper.getMainLooper()).idle();

        scenario.onActivity(activity -> withDialogsEnabled(() -> {
            activity.getSupportFragmentManager().executePendingTransactions();
            Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(TAG_LIST_CHOICE);
            assertNotNull("The coin actions dialog fragment should be shown", fragment);
            assertTrue(fragment instanceof ListChoiceDialogFragment);
            AlertDialog dialog = (AlertDialog) ((ListChoiceDialogFragment) fragment).getDialog();
            assertNotNull("The dialog fragment should have created its AlertDialog", dialog);
            // Click the action the way a user would, so the selection travels
            // through the dialog's own result plumbing
            dialog.getListView().performItemClick(null, actionIndex, actionIndex);
        }));
        shadowOf(Looper.getMainLooper()).idle();
    }

    /**
     * Returns true if a coin with the given database id is present in the list
     *
     * @param coinList   the list to search
     * @param databaseId the coin's database id
     */
    private static boolean containsDatabaseId(List<CoinSlot> coinList, long databaseId) {
        for (CoinSlot coinSlot : coinList) {
            if (coinSlot.getDatabaseId() == databaseId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test that picking delete from the real coin actions dialog deletes the
     * coin the dialog was opened on
     */
    @Test
    public void test_deleteViaRealActionsDialogRemovesTappedCoin() {
        Intent intent = createCollectionAndGetLaunchIntent();
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(intent)) {
            final int[] initialCount = new int[1];
            final long[] tappedDatabaseId = new long[1];
            scenario.onActivity(activity -> {
                initialCount[0] = activity.mCoinList.size();
                assertTrue("The collection should have coins to act on",
                        initialCount[0] > TAP_POSITION);
                tappedDatabaseId[0] = activity.mCoinList.get(TAP_POSITION).getDatabaseId();
            });

            openActionsDialogAndClick(scenario, ACTION_DELETE);

            scenario.onActivity(activity -> {
                assertEquals(initialCount[0] - 1, activity.mCoinList.size());
                assertEquals(initialCount[0] - 1, activity.mOriginalCoinList.size());
                assertFalse("The tapped coin should have been deleted",
                        containsDatabaseId(activity.mCoinList, tappedDatabaseId[0]));
                assertFalse("The tapped coin should have been deleted from the full list",
                        containsDatabaseId(activity.mOriginalCoinList, tappedDatabaseId[0]));
            });
        }
    }

    /**
     * Test that picking toggle from the real coin actions dialog flips the
     * collected state of the coin the dialog was opened on and leaves the
     * others alone
     */
    @Test
    public void test_toggleViaRealActionsDialogFlipsTappedCoin() {
        Intent intent = createCollectionAndGetLaunchIntent();
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(intent)) {
            final int[] initialCount = new int[1];
            final long[] tappedDatabaseId = new long[1];
            final boolean[] wasInCollection = new boolean[3];
            scenario.onActivity(activity -> {
                initialCount[0] = activity.mCoinList.size();
                assertTrue("The collection should have coins to act on",
                        initialCount[0] > TAP_POSITION);
                CoinSlot tappedCoinSlot = activity.mCoinList.get(TAP_POSITION);
                tappedDatabaseId[0] = tappedCoinSlot.getDatabaseId();
                wasInCollection[0] = activity.mCoinList.get(TAP_POSITION - 1).isInCollection();
                wasInCollection[1] = tappedCoinSlot.isInCollection();
                wasInCollection[2] = activity.mCoinList.get(TAP_POSITION + 1).isInCollection();
            });

            openActionsDialogAndClick(scenario, ACTION_TOGGLE);

            scenario.onActivity(activity -> {
                assertEquals(initialCount[0], activity.mCoinList.size());
                CoinSlot tappedCoinSlot = activity.mCoinList.get(TAP_POSITION);
                assertEquals("The dialog should have acted on the tapped coin",
                        tappedDatabaseId[0], tappedCoinSlot.getDatabaseId());
                assertEquals("The tapped coin's collected state should have flipped",
                        !wasInCollection[1], tappedCoinSlot.isInCollection());
                // Neighboring coins are untouched
                assertEquals(wasInCollection[0],
                        activity.mCoinList.get(TAP_POSITION - 1).isInCollection());
                assertEquals(wasInCollection[2],
                        activity.mCoinList.get(TAP_POSITION + 1).isInCollection());
            });
        }
    }
}
