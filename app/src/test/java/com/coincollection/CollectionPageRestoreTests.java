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
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.BaseTestCase;
import com.spencerpages.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenuItem;

import java.util.ArrayList;

/**
 * Regression tests for {@link CollectionPage}'s saved-instance-state restore
 * path. Lives in the {@code com.coincollection} package so it can flag a coin
 * as having uncommitted advanced-info edits via the package-private
 * {@link CoinSlot#setAdvInfoChanged(boolean)}.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionPageRestoreTests extends BaseTestCase {

    private static final CollectionListInfo INFO = COLLECTION_LIST_INFO_SCENARIOS[0];

    /**
     * Creates the test collection in the database, in the advanced display, so
     * CollectionPage can load it
     */
    private static void createAdvancedCollection() {
        try (ActivityScenario<CoinPageCreator> creatorScenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class))) {
            creatorScenario.onActivity(activity -> {
                activity.mCoinList = new ArrayList<>();
                ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(INFO);
                int index = INFO.getCollectionTypeIndex();
                activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                activity.createOrUpdateCoinListForAsyncThread();
                activity.mDbAdapter.createAndPopulateNewTable(INFO, 0, activity.mCoinList);
                activity.mDbAdapter.updateTableDisplay(INFO.getName(), CollectionPage.ADVANCED_DISPLAY);
            });
        }
    }

    private static Intent collectionPageIntent() {
        return new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, INFO.getCollectionTypeIndex())
                .putExtra(CollectionPage.COLLECTION_NAME, INFO.getName());
    }

    /**
     * Returns the laid-out advanced-view row for a list position
     */
    private static View getRow(CollectionPage activity, int position) {
        shadowOf(Looper.getMainLooper()).idle();
        ListView listView = activity.findViewById(R.id.advanced_collection_page);
        View row = listView.getChildAt(position - listView.getFirstVisiblePosition());
        assertNotNull("Row " + position + " isn't laid out", row);
        return row;
    }

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
        // Use the advanced display, since that is the view that tracks uncommitted
        // advanced-info edits and hosts the "Unsaved Changes" indicator.
        createAdvancedCollection();

        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {

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

    /**
     * Edits made in the advanced view (grade, quantity, notes and whether a coin is
     * collected) survive the activity being recreated, stay unsaved, and are written when
     * the user saves by locking the collection
     */
    @Test
    public void test_advancedViewEditsSurviveRecreate() {
        createAdvancedCollection();

        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            boolean[] initiallyCollected = new boolean[1];
            scenario.onActivity(activity -> {
                ((Spinner) getRow(activity, 0).findViewById(R.id.grade_selector)).setSelection(5);
                ((Spinner) getRow(activity, 1).findViewById(R.id.quantity_selector)).setSelection(3);
                ((EditText) getRow(activity, 2).findViewById(R.id.notes_edit_text)).setText("Rotated note");
                initiallyCollected[0] = activity.mCoinList.get(3).isInCollection();
                getRow(activity, 3).findViewById(R.id.coinImage).performClick();
                shadowOf(Looper.getMainLooper()).idle();
                assertEquals(5, (int) activity.mCoinList.get(0).getAdvancedGrades());
            });

            scenario.recreate();

            scenario.onActivity(activity -> {
                // The edits are back, and still marked unsaved
                ArrayList<CoinSlot> coins = activity.mOriginalCoinList;
                assertEquals(5, (int) coins.get(0).getAdvancedGrades());
                assertEquals(3, (int) coins.get(1).getAdvancedQuantities());
                assertEquals("Rotated note", coins.get(2).getAdvancedNotes());
                assertEquals(!initiallyCollected[0], coins.get(3).isInCollection());
                for (int i = 0; i < 4; i++) {
                    assertTrue("Coin " + i + " should still be unsaved", coins.get(i).hasAdvInfoChanged());
                }
                assertFalse(coins.get(4).hasAdvInfoChanged());
                assertEquals(View.VISIBLE, activity.findViewById(R.id.unsaved_message_textview).getVisibility());

                // ...and shown
                assertEquals(5, ((Spinner) getRow(activity, 0).findViewById(R.id.grade_selector))
                        .getSelectedItemPosition());
                assertEquals(3, ((Spinner) getRow(activity, 1).findViewById(R.id.quantity_selector))
                        .getSelectedItemPosition());
                assertEquals("Rotated note", ((EditText) getRow(activity, 2).findViewById(R.id.notes_edit_text))
                        .getText().toString());

                // ...but not written yet
                ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(INFO.getName(), true);
                assertEquals(0, (int) dbCoins.get(0).getAdvancedGrades());
                assertEquals(initiallyCollected[0], dbCoins.get(3).isInCollection());

                // Locking the collection saves them
                activity.onOptionsItemSelected(new RoboMenuItem(R.id.lock_unlock_collection));
                dbCoins = activity.mDbAdapter.getCoinList(INFO.getName(), true);
                assertEquals(5, (int) dbCoins.get(0).getAdvancedGrades());
                assertEquals(3, (int) dbCoins.get(1).getAdvancedQuantities());
                assertEquals("Rotated note", dbCoins.get(2).getAdvancedNotes());
                assertEquals(!initiallyCollected[0], dbCoins.get(3).isInCollection());
            });
        }
    }
}
