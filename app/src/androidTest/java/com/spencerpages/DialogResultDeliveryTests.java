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
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
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
 * Tests that answering a dialog does what it says, and - the part the rest of
 * the suite skips - that declining one leaves everything alone.
 * <p>
 * The migration routes every decision through
 * {@code BaseActivity.onDialogResult()}, where a confirm path and a cancel
 * path are separate branches. The existing tests exercise the confirm side of
 * each dialog; these cover the side that is supposed to do nothing, plus the
 * collection-options warning, which no test reached before.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class DialogResultDeliveryTests {

    private static final String COLLECTION_NAME = "Delivery Test";
    private static final String UNUSED_NAME = "Name That Should Not Stick";
    private static final String UNUSED_COIN_NAME = "Coin That Should Not Stick";

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
        UITestHelper.deleteAllCollections();
    }

    /**
     * Opens the test collection's coin grid
     */
    private static void openCollection() {
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
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
     * Test that dismissing the collection actions list takes no action at all
     */
    @Test
    public void test_collectionActionsDismissedByBackPress() {
        openCollectionActions();

        pressBack();

        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        onView(withText(COLLECTION_NAME)).check(matches(isDisplayed()));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that declining the delete confirmation reached through the menu
     * leaves the collection in place. The flow is two dialogs deep - a list to
     * pick the collection, then a confirmation - so the decision has to survive
     * being handed from one dialog to the next
     */
    @Test
    public void test_selectCollectionToDeleteCancelAtConfirmation() {
        UITestHelper.scrollToNavItems();
        onView(withText(R.string.delete_collection)).perform(click());

        UITestHelper.waitForDisplayed(withText(R.string.select_collection_delete));
        onView(withText(COLLECTION_NAME)).perform(click());

        UITestHelper.waitForDisplayed(withText(R.string.warning));
        onView(withText(R.string.no)).perform(click());

        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that confirming the rename dialog with an empty name is rejected and
     * leaves the collection named as it was
     */
    @Test
    public void test_renameCollectionEmptyNameRejected() {
        openCollection();

        onView(withId(R.id.rename_collection)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.select_collection_name));
        onView(withId(R.id.dialog_text_input)).perform(clearText(), closeSoftKeyboard());
        onView(withText(R.string.okay)).perform(click());

        UITestHelper.waitForDoesNotExist(withText(R.string.select_collection_name));
        pressBack();
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that cancelling the rename dialog keeps the original name, even
     * though a new one had been typed
     */
    @Test
    public void test_renameCollectionCancelKeepsName() {
        openCollection();

        onView(withId(R.id.rename_collection)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.select_collection_name));
        onView(withId(R.id.dialog_text_input))
                .perform(replaceText(UNUSED_NAME), closeSoftKeyboard());
        onView(withText(R.string.cancel)).perform(click());

        UITestHelper.waitForDoesNotExist(withText(R.string.select_collection_name));
        pressBack();
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));
        UITestHelper.waitForDoesNotExist(withText(UNUSED_NAME));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that cancelling the coin edit dialog leaves the coin untouched
     */
    @Test
    public void test_coinEditCancelKeepsCoinName() {
        openCollection();

        onData(anything())
                .inAdapterView(withId(R.id.standard_collection_page))
                .atPosition(0)
                .perform(longClick());
        UITestHelper.waitForDisplayed(withText(R.string.edit));
        onView(withText(R.string.edit)).perform(click());

        UITestHelper.waitForDisplayed(withId(R.id.coin_name_edittext));
        onView(withId(R.id.coin_name_edittext))
                .perform(replaceText(UNUSED_COIN_NAME), closeSoftKeyboard());
        onView(withText(R.string.cancel)).perform(click());

        UITestHelper.waitForDoesNotExist(withText(R.string.edit_coin_info));
        UITestHelper.waitForDoesNotExist(withText(UNUSED_COIN_NAME));
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that dismissing the coin filter list leaves the current filter
     * alone rather than applying whichever entry happened to be first
     */
    @Test
    public void test_coinFilterDismissedByBackPressKeepsAllCoinsVisible() {
        openCollection();

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.filter_coins)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.filter_dialog_title));

        pressBack();

        UITestHelper.waitForDoesNotExist(withText(R.string.filter_dialog_title));
        onView(withId(R.id.standard_collection_page)).check(matches(isDisplayed()));
        onView(withId(R.id.filter_status_indicator)).check(matches(not(isDisplayed())));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that declining the warning shown when collection options would drop
     * coins abandons the update and leaves the user on the edit page
     */
    @Test
    public void test_collectionOptionsWarningDeclineKeepsEditPage() {
        openCollectionOptionsWarning();

        onView(withText(R.string.no)).perform(click());

        UITestHelper.waitForDoesNotExist(withText(R.string.warning_collection_type_change));
        onView(withId(R.id.create_page)).check(matches(isDisplayed()));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that accepting the same warning goes through with the update
     */
    @Test
    public void test_collectionOptionsWarningConfirmAppliesUpdate() {
        openCollectionOptionsWarning();

        onView(withText(R.string.yes)).perform(click());

        UITestHelper.waitForMainActivity();
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Opens the collection for editing and changes its coin type, which is one
     * of the changes that would drop the coins already in the collection and so
     * raises the options warning
     */
    private static void openCollectionOptionsWarning() {
        openCollectionActions();
        onView(withText(R.string.edit)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.edit_enter_collection_name));

        // Switching the coin type is what the collection is warned about
        onView(withId(R.id.coin_selector)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Nickels"))).perform(click());
        onView(withId(R.id.create_page)).perform(scrollTo(), click());

        UITestHelper.waitForDisplayed(withText(R.string.warning_collection_type_change));
    }
}
