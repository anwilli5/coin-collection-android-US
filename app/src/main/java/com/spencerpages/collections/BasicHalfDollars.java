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

public class BasicHalfDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Half-Dollars";

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
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        int coinIndex = 0;

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (i == 1975 || i == 1976) {
                year = "1776-1976";
            }
            if (i == 1976 && startYear != 1976)
                continue;

            if (showMintMarks) {
                if (showP) {
                    if (i < 1968 || i > 1970) {
                        if (i >= 1980) {
                            coinList.add(new CoinSlot(year, "P", coinIndex++));
                        } else {
                            coinList.add(new CoinSlot(year, "", coinIndex++));
                        }
                    }
                }
                if (showD) {
                    if (i != 1965 && i != 1966 && i != 1967) {
                        coinList.add(new CoinSlot(year, "D", coinIndex++));
                    }
                }
            } else {
                coinList.add(new CoinSlot(year, "", coinIndex++));
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

        int total = 0;
        if (oldVersion <= 2) {
            // Need to add in 1968 - 1970 for Half Dollars unless it doesn't exist
            // NOTE - We can't fix this, because adding will mess up the _id fields, which we use to do ordering
            // We could make a new table and copy over all of the data, but that presents a lot of challenges
        }

        if (oldVersion <= 3) {
            // Add in new 2013 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2013);
        }

        if (oldVersion <= 4) {
            // Add in new 2014 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2014);
        }

        if (oldVersion <= 6) {
            // Add in new 2015 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2015);
        }

        if (oldVersion <= 7) {
            // Add in new 2016 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2016);
        }

        if (oldVersion <= 8) {
            // Add in new 2017 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2017);
        }

        if (oldVersion <= 11) {
            // Add in new 2018 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2018);
        }

        if (oldVersion <= 12) {
            // Add in new 2019 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2019);
        }

        if (oldVersion <= 13) {
            // Add in new 2020 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2020);
        }

        if (oldVersion <= 16) {
            // Add in new 2021 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2021);
        }

        if (oldVersion <= 18) {
            // Add in new 2022 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2022);
        }

        if (oldVersion <= 19) {
            // Add in new 2023 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2023);
        }

        if (oldVersion <= 20) {
            // Add in new 2024 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2024);
        }

        if (oldVersion <= 22) {
            // Add in new 2025 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2025);
        }

        return total;
    }
}
