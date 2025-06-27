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

public class TwentyCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Twenty Cents";

    private static final int REVERSE_IMAGE = R.drawable.a1876_cc_20c;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.a1876_cc_20c;

    @Override
    public int getStartYear() {return START_YEAR;}

    private static final Integer START_YEAR = 1875;
    private static final Integer STOP_YEAR = 1878;

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        return OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        int startYear = getIntegerParameter(parameters, CoinPageCreator.OPT_START_YEAR);
        int stopYear = getIntegerParameter(parameters, CoinPageCreator.OPT_STOP_YEAR);

        int coinIndex = 0;

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if(i==1875){
                coinList.add(new CoinSlot(year, "", coinIndex++));
                coinList.add(new CoinSlot(year, "S", coinIndex++));
                coinList.add(new CoinSlot(year, "CC", coinIndex++));
            }
            if(i==1876){
                coinList.add(new CoinSlot(year, "", coinIndex++));
                coinList.add(new CoinSlot(year, "CC", coinIndex++));
            }
            if(i==1877 || i==1878){
                coinList.add(new CoinSlot(year, "Proof", coinIndex++));
            }
        }
    }
    private static final int ATTRIBUTION = R.string.attr_twenty_cents;

    @Override
    public int getAttributionResId() {return ATTRIBUTION;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}