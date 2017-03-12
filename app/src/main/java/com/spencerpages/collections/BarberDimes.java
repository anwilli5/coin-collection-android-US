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

import com.spencerpages.CoinPageCreator;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class BarberDimes extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Barber Dimes";

    private static final Integer START_YEAR = 1892;
    private static final Integer STOP_YEAR = 1916;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_barber_dime;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_barber_dime;

    // https://commons.wikimedia.org/wiki/File:1914_Barber_Dime_NGC_MS64plus_Obverse.png
    // https://commons.wikimedia.org/wiki/File:1914_Barber_Dime_NGC_MS64plus_Reverse.png
    private static final String ATTRIBUTION = "Barber Dime images courtesy of Brandon Grossardt via Wikimedia";

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

        for(int i = startYear; i <= stopYear; i++){

            if(showMintMarks){
                if(showP){
                    identifierList.add(Integer.toString(i));
                    mintList.add("");
                }
                if(showD){
                    if( (i >= 1906 && i <= 1912) || i == 1914 ){
                        identifierList.add(Integer.toString(i));
                        mintList.add("D");
                    }
                }
                if(showS){
                    if(i != 1894){
                        identifierList.add(Integer.toString(i));
                        mintList.add("S");
                    }
                }
                if(showO){
                    if(i != 1904 && i < 1910 ){
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
