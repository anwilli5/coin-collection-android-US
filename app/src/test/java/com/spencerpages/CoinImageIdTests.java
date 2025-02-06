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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

@RunWith(ParameterizedRobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class CoinImageIdTests extends BaseTestCase {

    private final CollectionInfo mCoinTypeObj;

    public CoinImageIdTests(CollectionInfo coinTypeObj) {
        mCoinTypeObj = coinTypeObj;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static List<?> getCoinTypeObj() {
        return Arrays.asList(MainApplication.COLLECTION_TYPES);
    }

    /**
     * Test that calling getCoinSlotImage() on collections that use image IDs returns
     * an index within the bounds of the IMAGE ID list.
     */
    @Test
    public void test_getCoinSlotImage() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                for (FullCollection scenario1 : getRandomTestScenarios(mCoinTypeObj, 1)) {
                    // Add a coin into the collection used for this test
                    scenario1.mCoinList.add(0, new CoinSlot("Name", "Mint", 0, -1));

                    // Cover the range of possible image ids for this collection
                    CollectionInfo collectionInfo = scenario1.mCollectionListInfo.getCollectionObj();
                    Object[][] imageIds = collectionInfo.getImageIds();
                    for(int i = 0; i < imageIds.length; i++) {
                        // Test that getCoinSlotImage(false) gives the correct resource IDs
                        CoinSlot coinSlot = scenario1.mCoinList.get(0);
                        coinSlot.setImageId(i);
                        int resId = collectionInfo.getCoinSlotImage(coinSlot, false);
                        assertEquals((int)imageIds[i][1], resId);

                        // Test that getCoinSlotImage(true) correctly ignores the image ID
                        coinSlot.setImageId(-1);
                        int chkResId = collectionInfo.getCoinSlotImage(coinSlot, false);
                        coinSlot.setImageId(i);
                        resId = collectionInfo.getCoinSlotImage(coinSlot, true);
                        assertEquals(chkResId, resId);
                    }
                }
            });
        }
    }

    /**
     * Test that creating collections does not assign image IDs that are out-of-bounds
     * of the IMAGE ID list.
     */
    @Test
    public void test_imageIdCreation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                for (FullCollection scenario1 : getRandomTestScenarios(mCoinTypeObj, 5)) {
                    // Assert that collection creation didn't allocate any invalid image IDs
                    CollectionInfo collectionInfo = scenario1.mCollectionListInfo.getCollectionObj();
                    Object[][] imageIds = collectionInfo.getImageIds();
                    for (CoinSlot coinSlot : scenario1.mCoinList) {
                        assertTrue(coinSlot.getImageId() >= -1);
                        assertTrue(coinSlot.getImageId() < imageIds.length);
                    }
                }
            });
        }
    }
}
