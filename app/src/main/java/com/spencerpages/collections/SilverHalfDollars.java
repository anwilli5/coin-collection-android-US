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

    static final Object[][] COIN_IMG_IDS = {
            {"Flowing Hair", R.drawable.a1795_half_dollar_obv},                     // 0
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},         // 1
            {"Capped Bust", R.drawable.a1834_bust_half_dollar_obverse},             // 2
            {"Seated", R.drawable.a1885_half_dollar_obv},                           // 3
            {"Seated w Arrows", R.drawable.a1873_half_dollar_obverse},              // 4
            {"Barber", R.drawable.obv_barber_half},                                 // 5
            {"Walking Liberty", R.drawable.obv_walking_liberty_half},               // 6
            {"Franklin", R.drawable.obv_franklin_half},                             // 7
            {"Kennedy", R.drawable.obv_kennedy_half_dollar_unc},                    // 8
            {"Kennedy Proof", R.drawable.kennedyproof},                             // 9
            {"Kennedy Reverse Proof", R.drawable.ha2018srevproof},                  // 10
            {"Kennedy Reverse", R.drawable.rev_kennedy_half_dollar_unc},            // 11
            {"Franklin Reverse", R.drawable.rev_franklin_half},                     // 12
            {"Walking Liberty Reverse", R.drawable.rev_walking_liberty_half},       // 13
            {"Barber Reverse", R.drawable.rev_barber_half},                         // 14
            {"Flowing Hair (Old)", R.drawable.ha1795o},                             // 15
            {"Draped Bust (Old)", R.drawable.ha1796o},                              // 16
            {"Capped Bust (Old)", R.drawable.ha1837o},                              // 17
            {"Seated Liberty (Old)", R.drawable.ha1853o},                           // 18
    };

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
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = null;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        }
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}

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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_silver_proofs);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_barber_half);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_walker_half);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_franklin_half);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_kennedy_half);
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
        Boolean showOld = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showBarber = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showWalker = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showFrank = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showKen = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);

        int coinIndex = 0;

        if (showOld) {
            coinList.add(new CoinSlot("Flowing Hair", "", coinIndex++, getImgId("Flowing Hair (Old)")));
            coinList.add(new CoinSlot("Draped Bust", "", coinIndex++, getImgId("Draped Bust (Old)")));
            coinList.add(new CoinSlot("Capped Bust", "", coinIndex++, getImgId("Capped Bust (Old)")));
            coinList.add(new CoinSlot("Seated Liberty", "", coinIndex++, getImgId("Seated Liberty (Old)")));
        }
        if (showOld && !showBarber) {coinList.add(new CoinSlot("Barber", "", coinIndex++, getImgId("Barber")));}
        if (showOld && !showWalker) {coinList.add(new CoinSlot("Walker", "", coinIndex++, getImgId("Walking Liberty")));}
        if (showOld && !showFrank) {coinList.add(new CoinSlot("Franklin", "", coinIndex++, getImgId("Franklin")));}

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (i == 1922 || i == 1924 || i == 1925 || i == 1926 || i == 1930 || i == 1931 || i == 1932) {continue;}
            if (showBarber && i > 1891 && i < 1916) {
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Barber")));}
                if (showD && i >= 1906 && i != 1909 && i != 1910 && i != 1914) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Barber")));}
                if (showS) {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Barber")));}
                if (showO && i <= 1909) {coinList.add(new CoinSlot(year, "O", coinIndex++, getImgId("Barber")));}
            }
            if (showWalker && i > 1915 && i < 1948) {
                if (showP && (i < 1923 || i > 1933)) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Walking Liberty")));}
                if (showD && (i < 1923 || i > 1928) && i != 1933 && i != 1940) {
                    if (i == 1917) {
                        coinList.add(new CoinSlot(year, "D Obverse", coinIndex++, getImgId("Walking Liberty")));
                        coinList.add(new CoinSlot(year, "D Reverse", coinIndex++, getImgId("Walking Liberty")));
                    } else {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Walking Liberty")));}
                }
                if (showS && i != 1938 && i != 1947) {
                    if (i == 1917) {
                        coinList.add(new CoinSlot(year, "S Obverse", coinIndex++, getImgId("Walking Liberty")));
                        coinList.add(new CoinSlot(year, "S Reverse", coinIndex++, getImgId("Walking Liberty")));
                    } else {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Walking Liberty")));}
                }
            }
            if (showFrank && i > 1947 && i < 1964) {
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Franklin")));}
                if (showD && i != 1955 && i != 1956) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Franklin")));}
                if (showS && i != 1948 && i != 1950 && i <= 1954) {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Franklin")));}
            }
            if (showKen) {
                if (i == 1964) {
                    if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Kennedy")));}
                    if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Kennedy")));}
                    if (showSilver) {coinList.add(new CoinSlot(year, "Proof", coinIndex++, getImgId("Kennedy Proof")));}
                }
                if (i > 1964 && i < 1968) {
                    if (showP) {
                        coinList.add(new CoinSlot(year, "40%% Silver", coinIndex++, getImgId("Kennedy")));
                        coinList.add(new CoinSlot(year, "SMS 40%% Silver", coinIndex++, getImgId("Kennedy")));
                    }
                }
                if (i > 1967 && i < 1971) {
                    if (showD) {coinList.add(new CoinSlot(year, "D 40%% Silver", coinIndex++, getImgId("Kennedy")));}
                    if (showSilver) {coinList.add(new CoinSlot(year, "S 40%% Silver Proof", coinIndex++, getImgId("Kennedy Proof")));}
                }
                if (i == 1976) {
                    if (showS) {coinList.add(new CoinSlot("1776-1796", "S BU 40%% Silver", coinIndex++, getImgId("Kennedy")));}
                    if (showSilver) {coinList.add(new CoinSlot("1776-1976", "S 40%% Silver Proof", coinIndex++, getImgId("Kennedy Proof")));}
                }
                if (showSilver && i > 1991) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++, getImgId("Kennedy Proof")));
                    if (i == 2014) {
                        coinList.add(new CoinSlot(year, "P Proof", coinIndex++, getImgId("Kennedy Proof")));
                        coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Kennedy")));
                        coinList.add(new CoinSlot(year, "S Enhanced", coinIndex++, getImgId("Kennedy")));
                        coinList.add(new CoinSlot(year, "W Reverse Proof", coinIndex++, getImgId("Kennedy Reverse Proof")));
                    }
                    if (i == 2018){coinList.add(new CoinSlot(year, "S Reverse Proof", coinIndex++, getImgId("Kennedy Reverse Proof")));}

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
