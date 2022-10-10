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

package com.spencerpages.collections;

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.DatabaseHelper.runSqlUpdate;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class FirstSpouseGoldCoins extends CollectionInfo {

    public static final String COLLECTION_TYPE = "First Spouse Gold Coins";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Martha Washington", R.drawable.fs_2007_martha_washington_unc},
            {"Abigail Adams", R.drawable.fs_2007_abigail_adams_unc},
            {"Thomas Jefferson's Liberty", R.drawable.fs_2007_jeffersons_liberty_unc},
            {"Dolley Madison", R.drawable.fs_2007_dolley_madison_unc},
            {"Elizabeth Monroe", R.drawable.fs_2008_elizabeth_monroe_unc},
            {"Louisa Adams", R.drawable.fs_2008_louisa_adams_unc},
            {"Andrew Jackson's Liberty", R.drawable.fs_2008_jacksons_liberty_unc},
            {"Martin Van Buren's Liberty", R.drawable.fs_2008_van_burens_liberty_unc},
            {"Anna Harrison", R.drawable.fs_2009_anna_harrison_unc},
            {"Letitia Tyler", R.drawable.fs_2009_letitia_tyler_unc},
            {"Julia Tyler", R.drawable.fs_2009_julia_tyler_unc},
            {"Sarah Polk", R.drawable.fs_2009_sarah_polk_unc},
            {"Margaret Taylor", R.drawable.fs_2009_margaret_taylor_unc},
            {"Abigail Fillmore", R.drawable.fs_2010_abigail_fillmore_unc},
            {"Jane Pierce", R.drawable.fs_2010_jane_pierce_unc},
            {"James Buchanan's Liberty", R.drawable.fs_2010_buchanans_liberty_unc},
            {"Mary Todd Lincoln", R.drawable.fs_2010_mary_todd_lincoln_unc},
            {"Eliza Johnson", R.drawable.fs_2011_eliza_johnson_unc},
            {"Julia Grant", R.drawable.fs_2011_julia_grant_unc},
            {"Lucy Hayes", R.drawable.fs_2011_lucy_hayes_unc},
            {"Lucretia Garfield", R.drawable.fs_2011_lucretia_garfield_unc},
            {"Alice Paul", R.drawable.fs_2012_alice_paul_unc},
            {"Frances Cleveland 1", R.drawable.fs_2012_frances_cleveland_1_unc},
            {"Caroline Harrison", R.drawable.fs_2012_caroline_harrison_unc},
            {"Frances Cleveland 2", R.drawable.fs_2012_frances_cleveland_2_unc},
            {"Ida McKinley", R.drawable.fs_2013_ida_mckinley_unc},
            {"Edith Roosevelt", R.drawable.fs_2013_edith_roosevelt_unc},
            {"Helen Taft", R.drawable.fs_2013_helen_taft_unc},
            {"Ellen Wilson", R.drawable.fs_2013_ellen_wilson_unc},
            {"Edith Wilson", R.drawable.fs_2013_edith_wilson_unc},
            {"Florence Harding", R.drawable.fs_2014_florence_harding_unc},
            {"Grace Coolidge", R.drawable.fs_2014_grace_coolidge_unc},
            {"Lou Hoover", R.drawable.fs_2014_lou_hoover_unc},
            {"Eleanor Roosevelt", R.drawable.fs_2014_eleanor_roosevelt_unc},
            {"Bess Truman", R.drawable.fs_2015_bess_truman_unc},
            {"Mamie Eisenhower", R.drawable.fs_2015_mamie_eisenhower_unc},
            {"Jacqueline Kennedy", R.drawable.fs_2015_jacqueline_kennedy_unc},
            {"Lady Bird Johnson", R.drawable.fs_2015_lady_bird_johnson_unc},
            {"Patricia Nixon", R.drawable.fs_2016_patricia_nixon_unc},
            {"Betty Ford", R.drawable.fs_2016_betty_ford_unc},
            {"Nancy Reagan", R.drawable.fs_2016_nancy_reagan_unc},
            {"Barbara Bush", R.drawable.fs_2020_barbara_bush_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.first_spouse_obverse;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        int coinIndex = 0;
        for (Object[] coinData : COIN_IDENTIFIERS) {
            String identifier = (String) coinData[0];
            coinList.add(new CoinSlot(identifier, "", coinIndex++));
        }
    }

    @Override
    public int getAttributionResId() {
        return R.string.attr_mint;
    }

    @Override
    public int getStartYear() {
        return 0;
    }

    @Override
    public int getStopYear() {
        return 0;
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {
        String tableName = collectionListInfo.getName();
        int total = 0;

        if (oldVersion <= 3) {
            // Add in 2012 First Spouse Gold Coins
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Alice Paul");
            newCoinIdentifiers.add("Frances Cleveland 1");
            newCoinIdentifiers.add("Caroline Harrison");
            newCoinIdentifiers.add("Frances Cleveland 2");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 4) {
            // Add in 2013 First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Ida McKinley");
            newCoinIdentifiers.add("Edith Roosevelt");
            newCoinIdentifiers.add("Helen Taft");
            newCoinIdentifiers.add("Ellen Wilson");
            newCoinIdentifiers.add("Edith Wilson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 6) {
            // Add in 2014 First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Florence Harding");
            newCoinIdentifiers.add("Grace Coolidge");
            newCoinIdentifiers.add("Lou Hoover");
            newCoinIdentifiers.add("Eleanor Roosevelt");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 7) {
            // Add in 2015 First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Bess Truman");
            newCoinIdentifiers.add("Mamie Eisenhower");
            newCoinIdentifiers.add("Jacqueline Kennedy");
            newCoinIdentifiers.add("Lady Bird Johnson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 8) {
            // Add in remaining First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Patricia Nixon");
            newCoinIdentifiers.add("Betty Ford");
            newCoinIdentifiers.add("Nancy Reagan");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 10) {

            ContentValues values = new ContentValues();

            // Replace all the ’ characters with ' characters
            values.put(COL_COIN_IDENTIFIER, "Thomas Jefferson's Liberty");
            runSqlUpdate(db, tableName, values, COL_COIN_IDENTIFIER + "=?", new String[]{"Thomas Jefferson’s Liberty"});
            values.clear();

            values.put(COL_COIN_IDENTIFIER, "Andrew Jackson's Liberty");
            runSqlUpdate(db, tableName, values, COL_COIN_IDENTIFIER + "=?", new String[]{"Andrew Jackson’s Liberty"});
            values.clear();

            values.put(COL_COIN_IDENTIFIER, "Martin Van Buren's Liberty");
            runSqlUpdate(db, tableName, values, COL_COIN_IDENTIFIER + "=?", new String[]{"Martin Van Buren’s Liberty"});
            values.clear();

            values.put(COL_COIN_IDENTIFIER, "James Buchanan's Liberty");
            runSqlUpdate(db, tableName, values, COL_COIN_IDENTIFIER + "=?", new String[]{"James Buchanan’s Liberty"});
            values.clear();

        }

        if (oldVersion <= 16) {
            // Add in 2020 First Spouse Gold Coins
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Barbara Bush");
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        return total;
    }
}
