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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.coincollection.CollectionPage;
import com.coincollection.DatabaseAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Regression test for WI-02 (issue #404): recreating a {@link CollectionPage}
 * while the shared database is closed — as happens when the OS kills the app
 * process and the user returns to the collection from Recents — must not crash.
 * <p>
 * Before the fix, {@code CollectionPage.onCreate()} called
 * {@code DatabaseAdapter.fetchTableDisplay()} synchronously, which threw a
 * {@link NullPointerException} in {@code SQLiteDatabase.compileStatement()}
 * because the database had not been (re)opened yet. The fix defers all
 * database-dependent setup until the asynchronous open completes.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class CollectionPageProcessDeathTest {

    private static final String COLLECTION_NAME = "Process Death Test";
    // LincolnCents is at index 0 in MainApplication.COLLECTION_TYPES
    private static final int COLLECTION_TYPE_INDEX = 0;

    @Before
    public void setUp() {
        UITestHelper.disableSystemAnimations();
        UITestHelper.dismissSystemDialogs();
        UITestHelper.suppressAllTutorials();
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsCollection(COLLECTION_NAME, 0);
        UITestHelper.unlockCollection(COLLECTION_NAME);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Simulate returning to a collection after the OS reclaimed the app process:
     * close the shared database, then launch (and recreate) the CollectionPage.
     * The page must come up and render its grid without crashing, and the fix
     * must reopen the database asynchronously.
     */
    @Test
    public void test_collectionPageSurvivesProcessDeathWithClosedDatabase() {
        Context context = getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, CollectionPage.class);
        intent.putExtra(CollectionPage.COLLECTION_NAME, COLLECTION_NAME);
        intent.putExtra(CollectionPage.COLLECTION_TYPE_INDEX, COLLECTION_TYPE_INDEX);

        // Simulate process death: the shared database adapter is closed when the
        // activity is recreated (a fresh process has an unopened database).
        DatabaseAdapter sharedDbAdapter =
                ((MainApplication) context.getApplicationContext()).getDbAdapter();
        sharedDbAdapter.close();
        assertFalse("Database should be closed to simulate process death",
                sharedDbAdapter.isOpen());

        // Cold-start path: launch CollectionPage with the database closed. Before
        // the fix this crashed in onCreate, so ActivityScenario.launch would fail.
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(intent)) {
            UITestHelper.dismissTutorialDialogs();
            UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
            onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));

            // The deferred setup must have reopened the database asynchronously.
            assertTrue("Deferred setup should have reopened the database",
                    sharedDbAdapter.isOpen());

            // Recreate the activity with the database closed again to exercise the
            // deferred path on an activity recreation (config-change-style restore
            // after another process-death event).
            sharedDbAdapter.close();
            assertFalse(sharedDbAdapter.isOpen());
            scenario.recreate();

            UITestHelper.dismissTutorialDialogs();
            UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
            onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));
            assertTrue(sharedDbAdapter.isOpen());
        }
    }
}
