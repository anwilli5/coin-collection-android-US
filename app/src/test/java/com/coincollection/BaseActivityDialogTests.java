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

import static com.coincollection.dialog.DialogRequests.TAG_ALERT_PREFIX;
import static com.coincollection.dialog.DialogRequests.TAG_HELP;
import static com.coincollection.dialog.DialogRequests.TAG_PROGRESS;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.dialog.ProgressDialogFragment;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.BaseTestCase;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Unit tests covering the dialog lifecycle in {@link BaseActivity}. Dialogs are
 * owned by the FragmentManager so that they survive the activity being
 * recreated instead of leaking the window they were attached to.
 */
@RunWith(RobolectricTestRunner.class)
public class BaseActivityDialogTests extends BaseTestCase {

    // Argument key the dialogs carry their message under, mirroring
    // BaseDialogFragment.ARG_MESSAGE, which isn't visible from this package
    private static final String ARG_MESSAGE = "message";

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
     * Collects the cancelable alerts an activity is showing, in the order they
     * were raised. Each alert gets a tag of its own so that alerts stack instead
     * of replacing one another
     *
     * @param activity the host activity
     * @return the alert dialog fragments currently shown
     */
    private static List<Fragment> findAlertDialogs(BaseActivity activity) {
        activity.getSupportFragmentManager().executePendingTransactions();
        List<Fragment> alerts = new ArrayList<>();
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            String tag = fragment.getTag();
            if (tag != null && tag.startsWith(TAG_ALERT_PREFIX)) {
                alerts.add(fragment);
            }
        }
        return alerts;
    }

    /**
     * @param fragment a message dialog fragment
     * @return the message the dialog was created with
     */
    private static String getDialogMessage(Fragment fragment) {
        Bundle args = fragment.getArguments();
        return (args != null) ? args.getString(ARG_MESSAGE) : null;
    }

    /**
     * Sets whether a one-time help tip is still due to be shown
     *
     * @param helpStrKey the tip's preference key
     * @param show       true if the tip hasn't been acknowledged yet
     */
    private static void setHelpTipPending(String helpStrKey, boolean show) {
        ApplicationProvider.<Context>getApplicationContext()
                .getSharedPreferences(MainApplication.PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(helpStrKey, show).apply();
    }

    /**
     * Creates the first shared collection scenario (Lincoln Cents) in the
     * database, so MainActivity has a collection to list and reorder
     */
    private void createCollection() {
        CollectionListInfo info = COLLECTION_LIST_INFO_SCENARIOS[0];
        try (ActivityScenario<CoinPageCreator> creatorScenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class))) {
            creatorScenario.onActivity(activity -> {
                activity.mCoinList = new ArrayList<>();
                ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                int index = info.getCollectionTypeIndex();
                activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                activity.createOrUpdateCoinListForAsyncThread();
                activity.mDbAdapter.createAndPopulateNewTable(info, 0, activity.mCoinList);
            });
        }
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
                assertTrue(findAlertDialogs(activity).isEmpty());
                activity.showCancelableAlert("Test alert");
                assertEquals(1, findAlertDialogs(activity).size());
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

            scenario.onActivity(activity -> assertEquals(1, findAlertDialogs(activity).size()));
        }
    }

    /**
     * Test that a second alert stacks on top of the first instead of replacing
     * it. Two messages can be raised back to back (e.g. an import error followed
     * by a warning) and the first must not be hidden by the second
     */
    @Test
    public void test_secondAlertStacksOnFirst() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withDialogsEnabled(() -> {
                activity.showCancelableAlert("First alert");
                activity.showCancelableAlert("Second alert");

                List<Fragment> alerts = findAlertDialogs(activity);
                assertEquals(2, alerts.size());
                assertEquals("First alert", getDialogMessage(alerts.get(0)));
                assertEquals("Second alert", getDialogMessage(alerts.get(1)));
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
     * Test that alerts raised while the activity is stopped are held and shown
     * in order when it comes back, rather than being dropped or overwriting each
     * other. A task can finish while the app is in the background, and its error
     * messages must still reach the user
     */
    @Test
    public void test_alertsRaisedWhileStoppedAreShownOnResume() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.moveToState(Lifecycle.State.CREATED);

            scenario.onActivity(activity -> withDialogsEnabled(() -> {
                activity.showCancelableAlert("Task failed");
                activity.showCancelableAlert("Second failure");
                // Nothing can be shown while stopped, so both are held instead
                assertTrue(findAlertDialogs(activity).isEmpty());
                assertEquals(Arrays.asList("Task failed", "Second failure"),
                        activity.mActivityViewModel.mPendingAlertText);
            }));

            withDialogsEnabled(() -> {
                scenario.moveToState(Lifecycle.State.RESUMED);
                shadowOf(Looper.getMainLooper()).idle();
            });

            scenario.onActivity(activity -> {
                List<Fragment> alerts = findAlertDialogs(activity);
                assertEquals(2, alerts.size());
                assertEquals("Task failed", getDialogMessage(alerts.get(0)));
                assertEquals("Second failure", getDialogMessage(alerts.get(1)));
                assertTrue(activity.mActivityViewModel.mPendingAlertText.isEmpty());
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

            scenario.onActivity(activity -> assertEquals(1, findAlertDialogs(activity).size()));
        }
    }

    /**
     * Test that a help tip shown from a fragment's onCreateView is displayed.
     * The reorder fragment shows its tip from onCreateView, which runs inside a
     * FragmentManager transaction - settling pending transactions from there
     * used to throw IllegalStateException and crash the app for any user who
     * hadn't acknowledged the tip yet
     */
    @Test
    public void test_helpDialogShownFromFragmentOnCreateView() {
        createCollection();
        // Only the reorder tip is outstanding, so it is the only dialog in play
        setHelpTipPending("reorder_help1", true);
        setHelpTipPending("first_Time_screen1", false);
        setHelpTipPending("first_Time_screen4", false);

        withDialogsEnabled(() -> {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                    new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
                scenario.onActivity(activity -> {
                    activity.updateCollectionListFromDatabase();
                    assertNotNull(activity.launchReorderFragment());
                    // Runs the fragment transaction, so the fragment's
                    // onCreateView shows the tip from inside it
                    activity.getSupportFragmentManager().executePendingTransactions();
                });
                shadowOf(Looper.getMainLooper()).idle();

                scenario.onActivity(activity ->
                        assertNotNull(findDialog(activity, TAG_HELP)));
            }
        });
    }

    /**
     * Test that a later help tip is still shown once the user has acknowledged
     * the previous one. The FragmentManager resets a destroyed dialog fragment
     * so the instance can be reused, which puts it back in the state a brand new
     * fragment is in - so a liveness check based on the fragment's lifecycle
     * keeps reporting the acknowledged tip as showing and silently swallows
     * every tip raised afterwards
     */
    @Test
    public void test_helpTipShownAfterPreviousAcknowledged() {
        setHelpTipPending("first_Time_screen1", true);
        setHelpTipPending("first_Time_screen4", true);

        withDialogsEnabled(() -> {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                    new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
                scenario.onActivity(activity -> {
                    Fragment firstTip = findDialog(activity, TAG_HELP);
                    assertNotNull("The intro tip should be shown", firstTip);
                    // Acknowledging the tip takes it down, as tapping OK does
                    ((DialogFragment) firstTip).dismiss();
                    activity.getSupportFragmentManager().executePendingTransactions();
                });
                shadowOf(Looper.getMainLooper()).idle();

                scenario.onActivity(activity -> {
                    assertNull("The acknowledged tip should be gone",
                            findDialog(activity, TAG_HELP));
                    activity.createAndShowHelpDialog(
                            "first_Time_screen4", R.string.tutorial_more_options);
                    activity.getSupportFragmentManager().executePendingTransactions();
                    assertNotNull("A tip raised after the first was acknowledged must be shown",
                            findDialog(activity, TAG_HELP));
                });
            }
        });
    }

    /**
     * Test that a second help tip doesn't displace one the user hasn't
     * acknowledged yet. The first tip's preference is only cleared once it is
     * acknowledged, so replacing it would make it pop up again later
     */
    @Test
    public void test_secondHelpTipDoesNotReplaceFirst() {
        setHelpTipPending("first_Time_screen1", true);
        setHelpTipPending("first_Time_screen4", true);

        withDialogsEnabled(() -> {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                    new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
                scenario.onActivity(activity -> {
                    Fragment firstTip = findDialog(activity, TAG_HELP);
                    assertNotNull("The intro tip should be shown", firstTip);

                    assertTrue(activity.createAndShowHelpDialog(
                            "first_Time_screen4", R.string.tutorial_more_options));
                    assertSame(firstTip, findDialog(activity, TAG_HELP));
                });
            }
        });
    }
}
