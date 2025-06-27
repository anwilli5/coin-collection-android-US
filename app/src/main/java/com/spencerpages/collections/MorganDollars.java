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

public class MorganDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Morgan Dollars";

    private static final Integer START_YEAR = 1878;
    private static final Integer STOP_YEAR = 1921;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_morgan_dollar;

    private static final int REVERSE_IMAGE = R.drawable.rev_morgan_dollar;

    // https://commons.wikimedia.org/wiki/File:Morgan_Dollar_1880S_Obverse.png
    // https://commons.wikimedia.org/wiki/File:Morgan_Dollar_1880S_Reverse.png
    private static final int ATTRIBUTION = R.string.attr_morgan_dollars;

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

        // Use the MINT_MARK_4 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_o);

        // Use the MINT_MARK_5 checkbox for whether to include 'CC' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_cc);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        int startYear = getIntegerParameter(parameters, CoinPageCreator.OPT_START_YEAR);
        int stopYear = getIntegerParameter(parameters, CoinPageCreator.OPT_STOP_YEAR);
        boolean showMintMarks = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARKS);
        boolean showP = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        boolean showD = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        boolean showS = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        boolean showO = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        boolean showCC = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        int coinIndex = 0;

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if ((i > 1904 && i < 1921)) {
                continue;
            }

            if (showMintMarks) {
                if (showP) {
                    if (i == 1878) {
                        coinList.add(new CoinSlot("1878 8 Feathers", "", coinIndex++));
                        coinList.add(new CoinSlot("1878 7 Feathers", "", coinIndex++));
                    } else if (i != 1895) {
                        coinList.add(new CoinSlot(year, "", coinIndex++));
                    }
                }
                if (showD) {
                    if (i == 1921) {
                        coinList.add(new CoinSlot(year, "D", coinIndex++));
                    }
                }
                if (showO) {
                    if (i != 1878 && i != 1921) {
                        coinList.add(new CoinSlot(year, "O", coinIndex++));
                    }
                }
                if (showCC) {
                    if (i != 1886 && i != 1887 && i != 1888 && i <= 1893) {
                        coinList.add(new CoinSlot(year, "CC", coinIndex++));
                    }
                }
                if (showS) {
                    coinList.add(new CoinSlot(year, "S", coinIndex++));
                }
            } else {
                coinList.add(new CoinSlot(year, "", coinIndex++));
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
