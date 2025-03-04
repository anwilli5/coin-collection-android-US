

package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Sets extends CollectionInfo {
    public static final String COLLECTION_TYPE = "Coin Sets";

    private static final Object[][] COIN_IDENTIFIERS = {
            {"Proof Set", R.drawable.a24rg},
            {"Mint Set", R.drawable.a24rj},
            {"Silver Proof Set", R.drawable.a24rh},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Proof Set", R.drawable.a24rg},              //0
            {"Mint Set", R.drawable.a24rj},               //1
            {"Silver Proof Set", R.drawable.a24rh},       //2
    };
    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
    }

    private static final int REVERSE_IMAGE = R.drawable.a24rg;

    @Override
    public String getCoinType() {return COLLECTION_TYPE;}

    @Override
    public int getCoinImageIdentifier() {return REVERSE_IMAGE;}

    private static final Integer START_YEAR = 1947;

    @Override
    public int getStartYear() {return START_YEAR;}

    @Override
    public int getStopYear() {return STOP_YEAR;}

    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int ATTRIBUTION =R.string.attr_mint;

    @Override
    public int getAttributionResId() { return ATTRIBUTION;}

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


    public void getCreationParameters(HashMap<String, Object> parameters) {
        parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
        parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_Mint_Sets);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_Proof_Sets);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_Silver_Proof_Sets);
    }
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {
        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean show_mint = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean show_proof = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean show_silver= (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);



        int coinIndex = 0;


        for (int i = startYear; i <= stopYear;  i++) {
            String year = Integer.toString(i);
            if (show_mint && i > 1946 && i != 1950){
                coinList.add(new CoinSlot(year,String.format("%nMint Set"), coinIndex++,getImgId("Mint Set")));
            }
            if ( show_proof && i > 1964 ){
                coinList.add(new CoinSlot(year,String.format("%nProof Set"), coinIndex++,getImgId("Proof Set")));
            }
            if ( show_silver && i > 1949 && i < 1965) {
                coinList.add(new CoinSlot(year,String.format("Silver %nProof Set"), coinIndex++,getImgId("Silver Proof Set")));
            }
            if (show_silver && i >1991) {
                coinList.add(new CoinSlot(year,String.format("Silver %nProof Set"), coinIndex++,getImgId("Silver Proof Set")));
            }
        }
    }
    @Override
    public int onCollectionDatabaseUpgrade(SQLiteDatabase db, CollectionListInfo collectionListInfo,
                                           int oldVersion, int newVersion) {return 0;}
}



