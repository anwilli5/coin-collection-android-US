package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class EarlyDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Early Dollars";

    // Remember not to reorder this list and always add new ones to the end
    private static final Object[][] COIN_IMG_IDS = {
            {"Flowing Hair", R.drawable.a1795_half_dollar_obv},
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},
            {"Liberty Seated", R.drawable.a1885_half_dollar_obv},
            {"Liberty Seated (1836)", R.drawable.anostarsdime},
            {"Trade", R.drawable.annc1884_t_1_trade_dollar__judd_1732_},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IMG_IDS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final Integer START_YEAR = 1794;
    private static final Integer STOP_YEAR = 1885;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.annc1884_t_1_trade_dollar__judd_1732_;

    private static final int REVERSE_IMAGE = R.drawable.annc1884_t_1_trade_dollar__judd_1732_;

    @Override
    public String getCoinType() {
        return COLLECTION_TYPE;
    }

    @Override
    public int getCoinImageIdentifier() {
        return REVERSE_IMAGE;
    }

    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage;
        int imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        } else {
            slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        }
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_bust_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_seated_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_trade_dollars);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_o);

        // Use the MINT_MARK_2 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_cc);
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showbust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showseated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showtrade = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        int coinIndex = 0;

        for (Integer i = startYear; i <= stopYear; i++) {
            if(showbust){
                if(i==1794 || i==1795){coinList.add(new CoinSlot("Flowing Hair", String.format("%d", i), coinIndex++));}
                if(i>1795 && i<1799){coinList.add(new CoinSlot("Draped Bust", String.format("%d Sm Eagle", i), coinIndex++));}
                if(i>1797 && i<1804){coinList.add(new CoinSlot("Draped Bust", String.format("%d Heraldic Eagle", i), coinIndex++));}
                if(i==1804){coinList.add(new CoinSlot("Draped Bust", String.format("%d Rare", i), coinIndex++));}
            }
            if(showseated){
                if(i==1836){coinList.add(new CoinSlot("Liberty Seated", String.format("%d Gobrecht", i), coinIndex++, getImgId("Liberty Seated (1836)")));}
                if(i==1837 || i==1838){coinList.add(new CoinSlot("Liberty Seated", String.format("%d Gobrecht Proofs Only", i), coinIndex++));}
                if(i>1839 && i<1866 && i!= 1858){coinList.add(new CoinSlot("Liberty Seated", String.format("%d", i), coinIndex++));}
                if(i>1865 &&i<1873){coinList.add(new CoinSlot("Liberty Seated", String.format("%d Motto", i), coinIndex++));}
                if(showO){if(i==1846 || i==1850 || i==1851 || i==1859 || i==1860){coinList.add(new CoinSlot("Liberty Seated", String.format("%d O", i), coinIndex++));}}
                if(showS){if(i==1859 ||i==1872){coinList.add(new CoinSlot("Liberty Seated", String.format("%d S", i), coinIndex++));}}
                if(showCC){if( i>1869 && i<1874){coinList.add(new CoinSlot("Liberty Seated", String.format("%d CC", i), coinIndex++));}}
            }
            if(showtrade){
                if(i>1872 && i<1878){coinList.add(new CoinSlot("Trade", String.format("%d", i), coinIndex++));}
                if(showS){if(i>1872 && i<1879){coinList.add(new CoinSlot("Trade", String.format("%d S", i), coinIndex++));}}
                if(showCC){if(i>1872 && i<1879){coinList.add(new CoinSlot("Trade", String.format("%d CC", i), coinIndex++));}}
                if(i>1878 && i <1886){coinList.add(new CoinSlot("Trade", String.format("%d Proofs Only", i), coinIndex++));}
            }
        }
    }

    @Override
    public int getAttributionResId() {
        return R.string.attr_EarlyHalfs;
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
        return 0;
    }

    @Override
    public Object[][] getImageIds() {
        return COIN_IMG_IDS;
    }
}
