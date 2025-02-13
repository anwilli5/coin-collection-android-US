package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;

import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;


public class SmallCents extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Small Cents";

    private static final Object[][] OLDCOIN_COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.annc_us_1793_1c_flowing_hair_cent},
            {"Liberty Cap", R.drawable.a1794_cent_obv_venus_marina},
            {"Draped Bust", R.drawable.a1797_cent_obv},
            {"Capped Bust", R.drawable.annc_us_1813_1c_classic_head_cent},
            {"Coronet", R.drawable.a1819_cent_obv},
            {"Young Coronet", R.drawable.a1837_cent_obv},
            {"Young Braided Hair", R.drawable.a1839},
            {"Mature Braided Hair", R.drawable.a1855},

    };
    private static final Object[][] STEEL_COIN_IDENTIFIERS = {
            {"Flying Eagle", R.drawable.a1858_cent_obv},
            {"Indian Head", R.drawable.obv_indian_head_cent},
            {"Wheat", R.drawable.ab1909},
            {"Steel", R.drawable.a1943o},
            {"Memorial", R.drawable.amemorial},
            {"Memorial Zinc", R.drawable.obv_lincoln_cent_unc},
            {"Shield", R.drawable.obv_lincoln_cent_unc},
            {"Proof", R.drawable.lincolnproof_},
            {"Reverse Proof", R.drawable.cerevproof},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Flying Eagle", R.drawable.a1858_cent_obv},                            // 0
            {"Indian Head", R.drawable.obv_indian_head_cent},                       // 1
            {"Wheat", R.drawable.ab1909},                                           // 2
            {"Steel", R.drawable.a1943o},                                           // 3
            {"Memorial Copper", R.drawable.amemorial},                              // 4
            {"Zinc", R.drawable.obv_lincoln_cent_unc},                              // 5
            {"Proof", R.drawable.lincolnproof_},                                    // 6
            {"Reverse Proof", R.drawable.cerevproof},                               // 7
            {"Flowing Hair", R.drawable.annc_us_1793_1c_flowing_hair_cent},         // 8
            {"Liberty Cap", R.drawable.a1794_cent_obv_venus_marina},                // 9
            {"Draped Bust", R.drawable.a1797_cent_obv},                             // 10
            {"Capped Bust", R.drawable.annc_us_1813_1c_classic_head_cent},          // 11
            {"Coronet", R.drawable.a1819_cent_obv},                                 // 12
            {"Young Coronet", R.drawable.a1837_cent_obv},                           // 13
            {"Young Braided Hair", R.drawable.a1839},                               // 14
            {"Mature Braided Hair", R.drawable.a1855},                              // 15
            {"Early Childhood", R.drawable.bicent_2009_early_childhood_unc},        // 16
            {"Formative Years", R.drawable.bicent_2009_formative_years_unc},        // 17
            {"Professional Life", R.drawable.bicent_2009_professional_life_unc},    // 18
            {"Presidency", R.drawable.bicent_2009_presidency_unc},                  // 19
            {"Indian Reverse", R.drawable.rev_indian_head_cent},                    // 20
            {"Wheat Reverse", R.drawable.ab1909r},                                  // 21
            {"Memorial Reverse", R.drawable.rev_lincoln_cent_unc},                  // 22
            {"Shield Reverse", R.drawable.ashieldr},                                // 23
            {"1793 Chain Reverse", R.drawable.a1793chainrev},                       // 24
            {"1794 Reverse", R.drawable.a1794r},                                    // 25
            {"1819 Reverse", R.drawable.a1819r},                                    // 26
            {"1839 Reverse", R.drawable.a1839r},                                    // 27
            {"1858 Reverse", R.drawable.a1858r},                                    // 28
    };

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Childhood", R.drawable.bicent_2009_early_childhood_unc},
            {"Formative", R.drawable.bicent_2009_formative_years_unc},
            {"Professional", R.drawable.bicent_2009_professional_life_unc},
            {"Presidency", R.drawable.bicent_2009_presidency_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : OLDCOIN_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : STEEL_COIN_IDENTIFIERS) {COIN_MAP.put( (String) coinData[0],(Integer) coinData[1]);}
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final Integer START_YEAR = 1856;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int REVERSE_IMAGE = R.drawable.ab1909r;

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
        return (slotImage != null) ? slotImage : (int) STEEL_COIN_IDENTIFIERS[0][1];
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
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_eagle);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_indian);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_wheat);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_memorial);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_6, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_6_STRING_ID, R.string.include_shield_cents);

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_satin);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_MemProofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6_STRING_ID, R.string.include_w);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showold = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showeagle = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showindian = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showwheat = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showmem = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);
        Boolean showshield = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_6);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showsatin = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showw = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_6);


        int coinIndex = 0;

        if (showold) {
            for (Object[] coinData : OLDCOIN_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++));}
        }
        if (showold && !showeagle) {coinList.add(new CoinSlot("Flying Eagle","", coinIndex++));}
        if (showold && !showindian) {coinList.add(new CoinSlot("Indian Head","", coinIndex++));}

        for (Integer i = startYear; i <= stopYear; i++) {
            String phil =String.format("%d", i );
            String den =String.format("%d D", i );
            String satin =String.format("%d Satin", i );
            String satind =String.format("%d D Satin", i );
            if (showeagle && i>1855 && i<1859 ) {
                coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,0));}
            if (showindian) {
                if (showP) {
                    if (i == 1864) {
                        coinList.add(new CoinSlot("1864", "Copper", coinIndex++, 1));
                        coinList.add(new CoinSlot("1864", "Bronze", coinIndex++, 1));
                        coinList.add(new CoinSlot("1864", "L", coinIndex++, 1));
                    }
                    if (i > 1858 && i < 1910 && i != 1864) {
                        coinList.add(new CoinSlot(Integer.toString(i), "", coinIndex++, 1));}
                }
                if (showS) {
                    if (i == 1908 || i == 1909) {
                    coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,1));}
                }
            }
            if (showwheat) {
                if (i == 1909) {
                    if (showP) {
                        coinList.add(new CoinSlot( "1909","", coinIndex++,2));
                        coinList.add(new CoinSlot("1909","VDB", coinIndex++,2));}
                    if(showS){
                        coinList.add(new CoinSlot("1909","S", coinIndex++,2));
                        coinList.add(new CoinSlot("1909","S VDB", coinIndex++,2));}
                }
                if (i == 1922 && showD) {
                    coinList.add(new CoinSlot("1922","D", coinIndex++,2));
                    coinList.add(new CoinSlot("1922","No D", coinIndex++,2));}
                if (i == 1943) {
                    if (showP) {coinList.add(new CoinSlot("1943", "", coinIndex++,3));}
                    if (showD) {coinList.add(new CoinSlot("1943", "D", coinIndex++,3));}
                    if (showS) {coinList.add(new CoinSlot("1943","S", coinIndex++,3));}
                }
                if (i > 1909 && i < 1959 && i !=1922 && i != 1943) {
                    if (showP) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,2));}
                    if (showD && i != 1910 && i != 1921 && i != 1923) {
                        coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,2));}
                    if (showS && i != 1932 && i != 1933 && i != 1934 && i != 1956 && i != 1957 && i != 1958) {
                        coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,2));}
                }
            }
            if (showmem) {
                if (i == 1982) {
                    if (showP) {
                        coinList.add(new CoinSlot(Integer.toString(i),"Copper Large Date", coinIndex++,4));
                        coinList.add(new CoinSlot(Integer.toString(i),"Copper Small Date", coinIndex++,4));
                        coinList.add(new CoinSlot(Integer.toString(i),"Zinc Large Date", coinIndex++,5));
                        coinList.add(new CoinSlot(Integer.toString(i),"Zinc Small Date", coinIndex++,5));
                    }
                    if (showD) {
                        coinList.add(new CoinSlot(Integer.toString(i),"D Copper Large Date", coinIndex++,4));
                        coinList.add(new CoinSlot(Integer.toString(i),"D Zinc Large Date", coinIndex++,5));
                        coinList.add(new CoinSlot(Integer.toString(i),"D Zinc Small Date", coinIndex++,5));
                    }
                } else if (i == 2009) {
                    for (Object[] coinData : COIN_IDENTIFIERS) {
                        String bicentIdentifier = (String) coinData[0];
                        if (showP) {coinList.add(new CoinSlot(bicentIdentifier,phil, coinIndex++));}
                        if (showsatin) {coinList.add(new CoinSlot(bicentIdentifier,satin, coinIndex++));}
                        if (showD) {coinList.add(new CoinSlot(bicentIdentifier,den, coinIndex++));}
                        if (showsatin) {coinList.add(new CoinSlot(bicentIdentifier,satind, coinIndex++));}
                        if (showSProof) {coinList.add(new CoinSlot(bicentIdentifier,String.format("%d S Proof", i ), coinIndex++));}
                    }
                }
                if (showP) {
                    if (i > 1958 && i < 1982) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,4));}
                    if (i > 1964 && i < 1968) {coinList.add(new CoinSlot(Integer.toString(i),"SMS", coinIndex++,4));}
                    if (i > 1982 && i < 2009) {coinList.add(new CoinSlot(Integer.toString(i),"", coinIndex++,5));}
                }
                if (showsatin && i > 2004 && i < 2009){coinList.add(new CoinSlot(Integer.toString(i),"Satin", coinIndex++,5));}
                if (showD) {
                    if (i > 1958 && i < 1982 && i != 1965 && i != 1966 && i != 1967){coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,4));}
                    if (i > 1982 && i < 2009) {coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,5));}
                }
                if (showsatin && i > 2004 && i < 2009){coinList.add(new CoinSlot(Integer.toString(i),"D Satin", coinIndex++,5));}
                if (showS && i > 1967 && i < 1975) {coinList.add(new CoinSlot(Integer.toString(i),"S", coinIndex++,4));}
                if (showSProof && i > 1958 && i <  1965) {coinList.add(new CoinSlot(Integer.toString(i),"Proof", coinIndex++,6));}
                if (showSProof && i > 1967 && i<2009) {coinList.add(new CoinSlot(Integer.toString(i),"S Proof", coinIndex++,6));}
            }
            if (showshield){
                if (showP){
                    if (i > 2009 && i != 2017) {coinList.add(new CoinSlot(Integer.toString(i),"",coinIndex++,5));}
                    if (i == 2017) {coinList.add(new CoinSlot(Integer.toString(i),"P", coinIndex++,5));}
                }
                if ( showsatin && i == 2010){coinList.add(new CoinSlot(Integer.toString(i),"Satin", coinIndex++,5));}
                if (showD && i > 2009){coinList.add(new CoinSlot(Integer.toString(i),"D", coinIndex++,5));}
                if (showsatin && i == 2010) {coinList.add(new CoinSlot(Integer.toString(i),"D Satin", coinIndex++,5));}
                if (showw && i ==2019){
                    coinList.add(new CoinSlot(Integer.toString(i),"W" ,coinIndex++,5));
                    coinList.add(new CoinSlot(Integer.toString(i),"W Proof" ,coinIndex++,6));
                    coinList.add(new CoinSlot(Integer.toString(i),"W Reverse Proof" ,coinIndex++,7));
                }
                if(showSProof){
                    if (i>2009){coinList.add(new CoinSlot(Integer.toString(i),"S Proof", coinIndex++,6));}
                    if ( i == 2018) {coinList.add(new CoinSlot(Integer.toString(i),"S Reverse Proof" ,coinIndex++,7));}
                }
            }
        }
    }
    @Override
    public int getAttributionResId() {return R.string.attr_mint;}

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}

}



