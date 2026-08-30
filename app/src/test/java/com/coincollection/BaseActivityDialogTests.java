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

import static com.coincollection.dialog.DialogRequests.TAG_MESSAGE;
import static com.coincollection.dialog.DialogRequests.TAG_PROGRESS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.dialog.ProgressDialogFragment;
import com.spencerpages.BaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Unit tests covering the dialog lifecycle in {@link BaseActivity}. Dialogs are
 * owned by the FragmentManager so that they survive the activity being
 * recreated instead of leaking the window they were attached to.
 */
@RunWith(RobolectricTestRunner.class)
public class BaseActivityDialogTests extends BaseTestCase {

    /**
     * Runs an action with dialog suppression disabled, so a real dialog is
     * created instead of being skipped for unit tests
     *
     * @param action the action to run
     */
    private static void withDialogsEnabled(Runnable action) {
        boolean wasUnitTest = BaseActivity.isUnitTest;
        BaseActivity.isUnitTest = false;
        try {
            action.run();
        } finally {
            BaseActivity.isUnitTest = wasUnitTest;
        }
    }

    /**
     * Finds a dialog by tag in an activity's FragmentManager
     *
     * @param activity the host activity
     * @param tag      tag identifying the dialog
     * @return the dialog fragment, or null if it isn't shown
     */
    private static Fragment findDialog(BaseActivity activity, String tag) {
        activity.getSupportFragmentManager().executePendingTransactions();
        return activity.getSupportFragmentManager().findFragmentByTag(tag);
    }

