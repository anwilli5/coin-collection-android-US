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

public class SmallCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Small Cents";


    private static final String[] OLD_COIN_STRS = {
            "Flowing Hair",
            "Liberty Cap",
            "Draped Bust",
            "Capped Bust",
            "Coronet",
            "Young Coronet",
            "Young Braided Hair",
            "Mature Braided Hair",
    };

    private static final String[] BICENT_COIN_STRS = {
            "Early Childhood",
            "Formative Years",
            "Professional Life",
            "Presidency",
    };

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


    private static final Integer START_YEAR = 1856;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int REVERSE_IMAGE = R.drawable.ab1909r;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = null;
        int imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        }
        return (slotImage != null) ? slotImage : REVERSE_IMAGE ;
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
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_eagle_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_indian_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_wheat_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_memorial_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_6, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_6_STRING_ID, R.string.include_shield_cents);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_satin);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_mem_proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6_STRING_ID, R.string.include_w);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showOld = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showEagle = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showIndian = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showWheat = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showMem = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);
        Boolean showShield = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_6);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showSatin = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showW = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_6);

        int coinIndex = 0;

        if (showOld) {
            for (String identifier : OLD_COIN_STRS) {
                coinList.add(new CoinSlot(identifier, "", coinIndex++, getImgId(identifier)));}
        }
        if (showOld && !showEagle) {coinList.add(new CoinSlot("Flying Eagle","", coinIndex++, getImgId("Flying Eagle")));}
        if (showOld && !showIndian) {coinList.add(new CoinSlot("Indian Head","", coinIndex++, getImgId("Indian Head")));}

        for (Integer i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (showEagle && i>1855 && i<1859 ) {
                coinList.add(new CoinSlot(year,"", coinIndex++, getImgId("Flying Eagle")));
            }
            if (showIndian) {
                if (showP) {
                    if (i == 1864) {
                        coinList.add(new CoinSlot("1864", "Copper", coinIndex++, getImgId("Indian Head")));
                        coinList.add(new CoinSlot("1864", "Bronze", coinIndex++, getImgId("Indian Head")));
                        coinList.add(new CoinSlot("1864", "L", coinIndex++, getImgId("Indian Head")));}
                    if (i > 1858 && i < 1910 && i != 1864) {
                        coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Indian Head")));}
                }
                if (showS) {
                    if (i == 1908 || i == 1909) {
                        coinList.add(new CoinSlot(year,"S", coinIndex++, getImgId("Indian Head")));}
                }
            }
            if (showWheat) {
                if (i == 1909) {
                    if (showP) {
                        coinList.add(new CoinSlot( "1909","", coinIndex++, getImgId("Wheat")));
                        coinList.add(new CoinSlot("1909","VDB", coinIndex++, getImgId("Wheat")));}
                    if(showS){
                        coinList.add(new CoinSlot("1909","S", coinIndex++, getImgId("Wheat")));
                        coinList.add(new CoinSlot("1909","S VDB", coinIndex++, getImgId("Wheat")));}
                }
                if (i == 1922 && showD) {
                    coinList.add(new CoinSlot("1922","D", coinIndex++, getImgId("Wheat")));
                    coinList.add(new CoinSlot("1922","No D", coinIndex++, getImgId("Wheat")));}
                if (i == 1943) {
                    if (showP) {coinList.add(new CoinSlot("1943", "", coinIndex++, getImgId("Steel")));}
                    if (showD) {coinList.add(new CoinSlot("1943", "D", coinIndex++, getImgId("Steel")));}
                    if (showS) {coinList.add(new CoinSlot("1943","S", coinIndex++, getImgId("Steel")));}
                }
                if (i > 1909 && i < 1959 && i !=1922 && i != 1943) {
                    if (showP) {coinList.add(new CoinSlot(year,"", coinIndex++, getImgId("Wheat")));}
                    if (showD && i != 1910 && i != 1921 && i != 1923) {
                        coinList.add(new CoinSlot(year,"D", coinIndex++, getImgId("Wheat")));}
                    if (showS && i != 1932 && i != 1933 && i != 1934 && i != 1956 && i != 1957 && i != 1958) {
                        coinList.add(new CoinSlot(year,"S", coinIndex++, getImgId("Wheat")));}
                }
            }
            if (showMem) {
                if (i == 1982) {
                    if (showP) {
                        coinList.add(new CoinSlot(year,String.format("Copper%nLarge Date"), coinIndex++, getImgId("Memorial Copper")));
                        coinList.add(new CoinSlot(year,String.format("Copper%nSmall Date"), coinIndex++, getImgId("Memorial Copper")));
                        coinList.add(new CoinSlot(year,String.format("Zinc%nLarge Date"), coinIndex++, getImgId("Zinc")));
                        coinList.add(new CoinSlot(year,String.format("Zinc%nSmall Date"), coinIndex++, getImgId("Zinc")));
                    }
                    if (showD) {
                        coinList.add(new CoinSlot(year,String.format("D Copper%nLarge Date"), coinIndex++, getImgId("Memorial Copper")));
                        coinList.add(new CoinSlot(year,String.format("D Zinc%nLarge Date"), coinIndex++, getImgId("Zinc")));
                        coinList.add(new CoinSlot(year,String.format("D Zinc%nSmall Date"), coinIndex++, getImgId("Zinc")));
                    }
                } else if (i == 2009) {
                    for (String identifier : BICENT_COIN_STRS) {
                        if (showP) {coinList.add(new CoinSlot(year,String.format("%n%s",identifier), coinIndex++,getImgId(identifier)));}
                        if (showSatin) {coinList.add(new CoinSlot(year,String.format("Satin%n%s",identifier), coinIndex++,getImgId(identifier)));}
                        if (showD) {coinList.add(new CoinSlot(year,String.format("D%n%s",identifier), coinIndex++,getImgId(identifier)));}
                        if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s",identifier), coinIndex++,getImgId(identifier)));}
                        if (showSProof) {coinList.add(new CoinSlot(year,String.format("S Proof%n%s",identifier), coinIndex++,getImgId(identifier)));}
                    }
                }
                if (showP) {
                    if (i > 1958 && i < 1982) {coinList.add(new CoinSlot(year,"", coinIndex++, getImgId("Memorial Copper")));}
                    if (i > 1964 && i < 1968) {coinList.add(new CoinSlot(year,"SMS", coinIndex++, getImgId("Memorial Copper")));}
                    if (i > 1982 && i < 2009) {coinList.add(new CoinSlot(year,"", coinIndex++, getImgId("Zinc")));}
                }
                if (showSatin && i > 2004 && i < 2009){coinList.add(new CoinSlot(year,"Satin", coinIndex++, getImgId("Zinc")));}
                if (showD) {
                    if (i > 1958 && i < 1982 && i != 1965 && i != 1966 && i != 1967){
                        coinList.add(new CoinSlot(year,"D", coinIndex++, getImgId("Memorial Copper")));}
                    if (i > 1982 && i < 2009) {coinList.add(new CoinSlot(year,"D", coinIndex++, getImgId("Zinc")));}
                }
                if (showSatin && i > 2004 && i < 2009){coinList.add(new CoinSlot(year,"D Satin", coinIndex++, getImgId("Zinc")));}
                if (showS && i > 1967 && i < 1975) {coinList.add(new CoinSlot(year,"S", coinIndex++, getImgId("Memorial Copper")));}
                if (showSProof && i > 1958 && i <  1965) {coinList.add(new CoinSlot(year,"Proof", coinIndex++, getImgId("Proof")));}
                if (showSProof && i > 1967 && i<2009) {coinList.add(new CoinSlot(year,"S Proof", coinIndex++, getImgId("Proof")));}
            }
            if (showShield){
                if (showP){
                    if (i > 2009 && i != 2017) {coinList.add(new CoinSlot(year,"",coinIndex++, getImgId("Zinc")));}
                    if (i == 2017) {coinList.add(new CoinSlot(year,"P", coinIndex++, getImgId("Zinc")));}
                }
                if (showSatin && i == 2010){coinList.add(new CoinSlot(year,"Satin", coinIndex++, getImgId("Zinc")));}
                if (showD && i > 2009){coinList.add(new CoinSlot(year,"D", coinIndex++, getImgId("Zinc")));}
                if (showSatin && i == 2010) {coinList.add(new CoinSlot(year,"D Satin", coinIndex++, getImgId("Zinc")));}
                if (showW && i ==2019){
                    coinList.add(new CoinSlot(year,"W", coinIndex++, getImgId("Zinc")));
                    coinList.add(new CoinSlot(year,"W Proof", coinIndex++, getImgId("Proof")));
                    coinList.add(new CoinSlot(year,"W Reverse Proof", coinIndex++, getImgId("Reverse Proof")));
                }
                if(showSProof){
                    if (i>2009){coinList.add(new CoinSlot(year,"S Proof", coinIndex++, getImgId("Proof")));}
                    if ( i == 2018) {coinList.add(new CoinSlot(year,"S Reverse Proof", coinIndex++, getImgId("Reverse Proof")));}
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



