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

import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.DatabaseHelper.runSqlUpdate;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class NationalParkQuarters extends CollectionInfo {

    public static final String COLLECTION_TYPE = "National Park Quarters";

    private static final Object[][] COIN_IDENTIFIERS = {
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
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.parks_2010_grand_canyon_unc;

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
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Boolean showMintMarks = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        int coinIndex = 0;

        for (Object[] parksImageIdentifier : COIN_IDENTIFIERS) {
            String identifier = (String) parksImageIdentifier[0];

            if (showMintMarks) {
                if (showP) {
                    coinList.add(new CoinSlot(identifier, "P", coinIndex++));
                }
                if (showD) {
                    coinList.add(new CoinSlot(identifier, "D", coinIndex++));
                }
            } else {
                coinList.add(new CoinSlot(identifier, "", coinIndex++));
            }
        }
    }

    @Override
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
                                           int oldVersion, int newVersion) {
        String tableName = collectionListInfo.getName();
        int total = 0;

        if (oldVersion <= 2) {
            // Add in 2012 National Park Quarters
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("El Yunque");
            newCoinIdentifiers.add("Chaco Culture");
            newCoinIdentifiers.add("Acadia");
            newCoinIdentifiers.add("Hawaii Volcanoes");
            newCoinIdentifiers.add("Denali");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 3) {
            // Add in 2013 National Park Quarters
            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("White Mountain");
            newCoinIdentifiers.add("Perry's Victory");
            newCoinIdentifiers.add("Great Basin");
            newCoinIdentifiers.add("Fort McHenry");
            newCoinIdentifiers.add("Mount Rushmore");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 4) {
            // Add in 2014 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Great Smoky Mountains");
            newCoinIdentifiers.add("Shenandoah");
            newCoinIdentifiers.add("Arches");
            newCoinIdentifiers.add("Great Sand Dunes");
            newCoinIdentifiers.add("Everglades");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 6) {
            // Add in 2015 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Homestead");
            newCoinIdentifiers.add("Kisatchie");
            newCoinIdentifiers.add("Blue Ridge");
            newCoinIdentifiers.add("Bombay Hook");
            newCoinIdentifiers.add("Saratoga");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 7) {
            // Add in 2016 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Shawnee");
            newCoinIdentifiers.add("Cumberland Gap");
            newCoinIdentifiers.add("Harper's Ferry");
            newCoinIdentifiers.add("Theodore Roosevelt");
            newCoinIdentifiers.add("Fort Moultrie");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 8) {
            // Add in 2017 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Effigy Mounds");
            newCoinIdentifiers.add("Frederick Douglass");
            newCoinIdentifiers.add("Ozark Riverways");
            newCoinIdentifiers.add("Ellis Island");
            newCoinIdentifiers.add("George Rogers Clark");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 10) {

            ContentValues values = new ContentValues();

            // Replace all the ’ characters with ' characters
            values.put(COL_COIN_IDENTIFIER, "Perry's Victory");
            runSqlUpdate(db, tableName, values, COL_COIN_IDENTIFIER + "=?", new String[]{"Perry’s Victory"});
            values.clear();

            values.put(COL_COIN_IDENTIFIER, "Harper's Ferry");
            runSqlUpdate(db, tableName, values, COL_COIN_IDENTIFIER + "=?", new String[]{"Harper’s Ferry"});
            values.clear();
        }

        if (oldVersion <= 11) {
            // Add in 2018 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Pictured Rocks");
            newCoinIdentifiers.add("Apostle Islands");
            newCoinIdentifiers.add("Voyageurs");
            newCoinIdentifiers.add("Cumberland Island");
            newCoinIdentifiers.add("Block Island");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 12) {
            // Add in 2019 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Lowell");
            newCoinIdentifiers.add("American Memorial");
            newCoinIdentifiers.add("War in the Pacific");
            newCoinIdentifiers.add("San Antonio Missions");
            newCoinIdentifiers.add("River of No Return");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 13) {
            // Add in 2020 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("National Park of American Samoa");
            newCoinIdentifiers.add("Weir Farm");
            newCoinIdentifiers.add("Salt River Bay");
            newCoinIdentifiers.add("Marsh-Billings-Rockefeller");
            newCoinIdentifiers.add("Tallgrass Prairie");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        if (oldVersion <= 15) {
            // Add in 2021 National Park Quarters

            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
            newCoinIdentifiers.add("Tuskegee Airmen");

            // Add these coins, mimicking which coinMints the user already has defined
            total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
        }

        return total;
    }
}
