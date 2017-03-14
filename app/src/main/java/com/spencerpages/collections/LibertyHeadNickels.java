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
import com.spencerpages.R;
import com.coincollection.CollectionInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class LibertyHeadNickels extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Liberty Head Nickels";

    private static final Integer START_YEAR = 1883;
    private static final Integer STOP_YEAR = 1912;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_liberty_head_nickel;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_liberty_head_nickel;

    // https://commons.wikimedia.org/wiki/File:Liberty_Head_Nickel_1883_NoCents_Obverse.png
    // https://commons.wikimedia.org/wiki/File:Liberty_Head_Nickel_1883_NoCents_Reverse.png
    private static final String ATTRIBUTION = "Liberty Head Nickels images courtesy of Brandon Grossardt via Wikimedia";

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(String identifier, String mint, Boolean inCollection){
        return inCollection ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
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

        boolean added1883WithCents = false;
        for(int i = startYear; i <= stopYear; i++){

            String newValue = Integer.toString(i);

            if(i == 1883){
                if(!added1883WithCents){
                    newValue = "1883 w/ Cents";
                } else {
                    newValue = "1883 w/o Cents";
                }
            }

            if(showMintMarks){

                if(showP){
                    identifierList.add(newValue);
                    mintList.add("");
                }

                if(i == 1912){
                    if(showD){
                        identifierList.add(newValue);
                        mintList.add("D");
                    }
                    if(showS){
                        identifierList.add(newValue);
                        mintList.add("S");
                    }
                }
            } else {
                identifierList.add(newValue);
                mintList.add("");
            }

            if(i == 1883 && !added1883WithCents){
                added1883WithCents = true;
                i--;
            }
        }
    }

    public String getAttributionString(){
        return ATTRIBUTION;
    }

    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, String tableName,
                                           int oldVersion, int newVersion) {
        return 0;
    }
}
