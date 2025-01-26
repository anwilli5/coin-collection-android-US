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

public class StandingLibertyQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Standing Liberty Quarters";

    private static final Integer START_YEAR = 1916;
    private static final Integer STOP_YEAR = 1930;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_standing_liberty_quarter;

    private static final int REVERSE_IMAGE = R.drawable.rev_standing_liberty_quarter;

    // https://commons.wikimedia.org/wiki/File:Standing_Liberty_Quarter_Type1_1917S_Obverse.png
    // https://commons.wikimedia.org/wiki/File:Standing_Liberty_Quarter_Type2_1924D_Reverse.png
    private static final int ATTRIBUTION = R.string.attr_standing_liberty_quarters;

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

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        int coinIndex = 0;

        boolean addedTypeOne = false;

        for (int i = startYear; i <= stopYear; i++) {

            if (i == 1922) {
                continue;
            }

            String newValue;
            if (i == 1917) {
                if (!addedTypeOne) {
                    newValue = "1917 Type 1";
                } else {
                    newValue = "1917 Type 2";
                }
            } else {
                newValue = Integer.toString(i);
            }

            if (showMintMarks) {
                if (showP) {
                    coinList.add(new CoinSlot(newValue, "", coinIndex++));
                }
                if (showD) {
                    if (i != 1916 && i != 1921 && i != 1925 && i != 1923 && i != 1930) {
                        coinList.add(new CoinSlot(newValue, "D", coinIndex++));
                    }
                }
                if (showS) {
                    if (i != 1916 && i != 1921 && i != 1925) {
                        coinList.add(new CoinSlot(newValue, "S", coinIndex++));
                    }
                }
            } else {
                coinList.add(new CoinSlot(newValue, "", coinIndex++));
            }

            if (i == 1917 && !addedTypeOne) {
                addedTypeOne = true;
                i--;
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
        return 0;
    }
}
