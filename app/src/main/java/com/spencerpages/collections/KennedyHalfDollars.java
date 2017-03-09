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

import com.spencerpages.CoinPageCreator;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class KennedyHalfDollars extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Half-Dollars";

    private static final Integer START_YEAR = 1964;
    private static final Integer STOP_YEAR = CoinPageCreator.STILL_IN_PRODUCTION;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_kennedy_half_dollar_unc;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_kennedy_half_dollar_unc;

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(String identifier, String mint, Boolean inCollection){
        return inCollection ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.SHOW_MINT_MARKS, Boolean.FALSE);
        parameters.put(CoinPageCreator.SHOW_P, Boolean.TRUE);
        parameters.put(CoinPageCreator.SHOW_D, Boolean.FALSE);
    }

    // TODO Perform validation and throw exception
    public void populateCollectionArrays(HashMap<String, Object> parameters,
                                         ArrayList<String> identifierList,
                                         ArrayList<String> mintList) {

        Integer startYear       = (Integer) parameters.get(CoinPageCreator.START_YEAR);
        Integer stopYear        = (Integer) parameters.get(CoinPageCreator.STOP_YEAR);
        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.SHOW_P);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.SHOW_D);

        for(int i = startYear; i <= stopYear; i++){
            String newValue = Integer.toString(i);
            if(i == 1975 || i == 1976){
                newValue = "1776-1976";
            }
            if(i == 1976 && startYear != 1976)
                continue;

            if(showMintMarks){
                if(i < 1968 || i > 1970){
                    if(showP && i >= 1980){
                        identifierList.add(newValue);
                        mintList.add(" P");
                    } else if(showP) {
                        identifierList.add(newValue);
                        mintList.add("");
                    }
                }
            } else {
                identifierList.add(newValue);
                mintList.add("");
            }

            if(i != 1965 && i != 1966 && i != 1967){
                if(showMintMarks && showD){
                    identifierList.add(newValue);
                    mintList.add(" D");
                }
            }
        }
    }
}
