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
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
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
 * Tests for the collection page in basic (grid) view:
 * coin toggling, lock/unlock, filter, add coin, long-press coin actions.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class CollectionPageBasicViewTests {

    private static final String COLLECTION_NAME = "Lincoln Cents Test";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsCollection(COLLECTION_NAME, 0);
        UITestHelper.unlockCollection(COLLECTION_NAME);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Verify the basic grid view and coin toggle interaction.
     */
    @Test
    public void test_basicViewTapToCollect() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Verify Lock button is visible (unlocked state shows "Lock")
        onView(withText(R.string.lock_collection)).check(matches(isDisplayed()));

        // Click first coin to toggle to collected
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(click());

        // Verify the coin's content description now contains "collected"
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(containsString(UITestHelper.getString(R.string.collected)))));

        // Click again to toggle back to missing
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(click());

        // Verify it returned to "missing"
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(containsString(UITestHelper.getString(R.string.missing)))));

        // Navigate back
        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify locking prevents coin toggling and unlocking restores it.
     */
    @Test
    public void test_lockUnlockCollection() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Verify first coin is "missing"
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(containsString(UITestHelper.getString(R.string.missing)))));

        // Tap "Lock" to lock
        onView(withText(R.string.lock_collection)).perform(click());

        // Verify button changed to "Edit"
        onView(withText(R.string.unlock_collection)).check(matches(isDisplayed()));

        // Tap coin — should NOT toggle (still "missing")
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(containsString(UITestHelper.getString(R.string.missing)))));

        // Tap "Edit" to unlock
        onView(withText(R.string.unlock_collection)).perform(click());

        // Verify button changed back to "Lock"
        onView(withText(R.string.lock_collection)).check(matches(isDisplayed()));

        // Tap coin — should toggle now
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(containsString(UITestHelper.getString(R.string.collected)))));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify the coin filter functionality.
     */
    @Test
    public void test_filterCoins() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Toggle first coin to "collected"
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(click());

        // Open overflow menu → "Filter Coins"
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.filter_coins)).perform(click());

        // Verify filter dialog
        onView(withText(R.string.filter_dialog_title)).check(matches(isDisplayed()));

        // Select "Show Collected"
        onView(withText(R.string.show_collected_coins)).perform(click());

        // Verify filter indicator is displayed
        onView(withId(R.id.filter_status_indicator)).check(matches(isDisplayed()));

        // Clear filter: overflow → Filter Coins → Show All
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.filter_coins)).perform(click());
        onView(withText(R.string.show_all_coins)).perform(click());

        // Verify filter indicator gone
        onView(withId(R.id.filter_status_indicator)).check(matches(not(isDisplayed())));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify adding a custom coin via the overflow menu.
     */
    @Test
    public void test_addCoin() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Open overflow menu → "Add Coin"
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.add_coin)).perform(click());

        // Verify dialog
        onView(withText(R.string.edit_coin_info)).check(matches(isDisplayed()));
        onView(withId(R.id.coin_name_edittext)).check(matches(isDisplayed()));
        onView(withId(R.id.coin_mint_edittext)).check(matches(isDisplayed()));

        // Enter name and mint
        onView(withId(R.id.coin_name_edittext))
                .perform(typeText("Custom Coin"), closeSoftKeyboard());
        onView(withId(R.id.coin_mint_edittext))
                .perform(typeText("P"), closeSoftKeyboard());

        // Tap "Okay"
        onView(withText(R.string.okay)).perform(click());

        // Wait for dialog to close
        UITestHelper.waitForDoesNotExist(withText(R.string.edit_coin_info));

        // Verify grid is still displayed
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify coin-level long-press actions: Toggle Collected and Delete.
     */
    @Test
    public void test_coinLongPressActions() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Long-press the first coin
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(longClick());

        // Verify "Coin Actions" dialog with 4 options
        onView(withText(R.string.coin_actions)).check(matches(isDisplayed()));
        onView(withText(R.string.toggle_collected)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed()));
        onView(withText(R.string.copy)).check(matches(isDisplayed()));
        onView(withText(R.string.delete)).check(matches(isDisplayed()));

        // Tap "Toggle Collected"
        onView(withText(R.string.toggle_collected)).perform(click());

        // Verify state changed to "collected"
        UITestHelper.waitForAssertion(() -> onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(containsString(UITestHelper.getString(R.string.collected))))));

        // Long-press again and delete
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(longClick());

        onView(withText(R.string.delete)).perform(click());

        // Wait for dialog to close / action to complete
        UITestHelper.waitForDoesNotExist(withText(R.string.coin_actions));

        // Grid should still be displayed
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }
}