    /**
     * Test that an alert is shown as a dialog fragment, so the FragmentManager
     * owns its lifecycle
     */
    @Test
    public void test_alertIsShownAsFragment() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withDialogsEnabled(() -> {
                assertNull(findDialog(activity, TAG_MESSAGE));
                activity.showCancelableAlert("Test alert");
                assertNotNull(findDialog(activity, TAG_MESSAGE));
            }));
        }
    }

    /**
     * Test that an alert open across a configuration change is restored rather
     * than leaked along with the destroyed window
     */
    @Test
    public void test_alertSurvivesRecreate() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity ->
                    withDialogsEnabled(() -> activity.showCancelableAlert("Test alert")));

            scenario.recreate();
            shadowOf(Looper.getMainLooper()).idle();

            scenario.onActivity(activity -> assertNotNull(findDialog(activity, TAG_MESSAGE)));
        }
    }

    /**
     * Test that showing a second alert replaces the first rather than stacking
     * two dialogs on top of each other
     */
    @Test
    public void test_secondAlertReplacesFirst() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withDialogsEnabled(() -> {
                activity.showCancelableAlert("First alert");
                Fragment firstAlert = findDialog(activity, TAG_MESSAGE);
                assertNotNull(firstAlert);

                activity.showCancelableAlert("Second alert");
                Fragment secondAlert = findDialog(activity, TAG_MESSAGE);
                assertNotNull(secondAlert);
                assertTrue(firstAlert.isRemoving() || firstAlert != secondAlert);
            }));
        }
    }

    /**
     * Test that the progress dialog is shown as a fragment and reuses the
     * existing instance when the same task reports its progress again, which is
     * what happens when a running task re-attaches to a recreated activity
     */
    @Test
    public void test_progressDialogIsReused() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withDialogsEnabled(() -> {
                activity.createProgressDialog("Working");
                Fragment progress = findDialog(activity, TAG_PROGRESS);
                assertNotNull(progress);
                assertTrue(progress instanceof ProgressDialogFragment);

                activity.createProgressDialog("Still working");
                assertTrue(findDialog(activity, TAG_PROGRESS) == progress);

                activity.dismissProgressDialog();
                assertNull(findDialog(activity, TAG_PROGRESS));
            }));
        }
    }

    /**
     * Test that the progress dialog for a still-running task is restored when
     * the activity is recreated, and goes away once the task finishes
     */
    @Test
    public void test_progressDialogSurvivesRecreateWhileTaskRuns() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            // Hold the background work so the task stays in flight for the
            // whole test instead of racing the assertions
            PendingExecutor executor = new PendingExecutor();
            AsyncTaskRunner[] runner = new AsyncTaskRunner[1];

            withDialogsEnabled(() -> {
                scenario.onActivity(activity -> {
                    runner[0] = new AsyncTaskRunner(activity, executor, new Handler(Looper.getMainLooper()));
                    // The runner lives in the ViewModel, so it is the same
                    // instance the recreated activity attaches to
                    activity.mActivityViewModel.mSavedTaskRunner = runner[0];
                    activity.mTaskRunner = runner[0];
                    runner[0].execute(BaseActivity.TASK_EXPORT_COLLECTIONS);
                });
                shadowOf(Looper.getMainLooper()).idle();
            });
            scenario.onActivity(activity -> assertNotNull(findDialog(activity, TAG_PROGRESS)));

            // The task is still running when the activity is recreated
            withDialogsEnabled(() -> {
                scenario.recreate();
                shadowOf(Looper.getMainLooper()).idle();
            });
            scenario.onActivity(activity -> assertNotNull(findDialog(activity, TAG_PROGRESS)));

            // Letting the task finish takes the progress dialog down
            withDialogsEnabled(() -> {
                executor.runPending();
                shadowOf(Looper.getMainLooper()).idle();
            });
            scenario.onActivity(activity -> assertNull(findDialog(activity, TAG_PROGRESS)));
        }
    }

    /**
     * Test that a progress dialog restored for a task that is no longer
     * running is dropped, rather than leaving the user stuck behind a spinner
     */
    @Test
    public void test_staleProgressDialogIsDismissedOnRecreate() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity ->
                    withDialogsEnabled(() -> activity.createProgressDialog("Working")));

            withDialogsEnabled(() -> {
                scenario.recreate();
                shadowOf(Looper.getMainLooper()).idle();
            });

            scenario.onActivity(activity -> assertNull(findDialog(activity, TAG_PROGRESS)));
        }
    }

    /**
     * Test that an alert raised while the activity is stopped is held and shown
     * when it comes back, rather than being dropped. A task can finish while the
     * app is in the background, and its error message must still reach the user
     */
    @Test
    public void test_alertRaisedWhileStoppedIsShownOnResume() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.moveToState(Lifecycle.State.CREATED);

            scenario.onActivity(activity -> withDialogsEnabled(() -> {
                activity.showCancelableAlert("Task failed");
                // Nothing can be shown while stopped, so it is held instead
                assertNull(findDialog(activity, TAG_MESSAGE));
                assertNotNull(activity.mActivityViewModel.mPendingAlertText);
            }));

            withDialogsEnabled(() -> {
                scenario.moveToState(Lifecycle.State.RESUMED);
                shadowOf(Looper.getMainLooper()).idle();
            });

            scenario.onActivity(activity -> {
                assertNotNull(findDialog(activity, TAG_MESSAGE));
                assertNull(activity.mActivityViewModel.mPendingAlertText);
            });
        }
    }

    /**
     * Executor that holds onto submitted work until the test releases it, so a
     * task can be kept in flight deterministically
     */
    private static class PendingExecutor implements Executor {
        private final ArrayList<Runnable> mPending = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            mPending.add(command);
        }

        /**
         * Runs everything submitted so far
         */
        void runPending() {
            ArrayList<Runnable> toRun = new ArrayList<>(mPending);
            mPending.clear();
            for (Runnable runnable : toRun) {
                runnable.run();
            }
        }
    }

    /**
     * Test that pausing the activity leaves its dialogs alone. Dialogs used to
     * be dismissed on pause to avoid leaking them, which silently discarded
     * whatever the user was being asked to decide
     */
    @Test
    public void test_pauseDoesNotDismissDialogs() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity ->
                    withDialogsEnabled(() -> activity.showCancelableAlert("Test alert")));

            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.RESUMED);

            scenario.onActivity(activity -> assertNotNull(findDialog(activity, TAG_MESSAGE)));
        }
    }
}
