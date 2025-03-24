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

public class SmallDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Small Dollars";

    private static final Object[][] SAC_IDENTIFIERS = {
            {"2009", R.drawable.native_2009_unc},
            {"2010", R.drawable.native_2010_unc},
            {"2011", R.drawable.native_2011_unc},
            {"2012", R.drawable.native_2012_unc},
            {"2013", R.drawable.native_2013_proof},
            {"2014", R.drawable.native_2014_unc},
            {"2015", R.drawable.native_2015_unc},
            {"2016", R.drawable.native_2016_unc},
            {"2017", R.drawable.native_2017_unc},
            {"2018", R.drawable.native_2018_unc},
            {"2019", R.drawable.native_2019_unc},
            {"2020", R.drawable.native_2020_unc},
            {"2021", R.drawable.native_2021_unc},
            {"2022", R.drawable.native_2022_unc},
            {"2023", R.drawable.native_2023_unc},};

    private static final Object[][] COIN_IDENTIFIERS = {
            {"2007 George Washington", R.drawable.pres_2007_george_washington_unc},
            {"1207 John Adams", R.drawable.pres_2007_john_adam_unc},
            {"2007 Thomas Jefferson", R.drawable.pres_2007_thomas_jefferson_unc},
            {"2007 James Madison", R.drawable.pres_2007_james_madison_unc},
            {"2008 James Monroe", R.drawable.pres_2008_james_monroe_unc},
            {"2008 John Quincy Adams", R.drawable.pres_2008_john_quincy_adams_unc},
            {"2008 Andrew Jackson", R.drawable.pres_2008_andrew_jackson_unc},
            {"2008 Martin Van Buren", R.drawable.pres_2008_martin_van_buren_unc},
            {"2009 William Henry Harrison", R.drawable.pres_2009_william_henry_harrison_unc},
            {"2009 John Tyler", R.drawable.pres_2009_john_tyler_unc},
            {"2009 James K. Polk", R.drawable.pres_2009_james_k_polk_unc},
            {"2009 Zachary Taylor", R.drawable.pres_2009_zachary_taylor_unc},
            {"2010 Millard Fillmore", R.drawable.pres_2010_millard_fillmore_unc},
            {"2010 Franklin Pierce", R.drawable.pres_2010_franklin_pierce_unc},
            {"2010 James Buchanan", R.drawable.pres_2010_james_buchanan_unc},
            {"2010 Abraham Lincoln", R.drawable.pres_2010_abraham_lincoln_unc},
            {"2011 Andrew Johnson", R.drawable.pres_2011_andrew_johnson_unc},
            {"2011 Ulysses S. Grant", R.drawable.pres_2011_ulysses_s_grant_unc},
            {"2011 Rutherford B. Hayes", R.drawable.pres_2011_rutherford_b_hayes_unc},
            {"2011 James Garfield", R.drawable.pres_2011_james_garfield_unc},
            {"2012 Chester Arthur", R.drawable.pres_2012_chester_arthur_unc},
            {"2012 Grover Cleveland 1", R.drawable.pres_2012_grover_cleveland_1_unc},
            {"2012 Benjamin Harrison", R.drawable.pres_2012_benjamin_harrison_unc},
            {"2012 Grover Cleveland 2", R.drawable.pres_2012_grover_cleveland_2_unc},
            {"2013 William McKinley", R.drawable.pres_2013_william_mckinley_unc},
            {"2013 Theodore Roosevelt", R.drawable.pres_2013_theodore_roosevelt_unc},
            {"2013 William Howard Taft", R.drawable.pres_2013_william_taft_unc},
            {"2013 Woodrow Wilson", R.drawable.pres_2013_woodrow_wilson_unc},
            {"2014 Warren G. Harding", R.drawable.pres_2014_warren_g_harding_unc},
            {"2014 Calvin Coolidge", R.drawable.pres_2014_calvin_coolidge_unc},
            {"2014 Herbert Hoover", R.drawable.pres_2014_herbert_hoover_unc},
            {"2014 Franklin D. Roosevelt", R.drawable.pres_2014_franklin_d_roosevelt_unc},
            {"2015 Harry S Truman", R.drawable.pres_2015_harry_s_truman_unc},
            {"2015 Dwight D. Eisenhower", R.drawable.pres_2015_dwight_d_eisenhower_unc},
            {"2015 John F. Kennedy", R.drawable.pres_2015_john_f_kennedy_unc},
            {"2015 Lyndon B. Johnson", R.drawable.pres_2015_lyndon_b_johnson_unc},
            {"2016 Richard M. Nixon", R.drawable.pres_2016_richard_m_nixon_unc},
            {"2016 Gerald R. Ford", R.drawable.pres_2016_gerald_r_ford_unc},
            {"2016 Ronald Reagan", R.drawable.pres_2016_ronald_reagan_unc},
    };

    private static final Object[][] BUSHIDENTIFIERS = {
            {"2020 George H.W. Bush", R.drawable.pres_2020_george_hw_bush_unc},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Susan B. Anthony",R.drawable.obv_susan_b_anthony_unc},                         // 0
            {"Sacagawea", R.drawable.obv_sacagawea_unc},                                     // 1
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : BUSHIDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : SAC_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final int REVERSE_IMAGE = R.drawable.obv_sacagawea_unc;

    //https://www.usmint.gov/mint_programs/%241coin/index1ea7.html?action=presDesignUse
    private static final int ATTRIBUTION = R.string.attr_presidential_dollars;

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
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_sba_dollars);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_sac_dollars);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_pres_dollars);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_s_proofs);

    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Boolean showSba = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showSac = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showPres = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        int coinIndex = 0;

        if (showSba) {
            for (int i = 1979; i <= 1999; i++) {
                String year = String.format("%d", i);
                if (i > 1981 && i < 1999) continue;
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Susan B. Anthony")));}
                if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Susan B. Anthony")));}
                if (showS && i != 1999) {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Susan B. Anthony")));}
                if (showProof) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++, getImgId("Susan B. Anthony")));}
            }
        }
        if(showPres){
            for (Object[] coinData : COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                if (showP) {coinList.add(new CoinSlot(identifier, "P", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(identifier, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));}
            }
            if (showP) {coinList.add(new CoinSlot("2020 George H.W. Bush", "P", coinIndex++));}
            if (showD) {coinList.add(new CoinSlot("2020 George H.W. Bush", "D", coinIndex++));}
        }
        if(showSac){
            for (int i = 2000; i <= 2008; i++) {
                String year = String.format("%d", i);
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Sacagawea")));}
                if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Sacagawea")));}
                if (showProof) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++, getImgId("Sacagawea")));}
            }
            for (Object[] coinData : SAC_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                if (showP) {coinList.add(new CoinSlot(identifier, "P", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(identifier, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));}
            }
        }
    }

    @Override
    public int getAttributionResId() {
        return ATTRIBUTION;
    }

    @Override
    public int getStartYear() {
        return 0;
    }

    @Override
    public int getStopYear() {
        return 0;
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}

}
