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

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.DatabaseHelper.runSqlDelete;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class EisenhowerDollar extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Eisenhower Dollars";

    private static final Integer START_YEAR = 1971;
    private static final Integer STOP_YEAR = 1978;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_eisenhower_dollar;

    private static final int REVERSE_IMAGE = R.drawable.rev_eisenhower_dollar;

    // https://commons.wikimedia.org/wiki/File:1974S_Eisenhower_Obverse.jpg
    // https://commons.wikimedia.org/wiki/File:1974S_Eisenhower_Reverse.jpg
    private static final int ATTRIBUTION = R.string.attr_eisenhower_dollars;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
        return OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_s_Proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_silver);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSilver = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        int coinIndex = 0;

        for (int i = startYear; i <= stopYear; i++) {
            String newValue = Integer.toString(i);
            if (i==1971){
                if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                if (showSilver) {
                    if (showS) {coinList.add(new CoinSlot(newValue, "S 40% Silver", coinIndex++));}
                    if (showProof) {coinList.add(new CoinSlot(newValue, "S 40% Silver Proof", coinIndex++));}
                }
            }
            if (i==1972){
                if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                if (showSilver) {
                    if (showS) {coinList.add(new CoinSlot(newValue, "S 40% Silver", coinIndex++));}
                    if (showProof) {coinList.add(new CoinSlot(newValue, "S 40% Silver Proof", coinIndex++));}
                }
            }
            if (i==1973){
                if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(newValue, "S Proof", coinIndex++));}
                if (showSilver) {
                    if (showS) {coinList.add(new CoinSlot(newValue, "S 40% Silver", coinIndex++));}
                    if (showProof) {coinList.add(new CoinSlot(newValue, "S 40% Silver Proof", coinIndex++));}
                }
            }
            if (i==1974){
                if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(newValue, "S Proof", coinIndex++));}
                if (showSilver) {
                    if (showS) {coinList.add(new CoinSlot(newValue, "S 40% Silver", coinIndex++));}
                    if (showProof) {coinList.add(new CoinSlot(newValue, "S 40% Silver Proof", coinIndex++));}
                }
            }
            if (i == 1975) continue;
            if (i == 1976) {
                newValue = "1776-1976";
                if (showP) {
                    coinList.add(new CoinSlot(newValue, "Type I", coinIndex++));
                    coinList.add(new CoinSlot(newValue, "Type II", coinIndex++));}
                if (showD) {
                    coinList.add(new CoinSlot(newValue, "D Type I", coinIndex++));
                    coinList.add(new CoinSlot(newValue, "D Type II", coinIndex++));}
                if (showProof) {
                    coinList.add(new CoinSlot(newValue, "S Proof Type I", coinIndex++));
                    coinList.add(new CoinSlot(newValue, "S Proof Type II", coinIndex++));}
                if (showSilver) {
                    if (showS) {coinList.add(new CoinSlot(newValue, "S 40% Silver", coinIndex++));}
                    if (showProof) {coinList.add(new CoinSlot(newValue, "S 40% Silver Proof", coinIndex++));}
                }
            }
            if (i==1977){
                if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(newValue, "S Proof", coinIndex++));}
            }
            if (i==1978){
                if (showP) {coinList.add(new CoinSlot(newValue, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(newValue, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(newValue, "S Proof", coinIndex++));}
            }
        }
    }

    @Override
    public int getAttributionResId() {
        return ATTRIBUTION;
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
        String tableName = collectionListInfo.getName();
        int total = 0;

        if (oldVersion <= 2) {

            // Take out Eisenhower dollars > 1978
            for (int i = 1979; i <= 2012; i++) {
                total -= runSqlDelete(db, tableName, COL_COIN_IDENTIFIER + "=?", new String[]{String.valueOf(i)});
            }

            // Take out Eisenhower dollars with S marks
            total -= runSqlDelete(db, tableName, COL_COIN_MINT + "=?", new String[]{"S"});
        }

        return total;
    }
}
