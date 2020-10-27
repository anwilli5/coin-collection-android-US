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

public class EisenhowerDollar extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Eisenhower Dollars";

    private static final Integer START_YEAR = 1971;
    private static final Integer STOP_YEAR = 1978;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_eisenhower_dollar;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_eisenhower_dollar;

    // https://commons.wikimedia.org/wiki/File:1974S_Eisenhower_Obverse.jpg
    // https://commons.wikimedia.org/wiki/File:1974S_Eisenhower_Reverse.jpg
    private static final String ATTRIBUTION = "Eisenhower Dollar images courtesy of Brandon Grossardt via Wikimedia";

    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    public int getCoinSlotImage(CoinSlot coinSlot) {
        return coinSlot.isInCollection() ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
    }

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
    }

    // TODO Perform validation and throw exception
    @SuppressWarnings("ConstantConditions")
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);

        for (int i = startYear; i <= stopYear; i++) {
            String newValue = Integer.toString(i);
            if (i == 1975 || i == 1976) {
                newValue = "1776-1976";
            }
            if (i == 1976 && startYear != 1976)
                continue; // (what if start date is 1976)

            if (showMintMarks) {
                if (showP) {
                    coinList.add(new CoinSlot(newValue, ""));
                }
                if (showD) {
                    coinList.add(new CoinSlot(newValue, "D"));
                }
            } else {
                coinList.add(new CoinSlot(newValue, ""));
            }

            //if(i < 1973){
            //	if(mShowS){
            //	    mIdentifierList.add(newValue);
            //	    mMintList.add("S");
            //	}
            //}
        }
    }

    public String getAttributionString() {
        return ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
                                           int oldVersion, int newVersion) {
        int total = 0;

        if (oldVersion <= 2) {

            // Take out Eisenhower dollars > 1978
            for (int i = 1979; i <= 2012; i++) {
                int value = db.delete(tableName, "coinIdentifier=?", new String[]{String.valueOf(i)});
                total -= value;
            }

            // Take out Eisenhower dollars with S marks
            int value = db.delete(tableName, "coinMint=?", new String[]{"S"});
            total -= value;
        }

        return total;
    }
}