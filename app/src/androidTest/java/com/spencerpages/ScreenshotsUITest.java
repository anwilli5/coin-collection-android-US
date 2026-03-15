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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar;

/**
 * Screengrab tests for generating app store screenshots.
 * Uses UITestHelper to programmatically set up required collections
 * and suppress tutorial dialogs for reliable, repeatable screenshots.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ScreenshotsUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void beforeAll() {
        CleanStatusBar.enableWithDefaults();
        UITestHelper.disableSystemAnimations();
    }

    @AfterClass
    public static void afterAll() {
        CleanStatusBar.disable();
        UITestHelper.enableSystemAnimations();
    }

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
        // Create the collections needed for screenshots with some coins marked as collected
        // to produce more visually interesting screenshots:
        // - Lincoln Cents (index 0) ~73% collected
        // - National Park Quarters (index 5) ~63% collected
        // - Presidential Dollars (index 10) ~20% collected
        // - Morgan Dollars (index 22) ~38% collected (advanced view)
        UITestHelper.createCollectionWithCollected("Lincoln Cents", 0, 0,
                CollectionPage.SIMPLE_DISPLAY, 0.73);
        UITestHelper.createCollectionWithCollected("National Park Quarters", 5, 1,
                CollectionPage.SIMPLE_DISPLAY, 0.63);
        UITestHelper.createCollectionWithCollected("Presidential Dollars", 10, 2,
                CollectionPage.SIMPLE_DISPLAY, 0.45);
        UITestHelper.createCollectionWithCollected("Morgan Dollars", 22, 3,
                CollectionPage.ADVANCED_DISPLAY, 0.38);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Main Activity
     */
    @Test
    public void test_MainActivity() {
        UITestHelper.recreateActivity(activityRule);
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
        waitForRender();
        Screengrab.screenshot("main_screen");
    }

    /**
     * Presidential Dollars
     */
    @Test
    public void test_PresidentialDollarsActivity() {
        UITestHelper.recreateActivity(activityRule);
        onView(withText("Presidential Dollars")).perform(click());
        UITestHelper.dismissTutorialDialogs();
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));
        waitForRender();
        Screengrab.screenshot("presidential_dollars_screen");
    }

    /**
     * Lincoln Cents
     */
    @Test
    public void test_LincolnCentsActivity() {
        UITestHelper.recreateActivity(activityRule);
        onView(withText("Lincoln Cents")).perform(click());
        UITestHelper.dismissTutorialDialogs();
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));
        waitForRender();
        Screengrab.screenshot("lincoln_cents_screen");
    }

    /**
     * Morgan Dollars Advanced
     */
    @Test
    public void test_MorganDollarsActivity() {
        UITestHelper.recreateActivity(activityRule);
        onView(withText("Morgan Dollars")).perform(click());
        UITestHelper.dismissTutorialDialogs();
        onView(withId(R.id.advanced_collection_page)).check(matches(isDisplayed()));
        waitForRender();
        Screengrab.screenshot("morgan_dollars_screen");
    }

    /**
     * New Collection Page
     */
    @Test
    public void test_NewCollectionActivity() {
        UITestHelper.recreateActivity(activityRule);
        UITestHelper.scrollToNavItems();
        onView(withText(R.string.create_new_collection)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        onView(withId(R.id.edit_enter_collection_name)).check(matches(isDisplayed()));

        // Select a coin type so optional parameters become visible
        onView(withId(R.id.coin_selector)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Mercury Dimes"))).perform(click());

        // Use replaceText instead of typeText to avoid double-capitalisation
        // caused by android:inputType="textCapWords" on the EditText
        onView(withId(R.id.edit_enter_collection_name)).perform(replaceText("Mercury Dimes"));
        onView(withId(R.id.edit_enter_collection_name)).perform(closeSoftKeyboard());
        onView(withId(R.id.check_show_mint_mark)).perform(scrollTo(), click());
        onView(withId(R.id.check_edit_date_range)).perform(scrollTo(), click());
        waitForRender();
        Screengrab.screenshot("collection_creation_screen");
    }

    /**
     * Coin Actions
     */
    @Test
    public void test_CoinActionsActivity() {
        UITestHelper.recreateActivity(activityRule);
        onView(withText("Presidential Dollars")).perform(click());
        UITestHelper.dismissTutorialDialogs();
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));
        waitForRender();
        // Long-press a deterministic coin slot in the grid (avoid relying on off-screen text views)
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(longClick());
        Screengrab.screenshot("coin_actions_screen");
    }

    /**
     * Wait for all views to finish rendering by letting the UI go idle.
     * Replaces fixed sleeps with dynamic synchronization via UiAutomator.
     */
    private static void waitForRender() {
        UiDevice.getInstance(getInstrumentation()).waitForIdle();
    }
}