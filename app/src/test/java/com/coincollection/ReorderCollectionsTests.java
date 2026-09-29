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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.helper.SimpleItemTouchHelperCallback;
import com.spencerpages.BaseTestCase;
import com.spencerpages.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests reordering collections the way a user does it: dragging rows (through
 * {@link SimpleItemTouchHelperCallback}, as ItemTouchHelper calls it) or tapping the arrows,
 * then saving from the fragment's menu
 */
@RunWith(RobolectricTestRunner.class)
public class ReorderCollectionsTests extends BaseTestCase {

    private static final int NUM_COLLECTIONS = 4;

    /** Launches MainActivity with the test collections and opens the reorder fragment */
    private static ReorderCollections openReorderFragment(MainActivity activity) {
        for (int i = 0; i < NUM_COLLECTIONS; i++) {
            activity.mDbAdapter.createAndPopulateNewTable(COLLECTION_LIST_INFO_SCENARIOS[i], i, null);
        }
        activity.updateCollectionListFromDatabase();
        ReorderCollections fragment = activity.launchReorderFragment();
        assertNotNull(fragment);
        activity.getSupportFragmentManager().executePendingTransactions();
        shadowOf(Looper.getMainLooper()).idle();
        return fragment;
    }

    private static RecyclerView getRecyclerView(ReorderCollections fragment) {
        RecyclerView recyclerView = fragment.requireView().findViewById(R.id.reorder_collections_recycler_view);
        shadowOf(Looper.getMainLooper()).idle();
        return recyclerView;
    }

    private static RecyclerView.ViewHolder getViewHolder(RecyclerView recyclerView, int position) {
        shadowOf(Looper.getMainLooper()).idle();
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        assertNotNull("No view holder at " + position, holder);
        return holder;
    }

    /** The collection names in the given order of COLLECTION_LIST_INFO_SCENARIOS indexes */
    private static List<String> scenarioNames(int... indexes) {
        String[] names = new String[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            names[i] = COLLECTION_LIST_INFO_SCENARIOS[indexes[i]].getName();
        }
        return Arrays.asList(names);
    }

    private static List<String> adapterNames(ReorderCollections fragment) {
        List<String> names = new ArrayList<>();
        for (CollectionListInfo info : fragment.mAdapter.mItems) {
            names.add(info.getName());
        }
        return names;
    }

    private static List<String> databaseNames(MainActivity activity) {
        ArrayList<CollectionListInfo> entries = new ArrayList<>();
        activity.mDbAdapter.getAllTables(entries);
        List<String> names = new ArrayList<>();
        for (CollectionListInfo info : entries) {
            names.add(info.getName());
        }
        return names;
    }

    private static boolean isUnsavedIndicatorShown(ReorderCollections fragment) {
        return fragment.requireView().findViewById(R.id.unsaved_message_textview_reorder)
                .getVisibility() == View.VISIBLE;
    }

    private static int getBackgroundColor(View view) {
        return (view.getBackground() instanceof ColorDrawable)
                ? ((ColorDrawable) view.getBackground()).getColor() : 0;
    }

    @Test
    public void test_callbackAllowsVerticalDragOnly() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                ReorderCollections fragment = openReorderFragment(activity);
                RecyclerView recyclerView = getRecyclerView(fragment);

                // The fragment attaches an ItemTouchHelper to the list
                assertEquals(1, recyclerView.getItemDecorationCount());
                assertTrue(recyclerView.getItemDecorationAt(0) instanceof ItemTouchHelper);

                SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(fragment.mAdapter);
                assertEquals(ItemTouchHelper.Callback.makeMovementFlags(
                                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0),
                        callback.getMovementFlags(recyclerView, getViewHolder(recyclerView, 0)));
                assertFalse(callback.isItemViewSwipeEnabled());
                assertTrue(callback.isLongPressDragEnabled());
            });
        }
    }

    /**
     * Drags the first collection to the bottom one row at a time, as ItemTouchHelper does,
     * then saves from the menu and checks the order was written
     */
    @Test
    public void test_dragAndSavePersistsOrder() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                ReorderCollections fragment = openReorderFragment(activity);
                RecyclerView recyclerView = getRecyclerView(fragment);
                SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(fragment.mAdapter);
                assertEquals(scenarioNames(0, 1, 2, 3), adapterNames(fragment));
                assertFalse(isUnsavedIndicatorShown(fragment));

                // Pick the row up: it's highlighted while dragged
                RecyclerView.ViewHolder dragged = getViewHolder(recyclerView, 0);
                callback.onSelectedChanged(dragged, ItemTouchHelper.ACTION_STATE_DRAG);
                assertEquals(Color.LTGRAY, getBackgroundColor(dragged.itemView));

                // Move it past each row below it
                for (int target = 1; target < NUM_COLLECTIONS; target++) {
                    assertTrue(callback.onMove(recyclerView, dragged, getViewHolder(recyclerView, target)));
                    assertEquals(target, dragged.getBindingAdapterPosition());
                }
                assertEquals(scenarioNames(1, 2, 3, 0), adapterNames(fragment));
                assertTrue(isUnsavedIndicatorShown(fragment));

                // Drop it: the highlight goes away
                callback.clearView(recyclerView, dragged);
                assertEquals(0, getBackgroundColor(dragged.itemView));
                assertEquals(1.0f, dragged.itemView.getAlpha(), 0.0f);

                // Nothing is written until the user saves
                assertEquals(scenarioNames(0, 1, 2, 3), databaseNames(activity));

                fragment.onOptionsItemSelected(new RoboMenuItem(R.id.save_reordered_collections));
                assertEquals(scenarioNames(1, 2, 3, 0), databaseNames(activity));
                assertFalse(isUnsavedIndicatorShown(fragment));
                assertEquals(activity.getString(R.string.changes_saved), ShadowToast.getTextOfLatestToast());
                for (int i = 0; i < NUM_COLLECTIONS; i++) {
                    assertEquals(adapterNames(fragment).get(i), activity.mCollectionListEntries.get(i).getName());
                }

                // With nothing unsaved, Up closes the fragment without asking
                fragment.onOptionsItemSelected(new RoboMenuItem(android.R.id.home));
                activity.getSupportFragmentManager().executePendingTransactions();
                assertNull(activity.getSupportFragmentManager()
                        .findFragmentByTag(ReorderCollections.REORDER_COLLECTION));
            });
        }
    }

    /**
     * The up and down arrows (the accessible alternative to dragging) move a row one place,
     * and do nothing at the ends of the list
     */
    @Test
    public void test_arrowsMoveRows() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                ReorderCollections fragment = openReorderFragment(activity);
                RecyclerView recyclerView = getRecyclerView(fragment);

                getViewHolder(recyclerView, 0).itemView.findViewById(R.id.move_up_arrow).performClick();
                getViewHolder(recyclerView, NUM_COLLECTIONS - 1).itemView
                        .findViewById(R.id.move_down_arrow).performClick();
                assertEquals(scenarioNames(0, 1, 2, 3), adapterNames(fragment));
                assertFalse(isUnsavedIndicatorShown(fragment));

                getViewHolder(recyclerView, 0).itemView.findViewById(R.id.move_down_arrow).performClick();
                assertEquals(scenarioNames(1, 0, 2, 3), adapterNames(fragment));
                assertTrue(isUnsavedIndicatorShown(fragment));

                getViewHolder(recyclerView, 3).itemView.findViewById(R.id.move_up_arrow).performClick();
                assertEquals(scenarioNames(1, 0, 3, 2), adapterNames(fragment));

                fragment.onOptionsItemSelected(new RoboMenuItem(R.id.save_reordered_collections));
                assertEquals(scenarioNames(1, 0, 3, 2), databaseNames(activity));
            });
        }
    }

    /**
     * An unsaved order survives the activity being recreated
     */
    @Test
    public void test_unsavedOrderSurvivesRecreate() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                ReorderCollections fragment = openReorderFragment(activity);
                RecyclerView recyclerView = getRecyclerView(fragment);
                getViewHolder(recyclerView, 0).itemView.findViewById(R.id.move_down_arrow).performClick();
                assertEquals(scenarioNames(1, 0, 2, 3), adapterNames(fragment));
            });

            scenario.recreate();

            scenario.onActivity(activity -> {
                shadowOf(Looper.getMainLooper()).idle();
                ReorderCollections fragment = (ReorderCollections) activity.getSupportFragmentManager()
                        .findFragmentByTag(ReorderCollections.REORDER_COLLECTION);
                assertNotNull(fragment);
                assertEquals(scenarioNames(1, 0, 2, 3), adapterNames(fragment));
                assertTrue(isUnsavedIndicatorShown(fragment));
                assertEquals(scenarioNames(0, 1, 2, 3), databaseNames(activity));

                fragment.onOptionsItemSelected(new RoboMenuItem(R.id.save_reordered_collections));
                assertEquals(scenarioNames(1, 0, 2, 3), databaseNames(activity));
                assertSame(fragment, activity.getSupportFragmentManager()
                        .findFragmentByTag(ReorderCollections.REORDER_COLLECTION));
            });
        }
    }
}
