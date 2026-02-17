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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.allOf;

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
 * Test that verifies tutorial dialogs appear on first use.
 * All other tests suppress tutorials — this one explicitly enables them.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class TutorialDialogsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.ensureDbOpen();
        UITestHelper.deleteAllCollections();
        // Enable all tutorials so they appear fresh
        UITestHelper.resetTutorials();
    }

    @After
    public void tearDown() {
        // Suppress tutorials and clean up for other tests
        UITestHelper.suppressAllTutorials();
        UITestHelper.deleteAllCollections();
    }

    /**
     * Verify tutorial dialogs appear at key points during first use.
     *
     * This test follows the natural first-use flow:
     * 1. App launch → intro tutorial (screen1)
     * 2. New Collection → creation tutorial (screen2)
     * 3. Open collection → add/lock tutorial (screen3)
     * 4. Back to main → more options tutorial (screen4, because mNumberOfCollections > 0)
     * 5. Open collection again → edit/copy/delete tutorial (screen5)
     */
    @Test
    public void test_tutorialDialogsAppear() {
        // Dismiss any tutorial dialog from the initial ActivityScenarioRule launch
        // (tutorials may have been enabled from a previous state)
        UITestHelper.dismissTutorialDialogs();
        // Ensure tutorials are enabled for this test
        UITestHelper.resetTutorials();
        UITestHelper.ensureDbOpen();

        // Relaunch with tutorials enabled
        activityRule.getScenario().recreate();

        // Wait for the intro tutorial dialog to appear
        UITestHelper.waitForDisplayed(withText(R.string.intro_message), 10_000);

        // 1. Intro tutorial should appear on first launch
        onView(withText(R.string.intro_message)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_exp)).perform(click());

        // Verify main activity is now visible
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));

        // 2. Tap "New Collection" — creation page tutorial should appear
        onView(withText(R.string.create_new_collection)).perform(click());
        onView(withText(R.string.tutorial_select_coin_and_create)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_exp)).perform(click());

        // Verify creation page is visible
        onView(withId(R.id.edit_enter_collection_name)).check(matches(isDisplayed()));

        // Select a coin type from the spinner (default is "Select Collection Type" which is invalid)
        onView(withId(R.id.coin_selector)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Pennies"))).perform(click());

        // Create a collection to populate the main activity list
        onView(withId(R.id.edit_enter_collection_name))
                .perform(typeText("Tutorial Test"), closeSoftKeyboard());
        onView(withId(R.id.create_page)).perform(scrollTo(), click());

        // Wait for async creation to complete and return to main activity
        UITestHelper.waitForMainActivity();

        // 3. Open the collection — add/lock tutorial should appear (screen3)
        onView(withText("Tutorial Test")).perform(click());
        onView(withText(R.string.tutorial_add_to_and_lock_collection)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_exp)).perform(click());

        // Verify collection page is visible
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));

        // 4. Go back to main activity — more options tutorial should appear (screen4)
        //    because onResume() sees mNumberOfCollections > 0 on this return visit
        pressBack();
        onView(withText(R.string.tutorial_more_options)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_exp)).perform(click());

        // Verify main activity is visible
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));

        // 5. Open collection AGAIN — edit/copy/delete tutorial should appear (screen5)
        //    (only shows when screen3 didn't show on this visit)
        onView(withText("Tutorial Test")).perform(click());
        onView(withText(R.string.tutorial_edit_copy_delete_coins)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_exp)).perform(click());

        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }
}
