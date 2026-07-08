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
import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.BaseTestCase;
import com.spencerpages.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

/**
 * Regression tests for {@link CollectionPage}'s saved-instance-state restore
 * path. Lives in the {@code com.coincollection} package so it can flag a coin
 * as having uncommitted advanced-info edits via the package-private
 * {@link CoinSlot#setAdvInfoChanged(boolean)}.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionPageRestoreTests extends BaseTestCase {

    /**
     * Reproduces the crash from issue #405: on a state restore where a coin has
     * uncommitted advanced-info changes, {@code setupFromDatabase()} used to call
     * {@code showUnsavedTextView()} before {@code setContentView()} had run, so
     * {@code findViewById(R.id.unsaved_message_textview)} returned null and
     * {@code setVisibility()} threw a NullPointerException.
     * <p>
     * Before the fix this test crashes during {@link ActivityScenario#recreate()};
     * after the fix the "Unsaved Changes" indicator is shown correctly.
     */
    @Test
    public void test_showUnsavedTextViewSurvivesStateRestore() {
        CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
        String collectionName = info.getName();
        int coinTypeIdx = info.getCollectionTypeIndex();

        // Create the collection in the database so CollectionPage can load it.
        // Use the advanced display, since that is the view that tracks uncommitted
        // advanced-info edits and hosts the "Unsaved Changes" indicator.
        try (ActivityScenario<CoinPageCreator> creatorScenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class))) {
            creatorScenario.onActivity(activity -> {
                activity.mCoinList = new ArrayList<>();
                ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                int index = info.getCollectionTypeIndex();
                activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                activity.createOrUpdateCoinListForAsyncThread();
                activity.mDbAdapter.createAndPopulateNewTable(info, 0, activity.mCoinList);
                activity.mDbAdapter.updateTableDisplay(collectionName, CollectionPage.ADVANCED_DISPLAY);
            });
        }

        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                        .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, coinTypeIdx)
                        .putExtra(CollectionPage.COLLECTION_NAME, collectionName))) {

            // Simulate an uncommitted advanced-info edit on the first coin. This is
            // what onSaveInstanceState() persists and what the restore path checks.
            scenario.onActivity(activity -> {
                assertFalse("Collection should have coins", activity.mOriginalCoinList.isEmpty());
                activity.mOriginalCoinList.get(0).setAdvInfoChanged(true);
            });

            // Recreate the activity: onSaveInstanceState() saves the coin list and
            // onCreate() re-runs with a non-null savedInstanceState, exercising the
            // restore path that previously crashed.
            scenario.recreate();

            scenario.onActivity(activity -> {
                TextView unsavedView = activity.findViewById(R.id.unsaved_message_textview);
                assertNotNull("Unsaved-changes view should exist after restore", unsavedView);
                assertEquals("Unsaved-changes indicator should be shown after restore",
                        View.VISIBLE, unsavedView.getVisibility());
            });
        }
    }
}
