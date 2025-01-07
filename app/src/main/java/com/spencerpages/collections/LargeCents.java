package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;


public class LargeCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "LargeCents";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Flowing Hair Chain Reverse", R.drawable.annc_us_1793_1c_flowing_hair_cent},
            {"Flowing Hair Wreath Reverse", R.drawable.annc_us_1793_1c_flowing_hair_cent},
            {"Liberty Cap", R.drawable.a1794_cent_obv_venus_marina},
            {"Draped Bust", R.drawable.a1797cent_obv},
            {"Capped Bust", R.drawable.annc_us_1813_1c_classic_head_cent},
            {"Matron Coronet", R.drawable.a1819centrev},
            {"Young Coronet", R.drawable.a1837_cent_obv},
            {"Petite Braided Hair", R.drawable.a1839},
            {"Mature Braided Hair", R.drawable.a1855},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final int REVERSE_IMAGE = R.drawable.a1819_cent_obv;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    @Override
    public int getStartYear() {return START_YEAR;}

    private static final Integer START_YEAR = 1793;
    private static final Integer STOP_YEAR = 1855;

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_bust);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_coronet);
    }

    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showbust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showcoronet = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);

        int coinIndex = 0;

        for (Integer i = startYear; i <= stopYear; i++) {
            String date = String.format("%2d", i );
            String mdate = String.format("%n%2d", i );
            if (showbust) {
                if (i == 1793) {
                    coinList.add(new CoinSlot("Flowing Hair Chain Reverse", date, coinIndex++));
                    coinList.add(new CoinSlot("Flowing Hair Wreath Reverse", date, coinIndex++));}
                if (i > 1792 && i < 1797) {
                    coinList.add(new CoinSlot("Liberty Cap", mdate, coinIndex++));}
                if (i > 1796 && i < 1808) {
                    coinList.add(new CoinSlot("Draped Bust", mdate, coinIndex++));}
                if (i > 1807 && i < 1815) {
                    coinList.add(new CoinSlot("Capped Bust", mdate, coinIndex++));}
            }
            if (showcoronet) {
                if (i>1815 && i<1836){
                    coinList.add(new CoinSlot("Matron Coronet", date, coinIndex++));}
                if (i>1835 && i<1840){
                    coinList.add(new CoinSlot("Young Coronet", date, coinIndex++));}
                if (i>1838 && i<1844){
                    coinList.add(new CoinSlot("Petite Braided Hair", date, coinIndex++));}
                if (i>1843 && i<1858){
                    coinList.add(new CoinSlot("Mature Braided Hair", date, coinIndex++));}
            }
        }
    }
    private static final int ATTRIBUTION =R.string.attr_wiki;
    @Override
    public int getAttributionResId() {return ATTRIBUTION;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}

