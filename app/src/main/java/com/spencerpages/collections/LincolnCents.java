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
    private static final Integer STOP_YEAR = CoinPageCreator.STILL_IN_PRODUCTION;

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

        parameters.put(CoinPageCreator.EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.SHOW_MINT_MARKS, Boolean.FALSE);
        parameters.put(CoinPageCreator.SHOW_P, Boolean.TRUE);
        parameters.put(CoinPageCreator.SHOW_D, Boolean.FALSE);
        parameters.put(CoinPageCreator.SHOW_S, Boolean.FALSE);
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
        Boolean showS           = (Boolean) parameters.get(CoinPageCreator.SHOW_S);

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
                            mintList.add(" D");
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
                    mintList.add(" D");
                }
            }

            if(i <= 1974 && i != 1922 && i != 1932 && i != 1933 && i != 1934 && (i < 1956 || i > 1967)){
                if(showMintMarks && showS){
                    identifierList.add(newValue);
                    mintList.add(" S");
                }
            }

            // If we are adding in the VDB, turn this off
            if(i == 1909 && !addedVdb){
                i--;
                addedVdb = true;
            }
        }
    }
}
