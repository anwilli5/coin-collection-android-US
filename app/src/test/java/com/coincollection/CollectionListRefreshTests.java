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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.spencerpages.BaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit tests for when {@link MainActivity} reloads its collection list: on resume,
 * unless a task is writing the database, and not on every window focus change.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionListRefreshTests extends BaseTestCase {

    /**
     * @return a scenario for a freshly launched MainActivity
     */
    private static ActivityScenario<MainActivity> launchMainActivity() {
        return ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class));
    }

    /**
     * Pauses and resumes the activity, as returning from another activity does
     *
     * @param scenario the activity's scenario
     */
    private static void pauseAndResume(ActivityScenario<MainActivity> scenario) {
        scenario.moveToState(Lifecycle.State.STARTED);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    /**
     * Test that resuming picks up a collection added while the activity was in
     * the background, and that a window focus change alone (a dialog closing)
     * doesn't read the database
     */
    @Test
    public void test_resumeRefreshesListButFocusChangeDoesNot() {
        CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
        try (ActivityScenario<MainActivity> scenario = launchMainActivity()) {
            scenario.onActivity(activity -> {
                assertEquals(0, activity.mNumberOfCollections);
                activity.mDbAdapter.createAndPopulateNewTable(info, 0, null);
                activity.onWindowFocusChanged(true);
                assertEquals(0, activity.mNumberOfCollections);
            });

            pauseAndResume(scenario);

            scenario.onActivity(activity -> {
                assertFalse(activity.isDatabaseWriteInProgress());
                assertEquals(1, activity.mNumberOfCollections);
                assertEquals(info.getName(), activity.mCollectionListEntries.get(0).getName());
                activity.mDbAdapter.dropCollectionTable(info.getName());
            });
        }
    }

    /**
     * Regression test for the spurious "error reading database" message reported when
     * importing into an app that has no collections yet.
     * <p>
     * The import worker drops the collection_info table before recreating it, so the
     * collection list must not be re-read while an import is in flight. Every import
     * entry point has to mark the import as in progress, including the ones that skip
     * the confirmation dialog because there is nothing to overwrite.
     */
    @Test
    public void test_resumeDuringImportSkipsListRefresh() {
        CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
        try (ActivityScenario<MainActivity> scenario = launchMainActivity()) {
            scenario.onActivity(activity -> {
                assertEquals(0, activity.mNumberOfCollections);
                assertFalse(activity.isDatabaseWriteInProgress());

                // Queue the import on an executor that never runs it, so it is still
                // in flight when the activity resumes. The synchronous unit-test seam
                // would run it to completion inline, setting and clearing the flag
                // within this call and hiding the state onResume() has to observe
                AsyncTaskRunner runner = new AsyncTaskRunner(
                        activity, command -> { }, new Handler(Looper.getMainLooper()));
                activity.mTaskRunner = runner;
                activity.mActivityViewModel.mSavedTaskRunner = runner;
                BaseActivity.sRunTasksInline = false;

                // Returning from the file picker with a file starts the import
                activity.onActivityResult(MainActivity.PICK_IMPORT_FILE,
                        Activity.RESULT_OK, new Intent());
                assertTrue(activity.isDatabaseWriteInProgress());

                activity.mDbAdapter.createAndPopulateNewTable(info, 0, null);
            });

            // The picker returning also resumes the activity. The list must be left
            // alone while the import rewrites the database
            pauseAndResume(scenario);

            scenario.onActivity(activity -> {
                assertTrue(activity.isDatabaseWriteInProgress());
                assertEquals(0, activity.mNumberOfCollections);
                activity.mDbAdapter.dropCollectionTable(info.getName());
            });
        }
    }

    /**
     * Test that a failed read keeps showing the collections from the last
     * successful one instead of an empty list
     */
    @Test
    public void test_failedReadKeepsPreviousList() {
        CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
        try (ActivityScenario<MainActivity> scenario = launchMainActivity()) {
            scenario.onActivity(activity -> {
                DatabaseAdapter dbAdapter = activity.mDbAdapter;
                dbAdapter.createAndPopulateNewTable(info, 0, null);
                activity.updateCollectionListFromDatabaseAndUpdateViewForUIThread();
                assertEquals(1, activity.mNumberOfCollections);

                DatabaseAdapter failingAdapter = spy(dbAdapter);
                doThrow(new SQLException("read failed"))
                        .when(failingAdapter).getAllTables(any(), any());
                activity.mDbAdapter = failingAdapter;
                try {
                    activity.updateCollectionListFromDatabaseAndUpdateViewForUIThread();
                } finally {
                    activity.mDbAdapter = dbAdapter;
                }

                assertEquals(1, activity.mNumberOfCollections);
                assertEquals(info.getName(), activity.mCollectionListEntries.get(0).getName());
                assertEquals(1 + MainActivity.NUMBER_OF_COLLECTION_LIST_SPACERS,
                        activity.mCollectionListEntries.size());
                dbAdapter.dropCollectionTable(info.getName());
            });
        }
    }

    /**
     * Test that the list isn't read while the database isn't open (the open task
     * reloads it once it's done)
     */
    @Test
    public void test_refreshSkippedWhileDatabaseClosed() {
        try (ActivityScenario<MainActivity> scenario = launchMainActivity()) {
            scenario.onActivity(activity -> {
                DatabaseAdapter dbAdapter = activity.mDbAdapter;
                DatabaseAdapter closedAdapter = spy(dbAdapter);
                doReturn(false).when(closedAdapter).isOpen();
                activity.mDbAdapter = closedAdapter;
                try {
                    activity.updateCollectionListFromDatabaseAndUpdateViewForUIThread();
                } finally {
                    activity.mDbAdapter = dbAdapter;
                }
                verify(closedAdapter, never()).getAllTables(any(), any());
            });
        }
    }
}
