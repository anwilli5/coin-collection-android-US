package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MintSets extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Mint Sets";

    private static final int REVERSE_IMAGE = R.drawable.a24rj;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.a24rj;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    private static final Integer START_YEAR = 1959;
    private static final Integer STOP_YEAR = 2024;

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
    }
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);

        int coinIndex = 0;

        for (int i = startYear; i <= stopYear; i++) {
            if ( i == 1965 ){coinList.add(new CoinSlot(Integer.toString(i), "Special Mint Set", coinIndex++));}
            if ( i == 1966 ) {coinList.add(new CoinSlot(Integer.toString(i), "Special Mint Set", coinIndex++));}
            if ( i == 1967 ) {coinList.add(new CoinSlot(Integer.toString(i), "Special Mint Set", coinIndex++));}
            else  {coinList.add(new CoinSlot(Integer.toString(i), "", coinIndex++));}
        }
    }
    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}
