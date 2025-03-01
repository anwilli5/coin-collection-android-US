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

public class EarlyQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Early Quarters";

    private static final Object[][] OLDCOIN_IDENTIFIERS = {
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},
            {"Capped Bust", R.drawable.a1834_bust_half_dollar_obverse},
            {"Seated", R.drawable.a1885_half_dollar_obv},
    };

    private static final Object[][] COIN_IDENTIFIERS = {
            {"", R.drawable.obv_standing_liberty_quarter},
            {"Barber", R.drawable.obv_barber_quarter},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},  // 0
            {"Capped Bust", R.drawable.a1834_bust_half_dollar_obverse},      // 1
            {"Seated", R.drawable.a1885_half_dollar_obv},                    // 2
            {"Standing Liberty", R.drawable.obv_standing_liberty_quarter},   // 3
            {"Barber", R.drawable.obv_barber_quarter},                       // 4
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        for (Object[] coinData : OLDCOIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final Integer START_YEAR = 1776;
    private static final Integer STOP_YEAR = 1930;

    private static final int REVERSE_IMAGE = R.drawable.obv_barber_quarter;

    // https://commons.wikimedia.org/wiki/File:1914_Barber_Quarter_NGC_AU58_Obverse.png
    // https://commons.wikimedia.org/wiki/File:1914_Barber_Quarter_NGC_AU58_Reverse.png
    private static final int ATTRIBUTION = R.string.attr_barber_quarters;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        } else {
            slotImage = COIN_MAP.get(coinSlot.getIdentifier());
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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_bust_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_seated_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_barber_quarters);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_standing_quarters);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        // Use the MINT_MARK_4 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_o);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_cc);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showOld = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showBust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showSeated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showBarber = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showStanding = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        int coinIndex = 0;

        if (showOld && !showBust) {
            coinList.add(new CoinSlot("Draped Bust", "", coinIndex++));
            coinList.add(new CoinSlot("Capped Bust", "", coinIndex++));
        }
        if (showOld && !showSeated) {coinList.add(new CoinSlot("Liberty Seated", "", coinIndex++));}
        if (showOld && !showBarber) {coinList.add(new CoinSlot("Barber", "", coinIndex++));}

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if(showBust){
                if(i==1776){coinList.add(new CoinSlot(year, "Sm Eagle", coinIndex++, getImgId("Draped Bust")));}
                if(i==1804 || i==1805 || i==1806 || i==1807){
                    coinList.add(new CoinSlot(year, "Heraldic Eagle", coinIndex++, getImgId("Draped Bust")));}
                if(i==1815){coinList.add(new CoinSlot(year, "Lg Dia", coinIndex++, getImgId("Capped Bust")));}
                if(i>1817 && i<1829 && i!= 1826){coinList.add(new CoinSlot(year, "Lg Dia", coinIndex++, getImgId("Capped Bust")));}
                if(i>1830 && i<1839){coinList.add(new CoinSlot(year, "Sm Dia", coinIndex++, getImgId("Capped Bust")));}
            }
            if(showSeated){
                if(showP){
                    if(i==1838 || i==1839) {coinList.add(new CoinSlot(year, "No Drapery", coinIndex++, getImgId("Seated")));}
                    if(i>1839 && i<1866 && i!=1854 && i!=1855){coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Seated")));}
                    if(i==1853){coinList.add(new CoinSlot(year, "Arrows&Rays", coinIndex++, getImgId("Seated")));}
                    if(i==1854 || i==1855){coinList.add(new CoinSlot(year, "Arrows", coinIndex++, getImgId("Seated")));}
                    if(i>1865 && i<1892 && i !=1874){coinList.add(new CoinSlot(year, "Motto", coinIndex++, getImgId("Seated")));}
                    if(i==1873 || i==1874){coinList.add(new CoinSlot(year, "Arrows&Motto", coinIndex++, getImgId("Seated")));}
                }
                if(showO){
                    if(i==1840){coinList.add(new CoinSlot(year, "O No Drapery", coinIndex++, getImgId("Seated")));}
                    if(i>1839 && i<1861 && i!=1853 && i!=1854 && i!=1855 && i!=1845 && i!= 1846 && i!=1848) {
                        coinList.add(new CoinSlot(year, "O", coinIndex++, getImgId("Seated")));}
                    if(i==1853){coinList.add(new CoinSlot(year, "O Arrows&Rays", coinIndex++, getImgId("Seated")));}
                    if(i==1854 || i==1855){coinList.add(new CoinSlot(year, "O Arrows", coinIndex++, getImgId("Seated")));}
                    if(i==1891){coinList.add(new CoinSlot(year, "O Motto", coinIndex++, getImgId("Seated")));}
                }
                if (showS){
                    if( i==1855){coinList.add(new CoinSlot(year, "S Arrows", coinIndex++, getImgId("Seated")));}
                    if(i>1855 && i<1866 && i!=1863){coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Seated")));}
                    if(i>1865 && i<1873 && i!=1870){coinList.add(new CoinSlot(year, "S Motto", coinIndex++, getImgId("Seated")));}
                    if(i==1873 || i==1874) {coinList.add(new CoinSlot(year, "S Arrows", coinIndex++, getImgId("Seated")));}
                    if(i>1874 && i<1879){coinList.add(new CoinSlot(year, "S Motto", coinIndex++, getImgId("Seated")));}
                    if(i==1888 || i==1891){coinList.add(new CoinSlot(year, "S Motto", coinIndex++, getImgId("Seated")));}
                }
                if(showCC){
                    if(i>1869 && i<1873){coinList.add(new CoinSlot(year, "CC", coinIndex++, getImgId("Seated")));}
                    if(i==1873){coinList.add(new CoinSlot(year, "CC 5 Known", coinIndex++, getImgId("Seated")));}
                    if(i==1873){coinList.add(new CoinSlot(year, "CC Arrows", coinIndex++, getImgId("Seated")));}
                    if(i>1874 && i<1879){coinList.add(new CoinSlot(year, "CC Motto", coinIndex++, getImgId("Seated")));}
                }
            }

            if (showBarber && i < 1917 && i>1891) {
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Barber")));}
                if (showD && i >= 1906 && i != 1912) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Barber")));}
                if (showS && i != 1904 && i != 1906 && i != 1910 && i != 1916) {
                    coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Barber")));}
                if (showO && i <= 1909) {coinList.add(new CoinSlot(year, "O", coinIndex++, getImgId("Barber")));}
            }
            if (showStanding && i > 1915) {
                if (i == 1917) {
                    if (showP) {
                        coinList.add(new CoinSlot(year, "Type I", coinIndex++, getImgId("Standing Liberty")));
                        coinList.add(new CoinSlot(year, "Type II", coinIndex++, getImgId("Standing Liberty")));}
                    if (showD) {
                        coinList.add(new CoinSlot(year, "D Type I", coinIndex++, getImgId("Standing Liberty")));
                        coinList.add(new CoinSlot(year, "D Type II", coinIndex++, getImgId("Standing Liberty")));}
                    if (showS) {
                        coinList.add(new CoinSlot(year, "S Type I", coinIndex++, getImgId("Standing Liberty")));
                        coinList.add(new CoinSlot(year, "S Type II", coinIndex++, getImgId("Standing Liberty")));}
                } else {
                    if (showP && i!= 1922) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Standing Liberty")));}
                    if (showD && i != 1916 && i != 1921 && i != 1922 && i != 1925 && i != 1923 && i != 1930) {
                        coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Standing Liberty")));}
                    if (showS && i != 1916 && i != 1921 && i!= 1922 && i != 1925) {
                        coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Standing Liberty")));}
                }
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
                                           int oldVersion, int newVersion) {return 0;}
}