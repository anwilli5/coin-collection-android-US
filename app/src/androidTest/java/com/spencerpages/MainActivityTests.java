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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.DataInteraction;
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
 * Tests for the main activity: empty state, collection creation, long-press actions,
 * delete collection, and app info dialog.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class MainActivityTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Verify the main activity renders correctly with no collections.
     * All 6 navigation items present, no collection rows.
     */
    @Test
    public void test_emptyState() {
        // Relaunch to pick up clean state
        UITestHelper.recreateActivity(activityRule);

        // Verify main list is displayed
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));

        // Verify all 6 navigation items are visible
        onView(withText(R.string.create_new_collection)).check(matches(isDisplayed()));
        onView(withText(R.string.delete_collection)).check(matches(isDisplayed()));
        onView(withText(R.string.import_collection)).check(matches(isDisplayed()));
        onView(withText(R.string.export_collection)).check(matches(isDisplayed()));
        onView(withText(R.string.reorder_collection)).check(matches(isDisplayed()));
        onView(withText(R.string.app_info)).check(matches(isDisplayed()));

        // Verify no collection rows exist (collectionNameTextView should not exist)
        onView(withId(R.id.collectionNameTextView)).check(doesNotExist());
    }

    /**
     * Verify collection creation flow end-to-end via the UI.
     */
    @Test
    public void test_createLincolnCentsCollection() {
        // Relaunch to pick up clean state
        UITestHelper.recreateActivity(activityRule);

        // Tap "New Collection"
        onView(withText(R.string.create_new_collection)).perform(click());

        // Verify CoinPageCreator screen is shown
        onView(withId(R.id.edit_enter_collection_name)).check(matches(isDisplayed()));
        onView(withId(R.id.coin_selector)).check(matches(isDisplayed()));
        onView(withId(R.id.create_page)).check(matches(isDisplayed()));

        // Enter collection name
        onView(withId(R.id.edit_enter_collection_name))
                .perform(typeText("Lincoln Cents Test"), closeSoftKeyboard());

        // Select "Pennies" from the coin type spinner (default is "Select Collection Type")
        onView(withId(R.id.coin_selector)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Pennies"))).perform(click());

        // Create the collection
        onView(withId(R.id.create_page)).perform(scrollTo(), click());

        // Wait for async creation to complete and return to main activity
        UITestHelper.waitForMainActivity();

        // Verify return to main activity with the collection row
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
        onView(withText("Lincoln Cents Test")).check(matches(isDisplayed()));
        onView(withText(UITestHelper.formatCollectionProgress(0, 121))).check(matches(isDisplayed()));
    }

    /**
     * Verify multiple collections can coexist with spinner coin type selection.
     */
    @Test
    public void test_createPresidentialDollarsCollection() {
        // Create the first collection programmatically
        UITestHelper.createLincolnCentsCollection("Lincoln Cents Test", 0);

        // Relaunch to pick up the pre-created collection
        UITestHelper.recreateActivity(activityRule);

        // Tap "New Collection"
        onView(withText(R.string.create_new_collection)).perform(click());

        // Enter collection name
        onView(withId(R.id.edit_enter_collection_name))
                .perform(typeText("Presidential Dollars Test"), closeSoftKeyboard());

        // Select "Presidential Dollars" from the coin type spinner
        onView(withId(R.id.coin_selector)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Presidential Dollars"))).perform(click());

        // Create the collection
        onView(withId(R.id.create_page)).perform(scrollTo(), click());

        // Wait for async creation to complete and return to main activity
        UITestHelper.waitForMainActivity();

        // Verify both collections are visible on main activity
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
        onView(withText("Lincoln Cents Test")).check(matches(isDisplayed()));
        onView(withText("Presidential Dollars Test")).check(matches(isDisplayed()));
    }

    /**
     * Verify collection-level long-press actions: Copy creates a duplicate,
     * which can then be deleted via the Delete Collection nav item.
     */
    @Test
    public void test_longPressCollectionCopyAndDelete() {
        // Create two collections
        UITestHelper.createLincolnCentsCollection("Lincoln Cents Test", 0);
        UITestHelper.createPresidentialDollarsCollection("Presidential Dollars Test", 1);

        // Relaunch
        UITestHelper.recreateActivity(activityRule);

        // Long-press "Presidential Dollars Test"
        onView(withText("Presidential Dollars Test")).perform(longClick());

        // Verify "Collection Actions" dialog with 4 options
        onView(withText(R.string.collection_actions)).check(matches(isDisplayed()));
        onView(withText(R.string.view)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed()));
        onView(withText(R.string.copy)).check(matches(isDisplayed()));
        onView(withText(R.string.delete)).check(matches(isDisplayed()));

        // Tap "Copy"
        onView(withText(R.string.copy)).perform(click());

        // Wait for the copy to appear (name should be "Presidential Dollars Test Copy")
        UITestHelper.waitForDisplayed(withText("Presidential Dollars Test Copy"));

        // Now delete the copy via "Delete Collection" nav item
        onView(withText(R.string.delete_collection)).perform(click());

        // Verify picker dialog
        onView(withText(R.string.select_collection_delete)).check(matches(isDisplayed()));

        // Select the copy to delete
        onView(withText("Presidential Dollars Test Copy")).perform(click());

        // Verify warning dialog
        onView(withText(R.string.warning)).check(matches(isDisplayed()));

        // Confirm deletion
        onView(withText(R.string.yes)).perform(click());

        // Wait for delete and verify the copy is removed
        UITestHelper.waitForDoesNotExist(withText("Presidential Dollars Test Copy"));

        // Original collections should still exist
        onView(withText("Lincoln Cents Test")).check(matches(isDisplayed()));
        onView(withText("Presidential Dollars Test")).check(matches(isDisplayed()));
    }

    /**
     * Verify delete collection flow from the main activity navigation item.
     */
    @Test
    public void test_deleteCollectionViaMenu() {
        // Create collections including one to delete
        UITestHelper.createLincolnCentsCollection("Lincoln Cents Test", 0);
        UITestHelper.createCollection("Delete Me", 0, 1, 0);

        // Relaunch
        UITestHelper.recreateActivity(activityRule);

        // Verify "Delete Me" exists
        onView(withText("Delete Me")).check(matches(isDisplayed()));

        // Tap "Delete Collection"
        onView(withText(R.string.delete_collection)).perform(click());

        // Verify picker dialog
        onView(withText(R.string.select_collection_delete)).check(matches(isDisplayed()));

        // Select "Delete Me"
        onView(withText("Delete Me")).perform(click());

        // Verify warning dialog with YES/NO
        onView(withText(R.string.warning)).check(matches(isDisplayed()));
        onView(withText(R.string.yes)).check(matches(isDisplayed()));
        onView(withText(R.string.no)).check(matches(isDisplayed()));

        // Confirm deletion
        onView(withText(R.string.yes)).perform(click());

        // Wait for delete and verify "Delete Me" is removed
        UITestHelper.waitForDoesNotExist(withText("Delete Me"));

        // "Lincoln Cents Test" should still exist
        onView(withText("Lincoln Cents Test")).check(matches(isDisplayed()));
    }

    /**
     * Verify the App Info dialog displays correctly and can be dismissed.
     */
    @Test
    public void test_appInfo() {
        // Relaunch
        UITestHelper.recreateActivity(activityRule);

        // Tap "App Info"
        onView(withText(R.string.app_info)).perform(click());

        // Verify info dialog elements
        onView(withId(R.id.info_attribution)).check(matches(isDisplayed()));
        onView(withId(R.id.info_title)).check(matches(isDisplayed()));

        // Dismiss with back press
        pressBack();

        // Verify return to main activity
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }
}
