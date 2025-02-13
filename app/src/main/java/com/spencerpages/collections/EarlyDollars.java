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


    private static final Object[][] COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.a1795_half_dollar_obv},
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},
            {"Seated", R.drawable.a1885_half_dollar_obv},
            {"Seated ", R.drawable.anostarsdime},
            {"Trade", R.drawable.annc1884_t_1_trade_dollar__judd_1732_},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Flowing Hair", R.drawable.do1795_flowing_hairo},       // 0
            {"Draped Bust", R.drawable.do1799_draped_bust_dollaro},  // 1
            {"Seated Liberty", R.drawable.do1860o},                  // 2
            {"Trade", R.drawable.do1876tradeo},                      // 3
            {"Morgan", R.drawable.obv_morgan_dollar},                // 4
            {"Peace", R.drawable.obv_peace_dollar},                  // 5
            {"Eisenhower", R.drawable.obv_eisenhower_dollar},        // 6
            {"Eagle",R.drawable.obv_american_eagle_unc},             // 7
            {"Liberty Seated Goberecht", R.drawable.anostarsdime},   // 8
    };


    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
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

    @Override
    public int getCoinSlotImage(CoinSlot coinSlot, boolean ignoreImageId) {
        Integer slotImage;
        Integer imageId = coinSlot.getImageId();
        if (!ignoreImageId && (imageId >= 0 && imageId < COIN_IMG_IDS.length)) {
            slotImage = (Integer) COIN_IMG_IDS[imageId][1];
        } else {
            slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        }
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

    @Override
    public Object[][] getImageIds() {return COIN_IMG_IDS;}

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_bust);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_seated);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_trade);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_o);

        // Use the MINT_MARK_2 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_cc);

    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showbust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showseated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showtrade = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);

        int coinIndex = 0;


        for (Integer i = startYear; i <= stopYear; i++) {
            if(showbust){
                if(i==1794 || i==1795){coinList.add(new CoinSlot(Integer.toString(i),"Flowing Hair", coinIndex++,0));}
                if(i>1794 && i<1799){coinList.add(new CoinSlot(Integer.toString(i),"Draped Bust", coinIndex++,1));}
                if(i>1797 && i<1804){coinList.add(new CoinSlot(Integer.toString(i),"Draped Bust Heraldic Eagle",coinIndex++,1));}
                if(i==1804){coinList.add(new CoinSlot(Integer.toString(i),"Draped Bust Rare", coinIndex++,1));}
            }
            if(showseated){
                if (showP) {
                    if (i == 1836) {coinList.add(new CoinSlot(Integer.toString(i),"Gobrecht",coinIndex++,8));}
                    if (i == 1838 || i == 1839) {coinList.add(new CoinSlot(Integer.toString(i),"Gobrecht Proof", coinIndex++,2));}
                    if (i > 1839 && i < 1866 && i != 1858) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,2));}
                    if (i > 1865 && i < 1874) {coinList.add(new CoinSlot(Integer.toString(i),"Motto", coinIndex++,2));}
                }
                if(showO){
                    if(i==1846 || i==1850 || i==1851 || i==1859 || i==1860){coinList.add(new CoinSlot(Integer.toString(i),"O", coinIndex++,2));}
                }
                if(showS){
                    if( i==1859 || i==1870 || i==1872 || i == 1873){coinList.add(new CoinSlot(Integer.toString(i),"S",  coinIndex++,2));}
                }
                if(showCC && i>1869 && i<1874){coinList.add(new CoinSlot(Integer.toString(i),"CC", coinIndex++,2));}
            }
            if(showtrade){
                if (showP) {
                    if (i > 1872 && i < 1878) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,3));}
                    if(i>1878 && i <1886){coinList.add(new CoinSlot(Integer.toString(i),"Proof", coinIndex++,3));}
                }
                if(showS && i>1872 && i<1879){coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,3));}
                if(showCC && i>1872 && i<1879){coinList.add(new CoinSlot(Integer.toString(i),"CC", coinIndex++,3));}
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
}
