package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;


public class HalfCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Half Cents";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Liberty Cap 1793", R.drawable.annc_us_1793__c_liberty_cap_half_cent},
            {"Liberty Cap", R.drawable.annc_us_1794__c_liberty_cap_half_cent},
            {"Draped Bust", R.drawable.annc_us_1806__c_draped_bust_half_cent},
            {"Capped Bust", R.drawable.annc_us_1828__c_classic_head_half_cent__proof_},
            {"Braided Hair", R.drawable.annc_us_1844__c_braided_hair_half_cent__proof_},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final int REVERSE_IMAGE = R.drawable.annc_us_1793__c_liberty_cap_half_cent;

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
    private static final Integer STOP_YEAR = 1857;

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
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

        for (Integer i = startYear; i <= stopYear; i++) {
            if (i == 1793) {coinList.add(new CoinSlot("Liberty Cap 1793", "", coinIndex++));}
            if (i > 1793 && i < 1798) {coinList.add(new CoinSlot("Liberty Cap",String.format("%d", i), coinIndex++));}
            if (i > 1799 && i < 1809 && i!=1801) {coinList.add(new CoinSlot("Draped Bust",String.format("%d", i), coinIndex++));}
            if (i>1808 && i<1812) {coinList.add(new CoinSlot("Capped Bust",String.format("%d", i), coinIndex++));}
            if (i>1824 && i<1836 && i!=1827 && i!= 1830) {coinList.add(new CoinSlot("Capped Bust",String.format("%d", i), coinIndex++));}
            if (i==1836) {coinList.add(new CoinSlot("Capped Bust",String.format("%d Proof", i), coinIndex++));}
            if(i>1839 && i<1849){coinList.add(new CoinSlot("Braided Hair",String.format("%d Proof", i), coinIndex++));}
            if(i>1848 && i<1858 && i!=1852) {coinList.add(new CoinSlot("Braided Hair",String.format("%d", i), coinIndex++));}
            if(i==1852){coinList.add(new CoinSlot("Braided Hair",String.format("%d Proof", i), coinIndex++));}
        }
    }
    private static final int ATTRIBUTION =R.string.attr_halfcents;
    @Override
    public int getAttributionResId() {return ATTRIBUTION;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}

