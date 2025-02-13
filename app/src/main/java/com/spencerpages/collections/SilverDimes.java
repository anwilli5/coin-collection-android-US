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

public class SilverDimes extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Silver Dimes";


    private static final Object[][] OLDCOINS_COIN_IDENTIFIERS = {
            {"Draped Bust", R.drawable.a1797drapeddime},
            {"Capped Bust", R.drawable.a1820cappeddime},
            {"Seated Liberty", R.drawable.astarsdime},
    };


    private static final Object[][] COIN_IDENTIFIERS = {
            {"Roosevelt", R.drawable.obv_roosevelt_dime_unc},
            {"Barber", R.drawable.obv_barber_dime},
            {"Mercury", R.drawable.obv_mercury_dime},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Draped Bust", R.drawable.a1797drapeddime},                                     // 0
            {"Capped Bust", R.drawable.a1820cappeddime},                                     // 1
            {"Seated No Stars", R.drawable.anostarsdime},                                    // 2
            {"Seated Stars", R.drawable.astarsdime},                                         // 3
            {"Seated Stars&Arrows", R.drawable.astars_arrowsdime},                           // 4
            {"Seated Legend", R.drawable.alegenddime},                                       // 5
            {"Seated Legend&Arrows ", R.drawable.alegendarrowsdime},                         // 6
            {"Barber", R.drawable.obv_barber_dime},                                          // 7
            {"Mercury", R.drawable.obv_mercury_dime},                                        // 8
            {"Roosevelt", R.drawable.obv_roosevelt_dime_unc},                                // 9
            {"1796 Draped Bust Sm Eagle Reverse", R.drawable.adi1796draped_bustr},           // 10
            {"1807 Draped Bust Heraldic Eagle Reverse", R.drawable.adi1807draped_bustr},     // 11
            {"1821 Capped Bust Reverse", R.drawable.adi1821r},                               // 12
            {"1838 Seated No Stars Reverse", R.drawable.adi1838r},                           // 13
            {"1843 Seated Stars Reverse", R.drawable.adi1843r},                              // 14
            {"1884 Legend Reverse", R.drawable.adi1884r},                                    // 15
            {"1914 Barber Reverse", R.drawable.adi1914r},                                    // 16
            {"1943 Mercury Reverse", R.drawable.adi1843r},                                   // 17
            {"2016 Roosevelt Reverse", R.drawable.adi2016r},                                 // 18
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}

    }


    private static final Integer START_YEAR = 1793;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_roosevelt_dime_unc;

    private static final int REVERSE_IMAGE = R.drawable.obv_roosevelt_dime_unc;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}



    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        } else {
            slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        }
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_barber);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_merc);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_roos);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string .include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string .include_o);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string .include_silver_Proofs);
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showOld = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showBarber = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showMerc = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showRoos = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSilver = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);

        int coinIndex = 0;

        if (showOld) {
            for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++));
            }
        }
        if (showOld && !showBarber) {coinList.add(new CoinSlot("Barber","", coinIndex++));}
        if (showOld && !showMerc) {coinList.add(new CoinSlot("Mercury","", coinIndex++));}

        for (Integer i = startYear; i <= stopYear; i++) {
            if (showBarber && i > 1891 && i < 1917) {
                if (showP) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,7));}
                if (showD) {
                    if (i >= 1906 && i <= 1912) {coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,7));}
                    if (i == 1914) {coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,7));}
                }
                if (showS && i != 1894) {coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,7));}
                if (showO && i != 1904 && i < 1910) {coinList.add(new CoinSlot(Integer.toString(i),"O", coinIndex++,7));}
            }

            if (showMerc && i > 1915 && i < 1946) {
                if (i == 1922 || i == 1932 || i == 1933) continue;
                if (showP) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,8));}
                if (showD && i != 1923 && i != 1930) {coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,8));}
                if (showS && i != 1921 && i != 1934) {coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,8));}
            }

            if (showRoos){
                if (i > 1945 && i <= 1964) {
                    if (showP) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,9));}
                    if (showD) {coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,9));}
                    if (showS && i < 1956) {coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,9));}
                }
                if (showSilver) {
                    if (i > 1949 && i < 1965) {coinList.add(new CoinSlot(Integer.toString(i), "Silver Proof", coinIndex++, 9));}
                    if (i > 1991) {coinList.add(new CoinSlot(Integer.toString(i), "Silver Proof", coinIndex++, 9));}
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





