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

public class WashingtonSilver extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Washington Silver Quarters";

    private static final Object[][] STATES_COIN_IDENTIFIERS = {
            {"Delaware 1999", R.drawable.states_1999_delaware_unc},
            {"Pennsylvania 1999", R.drawable.states_1999_pennsylvania_unc},
            {"New Jersey 1999", R.drawable.states_1999_new_jersey_unc},
            {"Georgia 1999", R.drawable.states_1999_georgia_unc},
            {"Connecticut 1999", R.drawable.states_1999_connecticut_unc},
            {"Massachusetts 2000", R.drawable.states_2000_massachusetts},
            {"Maryland 2000", R.drawable.states_2000_maryland_unc},
            {"South Carolina 2000", R.drawable.states_2000_south_carolina_unc},
            {"New Hampshire 2000", R.drawable.states_2000_new_hampshire_unc},
            {"Virginia 2000", R.drawable.states_2000_virginia_unc},
            {"New York 2001", R.drawable.states_2001_new_york_unc},
            {"North Carolina 2001", R.drawable.states_2001_north_carolina_unc},
            {"Rhode Island 2001", R.drawable.states_2001_rhode_island_unc},
            {"Vermont 2001", R.drawable.states_2001_vermont_unc},
            {"Kentucky 2001", R.drawable.states_2001_kentucky_unc},
            {"Tennessee 2002", R.drawable.states_2002_tennessee_unc},
            {"Ohio 2002", R.drawable.states_2002_ohio_unc},
            {"Louisiana 2002", R.drawable.states_2002_louisiana_unc},
            {"Indiana 2002", R.drawable.states_2002_indiana_unc},
            {"Mississippi 2002", R.drawable.states_2002_mississippi_unc},
            {"Illinois 2003", R.drawable.states_2003_illinois_unc},
            {"Alabama 2003", R.drawable.states_2003_alabama_unc},
            {"Maine 2003", R.drawable.states_2003_maine_unc},
            {"Missouri 2003", R.drawable.states_2003_missouri_unc},
            {"Arkansas 2003", R.drawable.states_2003_arkansas_unc},
            {"Michigan 2004", R.drawable.states_2004_michigan_unc},
            {"Florida 2004", R.drawable.states_2004_florida_unc},
            {"Texas 2004", R.drawable.states_2004_texas_unc},
            {"Iowa 2004", R.drawable.states_2004_iowa_unc},
            {"Wisconsin 2004", R.drawable.states_2004_wisconsin_unc},
            {"California 2005", R.drawable.states_2005_california_unc},
            {"Minnesota 2005", R.drawable.states_2005_minnesota_unc},
            {"Oregon 2005", R.drawable.states_2005_oregon_unc},
            {"Kansas 2005", R.drawable.states_2005_kansas_unc},
            {"West Virginia 2005", R.drawable.states_2005_west_virginia_unc},
            {"Nevada 2006", R.drawable.states_2006_nevada_unc},
            {"Nebraska 2006", R.drawable.states_2006_nebraska_unc},
            {"Colorado 2006", R.drawable.states_2006_colorado_unc},
            {"North Dakota 2006", R.drawable.states_2006_north_dakota_unc},
            {"South Dakota 2006", R.drawable.states_2006_south_dakota_unc},
            {"Montana 2007", R.drawable.states_2007_montana_unc},
            {"Washington 2007", R.drawable.states_2007_washington_unc},
            {"Idaho 2007", R.drawable.states_2007_idaho_unc},
            {"Wyoming 2007", R.drawable.states_2007_wyoming_unc},
            {"Utah 2007", R.drawable.states_2007_utah_unc},
            {"Oklahoma 2008", R.drawable.states_2008_oklahoma_unc},
            {"New Mexico 2008", R.drawable.states_2008_new_mexico_unc},
            {"Arizona 2008", R.drawable.states_2008_arizona_unc},
            {"Alaska 2008", R.drawable.states_2008_alaska_unc},
            {"Hawaii 2008", R.drawable.states_2008_hawaii_unc},
    };

    public static final Object[][] DC_AND_TERR_COIN_IDENTIFIERS = {
            {"District of Columbia 2009", R.drawable.states_2009_dc_unc},
            {"Puerto Rico 2009", R.drawable.states_2009_puerto_rico_unc},
            {"Guam 2009", R.drawable.states_2009_guam_unc},
            {"American Samoa 2009", R.drawable.states_2009_american_samoa_unc},
            {"U.S. Virgin Islands 2009", R.drawable.states_2009_virgin_islands_unc},
            {"Northern Mariana Islands 2009", R.drawable.states_2009_northern_mariana_unc},
    };

    private static final Object[][] PARKS_COIN_IDENTIFIERS = {
            {"Hot Springs 2010", R.drawable.parks_2010_hot_springs_unc},
            {"Yellowstone 2010", R.drawable.parks_2010_yellowstone_unc},
            {"Yosemite 2010", R.drawable.parks_2010_yosemite_unc},
            {"Grand Canyon 2010", R.drawable.parks_2010_grand_canyon_unc},
            {"Mt. Hood 2010", R.drawable.parks_2010_mount_hood_unc},
            {"Gettysburg 2011", R.drawable.parks_2011_gettysburg_unc},
            {"Glacier 2011", R.drawable.parks_2011_glacier_unc},
            {"Olympic 2011", R.drawable.parks_2011_olympic_unc},
            {"Vicksburg 2011", R.drawable.parks_2011_vicksburg_unc},
            {"Chickasaw 2011", R.drawable.parks_2011_chickasaw_unc},
            {"El Yunque 2012", R.drawable.parks_2012_el_yunque_unc},
            {"Chaco Culture 2012", R.drawable.parks_2012_chaco_culture_unc},
            {"Acadia 2012", R.drawable.parks_2012_acadia_unc},
            {"Hawaii Volcanoes 2012", R.drawable.parks_2012_hawaii_volcanoes_unc},
            {"Denali 2012", R.drawable.parks_2012_denali_unc},
            {"White Mountain 2013", R.drawable.parks_2013_white_mountain_unc},
            {"Perry's Victory 2013", R.drawable.parks_2013_perrys_victory_unc},
            {"Great Basin 2013", R.drawable.parks_2013_great_basin_unc},
            {"Fort McHenry 2013", R.drawable.parks_2013_fort_mchenry_unc},
            {"Mount Rushmore 2013", R.drawable.parks_2013_mount_rushmore_unc},
            {"Great Smoky Mountains 2014", R.drawable.parks_2014_great_smoky_mountains_unc},
            {"Shenandoah 2014", R.drawable.parks_2014_shenandoah_unc},
            {"Arches 2014", R.drawable.parks_2014_arches_unc},
            {"Great Sand Dunes 2014", R.drawable.parks_2014_great_sand_dunes_unc},
            {"Everglades 2014", R.drawable.parks_2014_everglades_unc},
            {"Homestead 2015", R.drawable.parks_2015_homestead_unc},
            {"Kisatchie 2015", R.drawable.parks_2015_kisatchie_unc},
            {"Blue Ridge 2015", R.drawable.parks_2015_blue_ridge_unc},
            {"Bombay Hook 2015", R.drawable.parks_2015_bombay_hook_unc},
            {"Saratoga 2015", R.drawable.parks_2015_saratoga_unc},
            {"Shawnee 2016", R.drawable.parks_2016_shawnee_unc},
            {"Cumberland Gap 2016", R.drawable.parks_2016_cumberland_gap_unc},
            {"Harper's Ferry 2016", R.drawable.parks_2016_harpers_ferry_unc},
            {"Theodore Roosevelt 2016", R.drawable.parks_2016_theodore_roosevelt_unc},
            {"Fort Moultrie 2016", R.drawable.parks_2016_fort_moultrie_unc},
            {"Effigy Mounds 2017", R.drawable.parks_2017_effigy_mounds_proof},
            {"Frederick Douglass 2017", R.drawable.parks_2017_frederick_douglass_proof},
            {"Ozark Riverways 2017", R.drawable.parks_2017_ozark_riverways_proof},
            {"Ellis Island 2017", R.drawable.parks_2017_ellis_island_proof},
            {"George Rogers Clark 2017", R.drawable.parks_2017_george_rogers_clark_proof},
            {"Pictured Rocks 2018", R.drawable.parks_2018_pictured_rocks_proof},
            {"Pictured Rocks Reverse Proof 2018", R.drawable.parks_2018_pictured_rocks_proof},
            {"Apostle Islands 2018", R.drawable.parks_2018_apostle_islands_proof},
            {"Apostle Islands Reverse Proof 2018", R.drawable.parks_2018_apostle_islands_proof},
            {"Voyageurs 2018", R.drawable.parks_2018_voyageurs_proof},
            {"Voyageurs Reverse Proof 2018", R.drawable.parks_2018_voyageurs_proof},
            {"Cumberland Island 2018", R.drawable.parks_2018_cumberland_island_proof},
            {"Cumberland Island Reverse Proof 2018", R.drawable.parks_2018_cumberland_island_proof},
            {"Block Island 2018", R.drawable.parks_2018_block_island_proof},
            {"Block Island Reverse Proof 2018", R.drawable.parks_2018_block_island_proof},
            {"Lowell 2019", R.drawable.parks_2019_lowell_proof},
            {"American Memorial 2019", R.drawable.parks_2019_american_memorial_proof},
            {"War in the Pacific 2019", R.drawable.parks_2019_war_in_the_pacific_proof},
            {"San Antonio Missions 2019", R.drawable.parks_2019_san_antonio_missions_proof},
            {"River of No Return 2019", R.drawable.parks_2019_river_of_no_return_proof},
            {"National Park of American Samoa 2020", R.drawable.parks_2020_american_samoa_unc},
            {"Weir Farm 2020", R.drawable.parks_2020_weir_farm_unc},
            {"Salt River Bay 2020", R.drawable.parks_2020_salt_river_bay_unc},
            {"Marsh-Billings-Rockefeller 2020", R.drawable.parks_2020_marsh_billings_rockefeller_unc},
            {"Tallgrass Prairie 2020", R.drawable.parks_2020_tallgrass_prairie_unc},
            {"Tuskegee Airmen 2021", R.drawable.parks_2021_tuskegee_airmen_unc},
            {"Crossing The Delaware 2021", R.drawable.rev_2021_crossing_delaware_quarter_unc},
    };
    private static final Object[][] WOMEN_COIN_IDENTIFIERS = {
            {"Maya Angelou 2022", R.drawable.women_2022_maya_angelou_unc},
            {"Dr. Sally Ride 2022", R.drawable.women_2022_sally_ride_unc},
            {"Wilma Mankiller 2022", R.drawable.women_2022_wilma_mankiller_unc},
            {"Nina Otero-Warren 2022", R.drawable.women_2022_nina_otero_warren_unc},
            {"Anna May Wong 2022", R.drawable.women_2022_anna_may_wong_unc},
            {"Bessie Coleman 2023", R.drawable.women_2023_bessie_coleman_unc},
            {"Edith Kanaka'ole 2023", R.drawable.women_2023_edith_kanakaole_unc},
            {"Eleanor Roosevelt 2023", R.drawable.women_2023_eleanor_roosevelt_unc},
            {"Jovita Idar 2023", R.drawable.women_2023_jovita_idar_unc},
            {"Maria Tallchief 2023", R.drawable.women_2023_maria_tallchief_unc},
            {"Rev. Dr. Pauli Murray 2024", R.drawable.women_2024_pauli_murray_unc},
            {"Patsy Takemoto Mink 2024", R.drawable.women_2024_patsy_takemoto_unc},
            {"Dr. Mary Edwards Walker 2024", R.drawable.women_2024_mary_edwards_walker_unc},
            {"Celia Cruz 2024", R.drawable.women_2024_celia_cruz_unc},
            {"Zitkala-Ša 2024", R.drawable.women_2024_zitkala_sa_unc},
            {"Ida B. Wells 2025", R.drawable.women_2025_ida_b_wells_unc},
            {"Juliette Gordon Low 2025", R.drawable.women_2025_juliette_gordon_low_unc},
            {"Dr. Vera Rubin 2025", R.drawable.women_2025_vera_rubin_unc},
            {"Stacey Park Milbern 2025", R.drawable.women_2025_stacey_park_milbern_unc},
            {"Althea Gibson 2025", R.drawable.women_2025_althea_gibson_unc},
    };
    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        for (Object[] coinData : STATES_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : DC_AND_TERR_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : PARKS_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : WOMEN_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }
    private static final int REVERSE_IMAGE =R.drawable.quarter_front_92px;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.quarter_front_92px;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    private static final Integer START_YEAR = 0;
    private static final Integer STOP_YEAR = 0;

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    private static final int ATTRIBUTION = R.string.attr_mint;

    @Override
    public int getAttributionResId() { return ATTRIBUTION;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_classic_quarters);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_states_quarters_proof);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_territories_quarters_proof);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_parks_quarters_proof);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_women_quarters_proof);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);
    }
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Boolean showEagle = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showStates = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showTerr = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showParks = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showWomen = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);

        int coinIndex = 0;

        if (showEagle) {
            for (int i = 1932; i <= 1964; i++) {
                String year = Integer.toString(i);
                if (showP && i != 1933) {coinList.add(new CoinSlot(year, "", coinIndex++));}
                if (showD && i != 1933 && i != 1938) {coinList.add(new CoinSlot(year, "D", coinIndex++));}
                if (showS && i < 1955 && i != 1933 && i != 1934 && i != 1949){
                    coinList.add(new CoinSlot(year, "S", coinIndex++));}
            }
            if (showS){
                coinList.add(new CoinSlot("1776-1976", "S 40% Silver BU", coinIndex++));
                coinList.add(new CoinSlot("1776-1976", "S 40% Silver Proof", coinIndex++));
                for (int i = 1992; i <= 1998; i++) {
                    coinList.add(new CoinSlot(Integer.toString(i), "S Proof", coinIndex++));
                }
            }
        }
        if (showStates) {
            for (Object[] coinData : STATES_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));
            }
        }
        if (showTerr) {
            for (Object[] coinData : DC_AND_TERR_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));
            }
        }
        if (showParks) {
            for (Object[] coinData : PARKS_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));
            }
        }
        if (showWomen) {
            for (Object[] coinData : WOMEN_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "S Proof", coinIndex++));
            }
        }
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}