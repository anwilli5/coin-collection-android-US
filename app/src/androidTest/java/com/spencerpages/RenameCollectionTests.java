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
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.widget.EditText;

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
 * Tests for renaming a collection via the toolbar button.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class RenameCollectionTests {

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
     * Verify collection renaming via the toolbar button.
     */
    @Test
    public void test_renameCollection() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Tap the Rename button in toolbar
        onView(withId(R.id.rename_collection)).perform(click());

        // Verify rename dialog appears with title
        onView(withText(R.string.select_collection_name)).check(matches(isDisplayed()));

        // The dialog has an EditText pre-filled with the current name
        // Clear and enter new name
        onView(isAssignableFrom(EditText.class))
                .perform(clearText(), replaceText("Renamed Collection"), closeSoftKeyboard());

        // Tap "Okay" to confirm
        onView(withText(R.string.okay)).perform(click());

        // Verify toolbar title updated
        UITestHelper.waitForDisplayed(withText("Renamed Collection"));

        // Rename back to original name
        onView(withId(R.id.rename_collection)).perform(click());
        onView(isAssignableFrom(EditText.class))
                .perform(clearText(), replaceText(COLLECTION_NAME), closeSoftKeyboard());
        onView(withText(R.string.okay)).perform(click());

        // Verify title restored
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }
}
