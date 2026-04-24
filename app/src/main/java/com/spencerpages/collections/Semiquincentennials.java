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
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Semiquincentennials extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Semiquincentennials";

    private static final Object[][] COIN_IMG_IDS = {
            {"1776-2026 Cent", R.drawable.semiq_2026_penny_obv_unc},
            {"1776-2026 Nickel", R.drawable.semiq_2026_nickel_obv_unc},
            {"Emerging Liberty Dime", R.drawable.semiq_2026_dime_obv_unc},
            {"Mayflower Compact Quarter", R.drawable.semiq_2026_mayflower_compact_unc},
            {"Revolutionary War Quarter", R.drawable.semiq_2026_revolutionary_war_unc},
            {"Declaration of Independence Quarter", R.drawable.semiq_2026_declaration_unc},
            {"U.S. Constitution Quarter", R.drawable.semiq_2026_constitution_unc},
            {"Gettysburg Address Quarter", R.drawable.semiq_2026_gettysburg_unc},
            {"Enduring Liberty Half Dollar", R.drawable.semiq_2026_half_obv_unc},
    };

    private static final int REVERSE_IMAGE = R.drawable.semiq_2026_dime_obv_unc;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public Object[][] getImageIds() {
        return COIN_IMG_IDS;
    }

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = null;
        int imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        }
        return (slotImage != null) ? slotImage : REVERSE_IMAGE;
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.FALSE);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        boolean showMintMarks = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARKS);
        boolean showP = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        boolean showD = getBooleanParameter(parameters, CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        int coinIndex = 0;

        for (Object[] coinData : COIN_IMG_IDS) {
            String identifier = (String) coinData[0];
            int imageId = getImgId(identifier);

            if (showMintMarks) {
                if (showP) {
                    coinList.add(new CoinSlot(identifier, "P", coinIndex++, imageId));
                }
                if (showD) {
                    coinList.add(new CoinSlot(identifier, "D", coinIndex++, imageId));
                }
            } else {
                coinList.add(new CoinSlot(identifier, "", coinIndex++, imageId));
            }
        }
    }

    @Override
    public int getAttributionResId() {
        return R.string.attr_mint;
    }

    @Override
    public int getStartYear() {
        return 0;
    }

    @Override
    public int getStopYear() {
        return 0;
    }

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {
        return 0;
    }
}
