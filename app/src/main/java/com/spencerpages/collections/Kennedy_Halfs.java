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

public class Kennedy_Halfs extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Kennedy Halfs";

    private static final Object[][] OLDCOINS_COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.ha1795o},
            {"Draped Bust", R.drawable.ha1796o},
            {"Capped Bust", R.drawable.ha1837o},
            {"Seated Liberty", R.drawable.ha1853o},
            {"Barber", R.drawable.obv_barber_half},
            {"Liberty Walking", R.drawable.obv_walking_liberty_half},
            {"Franklin", R.drawable.obv_franklin_half},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final Integer START_YEAR = 1964;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_kennedy_half_dollar_unc;

    private static final int REVERSE_IMAGE = R.drawable.rev_kennedy_half_dollar_unc;

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
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string .include_satin);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string .include_s_Proofs);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_clad);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_silver);
    }
    // TODO Perform validation and throw exception

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showsatin = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showproofs = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showold = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showclad = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showsilver = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);

        int coinIndex = 0;


        if (showold) {
            for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++));
            }
        }
        for (int i = startYear; i <= stopYear; i++) {

            String newValue = Integer.toString(i);

            if (showclad) {
                if (i == 1976) {
                    newValue = "1776-1976";
                    if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                    if (showproofs) {coinList.add(new CoinSlot(newValue, "S Proof", coinIndex++));}
                }
                if ( i > 1970 && i != 1975 && i != 1976) {
                    if (showP && i < 1980) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                    if (showP && i > 1979) {coinList.add(new CoinSlot(newValue, "P", coinIndex++));}
                    if (showsatin && i > 2004 && i < 2011) {coinList.add(new CoinSlot(newValue, "P Satin", coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                    if (showsatin && i > 2004 && i < 2011) {coinList.add(new CoinSlot(newValue, "D Satin", coinIndex++));}
                    if (showproofs) {coinList.add(new CoinSlot(newValue, "S Proof", coinIndex++));}
                }
            }
            if (showsilver) {
                if (i == 1964) {
                    if (showP) {coinList.add(new CoinSlot(newValue, "Silver", coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot(newValue, "D Silver", coinIndex++));}
                    if (showproofs) {coinList.add(new CoinSlot(newValue, "Silver Proof", coinIndex++));}
                }
                if (i == 1965 || i == 1966 || i == 1967) {
                    if (showP) {
                        coinList.add(new CoinSlot(newValue, "40% Silver", coinIndex++));
                        coinList.add(new CoinSlot(newValue, "SMS 40% Silver", coinIndex++));}
                }
                if ( i == 1968 || i == 1969 || i == 1970 ) {
                    if (showD) {coinList.add(new CoinSlot(newValue, "D 40% Silver", coinIndex++));}
                    if (showproofs) {coinList.add(new CoinSlot(newValue, "S Proof 40% Silver", coinIndex++));}
                }
                if (i == 1976) {
                    newValue = "1776-1976";
                    if (showproofs){
                        coinList.add(new CoinSlot(newValue, "S 40% Silver", coinIndex++));
                        coinList.add(new CoinSlot(newValue, "S 40% Silver Proof", coinIndex++));}
                }
                if ( showproofs && i > 1991){coinList.add(new CoinSlot(newValue, "S Silver Proof", coinIndex++));}
            }

        }
    }

    @Override
    public int getAttributionResId() {
        return R.string.attr_mint;
    }

    @Override
    public int getStartYear() {
        return START_YEAR;
    }

    @Override
    public int getStopYear() {
        return STOP_YEAR;
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {

        return 0;
    }
}

