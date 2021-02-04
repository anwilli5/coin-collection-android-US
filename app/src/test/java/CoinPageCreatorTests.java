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

import com.coincollection.CoinPageCreator;
import com.coincollection.CollectionListInfo;
import com.spencerpages.SharedTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import static com.spencerpages.SharedTest.COLLECTION_LIST_INFO_SCENARIOS;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class CoinPageCreatorTests extends BaseTestCase {

    /**
     * Test converting to and from parameters
     */
    @Test
    public void test_createFromParameters() {

        try(ActivityScenario<CoinPageCreator> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CoinPageCreator.class)
                        .putExtra(CoinPageCreator.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<CoinPageCreator>() {
                @Override
                public void perform(CoinPageCreator activity) {
                    // Set up collections
                    for (CollectionListInfo info : COLLECTION_LIST_INFO_SCENARIOS) {
                        activity.mCoinList = new ArrayList<>();
                        activity.mParameters = CoinPageCreator.getParametersFromCollectionListInfo(info);
                        activity.mExistingCollection = null;
                        activity.mCoinTypeIndex = info.getCollectionTypeIndex();
                        activity.mCollectionObj = info.getCollectionObj();
                        activity.createOrUpdateCoinListForAsyncThread();
                        CollectionListInfo checkInfo = activity.getCollectionInfoFromParameters(info.getName());
                        assertTrue(SharedTest.compareCollectionListInfos(info, checkInfo));
                    }
                }
            });
        }
    }
}
