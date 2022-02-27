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

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PresidentialDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Presidential Dollars";

    //TODO Need to update to Harry S. Truman - requires db update too
    private static final Object[][] COIN_IDENTIFIERS = {
            {"George Washington",      R.drawable.pres_2007_george_washington_unc},
            {"John Adams",             R.drawable.pres_2007_john_adam_unc},
            {"Thomas Jefferson",       R.drawable.pres_2007_thomas_jefferson_unc},
            {"James Madison",          R.drawable.pres_2007_james_madison_unc},
            {"James Monroe",           R.drawable.pres_2008_james_monroe_unc},
            {"John Quincy Adams",      R.drawable.pres_2008_john_quincy_adams_unc},
            {"Andrew Jackson",         R.drawable.pres_2008_andrew_jackson_unc},
            {"Martin Van Buren",       R.drawable.pres_2008_martin_van_buren_unc},
            {"William Henry Harrison", R.drawable.pres_2009_william_henry_harrison_unc},
            {"John Tyler",             R.drawable.pres_2009_john_tyler_unc},
            {"James K. Polk",          R.drawable.pres_2009_james_k_polk_unc},
            {"Zachary Taylor",         R.drawable.pres_2009_zachary_taylor_unc},
            {"Millard Fillmore",       R.drawable.pres_2010_millard_fillmore_unc},
            {"Franklin Pierce",        R.drawable.pres_2010_franklin_pierce_unc},
            {"James Buchanan",         R.drawable.pres_2010_james_buchanan_unc},
            {"Abraham Lincoln",        R.drawable.pres_2010_abraham_lincoln_unc},
            {"Andrew Johnson",         R.drawable.pres_2011_andrew_johnson_unc},
            {"Ulysses S. Grant",       R.drawable.pres_2011_ulysses_s_grant_unc},
            {"Rutherford B. Hayes",    R.drawable.pres_2011_rutherford_b_hayes_unc},
            {"James Garfield",         R.drawable.pres_2011_james_garfield_unc},
            {"Chester Arthur",         R.drawable.pres_2012_chester_arthur_unc},
            {"Grover Cleveland 1",     R.drawable.pres_2012_grover_cleveland_1_unc},
            {"Benjamin Harrison",      R.drawable.pres_2012_benjamin_harrison_unc},
            {"Grover Cleveland 2",     R.drawable.pres_2012_grover_cleveland_2_unc},
            {"William McKinley",       R.drawable.pres_2013_william_mckinley_unc},
            {"Theodore Roosevelt",     R.drawable.pres_2013_theodore_roosevelt_unc},
            {"William Howard Taft",    R.drawable.pres_2013_william_taft_unc},
            {"Woodrow Wilson",         R.drawable.pres_2013_woodrow_wilson_unc},
            {"Warren G. Harding",      R.drawable.pres_2014_warren_g_harding_unc},
            {"Calvin Coolidge",        R.drawable.pres_2014_calvin_coolidge_unc},
            {"Herbert Hoover",         R.drawable.pres_2014_herbert_hoover_unc},
            {"Franklin D. Roosevelt",  R.drawable.pres_2014_franklin_d_roosevelt_unc},
            {"Harry Truman",           R.drawable.pres_2015_harry_s_truman_unc},
            {"Dwight D. Eisenhower",   R.drawable.pres_2015_dwight_d_eisenhower_unc},
            {"John F. Kennedy",        R.drawable.pres_2015_john_f_kennedy_unc},
            {"Lyndon B. Johnson",      R.drawable.pres_2015_lyndon_b_johnson_unc},
            {"Richard M. Nixon",       R.drawable.pres_2016_richard_m_nixon_unc},
            {"Gerald R. Ford",         R.drawable.pres_2016_gerald_r_ford_unc},
            {"Ronald Reagan",          R.drawable.pres_2016_ronald_reagan_unc},
            {"George H.W. Bush",       R.drawable.pres_2020_george_hw_bush_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS){
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.presidential_coin_obverse;

    //https://www.usmint.gov/mint_programs/%241coin/index1ea7.html?action=presDesignUse
    private static final int ATTRIBUTION = R.string.attr_presidential_dollars;

    @Override
    public String getCoinType() { return COLLECTION_TYPE; }

    @Override
    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot){
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        int coinIndex = 0;

        for (Object[] coinData : COIN_IDENTIFIERS){
            String identifier = (String) coinData[0];

            if (showMintMarks) {
                if (showP) {
                    coinList.add(new CoinSlot(identifier, "P", coinIndex++));
                }
                if (showD) {
                    coinList.add(new CoinSlot(identifier, "D", coinIndex++));
                }
            } else {
                coinList.add(new CoinSlot(identifier, "", coinIndex++));
            }
        }
    }

    @Override
    public int getAttributionResId(){
        return ATTRIBUTION;
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
        int total = 0;

        if(oldVersion <= 2) {
            // Add in 2012 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Chester Arthur");
            newCoinIdentifiers.add("Grover Cleveland 1");
            newCoinIdentifiers.add("Benjamin Harrison");
            newCoinIdentifiers.add("Grover Cleveland 2");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if(oldVersion <= 3) {
            // Add in 2013 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("William McKinley");
            newCoinIdentifiers.add("Theodore Roosevelt");
            newCoinIdentifiers.add("William Howard Taft");
            newCoinIdentifiers.add("Woodrow Wilson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 4) {
            // Add in 2014 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Warren G. Harding");
            newCoinIdentifiers.add("Calvin Coolidge");
            newCoinIdentifiers.add("Herbert Hoover");
            newCoinIdentifiers.add("Franklin D. Roosevelt");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 6) {
            // Add in 2015 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Harry Truman");
            newCoinIdentifiers.add("Dwight D. Eisenhower");
            newCoinIdentifiers.add("John F. Kennedy");
            newCoinIdentifiers.add("Lyndon B. Johnson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 7) {
            // Add in 2016 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Richard M. Nixon");
            newCoinIdentifiers.add("Gerald R. Ford");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 8) {
            // Add in missing 2016 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Ronald Reagan");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 16) {
            // Add in missing 2020 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("George H.W. Bush");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        return total;
    }
}
