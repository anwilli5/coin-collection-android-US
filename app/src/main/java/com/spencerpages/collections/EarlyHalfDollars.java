package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class EarlyHalfDollars extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Early Half Dollars";


    private static final Object[][] COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.a1795_half_dollar_obv},
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},
            {"Capped Bust", R.drawable.a1834_bust_half_dollar_obverse},
            {"Seated", R.drawable.a1885_half_dollar_obv},
            {"Seated ", R.drawable.a1873_half_dollar_obverse},
    };



    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}

    }


    private static final Integer START_YEAR = 1794;
    private static final Integer STOP_YEAR = 1891;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.a1885_half_dollar_obv;

    private static final int REVERSE_IMAGE = R.drawable.a1834_bust_half_dollar_obverse;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}



    public int getCoinSlotImage(CoinSlot coinSlot) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : OBVERSE_IMAGE_COLLECTED;
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_bust);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_seated);


        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'O' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_o);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string .include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string .include_cc);
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showbust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showseated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showO = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showCC = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);

        int coinIndex = 0;


        for (Integer i = startYear; i <= stopYear; i++) {
            if(showbust){
                if(i==1794 || i== 1795){coinList.add(new CoinSlot("Flowing Hair", String.format("%d", i), coinIndex++));}
                if(i>1795 && i<1808 && i!=1798 && i!=1799 && i!=1800 && i!= 1804){coinList.add(new CoinSlot("Draped Bust", String.format("%d", i), coinIndex++));}
                if(i>1806 && i<1840 && i!=1816){coinList.add(new CoinSlot("Capped Bust", String.format("%d", i), coinIndex++));}
                if(showO && i==1838){coinList.add(new CoinSlot("Capped Bust", String.format("%d O", i), coinIndex++));}
                if(showO && i==1839){coinList.add(new CoinSlot("Capped Bust", String.format("%d O", i), coinIndex++));}
            }
            if(showseated){
                if(showP){
                    if(i>1838 && i<1853){coinList.add(new CoinSlot("Seated", String.format("%d", i), coinIndex++));}
                    if(i==1853){coinList.add(new CoinSlot("Seated ", String.format("%d Arrows&Rays", i), coinIndex++));}
                    if(i==1854 ||i==1855){coinList.add(new CoinSlot("Seated ", String.format("%d Arrows", i), coinIndex++));}
                    if(i>1855 && i<1866){coinList.add(new CoinSlot("Seated", String.format("%d", i), coinIndex++));}
                    if(i==1866){coinList.add(new CoinSlot("Seated", String.format("%d", i), coinIndex++));}
                    if(i>1865 && i<1873){coinList.add(new CoinSlot("Seated", String.format("%d Motto", i), coinIndex++));}
                    if(i==1873){coinList.add(new CoinSlot("Seated", String.format("%d Motto", i), coinIndex++));
                                coinList.add(new CoinSlot("Seated ", String.format("%d Motto&Arrows", i), coinIndex++));}
                    if(i==1874){coinList.add(new CoinSlot("Seated ", String.format("%d Motto&Arrows", i), coinIndex++));}
                    if(i>1874 && i<1892){coinList.add(new CoinSlot("Seated", String.format("%d Motto", i), coinIndex++));}
                }
                if(showO){
                    if(i>1839 && i<1854){coinList.add(new CoinSlot("Seated", String.format("%d O", i), coinIndex++));}
                    if(i==1853){coinList.add(new CoinSlot("Seated ", String.format("%d O Arrows&Rays", i), coinIndex++));}
                    if(i==1854 ||i==1855){coinList.add(new CoinSlot("Seated ", String.format("%d O Arrows", i), coinIndex++));}
                    if(i>1855 && i<1862){coinList.add(new CoinSlot("Seated", String.format("%d O", i), coinIndex++));}
                }
                if(showS){
                    if(i==1855){coinList.add(new CoinSlot("Seated ", String.format("%d S Arrows", i), coinIndex++));}
                    if(i>1855 && i<1866){coinList.add(new CoinSlot("Seated", String.format("%d S", i), coinIndex++));}
                    if(i==1866){coinList.add(new CoinSlot("Seated", String.format("%d S", i), coinIndex++));}
                    if(i>1865 && i<1879 && i!=1874){coinList.add(new CoinSlot("Seated", String.format("%d S Motto", i), coinIndex++));}
                    if(i==1873 ||i==1874){coinList.add(new CoinSlot("Seated ", String.format("%d S Motto&Arrows", i), coinIndex++));}
                }
                if(showCC){
                    if(i>1869 && i<1874){coinList.add(new CoinSlot("Seated", String.format("%d CC Motto", i), coinIndex++));}
                    if(i==1873 ||i==1874){coinList.add(new CoinSlot("Seated ", String.format("%d CC Motto&Arrows", i), coinIndex++));}
                    if(i>1874 && i<1879){coinList.add(new CoinSlot("Seated", String.format("%d CC Motto", i), coinIndex++));}
                }
            }
        }
    }
    @Override
    public int getAttributionResId() {return R.string.attr_EarlyHalfs;}

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}







