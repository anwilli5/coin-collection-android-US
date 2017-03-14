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

import com.spencerpages.core.CoinPageCreator;
import com.spencerpages.core.CollectionInfo;
import com.spencerpages.core.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PresidentialDollars extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Presidential Dollars";

    //TODO Need to update to Harry S. Truman - requires db update too
    private static final String[] PRES_COIN_IDENTIFIERS = {
            "George Washington",
            "John Adams",
            "Thomas Jefferson",
            "James Madison",
            "James Monroe",
            "John Quincy Adams",
            "Andrew Jackson",
            "Martin Van Buren",
            "William Henry Harrison",
            "John Tyler",
            "James K. Polk",
            "Zachary Taylor",
            "Millard Fillmore",
            "Franklin Pierce",
            "James Buchanan",
            "Abraham Lincoln",
            "Andrew Johnson",
            "Ulysses S. Grant",
            "Rutherford B. Hayes",
            "James Garfield",
            "Chester Arthur",
            "Grover Cleveland 1",
            "Benjamin Harrison",
            "Grover Cleveland 2",
            "William McKinley",
            "Theodore Roosevelt",
            "William Howard Taft",
            "Woodrow Wilson",
            "Warren G. Harding",
            "Calvin Coolidge",
            "Herbert Hoover",
            "Franklin D. Roosevelt",
            "Harry Truman",
            "Dwight D. Eisenhower",
            "John F. Kennedy",
            "Lyndon B. Johnson",
            "Richard M. Nixon",
            "Gerald R. Ford",
            "Ronald Reagan",
    };

    private static final Integer[][] PRES_IMAGE_IDENTIFIERS = {
            { R.drawable.pres_2007_george_washington_unc,       R.drawable.pres_2007_george_washington_unc_25},
            { R.drawable.pres_2007_john_adam_unc,               R.drawable.pres_2007_john_adam_unc_25},
            { R.drawable.pres_2007_thomas_jefferson_unc,        R.drawable.pres_2007_thomas_jefferson_unc_25},
            { R.drawable.pres_2007_james_madison_unc,           R.drawable.pres_2007_james_madison_unc_25},
            { R.drawable.pres_2008_james_monroe_unc,            R.drawable.pres_2008_james_monroe_unc_25},
            { R.drawable.pres_2008_john_quincy_adams_unc,       R.drawable.pres_2008_john_quincy_adams_unc_25},
            { R.drawable.pres_2008_andrew_jackson_unc,          R.drawable.pres_2008_andrew_jackson_unc_25},
            { R.drawable.pres_2008_martin_van_buren_unc,        R.drawable.pres_2008_martin_van_buren_unc_25},
            { R.drawable.pres_2009_william_henry_harrison_unc,  R.drawable.pres_2009_william_henry_harrison_unc_25},
            { R.drawable.pres_2009_john_tyler_unc,              R.drawable.pres_2009_john_tyler_unc_25},
            { R.drawable.pres_2009_james_k_polk_unc,            R.drawable.pres_2009_james_k_polk_unc_25},
            { R.drawable.pres_2009_zachary_taylor_unc,          R.drawable.pres_2009_zachary_taylor_unc_25},
            { R.drawable.pres_2010_millard_fillmore_unc,        R.drawable.pres_2010_millard_fillmore_unc_25},
            { R.drawable.pres_2010_franklin_pierce_unc,         R.drawable.pres_2010_franklin_pierce_unc_25},
            { R.drawable.pres_2010_james_buchanan_unc,          R.drawable.pres_2010_james_buchanan_unc_25},
            { R.drawable.pres_2010_abraham_lincoln_unc,         R.drawable.pres_2010_abraham_lincoln_unc_25},
            { R.drawable.pres_2011_andrew_johnson_unc,          R.drawable.pres_2011_andrew_johnson_unc_25},
            { R.drawable.pres_2011_ulysses_s_grant_unc,         R.drawable.pres_2011_ulysses_s_grant_unc_25},
            { R.drawable.pres_2011_rutherford_b_hayes_unc,      R.drawable.pres_2011_rutherford_b_hayes_unc_25},
            { R.drawable.pres_2011_james_garfield_unc,          R.drawable.pres_2011_james_garfield_unc_25},
            { R.drawable.pres_2012_chester_arthur_unc,          R.drawable.pres_2012_chester_arthur_unc_25},
            { R.drawable.pres_2012_grover_cleveland_1_unc,      R.drawable.pres_2012_grover_cleveland_1_unc_25},
            { R.drawable.pres_2012_benjamin_harrison_unc,       R.drawable.pres_2012_benjamin_harrison_unc_25},
            { R.drawable.pres_2012_grover_cleveland_2_unc,      R.drawable.pres_2012_grover_cleveland_2_unc_25},
            { R.drawable.pres_2013_william_mckinley_unc,        R.drawable.pres_2013_william_mckinley_unc_25},
            { R.drawable.pres_2013_theodore_roosevelt_unc,      R.drawable.pres_2013_theodore_roosevelt_unc_25},
            { R.drawable.pres_2013_william_taft_unc,            R.drawable.pres_2013_william_taft_unc_25},
            { R.drawable.pres_2013_woodrow_wilson_unc,          R.drawable.pres_2013_woodrow_wilson_unc_25},
            { R.drawable.pres_2014_warren_g_harding_unc,        R.drawable.pres_2014_warren_g_harding_unc_25},
            { R.drawable.pres_2014_calvin_coolidge_unc,         R.drawable.pres_2014_calvin_coolidge_unc_25},
            { R.drawable.pres_2014_herbert_hoover_unc,          R.drawable.pres_2014_herbert_hoover_unc_25},
            { R.drawable.pres_2014_franklin_d_roosevelt_unc,    R.drawable.pres_2014_franklin_d_roosevelt_unc_25},
            { R.drawable.pres_2015_harry_s_truman_unc,          R.drawable.pres_2015_harry_s_truman_unc_25},
            { R.drawable.pres_2015_dwight_d_eisenhower_unc,     R.drawable.pres_2015_dwight_d_eisenhower_unc_25},
            { R.drawable.pres_2015_john_f_kennedy_unc,          R.drawable.pres_2015_john_f_kennedy_unc_25},
            { R.drawable.pres_2015_lyndon_b_johnson_unc,        R.drawable.pres_2015_lyndon_b_johnson_unc_25},
            { R.drawable.pres_2016_richard_m_nixon_unc,         R.drawable.pres_2016_richard_m_nixon_unc_25},
            { R.drawable.pres_2016_gerald_r_ford_unc,           R.drawable.pres_2016_gerald_r_ford_unc_25},
            { R.drawable.pres_2016_ronald_reagan_unc,           R.drawable.pres_2016_ronald_reagan_unc_25},
    };

    private static final HashMap<String, Integer[]> PRES_INFO = new HashMap<>();

    static {
        // Populate the PRES_INFO HashMap for quick image ID lookups later
        for (int i = 0; i < PRES_COIN_IDENTIFIERS.length; i++){
            PRES_INFO.put(PRES_COIN_IDENTIFIERS[i], PRES_IMAGE_IDENTIFIERS[i]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.presidential_coin_obverse;

    //https://www.usmint.gov/mint_programs/%241coin/index1ea7.html?action=presDesignUse
    private static final String ATTRIBUTION = "Presidential $1 Coin images from the United States Mint.";

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(String identifier, String mint, Boolean inCollection){
        return PRES_INFO.get(identifier)[inCollection ? 0 : 1];
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_P, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_D, Boolean.FALSE);
    }

    // TODO Perform validation and throw exception
    public void populateCollectionLists(HashMap<String, Object> parameters,
                                        ArrayList<String> identifierList,
                                        ArrayList<String> mintList) {

        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_P);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_D);

        for(int i = 0; i < PRES_COIN_IDENTIFIERS.length; i++){

            String identifier = PRES_COIN_IDENTIFIERS[i];

            if(showMintMarks){
                if(showP){
                    identifierList.add(identifier);
                    mintList.add("P");
                }
                if(showD){
                    identifierList.add(identifier);
                    mintList.add("D");
                }
            } else {
                identifierList.add(identifier);
                mintList.add("");
            }
        }
    }

    public String getAttributionString(){
        return ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
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
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if(oldVersion <= 3) {
            // Add in 2013 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("William McKinley");
            newCoinIdentifiers.add("Theodore Roosevelt");
            newCoinIdentifiers.add("William Howard Taft");
            newCoinIdentifiers.add("Woodrow Wilson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 4) {
            // Add in 2014 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Warren G. Harding");
            newCoinIdentifiers.add("Calvin Coolidge");
            newCoinIdentifiers.add("Herbert Hoover");
            newCoinIdentifiers.add("Franklin D. Roosevelt");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 6) {
            // Add in 2015 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Harry Truman");
            newCoinIdentifiers.add("Dwight D. Eisenhower");
            newCoinIdentifiers.add("John F. Kennedy");
            newCoinIdentifiers.add("Lyndon B. Johnson");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 7) {
            // Add in 2016 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Richard M. Nixon");
            newCoinIdentifiers.add("Gerald R. Ford");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        if (oldVersion <= 8) {
            // Add in missing 2016 Presidential Dollars

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Ronald Reagan");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        return total;
    }
}
