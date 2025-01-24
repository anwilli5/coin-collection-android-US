package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ProofSets extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Proof Sets";

    private static final int REVERSE_IMAGE = R.drawable.a24rg;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.a24rg;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    private static final Integer START_YEAR = 1950;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    private static final int ATTRIBUTION =R.string.attr_mint;

    @Override
    public int getAttributionResId() { return ATTRIBUTION;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
        return OBVERSE_IMAGE_COLLECTED;
    }

    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_s_Proofs);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_silver_Proofs);
    }
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showproof = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showsilver= (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        int coinIndex = 0;

        for (int i = startYear; i <= stopYear;  i++) {
            if ( showproof && i > 1967 ){
                coinList.add(new CoinSlot("Proof Set", Integer.toString(i), coinIndex++));
            }
            if ( showsilver && i < 1965) {
                coinList.add(new CoinSlot("Silver Proof Set", Integer.toString(i), coinIndex++));
            }
            if (showsilver && i >1991) {
                coinList.add(new CoinSlot("Silver Proof Set",Integer.toString(i), coinIndex++));
            }
        }
    }
    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}
