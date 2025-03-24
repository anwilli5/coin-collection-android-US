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

public class HalfCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Half Cents";

    private static final Object[][] COIN_IMG_IDS = {
            {"Liberty Cap 1793", R.drawable.annc_us_1793__c_liberty_cap_half_cent},      //0
            {"Liberty Cap", R.drawable.annc_us_1794__c_liberty_cap_half_cent},           //1
            {"Draped Bust", R.drawable.annc_us_1806__c_draped_bust_half_cent},           //2
            {"Capped Bust", R.drawable.annc_us_1828__c_classic_head_half_cent__proof_},  //3
            {"Braided Hair", R.drawable.annc_us_1844__c_braided_hair_half_cent__proof_}, //4
    };

    private static final int REVERSE_IMAGE = R.drawable.annc_us_1793__c_liberty_cap_half_cent;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getStartYear() {return START_YEAR;}

    private static final Integer START_YEAR = 1793;
    private static final Integer STOP_YEAR = 1857;

    @Override
    public int getStopYear() {return STOP_YEAR;}

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
    
    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);

        int coinIndex = 0;
        
        for (Integer i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (i == 1793) {coinList.add(new CoinSlot(year, String.format("%nLiberty Cap"), coinIndex++, getImgId("Liberty Cap 1793")));}
            if (i > 1793 && i < 1798) {coinList.add(new CoinSlot(year, String.format("%nLiberty Cap"), coinIndex++, getImgId("Liberty Cap")));}
            if (i > 1799 && i < 1809 && i!=1801) {coinList.add(new CoinSlot(year, String.format("%nDraped Bust"), coinIndex++, getImgId("Draped Bust")));}
            if (i>1808 && i<1812) {coinList.add(new CoinSlot(year, String.format("%nCapped Bust"), coinIndex++, getImgId("Capped Bust")));}
            if (i>1824 && i<1836 && i!=1827 && i!= 1830) {coinList.add(new CoinSlot(year, String.format("%nCapped Bust"), coinIndex++, getImgId("Capped Bust")));}
            if (i==1836) {coinList.add(new CoinSlot(year, String.format("Proof%nCapped Bust"), coinIndex++, getImgId("Capped Bust")));}
            if(i>1839 && i<1849){coinList.add(new CoinSlot(year, String.format("Proof%nBraided Hair"), coinIndex++, getImgId("Braided Hair")));}
            if(i>1848 && i<1858 && i!=1852) {coinList.add(new CoinSlot(year, String.format("%nBraided Hair"), coinIndex++, getImgId("Braided Hair")));}
            if(i==1852){coinList.add(new CoinSlot(year, String.format("Proof%nBraided Hair"), coinIndex++, getImgId("Braided Hair")));}
        }
    }

    private static final int ATTRIBUTION = R.string.attr_half_cents;
    @Override
    public int getAttributionResId() {return ATTRIBUTION;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}
