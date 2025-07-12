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
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class StateQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "State Quarters";

    private static final Object[][] STATES_COIN_IDENTIFIERS = {
            {"Delaware", R.drawable.states_1999_delaware_unc},
            {"Pennsylvania", R.drawable.states_1999_pennsylvania_unc},
            {"New Jersey", R.drawable.states_1999_new_jersey_unc},
            {"Georgia", R.drawable.states_1999_georgia_unc},
            {"Connecticut", R.drawable.states_1999_connecticut_unc},
            {"Massachusetts", R.drawable.states_2000_massachusetts},
            {"Maryland", R.drawable.states_2000_maryland_unc},
            {"South Carolina", R.drawable.states_2000_south_carolina_unc},
            {"New Hampshire", R.drawable.states_2000_new_hampshire_unc},
            {"Virginia", R.drawable.states_2000_virginia_unc},
            {"New York", R.drawable.states_2001_new_york_unc},
            {"North Carolina", R.drawable.states_2001_north_carolina_unc},
            {"Rhode Island", R.drawable.states_2001_rhode_island_unc},
            {"Vermont", R.drawable.states_2001_vermont_unc},
            {"Kentucky", R.drawable.states_2001_kentucky_unc},
            {"Tennessee", R.drawable.states_2002_tennessee_unc},
            {"Ohio", R.drawable.states_2002_ohio_unc},
            {"Louisiana", R.drawable.states_2002_louisiana_unc},
            {"Indiana", R.drawable.states_2002_indiana_unc},
            {"Mississippi", R.drawable.states_2002_mississippi_unc},
            {"Illinois", R.drawable.states_2003_illinois_unc},
            {"Alabama", R.drawable.states_2003_alabama_unc},
            {"Maine", R.drawable.states_2003_maine_unc},
            {"Missouri", R.drawable.states_2003_missouri_unc},
            {"Arkansas", R.drawable.states_2003_arkansas_unc},
            {"Michigan", R.drawable.states_2004_michigan_unc},
            {"Florida", R.drawable.states_2004_florida_unc},
            {"Texas", R.drawable.states_2004_texas_unc},
            {"Iowa", R.drawable.states_2004_iowa_unc},
            {"Wisconsin", R.drawable.states_2004_wisconsin_unc},
            {"California", R.drawable.states_2005_california_unc},
            {"Minnesota", R.drawable.states_2005_minnesota_unc},
            {"Oregon", R.drawable.states_2005_oregon_unc},
            {"Kansas", R.drawable.states_2005_kansas_unc},
            {"West Virginia", R.drawable.states_2005_west_virginia_unc},
            {"Nevada", R.drawable.states_2006_nevada_unc},
            {"Nebraska", R.drawable.states_2006_nebraska_unc},
            {"Colorado", R.drawable.states_2006_colorado_unc},
            {"North Dakota", R.drawable.states_2006_north_dakota_unc},
            {"South Dakota", R.drawable.states_2006_south_dakota_unc},
            {"Montana", R.drawable.states_2007_montana_unc},
            {"Washington", R.drawable.states_2007_washington_unc},
            {"Idaho", R.drawable.states_2007_idaho_unc},
            {"Wyoming", R.drawable.states_2007_wyoming_unc},
            {"Utah", R.drawable.states_2007_utah_unc},
            {"Oklahoma", R.drawable.states_2008_oklahoma_unc},
            {"New Mexico", R.drawable.states_2008_new_mexico_unc},
            {"Arizona", R.drawable.states_2008_arizona_unc},
            {"Alaska", R.drawable.states_2008_alaska_unc},
            {"Hawaii", R.drawable.states_2008_hawaii_unc},
    };

    public static final Object[][] DC_AND_TERR_COIN_IDENTIFIERS = {
            {"District of Columbia", R.drawable.states_2009_dc_unc},
            {"Puerto Rico", R.drawable.states_2009_puerto_rico_unc},
            {"Guam", R.drawable.states_2009_guam_unc},
            {"American Samoa", R.drawable.states_2009_american_samoa_unc},
            {"U.S. Virgin Islands", R.drawable.states_2009_virgin_islands_unc},
            {"Northern Mariana Islands", R.drawable.states_2009_northern_mariana_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : STATES_COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
        for (Object[] coinData : DC_AND_TERR_COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.states_2001_north_carolina_unc;

    // https://www.usmint.gov/consumer/index091c.html?action=designPolicy
    private static final int ATTRIBUTION = R.string.attr_state_quarters;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) STATES_COIN_IDENTIFIERS[0][1];
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

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        boolean showMintMarks = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARKS);
        boolean showP = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        boolean showD = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        boolean showTerritories = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_1);
        int coinIndex = 0;

        for (Object[] coinData : STATES_COIN_IDENTIFIERS) {
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
        if (showTerritories) {
            // Add those to the list
            for (Object[] coinData : DC_AND_TERR_COIN_IDENTIFIERS) {
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
    public int getAttributionResId() {
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
