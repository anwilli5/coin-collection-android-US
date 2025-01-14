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
            {"Peace Medal ", R.drawable.westward_2004_louisiana_purchase_unc},
            {"Keelboat ", R.drawable.westward_2004_keelboat_unc},
    };

    private static final Object[][] WESTWARD_2005_COIN_IDENTIFIERS = {
            {"Bison ", R.drawable.westward_2005_american_bison_unc},
            {"Ocean in View ", R.drawable.westward_2005_ocean_in_view_unc},
    };
    private static final Object[][] COIN_IDENTIFIERS = {
            {"   ", R.drawable.obv_shield_nickel},
            {"  ", R.drawable.obv_liberty_head_nickel},
            {"", R.drawable.obv_buffalo_nickel},
            {" ", R.drawable.obv_jefferson_nickel_unc},
            {"`", R.drawable.jeffersonuncirculated},
            {"'", R.drawable.jeffersonproof},
            {"Shield", R.drawable.obv_shield_nickel},
            {"Liberty", R.drawable.obv_liberty_head_nickel},
            {"Buffalo", R.drawable.obv_buffalo_nickel},
    };

    private static final HashMap<String, Integer> COIN_MAP = new HashMap<>();

    static {
        // Populate the COIN_MAP HashMap for quick image ID lookups later
        for (Object[] coinData : WESTWARD_2004_COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
        for (Object[] coinData : WESTWARD_2005_COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
        for (Object[] coinData : COIN_IDENTIFIERS) {
            COIN_MAP.put((String) coinData[0], (Integer) coinData[1]);
        }
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
    public int getCoinSlotImage(CoinSlot coinSlot) {
        Integer slotImage = COIN_MAP.get(coinSlot.getIdentifier());
        return (slotImage != null) ? slotImage : (int) COIN_IDENTIFIERS[0][1];
    }

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

        // Use the MINT_MARK_3 checkbox for whether to include 'S' coins
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4_STRING_ID, R.string.include_s_Proofs);

        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5_STRING_ID, R.string.include_satin);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1_STRING_ID, R.string.include_Shield);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_2, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_2_STRING_ID, R.string.include_Liberty);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_3, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_3_STRING_ID, R.string.include_Buffalo);

        parameters.put(CoinPageCreator.OPT_CHECKBOX_4, Boolean.TRUE);
        parameters.put(CoinPageCreator.OPT_CHECKBOX_4_STRING_ID, R.string.include_Jefferson);


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
        Boolean showshield = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_1);
        Boolean showLiberty = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_2);
        Boolean showBuffalo = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_3);
        Boolean showJefferson = (Boolean) parameters.get(CoinPageCreator.OPT_CHECKBOX_4);
        int coinIndex = 0;

        if (!showshield) {coinList.add(new CoinSlot("Shield","", coinIndex++));}
        if (!showLiberty) {coinList.add(new CoinSlot("Liberty","", coinIndex++));}
        if (!showBuffalo) {coinList.add(new CoinSlot("Buffalo","", coinIndex++));}

        for (int i = startYear; i <= stopYear; i++) {
            String phil = String.format("%d ", i);
            String den = String.format("%d D ", i);
            String sf = String.format("%d S ", i);
            String satin = String.format("%d Satin", i);
            String satind = String.format("%d D Satin", i);



            if (showshield) {
                if (i > 1865 && i < 1884 && i != 1877 && i != 1878) {
                    coinList.add(new CoinSlot("   ",String.format("%d    ", i  ), coinIndex++));
                }
            }
            if (showLiberty) {
                if (i == 1883) {
                    coinList.add(new CoinSlot("  ", String.format("%d w Cents  ", i), coinIndex++));
                    coinList.add(new CoinSlot("  ", String.format("%d No Cents  ", i), coinIndex++));
                }
                if (i > 1883 && i < 1913) {
                    coinList.add(new CoinSlot("  ",String.format("%d   ", i  ), coinIndex++));
                }
                if (showS && i == 1912) {
                    coinList.add(new CoinSlot("  ",String.format("%d D   ", i  ), coinIndex++));
                    coinList.add(new CoinSlot("  ",String.format("%d S   ", i  ), coinIndex++));
                }
            }
            if (showBuffalo) {
                if (i > 1912 && i < 1939 && i != 1922 && i != 1932 && i != 1933) {
                    if (showP) {
                        if (i != 1931 && i != 1938) {
                            if (i == 1913) {
                                coinList.add(new CoinSlot("", String.format("%d Type I", i), coinIndex++));
                                coinList.add(new CoinSlot("", String.format("%d Type II", i), coinIndex++));
                            } else {
                                coinList.add(new CoinSlot("", phil, coinIndex++));
                            }
                        }
                    }
                    if (showD) {
                        if (i != 1921 && i != 1923 && i != 1930 && i != 1931) {
                            if (i == 1913) {
                                coinList.add(new CoinSlot("", String.format("%d D Type I", i), coinIndex++));
                                coinList.add(new CoinSlot("", String.format("%d D Type II", i), coinIndex++));
                            } else {
                                coinList.add(new CoinSlot("", den, coinIndex++));
                            }
                        }
                    }
                    if (showS) {
                        if (i != 1934 && i != 1938) {
                            if (i == 1913) {
                                coinList.add(new CoinSlot("", String.format("%d S Type I", i), coinIndex++));
                                coinList.add(new CoinSlot("", String.format("%d S Type II", i), coinIndex++));
                            } else {
                                coinList.add(new CoinSlot("", sf, coinIndex++));
                            }
                        }
                    }
                }
            }
            if (showJefferson && i > 1937 && i < 2025) {
                if (i == 2004) {
                    // 2004 Jefferson Presidential Nickels
                    for (Object[] coinData : WESTWARD_2004_COIN_IDENTIFIERS) {
                        String identifier = (String) coinData[0];
                        if (showP) {coinList.add(new CoinSlot(identifier, phil, coinIndex++));}
                        if (showD) {coinList.add(new CoinSlot(identifier, den, coinIndex++));}
                        if (showSProof) {coinList.add(new CoinSlot(identifier,String.format("%d S Proof", i), coinIndex++));}
                    }
                }
                if (i == 2005) {
                    // 2005 Jefferson Presidential Nickels
                    for (Object[] coinData : WESTWARD_2005_COIN_IDENTIFIERS) {
                        String identifier = (String) coinData[0];
                        if (showP) {coinList.add(new CoinSlot(identifier, phil, coinIndex++));}
                        if (showSatin) {coinList.add(new CoinSlot(identifier, satin, coinIndex++));}
                        if (showD) {coinList.add(new CoinSlot(identifier, den, coinIndex++));}
                        if (showSatin) {coinList.add(new CoinSlot(identifier, satind, coinIndex++));}
                        if (showSProof) {coinList.add(new CoinSlot(identifier,String.format("%d S Proof", i), coinIndex++));}
                    }
                }
                if ( i == 1942){
                    if (showP) {
                        coinList.add(new CoinSlot(" ",String.format("%d ", i), coinIndex++));
                        coinList.add(new CoinSlot(" ",String.format("%d Silver ", i), coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot(" ",String.format("%d D ", i), coinIndex++));}
                    if (showS) {coinList.add(new CoinSlot(" ",String.format("%d S Silver ", i), coinIndex++));}
                }
                if ( i > 1942 && i < 1946){
                    if (showP) {coinList.add(new CoinSlot(" ",String.format("%d Silver ", i), coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot(" ",String.format("%d D Silver ", i), coinIndex++));}
                    if (showS) {coinList.add(new CoinSlot(" ",String.format("%d S Silver ", i), coinIndex++));}
                }
                if ( i  < 2004 && i != 1942 && i != 1943 && i != 1944 && i != 1945) {
                    if (showP && i != 1968 && i != 1969 && i != 1970) {
                        if (i >= 1980) {coinList.add(new CoinSlot(" ",String.format("%d P  ", i), coinIndex++));}
                        if (i < 1980) {coinList.add(new CoinSlot(" ",String.format("%d  ", i), coinIndex++));}
                        if (i > 1964 && i < 1968) {coinList.add(new CoinSlot(" ",String.format("%d SMS ", i), coinIndex++));}
                    }
                    if (showD && i != 1965 && i != 1966 && i != 1967) {coinList.add(new CoinSlot(" ",String.format("%d D  ", i), coinIndex++));}
                    if (showS && i <= 1970 && i != 1950 && (i < 1955 || i > 1967)) {coinList.add(new CoinSlot(" ",String.format("%d S  ", i), coinIndex++));}
                    if (showSProof && i > 1967) {coinList.add(new CoinSlot(" ",String.format("%d S Proof ", i), coinIndex++));}
                }
                if (i > 2005) {
                    if (showP) {coinList.add(new CoinSlot("`",String.format("%d P  ", i), coinIndex++));}
                    if (showSatin && i < 2011) {coinList.add(new CoinSlot("`",String.format("%d P Satin ", i), coinIndex++));}
                    if (showD) {coinList.add(new CoinSlot("`",String.format("%d D  ", i), coinIndex++));}
                    if (showSatin && i < 2011) {coinList.add(new CoinSlot("`",String.format("%d D Satin ", i), coinIndex++));}
                    if (showSProof) {coinList.add(new CoinSlot("'",String.format("%d S Proof ", i), coinIndex++));}
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

