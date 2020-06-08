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
import com.coincollection.DatabaseHelper;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class FirstSpouseGoldCoins extends CollectionInfo {

    private static final String COLLECTION_TYPE = "First Spouse Gold Coins";

    private static final String[] FS_COIN_IDENTIFIERS = {
            "Martha Washington",
            "Abigail Adams",
            "Thomas Jefferson's Liberty",
            "Dolley Madison",
            "Elizabeth Monroe",
            "Louisa Adams",
            "Andrew Jackson's Liberty",
            "Martin Van Buren's Liberty",
            "Anna Harrison",
            "Letitia Tyler",
            "Julia Tyler",
            "Sarah Polk",
            "Margaret Taylor",
            "Abigail Fillmore",
            "Jane Pierce",
            "James Buchanan's Liberty",
            "Mary Todd Lincoln",
            "Eliza Johnson",
            "Julia Grant",
            "Lucy Hayes",
            "Lucretia Garfield",
            "Alice Paul",
            "Frances Cleveland 1",
            "Caroline Harrison",
            "Frances Cleveland 2",
            "Ida McKinley",
            "Edith Roosevelt",
            "Helen Taft",
            "Ellen Wilson",
            "Edith Wilson",
            "Florence Harding",
            "Grace Coolidge",
            "Lou Hoover",
            "Eleanor Roosevelt",
            "Bess Truman",
            "Mamie Eisenhower",
            "Jacqueline Kennedy",
            "Lady Bird Johnson",
            "Patricia Nixon",
            "Betty Ford",
            "Nancy Reagan",
            /*
            "Rosalynn Carter",
            "Barbara Bush",
            "Hillary Clinton",
            "Laura Bush",
            "Michelle Obama",
            */
    };

    private static final Integer[][] FS_IMAGE_IDENTIFIERS = {
            { R.drawable.fs_2007_martha_washington_unc,   R.drawable.fs_2007_martha_washington_unc_25},
            { R.drawable.fs_2007_abigail_adams_unc,       R.drawable.fs_2007_abigail_adams_unc_25},
            { R.drawable.fs_2007_jeffersons_liberty_unc,  R.drawable.fs_2007_jeffersons_liberty_unc_25},
            { R.drawable.fs_2007_dolley_madison_unc,      R.drawable.fs_2007_dolley_madison_unc_25},
            { R.drawable.fs_2008_elizabeth_monroe_unc,    R.drawable.fs_2008_elizabeth_monroe_unc_25},
            { R.drawable.fs_2008_louisa_adams_unc,        R.drawable.fs_2008_louisa_adams_unc_25},
            { R.drawable.fs_2008_jacksons_liberty_unc,    R.drawable.fs_2008_jacksons_liberty_unc_25},
            { R.drawable.fs_2008_van_burens_liberty_unc,  R.drawable.fs_2008_van_burens_liberty_unc_25},
            { R.drawable.fs_2009_anna_harrison_unc,       R.drawable.fs_2009_anna_harrison_unc_25},
            { R.drawable.fs_2009_letitia_tyler_unc,       R.drawable.fs_2009_letitia_tyler_unc_25},
            { R.drawable.fs_2009_julia_tyler_unc,         R.drawable.fs_2009_julia_tyler_unc_25},
            { R.drawable.fs_2009_sarah_polk_unc,          R.drawable.fs_2009_sarah_polk_unc_25},
            { R.drawable.fs_2009_margaret_taylor_unc,     R.drawable.fs_2009_margaret_taylor_unc_25},
            { R.drawable.fs_2010_abigail_fillmore_unc,    R.drawable.fs_2010_abigail_fillmore_unc_25},
            { R.drawable.fs_2010_jane_pierce_unc,         R.drawable.fs_2010_jane_pierce_unc_25},
            { R.drawable.fs_2010_buchanans_liberty_unc,   R.drawable.fs_2010_buchanans_liberty_unc_25},
            { R.drawable.fs_2010_mary_todd_lincoln_unc,   R.drawable.fs_2010_mary_todd_lincoln_unc_25},
            { R.drawable.fs_2011_eliza_johnson_unc,       R.drawable.fs_2011_eliza_johnson_unc_25},
            { R.drawable.fs_2011_julia_grant_unc,         R.drawable.fs_2011_julia_grant_unc_25},
            { R.drawable.fs_2011_lucy_hayes_unc,          R.drawable.fs_2011_lucy_hayes_unc_25},
            { R.drawable.fs_2011_lucretia_garfield_unc,   R.drawable.fs_2011_lucretia_garfield_unc_25},
            { R.drawable.fs_2012_alice_paul_unc,          R.drawable.fs_2012_alice_paul_unc_25},
            { R.drawable.fs_2012_frances_cleveland_1_unc, R.drawable.fs_2012_frances_cleveland_1_unc_25},
            { R.drawable.fs_2012_caroline_harrison_unc,   R.drawable.fs_2012_caroline_harrison_unc_25},
            { R.drawable.fs_2012_frances_cleveland_2_unc, R.drawable.fs_2012_frances_cleveland_2_unc_25},
            { R.drawable.fs_2013_ida_mckinley_unc,        R.drawable.fs_2013_ida_mckinley_unc_25},
            { R.drawable.fs_2013_edith_roosevelt_unc,     R.drawable.fs_2013_edith_roosevelt_unc_25},
            { R.drawable.fs_2013_helen_taft_unc,          R.drawable.fs_2013_helen_taft_unc_25},
            { R.drawable.fs_2013_ellen_wilson_unc,        R.drawable.fs_2013_ellen_wilson_unc_25},
            { R.drawable.fs_2013_edith_wilson_unc,        R.drawable.fs_2013_edith_wilson_unc_25},
            { R.drawable.fs_2014_florence_harding_unc,    R.drawable.fs_2014_florence_harding_unc_25},
            { R.drawable.fs_2014_grace_coolidge_unc,      R.drawable.fs_2014_grace_coolidge_unc_25},
            { R.drawable.fs_2014_lou_hoover_unc,          R.drawable.fs_2014_lou_hoover_unc_25},
            { R.drawable.fs_2014_eleanor_roosevelt_unc,   R.drawable.fs_2014_eleanor_roosevelt_unc_25},
            { R.drawable.fs_2015_bess_truman_unc,         R.drawable.fs_2015_bess_truman_unc_25},
            { R.drawable.fs_2015_mamie_eisenhower_unc,    R.drawable.fs_2015_mamie_eisenhower_unc_25},
            { R.drawable.fs_2015_jacqueline_kennedy_unc,  R.drawable.fs_2015_jacqueline_kennedy_unc_25},
            { R.drawable.fs_2015_lady_bird_johnson_unc,   R.drawable.fs_2015_lady_bird_johnson_unc_25},
            { R.drawable.fs_2016_patricia_nixon_unc,      R.drawable.fs_2016_patricia_nixon_unc_25},
            { R.drawable.fs_2016_betty_ford_unc,          R.drawable.fs_2016_betty_ford_unc_25},
            { R.drawable.fs_2016_nancy_reagan_unc,        R.drawable.fs_2016_nancy_reagan_unc_25},
    };

    private static final HashMap<String, Integer[]> FS_INFO = new HashMap<>();

    static {
        // Populate the FS_INFO HashMap for quick image ID lookups later
        for (int i = 0; i < FS_COIN_IDENTIFIERS.length; i++){
            FS_INFO.put(FS_COIN_IDENTIFIERS[i], FS_IMAGE_IDENTIFIERS[i]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.first_spouse_obverse;

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(CoinSlot coinSlot){
        return FS_INFO.get(coinSlot.getIdentifier())[coinSlot.isInCollection() ? 0 : 1];
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        for (String fsCoinIdentifier : FS_COIN_IDENTIFIERS) {
            coinList.add(new CoinSlot(fsCoinIdentifier, ""));
        }
    }
    public String getAttributionString(){
        return MainApplication.DEFAULT_ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
                                           int oldVersion, int newVersion) {

        int total = 0;

        if(oldVersion <= 3) {
            // Add in 2012 First Spouse Gold Coins
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Alice Paul");
            newCoinIdentifiers.add("Frances Cleveland 1");
            newCoinIdentifiers.add("Caroline Harrison");
            newCoinIdentifiers.add("Frances Cleveland 2");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
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
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 6) {
            // Add in 2014 First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Florence Harding");
            newCoinIdentifiers.add("Grace Coolidge");
            newCoinIdentifiers.add("Lou Hoover");
            newCoinIdentifiers.add("Eleanor Roosevelt");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 7) {
            // Add in 2015 First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Bess Truman");
            newCoinIdentifiers.add("Mamie Eisenhower");
            newCoinIdentifiers.add("Jacqueline Kennedy");
            newCoinIdentifiers.add("Lady Bird Johnson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 8) {
            // Add in remaining First Spouse Gold Coins

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Patricia Nixon");
            newCoinIdentifiers.add("Betty Ford");
            newCoinIdentifiers.add("Nancy Reagan");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if(oldVersion <= 10){

            ContentValues values = new ContentValues();

            // Replace all the ’ characters with ' characters
            values.put("coinIdentifier", "Thomas Jefferson's Liberty");
            db.update(tableName, values, "coinIdentifier=?", new String[]{"Thomas Jefferson’s Liberty"});
            values.clear();

            values.put("coinIdentifier", "Andrew Jackson's Liberty");
            db.update(tableName, values, "coinIdentifier=?", new String[]{"Andrew Jackson’s Liberty"});
            values.clear();

            values.put("coinIdentifier", "Martin Van Buren's Liberty");
            db.update(tableName, values, "coinIdentifier=?", new String[]{"Martin Van Buren’s Liberty"});
            values.clear();

            values.put("coinIdentifier", "James Buchanan's Liberty");
            db.update(tableName, values, "coinIdentifier=?", new String[]{"James Buchanan’s Liberty"});
            values.clear();

        }

        return total;
    }
}
