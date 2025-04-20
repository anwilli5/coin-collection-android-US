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

public class CladQuarters extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Clad Quarters";

    private static final Object[][]  COIN_IMG_IDS = {
            {"Delaware", R.drawable.states_1999_delaware_unc},
            {"Pennsylvania", R.drawable.states_1999_pennsylvania_unc},
            {"New Jersey", R.drawable.states_1999_new_jersey_unc},
            {"Georgia", R.drawable.states_1999_georgia_unc},
            {"Connecticut", R.drawable.states_1999_connecticut_unc},
            {"Massachusetts", R.drawable.states_2000_massachusetts},
            {"Maryland", R.drawable.states_2000_maryland_unc},
            {"South Carolina", R.drawable.states_2000_south_carolina_unc},
            {"New Hampshire", R.drawable.states_2000_new_hampshire_unc},
            {"Virginia", R.drawable.states_2000_virginia_unc},
            {"New York", R.drawable.states_2001_new_york_unc},
            {"North Carolina", R.drawable.states_2001_north_carolina_unc},
            {"Rhode Island", R.drawable.states_2001_rhode_island_unc},
            {"Vermont", R.drawable.states_2001_vermont_unc},
            {"Kentucky", R.drawable.states_2001_kentucky_unc},
            {"Tennessee", R.drawable.states_2002_tennessee_unc},
            {"Ohio", R.drawable.states_2002_ohio_unc},
            {"Louisiana", R.drawable.states_2002_louisiana_unc},
            {"Indiana", R.drawable.states_2002_indiana_unc},
            {"Mississippi", R.drawable.states_2002_mississippi_unc},
            {"Illinois", R.drawable.states_2003_illinois_unc},
            {"Alabama", R.drawable.states_2003_alabama_unc},
            {"Maine", R.drawable.states_2003_maine_unc},
            {"Missouri", R.drawable.states_2003_missouri_unc},
            {"Arkansas", R.drawable.states_2003_arkansas_unc},
            {"Michigan", R.drawable.states_2004_michigan_unc},
            {"Florida", R.drawable.states_2004_florida_unc},
            {"Texas", R.drawable.states_2004_texas_unc},
            {"Iowa", R.drawable.states_2004_iowa_unc},
            {"Wisconsin", R.drawable.states_2004_wisconsin_unc},
            {"California", R.drawable.states_2005_california_unc},
            {"Minnesota", R.drawable.states_2005_minnesota_unc},
            {"Oregon", R.drawable.states_2005_oregon_unc},
            {"Kansas", R.drawable.states_2005_kansas_unc},
            {"West Virginia", R.drawable.states_2005_west_virginia_unc},
            {"Nevada", R.drawable.states_2006_nevada_unc},
            {"Nebraska", R.drawable.states_2006_nebraska_unc},
            {"Colorado", R.drawable.states_2006_colorado_unc},
            {"North Dakota", R.drawable.states_2006_north_dakota_unc},
            {"South Dakota", R.drawable.states_2006_south_dakota_unc},
            {"Montana", R.drawable.states_2007_montana_unc},
            {"Washington", R.drawable.states_2007_washington_unc},
            {"Idaho", R.drawable.states_2007_idaho_unc},
            {"Wyoming", R.drawable.states_2007_wyoming_unc},
            {"Utah", R.drawable.states_2007_utah_unc},
            {"Oklahoma", R.drawable.states_2008_oklahoma_unc},
            {"New Mexico", R.drawable.states_2008_new_mexico_unc},
            {"Arizona", R.drawable.states_2008_arizona_unc},
            {"Alaska", R.drawable.states_2008_alaska_unc},
            {"Hawaii", R.drawable.states_2008_hawaii_unc},
            {"District of Columbia", R.drawable.states_2009_dc_unc},
            {"Puerto Rico", R.drawable.states_2009_puerto_rico_unc},
            {"Guam", R.drawable.states_2009_guam_unc},
            {"American Samoa", R.drawable.states_2009_american_samoa_unc},
            {"U.S. Virgin Islands", R.drawable.states_2009_virgin_islands_unc},
            {"Northern Mariana Islands", R.drawable.states_2009_northern_mariana_unc},
            {"Hot Springs", R.drawable.parks_2010_hot_springs_unc},
            {"Yellowstone", R.drawable.parks_2010_yellowstone_unc},
            {"Yosemite", R.drawable.parks_2010_yosemite_unc},
            {"Grand Canyon", R.drawable.parks_2010_grand_canyon_unc},
            {"Mt. Hood", R.drawable.parks_2010_mount_hood_unc},
            {"Gettysburg", R.drawable.parks_2011_gettysburg_unc},
            {"Glacier", R.drawable.parks_2011_glacier_unc},
            {"Olympic", R.drawable.parks_2011_olympic_unc},
            {"Vicksburg", R.drawable.parks_2011_vicksburg_unc},
            {"Chickasaw", R.drawable.parks_2011_chickasaw_unc},
            {"El Yunque", R.drawable.parks_2012_el_yunque_unc},
            {"Chaco Culture", R.drawable.parks_2012_chaco_culture_unc},
            {"Acadia", R.drawable.parks_2012_acadia_unc},
            {"Hawaii Volcanoes", R.drawable.parks_2012_hawaii_volcanoes_unc},
            {"Denali", R.drawable.parks_2012_denali_unc},
            {"White Mountain", R.drawable.parks_2013_white_mountain_unc},
            {"Perry's Victory", R.drawable.parks_2013_perrys_victory_unc},
            {"Great Basin", R.drawable.parks_2013_great_basin_unc},
            {"Fort McHenry", R.drawable.parks_2013_fort_mchenry_unc},
            {"Mount Rushmore", R.drawable.parks_2013_mount_rushmore_unc},
            {"Great Smoky Mountains", R.drawable.parks_2014_great_smoky_mountains_unc},
            {"Shenandoah", R.drawable.parks_2014_shenandoah_unc},
            {"Arches", R.drawable.parks_2014_arches_unc},
            {"Great Sand Dunes", R.drawable.parks_2014_great_sand_dunes_unc},
            {"Everglades", R.drawable.parks_2014_everglades_unc},
            {"Homestead", R.drawable.parks_2015_homestead_unc},
            {"Kisatchie", R.drawable.parks_2015_kisatchie_unc},
            {"Blue Ridge", R.drawable.parks_2015_blue_ridge_unc},
            {"Bombay Hook", R.drawable.parks_2015_bombay_hook_unc},
            {"Saratoga", R.drawable.parks_2015_saratoga_unc},
            {"Shawnee", R.drawable.parks_2016_shawnee_unc},
            {"Cumberland Gap", R.drawable.parks_2016_cumberland_gap_unc},
            {"Harper's Ferry", R.drawable.parks_2016_harpers_ferry_unc},
            {"Theodore Roosevelt", R.drawable.parks_2016_theodore_roosevelt_unc},
            {"Fort Moultrie", R.drawable.parks_2016_fort_moultrie_unc},
            {"Effigy Mounds", R.drawable.parks_2017_effigy_mounds_proof},
            {"Frederick Douglass", R.drawable.parks_2017_frederick_douglass_proof},
            {"Ozark Riverways", R.drawable.parks_2017_ozark_riverways_proof},
            {"Ellis Island", R.drawable.parks_2017_ellis_island_proof},
            {"George Rogers Clark", R.drawable.parks_2017_george_rogers_clark_proof},
            {"Pictured Rocks", R.drawable.parks_2018_pictured_rocks_proof},
            {"Apostle Islands", R.drawable.parks_2018_apostle_islands_proof},
            {"Voyageurs", R.drawable.parks_2018_voyageurs_proof},
            {"Cumberland Island", R.drawable.parks_2018_cumberland_island_proof},
            {"Block Island", R.drawable.parks_2018_block_island_proof},
            {"Lowell", R.drawable.parks_2019_lowell_proof},
            {"American Memorial", R.drawable.parks_2019_american_memorial_proof},
            {"War in the Pacific", R.drawable.parks_2019_war_in_the_pacific_proof},
            {"San Antonio Missions", R.drawable.parks_2019_san_antonio_missions_proof},
            {"River of No Return", R.drawable.parks_2019_river_of_no_return_proof},
            {"National Park of American Samoa", R.drawable.parks_2020_american_samoa_unc},
            {"Weir Farm", R.drawable.parks_2020_weir_farm_unc},
            {"Salt River Bay", R.drawable.parks_2020_salt_river_bay_unc},
            {"Marsh-Billings-Rockefeller", R.drawable.parks_2020_marsh_billings_rockefeller_unc},
            {"Tallgrass Prairie", R.drawable.parks_2020_tallgrass_prairie_unc},
            {"Tuskegee Airmen", R.drawable.parks_2021_tuskegee_airmen_unc},
            {"Crossing The Delaware", R.drawable.rev_2021_crossing_delaware_quarter_unc},
            {"Maya Angelou", R.drawable.women_2022_maya_angelou_unc},
            {"Dr. Sally Ride", R.drawable.women_2022_sally_ride_unc},
            {"Wilma Mankiller", R.drawable.women_2022_wilma_mankiller_unc},
            {"Nina Otero-Warren", R.drawable.women_2022_nina_otero_warren_unc},
            {"Anna May Wong", R.drawable.women_2022_anna_may_wong_unc},
            {"Bessie Coleman", R.drawable.women_2023_bessie_coleman_unc},
            {"Edith Kanaka'ole", R.drawable.women_2023_edith_kanakaole_unc},
            {"Eleanor Roosevelt", R.drawable.women_2023_eleanor_roosevelt_unc},
            {"Jovita Idar", R.drawable.women_2023_jovita_idar_unc},
            {"Maria Tallchief", R.drawable.women_2023_maria_tallchief_unc},
            {"Rev. Dr. Pauli Murray", R.drawable.women_2024_pauli_murray_unc},
            {"Patsy Takemoto Mink", R.drawable.women_2024_patsy_takemoto_unc},
            {"Dr. Mary Edwards Walker", R.drawable.women_2024_mary_edwards_walker_unc},
            {"Celia Cruz", R.drawable.women_2024_celia_cruz_unc},
            {"Zitkala-Ša", R.drawable.women_2024_zitkala_sa_unc},
            {"Ida B. Wells", R.drawable.women_2025_ida_b_wells_unc},
            {"Juliette Gordon Low", R.drawable.women_2025_juliette_gordon_low_unc},
            {"Dr. Vera Rubin", R.drawable.women_2025_vera_rubin_unc},
            {"Stacey Park Milbern", R.drawable.women_2025_stacey_park_milbern_unc},
            {"Althea Gibson", R.drawable.women_2025_althea_gibson_unc},
    };
    private static final String[] NINETYNINE = {
            "Delaware",
            "Pennsylvania",
            "New Jersey",
            "Georgia",
            "Connecticut",
    };
    private static final String[] OO= {
            "Massachusetts",
            "Maryland",
            "South Carolina",
            "New Hampshire",
            "Virginia",
    };
    private static final String[] OONE = {
            "New York",
            "North Carolina",
            "Rhode Island",
            "Vermont",
            "Kentucky",
    };
    private static final String[] OTWO= {
            "Tennessee",
            "Ohio",
            "Louisiana",
            "Indiana",
            "Mississippi",
    };
    private static final String[] OTHREE= {
            "Illinois",
            "Alabama",
            "Maine",
            "Missouri",
            "Arkansas",
    };
    private static final String[] OFOUR= {
            "Michigan",
            "Florida",
            "Texas",
            "Iowa",
            "Wisconsin",
    };
    private static final String[] OFIVE= {
            "California",
            "Minnesota",
            "Oregon",
            "Kansas",
            "West Virginia",
    };
    private static final String[] OSIX= {
            "Nevada",
            "Nebraska",
            "Colorado",
            "North Dakota",
            "South Dakota",
    };
    private static final String[] OSEVEN= {
            "Montana",
            "Washington",
            "Idaho",
            "Wyoming",
            "Utah",
    };
    private static final String[] OEIGHT= {
            "Oklahoma",
            "New Mexico",
            "Arizona",
            "Alaska",
            "Hawaii",
    };
    private static final String[] ONINE= {
            "District of Columbia",
            "Puerto Rico",
            "Guam",
            "American Samoa",
            "U.S. Virgin Islands",
            "Northern Mariana Islands",
    };
    private static final String[] TEN= {
            "Hot Springs",
            "Yellowstone",
            "Yosemite",
            "Grand Canyon",
            "Mt. Hood",
    };
    private static final String[] ELEVEN= {
            "Gettysburg",
            "Glacier",
            "Olympic",
            "Vicksburg",
            "Chickasaw",
    };
    private static final String[] TWELVE= {
            "El Yunque",
            "Chaco Culture",
            "Acadia",
            "Hawaii Volcanoes",
            "Denali",
    };
    private static final String[] THIRTEEN= {
            "White Mountain",
            "Perry's Victory",
            "Great Basin",
            "Fort McHenry",
            "Mount Rushmore",
    };
    private static final String[] FOURTEEN= {
            "Great Smoky Mountains",
            "Shenandoah",
            "Arches",
            "Great Sand Dunes",
            "Everglades",
    };
    private static final String[] FIFETEEN= {
            "Homestead",
            "Kisatchie",
            "Blue Ridge",
            "Bombay Hook",
            "Saratoga",
    };
    private static final String[] SIXTEEN= {
            "Shawnee",
            "Cumberland Gap",
            "Harper's Ferry",
            "Theodore Roosevelt",
            "Fort Moultrie",
    };
    private static final String[] SEVENTEEN= {
            "Effigy Mounds",
            "Frederick Douglass",
            "Ozark Riverways",
            "Ellis Island",
            "George Rogers Clark",
    };
    private static final String[] EIGHTEEN= {
            "Pictured Rocks",
            "Apostle Islands",
            "Voyageurs",
            "Cumberland Island",
            "Block Island",
    };
    private static final String[] NINETEEN= {
            "Lowell",
            "American Memorial",
            "War in the Pacific",
            "San Antonio Missions",
            "River of No Return",
    };
    private static final String[] TWENTY= {
            "National Park of American Samoa",
            "Weir Farm",
            "Salt River Bay",
            "Marsh-Billings-Rockefeller",
            "Tallgrass Prairie",
    };
    private static final String[] TWENTYONE= {
            "Tuskegee Airmen",
    };
    private static final String[] TWENTYONEA= {
            "Crossing The Delaware",
    };
    private static final String[] TWENTYTWO= {
            "Maya Angelou",
            "Dr. Sally Ride",
            "Wilma Mankiller",
            "Nina Otero-Warren",
            "Anna May Wong",
    };
    private static final String[] TWENTYTHREE= {
            "Bessie Coleman",
            "Edith Kanaka'ole",
            "Eleanor Roosevelt",
            "Jovita Idar",
            "Maria Tallchief",
    };
    private static final String[] TWENTYFOUR= {
            "Rev. Dr. Pauli Murray",
            "Patsy Takemoto Mink",
            "Dr. Mary Edwards Walker",
            "Celia Cruz",
            "Zitkala-Ša",
    };
    private static final String[] TWENTYFIVE= {
            "Ida B. Wells",
            "Juliette Gordon Low",
            "Dr. Vera Rubin",
            "Stacey Park Milbern",
            "Althea Gibson",
    };

    private static final int REVERSE_IMAGE = R.drawable.a98_quarter_reverseby636buster;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    private static final Integer START_YEAR = 0;
    private static final Integer STOP_YEAR = 0;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.quarter_front_92px;

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    private static final int ATTRIBUTION = R.string.attr_quarters;

    @Override
    public int getAttributionResId() { return ATTRIBUTION;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = null;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        }
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}


    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_classic_quarters);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_states_quarters);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_parks_quarters);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_women_quarters);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_w);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_satin);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6_STRING_ID, R.string.include_s_proofs);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Boolean showEagle = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showStates = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showParks = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showWomen = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showW = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSatin = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_6);

        int coinIndex = 0;

        if (showEagle) {
            for (int i = 1965; i <= 1967; i++) {
                String year = Integer.toString(i);
                if (showP) {
                    coinList.add(new CoinSlot(year, "", coinIndex++));
                    coinList.add(new CoinSlot(year, "SMS", coinIndex++));
                }
            }
            for (int i = 1968; i <= 1979; i++) {
                String year = (i == 1976) ? "1776-1976" : Integer.toString(i);
                if (i == 1975){ continue; }
                if (showP) {coinList.add(new CoinSlot(year, "", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++));}
            }
            for (int i = 1980; i <= 1998; i++) {
                String year = Integer.toString(i);
                if (showP) {coinList.add(new CoinSlot(year, "P", coinIndex++));}
                if (showD) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
                if (showProof) {coinList.add(new CoinSlot(year, "S Proof", coinIndex++));}
            }
        }
        if (showStates) {
            for (String identifier : NINETYNINE) {
                String year = Integer.toString(1999);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OO) {
                String year = Integer.toString(2000);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OONE) {
                String year = Integer.toString(2001);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OTWO) {
                String year = Integer.toString(2002);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OTHREE) {
                String year = Integer.toString(2003);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OFOUR) {
                String year = Integer.toString(2004);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OFIVE) {
                String year = Integer.toString(2005);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("P Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OSIX) {
                String year = Integer.toString(2006);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("P Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OSEVEN) {
                String year = Integer.toString(2007);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("P Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : OEIGHT) {
                String year = Integer.toString(2008);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("P Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : ONINE) {
                String year = Integer.toString(2009);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("P Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
        }
        if (showParks) {
            for (String identifier : TEN) {
                String year = Integer.toString(2010);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("P Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showSatin) {coinList.add(new CoinSlot(year,String.format("D Satin%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : ELEVEN) {
                String year = Integer.toString(2011);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWELVE) {
                String year = Integer.toString(2012);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : THIRTEEN) {
                String year = Integer.toString(2013);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : FOURTEEN) {
                String year = Integer.toString(2014);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : FIFETEEN) {
                String year = Integer.toString(2015);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : SIXTEEN) {
                String year = Integer.toString(2016);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : SEVENTEEN) {
                String year = Integer.toString(2017);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : EIGHTEEN) {
                String year = Integer.toString(2018);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : NINETEEN) {
                String year = Integer.toString(2019);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showW) {coinList.add(new CoinSlot(year, String.format("W%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTY) {
                String year = Integer.toString(2020);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showW) {coinList.add(new CoinSlot(year, String.format("W%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTYONE) {
                String year = Integer.toString(2021);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTYONEA) {
                String year = Integer.toString(2021);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
        }
        if (showWomen){
            for (String identifier : TWENTYTWO) {
                String year = Integer.toString(2022);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTYTHREE) {
                String year = Integer.toString(2023);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTYFOUR) {
                String year = Integer.toString(2024);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
            for (String identifier : TWENTYFIVE) {
                String year = Integer.toString(2025);
                if (showP) {coinList.add(new CoinSlot(year,String.format("P%n%s", identifier) , coinIndex++, getImgId(identifier)));}
                if (showD) {coinList.add(new CoinSlot(year, String.format("D%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showS) {coinList.add(new CoinSlot(year, String.format("S%n%s", identifier), coinIndex++, getImgId(identifier)));}
                if (showProof) {coinList.add(new CoinSlot(year, String.format("S Proof%n%s", identifier), coinIndex++, getImgId(identifier)));}
            }
        }
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}


