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
import com.coincollection.CollectionInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class JeffersonNickels extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Nickels";

    private static final String[] WESTWARD_2004_COIN_IDENTIFIERS = {
            "Peace Medal",
            "Keelboat"
    };

    private static final Integer[][] WESTWARD_2004_IMAGE_IDENTIFIERS = {
            { R.drawable.westward_2004_louisiana_purchase_unc, R.drawable.westward_2004_louisiana_purchase_unc_25},
            { R.drawable.westward_2004_keelboat_unc,           R.drawable.westward_2004_keelboat_unc_25},
    };

    private static final String[] WESTWARD_2005_COIN_IDENTIFIERS = {
            "American Bison",
            "Ocean in View!"
    };

    private static final Integer[][] WESTWARD_2005_IMAGE_IDENTIFIERS = {
            { R.drawable.westward_2005_american_bison_unc, R.drawable.westward_2005_american_bison_unc_25},
            { R.drawable.westward_2005_ocean_in_view_unc, R.drawable.westward_2005_ocean_in_view_unc_25},
    };

    private static final HashMap<String, Integer[]> WESTWARD_INFO = new HashMap<>();

    static {
        // Populate the WESTWARD_INFO HashMap for quick image ID lookups later
        for (int i = 0; i < WESTWARD_2004_COIN_IDENTIFIERS.length; i++){
            WESTWARD_INFO.put(WESTWARD_2004_COIN_IDENTIFIERS[i], WESTWARD_2004_IMAGE_IDENTIFIERS[i]);
        }
        for (int i = 0; i < WESTWARD_2005_COIN_IDENTIFIERS.length; i++){
            WESTWARD_INFO.put(WESTWARD_2005_COIN_IDENTIFIERS[i], WESTWARD_2005_IMAGE_IDENTIFIERS[i]);
        }
    }

    private static final Integer START_YEAR = 1938;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_jefferson_nickel_unc;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_jefferson_nickel_unc;

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(String identifier, String mint, Boolean inCollection){
        if(WESTWARD_INFO.containsKey(identifier)){
            return WESTWARD_INFO.get(identifier)[inCollection ? 0 : 1];
        } else {
            return inCollection ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
        }
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);
    }

    // TODO Perform validation and throw exception
    public void populateCollectionLists(HashMap<String, Object> parameters,
                                        ArrayList<String> identifierList,
                                        ArrayList<String> mintList) {

        Integer startYear       = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear        = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);

        for(int i = startYear; i <= stopYear; i++){

            if(i == 2004){
                // 2004 Jefferson Presidential Nickels
                for(int j = 0; j < WESTWARD_2004_COIN_IDENTIFIERS.length; j++){

                    String identifier = WESTWARD_2004_COIN_IDENTIFIERS[j];

                    if(showMintMarks){
                        if(showP){
                            identifierList.add(identifier);
                            mintList.add("P");
                        }
                        if(showD){
                            identifierList.add(identifier);
                            mintList.add("D");
                        }
                    } else {
                        identifierList.add(identifier);
                        mintList.add("");
                    }
                }
                continue;
            }

            if(i == 2005){
                // 2005 Jefferson Presidential Nickels
                for(int j = 0; j < WESTWARD_2005_COIN_IDENTIFIERS.length; j++){

                    String identifier = WESTWARD_2005_COIN_IDENTIFIERS[j];

                    if(showMintMarks){
                        if(showP){
                            identifierList.add(identifier);
                            mintList.add("P");
                        }
                        if(showD){
                            identifierList.add(identifier);
                            mintList.add("D");
                        }
                    } else {
                        identifierList.add(identifier);
                        mintList.add("");
                    }
                }
                continue;
            }

            if(showMintMarks){
                if(i != 1968 && i != 1969 && i != 1970){
                    if(showP && i >= 1980){
                        identifierList.add(Integer.toString(i));
                        mintList.add("P");
                    } else if(showP){
                        identifierList.add(Integer.toString(i));
                        mintList.add("");
                    }
                }
            } else {
                identifierList.add(Integer.toString(i));
                mintList.add("");
            }

            if(i != 1965 && i != 1966 && i != 1967){
                if(showMintMarks && showD){
                    identifierList.add(Integer.toString(i));
                    mintList.add("D");
                }
            }
            if(i <= 1970 && i != 1950 && (i < 1955 || i > 1967)){
                if(showMintMarks && showS){
                    identifierList.add(Integer.toString(i));
                    mintList.add("S");
                }
            }
        }
    }
    public String getAttributionString(){
        return MainApplication.DEFAULT_ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
                                           int oldVersion, int newVersion) {

        int total = 0;

        if(oldVersion <= 2) {

            // Remove 1955s nickel
            int value = db.delete(tableName, "coinIdentifier=? AND coinMint=?", new String[]{"1955", "S"});
            // Remove 1965-1967 D Nickel
            value += db.delete(tableName, "coinIdentifier=? AND coinMint=?", new String[]{"1965", "D"});
            value += db.delete(tableName, "coinIdentifier=? AND coinMint=?", new String[]{"1966", "D"});
            value += db.delete(tableName, "coinIdentifier=? AND coinMint=?", new String[]{"1967", "D"});

            // We can't add the new identifiers, just delete the old ones
            // TODO What should we do
            //value += db.delete("[" + name + "]", "coinIdentifier=?", new String[] { "2004" });
            //value += db.delete("[" + name + "]", "coinIdentifier=?", new String[] { "2005" });

            total = total - value;
        }

        if(oldVersion <= 3) {
            // Add in new 2013 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2013");
            total += value;
        }

        if (oldVersion <= 4) {
            // Add in new 2014 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2014");
            total += value;
        }

        if (oldVersion <= 6) {
            // Add in new 2015 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2015");
            total += value;
        }

        if (oldVersion <= 7) {
            // Add in new 2016 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2016");
            total += value;
        }

        if (oldVersion <= 8) {
            // Add in new 2017 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2017");
            total += value;
        }

        if (oldVersion <= 11) {
            // Add in new 2018 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2018");
            total += value;
        }

        if (oldVersion <= 12) {
            // Add in new 2019 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2019");
            total += value;
        }

        if (oldVersion <= 13) {
            // Add in new 2020 coins if applicable
            int value = DatabaseHelper.addFromYear(db, tableName, "2020");
            total += value;
        }

        return total;
    }
}
