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

public class WashingtonQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Washington Quarters";

    private static final Integer START_YEAR = 1932;
    private static final Integer STOP_YEAR = 1998;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.quarter_front_92px;

    private static final int REVERSE_IMAGE = R.drawable.rev_1976_washington_quarter_unc;

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
        return OBVERSE_IMAGE_COLLECTED;}

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_silver_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_clad_coins);


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

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showProofs = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSilverProofs = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showSilver = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showClad = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        int coinIndex = 0;

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if ( i == 1976) {year = "1776-1976";}
            if (i == 1933 || (i == 1975))
                continue;

           if (showClad){
               if (showP) {
                   if (i >= 1980) {coinList.add(new CoinSlot(year, "P", coinIndex++));}
                   if (i < 1980){coinList.add(new CoinSlot(year, "", coinIndex++));}
                   if(i > 1964 && i < 1968 ){coinList.add(new CoinSlot(year, "SMS", coinIndex++));}
               }
               if (showD) {
                   if (i != 1938 && (i < 1965 || i > 1967)) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
               }
               if (showS) {
                   if (i < 1955 && i != 1934 && i != 1949) {coinList.add(new CoinSlot(year, "S", coinIndex++));}
               }
               if (showProofs){
                   if (i > 1967){coinList.add(new CoinSlot(year, "S Proof", coinIndex++));}
               }
           }
           if (showSilver){
               if ( i > 1931 && i <= 1964) {
                   if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++));}
                   if (showD && i != 1938) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
                   if (showS && i < 1955 && i != 1934 && i != 1949){
                       coinList.add(new CoinSlot(year, "S", coinIndex++));}
               }
               if (showSilverProofs) {
                   if ( i == 1976) {
                       coinList.add(new CoinSlot("1776-1976", String.format("S%n40%% Silver BU"), coinIndex++));
                       coinList.add(new CoinSlot("1776-1976", String.format("S%n40%% Silver Proof"), coinIndex++));
                   }
                   if (i > 1991) {coinList.add(new CoinSlot(Integer.toString(i), String.format("S%nSilver Proof"), coinIndex++));}
               }
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

