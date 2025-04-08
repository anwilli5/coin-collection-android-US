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

public class LargeCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Large Cents";

    private static final Object[][] COIN_IMG_IDS = {
            {"Flying Eagle", R.drawable.a1858_cent_obv},                            // 0
            {"Indian Head", R.drawable.obv_indian_head_cent},                       // 1
            {"Wheat", R.drawable.ab1909},                                           // 2
            {"Steel", R.drawable.a1943o},                                           // 3
            {"Memorial Copper", R.drawable.amemorial},                              // 4
            {"Zinc", R.drawable.obv_lincoln_cent_unc},                              // 5
            {"Proof", R.drawable.lincolnproof_},                                    // 6
            {"Reverse Proof", R.drawable.cerevproof},                               // 7
            {"Flowing Hair", R.drawable.annc_us_1793_1c_flowing_hair_cent},         // 8
            {"Liberty Cap", R.drawable.a1794_cent_obv_venus_marina},                // 9
            {"Draped Bust", R.drawable.a1797_cent_obv},                             // 10
            {"Capped Bust", R.drawable.annc_us_1813_1c_classic_head_cent},          // 11
            {"Coronet", R.drawable.a1819_cent_obv},                                 // 12
            {"Young Coronet", R.drawable.a1837_cent_obv},                           // 13
            {"Young Braided Hair", R.drawable.a1839},                               // 14
            {"Mature Braided Hair", R.drawable.a1855},                              // 15
            {"Early Childhood", R.drawable.bicent_2009_early_childhood_unc},        // 16
            {"Formative Years", R.drawable.bicent_2009_formative_years_unc},        // 17
            {"Professional Life", R.drawable.bicent_2009_professional_life_unc},    // 18
            {"Presidency", R.drawable.bicent_2009_presidency_unc},                  // 19
            {"Indian Reverse", R.drawable.rev_indian_head_cent},                    // 20
            {"Wheat Reverse", R.drawable.ab1909r},                                  // 21
            {"Memorial Reverse", R.drawable.rev_lincoln_cent_unc},                  // 22
            {"Shield Reverse", R.drawable.ashieldr},                                // 23
            {"1793 Chain Reverse", R.drawable.a1793chainrev},                       // 24
            {"1794 Reverse", R.drawable.a1794r},                                    // 25
            {"1819 Reverse", R.drawable.a1819r},                                    // 26
            {"1839 Reverse", R.drawable.a1839r},                                    // 27
            {"1858 Reverse", R.drawable.a1858r},                                    // 28
    };

    private static final int REVERSE_IMAGE = R.drawable.a1819_cent_obv;

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
        int imageId = coinSlot.getImageId();
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

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_bust_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_coronet_coins);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showBust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showCoronet = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);

        int coinIndex = 0;

        for (Integer i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (showBust) {
                if (i == 1793) {
                    coinList.add(new CoinSlot(year, String.format("%nFlowing Hair%nChain Reverse"), coinIndex++, getImgId("Flowing Hair")));
                    coinList.add(new CoinSlot(year, String.format("%nFlowing Hair%nWreath Reverse"), coinIndex++, getImgId("Flowing Hair")));}
                if (i > 1792 && i < 1797) {
                    coinList.add(new CoinSlot(year, String.format("%nLiberty Cap"), coinIndex++, getImgId("Liberty Cap")));}
                if (i > 1795 && i < 1808) {
                    coinList.add(new CoinSlot(year, String.format("%nDraped Bust"), coinIndex++, getImgId("Draped Bust")));}
                if (i > 1807 && i < 1815) {
                    coinList.add(new CoinSlot(year, String.format("%nCapped Bust"), coinIndex++, getImgId("Capped Bust")));}
            }
            if (showCoronet) {
                if (i>1815 && i<1836){
                    coinList.add(new CoinSlot(year, "Matron Coronet", coinIndex++, getImgId("Coronet")));}
                if (i>1835 && i<1840){
                    coinList.add(new CoinSlot(year, "Young Coronet", coinIndex++, getImgId("Young Coronet")));}
                if (i>1838 && i<1844){
                    coinList.add(new CoinSlot(year, "Petite Braided Hair", coinIndex++, getImgId("Young Braided Hair")));}
                if (i>1843 && i<1858){
                    coinList.add(new CoinSlot(year, "Mature Braided Hair", coinIndex++, getImgId("Mature Braided Hair")));}
            }
        }
    }
    private static final int ATTRIBUTION = R.string.attr_large_cents;
    @Override
    public int getAttributionResId() {return ATTRIBUTION;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}

