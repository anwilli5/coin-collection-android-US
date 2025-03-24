
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

public class AmericanWomenQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "American Women Quarters";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Maya Angelou 2022", R.drawable.women_2022_maya_angelou_unc},
            {"Dr. Sally Ride 2022", R.drawable.women_2022_sally_ride_unc},
            {"Wilma Mankiller 2022", R.drawable.women_2022_wilma_mankiller_unc},
            {"Nina Otero-Warren 2022", R.drawable.women_2022_nina_otero_warren_unc},
            {"Anna May Wong 2022", R.drawable.women_2022_anna_may_wong_unc},
            {"Bessie Coleman 2023", R.drawable.women_2023_bessie_coleman_unc},
            {"Edith Kanaka'ole 2023", R.drawable.women_2023_edith_kanakaole_unc},
            {"Eleanor Roosevelt 2023", R.drawable.women_2023_eleanor_roosevelt_unc},
            {"Jovita Idar 2023", R.drawable.women_2023_jovita_idar_unc},
            {"Maria Tallchief 2023", R.drawable.women_2023_maria_tallchief_unc},

    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.women_2022_maya_angelou_unc;

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

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_s_proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_silver_proofs);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSilver = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        int coinIndex = 0;

        for (Object[] parksImageIdentifier : COIN_IDENTIFIERS) {
            String identifier = (String) parksImageIdentifier[0];
            if (showP) {coinList.add(new CoinSlot(identifier, "P", coinIndex++));}
            if (showD) {coinList.add(new CoinSlot(identifier, "D", coinIndex++));}
            if (showS) {coinList.add(new CoinSlot(identifier, "S", coinIndex++));}
            if (showProof) {coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));}
            if (showSilver) {coinList.add(new CoinSlot(identifier, "S Silver Proof", coinIndex++));}
        }
    }

    @Override
    public int getAttributionResId() {
        return R.string.attr_mint;
    }

    @Override
    public int getStartYear() {return 0;}

    @Override
    public int getStopYear() {
        return 0;
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {

        int total = 0;

        if (oldVersion <= 19) {
            // Add in new 2023 coins if applicable
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Bessie Coleman 2023");
            newCoinIdentifiers.add("Edith Kanaka'ole 2023");
            newCoinIdentifiers.add("Eleanor Roosevelt 2023");
            newCoinIdentifiers.add("Jovita Idar 2023");
            newCoinIdentifiers.add("Maria Tallchief 2023");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }
        if (oldVersion <= 20 ){
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Rev. Dr. Pauli Muarray 2024");
            newCoinIdentifiers.add("Patsy Takemoto 2024");
            newCoinIdentifiers.add("Dr. Mary Edwards Walker 2024");
            newCoinIdentifiers.add("Celia Cruz 2024");
            newCoinIdentifiers.add("Zitkala Sa 2024");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        return total;
    }
}
