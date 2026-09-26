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
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that dialogs survive a configuration change instead of leaking the
 * window they were attached to and silently discarding the user's decision.
 * <p>
 * Each test opens a dialog, rotates, and checks that the dialog is still there
 * and still does what it was asked to do. The final check scans logcat for the
 * WindowLeaked entries that the old, untracked dialogs produced.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class DialogRotationTests {

    private static final String COLLECTION_NAME = "Rotation Test";
    private static final String RENAMED_COIN = "Renamed Coin";
    private static final String RENAMED_COLLECTION = "Rotation Test Renamed";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.ensureDbOpen();
        UITestHelper.suppressAllTutorials();
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsCollection(COLLECTION_NAME, 0);
        UITestHelper.unlockCollection(COLLECTION_NAME);
        UITestHelper.recreateActivity(activityRule);
        UITestHelper.clearLogcat();
    }

    @After
    public void tearDown() {
        UITestHelper.setOrientationNatural();
        UITestHelper.clearCoinFilter(COLLECTION_NAME);
        UITestHelper.clearCoinFilter(RENAMED_COLLECTION);
        UITestHelper.deleteAllCollections();
    }

    /**
     * Opens the test collection and long-presses its first coin to bring up
     * the coin actions list
     */
    private static void openFirstCoinActions() {
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(longClick());
        UITestHelper.waitForDisplayed(withText(R.string.toggle_collected));
    }

    /**
     * Opens the long-press actions list for the test collection
     */
    private static void openCollectionActions() {
        onData(UITestHelper.withCollectionName(COLLECTION_NAME))
                .inAdapterView(withId(R.id.main_activity_listview))
                .perform(longClick());
        UITestHelper.waitForDisplayed(withText(R.string.delete));
    }

    /**
     * Test that the collection actions list survives a rotation
     */
    @Test
    public void test_collectionActionsSurviveRotation() {
        openCollectionActions();
        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.delete));
        pressBack();
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that picking an action from the collection actions list after a
     * rotation still works. The recreated activity never regains window focus
     * while the dialog is up, so it has to refresh its collection list from a
     * lifecycle callback instead
     */
    @Test
    public void test_collectionActionsWorkAfterRotation() {
        openCollectionActions();
        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.delete));

        // Viewing after the rotation must still open the collection
        onView(withText(R.string.view)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the delete confirmation survives a rotation and that
     * confirming afterwards still deletes the collection it was opened for
     */
    @Test
    public void test_deleteConfirmationSurvivesRotationAndDeletes() {
        openCollectionActions();
        onView(withText(R.string.delete)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.warning));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.warning));

        // Confirming after the rotation must still act on the right collection
        onView(withText(R.string.yes)).perform(click());
        UITestHelper.waitForDoesNotExist(withText(COLLECTION_NAME));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that dismissing the delete confirmation after a rotation leaves the
     * collection alone
     */
    @Test
    public void test_deleteConfirmationSurvivesRotationAndCancels() {
        openCollectionActions();
        onView(withText(R.string.delete)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.warning));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.warning));

        onView(withText(R.string.no)).perform(click());
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the coin actions list survives a rotation and still acts on
     * the coin it was opened for. The dialog identifies its coin by database
     * id precisely because the coin list is rebuilt on the recreated activity
     */
    @Test
    public void test_coinActionsSurviveRotationAndActOnTappedCoin() {
        openFirstCoinActions();

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.toggle_collected));

        // Toggling after the rotation must still reach the tapped coin
        onView(withText(R.string.toggle_collected)).perform(click());
        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.coinImage))
                .check(matches(withContentDescription(
                        containsString(UITestHelper.getString(R.string.collected)))));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the coin edit dialog survives a rotation, keeping both the
     * text the user has typed and the coin it applies to
     */
    @Test
    public void test_coinEditDialogSurvivesRotation() {
        openFirstCoinActions();
        onView(withText(R.string.edit)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.coin_name_edittext));

        // Type a new name, then rotate before confirming
        onView(withId(R.id.coin_name_edittext)).perform(replaceText(RENAMED_COIN), closeSoftKeyboard());
        UITestHelper.setOrientationLeft();
        UITestHelper.waitForDisplayed(withId(R.id.coin_name_edittext));
        UITestHelper.setOrientationNatural();
        UITestHelper.waitForDisplayed(withId(R.id.coin_name_edittext));

        // The typed text survived, and confirming renames the tapped coin
        onView(withId(R.id.coin_name_edittext)).check(matches(withText(RENAMED_COIN)));
        onView(withText(R.string.okay)).perform(click());
        UITestHelper.waitForDisplayed(withText(RENAMED_COIN));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the about box survives a rotation
     */
    @Test
    public void test_aboutDialogSurvivesRotation() {
        UITestHelper.scrollToNavItems();
        onView(withText(R.string.app_info)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.info_attribution));

        UITestHelper.setOrientationLeft();
        UITestHelper.waitForDisplayed(withId(R.id.info_attribution));
        UITestHelper.setOrientationNatural();
        UITestHelper.waitForDisplayed(withId(R.id.info_attribution));

        pressBack();
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the rename dialog survives a rotation, keeping the text the
     * user had typed, and still renames the collection afterwards
     */
    @Test
    public void test_renameDialogSurvivesRotation() {
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        onView(withId(R.id.rename_collection)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.dialog_text_input));
        onView(withId(R.id.dialog_text_input))
                .perform(replaceText(RENAMED_COLLECTION), closeSoftKeyboard());

        UITestHelper.rotateAndAssertStillDisplayed(withId(R.id.dialog_text_input));

        // The typed text survived, and confirming renames the collection
        onView(withId(R.id.dialog_text_input)).check(matches(withText(RENAMED_COLLECTION)));
        onView(withText(R.string.okay)).perform(click());
        UITestHelper.waitForDisplayed(withText(RENAMED_COLLECTION));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the coin filter list survives a rotation and still applies the
     * filter picked afterwards
     */
    @Test
    public void test_coinFilterSurvivesRotation() {
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.filter_coins)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.filter_dialog_title));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.filter_dialog_title));

        onView(withText(R.string.show_collected_coins)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.filter_status_indicator));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the unsaved-changes prompt survives a rotation and still
     * leaves the page when confirmed
     */
    @Test
    public void test_unsavedChangesExitSurvivesRotation() {
        UITestHelper.setCollectionDisplayType(COLLECTION_NAME, CollectionPage.ADVANCED_DISPLAY);
        UITestHelper.recreateActivity(activityRule);

        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.advanced_collection_page));

        // Changing a grade is what marks the page as having unsaved changes
        onData(anything())
                .inAdapterView(withId(R.id.advanced_collection_page))
                .atPosition(0)
                .onChildView(withId(R.id.grade_selector))
                .perform(click());
        onData(anything()).atPosition(1).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.unsaved_message_textview));

        pressBack();
        UITestHelper.waitForDisplayed(withText(R.string.dialog_unsaved_changes_exit));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.dialog_unsaved_changes_exit));

        onView(withText(R.string.okay)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the warning raised when collection options would drop coins
     * survives a rotation and can still be declined
     */
    @Test
    public void test_collectionOptionsWarningSurvivesRotation() {
        openCollectionActions();
        onView(withText(R.string.edit)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.edit_enter_collection_name));

        onView(withId(R.id.coin_selector)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Nickels"))).perform(click());
        onView(withId(R.id.create_page)).perform(scrollTo(), click());
        UITestHelper.waitForDisplayed(withText(R.string.warning_collection_type_change));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.warning_collection_type_change));

        onView(withText(R.string.no)).perform(click());
        UITestHelper.waitForDoesNotExist(withText(R.string.warning_collection_type_change));
        onView(withId(R.id.create_page)).check(matches(isDisplayed()));
        UITestHelper.assertNoLeakedWindows();
    }
}
