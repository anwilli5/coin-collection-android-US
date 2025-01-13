package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class HalfDimes extends CollectionInfo {

    public static final String COLLECTION_TYPE = "Half Dimes";


    private static final Object[][] COIN_IDENTIFIERS = {
            {"Flowing Hair", R.drawable.a1794_half_dime},
            {"Draped Bust", R.drawable.a1797drapeddime},
            {"Capped Bust", R.drawable.a1820cappeddime},
            {"Liberty Seated No Stars", R.drawable.anostarsdime},
            {"liberty Seated Stars", R.drawable.astarsdime},
            {"Liberty Seated Arrows", R.drawable.astars_arrowsdime},
            {"Liberty Seated Legend", R.drawable.alegenddime},
    };



    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}

    }


    private static final Integer START_YEAR = 1794;
    private static final Integer STOP_YEAR = 1873;

    private static final int OBVERSE_IMAGE_COLLECTED = R.drawable.astarsdime;

    private static final int REVERSE_IMAGE = R.drawable.astarsdime;

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
    }

    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showbust = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showseated = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showo = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);

        int coinIndex = 0;


        for (Integer i = startYear; i <= stopYear; i++) {
            if (showbust && i == 1794) {
                coinList.add(new CoinSlot("Flowing Hair", String.format("%d", i), coinIndex++));
            }
            if (showbust && i > 1795 && i < 1798) {
                coinList.add(new CoinSlot("Draped Bust", String.format("%d Small Eagle", i), coinIndex++));
            }
            if (showbust && i > 1799 && i < 1806 && i != 1804) {
                coinList.add(new CoinSlot("Draped Bust", String.format("%d Heraldic Eagle", i), coinIndex++));
            }
            if (showbust && i > 1828 && i < 1838) {
                coinList.add(new CoinSlot("Capped Bust", String.format("%d", i), coinIndex++));
            }
            if (showseated) {
                if (showP) {
                    if (i == 1837) {coinList.add(new CoinSlot("Liberty Seated No Stars", String.format("%d Sm Date", i), coinIndex++));}
                    if (i == 1837) {coinList.add(new CoinSlot("Liberty Seated No Stars", String.format("%d Lg Date", i), coinIndex++));}
                    if (i > 1837 && i < 1841) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d No Drapery", i), coinIndex++));}
                    if (i > 1839 && i < 1860 && i != 1854 && i != 1855) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d", i), coinIndex++));}
                    if (i == 1848) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d Lg Date", i), coinIndex++));}
                    if (i == 1849) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d 9 Over 6", i), coinIndex++));}
                    if (i == 1853 || i == 1854 || i == 1855) {coinList.add(new CoinSlot("Liberty Seated Arrows", String.format("%d", i), coinIndex++));}
                    if (i == 1858) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d Double Date", i), coinIndex++));
                        coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d Over Inverted Date", i), coinIndex++));}
                    if (i > 1859) {coinList.add(new CoinSlot("Liberty Seated Legend", String.format("%d", i), coinIndex++));}
                    if (i == 1861) {coinList.add(new CoinSlot("Liberty Seated Legend", String.format("%d 1 Over 0", i), coinIndex++));}
                }
                if (showo) {
                    if (i == 1838) {coinList.add(new CoinSlot("Liberty Seated No Stars", String.format("%d O", i), coinIndex++));}
                    if (i == 1839 || i == 1840) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d O No Drapery", i), coinIndex++));}
                    if (i > 1839 && i < 1860 && i != 1843 && i != 1845 && i != 1846 && i != 1847 && i != 1854 && i != 1855) {coinList.add(new CoinSlot("Liberty Seated Stars", String.format("%d O", i), coinIndex++));}
                    if (i == 1853 || i == 1854 || i == 1855) {coinList.add(new CoinSlot("Liberty Seated Arrows", String.format("%d O", i), coinIndex++));}
                    if (i > 1859) {coinList.add(new CoinSlot("Liberty Seated Legend", String.format("%d O", i), coinIndex++));}
                }
                if (showS) {
                    if (i > 1859 && i != 1870) {coinList.add(new CoinSlot("Liberty Seated Legend", String.format("%d S", i), coinIndex++));}
                    if (i == 1870) {coinList.add(new CoinSlot("Liberty Seated Legend", String.format("%d S One Known", i), coinIndex++));}
                    if (i == 1872) {coinList.add(new CoinSlot("Liberty Seated Legend", String.format("%d S Under Bow", i), coinIndex++));}
                }
            }
        }
    }
    @Override
    public int getAttributionResId () {
        return R.string.attr_wikihalfdimes;
    }

    @Override
    public int getStartYear () {
        return START_YEAR;
    }

    @Override
    public int getStopYear () {
        return STOP_YEAR;
    }

    @Override
    public int onCollectionDatabaseUpgrade (SQLiteDatabase db, CollectionListInfo
            collectionListInfo,
                                            int oldVersion, int newVersion){
        return 0;
    }
}








