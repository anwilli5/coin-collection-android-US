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

import static org.junit.Assert.assertTrue;

import android.os.Parcel;

import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.helper.ParcelableHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for Parcelable implementations. These are pure data tests that
 * need to run on a device (for android.os.Parcel) but do not require
 * an Activity. Extracted from AndroidUITests.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@SmallTest
public class ParcelableTests {

    /**
     * Test that CollectionListInfo parcelable implementation works
     */
    @Test
    public void test_ParcelableCollectionListInfo() {
        for (CollectionListInfo info : SharedTest.COLLECTION_LIST_INFO_SCENARIOS) {
            Parcel testParcel = Parcel.obtain();
            info.writeToParcel(testParcel, info.describeContents());
            testParcel.setDataPosition(0);
            CollectionListInfo checkInfo = CollectionListInfo.CREATOR.createFromParcel(testParcel);
            assertTrue(SharedTest.compareCollectionListInfos(info, checkInfo));
            testParcel.recycle();
        }
    }

    /**
     * Test that CoinSlot parcelable implementation works
     */
    @Test
    public void test_ParcelableCoinSlot() {
        for (CoinSlot coinSlot : SharedTest.COIN_SLOT_SCENARIOS) {
            Parcel testParcel = Parcel.obtain();
            coinSlot.writeToParcel(testParcel, coinSlot.describeContents());
            testParcel.setDataPosition(0);
            CoinSlot checkInfo = CoinSlot.CREATOR.createFromParcel(testParcel);
            assertTrue(SharedTest.compareCoinSlots(coinSlot, checkInfo, true, true));
            testParcel.recycle();
        }
    }

    /**
     * Test that ParcelableHashMap parcelable implementation works
     */
    @Test
    public void test_ParcelableParameters() {
        for (ParcelableHashMap parameters : SharedTest.PARAMETER_SCENARIOS) {
            Parcel testParcel = Parcel.obtain();
            parameters.writeToParcel(testParcel, parameters.describeContents());
            testParcel.setDataPosition(0);
            ParcelableHashMap checkInfo = ParcelableHashMap.CREATOR.createFromParcel(testParcel);
            assertTrue(SharedTest.compareParameters(parameters, checkInfo));
            testParcel.recycle();
        }
    }
}
