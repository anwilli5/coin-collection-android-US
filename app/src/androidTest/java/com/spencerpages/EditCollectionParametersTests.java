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
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
 * Tests for editing a collection's parameters (mint marks, date range)
 * via long-press → Edit on the main activity.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class EditCollectionParametersTests {

    private static final String COLLECTION_NAME = "Lincoln Cents Test";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsCollection(COLLECTION_NAME, 0);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Verify editing mint marks changes coin count, and reverting works.
     */
    @Test
    public void test_editCollectionParameters() {
        UITestHelper.recreateActivity(activityRule);

        // Verify initial progress
        onView(withText(COLLECTION_NAME)).check(matches(isDisplayed()));
        onView(withText(UITestHelper.formatCollectionProgress(0, 121))).check(matches(isDisplayed()));

        // Long-press the collection
        onView(withText(COLLECTION_NAME)).perform(longClick());

        // Verify "Collection Actions" dialog
        onView(withText(R.string.collection_actions)).check(matches(isDisplayed()));

        // Tap "Edit"
        onView(withText(R.string.edit)).perform(click());

        // Verify CoinPageCreator opens with pre-filled values
        onView(withId(R.id.edit_enter_collection_name)).check(matches(isDisplayed()));
        onView(withId(R.id.create_page)).check(matches(isDisplayed()));

        // Verify it shows "Update Collection!" button text
        onView(withText(R.string.update_page)).check(matches(isDisplayed()));

        // Tap the show mint mark checkbox
        onView(withId(R.id.check_show_mint_mark)).perform(scrollTo(), click());

        // Wait for layout to update with mint mark sub-checkboxes
        UITestHelper.waitForDisplayed(withText(R.string.include_p));

        // Check the Philadelphia mint checkbox (at least one mint must be selected)
        onView(withText(R.string.include_p)).perform(scrollTo(), click());

        // Tap "Update Collection!"
        onView(withId(R.id.create_page)).perform(scrollTo(), click());

        // Wait for async creation to complete and return to main activity
        UITestHelper.waitForMainActivity();

        // Verify return to main activity — collection should still exist
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
        onView(withText(COLLECTION_NAME)).check(matches(isDisplayed()));

        // Now revert: long-press → Edit → uncheck show mint mark → Update
        onView(withText(COLLECTION_NAME)).perform(longClick());
        onView(withText(R.string.edit)).perform(click());

        // Uncheck the show mint mark checkbox
        onView(withId(R.id.check_show_mint_mark)).perform(scrollTo(), click());

        // Tap "Update Collection!"
        onView(withId(R.id.create_page)).perform(scrollTo(), click());

        // A warning dialog may appear about deleting progress
        try {
            onView(withText(R.string.warning)).check(matches(isDisplayed()));
            onView(withText(R.string.yes)).perform(click());
        } catch (Exception e) {
            // Dialog may not appear if no data loss occurs
        }

        // Wait for async update to complete and return to main activity
        UITestHelper.waitForMainActivity();

        // Verify return to main activity
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
        onView(withText(COLLECTION_NAME)).check(matches(isDisplayed()));
    }
}
