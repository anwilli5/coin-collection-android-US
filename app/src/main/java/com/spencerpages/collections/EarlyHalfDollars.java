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

    static final Object[][] COIN_IMG_IDS = {
            {"Flowing Hair", R.drawable.a1795_half_dollar_obv},                     // 0
            {"Draped Bust", R.drawable.a1796_half_dollar_obverse_15_stars},         // 1
            {"Capped Bust", R.drawable.a1834_bust_half_dollar_obverse},             // 2
            {"Seated", R.drawable.a1885_half_dollar_obv},                           // 3
            {"Seated w Arrows", R.drawable.a1873_half_dollar_obverse},              // 4
            {"Barber", R.drawable.obv_barber_half},                                 // 5
            {"Walking Liberty", R.drawable.obv_walking_liberty_half},               // 6
            {"Franklin", R.drawable.obv_franklin_half},                             // 7
            {"Kennedy", R.drawable.obv_kennedy_half_dollar_unc},                    // 8
            {"Kennedy Proof", R.drawable.kennedyproof},                             // 9
            {"Kennedy Reverse Proof", R.drawable.ha2018srevproof},                  // 10
            {"Kennedy Reverse", R.drawable.rev_kennedy_half_dollar_unc},            // 11
            {"Franklin Reverse", R.drawable.rev_franklin_half},                     // 12
            {"Walking Liberty Reverse", R.drawable.rev_walking_liberty_half},       // 13
            {"Barber Reverse", R.drawable.rev_barber_half},                         // 14

    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final Integer START_YEAR = 1794;
    private static final Integer STOP_YEAR = 1891;

    private static final int REVERSE_IMAGE = R.drawable.a1834_bust_half_dollar_obverse;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

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
                if(i==1794 || i== 1795){coinList.add(new CoinSlot(Integer.toString(i),"Flowing Hair", coinIndex++,0));}
                if(i>1795 && i<1808 && i!=1798 && i!=1799 && i!=1800 && i!= 1804){coinList.add(new CoinSlot(Integer.toString(i),"Draped Bust", coinIndex++,1));}
                if(i>1806 && i<1840 && i!=1816){coinList.add(new CoinSlot(Integer.toString(i),"Capped Bust", coinIndex++,2));}
                if(showO && i==1838){coinList.add(new CoinSlot(Integer.toString(i),"Capped Bust",  coinIndex++,2));}
                if(showO && i==1839){coinList.add(new CoinSlot(Integer.toString(i),"Capped Bust",  coinIndex++,2));}
            }
            if(showseated){
                if(showP){
                    if(i>1838 && i<1853){coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,3));}
                    if(i==1853){coinList.add(new CoinSlot(Integer.toString(i),"Arrows&Rays", coinIndex++,4));}
                    if(i==1854 ||i==1855){coinList.add(new CoinSlot(Integer.toString(i),"Arrows", coinIndex++,4));}
                    if(i>1855 && i<1866){coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,3));}
                    if(i==1866){coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,3));}
                    if(i>1865 && i<1873){coinList.add(new CoinSlot(Integer.toString(i),"Motto", coinIndex++,3));}
                    if(i==1873){coinList.add(new CoinSlot(Integer.toString(i),"Motto", coinIndex++,3));
                                coinList.add(new CoinSlot(Integer.toString(i),"Motto&Arrows", coinIndex++,4));}
                    if(i==1874){coinList.add(new CoinSlot(Integer.toString(i),"Motto&Arrows", coinIndex++,4));}
                    if(i>1874 && i<1892){coinList.add(new CoinSlot(Integer.toString(i),"Motto", coinIndex++,3));}
                }
                if(showO){
                    if(i>1839 && i<1854){coinList.add(new CoinSlot(Integer.toString(i),"O", coinIndex++,3));}
                    if(i==1853){coinList.add(new CoinSlot(Integer.toString(i),"O Arrows&Rays", coinIndex++,4));}
                    if(i==1854 ||i==1855){coinList.add(new CoinSlot(Integer.toString(i),"O Arrows", coinIndex++,4));}
                    if(i>1855 && i<1862){coinList.add(new CoinSlot(Integer.toString(i),"O", coinIndex++,3));}
                }
                if(showS){
                    if(i==1855){coinList.add(new CoinSlot(Integer.toString(i),"S Arrows", coinIndex++,4));}
                    if(i>1855 && i<1866){coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,3));}
                    if(i==1866){coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,3));}
                    if(i>1865 && i<1879 && i!=1874){coinList.add(new CoinSlot(Integer.toString(i),"S Motto", coinIndex++,3));}
                    if(i==1873 ||i==1874){coinList.add(new CoinSlot(Integer.toString(i),"S Motto&Arrows", coinIndex++,4));}
                }
                if(showCC){
                    if(i>1869 && i<1874){coinList.add(new CoinSlot(Integer.toString(i),"CC Motto", coinIndex++,3));}
                    if(i==1873 ||i==1874){coinList.add(new CoinSlot(Integer.toString(i),"CC Motto&Arrows", coinIndex++,4));}
                    if(i>1874 && i<1879){coinList.add(new CoinSlot(Integer.toString(i),"CC Motto", coinIndex++,3));}
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







