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
            {"2023", R.drawable.native_2023_unc},
            {"2024", R.drawable.native_2024_unc},
            {"2025", R.drawable.native_2025_unc},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Susan B. Anthony",R.drawable.obv_susan_b_anthony_unc},
            {"Sacagawea", R.drawable.obv_sacagawea_unc},
            {"George Washington", R.drawable.pres_2007_george_washington_unc},
            {"John Adams", R.drawable.pres_2007_john_adam_unc},
            {"Thomas Jefferson", R.drawable.pres_2007_thomas_jefferson_unc},
            {"James Madison", R.drawable.pres_2007_james_madison_unc},
            {"James Monroe", R.drawable.pres_2008_james_monroe_unc},
            {"John Quincy Adams", R.drawable.pres_2008_john_quincy_adams_unc},
            {"Andrew Jackson", R.drawable.pres_2008_andrew_jackson_unc},
            {"Martin Van Buren", R.drawable.pres_2008_martin_van_buren_unc},
            {"William Henry Harrison", R.drawable.pres_2009_william_henry_harrison_unc},
            {"John Tyler", R.drawable.pres_2009_john_tyler_unc},
            {"James K. Polk", R.drawable.pres_2009_james_k_polk_unc},
            {"Zachary Taylor", R.drawable.pres_2009_zachary_taylor_unc},
            {"Millard Fillmore", R.drawable.pres_2010_millard_fillmore_unc},
            {"Franklin Pierce", R.drawable.pres_2010_franklin_pierce_unc},
            {"James Buchanan", R.drawable.pres_2010_james_buchanan_unc},
            {"Abraham Lincoln", R.drawable.pres_2010_abraham_lincoln_unc},
            {"Andrew Johnson", R.drawable.pres_2011_andrew_johnson_unc},
            {"Ulysses S. Grant", R.drawable.pres_2011_ulysses_s_grant_unc},
            {"Rutherford B. Hayes", R.drawable.pres_2011_rutherford_b_hayes_unc},
            {"James Garfield", R.drawable.pres_2011_james_garfield_unc},
            {"Chester Arthur", R.drawable.pres_2012_chester_arthur_unc},
            {"Grover Cleveland 1", R.drawable.pres_2012_grover_cleveland_1_unc},
            {"Benjamin Harrison", R.drawable.pres_2012_benjamin_harrison_unc},
            {"Grover Cleveland 2", R.drawable.pres_2012_grover_cleveland_2_unc},
            {"William McKinley", R.drawable.pres_2013_william_mckinley_unc},
            {"Theodore Roosevelt", R.drawable.pres_2013_theodore_roosevelt_unc},
            {"William Howard Taft", R.drawable.pres_2013_william_taft_unc},
            {"Woodrow Wilson", R.drawable.pres_2013_woodrow_wilson_unc},
            {"Warren G. Harding", R.drawable.pres_2014_warren_g_harding_unc},
            {"Calvin Coolidge", R.drawable.pres_2014_calvin_coolidge_unc},
            {"Herbert Hoover", R.drawable.pres_2014_herbert_hoover_unc},
            {"Franklin D. Roosevelt", R.drawable.pres_2014_franklin_d_roosevelt_unc},
            {"Harry S Truman", R.drawable.pres_2015_harry_s_truman_unc},
            {"Dwight D. Eisenhower", R.drawable.pres_2015_dwight_d_eisenhower_unc},
            {"John F. Kennedy", R.drawable.pres_2015_john_f_kennedy_unc},
            {"Lyndon B. Johnson", R.drawable.pres_2015_lyndon_b_johnson_unc},
            {"Richard M. Nixon", R.drawable.pres_2016_richard_m_nixon_unc},
            {"Gerald R. Ford", R.drawable.pres_2016_gerald_r_ford_unc},
            {"Ronald Reagan", R.drawable.pres_2016_ronald_reagan_unc},
            {"George H.W. Bush", R.drawable.pres_2020_george_hw_bush_unc},
    };
    private static final String[] SEVEN = {
            "George Washington",
            "John Adams",
            "Thomas Jefferson",
            "James Madison",
    };
    private static final String[] EIGHT = {
            "James Monroe",
            "John Quincy Adams",
            "Andrew Jackson",
            "Martin Van Buren",
    };
    private static final String[] NINE = {
            "William Henry Harrison",
            "John Tyler",
            "James K. Polk",
            "Zachary Taylor",
    };
    private static final String[] TEN = {
            "Millard Fillmore",
            "Franklin Pierce",
            "James Buchanan",
            "Abraham Lincoln",
    };
    private static final String[] ELEVEN = {
            "Andrew Johnson",
            "Ulysses S. Grant",
            "Rutherford B. Hayes",
            "James Garfield",
    };
    private static final String[] TWELVE = {
            "Chester Arthur",
            "Grover Cleveland 1",
            "Benjamin Harrison",
            "Grover Cleveland 2",
    };
    private static final String[] THIRTEEN = {
            "William McKinley",
            "Theodore Roosevelt",
            "William Howard Taft",
            "Woodrow Wilson",
    };
    private static final String[] FOURTEEN = {
            "Warren G. Harding",
            "Calvin Coolidge",
            "Herbert Hoover",
            "Franklin D. Roosevelt",
    };
    private static final String[] FIFTEEN = {
            "Harry S Truman",
            "Dwight D. Eisenhower",
            "John F. Kennedy",
            "Lyndon B. Johnson",
    };
    private static final String[] SIXTEEN = {
            "Richard M. Nixon",
            "Gerald R. Ford",
            "Ronald Reagan",
    };
    private static final String[] TWENTY = {
            "George H.W. Bush",
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
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
        int imageId = coinSlot.getImageId();
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

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        boolean showSba = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_1);
        boolean showSac = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_2);
        boolean showPres = getBooleanParameter(parameters, CoinPageCreator.OPT_CHECKBOX_3);
        boolean showP = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        boolean showD = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        boolean showS = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        boolean showProof = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        int coinIndex = 0;

        if (showSba) {
            for (int i = 1979; i <= 1999; i++) {
                String year = Integer.toString(i);
                if (i > 1981 && i < 1999) continue;
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++, getImgId("Susan B. Anthony")));}
                if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++, getImgId("Susan B. Anthony")));}
                if (showS && i != 1999) {coinList.add(new CoinSlot(year, "S", coinIndex++, getImgId("Susan B. Anthony")));}
                if (showProof) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++, getImgId("Susan B. Anthony")));}
            }
        }
        if(showPres){
            for (String identifier : SEVEN) {
                String year = Integer.toString(2007);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : EIGHT) {
                String year = Integer.toString(2008);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : NINE) {
                String year = Integer.toString(2009);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TEN) {
                String year = Integer.toString(2010);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : ELEVEN) {
                String year = Integer.toString(2011);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWELVE) {
                String year = Integer.toString(2012);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : THIRTEEN) {
                String year = Integer.toString(2013);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : FOURTEEN) {
                String year = Integer.toString(2014);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : FIFTEEN) {
                String year = Integer.toString(2015);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : SIXTEEN) {
                String year = Integer.toString(2016);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTY) {
                String year = Integer.toString(2020);
                if (showP) {coinList.add(new CoinSlot(year, String.format("P%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
        }
        if(showSac){
            for (int i = 2000; i <= 2008; i++) {
                String year =Integer.toString(i) ;
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