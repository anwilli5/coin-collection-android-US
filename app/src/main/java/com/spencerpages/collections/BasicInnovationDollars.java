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

public class BasicInnovationDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "American Innovation Dollars";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Introductory", R.drawable.innovation_2018_introductory_unc},
            {"Delaware", R.drawable.innovation_2019_delaware_unc},
            {"Pennsylvania", R.drawable.innovation_2019_pennsylvania_unc},
            {"New Jersey", R.drawable.innovation_2019_new_jersey_unc},
            {"Georgia", R.drawable.innovation_2019_georgia_unc},
            {"Connecticut", R.drawable.innovation_2020_connecticut_unc},
            {"Massachusetts", R.drawable.innovation_2020_massachusetts_unc},
            {"Maryland", R.drawable.innovation_2020_maryland_unc},
            {"South Carolina", R.drawable.innovation_2020_south_carolina_unc},
            {"New Hampshire", R.drawable.innovation_2021_new_hampshire_unc},
            {"Virginia", R.drawable.innovation_2021_virginia_unc},
            {"New York", R.drawable.innovation_2021_new_york_unc},
            {"North Carolina", R.drawable.innovation_2021_north_carolina_unc},
            {"Rhode Island", R.drawable.innovation_2022_rhode_island_unc},
            {"Vermont", R.drawable.innovation_2022_vermont_unc},
            {"Kentucky", R.drawable.innovation_2022_kentucky_unc},
            {"Tennessee", R.drawable.innovation_2022_tennessee_unc},
            {"Ohio", R.drawable.innovation_2023_ohio_unc},
            {"Louisiana", R.drawable.innovation_2023_louisiana_unc},
            {"Indiana", R.drawable.innovation_2023_indiana_unc},
            {"Mississippi", R.drawable.innovation_2023_mississippi_unc},
            {"Illinois", R.drawable.innovation_2024_illinois_unc},
            {"Alabama", R.drawable.innovation_2024_alabama_unc},
            {"Maine", R.drawable.innovation_2024_maine_unc},
            {"Missouri", R.drawable.innovation_2024_missouri_unc},
            {"Arkansas", R.drawable.innovation_2025_arkansas_unc},
            {"Michigan", R.drawable.innovation_2025_michigan_unc},
            {"Florida", R.drawable.innovation_2025_florida_unc},
            {"Texas", R.drawable.innovation_2025_texas_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.innovation_2018_introductory_unc;

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

        Boolean showMintMarks = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        int coinIndex = 0;

        for (Object[] coinData : COIN_IDENTIFIERS) {
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

        int total = 0;

        if (oldVersion <= 13) {
            // Add in new 2019 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Delaware");
            newCoinIdentifiers.add("Pennsylvania");
            newCoinIdentifiers.add("New Jersey");
            newCoinIdentifiers.add("Georgia");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 16) {
            // Add in new 2020 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Connecticut");
            newCoinIdentifiers.add("Massachusetts");
            newCoinIdentifiers.add("Maryland");
            newCoinIdentifiers.add("South Carolina");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 18) {
            // Add in new 2021 and 2022 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("New Hampshire");
            newCoinIdentifiers.add("Virginia");
            newCoinIdentifiers.add("New York");
            newCoinIdentifiers.add("North Carolina");
            newCoinIdentifiers.add("Rhode Island");
            newCoinIdentifiers.add("Vermont");
            newCoinIdentifiers.add("Kentucky");
            newCoinIdentifiers.add("Tennessee");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 19) {
            // Add in new 2023 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Ohio");
            newCoinIdentifiers.add("Louisiana");
            newCoinIdentifiers.add("Indiana");
            newCoinIdentifiers.add("Mississippi");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 20) {
            // Add in new 2024 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Illinois");
            newCoinIdentifiers.add("Alabama");
            newCoinIdentifiers.add("Maine");
            newCoinIdentifiers.add("Missouri");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 22) {
            // Add in new 2025 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Arkansas");
            newCoinIdentifiers.add("Michigan");
            newCoinIdentifiers.add("Florida");
            newCoinIdentifiers.add("Texas");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        return total;
    }
}
