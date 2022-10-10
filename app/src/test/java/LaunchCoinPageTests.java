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

import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CollectionInfo;
import com.coincollection.CollectionPage;
import com.coincollection.MainActivity;
import com.spencerpages.MainApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

@RunWith(ParameterizedRobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class LaunchCoinPageTests extends BaseTestCase {

    private final CollectionInfo mCoinTypeObj;

    public LaunchCoinPageTests(CollectionInfo coinTypeObj) {
        mCoinTypeObj = coinTypeObj;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static List<?> getCoinTypeObj() {
        return Arrays.asList(MainApplication.COLLECTION_TYPES);
    }

    /**
     * Test launching collections
     */
    @Test
    public void test_launchCoinPage() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(activity -> {
                for (FullCollection scenario1 : getRandomTestScenarios(mCoinTypeObj, 1)) {
                    // Create the collection in the database
                    activity.mDbAdapter.createAndPopulateNewTable(scenario1.mCollectionListInfo,
                            scenario1.mDisplayOrder, scenario1.mCoinList);
                    activity.updateCollectionListFromDatabase();

                    // Launch the collection
                    Intent intent = activity.launchCoinPageActivity(scenario1.mCollectionListInfo);
                    assertNotNull(intent);
                    CollectionPage coinActivity = Robolectric.buildActivity(CollectionPage.class, intent).get();
                    assertNotNull(coinActivity);
                    coinActivity.onCreate(null);

                    // Clean up
                    activity.deleteDatabase(scenario1.mCollectionListInfo.getName());
                }
            });
        }
    }
}
