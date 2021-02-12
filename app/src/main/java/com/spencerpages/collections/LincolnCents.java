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

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.DatabaseHelper;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

import static com.coincollection.CoinSlot.COIN_SLOT_WHERE_CLAUSE;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.DatabaseHelper.runSqlDelete;
import static com.coincollection.DatabaseHelper.runSqlUpdate;

public class LincolnCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Pennies";

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

    @Override
    public String getCoinType() { return COLLECTION_TYPE; }

    @Override
    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot){
        Integer[] slotImages = BICENT_INFO.get(coinSlot.getIdentifier());
        boolean inCollection = coinSlot.isInCollection();
        if(slotImages != null){
            return slotImages[inCollection ? 0 : 1];
        } else {
            return inCollection ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
        }
    }

    @Override
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
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear       = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear        = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);

        boolean addedVdb = false;

        for(Integer i = startYear; i <= stopYear; i++){

            // Support V.D.B.
            String newValue = Integer.toString(i);
            if(i == 1909 && !addedVdb){
                newValue = "1909 V.D.B";
            }

            if(i == 2009){

                // Add support for 2009 Lincoln Presidential Pennies
                for (String bicentIdentifier : BICENT_COIN_IDENTIFIERS) {

                    if (showMintMarks) {
                        if (showP) {
                            coinList.add(new CoinSlot(bicentIdentifier, ""));
                        }
                        if (showD) {
                            coinList.add(new CoinSlot(bicentIdentifier, "D"));
                        }
                    } else {
                        coinList.add(new CoinSlot(bicentIdentifier, ""));
                    }
                }
                continue;
            }

            if(showMintMarks){
                if(showP){
                    // The P was never on any Pennies
                    coinList.add(new CoinSlot(newValue, ""));
                }
                if(showD){
                    if(i != 1909 && i != 1910 && i != 1921 && i != 1923 && i != 1965 && i != 1966 && i != 1967){
                        coinList.add(new CoinSlot(newValue, "D"));
                    }
                }
                if(showS){
                    if(i <= 1974 && i != 1922 && i != 1932 && i != 1933 && i != 1934 && (i < 1956 || i > 1967)){
                        coinList.add(new CoinSlot(newValue, "S"));
                    }
                }
            } else {
                coinList.add(new CoinSlot(newValue, ""));
            }

            // If we are adding in the VDB, turn this off
            if(i == 1909 && !addedVdb){
                i--;
                addedVdb = true;
            }
        }
    }

    @Override
    public int getAttributionResId(){
        return R.string.attr_mint;
    }

    @Override
    public int getStartYear() {
        return START_YEAR;
    }

    @Override
    public int getStopYear() {
        return STOP_YEAR;
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {
        String tableName = collectionListInfo.getName();
        int total = 0;

        if(oldVersion <= 2) {

            // Remove 1921 D Penny
            total -= runSqlDelete(db, tableName, COIN_SLOT_WHERE_CLAUSE, new String[]{"1921", "D"});

            // TODO What should we do?
            // We can't add the new identifiers, just delete the old ones
            //total -= runSqlDelete(db, "[" + name + "]", COL_COIN_IDENTIFIER + "=?", new String[] { "2009" });
        }

        if(oldVersion <= 3) {

            // 1. Bug fix: The bicentennials should not display mint mark "P"
            ContentValues values = new ContentValues();
            values.put(COL_COIN_MINT, "");
            // This shortcut works because pennies never carried the "P" mint mark
            runSqlUpdate(db, tableName, values, COL_COIN_MINT + "=?", new String[]{"P"});

            // 3. 1909 V.D.B. - Can't do anything since it is in the middle of the collection

            // Add in new 2013 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2013);
        }

        if (oldVersion <= 4) {
            // Add in new 2014 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2014);
        }

        if (oldVersion <= 6) {
            // Add in new 2015 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2015);
        }

        if (oldVersion <= 7) {
            // Add in new 2016 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2016);
        }

        if (oldVersion <= 8) {
            // Add in new 2017 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2017);
        }

        if (oldVersion <= 11) {
            // Add in new 2018 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2018);
        }

        if (oldVersion <= 12) {
            // Add in new 2019 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2019);
        }

        if (oldVersion <= 13) {
            // Add in new 2020 coins if applicable
            total += DatabaseHelper.addFromYear(db, collectionListInfo, 2020);
        }

        return total;
    }
}
