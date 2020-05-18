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

public class WalkingLibertyHalfDollars extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Walking Liberty Half Dollars";

    private static final Integer START_YEAR = 1916;
    private static final Integer STOP_YEAR = 1947;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_walking_liberty_half;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_walking_liberty_half;

    // https://commons.wikimedia.org/wiki/File:Walking_Liberty_Half_Dollar_1945D_Obverse.png
    // https://commons.wikimedia.org/wiki/File:Walking_Liberty_Half_Dollar_1945D_Reverse.png
    private static final String ATTRIBUTION = "Walking Liberty Half Dollar images courtesy of Brandon Grossardt via Wikimedia";

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
    @SuppressWarnings("ConstantConditions")
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

            if(i == 1922 || i == 1924 || i == 1925 || i == 1926 ||
                    i == 1930 || i == 1931 || i == 1932){
                continue;
            }

            if(showMintMarks){
                if(showP){
                    if( (i < 1923 || i > 1933) ){
                        identifierList.add(Integer.toString(i));
                        mintList.add("");
                    }
                }
                if(showD){
                    if( (i < 1923 || i > 1928) && i != 1933 && i != 1940){
                        if(i == 1917){
                            identifierList.add(Integer.toString(i));
                            mintList.add(" D Obv");

                            identifierList.add(Integer.toString(i));
                            mintList.add(" D Rev");
                        } else {
                            identifierList.add(Integer.toString(i));
                            mintList.add("D");
                        }
                    }
                }
                if(showS){
                    if(i != 1938 && i != 1947){
                        if(i == 1917){
                            identifierList.add(Integer.toString(i));
                            mintList.add(" S Obv");

                            identifierList.add(Integer.toString(i));
                            mintList.add(" S Rev");
                        } else {
                            identifierList.add(Integer.toString(i));
                            mintList.add("S");
                        }
                    }
                }
            } else {
                identifierList.add(Integer.toString(i));
                mintList.add("");
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
