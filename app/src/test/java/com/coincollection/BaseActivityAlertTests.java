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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.spencerpages.BaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit tests covering the alert lifecycle in {@link BaseActivity}. Alerts shown
 * via showAlert() must be tracked so they can be dismissed before the activity
 * is torn down, otherwise the dialog window is leaked.
 */
@RunWith(RobolectricTestRunner.class)
public class BaseActivityAlertTests extends BaseTestCase {

    /**
     * Runs an action with alert suppression disabled, so a real dialog is
     * created instead of being skipped for unit tests
     *
     * @param action the action to run
     */
    private static void withAlertsEnabled(Runnable action) {
        boolean wasUnitTest = BaseActivity.isUnitTest;
        BaseActivity.isUnitTest = false;
        try {
            action.run();
        } finally {
            BaseActivity.isUnitTest = wasUnitTest;
        }
    }

    /**
     * Shows a tracked alert through the showAlert() path under test
     *
     * @param activity the host activity
     * @param message  the alert message
     */
    private static void showTestAlert(BaseActivity activity, String message) {
        activity.showAlert(activity.newBuilder().setMessage(message).setCancelable(true));
    }

    /**
     * Test that an alert shown by showAlert() is tracked on the activity
     */
    @Test
    public void test_alertIsTracked() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withAlertsEnabled(() -> {
                assertNull(activity.mCurrentAlert);
                showTestAlert(activity, "Test alert");
                assertNotNull(activity.mCurrentAlert);
                assertTrue(activity.mCurrentAlert.isShowing());
            }));
        }
    }

    /**
     * Test that dismissing the alert itself stops it from being tracked, so a
     * stale reference isn't held after the user closes it
     */
    @Test
    public void test_selfDismissClearsTracking() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withAlertsEnabled(() -> {
                showTestAlert(activity, "Test alert");
                assertNotNull(activity.mCurrentAlert);
                activity.mCurrentAlert.dismiss();
            }));

            // The dismiss callback is delivered through the main looper
            shadowOf(Looper.getMainLooper()).idle();

            scenario.onActivity(activity -> assertNull(activity.mCurrentAlert));
        }
    }

    /**
     * Test that showing a second alert dismisses the first one, so only the
     * newest alert is tracked
     */
    @Test
    public void test_secondAlertDismissesFirst() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withAlertsEnabled(() -> {
                showTestAlert(activity, "First alert");
                AlertDialog firstAlert = activity.mCurrentAlert;
                assertNotNull(firstAlert);

                showTestAlert(activity, "Second alert");
                assertNotNull(activity.mCurrentAlert);
                assertFalse(firstAlert.isShowing());
                assertTrue(activity.mCurrentAlert.isShowing());
            }));
        }
    }

    /**
     * Test that pausing the activity dismisses any open alert
     */
    @Test
    public void test_onPauseDismissesAlert() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            final AlertDialog[] shownAlert = new AlertDialog[1];
            scenario.onActivity(activity -> withAlertsEnabled(() -> {
                showTestAlert(activity, "Test alert");
                shownAlert[0] = activity.mCurrentAlert;
                assertNotNull(shownAlert[0]);
            }));

            scenario.moveToState(Lifecycle.State.CREATED);

            assertFalse(shownAlert[0].isShowing());
            scenario.onActivity(activity -> assertNull(activity.mCurrentAlert));
        }
    }

    /**
     * Test that destroying the activity dismisses any open alert, which is the
     * point at which the window would otherwise be leaked
     */
    @Test
    public void test_onDestroyDismissesAlert() {
        final AlertDialog[] shownAlert = new AlertDialog[1];
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> withAlertsEnabled(() -> {
                showTestAlert(activity, "Test alert");
                shownAlert[0] = activity.mCurrentAlert;
                assertNotNull(shownAlert[0]);
            }));

            scenario.moveToState(Lifecycle.State.DESTROYED);
        }
        assertFalse(shownAlert[0].isShowing());
    }
}
