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

package com.coincollection;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.coincollection.dialog.DialogRequests.KEY_PAYLOAD;
import static com.coincollection.dialog.DialogRequests.KEY_REQUEST_ID;
import static com.coincollection.dialog.DialogRequests.KEY_SELECTED_INDEX;
import static com.coincollection.dialog.DialogRequests.PAYLOAD_COIN_DATABASE_ID;
import static com.coincollection.dialog.DialogRequests.REQUEST_COIN_ACTIONS;
import static com.coincollection.dialog.DialogRequests.TAG_ALERT_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.spencerpages.MainApplication;
import com.spencerpages.R;
import com.spencerpages.UITestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Tests for dialogs that outlive the activity that showed them: the progress
 * spinner re-attaching to a recreated activity, a stale spinner being swept
 * away, alerts raised while the activity is stopped, and a dialog answered
 * before the page behind it has finished loading.
 * <p>
 * This class lives in {@code com.coincollection} rather than alongside the
 * other UI tests so it can hand the activity a task runner whose background
 * work it controls. Racing a real task is the only other way to catch the
 * progress dialog, and a task fast enough to be realistic is too fast to
 * observe reliably.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class DialogRestoreTests {

    private static final String COLLECTION_NAME = "Restore Test";
    private static final String SECOND_COLLECTION_NAME = "Second Restore Test";
    private static final String COPY_NAME = "Restore Test Copy";
    // LincolnCents is at index 0 in MainApplication.COLLECTION_TYPES
    private static final int COLLECTION_TYPE_INDEX = 0;
    private static final String FIRST_ALERT = "First alert text";
    private static final String SECOND_ALERT = "Second alert text";

    private final GatedExecutor mGatedExecutor = new GatedExecutor();

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
        UITestHelper.clearLogcat();
    }

    @After
    public void tearDown() {
        // Let any held task finish so its thread isn't left parked
        mGatedExecutor.release();
        mGatedExecutor.shutdown();
        UITestHelper.setOrientationNatural();
        UITestHelper.ensureDbOpen();
        UITestHelper.deleteAllCollections();
    }

    /**
     * An executor that holds every task it is given until the test releases
     * it, so a long-running task can be simulated without depending on how
     * long real work happens to take
     */
    private static class GatedExecutor implements Executor {

        private final CountDownLatch mGate = new CountDownLatch(1);
        private final ExecutorService mDelegate = Executors.newSingleThreadExecutor();

        @Override
        public void execute(Runnable command) {
            mDelegate.execute(() -> {
                try {
                    // Bounded so a failing test can't park the thread forever
                    mGate.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                command.run();
            });
        }

        void release() {
            mGate.countDown();
        }

        void shutdown() {
            mDelegate.shutdown();
        }
    }

    /**
     * Give an activity a task runner backed by the gated executor, so a task
     * started on it stays running until the test says otherwise. The runner
     * also goes into the ViewModel, which is where a recreated activity picks
     * its runner up from
     *
     * @param activity the activity to attach the runner to
     */
    private void attachGatedTaskRunner(BaseActivity activity) {
        AsyncTaskRunner runner = new AsyncTaskRunner(
                activity, mGatedExecutor, new Handler(Looper.getMainLooper()));
        activity.mTaskRunner = runner;
        activity.mActivityViewModel.mSavedTaskRunner = runner;
    }

    /**
     * @return how many cancelable alerts the activity is showing
     */
    private int countAlerts() {
        final int[] count = new int[1];
        activityRule.getScenario().onActivity(activity ->
                count[0] = UITestHelper.countDialogsWithTagPrefix(activity, TAG_ALERT_PREFIX));
        return count[0];
    }

    /**
     * Test that the progress dialog is still shown after a rotation while its
     * task is still running. The dialog belongs to the FragmentManager, and
     * the running task re-reports itself to the recreated activity
     */
    @Test
    public void test_progressDialogSurvivesRotationWhileTaskRuns() {
        activityRule.getScenario().onActivity(activity -> {
            attachGatedTaskRunner(activity);
            activity.kickOffAsyncTaskRunner(BaseActivity.TASK_OPEN_DATABASE);
        });
        UITestHelper.waitForDisplayed(withId(R.id.progress_message));

        UITestHelper.rotateAndAssertStillDisplayed(withId(R.id.progress_message));

        // Finishing the task takes the spinner down
        mGatedExecutor.release();
        UITestHelper.waitForDoesNotExist(withId(R.id.progress_message));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a delete started before a rotation still completes, and that the
     * recreated activity shows the list without the deleted collection. The
     * delete runs on the background thread with the progress dialog up
     */
    @Test
    public void test_deleteCompletesAcrossRotation() {
        activityRule.getScenario().onActivity(activity -> {
            attachGatedTaskRunner(activity);
            activity.startDeleteCollectionTask(COLLECTION_NAME);
        });
        UITestHelper.waitForDisplayed(withText(R.string.deleting_collection));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.deleting_collection));

        mGatedExecutor.release();
        UITestHelper.waitForDoesNotExist(withId(R.id.progress_message));
        UITestHelper.waitForDoesNotExist(withText(COLLECTION_NAME));
        activityRule.getScenario().onActivity(activity ->
                assertEquals(0, activity.mNumberOfCollections));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a copy started before a rotation still completes, and that the
     * copy shows up right after its source in the recreated activity's list
     */
    @Test
    public void test_copyCompletesAcrossRotation() {
        UITestHelper.createLincolnCentsCollection(SECOND_COLLECTION_NAME, 1);

        activityRule.getScenario().onActivity(activity -> {
            activity.updateCollectionListFromDatabaseAndUpdateViewForUIThread();
            attachGatedTaskRunner(activity);
            activity.startCopyCollectionTask(COLLECTION_NAME);
        });
        UITestHelper.waitForDisplayed(withText(R.string.copying_collection));

        UITestHelper.rotateAndAssertStillDisplayed(withText(R.string.copying_collection));

        mGatedExecutor.release();
        UITestHelper.waitForDoesNotExist(withId(R.id.progress_message));
        UITestHelper.waitForDisplayed(withText(COPY_NAME));
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(3, activity.mNumberOfCollections);
            assertEquals(COLLECTION_NAME, activity.mCollectionListEntries.get(0).getName());
            assertEquals(COPY_NAME, activity.mCollectionListEntries.get(1).getName());
            assertEquals(SECOND_COLLECTION_NAME, activity.mCollectionListEntries.get(2).getName());
        });
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a progress dialog restored for a task that is no longer
     * running is dismissed rather than left on screen. After process death the
     * FragmentManager restores the spinner, but the task it belonged to died
     * with the old process
     */
    @Test
    public void test_staleProgressDialogDismissedAfterRecreate() {
        // A spinner with no task behind it, as restoring one after process
        // death produces
        activityRule.getScenario().onActivity(activity ->
                activity.createProgressDialog("Stale spinner"));
        UITestHelper.waitForDisplayed(withId(R.id.progress_message));

        activityRule.getScenario().recreate();

        UITestHelper.waitForDoesNotExist(withId(R.id.progress_message));
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a second alert stacks on top of the first instead of replacing
     * it. Two messages can be raised back to back - an import error followed
     * by a warning, say - and the first must not be taken away before it has
     * been read
     */
    @Test
    public void test_alertsStackInsteadOfReplacingEachOther() {
        activityRule.getScenario().onActivity(activity -> {
            activity.showCancelableAlert(FIRST_ALERT);
            activity.showCancelableAlert(SECOND_ALERT);
        });

        UITestHelper.waitForDisplayed(withText(SECOND_ALERT));
        UITestHelper.waitForAssertion(() ->
                assertEquals("Both alerts should be showing", 2, countAlerts()));

        // Dismissing the top alert reveals the one underneath
        pressBack();
        UITestHelper.waitForDisplayed(withText(FIRST_ALERT));
        pressBack();
        UITestHelper.waitForDisplayed(withId(R.id.main_activity_listview));
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that an alert raised while the activity is stopped is held and
     * shown when it comes back. A task can finish while the app is in the
     * background, and no fragment transaction can be committed then - but the
     * message still has to reach the user
     */
    @Test
    public void test_alertRaisedWhileStoppedIsShownOnReturn() {
        activityRule.getScenario().moveToState(Lifecycle.State.CREATED);

        activityRule.getScenario().onActivity(activity -> {
            activity.showCancelableAlert(FIRST_ALERT);
            assertEquals("The alert should be held rather than dropped",
                    1, activity.mActivityViewModel.mPendingAlertText.size());
        });

        activityRule.getScenario().moveToState(Lifecycle.State.RESUMED);

        UITestHelper.waitForDisplayed(withText(FIRST_ALERT));
        activityRule.getScenario().onActivity(activity ->
                assertEquals("The held alert should have been handed over",
                        0, activity.mActivityViewModel.mPendingAlertText.size()));
        pressBack();
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Test that a coin dialog answered before the collection page has loaded
     * is dropped instead of crashing. After process death the FragmentManager
     * restores the dialog immediately, while the page behind it is still
     * waiting for the database to open - so it has no coin list to act on
     */
    @Test
    public void test_coinDialogResultDroppedBeforeDatabaseSetup() {
        Context context = getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, CollectionPage.class);
        intent.putExtra(CollectionPage.COLLECTION_NAME, COLLECTION_NAME);
        intent.putExtra(CollectionPage.COLLECTION_TYPE_INDEX, COLLECTION_TYPE_INDEX);

        DatabaseAdapter sharedDbAdapter =
                ((MainApplication) context.getApplicationContext()).getDbAdapter();

        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(intent)) {
            UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));

            // Hand the page a task runner whose work is held, then take the
            // database away and rebuild it: the recreated page picks that
            // runner up, so its database open never completes
            scenario.onActivity(this::attachGatedTaskRunner);
            sharedDbAdapter.close();
            scenario.recreate();

            scenario.onActivity(activity -> {
                assertNull("The page should still be waiting for its coin list",
                        activity.mCoinList);
                // Answering the restored dialog must be dropped, not crash
                activity.onDialogResult(REQUEST_COIN_ACTIONS, buildCoinActionResult());
            });

            // Letting the open finish brings the page up as usual
            mGatedExecutor.release();
            UITestHelper.waitForDisplayed(withId(R.id.standard_collection_page));
        }
        UITestHelper.assertNoLeakedWindows();
    }

    /**
     * Builds the result the coin actions dialog reports when its first entry
     * (toggle collected) is picked for a coin
     *
     * @return the result bundle
     */
    private static Bundle buildCoinActionResult() {
        Bundle payload = new Bundle();
        payload.putLong(PAYLOAD_COIN_DATABASE_ID, 1L);
        Bundle result = new Bundle();
        result.putInt(KEY_REQUEST_ID, REQUEST_COIN_ACTIONS);
        result.putInt(KEY_SELECTED_INDEX, 0);
        result.putBundle(KEY_PAYLOAD, payload);
        return result;
    }
}
