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
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.CollectionPage;
import com.coincollection.DatabaseAdapter;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsAnything;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Shared utility methods for UI instrumented tests.
 * Provides database setup/teardown helpers and tutorial suppression
 * so that each test can run independently with its own state.
 */
public class UITestHelper {

    private static final long DEFAULT_TIMEOUT_MS = 30_000;
    private static final long POLL_INTERVAL_MS = 250;

    public static String getString(@StringRes int resId) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getString(resId);
    }

    public static String getStringArrayItem(@ArrayRes int arrayResId, int index) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getResources().getStringArray(arrayResId)[index];
    }

    // SharedPreferences keys for tutorial dialogs
    private static final String PREFS_NAME = "mainPreferences";
    private static final String[] TUTORIAL_KEYS = {
            "first_Time_screen1",
            "first_Time_screen2",
            "first_Time_screen3",
            "first_Time_screen4",
            "first_Time_screen5",
            "reorder_help1",
    };

    /**
     * Suppress all first-time tutorial dialogs by setting their
     * SharedPreferences flags to false (already seen).
     * Uses commit() for synchronous write to ensure prefs are set
     * before any activity reads them.
     */
    public static void suppressAllTutorials() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : TUTORIAL_KEYS) {
            editor.putBoolean(key, false);
        }
        editor.commit();
    }

    /**
     * Reset all tutorial flags to true so they will appear on next launch.
     * Uses commit() for synchronous write.
     */
    public static void resetTutorials() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : TUTORIAL_KEYS) {
            editor.putBoolean(key, true);
        }
        editor.commit();
    }

    /**
     * Unlock a collection if it was locked (clear the lock preference).
     */
    public static void unlockCollection(String collectionName) {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(collectionName + CollectionPage.IS_LOCKED, false).apply();
    }

    /**
     * Delete all collections from the database.
     */
    public static void deleteAllCollections() {
        Context context = getInstrumentation().getTargetContext();
        DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
        dbAdapter.open();
        // Collect all names FIRST, then close the cursor before modifying the table.
        // Dropping tables while iterating a cursor over the same info table can
        // cause the cursor to skip rows when its CursorWindow is re-filled.
        ArrayList<String> names = new ArrayList<>();
        Cursor cursor = dbAdapter.getAllCollectionNames();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    names.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        for (String name : names) {
            dbAdapter.dropCollectionTable(name);
        }
        dbAdapter.close();
    }

    /**
     * Create a collection using default parameters for a given coin type.
     *
     * @param name           The collection name
     * @param typeIndex      Index into MainApplication.COLLECTION_TYPES
     * @param displayOrder   Display order within the list
     * @param displayType    CollectionPage.SIMPLE_DISPLAY or ADVANCED_DISPLAY
     * @return The number of coins created
     */
    public static int createCollection(String name, int typeIndex, int displayOrder, int displayType) {
        Context context = getInstrumentation().getTargetContext();
        CollectionInfo collectionInfo = MainApplication.COLLECTION_TYPES[typeIndex];

        // Generate default parameters and coin list
        ParcelableHashMap parameters = new ParcelableHashMap();
        collectionInfo.getCreationParameters(parameters);
        ArrayList<CoinSlot> coinList = new ArrayList<>();
        collectionInfo.populateCollectionLists(parameters, coinList);

        // Build CollectionListInfo
        CollectionListInfo info = new CollectionListInfo(
                name,
                coinList.size(),
                0,
                typeIndex,
                displayType,
                0, 0, "", ""
        );

        // Insert into database
        DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
        dbAdapter.open();
        dbAdapter.createAndPopulateNewTable(info, displayOrder, coinList);
        dbAdapter.close();

        return coinList.size();
    }

    /**
     * Create a Lincoln Cents collection with SIMPLE_DISPLAY.
     *
     * @param name         Collection name
     * @param displayOrder Display order
     * @return Number of coins created (should be 121)
     */
    public static int createLincolnCentsCollection(String name, int displayOrder) {
        // LincolnCents is at index 0 in MainApplication.COLLECTION_TYPES
        return createCollection(name, 0, displayOrder, CollectionPage.SIMPLE_DISPLAY);
    }

    /**
     * Create a Presidential Dollars collection with SIMPLE_DISPLAY.
     *
     * @param name         Collection name
     * @param displayOrder Display order
     * @return Number of coins created
     */
    public static int createPresidentialDollarsCollection(String name, int displayOrder) {
        // PresidentialDollars is at index 10 in MainApplication.COLLECTION_TYPES
        return createCollection(name, 10, displayOrder, CollectionPage.SIMPLE_DISPLAY);
    }

    /**
     * Create a Lincoln Cents collection with ADVANCED_DISPLAY.
     *
     * @param name         Collection name
     * @param displayOrder Display order
     * @return Number of coins created
     */
    public static int createLincolnCentsAdvancedCollection(String name, int displayOrder) {
        return createCollection(name, 0, displayOrder, CollectionPage.ADVANCED_DISPLAY);
    }

    /**
     * Set the display type of a collection.
     *
     * @param name        Collection name
     * @param displayType CollectionPage.SIMPLE_DISPLAY or ADVANCED_DISPLAY
     */
    public static void setCollectionDisplayType(String name, int displayType) {
        Context context = getInstrumentation().getTargetContext();
        DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
        dbAdapter.open();
        dbAdapter.updateTableDisplay(name, displayType);
        dbAdapter.close();
    }

    /**
     * Ensure the MainApplication's DB adapter is open.
     * Prevents the async TASK_OPEN_DATABASE from showing a progress dialog
     * that steals window focus and causes RootViewWithoutFocusException.
     */
    public static void ensureDbOpen() {
        Context context = getInstrumentation().getTargetContext();
        MainApplication app = (MainApplication) context.getApplicationContext();
        DatabaseAdapter dbAdapter = app.getDbAdapter();
        if (!dbAdapter.isOpen()) {
            dbAdapter.open();
        }
    }

    /**
     * Dismiss any tutorial dialog that may be showing by clicking the "Okay!" button.
     * Tries up to 3 times to handle multiple sequential dialogs.
     * Safe to call when no dialog is showing - the exception is caught and ignored.
     */
    public static void dismissTutorialDialogs() {
        for (int i = 0; i < 3; i++) {
            if (!tryClickIfDisplayed(withText(R.string.okay_exp), 2_000)) {
                return;
            }
        }
    }

    /**
     * Recreate the activity and force a synchronous DB reload.
     * After recreate(), the adapter is empty until onWindowFocusChanged(true) fires.
     * This method forces an immediate reload so tests can interact with the list.
     * Also dismisses any tutorial dialogs that appear from the initial launch or recreate.
     * Waits until the main activity ListView is fully visible before returning.
     */
    public static void recreateActivity(ActivityScenarioRule<MainActivity> activityRule) {
        disableSystemAnimations();
        ensureDbOpen();
        // Dismiss any tutorial dialog from the initial ActivityScenarioRule launch
        dismissTutorialDialogs();
        // Suppress tutorials so they won't appear on recreate
        suppressAllTutorials();
        activityRule.getScenario().recreate();
        // Dismiss any tutorial dialog that might have appeared despite suppression
        dismissTutorialDialogs();
        activityRule.getScenario().onActivity(
                MainActivity::updateCollectionListFromDatabaseAndUpdateViewForUIThread);
        // Wait until the main activity is fully loaded and visible
        waitForMainActivity();
    }

    /**
     * Disables system animations on emulator/device to prevent Espresso click failures.
     * Best-effort: failures are ignored.
     */
    public static void disableSystemAnimations() {
        try {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            device.executeShellCommand("settings put global window_animation_scale 0");
            device.executeShellCommand("settings put global transition_animation_scale 0");
            device.executeShellCommand("settings put global animator_duration_scale 0");
        } catch (Exception ignored) {
        }
    }

    /**
     * Re-enables system animations on emulator/device. Best-effort.
     */
    public static void enableSystemAnimations() {
        try {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            device.executeShellCommand("settings put global window_animation_scale 1");
            device.executeShellCommand("settings put global transition_animation_scale 1");
            device.executeShellCommand("settings put global animator_duration_scale 1");
        } catch (Exception ignored) {
        }
    }

    /**
     * Wait for the main activity ListView to become visible.
     * Checks immediately, then polls every 500ms for up to 30 seconds.
     * Also dismisses any tutorial dialogs that may appear.
     */
    public static void waitForMainActivity() {
        // If a tutorial dialog steals focus, dismiss and retry.
        long deadline = System.currentTimeMillis() + DEFAULT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (tryIsDisplayed(withId(R.id.main_activity_listview))) {
                return;
            }
            tryClickIfDisplayed(withText(R.string.okay_exp), 500);
            loopMainThreadFor(POLL_INTERVAL_MS);
        }
        // Final attempt — throw a useful Espresso assertion if still not visible.
        onView(withId(R.id.main_activity_listview)).check(
                ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Wait until a view matching matcher is displayed.
     */
    public static void waitForDisplayed(Matcher<View> matcher) {
        waitForDisplayed(matcher, DEFAULT_TIMEOUT_MS);
    }

    public static void waitForDisplayed(Matcher<View> matcher, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (tryIsDisplayed(matcher)) {
                return;
            }
            loopMainThreadFor(POLL_INTERVAL_MS);
        }
        onView(matcher).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Wait until no view matching matcher is displayed (either gone or not in view hierarchy).
     */
    public static void waitForNotDisplayed(Matcher<View> matcher) {
        waitForNotDisplayed(matcher, DEFAULT_TIMEOUT_MS);
    }

    public static void waitForNotDisplayed(Matcher<View> matcher, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (!tryIsDisplayed(matcher)) {
                return;
            }
            loopMainThreadFor(POLL_INTERVAL_MS);
        }
        // If it's still displayed, assert to fail.
        onView(matcher).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Wait until a view matching matcher does not exist in the view hierarchy.
     */
    public static void waitForDoesNotExist(Matcher<View> matcher) {
        waitForDoesNotExist(matcher, DEFAULT_TIMEOUT_MS);
    }

    public static void waitForDoesNotExist(Matcher<View> matcher, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (tryDoesNotExist(matcher)) {
                return;
            }
            loopMainThreadFor(POLL_INTERVAL_MS);
        }
        // If it still exists, throw with Espresso.
        onView(matcher).check(ViewAssertions.doesNotExist());
    }

    /**
     * Wait until the provided assertion passes without throwing.
     */
    public static void waitForAssertion(Runnable assertion) {
        waitForAssertion(assertion, DEFAULT_TIMEOUT_MS);
    }

    public static void waitForAssertion(Runnable assertion, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                assertion.run();
                return;
            } catch (Throwable t) {
                lastError = t;
                loopMainThreadFor(POLL_INTERVAL_MS);
            }
        }
        if (lastError instanceof RuntimeException) {
            throw (RuntimeException) lastError;
        }
        if (lastError instanceof Error) {
            throw (Error) lastError;
        }
        throw new AssertionError("Timed out waiting for assertion", lastError);
    }

    /**
     * Attempt to click matcher if it becomes displayed within timeout.
     */
    public static boolean tryClickIfDisplayed(Matcher<View> matcher, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (tryIsDisplayed(matcher)) {
                try {
                    onView(matcher).perform(click());
                    return true;
                } catch (Exception ignored) {
                    // Fall through and retry.
                }
            }
            loopMainThreadFor(POLL_INTERVAL_MS);
        }
        return false;
    }

    private static boolean tryIsDisplayed(Matcher<View> matcher) {
        try {
            onView(matcher).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean tryDoesNotExist(Matcher<View> matcher) {
        try {
            onView(matcher).check(ViewAssertions.doesNotExist());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void loopMainThreadFor(long durationMs) {
        onView(isRoot()).perform(loopMainThreadForAtLeast(durationMs));
    }

    private static ViewAction loopMainThreadForAtLeast(final long durationMs) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new IsAnything<>();
            }

            @Override
            public String getDescription() {
                return "Loop the main thread for at least " + durationMs + "ms";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(durationMs);
            }
        };
    }

    /**
     * Build the expected progress string using the app's template resource.
     * This intentionally matches the current UI (including any trailing characters in the template).
     */
    public static String formatCollectionProgress(int collected, int total) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getString(R.string.collection_completion_template, collected, total);
    }

    /**
     * Matcher for CollectionListInfo items in the main activity ListView.
     * Matches by collection name.
     */
    public static Matcher<Object> withCollectionName(final String name) {
        return new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object item) {
                if (item instanceof CollectionListInfo) {
                    return ((CollectionListInfo) item).getName().equals(name);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("CollectionListInfo with name '" + name + "'");
            }
        };
    }

    /**
     * Scroll the main activity ListView to the bottom to ensure nav items are visible.
     */
    public static void scrollToNavItems() {
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
    }

    /**
     * ViewAction that clicks a child view with the given ID inside a parent view.
     * Useful for clicking buttons inside RecyclerView items.
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }

    /**
     * Matcher for the nth child of a parent view.
     * Useful for matching a specific child within an AdapterView or RecyclerView.
     *
     * @param parentMatcher Matcher for the parent view
     * @param childPosition Position of the child to match (0-based)
     */
    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("position " + childPosition + " of parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return false;
                }
                ViewGroup parent = (ViewGroup) view.getParent();
                if (!parentMatcher.matches(parent)) {
                    return false;
                }
                return parent.getChildAt(childPosition) == view;
            }
        };
    }
}
