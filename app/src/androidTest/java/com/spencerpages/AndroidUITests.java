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

import android.os.Build;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.uiautomator.UiDevice;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class AndroidUITests extends TestCase {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Scrolls down a few times (hopefully enough to reach the end)
     */
    public static void scrollAllTheWayDown() {
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
        onView(withId(R.id.main_activity_listview)).perform(swipeUp());
    }

    /**
     * Set orientation left
     */
    public static void setOrientationLeft() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            device.setOrientationLeft();
        } catch (RemoteException e) {
            fail();
        }
    }

    /**
     * Set orientation natural
     */
    public static void setOrientationNatural() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            device.setOrientationNatural();
        } catch (RemoteException e) {
            fail();
        }
    }
    /**
     * Test launching the main activity
     */
    @Test
    public void basicMainActivity() {
        onView(withId(R.id.main_activity_listview)).check(matches(isDisplayed()));
    }

    /**
     * Test launching the creation page activity
     */
    @Test
    public void launchCreationPage() {
        scrollAllTheWayDown();
        onView(withText(R.string.create_new_collection)).perform(click());
        onView(withId(R.id.edit_enter_collection_name)).check(matches(isDisplayed()));
    }

    /**
     * Test launching the delete collection view
     */
    @Test
    public void launchDeleteCollectionView() {
        scrollAllTheWayDown();
        onView(withText(R.string.delete_collection)).perform(click());
        onView(withText(R.string.select_collection_delete)).check(matches(isDisplayed()));
    }

    /**
     * Test launching the app info collection view
     */
    @Test
    public void launchAppInfoView() {
        scrollAllTheWayDown();
        onView(withText(R.string.app_info)).perform(click());
        onView(withId(R.id.info_textview)).check(matches(isDisplayed()));
    }

    /**
     * Test exporting existing collections (assumes some collections already exist)
     */
    @Test
    public void exportCollections() {
        scrollAllTheWayDown();
        onView(withText(R.string.export_collection)).perform(click());
        onView(withText(R.string.yes)).perform(click());
    }

    /**
     * Test that CollectionListInfo parcelable implementation works
     */
    @Test
    public void test_ParcelableCollectionListInfo() {
        for (CollectionListInfo info : SharedTest.COLLECTION_LIST_INFO_SCENARIOS){
            Parcel testParcel = Parcel.obtain();
            info.writeToParcel(testParcel, info.describeContents());
            testParcel.setDataPosition(0);
            CollectionListInfo checkInfo = CollectionListInfo.CREATOR.createFromParcel(testParcel);
            assertTrue(SharedTest.compareCollectionListInfos(info, checkInfo));
        }
    }

    /**
     * Test that CoinSlot parcelable implementation works
     */
    @Test
    public void test_ParcelableCoinSlot() {
        for (CoinSlot coinSlot : SharedTest.COIN_SLOT_SCENARIOS){
            Parcel testParcel = Parcel.obtain();
            coinSlot.writeToParcel(testParcel, coinSlot.describeContents());
            testParcel.setDataPosition(0);
            CoinSlot checkInfo = CoinSlot.CREATOR.createFromParcel(testParcel);
            assertTrue(SharedTest.compareCoinSlots(coinSlot, checkInfo));
        }
    }

    /**
     * Test that ParcelableHashMap parcelable implementation works
     */
    @Test
    public void test_ParcelableParameters() {
        for (ParcelableHashMap parameters : SharedTest.PARAMETER_SCENARIOS){
            Parcel testParcel = Parcel.obtain();
            parameters.writeToParcel(testParcel, parameters.describeContents());
            testParcel.setDataPosition(0);
            ParcelableHashMap checkInfo = ParcelableHashMap.CREATOR.createFromParcel(testParcel);
            assertTrue(SharedTest.compareParameters(parameters, checkInfo));
        }
    }

    /**
     * Test export and rotate
     */
    @Test
    public void test_exportCollectionsAndRotate() {

        scrollAllTheWayDown();
        onView(withText(R.string.export_collection)).perform(click());
        onView(withText(R.string.yes)).perform(click());
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            setOrientationLeft();
            scrollAllTheWayDown();
            setOrientationNatural();
        }
    }
}