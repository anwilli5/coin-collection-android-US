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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.MainActivity;
import com.spencerpages.collections.AmericanEagleSilverDollars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.HashMap;

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.spencerpages.MainApplication.DATABASE_NAME;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class CollectionUpgradeTests {

    private final static int VERSION_1_YEAR = 2013;

    class TestDatabaseHelper extends SQLiteOpenHelper {

        TestDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE collection_info (_id integer primary key,"
                    + " name text not null,"
                    + " coinType text not null,"
                    + " total integer"
                    + ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void createV1Collection(SQLiteDatabase db, String collectionName, String coinType, ArrayList<Object[]> coinList) {

        db.execSQL("CREATE TABLE [" + collectionName
                + "] (_id integer primary key,"
                + " coinIdentifier text not null,"
                + " coinMint text,"
                + " inCollection integer);");

        for(Object[] coinInfo : coinList){
            ContentValues initialValues = new ContentValues();
            initialValues.put("coinIdentifier", (String) coinInfo[0]);
            initialValues.put("coinMint", (String) coinInfo[1]);
            initialValues.put("inCollection", (int) coinInfo[2]);
            db.insert("[" + collectionName + "]", null, initialValues);
        }

        ContentValues values = new ContentValues();
        values.put("name", collectionName);
        values.put("coinType", coinType);
        values.put("total", coinList.size());
        long value = db.insert("collection_info", null, values);
    }

    private void populateGeneralCollection(SQLiteDatabase db, String coinType, String collectionName, int startYear,
                                           CollectionInfo collection) {
        ArrayList<Object[]> coinList = new ArrayList<>();
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
    }

    private void validateUpdatedDb(CollectionInfo collectionInfo, String collectionName, MainActivity activity){

        // Create a new database from scratch
        HashMap<String, Object> parameters = new HashMap<>();
        collectionInfo.getCreationParameters(parameters);
        ArrayList<CoinSlot> newCoinList = new ArrayList<>();
        collectionInfo.populateCollectionLists(parameters, newCoinList);

        // Get coins from the updated database
        activity.mDbAdapter.open();
        Cursor resultCursor = activity.mDbAdapter.getAllIdentifiers(collectionName);
        assertNotNull(resultCursor);
        ArrayList<CoinSlot> dbCoinList = new ArrayList<>();
        if (resultCursor.moveToFirst()){
            do {
                dbCoinList.add(new CoinSlot(
                        resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_IDENTIFIER)),
                        resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_MINT)),
                        false));
            } while(resultCursor.moveToNext());
        }
        resultCursor.close();

        // Make sure coin lists match
        assertEquals(newCoinList.size(), dbCoinList.size());
        for(int i = 0; i < newCoinList.size(); i++){
            assertEquals(newCoinList.get(i).getIdentifier(), dbCoinList.get(i).getIdentifier());
            assertEquals(newCoinList.get(i).getMint(), dbCoinList.get(i).getMint());
        }

        // Make sure total matches
        resultCursor = activity.mDbAdapter.getAllTables();
        Boolean foundTable = false;
        if (resultCursor.moveToFirst()){
            do{
                if(collectionName.equals(resultCursor.getString(resultCursor.getColumnIndex("name")))){
                    foundTable = true;
                    assertEquals(newCoinList.size(), resultCursor.getInt(resultCursor.getColumnIndex("total")));
                }
            } while(resultCursor.moveToNext());
        }
        resultCursor.close();
        assertEquals(Boolean.TRUE, foundTable);
    }

    /**
     * For AmericanEagleSilverDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_AmericanEagleSilverDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new AmericanEagleSilverDollars();
        String coinType = "American Eagle Silver Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1986;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        populateGeneralCollection(db, coinType, collectionName, startYear, collection);
        db.close();
        MainActivity activity = Robolectric.setupActivity(MainActivity.class);

        // Compare against a new database
        validateUpdatedDb(collection, collectionName, activity);
    }

    /**
     * For AmericanInnovationDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For BarberDimes
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For BarberHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For BarberQuarters
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For BuffaloNickels
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For EisenhowerDollar
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For FirstSpouseGoldCoins
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For FranklinHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For IndianHeadCents
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For JeffersonNickels
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For KennedyHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For LibertyHeadNickels
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For LincolnCents
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For MercuryDimes
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For MorganDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For NationalParkQuarters
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For NativeAmericanDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For PeaceDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For PresidentialDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For RooseveltDimes
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For StandingLibertyQuarters
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For StateQuarters
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For SusanBAnthonyDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For WalkingLibertyHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */

    /**
     * For WashingtonQuarters
     * - Test that the number of coins is correct upon collection creation
     */
}