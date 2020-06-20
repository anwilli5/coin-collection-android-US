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
import com.spencerpages.R;
import com.coincollection.CollectionInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class StateQuarters extends CollectionInfo {

    private static final String COLLECTION_TYPE = "State Quarters";

    private static final String[] STATES_COIN_IDENTIFIERS = {
            "Delaware",
            "Pennsylvania",
            "New Jersey",
            "Georgia",
            "Connecticut",
            "Massachusetts",
            "Maryland",
            "South Carolina",
            "New Hampshire",
            "Virginia",
            "New York",
            "North Carolina",
            "Rhode Island",
            "Vermont",
            "Kentucky",
            "Tennessee",
            "Ohio",
            "Louisiana",
            "Indiana",
            "Mississippi",
            "Illinois",
            "Alabama",
            "Maine",
            "Missouri",
            "Arkansas",
            "Michigan",
            "Florida",
            "Texas",
            "Iowa",
            "Wisconsin",
            "California",
            "Minnesota",
            "Oregon",
            "Kansas",
            "West Virginia",
            "Nevada",
            "Nebraska",
            "Colorado",
            "North Dakota",
            "South Dakota",
            "Montana",
            "Washington",
            "Idaho",
            "Wyoming",
            "Utah",
            "Oklahoma",
            "New Mexico",
            "Arizona",
            "Alaska",
            "Hawaii",
    };

    private static final Integer[][] STATES_IMAGE_IDENTIFIERS = {
            { R.drawable.states_1999_delaware_unc,      R.drawable.states_1999_delaware_unc_25},
            { R.drawable.states_1999_pennsylvania_unc,  R.drawable.states_1999_pennsylvania_unc_25},
            { R.drawable.states_1999_new_jersey_unc,    R.drawable.states_1999_new_jersey_unc_25},
            { R.drawable.states_1999_georgia_unc,       R.drawable.states_1999_georgia_unc_25},
            { R.drawable.states_1999_connecticut_unc,   R.drawable.states_1999_connecticut_unc_25},
            { R.drawable.states_2000_massachusetts,     R.drawable.states_2000_massachusetts_25},
            { R.drawable.states_2000_maryland_unc,      R.drawable.states_2000_maryland_unc_25},
            { R.drawable.states_2000_south_carolina_unc,R.drawable.states_2000_south_carolina_unc_25},
            { R.drawable.states_2000_new_hampshire_unc, R.drawable.states_2000_new_hampshire_unc_25},
            { R.drawable.states_2000_virginia_unc,      R.drawable.states_2000_virginia_unc_25},
            { R.drawable.states_2001_new_york_unc,      R.drawable.states_2001_new_york_unc_25},
            { R.drawable.states_2001_north_carolina_unc,R.drawable.states_2001_north_carolina_unc_25},
            { R.drawable.states_2001_rhode_island_unc,  R.drawable.states_2001_rhode_island_unc_25},
            { R.drawable.states_2001_vermont_unc,       R.drawable.states_2001_vermont_unc_25},
            { R.drawable.states_2001_kentucky_unc,      R.drawable.states_2001_kentucky_unc_25},
            { R.drawable.states_2002_tennessee_unc,     R.drawable.states_2002_tennessee_unc_25},
            { R.drawable.states_2002_ohio_unc,          R.drawable.states_2002_ohio_unc_25},
            { R.drawable.states_2002_louisiana_unc,     R.drawable.states_2002_louisiana_unc_25},
            { R.drawable.states_2002_indiana_unc,       R.drawable.states_2002_indiana_unc_25},
            { R.drawable.states_2002_mississippi_unc,   R.drawable.states_2002_mississippi_unc_25},
            { R.drawable.states_2003_illinois_unc,      R.drawable.states_2003_illinois_unc_25},
            { R.drawable.states_2003_alabama_unc,       R.drawable.states_2003_alabama_unc_25},
            { R.drawable.states_2003_maine_unc,         R.drawable.states_2003_maine_unc_25},
            { R.drawable.states_2003_missouri_unc,      R.drawable.states_2003_missouri_unc_25},
            { R.drawable.states_2003_arkansas_unc,      R.drawable.states_2003_arkansas_unc_25},
            { R.drawable.states_2004_michigan_unc,      R.drawable.states_2004_michigan_unc_25},
            { R.drawable.states_2004_florida_unc,       R.drawable.states_2004_florida_unc_25},
            { R.drawable.states_2004_texas_unc,         R.drawable.states_2004_texas_unc_25},
            { R.drawable.states_2004_iowa_unc,          R.drawable.states_2004_iowa_unc_25},
            { R.drawable.states_2004_wisconsin_unc,     R.drawable.states_2004_wisconsin_unc_25},
            { R.drawable.states_2005_california_unc,    R.drawable.states_2005_california_unc_25},
            { R.drawable.states_2005_minnesota_unc,     R.drawable.states_2005_minnesota_unc_25},
            { R.drawable.states_2005_oregon_unc,        R.drawable.states_2005_oregon_unc_25},
            { R.drawable.states_2005_kansas_unc,        R.drawable.states_2005_kansas_unc_25},
            { R.drawable.states_2005_west_virginia_unc, R.drawable.states_2005_west_virginia_unc_25},
            { R.drawable.states_2006_nevada_unc,        R.drawable.states_2006_nevada_unc_25},
            { R.drawable.states_2006_nebraska_unc,      R.drawable.states_2006_nebraska_unc_25},
            { R.drawable.states_2006_colorado_unc,      R.drawable.states_2006_colorado_unc_25},
            { R.drawable.states_2006_north_dakota_unc,  R.drawable.states_2006_north_dakota_unc_25},
            { R.drawable.states_2006_south_dakota_unc,  R.drawable.states_2006_south_dakota_unc_25},
            { R.drawable.states_2007_montana_unc,       R.drawable.states_2007_montana_unc_25},
            { R.drawable.states_2007_washington_unc,    R.drawable.states_2007_washington_unc_25},
            { R.drawable.states_2007_idaho_unc,         R.drawable.states_2007_idaho_unc_25},
            { R.drawable.states_2007_wyoming_unc,       R.drawable.states_2007_wyoming_unc_25},
            { R.drawable.states_2007_utah_unc,          R.drawable.states_2007_utah_unc_25},
            { R.drawable.states_2008_oklahoma_unc,      R.drawable.states_2008_oklahoma_unc_25},
            { R.drawable.states_2008_new_mexico_unc,    R.drawable.states_2008_new_mexico_unc_25},
            { R.drawable.states_2008_arizona_unc,       R.drawable.states_2008_arizona_unc_25},
            { R.drawable.states_2008_alaska_unc,        R.drawable.states_2008_alaska_unc_25},
            { R.drawable.states_2008_hawaii_unc,        R.drawable.states_2008_hawaii_unc_25},
    };

    private static final String[] DC_AND_TERR_COIN_IDENTIFIERS = {
            "District of Columbia",
            "Puerto Rico",
            "Guam",
            "American Samoa",
            "U.S. Virgin Islands",
            "Northern Mariana Islands",
    };

    private static final Integer[][] DC_AND_TERR_IMAGE_IDENTIFIERS = {
            { R.drawable.states_2009_dc_unc,                R.drawable.states_2009_dc_unc_25},
            { R.drawable.states_2009_puerto_rico_unc,       R.drawable.states_2009_puerto_rico_unc_25},
            { R.drawable.states_2009_guam_unc,              R.drawable.states_2009_guam_unc_25},
            { R.drawable.states_2009_american_samoa_unc,    R.drawable.states_2009_american_samoa_unc_25},
            { R.drawable.states_2009_virgin_islands_unc,    R.drawable.states_2009_virgin_islands_unc_25},
            { R.drawable.states_2009_northern_mariana_unc,  R.drawable.states_2009_northern_mariana_unc_25},
    };

    private static final HashMap<String, Integer[]> STATES_INFO = new HashMap<>();

    static {
        // Populate the STATES_INFO HashMap for quick image ID lookups later
        for (int i = 0; i < STATES_COIN_IDENTIFIERS.length; i++){
            STATES_INFO.put(STATES_COIN_IDENTIFIERS[i], STATES_IMAGE_IDENTIFIERS[i]);
        }
        for (int i = 0; i < DC_AND_TERR_COIN_IDENTIFIERS.length; i++){
            STATES_INFO.put(DC_AND_TERR_COIN_IDENTIFIERS[i], DC_AND_TERR_IMAGE_IDENTIFIERS[i]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.states_2001_north_carolina_unc;

    // https://www.usmint.gov/consumer/index091c.html?action=designPolicy
    private static final String ATTRIBUTION = "Quarter-dollar coin images from the United States Mint.";

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(CoinSlot coinSlot){
        Integer[] slotImages = STATES_INFO.get(coinSlot.getIdentifier());
        boolean inCollection = coinSlot.isInCollection();
        if(slotImages != null){
            return slotImages[inCollection ? 0 : 1];
        } else {
            return inCollection ? (int) STATES_IMAGE_IDENTIFIERS[0][0] : (int) STATES_IMAGE_IDENTIFIERS[0][1];
        }
    }

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
    @SuppressWarnings("ConstantConditions")
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showTerritories = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);

        for (String identifier : STATES_COIN_IDENTIFIERS) {

            if (showMintMarks) {
                if (showP) {
                    coinList.add(new CoinSlot(identifier, "P"));
                }
                if (showD) {
                    coinList.add(new CoinSlot(identifier, "D"));
                }
            } else {
                coinList.add(new CoinSlot(identifier, ""));
            }
        }
        if(showTerritories){
            // Add those to the list
            for (String identifier : DC_AND_TERR_COIN_IDENTIFIERS) {

                if (showMintMarks) {
                    if (showP) {
                        coinList.add(new CoinSlot(identifier, "P"));
                    }
                    if (showD) {
                        coinList.add(new CoinSlot(identifier, "D"));
                    }
                } else {
                    coinList.add(new CoinSlot(identifier, ""));
                }
            }
        }
    }

    public String getAttributionString(){
        return ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
                                           int oldVersion, int newVersion) {
        return 0;
    }
}
