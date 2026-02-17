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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.coincollection.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the reorder collections screen: reorder and save, unsaved changes dialog.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class ReorderCollectionsTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsCollection("Collection A", 0);
        UITestHelper.createPresidentialDollarsCollection("Collection B", 1);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Verify collection reordering with save.
     */
    @Test
    public void test_reorderAndSave() {
        UITestHelper.recreateActivity(activityRule);

        // Tap "Reorder Collections"
        onView(withText(R.string.reorder_collection)).perform(click());

        // Verify RecyclerView is displayed
        onView(withId(R.id.reorder_collections_recycler_view)).check(matches(isDisplayed()));

        // Tap the move_down_arrow on the first item to swap positions
        onView(withId(R.id.reorder_collections_recycler_view))
                .perform(actionOnItemAtPosition(0,
                        UITestHelper.clickChildViewWithId(R.id.move_down_arrow)));

        // Verify "Unsaved Changes" appears
        onView(withId(R.id.unsaved_message_textview_reorder)).check(matches(isDisplayed()));

        // Tap "Save" button
        onView(withId(R.id.save_reordered_collections)).perform(click());

        // Wait for save and verify "Unsaved Changes" disappears
        UITestHelper.waitForNotDisplayed(withId(R.id.unsaved_message_textview_reorder));

        // Navigate back
        pressBack();

        // Verify main activity is displayed
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify the unsaved changes guard in the reorder screen.
     */
    @Test
    public void test_reorderUnsavedChangesDialog() {
        UITestHelper.recreateActivity(activityRule);

        // Tap "Reorder Collections"
        onView(withText(R.string.reorder_collection)).perform(click());
        onView(withId(R.id.reorder_collections_recycler_view)).check(matches(isDisplayed()));

        // Tap move_up_arrow on second collection to swap positions
        onView(withId(R.id.reorder_collections_recycler_view))
                .perform(actionOnItemAtPosition(1,
                        UITestHelper.clickChildViewWithId(R.id.move_up_arrow)));

        // Verify "Unsaved Changes"
        onView(withId(R.id.unsaved_message_textview_reorder)).check(matches(isDisplayed()));

        // Press back
        pressBack();

        // Verify dialog appears
        onView(withText(R.string.dialog_unsaved_changes_exit)).check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).check(matches(isDisplayed()));
        onView(withText(R.string.okay)).check(matches(isDisplayed()));

        // Tap "Cancel" — should stay on reorder screen
        onView(withText(R.string.cancel)).perform(click());
        onView(withId(R.id.reorder_collections_recycler_view)).check(matches(isDisplayed()));

        // Press back again
        pressBack();

        // Dialog appears again
        onView(withText(R.string.dialog_unsaved_changes_exit)).check(matches(isDisplayed()));

        // Tap "Okay" — return to main activity (changes discarded)
        onView(withText(R.string.okay)).perform(click());

        // Verify main activity
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }
}
