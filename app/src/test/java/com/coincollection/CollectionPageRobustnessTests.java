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

import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.database.SQLException;
import android.widget.GridView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.BaseTestCase;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;

/**
 * Tests that {@link CollectionPage} stays consistent when things go wrong: a
 * failed database write must not change the coins shown, and a launch without
 * a valid collection must close instead of crashing
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionPageRobustnessTests extends BaseTestCase {

    private static final CollectionListInfo INFO = COLLECTION_LIST_INFO_SCENARIOS[0];

    /**
     * Creates the test collection in the database with the given display type
     *
     * @param displayType CollectionPage.SIMPLE_DISPLAY or ADVANCED_DISPLAY
     */
    private static void createCollection(int displayType) {
        try (ActivityScenario<CoinPageCreator> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class))) {
            scenario.onActivity(activity -> {
                activity.mCoinList = new ArrayList<>();
                ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(INFO);
                int index = INFO.getCollectionTypeIndex();
                activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                activity.createOrUpdateCoinListForAsyncThread();
                activity.mDbAdapter.createAndPopulateNewTable(INFO, 0, activity.mCoinList);
                activity.mDbAdapter.updateTableDisplay(INFO.getName(), displayType);
            });
        }
    }

    private static Intent collectionPageIntent() {
        return new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, INFO.getCollectionTypeIndex())
                .putExtra(CollectionPage.COLLECTION_NAME, INFO.getName());
    }

    @Test
    public void test_failedDeleteLeavesCoinListsUnchanged() {
        createCollection(CollectionPage.SIMPLE_DISPLAY);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                DatabaseAdapter realDb = activity.mDbAdapter;
                DatabaseAdapter failingDb = spy(realDb);
                doThrow(new SQLException("forced failure")).when(failingDb)
                        .removeCoinSlotFromCollection(any(), anyString(), anyInt());
                activity.mDbAdapter = failingDb;

                int coinCount = activity.mCoinList.size();
                int originalCount = activity.mOriginalCoinList.size();
                CoinSlot firstCoin = activity.mCoinList.get(0);

                activity.deleteCoinSlotAtPosition(0);

                verify(failingDb).removeCoinSlotFromCollection(any(), anyString(), anyInt());
                assertEquals(coinCount, activity.mCoinList.size());
                assertEquals(originalCount, activity.mOriginalCoinList.size());
                assertSame(firstCoin, activity.mCoinList.get(0));
                assertEquals(originalCount, realDb.getCoinList(INFO.getName(), true).size());
                activity.mDbAdapter = realDb;
            });
        }
    }

    @Test
    public void test_failedToggleLeavesCoinStateUnchanged() {
        createCollection(CollectionPage.SIMPLE_DISPLAY);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                GridView gridView = activity.findViewById(R.id.standard_collection_page);
                CoinSlot firstCoin = activity.mCoinList.get(0);
                boolean initialState = firstCoin.isInCollection();

                // A working tap toggles the coin, confirming the tap reaches the toggle
                gridView.performItemClick(null, 0, 0);
                assertEquals(!initialState, firstCoin.isInCollection());

                DatabaseAdapter realDb = activity.mDbAdapter;
                DatabaseAdapter failingDb = spy(realDb);
                doThrow(new SQLException("forced failure")).when(failingDb)
                        .toggleInCollection(anyString(), any());
                activity.mDbAdapter = failingDb;

                // A failed tap leaves both the coin and the database as they were
                gridView.performItemClick(null, 0, 0);
                verify(failingDb).toggleInCollection(anyString(), any());
                assertEquals(!initialState, firstCoin.isInCollection());
                assertEquals(!initialState,
                        realDb.getCoinList(INFO.getName(), true).get(0).isInCollection());
                activity.mDbAdapter = realDb;
            });
        }
    }

    @Test
    public void test_failedCoinUpdateRestoresCoinDetails() {
        createCollection(CollectionPage.SIMPLE_DISPLAY);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                DatabaseAdapter realDb = activity.mDbAdapter;
                DatabaseAdapter failingDb = spy(realDb);
                doThrow(new SQLException("forced failure")).when(failingDb)
                        .updateCoinNameMintImage(anyString(), any());
                activity.mDbAdapter = failingDb;

                CoinSlot coin = activity.mCoinList.get(0);
                String name = coin.getIdentifier();
                String mint = coin.getMint();
                int imageId = coin.getImageId();

                activity.updateCoinDetails(coin, name + " renamed", mint + " changed", imageId + 1);

                verify(failingDb).updateCoinNameMintImage(anyString(), any());
                assertEquals(name, coin.getIdentifier());
                assertEquals(mint, coin.getMint());
                assertEquals(imageId, coin.getImageId());
                activity.mDbAdapter = realDb;
            });
        }
    }

    /**
     * Launches CollectionPage with the given intent and checks that it closes
     * itself with an error message rather than crashing
     */
    private static void assertClosesWithError(Intent intent) {
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(intent)) {
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
            assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.error_opening_collection),
                    ShadowToast.getTextOfLatestToast());
        }
    }

    @Test
    public void test_outOfRangeCollectionTypeCloses() {
        assertClosesWithError(collectionPageIntent()
                .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, MainApplication.COLLECTION_TYPES.length));
    }

    @Test
    public void test_missingCollectionTypeCloses() {
        Intent intent = collectionPageIntent();
        intent.removeExtra(CollectionPage.COLLECTION_TYPE_INDEX);
        assertClosesWithError(intent);
    }

    @Test
    public void test_missingCollectionNameCloses() {
        Intent intent = collectionPageIntent();
        intent.removeExtra(CollectionPage.COLLECTION_NAME);
        assertClosesWithError(intent);
    }

    /**
     * The back callback is only enabled while there are unsaved changes, so the
     * system can handle back (and animate predictive back) the rest of the time
     */
    @Test
    public void test_backCallbackEnabledOnlyWithUnsavedChanges() {
        createCollection(CollectionPage.ADVANCED_DISPLAY);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                assertFalse(activity.getOnBackPressedDispatcher().hasEnabledCallbacks());
                activity.showUnsavedTextView();
                assertTrue(activity.getOnBackPressedDispatcher().hasEnabledCallbacks());
            });
        }
    }
}
