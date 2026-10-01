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
package com.coincollection;

import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.CollectionListInfo.COL_TOTAL;
import static com.coincollection.CollectionListInfo.TBL_COLLECTION_INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.spencerpages.BaseTestCase;
import com.spencerpages.MainApplication;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.WalkingLibertyHalfDollars;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Tests that the data layer reports failures instead of hiding them, and copes with values
 * it didn't write itself
 */
@RunWith(RobolectricTestRunner.class)
public class DataLayerRobustnessTests extends BaseTestCase {

    private static final String COLLECTION_NAME = "Robustness Pennies";

    private DatabaseAdapter mDbAdapter;

    @Before
    public void openDatabase() {
        mDbAdapter = new DatabaseAdapter(ApplicationProvider.getApplicationContext());
        mDbAdapter.open();
    }

    @After
    public void closeDatabase() {
        mDbAdapter.close();
    }

    /**
     * Creates a three-coin Lincoln Cents collection and returns its coins as stored
     */
    private ArrayList<CoinSlot> createCollection() {
        ArrayList<CoinSlot> coins = new ArrayList<>();
        coins.add(new CoinSlot("1909", "", 0));
        coins.add(new CoinSlot("1910", "", 1));
        coins.add(new CoinSlot("1911", "", 2));
        int typeIndex = MainApplication.getIndexFromCollectionNameStr(LincolnCents.COLLECTION_TYPE);
        CollectionListInfo info = new CollectionListInfo(COLLECTION_NAME, coins.size(), 0,
                typeIndex, CollectionPage.SIMPLE_DISPLAY, 1909, 1911, "0", "0");
        mDbAdapter.createAndPopulateNewTable(info, 0, coins);
        return mDbAdapter.getCoinList(COLLECTION_NAME, true);
    }

    private static SQLiteDatabase getDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        return new DatabaseHelper(context).getWritableDatabase();
    }

    private static int getStoredTotal(SQLiteDatabase db) {
        try (Cursor cursor = db.query(TBL_COLLECTION_INFO, new String[]{COL_TOTAL},
                COL_NAME + "=?", new String[]{COLLECTION_NAME}, null, null, null)) {
            assertTrue(cursor.moveToFirst());
            return cursor.getInt(0);
        }
    }

    /**
     * A failed insert used to return -1 unnoticed, and the upgrade code counted the coin
     * anyway, so the collection's total no longer matched its coins
     */
    @Test
    public void test_addCoinFailurePropagates() {
        createCollection();
        SQLiteDatabase db = getDatabase();

        // The identifier column is NOT NULL, so this insert fails
        assertThrows(SQLException.class,
                () -> DatabaseHelper.addCoin(db, COLLECTION_NAME, null, "", -1, 3));

        assertEquals(3, mDbAdapter.getCoinList(COLLECTION_NAME, false).size());
        assertEquals(3, getStoredTotal(db));
    }

    @Test
    public void test_toggleInCollection() {
        CoinSlot coin = createCollection().get(1);
        assertEquals(0, mDbAdapter.fetchIsInCollection(COLLECTION_NAME, coin));
        mDbAdapter.toggleInCollection(COLLECTION_NAME, coin);
        assertEquals(1, mDbAdapter.fetchIsInCollection(COLLECTION_NAME, coin));
        mDbAdapter.toggleInCollection(COLLECTION_NAME, coin);
        assertEquals(0, mDbAdapter.fetchIsInCollection(COLLECTION_NAME, coin));

        // Only the toggled coin changed
        for (CoinSlot other : mDbAdapter.getCoinList(COLLECTION_NAME, false)) {
            assertEquals(0, mDbAdapter.fetchIsInCollection(COLLECTION_NAME, other));
        }
    }

    /**
     * The coin list reads NULL as not collected, so toggling it must collect the coin
     */
    @Test
    public void test_toggleInCollectionTreatsNullAsNotCollected() {
        CoinSlot coin = createCollection().get(0);
        getDatabase().execSQL("UPDATE [" + COLLECTION_NAME + "] SET "
                + CoinSlot.COL_IN_COLLECTION + " = NULL WHERE _id = " + coin.getDatabaseId());
        mDbAdapter.toggleInCollection(COLLECTION_NAME, coin);
        assertEquals(1, mDbAdapter.fetchIsInCollection(COLLECTION_NAME, coin));
    }

    @Test
    public void test_toggleInCollectionMissingCoinThrows() {
        createCollection();
        CoinSlot missing = new CoinSlot(9999, "1999", "", false, 0, false, -1);
        assertThrows(SQLException.class, () -> mDbAdapter.toggleInCollection(COLLECTION_NAME, missing));
    }

    @Test
    public void test_coinSlotEqualsAndHashCodeAllowNullMint() {
        CoinSlot first = new CoinSlot(1, "1909", null, false, 0, false, -1);
        CoinSlot second = new CoinSlot(2, "1909", null, true, 5, false, -1);
        CoinSlot blankMint = new CoinSlot(3, "1909", "", false, 0, false, -1);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, blankMint);
        assertNotEquals(blankMint, first);

        HashSet<CoinSlot> set = new HashSet<>();
        set.add(first);
        assertTrue(set.contains(second));
    }

    /**
     * The legacy parameter detection reads a year from the first four characters of an
     * identifier. A shorter identifier (e.g. a custom coin) used to throw
     * StringIndexOutOfBoundsException and fail the database upgrade
     */
    @Test
    public void test_shortIdentifiersDoNotBreakLegacyParameterDetection() {
        int typeIndex = MainApplication.getIndexFromCollectionNameStr(WalkingLibertyHalfDollars.COLLECTION_TYPE);
        CollectionListInfo info = new CollectionListInfo("Halves", 2, 0, typeIndex,
                CollectionPage.SIMPLE_DISPLAY, 0, 0, "0", "0");
        ArrayList<CoinSlot> coins = new ArrayList<>();
        coins.add(new CoinSlot("Odd", "", 0));
        coins.add(new CoinSlot("1934", "", 1));

        info.setCreationParametersFromCoinData(coins);

        // The start year can't be read, so it falls back to the series' own dates
        WalkingLibertyHalfDollars series = new WalkingLibertyHalfDollars();
        assertEquals(series.getStartYear(), info.getStartYear());
        assertEquals(series.getStopYear(), info.getEndYear());
    }
}
