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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.fail;

import android.os.Build;
import android.os.RemoteException;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.uiautomator.UiDevice;

import com.coincollection.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for export and rotation scenarios.
 * <p>
 * Basic navigation tests (main activity, creation page, delete, app info)
 * and Parcelable tests have been moved to dedicated test classes:
 * - MainActivityTests, CollectionPageBasicViewTests, etc. for navigation
 * - ParcelableTests for serialization tests
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class ExportCollectionTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
        // Create a collection so export tests have data to work with
        UITestHelper.createLincolnCentsCollection("Lincoln Cents", 0);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Set orientation left
     */
    public static void setOrientationLeft() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            device.setOrientationLeft();
        } catch (RemoteException e) {
            fail();
        }
    }

    /**
     * Set orientation natural
     */
    public static void setOrientationNatural() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            device.setOrientationNatural();
        } catch (RemoteException e) {
            fail();
        }
    }

    /**
     * Test exporting existing collections — verify format picker appears
     */
    @Test
    public void test_exportCollections() {
        UITestHelper.recreateActivity(activityRule);
        UITestHelper.scrollToNavItems();
        onView(withText(R.string.export_collection)).perform(click());
        // Verify format picker dialog appears with JSON and CSV options
        onView(withText(R.string.json_file)).check(matches(isDisplayed()));
    }

    /**
     * Test that export dialog and rotation work without crashing.
     * Verifies the format picker dialog survives a rotation.
     */
    @Test
    public void test_exportCollectionsAndRotate() {
        UITestHelper.recreateActivity(activityRule);
        UITestHelper.scrollToNavItems();
        onView(withText(R.string.export_collection)).perform(click());
        // Verify format picker appears
        onView(withText(R.string.json_file)).check(matches(isDisplayed()));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setOrientationLeft();
            // After rotation, verify the activity recovers
            UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
            setOrientationNatural();
        }
    }
}