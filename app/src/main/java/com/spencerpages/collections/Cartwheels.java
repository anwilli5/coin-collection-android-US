
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

public class Cartwheels extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Cartwheels";

    private static final Object[][] OLD_COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.do1795_flowing_hairo},
            {"Draped Bust", R.drawable.do1799_draped_bust_dollaro},
            {"Seated Liberty", R.drawable.do1860o},
            {"Trade", R.drawable.do1876tradeo},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Flowing Hair", R.drawable.do1795_flowing_hairo},       // 0
            {"Draped Bust", R.drawable.do1799_draped_bust_dollaro},  // 1
            {"Seated Liberty", R.drawable.do1860o},                  // 2
            {"Trade", R.drawable.do1876tradeo},                      // 3
            {"Morgan", R.drawable.obv_morgan_dollar},                // 4
            {"Peace", R.drawable.obv_peace_dollar},                  // 5
            {"Eisenhower", R.drawable.obv_eisenhower_dollar},        // 6
            {"Eagle", R.drawable.obv_american_eagle_unc},            // 7
            {"Liberty Seated Gobrecht", R.drawable.anostarsdime},    // 8
    };

    private static final Integer START_YEAR = 1878;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int REVERSE_IMAGE = R.drawable.obv_peace_dollar ;

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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_morgan_dollars);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_peace_dollars);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_ike_dollars);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_eagle_dollars);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_o);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_cc);
    }
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showOld = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showMorgan = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showPeace = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showIke = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showEagle = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);

        int coinIndex = 0;

        if (showOld) {
            for (Object[] coinData : OLD_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++, getImgId(identifier)));
            }
        }
        if (showOld && !showMorgan){coinList.add(new CoinSlot("Morgan", "", coinIndex++, getImgId("Morgan")));}
        if (showOld && !showPeace){coinList.add(new CoinSlot("Peace", "", coinIndex++, getImgId("Peace")));}
        if (showOld && !showIke){coinList.add(new CoinSlot("Eisenhower", "", coinIndex++, getImgId("Eisenhower")));}

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);

            if ((i > 1904 && i < 1921)) {continue;}
            else if (i >= 1929 && i <= 1933) {continue;}

            if (showMorgan && i >1877 && i < 1922) {
                if (showP) {
                    if (i == 1878) {
                        coinList.add(new CoinSlot(year, "8 Feathers", coinIndex++, getImgId("Morgan")));
                        coinList.add(new CoinSlot(year, "7 Feathers", coinIndex++, getImgId("Morgan")));
                    } else if (i != 1895) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Morgan")));}
                }
                if (showO && i != 1878 && i != 1921) {coinList.add(new CoinSlot(year, "O", coinIndex++, getImgId("Morgan")));}
                if (showS) {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Morgan")));}
                if (showCC && i != 1886 && i != 1887 && i != 1888 && i <= 1893) {
                    coinList.add(new CoinSlot(year, "CC", coinIndex++, getImgId("Morgan")));}
                if (showD && i == 1921) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Morgan")));}
            }
            if (showPeace && i > 1920 && i < 1936){
                if (showP) {coinList.add(new CoinSlot(year,"", coinIndex++, getImgId("Peace")));}
                if (showD && i != 1921 && i != 1924 && i != 1925 && i != 1928 && i != 1935) {
                    coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Peace")));}
                if (showS && i != 1921) {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Peace")));}
            }
            if (showIke && i > 1970 && i < 1979){
                if (i < 1975) {
                    if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Eisenhower")));}
                    if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Eisenhower")));}
                    if (showS) {
                        {coinList.add(new CoinSlot(year, "S 40% Silver", coinIndex++, getImgId("Eisenhower")));}
                        {coinList.add(new CoinSlot(year, "S 40% Silver Proof", coinIndex++, getImgId("Eisenhower")));}
                        if (i > 1972){coinList.add(new CoinSlot(year, "S Proof", coinIndex++, getImgId("Eisenhower")));}
                    }
                }
                if (i == 1975) {continue;}
                if (i == 1976) {
                    if (showP) {
                        coinList.add(new CoinSlot("1776-1976", "Type I", coinIndex++, getImgId("Eisenhower")));
                        coinList.add(new CoinSlot("1776-1976", "Type II", coinIndex++, getImgId("Eisenhower")));}
                    if (showD) {
                        coinList.add(new CoinSlot("1776-1976", "D Type I", coinIndex++, getImgId("Eisenhower")));
                        coinList.add(new CoinSlot("1776-1976", "D Type II", coinIndex++, getImgId("Eisenhower")));}
                    if (showS) {
                        coinList.add(new CoinSlot("1776-1976", "S Proof Type I", coinIndex++, getImgId("Eisenhower")));
                        coinList.add(new CoinSlot("1776-1976", "S Proof Type II", coinIndex++, getImgId("Eisenhower")));
                        coinList.add(new CoinSlot("1776-1976", "S 40% Silver", coinIndex++, getImgId("Eisenhower")));
                        coinList.add(new CoinSlot("1776-1976", "S 40% Silver Proof", coinIndex++, getImgId("Eisenhower")));}
                }
                if ( i > 1976 ){
                    if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Eisenhower")));}
                    if (showD) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Eisenhower")));}
                    if (showS) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++, getImgId("Eisenhower")));}
                }
            }
            if (showEagle){
                if (showP && i >1985) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Eagle")));
                    if (i == 2006) {coinList.add(new CoinSlot(year, "W Burnished", coinIndex++, getImgId("Eagle")));}
                    if (i == 2007) {coinList.add(new CoinSlot(year, "W Burnished", coinIndex++, getImgId("Eagle")));}
                    if (i == 2008) {coinList.add(new CoinSlot(year, "W Burnished", coinIndex++, getImgId("Eagle")));}
                    if (i == 2011) {coinList.add(new CoinSlot(year, "W Burnished", coinIndex++, getImgId("Eagle")));}
                }
            }
        }
    }
    @Override
    public int getAttributionResId() {return R.string.attr_mint;}

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}
