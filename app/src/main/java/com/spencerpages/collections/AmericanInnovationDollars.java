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

public class AmericanInnovationDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "American Innovation Dollars w/ Proofs";

    private static final Object[][] COIN_IMG_IDS = {
            {"Introductory", R.drawable.innovation_2018_introductory_unc},
            {"Delaware", R.drawable.innovation_2019_delaware_unc},
            {"Pennsylvania", R.drawable.innovation_2019_pennsylvania_unc},
            {"New Jersey", R.drawable.innovation_2019_new_jersey_unc},
            {"Georgia", R.drawable.innovation_2019_georgia_unc},
            {"Connecticut", R.drawable.innovation_2020_connecticut_unc},
            {"Massachusetts", R.drawable.innovation_2020_massachusetts_unc},
            {"Maryland", R.drawable.innovation_2020_maryland_unc},
            {"South Carolina", R.drawable.innovation_2020_south_carolina_unc},
            {"New Hampshire", R.drawable.innovation_2021_new_hampshire_unc},
            {"Virginia", R.drawable.innovation_2021_virginia_unc},
            {"New York", R.drawable.innovation_2021_new_york_unc},
            {"North Carolina", R.drawable.innovation_2021_north_carolina_unc},
            {"Rhode Island", R.drawable.innovation_2022_rhode_island_unc},
            {"Vermont", R.drawable.innovation_2022_vermont_unc},
            {"Kentucky", R.drawable.innovation_2022_kentucky_unc},
            {"Tennessee", R.drawable.innovation_2022_tennessee_unc},
            {"Ohio", R.drawable.innovation_2023_ohio_unc},
            {"Louisiana", R.drawable.innovation_2023_louisiana_unc},
            {"Indiana", R.drawable.innovation_2023_indiana_unc},
            {"Mississippi", R.drawable.innovation_2023_mississippi_unc},
            {"Illinois", R.drawable.innovation_2024_illinois_unc},
            {"Alabama", R.drawable.innovation_2024_alabama_unc},
            {"Maine", R.drawable.innovation_2024_maine_unc},
            {"Missouri", R.drawable.innovation_2024_missouri_unc},
            {"Arkansas", R.drawable.innovation_2025_arkansas_unc},
            {"Michigan", R.drawable.innovation_2025_michigan_unc},
            {"Florida", R.drawable.innovation_2025_florida_unc},
            {"Texas", R.drawable.innovation_2025_texas_unc},
    };

    private static final String[] EIGHTEEN = {
            "Introductory",
    };
    private static final String[] NINETEEN = {
            "Delaware",
            "Pennsylvania",
            "New Jersey",
            "Georgia",
    };
    private static final String[] TWENTY = {
            "Connecticut",
            "Massachusetts",
            "Maryland",
            "South Carolina",
    };
    private static final String[] TWENTY_ONE = {
            "New Hampshire",
            "Virginia",
            "New York",
            "North Carolina",
    };
    private static final String[] TWENTY_TWO = {
            "Rhode Island",
            "Vermont",
            "Kentucky",
            "Tennessee",
    };
    private static final String[] TWENTY_THREE = {
            "Ohio",
            "Louisiana",
            "Indiana",
            "Mississippi",
    };
    private static final String[] TWENTY_FOUR = {
            "Illinois",
            "Alabama",
            "Maine",
            "Missouri",
    };
    private static final String[] TWENTY_FIVE = {
            "Arkansas",
            "Michigan",
            "Florida",
            "Texas",
    };
    private static final int REVERSE_IMAGE = R.drawable.innovation_2018_introductory_unc;

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

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s_proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_rev_proofs);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        boolean showP = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        boolean showD = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        boolean showProof = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        boolean showRProof = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        int coinIndex = 0;

        for (String identifier : EIGHTEEN) {
            String year = Integer.toString(2018);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : NINETEEN) {
            String year = Integer.toString(2019);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : TWENTY) {
            String year = Integer.toString(2020);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : TWENTY_ONE) {
            String year = Integer.toString(2021);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : TWENTY_TWO) {
            String year = Integer.toString(2022);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : TWENTY_THREE) {
            String year = Integer.toString(2023);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : TWENTY_FOUR) {
            String year = Integer.toString(2024);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
        for (String identifier : TWENTY_FIVE) {
            String year = Integer.toString(2025);
            if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
            if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showProof) {coinList.add(new CoinSlot(year, String.format("S%nProof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            if (showRProof) {coinList.add(new CoinSlot(year, String.format("S%nReverse Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
        }
    }

    public int getAttributionResId() {
        return R.string.attr_mint;
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
