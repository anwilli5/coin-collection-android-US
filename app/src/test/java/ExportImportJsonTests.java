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
import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.MainActivity;
import com.spencerpages.MainApplication;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.coincollection.ExportImportHelper.JSON_CHARSET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(ParameterizedRobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class ExportImportJsonTests extends BaseTestCase {

    private final CollectionInfo mCoinTypeObj;

    public ExportImportJsonTests(CollectionInfo coinTypeObj) {
        mCoinTypeObj = coinTypeObj;
    }

    @Rule
    public final TemporaryFolder mTempFolder = new TemporaryFolder();

    /**
     * Get a temporary file with a given name
     * @param filename file name
     * @return file
     */
    File getTempFile(String filename) {
        try {
            return mTempFolder.newFile(filename);
        } catch (IOException e) {
            fail();
        }
        return null;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static List<?> getCoinTypeObj() {
        return Arrays.asList(MainApplication.COLLECTION_TYPES);
    }

    /**
     * Test exporting/importing in JSON for each collection type
     */
    @Test
    public void test_exportImportJson() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                        .putExtra(MainActivity.UNIT_TEST_USE_ASYNC_TASKS, false))) {
            scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
                @Override
                public void perform(MainActivity activity) {
                    for (FullCollection scenario : getRandomTestScenarios(activity, mCoinTypeObj, 1)) {
                        // Create the collection in the database
                        activity.mDbAdapter.createAndPopulateNewTable(scenario.mCollectionListInfo,
                                scenario.mDisplayOrder, scenario.mCoinList);
                        activity.updateCollectionListFromDatabase();

                        File exportFile = getTempFile("coin-collection-" + random.nextInt() + ".json");
                        OutputStream outputStream = openOutputStream(exportFile);
                        try {
                            // Write the JSON file
                            JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, JSON_CHARSET));
                            scenario.mCollectionListInfo.writeToJson(writer, activity.mDbAdapter, scenario.mCoinList);
                            writer.close();
                            closeStream(outputStream);

                            // Read the JSON file
                            InputStream inputStream = openInputStream(exportFile);
                            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, JSON_CHARSET));
                            ArrayList<CoinSlot> checkCoinList = new ArrayList<>();
                            CollectionListInfo checkInfo = new CollectionListInfo(reader, checkCoinList);
                            reader.close();
                            closeStream(inputStream);

                            // Compare the results
                            compareCollectionListInfos(scenario.mCollectionListInfo, checkInfo);
                            assertEquals(scenario.mCoinList, checkCoinList);

                        } catch (Exception ignored) {
                            fail();
                        }

                        // Clean up
                        activity.deleteDatabase(scenario.mCollectionListInfo.getName());
                    }
                }
            });
        }
    }
}
