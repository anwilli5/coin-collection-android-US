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
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;
import com.coincollection.CollectionInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class StateQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "State Quarters";

    private static final Object[][] STATES_COIN_IDENTIFIERS = {
            {"Delaware",       R.drawable.states_1999_delaware_unc,      R.drawable.states_1999_delaware_unc_25},
            {"Pennsylvania",   R.drawable.states_1999_pennsylvania_unc,  R.drawable.states_1999_pennsylvania_unc_25},
            {"New Jersey",     R.drawable.states_1999_new_jersey_unc,    R.drawable.states_1999_new_jersey_unc_25},
            {"Georgia",        R.drawable.states_1999_georgia_unc,       R.drawable.states_1999_georgia_unc_25},
            {"Connecticut",    R.drawable.states_1999_connecticut_unc,   R.drawable.states_1999_connecticut_unc_25},
            {"Massachusetts",  R.drawable.states_2000_massachusetts,     R.drawable.states_2000_massachusetts_25},
            {"Maryland",       R.drawable.states_2000_maryland_unc,      R.drawable.states_2000_maryland_unc_25},
            {"South Carolina", R.drawable.states_2000_south_carolina_unc,R.drawable.states_2000_south_carolina_unc_25},
            {"New Hampshire",  R.drawable.states_2000_new_hampshire_unc, R.drawable.states_2000_new_hampshire_unc_25},
            {"Virginia",       R.drawable.states_2000_virginia_unc,      R.drawable.states_2000_virginia_unc_25},
            {"New York",       R.drawable.states_2001_new_york_unc,      R.drawable.states_2001_new_york_unc_25},
            {"North Carolina", R.drawable.states_2001_north_carolina_unc,R.drawable.states_2001_north_carolina_unc_25},
            {"Rhode Island",   R.drawable.states_2001_rhode_island_unc,  R.drawable.states_2001_rhode_island_unc_25},
            {"Vermont",        R.drawable.states_2001_vermont_unc,       R.drawable.states_2001_vermont_unc_25},
            {"Kentucky",       R.drawable.states_2001_kentucky_unc,      R.drawable.states_2001_kentucky_unc_25},
            {"Tennessee",      R.drawable.states_2002_tennessee_unc,     R.drawable.states_2002_tennessee_unc_25},
            {"Ohio",           R.drawable.states_2002_ohio_unc,          R.drawable.states_2002_ohio_unc_25},
            {"Louisiana",      R.drawable.states_2002_louisiana_unc,     R.drawable.states_2002_louisiana_unc_25},
            {"Indiana",        R.drawable.states_2002_indiana_unc,       R.drawable.states_2002_indiana_unc_25},
            {"Mississippi",    R.drawable.states_2002_mississippi_unc,   R.drawable.states_2002_mississippi_unc_25},
            {"Illinois",       R.drawable.states_2003_illinois_unc,      R.drawable.states_2003_illinois_unc_25},
            {"Alabama",        R.drawable.states_2003_alabama_unc,       R.drawable.states_2003_alabama_unc_25},
            {"Maine",          R.drawable.states_2003_maine_unc,         R.drawable.states_2003_maine_unc_25},
            {"Missouri",       R.drawable.states_2003_missouri_unc,      R.drawable.states_2003_missouri_unc_25},
            {"Arkansas",       R.drawable.states_2003_arkansas_unc,      R.drawable.states_2003_arkansas_unc_25},
            {"Michigan",       R.drawable.states_2004_michigan_unc,      R.drawable.states_2004_michigan_unc_25},
            {"Florida",        R.drawable.states_2004_florida_unc,       R.drawable.states_2004_florida_unc_25},
            {"Texas",          R.drawable.states_2004_texas_unc,         R.drawable.states_2004_texas_unc_25},
            {"Iowa",           R.drawable.states_2004_iowa_unc,          R.drawable.states_2004_iowa_unc_25},
            {"Wisconsin",      R.drawable.states_2004_wisconsin_unc,     R.drawable.states_2004_wisconsin_unc_25},
            {"California",     R.drawable.states_2005_california_unc,    R.drawable.states_2005_california_unc_25},
            {"Minnesota",      R.drawable.states_2005_minnesota_unc,     R.drawable.states_2005_minnesota_unc_25},
            {"Oregon",         R.drawable.states_2005_oregon_unc,        R.drawable.states_2005_oregon_unc_25},
            {"Kansas",         R.drawable.states_2005_kansas_unc,        R.drawable.states_2005_kansas_unc_25},
            {"West Virginia",  R.drawable.states_2005_west_virginia_unc, R.drawable.states_2005_west_virginia_unc_25},
            {"Nevada",         R.drawable.states_2006_nevada_unc,        R.drawable.states_2006_nevada_unc_25},
            {"Nebraska",       R.drawable.states_2006_nebraska_unc,      R.drawable.states_2006_nebraska_unc_25},
            {"Colorado",       R.drawable.states_2006_colorado_unc,      R.drawable.states_2006_colorado_unc_25},
            {"North Dakota",   R.drawable.states_2006_north_dakota_unc,  R.drawable.states_2006_north_dakota_unc_25},
            {"South Dakota",   R.drawable.states_2006_south_dakota_unc,  R.drawable.states_2006_south_dakota_unc_25},
            {"Montana",        R.drawable.states_2007_montana_unc,       R.drawable.states_2007_montana_unc_25},
            {"Washington",     R.drawable.states_2007_washington_unc,    R.drawable.states_2007_washington_unc_25},
            {"Idaho",          R.drawable.states_2007_idaho_unc,         R.drawable.states_2007_idaho_unc_25},
            {"Wyoming",        R.drawable.states_2007_wyoming_unc,       R.drawable.states_2007_wyoming_unc_25},
            {"Utah",           R.drawable.states_2007_utah_unc,          R.drawable.states_2007_utah_unc_25},
            {"Oklahoma",       R.drawable.states_2008_oklahoma_unc,      R.drawable.states_2008_oklahoma_unc_25},
            {"New Mexico",     R.drawable.states_2008_new_mexico_unc,    R.drawable.states_2008_new_mexico_unc_25},
            {"Arizona",        R.drawable.states_2008_arizona_unc,       R.drawable.states_2008_arizona_unc_25},
            {"Alaska",         R.drawable.states_2008_alaska_unc,        R.drawable.states_2008_alaska_unc_25},
            {"Hawaii",         R.drawable.states_2008_hawaii_unc,        R.drawable.states_2008_hawaii_unc_25},
    };

    public static final Object[][] DC_AND_TERR_COIN_IDENTIFIERS = {
            {"District of Columbia",     R.drawable.states_2009_dc_unc,                R.drawable.states_2009_dc_unc_25},
            {"Puerto Rico",              R.drawable.states_2009_puerto_rico_unc,       R.drawable.states_2009_puerto_rico_unc_25},
            {"Guam",                     R.drawable.states_2009_guam_unc,              R.drawable.states_2009_guam_unc_25},
            {"American Samoa",           R.drawable.states_2009_american_samoa_unc,    R.drawable.states_2009_american_samoa_unc_25},
            {"U.S. Virgin Islands",      R.drawable.states_2009_virgin_islands_unc,    R.drawable.states_2009_virgin_islands_unc_25},
            {"Northern Mariana Islands", R.drawable.states_2009_northern_mariana_unc,  R.drawable.states_2009_northern_mariana_unc_25},
    };

    private static final HashMap<String, Integer[]> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : STATES_COIN_IDENTIFIERS){
            COIN_MAP.put((String) coinData[0],
                    new Integer[]{(Integer) coinData[1], (Integer) coinData[2]});
        }
        for (Object[] coinData : DC_AND_TERR_COIN_IDENTIFIERS){
            COIN_MAP.put((String) coinData[0],
                    new Integer[]{(Integer) coinData[1], (Integer) coinData[2]});
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.states_2001_north_carolina_unc;

    // https://www.usmint.gov/consumer/index091c.html?action=designPolicy
    private static final int ATTRIBUTION = R.string.attr_state_quarters;

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
            return inCollection ? (int) STATES_COIN_IDENTIFIERS[0][1] : (int) STATES_COIN_IDENTIFIERS[0][2];
        }
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

        // Use one of the customizable checkboxes for the 'Show Territories' options
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.show_territories);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showTerritories = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        int coinIndex = 0;

        for(Object[] coinData : STATES_COIN_IDENTIFIERS){
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
        if(showTerritories){
            // Add those to the list
            for(Object[] coinData : DC_AND_TERR_COIN_IDENTIFIERS){
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
        return 0;
    }
}
