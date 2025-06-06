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

import static com.coincollection.CoinSlot.COIN_SLOT_NAME_MINT_WHERE_CLAUSE;
import static com.coincollection.DatabaseHelper.runSqlDelete;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Quarters";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"1776-1976", R.drawable.rev_1976_washington_quarter_unc},
            {"Crossing the Delaware", R.drawable.rev_2021_crossing_delaware_quarter_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final Integer START_YEAR = 1932;
    private static final Integer STOP_YEAR = 2021;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.quarter_front_92px;

    // TODO Replace with standard back when good image becomes available
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
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
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

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (i == 1975 || i == 1976) {
                year = "1776-1976";
            }
            if (i == 1933 || (i == 1976 && startYear != 1976))
                continue;
            if (i > 1998 && i < 2021)
                continue;
            if (i == 2021) {
                year = "Crossing the Delaware";
            }

            if (showMintMarks) {
                if (showP) {
                    if (i >= 1980) {
                        coinList.add(new CoinSlot(year, "P", coinIndex++));
                    } else {
                        coinList.add(new CoinSlot(year, "", coinIndex++));
                    }
                }
                if (showD) {
                    if (i != 1938 && (i < 1965 || i > 1967)) {
                        coinList.add(new CoinSlot(year, "D", coinIndex++));
                    }
                }
                if (showS) {
                    if (i < 1955 && i != 1934 && i != 1949) {
                        coinList.add(new CoinSlot(year, "S", coinIndex++));
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
        String tableName = collectionListInfo.getName();
        int total = 0;

        if (oldVersion <= 2) {
            // Remove 1965 - 1967 D quarters
            total -= runSqlDelete(db, tableName, COIN_SLOT_NAME_MINT_WHERE_CLAUSE, new String[]{"1965", "D"});
            total -= runSqlDelete(db, tableName, COIN_SLOT_NAME_MINT_WHERE_CLAUSE, new String[]{"1966", "D"});
            total -= runSqlDelete(db, tableName, COIN_SLOT_NAME_MINT_WHERE_CLAUSE, new String[]{"1967", "D"});
        }

        if (oldVersion <= 15) {
            // Add in new 2021 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 1998, 2021, "Crossing the Delaware");
        }

        return total;
    }
}
