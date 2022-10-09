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

import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CollectionInfo;
import com.spencerpages.MainApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

@RunWith(ParameterizedRobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class DatabaseAccessTests extends BaseTestCase {

    private final CollectionInfo mCoinTypeObj;

    public DatabaseAccessTests(CollectionInfo coinTypeObj) {
        mCoinTypeObj = coinTypeObj;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static List<?> getCoinTypeObj() {
        return Arrays.asList(MainApplication.COLLECTION_TYPES);
    }

    @Test
    public void test_createWithVariableDates() {

        try (ActivityScenario<CoinPageCreator> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class)
                        .putExtra(CoinPageCreator.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {

                for (FullCollection scenario1 : getRandomTestScenarios(mCoinTypeObj, 20)) {
                    String collectionName = scenario1.mCollectionListInfo.getName();

                    // Create the collection in the database
                    activity.mDbAdapter.createAndPopulateNewTable(scenario1.mCollectionListInfo,
                            scenario1.mDisplayOrder, scenario1.mCoinList);

                    // Check that the collection was made correctly in the database
                    compareCollectionWithDb(activity, scenario1.mCollectionListInfo,
                            scenario1.mCoinList, scenario1.mDisplayOrder);

                    // Update the collection in the database
                    activity.mDbAdapter.updateExistingCollection(collectionName,
                            scenario1.mCollectionListInfo, scenario1.mCoinList);

                    // Check that the collection was updated correctly in the database
                    compareCollectionWithDb(activity, scenario1.mCollectionListInfo,
                            scenario1.mCoinList, scenario1.mDisplayOrder);

                    // Delete the collection from the database
                    activity.mDbAdapter.dropCollectionTable(collectionName);
                }
            });
        }
    }
}
