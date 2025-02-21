/*
 * Coin Collection, an Android app that helps users track the coins that they've collected
 * Copyright (C) 2010-2016 Andrew Williams
 *
 * This file is part of Coin Collection.
 *
 * Coin Collection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coin Collection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coin Collection.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AllNickels extends CollectionInfo {

    public static final String COLLECTION_TYPE = "All Nickels";

    private static final Object[][] WESTWARD_2004_COIN_IDENTIFIERS = {
            {"Peace Medal", R.drawable.westward_2004_louisiana_purchase_unc},
            {"Keelboat", R.drawable.westward_2004_keelboat_unc},
    };

    private static final Object[][] WESTWARD_2005_COIN_IDENTIFIERS = {
            {"Bison", R.drawable.westward_2005_american_bison_unc},
            {"Ocean in View", R.drawable.westward_2005_ocean_in_view_unc},
    };
    private static final Object[][] COIN_IDENTIFIERS = {
            {"Shield", R.drawable.obv_shield_nickel},
            {"Liberty", R.drawable.obv_liberty_head_nickel},
            {"Buffalo", R.drawable.obv_buffalo_nickel},
            {"Jefferson", R.drawable.obv_jefferson_nickel_unc},
            {"Modern Jefferson", R.drawable.jeffersonuncirculated},
            {"Modern Jefferson Proof", R.drawable.jeffersonproof},
    };

    private static final Object[][] COIN_IMG_IDS = {
            {"Shield", R.drawable.anishield_nickel_with_rays___1867_obverse},                         // 0
            {"Liberty", R.drawable.obv_liberty_head_nickel},                                          // 1
            {"Buffalo", R.drawable.obv_buffalo_nickel},                                               // 2
            {"Classic Jefferson", R.drawable.obv_jefferson_nickel_unc},                               // 3
            {"2024 Modern Jefferson", R.drawable.jeffersonuncirculated},                              // 4
            {"2024 Proof Jefferson", R.drawable.jeffersonproof},                                      // 5
            {"Peace Medal", R.drawable.westward_2004_louisiana_purchase_unc},                         // 6
            {"Keelboat", R.drawable.westward_2004_keelboat_unc},                                      // 7
            {"Bison", R.drawable.westward_2005_american_bison_unc},                                   // 8
            {"Ocean in View", R.drawable.westward_2005_ocean_in_view_unc},                            // 9
            {"2005 Obverse Proof", R.drawable.ani2005o},                                              // 10
            {"Jefferson Reverse", R.drawable.ani2003r},                                               // 11
            {"Buffalo Reverse", R.drawable.ani1935r},                                                 // 12
            {"Liberty Reverse", R.drawable.ani1883r},                                                 // 13
            {"Shield Reverse", R.drawable.anishield_nickel_without_rays___reverse},                   // 14
            {"1867 Shield Reverse with Rays", R.drawable.anishield_nickel_with_rays___1867_reverse},  // 15
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : WESTWARD_2004_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : WESTWARD_2005_COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
        for (Object[] coinData : COIN_IDENTIFIERS) {COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);}
    }

    private static final Integer START_YEAR = 1866;
    private static final Integer STOP_YEAR = CoinPageCreator.OPTVAL_STILL_IN_PRODUCTION;

    private static final int REVERSE_IMAGE = R.drawable.rev_buffalo_nickel;

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

        // Use the MINT_MARK_1 checkbox for whether to include 'P' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1_STRING_ID, R.string.include_p);

        // Use the MINT_MARK_2 checkbox for whether to include 'D' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2_STRING_ID, R.string.include_d);

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3_STRING_ID, R.string.include_s);

        // Use the MINT_MARK_4 checkbox for whether to include 'S' proof coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_s_proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_satin);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6_STRING_ID, R.string.include_w);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_old_coins);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_shield_nickels);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_liberty_nickels);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_buffalo_nickels);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_5, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_5_STRING_ID, R.string.include_jefferson_nickels);
    }

    // TODO Perform validation and throw exception
    @Override
    public void populateCollectionLists(HashMap<String, Object> parameters, ArrayList<CoinSlot> coinList) {

        Integer startYear = (Integer) parameters.get(CoinPageCreator.OPT_START_YEAR);
        Integer stopYear = (Integer) parameters.get(CoinPageCreator.OPT_STOP_YEAR);
        Boolean showP = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_1);
        Boolean showD = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_2);
        Boolean showS = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_3);
        Boolean showSProof = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_4);
        Boolean showSatin = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_5);
        Boolean showW = (Boolean) parameters.get(CoinPageCreator.OPT_SHOW_MINT_MARK_6);
        Boolean showold = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showshield = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showLiberty = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showBuffalo = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        Boolean showJefferson = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_5);
        int coinIndex = 0;

        if (showold && !showshield) {coinList.add(new CoinSlot("Shield","", coinIndex++));}
        if (showold && !showLiberty) {coinList.add(new CoinSlot("Liberty","", coinIndex++));}
        if (showold && !showBuffalo) {coinList.add(new CoinSlot("Buffalo","", coinIndex++));}

        for (int i = startYear; i <= stopYear; i++) {
            String year = Integer.toString(i);
            if (showshield) {
                if (i > 1865 && i < 1884 && i != 1877 && i != 1878) {
                    coinList.add(new CoinSlot(year,"", coinIndex++,0));}
                if (i ==1877 || i ==1878){
                    coinList.add(new CoinSlot(year,"", coinIndex++,0));}
            }
            if (showLiberty) {
                if (showP) {
                    if (i == 1883) {
                        coinList.add(new CoinSlot(year, "w Cents", coinIndex++,1));
                        coinList.add(new CoinSlot(year,"No Cents", coinIndex++,1));}
                    if (i > 1883 && i < 1913) {coinList.add(new CoinSlot("Liberty", String.format("%d   ", i), coinIndex++));}
                }
                if (showD && i == 1912) {coinList.add(new CoinSlot(year,"D", coinIndex++,1));}
                if (showS && i == 1912) {coinList.add(new CoinSlot(year,"S", coinIndex++,1));}
            }

            if (showBuffalo) {
                if (i > 1912 && i < 1939 && i != 1922 && i != 1932 && i != 1933) {
                    if (showP) {
                        if (i != 1931 && i != 1938) {
                            if (i == 1913) {
                                coinList.add(new CoinSlot(year, "Type I", coinIndex++,2));
                                coinList.add(new CoinSlot(year, "Type II", coinIndex++,2));
                            } else {
                                coinList.add(new CoinSlot(year,"", coinIndex++,2));
                            }
                        }
                    }
                    if (showD) {
                        if (i != 1921 && i != 1923 && i != 1930 && i != 1931) {
                            if (i == 1913) {
                                coinList.add(new CoinSlot(year, "D Type I", coinIndex++,2));
                                coinList.add(new CoinSlot(year, "D Type II", coinIndex++,2));
                            } else {
                                coinList.add(new CoinSlot(year,"D", coinIndex++,2));
                            }
                        }
                    }
                    if (showS) {
                        if (i != 1934 && i != 1938) {
                            if (i == 1913) {
                                coinList.add(new CoinSlot(year, "S Type I", coinIndex++,2));
                                coinList.add(new CoinSlot(year, "S Type II", coinIndex++,2));
                            } else {
                                coinList.add(new CoinSlot(year, "S", coinIndex++,2));
                            }
                        }
                    }
                }
            }
            if (showJefferson && i > 1937) {
                if ( i == 1942){
                    if (showP) {
                        coinList.add(new CoinSlot("1942","", coinIndex++,3));
                        coinList.add(new CoinSlot("1942","Silver", coinIndex++,3));}
                    if (showD) {coinList.add(new CoinSlot("1942","D", coinIndex++,3));}
                    if (showS) {coinList.add(new CoinSlot("1942","S Silver", coinIndex++,3));}
                }
                if ( i > 1942 && i < 1946){
                    if (showP) {coinList.add(new CoinSlot(year,"Silver", coinIndex++,3));}
                    if (showD) {coinList.add(new CoinSlot(year,"D Silver", coinIndex++,3));}
                    if (showS) {coinList.add(new CoinSlot(year,"S Silver", coinIndex++,3));}
                }
                if ( i  < 2004 && i != 1942 && i != 1943 && i != 1944 && i != 1945) {
                    if (showP && i != 1968 && i != 1969 && i != 1970) {
                        if (i >= 1980) {coinList.add(new CoinSlot(year,"P", coinIndex++,3));}
                        if (i < 1980) {coinList.add(new CoinSlot(year,"", coinIndex++,3));}
                        if (i > 1964 && i < 1968) {coinList.add(new CoinSlot(year,"SMS", coinIndex++,3));}
                    }
                    if (showD && i != 1965 && i != 1966 && i != 1967) {coinList.add(new CoinSlot(year,"D", coinIndex++,3));}
                    if (showS && i <= 1970 && i != 1950 && (i < 1955 || i > 1967)) {coinList.add(new CoinSlot(year,"S", coinIndex++,3));}
                    if (showSProof && i > 1967) {coinList.add(new CoinSlot(year,"S Proof", coinIndex++,3));}
                }
                if (i ==2004){
                    if (showP) {coinList.add(new CoinSlot(year, "P Peace Medal ", coinIndex++,6));
                                coinList.add(new CoinSlot(year, "P Keelboat", coinIndex++,7));}
                    if (showD) {coinList.add(new CoinSlot(year, "D Peace Medal", coinIndex++,6));
                                coinList.add(new CoinSlot(year, "D Keelboat", coinIndex++,7));}
                    if (showSProof) {coinList.add(new CoinSlot(year,"S Proof Peace Medal", coinIndex++,6));
                                coinList.add(new CoinSlot(year,"S Proof Keelboat", coinIndex++,7));}
                }
                if (i == 2005){
                    if (showP) {coinList.add(new CoinSlot(year,"P Bison", coinIndex++,8));
                                coinList.add(new CoinSlot(year,"P Ocean in View", coinIndex++,9));}
                    if (showSatin) {coinList.add(new CoinSlot(year,"P Satin Bison", coinIndex++,8));
                                coinList.add(new CoinSlot(year,"P Satin Ocean in View", coinIndex++,9));}
                    if (showD) {coinList.add(new CoinSlot(year,"D Bison", coinIndex++,8));
                                coinList.add(new CoinSlot(year,"D Ocean in View", coinIndex++,9));}
                    if (showSatin) {coinList.add(new CoinSlot(year,"D Satin Bison", coinIndex++,8));
                                coinList.add(new CoinSlot(year,"D Satin Ocean in View", coinIndex++,9));}
                    if (showSProof) {coinList.add(new CoinSlot(year,"S Proof Bison", coinIndex++,10));
                                coinList.add(new CoinSlot(year,"S Proof Ocean in View", coinIndex++,10));}
                }
                if (i > 2005) {
                    if (showP) {coinList.add(new CoinSlot(year,"P", coinIndex++,4));}
                    if (showSatin && i < 2011) {coinList.add(new CoinSlot(year,"P Satin", coinIndex++,4));}
                    if (showD) {coinList.add(new CoinSlot(year,"D", coinIndex++,4));}
                    if (showSatin && i < 2011) {coinList.add(new CoinSlot(year,"D Satin", coinIndex++,4));}
                    if (showSProof) {coinList.add(new CoinSlot(year,"S Proof", coinIndex++,5));}
                    if (showSProof && i == 2018) {coinList.add(new CoinSlot(year,"S Reverse Proof", coinIndex++,5));}
                    if (showW && i == 2020) {
                        coinList.add(new CoinSlot(year,"W", coinIndex++,4));
                        coinList.add(new CoinSlot(year,"W Proof", coinIndex++,4));
                    }
                }
            }
        }
    }

    @Override
    public int getAttributionResId() {return R.string.attr_mint;}

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
                                           int oldVersion, int newVersion) {return 0;}

}
