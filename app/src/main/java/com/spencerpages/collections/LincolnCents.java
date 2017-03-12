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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.spencerpages.CoinPageCreator;
import com.spencerpages.DatabaseHelper;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LincolnCents extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Pennies";

    private static final String[] BICENT_COIN_IDENTIFIERS = {
            "Early Childhood",
            "Formative Years",
            "Professional Life",
            "Presidency",
    };

    private static final Integer[][] BICENT_IMAGE_IDENTIFIERS = {
            { R.drawable.bicent_2009_early_childhood_unc,   R.drawable.bicent_2009_early_childhood_unc_25},
            { R.drawable.bicent_2009_formative_years_unc,   R.drawable.bicent_2009_formative_years_unc_25},
            { R.drawable.bicent_2009_professional_life_unc, R.drawable.bicent_2009_professional_life_unc_25},
            { R.drawable.bicent_2009_presidency_unc,        R.drawable.bicent_2009_presidency_unc_25},
    };

    private static final HashMap<String, Integer[]> BICENT_INFO = new HashMap<>();

    static {
        // Populate the BICENT_INFO HashMap for quick image ID lookups later
        for (int i = 0; i < BICENT_COIN_IDENTIFIERS.length; i++){
            BICENT_INFO.put(BICENT_COIN_IDENTIFIERS[i], BICENT_IMAGE_IDENTIFIERS[i]);
        }
    }

    private static final Integer START_YEAR = 1909;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_lincoln_cent_unc;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_lincoln_cent_unc;

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(String identifier, String mint, Boolean inCollection){

        if(BICENT_INFO.containsKey(identifier)){
            return BICENT_INFO.get(identifier)[inCollection ? 0 : 1];
        } else {
            return inCollection ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
        }
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_P, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_D, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_S, Boolean.FALSE);
    }

    // TODO Perform validation and throw exception
    public void populateCollectionLists(HashMap<String, Object> parameters,
                                        ArrayList<String> identifierList,
                                        ArrayList<String> mintList) {

        Integer startYear       = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear        = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_P);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_D);
        Boolean showS           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_S);

        boolean addedVdb = false;

        for(Integer i = startYear; i <= stopYear; i++){

            // Support V.D.B.
            String newValue = Integer.toString(i);
            if(i == 1909 && !addedVdb){
                newValue = "1909 V.D.B";
            }

            if(i == 2009){

                // Add support for 2009 Lincoln Presidential Pennies
                for(int j = 0; j < BICENT_COIN_IDENTIFIERS.length; j++){

                    String bicentIdentifier = BICENT_COIN_IDENTIFIERS[j];
                    if(showMintMarks){
                        if(showP){
                            identifierList.add(bicentIdentifier);
                            mintList.add("");
                        }
                        if(showD){
                            identifierList.add(bicentIdentifier);
                            mintList.add("D");
                        }
                    } else {
                        identifierList.add(bicentIdentifier);
                        mintList.add("");
                    }
                }
                continue;
            }

            if(showMintMarks){
                if(showP){
                    // The P was never on any Pennies
                    identifierList.add(newValue);
                    mintList.add("");
                }
            } else {
                identifierList.add(newValue);
                mintList.add("");
            }

            if(i != 1909 && i != 1910 && i != 1921 && i != 1923 && i != 1965 && i != 1966 && i != 1967){
                if(showMintMarks && showD){
                    identifierList.add(newValue);
                    mintList.add("D");
                }
            }

            if(i <= 1974 && i != 1922 && i != 1932 && i != 1933 && i != 1934 && (i < 1956 || i > 1967)){
                if(showMintMarks && showS){
                    identifierList.add(newValue);
                    mintList.add("S");
                }
            }

            // If we are adding in the VDB, turn this off
            if(i == 1909 && !addedVdb){
                i--;
                addedVdb = true;
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

            // Remove 1921 D Penny
            int value = db.delete(tableName, "coinIdentifier=? AND coinMint=?", new String[]{"1921", "D"});

            // TODO What should we do?
            // We can't add the new identifiers, just delete the old ones
            //value += db.delete("[" + name + "]", "coinIdentifier=?", new String[] { "2009" });

            total = total - value;

        }

        if(oldVersion <= 3) {

            // 1. Bug fix: The bicentennials should not display mint mark "P"
            ContentValues values = new ContentValues();
            values.put("coinMint", "");
            // This shortcut works because pennies never carried the "P" mint mark
            db.update(tableName, values, "coinMint=?", new String[]{"P"});

            // 3. 1909 V.D.B. - Can't do anything since it is in the middle of the collection

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

            return total;
    }
}
