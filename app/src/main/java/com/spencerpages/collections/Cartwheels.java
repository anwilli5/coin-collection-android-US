
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

    private static final Object[][] OLDCOINS_COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.do1795_flowing_hairo},
            {"Draped Bust", R.drawable.do1799_draped_bust_dollaro},
            {"Seated Liberty", R.drawable.do1860o},
            {"Trade", R.drawable.do1876tradeo},

    };

    private static final Object[][] COIN_IDENTIFIERS = {
            {"`", R.drawable.obv_morgan_dollar},
            {" ", R.drawable.obv_peace_dollar},
            {"", R.drawable.obv_eisenhower_dollar},
            {"SBA", R.drawable.obv_susan_b_anthony_unc},
            {"Eagle",R.drawable.obv_american_eagle_unc},
            {"Morgan", R.drawable.obv_morgan_dollar},
            {"Peacer", R.drawable.obv_peace_dollar},
            {"Eisenhower", R.drawable.obv_eisenhower_dollar},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final Integer START_YEAR = 1878;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_american_eagle_unc;

    private static final int REVERSE_IMAGE =R.drawable.obv_peace_dollar ;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_Morgan);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_Peace);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_Ikes);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_Eagles);

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
        Boolean showold = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showmorgan = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showpeace = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showike = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showeagle = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);

        int coinIndex = 0;

        if (showold) {
            for (Object[] coinData : OLDCOINS_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++));
            }
        }
        if (showold && !showmorgan){coinList.add(new CoinSlot("Morgan","", coinIndex++));}
        if (showold && !showpeace){coinList.add(new CoinSlot("Peace","", coinIndex++));}
        if (showold && !showike){coinList.add(new CoinSlot("Eisenhower","", coinIndex++));}

        for (int i = startYear; i <= stopYear; i++) {

            if ((i > 1904 && i < 1921)) {continue;
            }else if (i >= 1929 && i <= 1933) {continue;}

            String phil = String.format("%d ", i);
            String den = String.format("%d D ", i);
            String sf = String.format("%d S ", i);
            String no = String.format("%d O ", i);
            String cc = String.format("%d CC ", i);
            if (showmorgan && i >1877 && i < 1922) {
                if (showP && i == 1878) {
                    coinList.add(new CoinSlot("`",String.format("%d 8 Feathers", i), coinIndex++));
                    coinList.add(new CoinSlot("`",String.format("%d 7 Feathers", i), coinIndex++));
                } else if (i != 1895) {coinList.add(new CoinSlot("`",phil, coinIndex++));}
                if (showD && i == 1921) {coinList.add(new CoinSlot("`",den, coinIndex++));}
                if (showO && i != 1878 && i != 1921) {coinList.add(new CoinSlot("`",no, coinIndex++));}
                if (showCC && i != 1886 && i != 1887 && i != 1888 && i <= 1893) {coinList.add(new CoinSlot("`",cc, coinIndex++));}
                if (showS) {coinList.add(new CoinSlot("`",sf, coinIndex++));
                }
            }
            if (showpeace && i > 1920 && i < 1936){
                if (showP) {coinList.add(new CoinSlot(" ",String.format("%d  ", i), coinIndex++));}
                if (showD && i != 1921 && i != 1924 && i != 1925 && i != 1928 && i != 1935) {coinList.add(new CoinSlot(" ",String.format("%d D  ", i), coinIndex++));}
                if (showS && i != 1921) {coinList.add(new CoinSlot(" ",String.format("%d S  ", i), coinIndex++));}
            }
            if (showike && i > 1970 && i < 1979){
                if (i < 1975) {
                    if (showP) {coinList.add(new CoinSlot("",phil, coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot("",den, coinIndex++));}
                    if (showS) {
                        {coinList.add(new CoinSlot("",String.format("%d S 40%% Silver", i), coinIndex++));}
                        {coinList.add(new CoinSlot("",String.format("%d S 40%% Silver Proof", i), coinIndex++));}
                        if (i > 1972){coinList.add(new CoinSlot("",String.format("%2d S Proof", i), coinIndex++));}
                    }
                }
                if (i == 1975) {continue;}
                if (i == 1976) {
                    if (showP) {
                        coinList.add(new CoinSlot("", String.format("%d Type I", i), coinIndex++));
                        coinList.add(new CoinSlot("",String.format("%d Type II", i), coinIndex++));}
                    if (showD) {
                        coinList.add(new CoinSlot("",String.format("%d D Type I", i), coinIndex++));
                        coinList.add(new CoinSlot("",String.format("%d D Type II", i), coinIndex++));}
                    if (showS) {
                        coinList.add(new CoinSlot("",String.format("%d S Proof Type I", i), coinIndex++));
                        coinList.add(new CoinSlot("",String.format("%d S Proof Type II", i), coinIndex++));
                        coinList.add(new CoinSlot("",String.format("%d S 40%% Silver", i), coinIndex++));
                        coinList.add(new CoinSlot("",String.format("%d S 40%% Silver Proof", i), coinIndex++));}
                }
                if ( i > 1976 ){
                    if (showP) {coinList.add(new CoinSlot("",phil, coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot("",den, coinIndex++));}
                    if (showS) {coinList.add(new CoinSlot("",String.format("%d S Proof", i), coinIndex++));}
                }
            }
            if (showeagle && i >1985){coinList.add(new CoinSlot(Integer.toString(i), "", coinIndex++));
                if (i == 2006) {coinList.add(new CoinSlot(Integer.toString(i), "W Burnished", coinIndex++));}
                if (i == 2007) {coinList.add(new CoinSlot(Integer.toString(i), "W Burnished", coinIndex++));}
                if (i == 2008) {coinList.add(new CoinSlot(Integer.toString(i), "W Burnished", coinIndex++));}
                if (i == 2011) {coinList.add(new CoinSlot(Integer.toString(i), "W Burnished", coinIndex++));}
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

