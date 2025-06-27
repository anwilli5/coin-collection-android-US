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

public class RooseveltDimes extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Roosevelt Dimes";

    private static final String[] OLD_COINS_STRS = {
            "Draped Bust",
            "Capped Bust",
            "Seated",
            "Barber",
            "Mercury",
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Draped Bust", R.drawable.a1797drapeddime},
            {"Capped Bust", R.drawable.a1820cappeddime},
            {"Seated", R.drawable.astarsdime},
            {"Barber", R.drawable.obv_barber_dime},
            {"Mercury", R.drawable.obv_mercury_dime},
    };

    private static final Integer START_YEAR = 1946;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_roosevelt_dime_unc;

    private static final int REVERSE_IMAGE = R.drawable.rev_roosevelt_dime_unc;

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
        Integer slotImage = null;
        int imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        }
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED ;
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_silver_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_clad_coins);

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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_w);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_satin);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6_STRING_ID, R.string.include_s_proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_7, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_7_STRING_ID, R.string.include_silver_proofs);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        int startYear = getIntegerParameter(parameters, CoinPageCreator.OPT_START_YEAR);
        int stopYear = getIntegerParameter(parameters, CoinPageCreator.OPT_STOP_YEAR);
        boolean showP = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        boolean showD = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        boolean showS = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        boolean showW = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        boolean showSatin = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        boolean showProofs = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_6);
        boolean showSilverProofs = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_7);
        boolean showOld = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_1);
        boolean showSilver = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_2);
        boolean showClad = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_3);
        int coinIndex = 0;

        if (showOld) {
            for (String identifier : OLD_COINS_STRS) {
                coinList.add(new CoinSlot(identifier, "", coinIndex++, getImgId(identifier)));
            }
        }

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if(showSilver) {
                if (i > 1945 && i <= 1964) {
                    if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
                    if (showS && i < 1956) {coinList.add(new CoinSlot(year, "S", coinIndex++));}
                }
                if (showSilverProofs) {
                    if (i > 1949 && i < 1965) {coinList.add(new CoinSlot(year, "Silver Proof", coinIndex++));}
                    if (i > 1991) {coinList.add(new CoinSlot(year, "S Silver Proof", coinIndex++));}
                    if (i > 2918) {coinList.add(new CoinSlot(year, "S Reverse Silver Proof", coinIndex++));}
                }
            }
            if(showClad) {
                if (i > 1964 && i < 1968) {
                    coinList.add(new CoinSlot(year, "", coinIndex++));
                    coinList.add(new CoinSlot(year, "SMS", coinIndex++));
                }
                if (showP) {
                    if (i > 1967 && i < 1980) {coinList.add(new CoinSlot(year, "", coinIndex++));}
                    if (i > 1979) {coinList.add(new CoinSlot(year, "P", coinIndex++));}
                    if (showSatin && i > 2004 && i < 2011) {coinList.add(new CoinSlot(year, "P Satin", coinIndex++));}
                }
                if (showD) {
                    if (i > 1967) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
                    if (showSatin && i > 2004 && i < 2011) {coinList.add(new CoinSlot(year, "D Satin", coinIndex++));}
                }
                if (showW && i == 1996) {coinList.add(new CoinSlot(year, "W", coinIndex++));}
                if (showProofs && i > 1967) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++));}
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
                                           int oldVersion, int newVersion) {return 0;}
}