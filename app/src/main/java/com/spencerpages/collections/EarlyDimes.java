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

public class EarlyDimes extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Early Dimes";

    private static final Object[][] COIN_IMG_IDS = {
            {"Draped Bust", R.drawable.a1797drapeddime},                                     // 0
            {"Capped Bust", R.drawable.a1820cappeddime},                                     // 1
            {"Seated No Stars", R.drawable.anostarsdime},                                    // 2
            {"Seated Stars", R.drawable.astarsdime},                                         // 3
            {"Seated Stars&Arrows", R.drawable.astars_arrowsdime},                           // 4
            {"Seated Legend", R.drawable.alegenddime},                                       // 5
            {"Seated Legend&Arrows", R.drawable.alegendarrowsdime},                          // 6
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

    private static final Integer START_YEAR = 1796;
    private static final Integer STOP_YEAR = 1891;

    private static final int REVERSE_IMAGE = R.drawable.anostarsdime;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_draped_bust_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_capped_bust_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_seated_coins);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_o);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string .include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string .include_cc);
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showOld = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showDraped = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showCapped = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showSeated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);

        int coinIndex = 0;

        if (showOld && !showDraped) {coinList.add(new CoinSlot("Draped Bust", "", coinIndex++, getImgId("Draped Bust")));}
        if (showOld && !showCapped) {coinList.add(new CoinSlot("Capped Bust", "", coinIndex++, getImgId("Capped Bust")));}

        for (Integer i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (showDraped && i > 1795 && i < 1798) {
                coinList.add(new CoinSlot(year, "Small Eagle", coinIndex++, getImgId("Draped Bust")));}
            if (showDraped && i > 1797 && i < 1808 && i !=1799 && i != 1806) {
                coinList.add(new CoinSlot(year, "Heraldic Eagle", coinIndex++, getImgId("Draped Bust")));}
            if (showCapped && i==1809) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Capped Bust")));}
            if (showCapped && i==1811) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Capped Bust")));}
            if (showCapped && i==1814) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Capped Bust")));}
            if (showCapped && i>1819 && i<1838  && i!=1826) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Capped Bust")));}
            if (showSeated){
                if (showP){
                    if ( i == 1837){coinList.add(new CoinSlot(year, "No Stars", coinIndex++, getImgId("Seated No Stars")));}
                    if ( i>1837 && i<1860 && i!=1854 && i!= 1855 ){coinList.add(new CoinSlot(year, "Stars", coinIndex++, getImgId("Seated Stars")));}
                    if ( i == 1853 || i == 1854 || i== 1855){coinList.add(new CoinSlot(year, "Arrows", coinIndex++, getImgId("Seated Stars&Arrows")));}
                    if( i > 1859 && i<1892 && i!=1874){coinList.add(new CoinSlot(year, "Legend", coinIndex++, getImgId("Seated Legend")));}
                    if( i==1873 || i==1874){coinList.add(new CoinSlot(year, "Arrows", coinIndex++, getImgId("Seated Legend&Arrows")));}
                }
                if(showO && (i>1837 && i<1861 || i==1891) && i!=1844 && i!=1846 && i!=1847 && i !=1848 && i!=1855){
                    if ( i == 1838){coinList.add(new CoinSlot(year, "O No Stars", coinIndex++, getImgId("Seated No Stars")));}
                    if ( (i > 1838 && i < 1860) && i != 1853 && i !=1854){coinList.add(new CoinSlot(year, "O Stars", coinIndex++, getImgId("Seated Stars")));}
                    if (  i == 1853 || i ==1854){coinList.add(new CoinSlot(year, "O Arrows", coinIndex++, getImgId("Seated Stars&Arrows")));}
                    if (  i == 1860 || i ==1891){coinList.add(new CoinSlot(year, "O Legend", coinIndex++, getImgId("Seated Legend")));}
                }
                if (showS && (i>1855 && i<1892) && i!=1857 && i!=1878 && i!= 1879 && i!=1880 && i!=1881 && i!=1882 && i!=1883) {
                    if(i<1861){coinList.add(new CoinSlot(year, "S Stars", coinIndex++, getImgId("Seated Stars")));}
                    if ( i >1860 && i != 1873 && i != 1874){coinList.add(new CoinSlot(year, "S Legend", coinIndex++, getImgId("Seated Legend")));}
                    if ( i == 1873 || i ==1874){coinList.add(new CoinSlot(year, "S Arrows", coinIndex++, getImgId("Seated Legend&Arrows")));}
                }
                if(showCC) {
                    if (i > 1870 && i < 1879 && i != 1873 && i != 1874) {
                        coinList.add(new CoinSlot(year, "CC Legend", coinIndex++, getImgId("Seated Legend")));}
                    if (i == 1873){
                        coinList.add(new CoinSlot(year, "CC Legend", coinIndex++, getImgId("Seated Legend")));
                        coinList.add(new CoinSlot(year, "CC Arrows",  coinIndex++, getImgId("Seated Legend&Arrows")));}
                    if (i == 1874) {coinList.add(new CoinSlot(year, "CC Arrows", coinIndex++, getImgId("Seated Legend&Arrows")));}
                }
            }
        }
    }
    
    @Override
    public int getAttributionResId() {return R.string.attr_dimes;}

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}