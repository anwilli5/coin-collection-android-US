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

public class BarberHalfDollars extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Barber Half Dollars";

    private static final Integer START_YEAR = 1892;
    private static final Integer STOP_YEAR = 1915;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_barber_half;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_barber_half;

    // https://commons.wikimedia.org/wiki/File:1907-D_Barber_half_obverse.jpg
    // https://commons.wikimedia.org/wiki/File:1907-D_Barber_half_reverse.jpg
    private static final String ATTRIBUTION = "Barber Half Dollar images courtesy of Lost Dutchman Rare Coins via Wikimedia";

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
        parameters.put(CoinPageCreator.OPT_SHOW_O, Boolean.FALSE);
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
        Boolean showO           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_O);

        for(int i =startYear; i <= stopYear; i++){
            if(showMintMarks){
                if(showP){
                    identifierList.add(Integer.toString(i));
                    mintList.add("");
                }
                if(showD){
                    if( i >= 1906 && i != 1909 && i != 1910 && i != 1914){
                        identifierList.add(Integer.toString(i));
                        mintList.add("D");
                    }
                }
                if(showS){
                    identifierList.add(Integer.toString(i));
                    mintList.add("S");
                }
                if(showO){
                    if( i <= 1909 ){
                        identifierList.add(Integer.toString(i));
                        mintList.add("O");
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
