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

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class WestPoint extends CollectionInfo {

    public static final String COLLECTION_TYPE = "WestPoint Mint";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"2019 W Lincoln Cent", R.drawable.obv_lincoln_cent_unc},
            {"2019 W Proof Lincoln Cent", R.drawable.lincolnproof_},
            {"2019 W Reverse Proof Lincoln Cent", R.drawable.cerevproof},
            {"2020 W Jefferson Nickel", R.drawable.jeffersonuncirculated},
            {"2020 W Proof Jefferson Nickel", R.drawable.jeffersonproof},
            {"1996 W Roosevelt Dime", R.drawable.obv_roosevelt_dime_unc},
            {"2019 W Lowell Quarter", R.drawable.parks_2019_lowell_proof},
            {"2019 W American Memorial Quarter", R.drawable.parks_2019_american_memorial_proof},
            {"2019 W War in the Pacific Quarter", R.drawable.parks_2019_war_in_the_pacific_proof},
            {"2019 W San Antonio Missions Quarter", R.drawable.parks_2019_san_antonio_missions_proof},
            {"2019 W River of No Return Quarter", R.drawable.parks_2019_river_of_no_return_proof},
            {"2020 W American Samoa Quarter", R.drawable.parks_2020_american_samoa_unc},
            {"2020 W Weir Farm Quarter", R.drawable.parks_2020_weir_farm_unc},
            {"2020 W Salt River Bay Quarter", R.drawable.parks_2020_salt_river_bay_unc},
            {"2020 W Marsh-Billings Quarter", R.drawable.parks_2020_marsh_billings_rockefeller_unc},
            {"2020 W Tallgrass Prairie Quarter", R.drawable.parks_2020_tallgrass_prairie_unc},
            {"2014 W Reverse Proof Kennedy Halve", R.drawable.ha2018srevproof},
            {"2006 W Burnished Silver Eagle", R.drawable.obv_american_eagle_unc},
            {"2007 W Burnished Silver Eagle", R.drawable.obv_american_eagle_unc},
            {"2008 W Burnished Silver Eagle", R.drawable.obv_american_eagle_unc},
            {"2011 W Burnished Silver Eagle", R.drawable.obv_american_eagle_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.ha2018srevproof;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getStartYear() {
        return 0;
    }

    @Override
    public int getStopYear() {return 0;}

    private static final int ATTRIBUTION =R.string.attr_mint;

    @Override
    public int getAttributionResId() { return ATTRIBUTION;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }
    public void getCreationParameters(HashMap<String, Object> parameters) {
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        int coinIndex = 0;

        for (Object[] coinData : COIN_IDENTIFIERS) {
            String identifier = (String) coinData[0] ;
            coinList.add(new CoinSlot(identifier, "", coinIndex++));
        }
    }
    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}



