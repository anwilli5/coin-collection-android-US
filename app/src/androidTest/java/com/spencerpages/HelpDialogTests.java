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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.coincollection.dialog.DialogRequests.TAG_HELP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
 * Tests for the one-time help tips, which are ordinary dialog fragments shown
 * through {@code BaseActivity.createAndShowHelpDialog()}.
 * <p>
 * Two of these guard defects that no other suite could reach. The reorder tip
 * is shown from a fragment's {@code onCreateView}, which runs inside a
 * FragmentManager transaction - settling pending transactions from there threw
 * and crashed the app. And a tip that has been acknowledged must not keep a
 * later tip from being shown: a destroyed dialog fragment is reset for reuse
 * rather than left in a destroyed state, so a liveness check that trusts the
 * fragment's lifecycle keeps reporting the old tip as showing.
 * <p>
 * {@code UITestHelper.suppressAllTutorials()} clears every tip, so each test
 * leaves exactly one tip outstanding and knows which dialog it is looking at.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class HelpDialogTests {

    private static final String COLLECTION_NAME = "Help Tip Test";
    private static final String SECOND_COLLECTION_NAME = "Help Tip Test Two";

    private static final String TIP_INTRO = "first_Time_screen1";
    private static final String TIP_MORE_OPTIONS = "first_Time_screen4";
    private static final String TIP_REORDER = "reorder_help1";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        UITestHelper.ensureDbOpen();
        UITestHelper.suppressAllTutorials();
        // The rule launches the activity before this runs, so a tip left over
        // from an earlier test may already be up. It has to go before the tests
        // recreate the activity, or the FragmentManager restores it and the
        // activity under test never shows a tip of its own
        UITestHelper.dismissTutorialDialogs();
        UITestHelper.deleteAllCollections();
        UITestHelper.createLincolnCentsCollection(COLLECTION_NAME, 0);
        UITestHelper.unlockCollection(COLLECTION_NAME);
        UITestHelper.createLincolnCentsCollection(SECOND_COLLECTION_NAME, 1);
        UITestHelper.unlockCollection(SECOND_COLLECTION_NAME);
        UITestHelper.clearLogcat();
    }

    @After
    public void tearDown() {
        UITestHelper.setOrientationNatural();
        UITestHelper.suppressAllTutorials();
        UITestHelper.deleteAllCollections();
    }

    /**
     * Relaunch the activity so its onCreate re-reads the tutorial preferences.
     * {@code UITestHelper.recreateActivity} can't be used for this - it
     * dismisses every tip it finds, which is exactly what these tests assert on
     */
    private void recreateKeepingTips() {
        activityRule.getScenario().recreate();
        UITestHelper.waitForWindowFocus();
    }

    /**
     * @return how many help dialogs the activity is currently showing
     */
    private int countHelpDialogs() {
        final int[] count = new int[1];
        activityRule.getScenario().onActivity(activity ->
                count[0] = UITestHelper.countDialogsWithTagPrefix(activity, TAG_HELP));
        return count[0];
    }

    /**
     * Test that the reorder tip is shown when the reorder fragment opens.
     * <p>
     * The tip is raised from {@code ReorderCollections.onCreateView()}, which
     * the FragmentManager runs while it is executing the transaction that adds
     * the fragment. Asking that same FragmentManager to settle its pending
     * transactions from there throws IllegalStateException, so before the fix
     * this crashed for every user who had not yet acknowledged the tip
     */
    @Test
    public void test_reorderTipShownFromFragmentOnCreateView() {
        // The tip must be marked pending after the recreate: the helper
        // suppresses every tutorial as part of rebuilding the activity
        UITestHelper.recreateActivity(activityRule);
        UITestHelper.setTutorialPending(TIP_REORDER, true);

        onView(withText(R.string.reorder_collection)).perform(click());

        // The app must still be alive and the tip must be up
        UITestHelper.waitForDisplayed(withText(R.string.tutorial_reorder_collections));
        onView(withText(R.string.okay_exp)).perform(click());

        UITestHelper.waitForDisplayed(withId(R.id.reorder_collections_recycler_view));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that the reorder tip survives a rotation. The fragment's
     * onCreateView runs again on the recreated activity and asks for the tip a
     * second time, which must keep the restored dialog rather than stacking a
     * second one on top of it
     */
    @Test
    public void test_reorderTipSurvivesRotation() {
        UITestHelper.recreateActivity(activityRule);
        UITestHelper.setTutorialPending(TIP_REORDER, true);

        onView(withText(R.string.reorder_collection)).perform(click());
        UITestHelper.waitForDisplayed(withText(R.string.tutorial_reorder_collections));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.tutorial_reorder_collections));
        assertEquals("Only one reorder tip should be showing", 1, countHelpDialogs());

        onView(withText(R.string.okay_exp)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.reorder_collections_recycler_view));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a tip survives a rotation without being torn down and shown
     * again. The activity asks for the same tip on every onCreate, so the
     * restored dialog has to be kept rather than replaced
     */
    @Test
    public void test_introTipSurvivesRotationWithoutBeingReplaced() {
        UITestHelper.setTutorialPending(TIP_INTRO, true);
        recreateKeepingTips();
        UITestHelper.waitForDisplayed(withText(R.string.intro_message));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.intro_message));
        assertEquals("Only one intro tip should be showing", 1, countHelpDialogs());

        onView(withText(R.string.okay_exp)).perform(click());
        UITestHelper.waitForDoesNotExist(withText(R.string.intro_message));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a tip raised after an earlier one was acknowledged is still
     * shown. The acknowledged tip is destroyed, and a destroyed dialog fragment
     * is reset for reuse - so a liveness check based on the fragment's
     * lifecycle keeps reporting it as showing and silently swallows this tip
     */
    @Test
    public void test_laterTipShownAfterEarlierAcknowledged() {
        UITestHelper.setTutorialPending(TIP_INTRO, true);
        UITestHelper.setTutorialPending(TIP_MORE_OPTIONS, true);
        recreateKeepingTips();

        // Acknowledge the intro tip, which leaves a destroyed tip behind
        UITestHelper.waitForDisplayed(withText(R.string.intro_message));
        onView(withText(R.string.okay_exp)).perform(click());
        UITestHelper.waitForDisplayed(withText(COLLECTION_NAME));

        // Coming back to the list raises the next tip from onResume()
        onView(withText(COLLECTION_NAME)).perform(click());
        UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
        pressBack();

        UITestHelper.waitForDisplayed(withText(R.string.tutorial_more_options));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that acknowledging a tip sticks: the preference is written and the
     * tip does not come back when the activity is recreated
     */
    @Test
    public void test_tipAcknowledgementPersistsAcrossRotation() {
        UITestHelper.setTutorialPending(TIP_INTRO, true);
        recreateKeepingTips();

        UITestHelper.waitForDisplayed(withText(R.string.intro_message));
        onView(withText(R.string.okay_exp)).perform(click());
        UITestHelper.waitForDoesNotExist(withText(R.string.intro_message));

        UITestHelper.waitForAssertion(() -> assertFalse(
                "Acknowledging the tip should clear its preference",
                UITestHelper.isTutorialPending(TIP_INTRO)));

        UITestHelper.setOrientationLeft();
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.setOrientationNatural();
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));

        // The acknowledged tip must not reappear on the recreated activity
        UITestHelper.waitForDoesNotExist(withText(R.string.intro_message));
        assertEquals("No help dialog should be showing", 0, countHelpDialogs());
        UITestHelper.assertNoLeakedWindows();
    }
}
