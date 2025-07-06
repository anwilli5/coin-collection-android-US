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

import static com.coincollection.CollectionListInfo.ALL_CHECKBOXES_MASK;
import static com.coincollection.CollectionListInfo.ALL_MINT_MASK;
import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.helper.ParcelableHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
public class CoinPageCreatorTests extends BaseTestCase {

    /**
     * Test converting to and from parameters
     */
    @Test
    public void test_createFromParameters() {

        try (ActivityScenario<CoinPageCreator> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class))) {
            scenario.onActivity(activity -> {
                // Set up collections
                for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS) {
                    activity.mCoinList = new ArrayList<>();
                    ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                    int index = info.getCollectionTypeIndex();
                    activity.setInternalStateFromCollectionIndex(index, activity.getCollectionListPos(index), parameters);
                    activity.createOrUpdateCoinListForAsyncThread();
                    CollectionListInfo checkInfo = activity.getCollectionInfoFromParameters(info.getName());
                    assertTrue(SharedTest.compareCollectionListInfos(info, checkInfo));
                    assertTrue(activity.validateStartAndStopYears());
                }
            });
        }
    }

    /**
     * Test updating a collection
     */
    @Test
    public void test_updateCollection() {
        for (final CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS) {
            try (ActivityScenario<CoinPageCreator> scenario = ActivityScenario.launch(
                    new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class)
                            .putExtra(CoinPageCreator.EXISTING_COLLECTION_EXTRA, info))) {
                scenario.onActivity(activity -> {
                    // Create the collection in the DB before testing
                    HashMap<String, Object> parameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                    ArrayList<CoinSlot> coinList = new ArrayList<>();
                    info.getCollectionObj().populateCollectionLists(parameters, coinList);
                    activity.mDbAdapter.createAndPopulateNewTable(info, 0, coinList);

                    // Perform the update and check the result
                    activity.createOrUpdateCoinListForAsyncThread();
                    compareCollectionWithDb(activity, info, null, 0);

                    // Delete the collection for the next test
                    activity.mDbAdapter.dropCollectionTable(info.getName());
                });
            }
        }
    }

    /**
     * Test getCreationParameters() for each collection
     */
    @Test
    public void test_getCreationParameters() {
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            CollectionListInfo collectionListInfo = getCollectionListInfo("X", collectionInfo, new ArrayList<>());
            collectionListInfo.setMintMarkFlags(Long.toString(-1L)); // All possible mint marks set
            collectionListInfo.setCheckboxFlags(Long.toString(-1L)); // All possible checkboxes set
            ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(collectionListInfo);

            // Assert that collections don't provide the same option twice
            // - This can also occur if parameters themselves map to the same value, such as coins
            //   that cannot live in the same collection due to sharing a checkbox value.
            ArrayList<Integer> checkBoxValues = new ArrayList<>();
            for (String key: CoinPageCreator.CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.values()) {
                if (parameters.containsKey(key)) {
                    for(Integer value: checkBoxValues) {
                        assertNotEquals(value, parameters.get(key));
                    }
                    checkBoxValues.add((Integer)parameters.get(key));
                }
            }

            // Assert that collections don't provide the same mint mark twice
            ArrayList<Integer> mintMarkValues = new ArrayList<>();
            for (String key: CoinPageCreator.SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.values()) {
                if (parameters.containsKey(key)) {
                    for(Integer value: mintMarkValues) {
                        assertNotEquals(value, parameters.get(key));
                    }
                    mintMarkValues.add((Integer)parameters.get(key));
                }
            }

            // Assert that parameters aren't outside the expected range
            assertEquals(0, (CoinPageCreator.getCheckboxFlagsFromParameters(parameters) & ~ALL_CHECKBOXES_MASK));
            assertEquals(0, (CoinPageCreator.getMintMarkFlagsFromParameters(parameters) & ~ALL_MINT_MASK));
        }
    }

    /**
     * Test getCheckboxFlagsFromParameters() for each collection
     */
    @Test
    public void test_getCheckboxFlagsFromParameters() {
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            CollectionListInfo collectionListInfo = getCollectionListInfo("X", collectionInfo, new ArrayList<>());
            ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(collectionListInfo);
            // Unset all checkbox options
            for (String key: CoinPageCreator.CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                if (parameters.containsKey(key)) {
                    parameters.put(key, Boolean.FALSE);
                }
            }
            // Assert that setting each checkbox item to true results in a non-zero value
            // when getCheckboxFlagsFromParameters() is called
            for (String key: CoinPageCreator.CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                if (parameters.containsKey(key)) {
                    parameters.put(key, Boolean.TRUE);
                    assertNotEquals(0, CoinPageCreator.getCheckboxFlagsFromParameters(parameters));
                    parameters.put(key, Boolean.FALSE);
                }
            }
        }
    }

    /**
     * Test getMintMarkFlagsFromParameters() for each collection
     */
    @Test
    public void test_getMintMarkFlagsFromParameters() {
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            CollectionListInfo collectionListInfo = getCollectionListInfo("X", collectionInfo, new ArrayList<>());
            ParcelableHashMap parameters = CoinPageCreator.getParametersFromCollectionListInfo(collectionListInfo);
            // Unset all mint mark options
            for (String key: CoinPageCreator.SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                if (parameters.containsKey(key)) {
                    parameters.put(key, Boolean.FALSE);
                }
            }
            // Assert that setting each checkbox item to true results in a non-zero value
            // when getMintMarkFlagsFromParameters() is called
            for (String key: CoinPageCreator.SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                if (parameters.containsKey(key)) {
                    parameters.put(key, Boolean.TRUE);
                    assertNotEquals(0, CoinPageCreator.getMintMarkFlagsFromParameters(parameters));
                    parameters.put(key, Boolean.FALSE);
                }
            }
        }
    }
}
