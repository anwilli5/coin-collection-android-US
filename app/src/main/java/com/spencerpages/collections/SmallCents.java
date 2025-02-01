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
            {"Memorial", R.drawable.ab1909},
            {"Memorial Zinc", R.drawable.obv_lincoln_cent_unc},
            {"Shield", R.drawable.obv_lincoln_cent_unc},
            {"Proof", R.drawable.lincolnproof_},
    };

    private static final Object[][] A1982_COIN_IDENTIFIERS = {
            {"Copper Large Date 1982", R.drawable.ab1909},
            {"Copper Small Date 1982", R.drawable.ab1909},
            {"Zinc Large Date 1982", R.drawable.obv_lincoln_cent_unc},
            {"Zinc Small Date 1982", R.drawable.obv_lincoln_cent_unc},
            {"Copper Large Date 1982 D", R.drawable.ab1909},
            {"Zinc Large Date 1982 D", R.drawable.obv_lincoln_cent_unc},
            {"Zinc Small Date 1982 D", R.drawable.obv_lincoln_cent_unc},
    };

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Early Childhood", R.drawable.bicent_2009_early_childhood_unc},
            {"Formative Years", R.drawable.bicent_2009_formative_years_unc},
            {"Professional Life", R.drawable.bicent_2009_professional_life_unc},
            {"Presidency", R.drawable.bicent_2009_presidency_unc},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : OLDCOIN_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : STEEL_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : A1982_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
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
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) STEEL_COIN_IDENTIFIERS[0][1];
    }

    @Override
    public void getCreationParameters(HashMap<String, Object> parameters) {

        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, Boolean.TRUE);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_eagle_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_indian_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_wheat_cents);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_memorial_cents);

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
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_mem_proofs);
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
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showsatin = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        int coinIndex = 0;

        if (showold) {
            for (Object[] coinData : OLDCOIN_COIN_IDENTIFIERS) {
                String identifier = (String) coinData[0];
                coinList.add(new CoinSlot(identifier, "", coinIndex++));}
        }
        if (showold && !showeagle) {coinList.add(new CoinSlot("Flying Eagle","", coinIndex++));}
        if (showold && !showindian) {coinList.add(new CoinSlot("Indian Head","", coinIndex++));}

        for (Integer i = startYear; i <= stopYear; i++) {
            String phil =String.format("%n%d ", i );
            String den =String.format("%n%d D ", i );
            String sf =String.format("%n%d S ", i );
            String satin =String.format("%n%d Satin", i );
            String satind =String.format("%n%d D Satin", i );
            String sms =String.format("%n%d SMS", i );
            if (showeagle && i>1855 && i<1859 ) {
                coinList.add(new CoinSlot("Flying Eagle", phil, coinIndex++));}
            if (showindian) {
                if (showP){
                    if (i == 1864) {
                        coinList.add(new CoinSlot("Indian Head",String.format("%n%d Copper", i ), coinIndex++));
                        coinList.add(new CoinSlot("Indian Head",String.format("%n%d Bronze", i ), coinIndex++));
                        coinList.add(new CoinSlot("Indian Head",String.format("%n%d L", i ), coinIndex++));
                    }
                    if ( i >1858 && i <1910 && i !=1864)
                        coinList.add(new CoinSlot("Indian Head",phil, coinIndex++));
                }
                if (showS) {
                    if (i == 1908 || i == 1909) {
                        coinList.add(new CoinSlot("Indian Head",sf, coinIndex++));
                    }
                }
            }
            if (showwheat) {
                if (i == 1909) {
                    if (showP) {
                        coinList.add(new CoinSlot("Wheat",phil, coinIndex++));
                        coinList.add(new CoinSlot("Wheat", String.format("%n%d VDB ", i), coinIndex++));}
                    if(showS){
                        coinList.add(new CoinSlot("Wheat",sf, coinIndex++));
                        coinList.add(new CoinSlot("Wheat",String.format("%n%d S VDB ", i ), coinIndex++));}
                }
                if (i == 1922 && showD) {
                    coinList.add(new CoinSlot("Wheat",den, coinIndex++));
                    coinList.add(new CoinSlot("Wheat",String.format("%n%d No D", i ), coinIndex++));}
                if (i == 1943) {
                    if (showP) {coinList.add(new CoinSlot("Steel", phil, coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot("Steel", den, coinIndex++));}
                    if (showS) {coinList.add(new CoinSlot("Steel", sf, coinIndex++));}
                }
                if (i > 1909 && i < 1959 && i !=1922 && i != 1943) {
                    if (showP) {coinList.add(new CoinSlot("Wheat",phil, coinIndex++));}
                    if (showD && i != 1910 && i != 1921 && i != 1923) {coinList.add(new CoinSlot("Wheat",den, coinIndex++));}
                    if (showS && i != 1932 && i != 1933 && i != 1934 && i != 1956 && i != 1957 && i != 1958) {
                        coinList.add(new CoinSlot("Wheat",sf, coinIndex++));}
                }
            }
            if (showmem) {
                if (i == 1982) {
                    for (Object[] coinData : A1982_COIN_IDENTIFIERS) {
                        String identifier = (String) coinData[0];
                        coinList.add(new CoinSlot(identifier, "", coinIndex++));
                    }
                } else if (i == 2009) {
                    for (Object[] coinData : COIN_IDENTIFIERS) {
                        String bicentIdentifier = (String) coinData[0];
                        if (showP) {coinList.add(new CoinSlot(bicentIdentifier,phil, coinIndex++));}
                        if (showsatin) {coinList.add(new CoinSlot(bicentIdentifier,satin, coinIndex++));}
                        if (showD) {coinList.add(new CoinSlot(bicentIdentifier,den, coinIndex++));}
                        if (showsatin) {coinList.add(new CoinSlot(bicentIdentifier,satind, coinIndex++));}
                        if (showSProof) {coinList.add(new CoinSlot(bicentIdentifier,String.format("%n%d S Proof", i ), coinIndex++));}
                    }
                }
                if (showP) {
                    if (i > 1958 && i < 1982) {coinList.add(new CoinSlot("Memorial",phil, coinIndex++));}
                    if (i > 1964 && i < 1968) {coinList.add(new CoinSlot("Memorial",sms, coinIndex++));}
                    if (i > 1982 && i < 2009) {coinList.add(new CoinSlot("Memorial Zinc",phil, coinIndex++));}
                    if (i > 2009 && i != 2017) {coinList.add(new CoinSlot("Shield",phil, coinIndex++));}
                    if (i == 2017) {coinList.add(new CoinSlot("Shield",String.format("%n%d P ", i ), coinIndex++));}
                    if (showsatin && i > 2004 && i < 2009) {coinList.add(new CoinSlot("Memorial Zinc",satin, coinIndex++));}
                    if (showsatin && i == 2010) {coinList.add(new CoinSlot("Shield",satin, coinIndex++));}
                }
                if (showD) {
                    if (i > 1958 && i < 1982 && i != 1965 && i != 1966 && i != 1967){coinList.add(new CoinSlot("Memorial",den, coinIndex++));}
                    if (i > 1982 && i < 2009) {coinList.add(new CoinSlot("Memorial Zinc",den, coinIndex++));}
                    if (i > 2009) {coinList.add(new CoinSlot("Shield",den, coinIndex++));}
                    if (showsatin && i > 2004 && i < 2009) {coinList.add(new CoinSlot("Memorial Zinc",satind, coinIndex++));}
                    if (showsatin && i == 2010) {coinList.add(new CoinSlot("Shield",satind, coinIndex++));}
                }
                if (showS && i > 1967 && i < 1975) {coinList.add(new CoinSlot("Memorial",sf, coinIndex++));}
                if (showSProof && i > 1958 && i <  1965) {coinList.add(new CoinSlot("Proof",phil, coinIndex++));}
                if (showSProof && i > 1967 && i!=2009) {coinList.add(new CoinSlot("Proof",sf, coinIndex++));}
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
