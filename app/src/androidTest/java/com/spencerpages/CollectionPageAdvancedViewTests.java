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
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import android.os.Build;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.uiautomator.UiDevice;

import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Tests for the collection page in advanced (list) view:
 * grade editing, unsaved changes, view switching, unsaved blocks view switch.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class CollectionPageAdvancedViewTests {

    private static final String COLLECTION_NAME = "Lincoln Cents Test";
    private static final String NAV_OVERLAY_PREFIX = "com.android.internal.systemui.navbar.";
    private static final String NAV_MODE_GESTURAL = "2";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsAdvancedCollection(COLLECTION_NAME, 0);
        UITestHelper.unlockCollection(COLLECTION_NAME);
    }

    @After
    public void tearDown() {
        UITestHelper.deleteAllCollections();
    }

    /**
     * Verify switching to advanced view and editing coin details.
     */
    @Test
    public void test_advancedViewEditGrade() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();

        // Verify advanced view is displayed
        UITestHelper.waitForDisplayed(withId(R.id.advanced_collection_page));

        // Verify "Save" button is visible (unlocked advanced mode shows "Save")
        onView(withText(R.string.lock_collection_adv)).check(matches(isDisplayed()));

        // Verify grade_selector spinner is present on first item
        onData(anything())
                .inAdapterView(withId(R.id.advanced_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.grade_selector))
                .check(matches(isDisplayed()));

        // Click the grade spinner on the first coin
        onData(anything())
                .inAdapterView(withId(R.id.advanced_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.grade_selector))
                .perform(click());

        // Select "AG-3" from the dropdown
        onData(allOf(is(instanceOf(String.class)), is(UITestHelper.getStringArrayItem(R.array.coin_grades, 3))))
            .perform(click());

        // Verify "Unsaved Changes" text appears
        onView(withId(R.id.unsaved_message_textview)).check(matches(isDisplayed()));
        onView(withText(R.string.unsaved_message)).check(matches(isDisplayed()));

        // Tap "Save" in toolbar — saves and locks
        onView(withText(R.string.lock_collection_adv)).perform(click());

        // Wait for activity restart (save triggers restart in advanced mode)
        UITestHelper.waitForDisplayed(withText(R.string.unlock_collection));

        // After saving and locking, button should show "Edit"
        onView(withText(R.string.unlock_collection)).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify the unsaved changes confirmation dialog on back press.
     */
    @Test
    public void test_unsavedChangesBackNavDialog() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.advanced_collection_page));

        // Change a grade spinner value to create unsaved state
        onData(anything())
                .inAdapterView(withId(R.id.advanced_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.grade_selector))
                .perform(click());

        onData(allOf(is(instanceOf(String.class)), is(UITestHelper.getStringArrayItem(R.array.coin_grades, 3))))
            .perform(click());

        // Verify "Unsaved Changes" appears
        onView(withId(R.id.unsaved_message_textview)).check(matches(isDisplayed()));

        // Press back
        pressBack();

        // Verify confirmation dialog appears
        onView(withText(R.string.dialog_unsaved_changes_exit)).check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).check(matches(isDisplayed()));
        onView(withText(R.string.okay)).check(matches(isDisplayed()));

        // Tap "Cancel" — should remain on collection page
        onView(withText(R.string.cancel)).perform(click());
        onView(withId(R.id.advanced_collection_page)).check(matches(isDisplayed()));

        // Press back again
        pressBack();

        // Dialog appears again
        onView(withText(R.string.dialog_unsaved_changes_exit)).check(matches(isDisplayed()));

        // Tap "Okay" — should return to main activity
        onView(withText(R.string.okay)).perform(click());

        // Verify return to main activity
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify switching from advanced back to basic view.
     */
    @Test
    public void test_switchAdvancedToBasic() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection (already set to advanced display)
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();

        // Verify advanced view
        UITestHelper.waitForDisplayed(withId(R.id.advanced_collection_page));

        // Open overflow menu and switch to "Basic View"
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.simple_view_string)).perform(click());

        // Wait for activity restart
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        // Verify basic (grid) view is shown
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Verify that switching views is blocked when unsaved changes exist.
     */
    @Test
    public void test_unsavedChangesBlocksViewSwitch() {
        UITestHelper.recreateActivity(activityRule);

        // Open the collection (advanced view)
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.advanced_collection_page));

        // Make a change to create unsaved state
        onData(anything())
                .inAdapterView(withId(R.id.advanced_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.grade_selector))
                .perform(click());

        onData(allOf(is(instanceOf(String.class)), is(UITestHelper.getStringArrayItem(R.array.coin_grades, 1))))
            .perform(click());

        // Verify "Unsaved Changes"
        onView(withId(R.id.unsaved_message_textview)).check(matches(isDisplayed()));

        // Try switching to basic view
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.simple_view_string)).perform(click());

        // Verify blocking dialog appears with only "Okay" button
        onView(withText(R.string.dialog_unsaved_changes_change_views)).check(matches(isDisplayed()));
        onView(withText(R.string.okay)).check(matches(isDisplayed()));

        // Tap "Okay" to dismiss — should stay on advanced view
        onView(withText(R.string.okay)).perform(click());

        // Still on advanced view
        onView(withId(R.id.advanced_collection_page)).check(matches(isDisplayed()));

        // Save changes
        onView(withText(R.string.lock_collection_adv)).perform(click());

        // Wait for save and activity restart
        UITestHelper.waitForDisplayed(withText(R.string.unlock_collection));

        // After saving, button changes to "Edit"
        onView(withText(R.string.unlock_collection)).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }
    /**
     * Verify that with gesture navigation, Back after the page's first edit
     * closes the keyboard and only a second Back shows the leave dialog.
     * Android 13+ gives Back to the most recently registered handler, so a back
     * callback enabled at the first edit, while the keyboard is open, takes
     * Back ahead of the keyboard. An injected KEYCODE_BACK doesn't go through
     * that dispatch, so this swipes in from the screen edge like a user.
     */
    @Test
    public void test_backGestureClosesKeyboardBeforeLeaveDialog() throws Exception {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        String navMode = device.executeShellCommand("settings get secure navigation_mode").trim();
        String showIme = device.executeShellCommand("settings get secure show_ime_with_hard_keyboard").trim();
        try {
            // Gesture navigation, and the soft keyboard even if the emulator
            // reports a hardware one
            device.executeShellCommand("cmd overlay enable " + NAV_OVERLAY_PREFIX + "gestural");
            device.executeShellCommand("settings put secure show_ime_with_hard_keyboard 1");
            UITestHelper.waitForAssertion(() -> {
                try {
                    assertEquals(NAV_MODE_GESTURAL,
                            device.executeShellCommand("settings get secure navigation_mode").trim());
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            });

            UITestHelper.recreateActivity(activityRule);
            onView(withText(COLLECTION_NAME)).perform(click());
            UITestHelper.dismissTutorialDialogs();
            UITestHelper.waitForDisplayed(withId(R.id.advanced_collection_page));

            // Tap the first coin's notes and wait for the keyboard, then make the
            // page's first unsaved change, as a user typing would
            onData(anything())
                    .inAdapterView(withId(R.id.advanced_collection_page))
                    .atPosition(0)
                    .onChildView(withId(R.id.notes_edit_text))
                    .perform(click());
            waitForKeyboard(true);
            onData(anything())
                    .inAdapterView(withId(R.id.advanced_collection_page))
                    .atPosition(0)
                    .onChildView(withId(R.id.notes_edit_text))
                    .perform(replaceText("Note"));
            onView(withId(R.id.unsaved_message_textview)).check(matches(isDisplayed()));

            // The first Back closes only the keyboard
            swipeBackFromEdge(device);
            waitForKeyboard(false);
            onView(withText(R.string.dialog_unsaved_changes_exit)).check(doesNotExist());

            // The second Back asks before discarding the edit
            swipeBackFromEdge(device);
            UITestHelper.waitForDisplayed(withText(R.string.dialog_unsaved_changes_exit));
            onView(withText(R.string.cancel)).perform(click());
            onView(withId(R.id.advanced_collection_page)).check(matches(isDisplayed()));
        } finally {
            if (!NAV_MODE_GESTURAL.equals(navMode)) {
                device.executeShellCommand("cmd overlay enable " + NAV_OVERLAY_PREFIX
                        + ("1".equals(navMode) ? "twobutton" : "threebutton"));
            }
            device.executeShellCommand("settings put secure show_ime_with_hard_keyboard "
                    + ("null".equals(showIme) ? "0" : showIme));
        }
    }

    /**
     * Swipe in from the left edge of the screen: the back gesture under
     * gesture navigation
     */
    private static void swipeBackFromEdge(UiDevice device) {
        int y = device.getDisplayHeight() / 2;
        device.swipe(1, y, device.getDisplayWidth() * 2 / 5, y, 50);
    }

    /**
     * Wait until the soft keyboard is shown or hidden
     */
    private static void waitForKeyboard(boolean shown) {
        UITestHelper.waitForAssertion(() -> onView(withId(R.id.advanced_collection_page))
                .check((view, noViewFoundException) -> {
                    if (noViewFoundException != null) {
                        throw noViewFoundException;
                    }
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
                    assertEquals("keyboard shown", shown,
                            insets != null && insets.isVisible(WindowInsetsCompat.Type.ime()));
                }));
    }
}
