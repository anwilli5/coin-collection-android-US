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

public class BuffaloNickels extends CollectionInfo{

    private static final String COLLECTION_TYPE = "Buffalo Nickels";

    private static final Integer START_YEAR = 1913;
    private static final Integer STOP_YEAR = 1938;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.buffalo_nickel_front;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.buffalo_nickel_back;

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

        for(int i = startYear; i <= stopYear; i++){

            if(i == 1922 || i == 1932 || i == 1933){
                continue;
            }

            if(showMintMarks){
                if(showP){
                    if(i != 1931 && i != 1938){
                        if(i == 1913){
                            identifierList.add(Integer.toString(i));
                            mintList.add(" Type 1");

                            identifierList.add(Integer.toString(i));
                            mintList.add(" Type 2");
                        } else {
                            identifierList.add(Integer.toString(i));
                            mintList.add("");
                        }
                    }
                }
                if(showD){
                    if(i != 1921 && i != 1923 && i != 1930 && i != 1931){
                        if(i == 1913){
                            identifierList.add(Integer.toString(i));
                            mintList.add(" D Type 1");

                            identifierList.add(Integer.toString(i));
                            mintList.add(" D Type 2");
                        } else {
                            identifierList.add(Integer.toString(i));
                            mintList.add("D");
                        }
                    }
                }
                if(showS){
                    if(i != 1934 && i != 1938){
                        if(i == 1913){
                            identifierList.add(Integer.toString(i));
                            mintList.add(" S Type 1");

                            identifierList.add(Integer.toString(i));
                            mintList.add(" S Type 2");
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
}
