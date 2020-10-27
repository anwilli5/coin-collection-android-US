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
import com.coincollection.CoinSlot;
import com.spencerpages.MainApplication;
import com.spencerpages.R;
import com.coincollection.CollectionInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class SusanBAnthonyDollars extends CollectionInfo {

    private static final String COLLECTION_TYPE = "Susan B. Anthony Dollars";

    private static final Integer START_YEAR = 1979;
    private static final Integer STOP_YEAR = 1999;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.obv_susan_b_anthony_unc;
    private static final int OBVERSE_IMAGE_MISSING = R.drawable.openslot;

    private static final int REVERSE_IMAGE = R.drawable.rev_susan_b_anthony_unc;

    public String getCoinType() { return COLLECTION_TYPE; }

    public int getCoinImageIdentifier() { return REVERSE_IMAGE; }

    public int getCoinSlotImage(CoinSlot coinSlot){
        return coinSlot.isInCollection() ? OBVERSE_IMAGE_COLLECTED : OBVERSE_IMAGE_MISSING;
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
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear       = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear        = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showMintMarks   = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARKS);
        Boolean showP           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS           = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);

        for(int i = startYear; i <= stopYear; i++){
            if(i > 1981 && i < 1999)
                continue;

            if(showMintMarks){
                // 1979 showed the P mint mark
                if(showP) {
                    if (i >= 1979) {
                        coinList.add(new CoinSlot(Integer.toString(i), "P"));
                    } else {
                        coinList.add(new CoinSlot(Integer.toString(i), ""));
                    }
                }
                if(showD){
                    coinList.add(new CoinSlot(Integer.toString(i), "D"));
                }
                if(showS){
                    if(i != 1999){
                        coinList.add(new CoinSlot(Integer.toString(i), "S"));
                    }
                }
            } else {
                coinList.add(new CoinSlot(Integer.toString(i), ""));
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
            // Remove 1982 Susan B Anthony's
            int value = db.delete(tableName, "coinIdentifier=?", new String[]{"1982"});
            total = total - value;
        }

        return total;
    }
}
