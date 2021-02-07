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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.DatabaseHelper.runSqlUpdate;

public class FirstSpouseGoldCoins extends CollectionInfo {

    public static final String COLLECTION_TYPE = "First Spouse Gold Coins";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Martha Washington",          R.drawable.fs_2007_martha_washington_unc,   R.drawable.fs_2007_martha_washington_unc_25},
            {"Abigail Adams",              R.drawable.fs_2007_abigail_adams_unc,       R.drawable.fs_2007_abigail_adams_unc_25},
            {"Thomas Jefferson's Liberty", R.drawable.fs_2007_jeffersons_liberty_unc,  R.drawable.fs_2007_jeffersons_liberty_unc_25},
            {"Dolley Madison",             R.drawable.fs_2007_dolley_madison_unc,      R.drawable.fs_2007_dolley_madison_unc_25},
            {"Elizabeth Monroe",           R.drawable.fs_2008_elizabeth_monroe_unc,    R.drawable.fs_2008_elizabeth_monroe_unc_25},
            {"Louisa Adams",               R.drawable.fs_2008_louisa_adams_unc,        R.drawable.fs_2008_louisa_adams_unc_25},
            {"Andrew Jackson's Liberty",   R.drawable.fs_2008_jacksons_liberty_unc,    R.drawable.fs_2008_jacksons_liberty_unc_25},
            {"Martin Van Buren's Liberty", R.drawable.fs_2008_van_burens_liberty_unc,  R.drawable.fs_2008_van_burens_liberty_unc_25},
            {"Anna Harrison",              R.drawable.fs_2009_anna_harrison_unc,       R.drawable.fs_2009_anna_harrison_unc_25},
            {"Letitia Tyler",              R.drawable.fs_2009_letitia_tyler_unc,       R.drawable.fs_2009_letitia_tyler_unc_25},
            {"Julia Tyler",                R.drawable.fs_2009_julia_tyler_unc,         R.drawable.fs_2009_julia_tyler_unc_25},
            {"Sarah Polk",                 R.drawable.fs_2009_sarah_polk_unc,          R.drawable.fs_2009_sarah_polk_unc_25},
            {"Margaret Taylor",            R.drawable.fs_2009_margaret_taylor_unc,     R.drawable.fs_2009_margaret_taylor_unc_25},
            {"Abigail Fillmore",           R.drawable.fs_2010_abigail_fillmore_unc,    R.drawable.fs_2010_abigail_fillmore_unc_25},
            {"Jane Pierce",                R.drawable.fs_2010_jane_pierce_unc,         R.drawable.fs_2010_jane_pierce_unc_25},
            {"James Buchanan's Liberty",   R.drawable.fs_2010_buchanans_liberty_unc,   R.drawable.fs_2010_buchanans_liberty_unc_25},
            {"Mary Todd Lincoln",          R.drawable.fs_2010_mary_todd_lincoln_unc,   R.drawable.fs_2010_mary_todd_lincoln_unc_25},
            {"Eliza Johnson",              R.drawable.fs_2011_eliza_johnson_unc,       R.drawable.fs_2011_eliza_johnson_unc_25},
            {"Julia Grant",                R.drawable.fs_2011_julia_grant_unc,         R.drawable.fs_2011_julia_grant_unc_25},
            {"Lucy Hayes",                 R.drawable.fs_2011_lucy_hayes_unc,          R.drawable.fs_2011_lucy_hayes_unc_25},
            {"Lucretia Garfield",          R.drawable.fs_2011_lucretia_garfield_unc,   R.drawable.fs_2011_lucretia_garfield_unc_25},
            {"Alice Paul",                 R.drawable.fs_2012_alice_paul_unc,          R.drawable.fs_2012_alice_paul_unc_25},
            {"Frances Cleveland 1",        R.drawable.fs_2012_frances_cleveland_1_unc, R.drawable.fs_2012_frances_cleveland_1_unc_25},
            {"Caroline Harrison",          R.drawable.fs_2012_caroline_harrison_unc,   R.drawable.fs_2012_caroline_harrison_unc_25},
            {"Frances Cleveland 2",        R.drawable.fs_2012_frances_cleveland_2_unc, R.drawable.fs_2012_frances_cleveland_2_unc_25},
            {"Ida McKinley",               R.drawable.fs_2013_ida_mckinley_unc,        R.drawable.fs_2013_ida_mckinley_unc_25},
            {"Edith Roosevelt",            R.drawable.fs_2013_edith_roosevelt_unc,     R.drawable.fs_2013_edith_roosevelt_unc_25},
            {"Helen Taft",                 R.drawable.fs_2013_helen_taft_unc,          R.drawable.fs_2013_helen_taft_unc_25},
            {"Ellen Wilson",               R.drawable.fs_2013_ellen_wilson_unc,        R.drawable.fs_2013_ellen_wilson_unc_25},
            {"Edith Wilson",               R.drawable.fs_2013_edith_wilson_unc,        R.drawable.fs_2013_edith_wilson_unc_25},
            {"Florence Harding",           R.drawable.fs_2014_florence_harding_unc,    R.drawable.fs_2014_florence_harding_unc_25},
            {"Grace Coolidge",             R.drawable.fs_2014_grace_coolidge_unc,      R.drawable.fs_2014_grace_coolidge_unc_25},
            {"Lou Hoover",                 R.drawable.fs_2014_lou_hoover_unc,          R.drawable.fs_2014_lou_hoover_unc_25},
            {"Eleanor Roosevelt",          R.drawable.fs_2014_eleanor_roosevelt_unc,   R.drawable.fs_2014_eleanor_roosevelt_unc_25},
            {"Bess Truman",                R.drawable.fs_2015_bess_truman_unc,         R.drawable.fs_2015_bess_truman_unc_25},
            {"Mamie Eisenhower",           R.drawable.fs_2015_mamie_eisenhower_unc,    R.drawable.fs_2015_mamie_eisenhower_unc_25},
            {"Jacqueline Kennedy",         R.drawable.fs_2015_jacqueline_kennedy_unc,  R.drawable.fs_2015_jacqueline_kennedy_unc_25},
            {"Lady Bird Johnson",          R.drawable.fs_2015_lady_bird_johnson_unc,   R.drawable.fs_2015_lady_bird_johnson_unc_25},
            {"Patricia Nixon",             R.drawable.fs_2016_patricia_nixon_unc,      R.drawable.fs_2016_patricia_nixon_unc_25},
            {"Betty Ford",                 R.drawable.fs_2016_betty_ford_unc,          R.drawable.fs_2016_betty_ford_unc_25},
            {"Nancy Reagan",               R.drawable.fs_2016_nancy_reagan_unc,        R.drawable.fs_2016_nancy_reagan_unc_25},
            {"Barbara Bush",               R.drawable.fs_2020_barbara_bush_unc,        R.drawable.fs_2020_barbara_bush_unc_25},
    };

    private static final HashMap<String, Integer[]> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS){
            COIN_MAP.put((String) coinData[0],
                    new Integer[]{(Integer) coinData[1], (Integer) coinData[2]});
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.first_spouse_obverse;

    @Override
    public String getCoinType() { return COLLECTION_TYPE; }

    @Override
    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot){
        Integer[] slotImages = COIN_MAP.get(coinSlot.getIdentifier());
        boolean inCollection = coinSlot.isInCollection();
        if(slotImages != null){
            return slotImages[inCollection ? 0 : 1];
        } else {
            return inCollection ? (int) COIN_IDENTIFIERS[0][1] : (int) COIN_IDENTIFIERS[0][2];
        }
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        for (Object[] coinData : COIN_IDENTIFIERS) {
            String identifier = (String) coinData[0];
            coinList.add(new CoinSlot(identifier, ""));
        }
    }

    @Override
    public int getAttributionResId(){
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

        if(oldVersion <= 3) {
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

        if(oldVersion <= 10){

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
