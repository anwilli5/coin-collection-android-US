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

public class EarlyDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Early Dollars";


    private static final Object[][] COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.a1795_half_dollar_obv},
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},
            {"Seated", R.drawable.a1885_half_dollar_obv},
            {"Seated ", R.drawable.anostarsdime},
            {"Trade", R.drawable.annc1884_t_1_trade_dollar__judd_1732_},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Flowing Hair", R.drawable.do1795_flowing_hairo},       // 0
            {"Draped Bust", R.drawable.do1799_draped_bust_dollaro},  // 1
            {"Seated Liberty", R.drawable.do1860o},                  // 2
            {"Trade", R.drawable.do1876tradeo},                      // 3
            {"Morgan", R.drawable.obv_morgan_dollar},                // 4
            {"Peace", R.drawable.obv_peace_dollar},                  // 5
            {"Eisenhower", R.drawable.obv_eisenhower_dollar},        // 6
            {"Eagle",R.drawable.obv_american_eagle_unc},             // 7
            {"Liberty Seated Gobrecht", R.drawable.anostarsdime},    // 8
    };


   private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
       for (Object[] coinData : COIN_IDENTIFIERS) {
           COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
       }

    }


    private static final Integer START_YEAR = 1794;
    private static final Integer STOP_YEAR = 1885;

    private static final int REVERSE_IMAGE = R.drawable.annc1884_t_1_trade_dollar__judd_1732_;

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
        Integer slotImage;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        } else {
            slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        }
        return (slotImage != null) ? slotImage :(int) COIN_IDENTIFIERS[0][1];
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
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_bust);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_seated);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_trade);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_o);

        // Use the MINT_MARK_2 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_cc);

    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean show_bust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean show_seated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean show_trade = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);

        int coinIndex = 0;


        for (Integer i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if(show_bust){
                if(i==1794 || i==1795){coinList.add(new CoinSlot(year,"Flowing Hair", coinIndex++,getImgId("Flowing Hair")));}
                if(i>1794 && i<1799){coinList.add(new CoinSlot(year,"Draped Bust", coinIndex++,getImgId("Draped Bust")));}
                if(i>1797 && i<1804){coinList.add(new CoinSlot(year,"Draped Bust Heraldic Eagle",coinIndex++,getImgId("Draped Bust")));}
                if(i==1804){coinList.add(new CoinSlot(year,"Draped Bust Rare", coinIndex++,getImgId("Draped Bust")));}
            }
            if(show_seated){
                if (showP) {
                    if (i == 1836) {coinList.add(new CoinSlot(year,"Gobrecht",coinIndex++,getImgId("Liberty Seated Gobrecht")));}
                    if (i == 1838 || i == 1839) {coinList.add(new CoinSlot(year,"Gobrecht Proof", coinIndex++,getImgId("Seated Liberty")));}
                    if (i > 1839 && i < 1866 && i != 1858) {coinList.add(new CoinSlot(year,"", coinIndex++,getImgId("Seated Liberty")));}
                    if (i > 1865 && i < 1874) {coinList.add(new CoinSlot(year,"Motto", coinIndex++,getImgId("Seated Liberty")));}
                }
                if(showO){
                    if(i==1846 || i==1850 || i==1851 || i==1859 || i==1860){
                        coinList.add(new CoinSlot(year,"O", coinIndex++,getImgId("Seated Liberty")));}
                }
                if(showS){
                    if( i==1859 || i==1870 || i==1872 || i == 1873){
                        coinList.add(new CoinSlot(year,"S",  coinIndex++,getImgId("Seated Liberty")));}
                }
                if(showCC && i>1869 && i<1874){coinList.add(new CoinSlot(year,"CC", coinIndex++,getImgId("Seated Liberty")));}
            }
            if(show_trade){
                if (showP) {
                    if (i > 1872 && i < 1878) {coinList.add(new CoinSlot(year,"", coinIndex++,getImgId("Trade")));}
                    if(i>1878 && i <1886){coinList.add(new CoinSlot(year,"Proof", coinIndex++,getImgId("Trade")));}
                }
                if(showS && i>1872 && i<1879){coinList.add(new CoinSlot(year,"S", coinIndex++,getImgId("Trade")));}
                if(showCC && i>1872 && i<1879){coinList.add(new CoinSlot(year,"CC", coinIndex++,getImgId("Trade")));}
            }
        }
    }

    @Override
    public int getAttributionResId() {
        return R.string.attr_EarlyHalfs;
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
