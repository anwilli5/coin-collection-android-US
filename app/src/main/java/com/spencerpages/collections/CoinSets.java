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

public class CoinSets extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Coin Sets";

    private static final Object[][] COIN_IMG_IDS = {
            {"Proof Set", R.drawable.a24rg},              // 0
            {"Mint Set", R.drawable.a24rj},               // 1
            {"Silver Proof Set", R.drawable.a24rh},       // 2
    };

    private static final int REVERSE_IMAGE = R.drawable.a24rg;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    private static final Integer START_YEAR = 1947;

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int ATTRIBUTION = R.string.attr_mint;

    @Override
    public int getAttributionResId() { return ATTRIBUTION;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = null;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        }
        return (slotImage != null) ? slotImage : REVERSE_IMAGE;
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}

    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_mint_sets);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_proof_sets);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_silver_proof_sets);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMint = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showProof = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showSilver= (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);

        int coinIndex = 0;

        for (int i = startYear; i <= stopYear;  i++) {
            String year = Integer.toString(i);
            if (showMint && i > 1946 && i != 1950){
                coinList.add(new CoinSlot(year, String.format("%nMint Set"), coinIndex++, getImgId("Mint Set")));
            }
            if (showProof && i > 1964 ){
                coinList.add(new CoinSlot(year, String.format("%nProof Set"), coinIndex++, getImgId("Proof Set")));
            }
            if (showSilver && i > 1949 && i < 1965) {
                coinList.add(new CoinSlot(year, String.format("Silver %nProof Set"), coinIndex++, getImgId("Silver Proof Set")));
            }
            if (showSilver && i >1991) {
                coinList.add(new CoinSlot(year, String.format("Silver %nProof Set"), coinIndex++, getImgId("Silver Proof Set")));
            }
        }
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}