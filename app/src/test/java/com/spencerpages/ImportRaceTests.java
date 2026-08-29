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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.BaseActivity;
import com.coincollection.CollectionListInfo;
import com.coincollection.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ImportRaceTests extends BaseTestCase {

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
    public void test_importWithNoCollectionsSkipsListRefresh() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                activity.updateCollectionListFromDatabase();
                assertEquals(0, activity.mNumberOfCollections);
                assertFalse(activity.isImportInProgress());

                // Use the real (asynchronous) task path so the import is only queued
                // here. The synchronous unit-test seam would run the import to
                // completion inline, setting and clearing the flag within this call
                // and hiding the state the window focus handler has to observe.
                BaseActivity.isUnitTest = false;
                try {
                    // The result intent carries no URI, so the queued task fails fast
                    // without performing any file I/O
                    activity.onActivityResult(MainActivity.PICK_IMPORT_FILE,
                            Activity.RESULT_OK, new Intent());
                } finally {
                    BaseActivity.isUnitTest = true;
                }

                assertTrue(activity.isImportInProgress());

                // Returning from the file picker gives the collection list focus again.
                // The list must be left alone while the import rewrites the database.
                CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
                activity.mDbAdapter.createAndPopulateNewTable(info, 0, null);
                activity.onWindowFocusChanged(true);
                assertEquals(0, activity.mNumberOfCollections);

                // Clean up
                activity.mDbAdapter.dropCollectionTable(info.getName());
            });
        }
    }

    /**
     * Control for the test above - the collection list is still refreshed when the
     * window gains focus and no import is running
     */
    @Test
    public void test_windowFocusRefreshesListWhenNotImporting() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {
                activity.updateCollectionListFromDatabase();
                assertEquals(0, activity.mNumberOfCollections);

                CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
                activity.mDbAdapter.createAndPopulateNewTable(info, 0, null);
                activity.onWindowFocusChanged(true);
                assertFalse(activity.isImportInProgress());
                assertEquals(1, activity.mNumberOfCollections);

                // Clean up
                activity.mDbAdapter.dropCollectionTable(info.getName());
            });
        }
    }
}
