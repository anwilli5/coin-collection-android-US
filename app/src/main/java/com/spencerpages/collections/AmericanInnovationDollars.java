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
import com.coincollection.CollectionInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AmericanInnovationDollars extends CollectionInfo {

    private static final String COLLECTION_TYPE = "American Innovation Dollars";

    private static final Object[][] COIN_IDENTIFIERS = {
            { "Introductory", R.drawable.innovation_2018_introductory_unc, R.drawable.innovation_2018_introductory_unc_25},
            { "Delaware",     R.drawable.innovation_2019_delaware_unc, R.drawable.innovation_2019_delaware_unc_25},
            { "Pennsylvania", R.drawable.innovation_2019_pennsylvania_unc, R.drawable.innovation_2019_pennsylvania_unc_25},
            { "New Jersey",   R.drawable.innovation_2019_new_jersey_unc, R.drawable.innovation_2019_new_jersey_unc_25},
            { "Georgia",      R.drawable.innovation_2019_georgia_unc, R.drawable.innovation_2019_georgia_unc_25},
            /*
            { "Connecticut",    R.drawable.innovation_2020_connecticut_unc, R.drawable.innovation_2020_connecticut_unc_25},
            { "Massachusetts",  R.drawable.innovation_2020_massachusetts_unc, R.drawable.innovation_2020_massachusetts_unc_25},
            { "Maryland",       R.drawable.innovation_2020_maryland_unc, R.drawable.innovation_2020_maryland_unc_25},
            { "South Carolina", R.drawable.innovation_2020_south_carolina_unc, R.drawable.innovation_2020_south_carolina_unc_25}
             */
    };

    private static final HashMap<String, Integer[]> COIN_INFO = new HashMap<>();

    static {
        // Populate the COIN_INFO HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS){
            COIN_INFO.put((String) coinData[0],
                    new Integer[]{(Integer) coinData[1], (Integer) coinData[2]});
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.innovation_2018_introductory_unc;

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(String identifier, String mint, Boolean inCollection){
        return COIN_INFO.get(identifier)[inCollection ? 0 : 1];
    }

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
    public void populateCollectionLists(HashMap<String, Object> parameters,
                                        ArrayList<String> identifierList,
                                        ArrayList<String> mintList) {

        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);

        for(Object[] coinData : COIN_IDENTIFIERS){

            String identifier = (String) coinData[0];

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
        return MainApplication.DEFAULT_ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
                                           int oldVersion, int newVersion) {

        int total = 0;

        if (oldVersion <= 13) {
            // Add in new 2019 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Delaware");
            newCoinIdentifiers.add("Pennsylvania");
            newCoinIdentifiers.add("New Jersey");
            newCoinIdentifiers.add("Georgia");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, tableName, newCoinIdentifiers);
        }

        return total;
    }
}
