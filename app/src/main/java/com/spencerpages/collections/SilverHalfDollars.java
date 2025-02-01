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

public class SilverHalfDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Silver Half Dollars";

    private static final Object[][] OLDCOINS_COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.ha1795o},
            {"Draped Bust", R.drawable.ha1796o},
            {"Capped Bust", R.drawable.ha1837o},
            {"Seated Liberty", R.drawable.ha1853o},
    };

    private static final Object[][] COIN_IDENTIFIERS = {
            {" ", R.drawable.obv_barber_half},
            {"Walker", R.drawable.obv_walking_liberty_half},
            {"`", R.drawable.obv_franklin_half},
            {"", R.drawable.obv_kennedy_half_dollar_unc},
            {"'   ", R.drawable.kennedyproof},
            {"'", R.drawable.ha2018srevproof},
            {"Barber", R.drawable.obv_barber_half},
            {"Franklin", R.drawable.obv_franklin_half},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final Integer START_YEAR = 1892;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_walking_liberty_half;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_o);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_silver_Proofs);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_barber_half);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_walker);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_franklin);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_Kennedy);
    }
    // TODO Perform validation and throw exception

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSilver = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showold = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showbarber = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showwalker = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showfrank = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showken = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);

        int coinIndex = 0;

        if (showold) {
            for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++));
            }
        }
        if (showold && !showbarber) { coinList.add(new CoinSlot("Barber","", coinIndex++));}
        if (showold && !showwalker) {coinList.add(new CoinSlot("Walker","", coinIndex++));}
        if (showold && !showfrank) {coinList.add(new CoinSlot("Franklin","", coinIndex++));}

        for (int i = startYear; i <= stopYear; i++) {


            if (i == 1922 || i == 1924 || i == 1925 || i == 1926 || i == 1930 || i == 1931 || i == 1932) {continue;}

            String phil = String.format("%d ", i);
            String den = String.format("%d D ", i);
            String sf = String.format("%d S ", i);

            if (showbarber && i > 1891 && i < 1916) {
                if (showP) {coinList.add(new CoinSlot(" ",String.format("%d  ", i), coinIndex++));}
                if (showD && i >= 1906 && i != 1909 && i != 1910 && i != 1914) {coinList.add(new CoinSlot(" ",String.format("%d D  ", i), coinIndex++));}
                if (showS) {coinList.add(new CoinSlot(" ",String.format("%d S  ", i), coinIndex++));}
                if (showO && i <= 1909) {coinList.add(new CoinSlot(" ",String.format("%d O  ", i), coinIndex++));}
            }
            if (showwalker && i > 1915 && i < 1948) {
                if (showP && (i < 1923 || i > 1933)) {coinList.add(new CoinSlot(Integer.toString(i), "", coinIndex++));}
                if (showD && (i < 1923 || i > 1928) && i != 1933 && i != 1940) {
                    if (i == 1917) {
                        coinList.add(new CoinSlot(Integer.toString(i), "D Obverse", coinIndex++));
                        coinList.add(new CoinSlot(Integer.toString(i), "D Reverse", coinIndex++));
                    } else {coinList.add(new CoinSlot(Integer.toString(i), "D", coinIndex++));}
                }
                if (showS && i != 1938 && i != 1947) {
                    if (i == 1917) {
                        coinList.add(new CoinSlot(Integer.toString(i), "S Obverse", coinIndex++));
                        coinList.add(new CoinSlot(Integer.toString(i), "S Reverse", coinIndex++));
                    } else {coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++));}
                }
            }
            if (showfrank && i > 1947 && i < 1964) {
                if (showP) {coinList.add(new CoinSlot("`",phil, coinIndex++));}
                if (showD && i != 1955 && i != 1956) {coinList.add(new CoinSlot("`",den, coinIndex++));}
                if (showS && i != 1948 && i != 1950 && i <= 1954) {coinList.add(new CoinSlot("`",sf, coinIndex++));}
            }
            if (showken) {
                if (i == 1964) {
                    if (showP) {coinList.add(new CoinSlot("", phil, coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot("", den, coinIndex++));}
                    if (showSilver) {coinList.add(new CoinSlot("", String.format("%d Proof  ", i), coinIndex++));}
                }
                if (i > 1964 && i < 1968) {
                    if (showP) {
                        coinList.add(new CoinSlot("", String.format("%d 40%% Silver", i), coinIndex++));
                        coinList.add(new CoinSlot("", String.format("%d SMS 40%% Silver", i), coinIndex++));
                    }
                }
                if (i > 1967 && i < 1971) {
                    if (showD) {coinList.add(new CoinSlot("", String.format("%d D 40%% Silver", i), coinIndex++));}
                    if (showSilver) {coinList.add(new CoinSlot("", String.format("%d S 40%% Silver Proof", i), coinIndex++));}
                }
                if (i == 1976) {
                    if (showS) {coinList.add(new CoinSlot("", "1776-1796 S BU 40%% Silver", coinIndex++));}
                    if (showSilver) {coinList.add(new CoinSlot("","1776-1976 S 40%% Silver Proof", coinIndex++));}
                }
                if (showSilver && i > 1991) {coinList.add(new CoinSlot("", String.format("%d S Proof  ", i), coinIndex++));
                    if (i == 2014) {
                        {coinList.add(new CoinSlot("", String.format("%d P Proof  ", i), coinIndex++));}
                        {coinList.add(new CoinSlot("", den, coinIndex++));}
                        {coinList.add(new CoinSlot("", String.format("%d S Enhanced", i), coinIndex++));}
                        {coinList.add(new CoinSlot("'", String.format("%d W  ", i), coinIndex++));}
                    }
                    if (i == 2018){coinList.add(new CoinSlot("'", String.format("%d S  ", i), coinIndex++));}

                }
            }
        }
    }

    @Override
    public int getAttributionResId() {return R.string.attr_mint;}

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}
