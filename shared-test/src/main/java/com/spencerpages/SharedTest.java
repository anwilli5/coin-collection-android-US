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

package com.spencerpages;

import static com.coincollection.CoinPageCreator.OPT_CHECKBOX_1;
import static com.coincollection.CoinPageCreator.OPT_CHECKBOX_2;
import static com.coincollection.CoinPageCreator.OPT_EDIT_DATE_RANGE;
import static com.coincollection.CoinPageCreator.OPT_SHOW_MINT_MARKS;
import static com.coincollection.CoinPageCreator.OPT_SHOW_MINT_MARK_1;
import static com.coincollection.CoinPageCreator.OPT_SHOW_MINT_MARK_2;
import static com.coincollection.CoinPageCreator.OPT_SHOW_MINT_MARK_3;
import static com.coincollection.CoinPageCreator.OPT_SHOW_MINT_MARK_4;
import static com.coincollection.CoinPageCreator.OPT_SHOW_MINT_MARK_5;
import static com.coincollection.CoinPageCreator.OPT_START_YEAR;
import static com.coincollection.CoinPageCreator.OPT_STOP_YEAR;
import static com.spencerpages.MainApplication.getIndexFromCollectionNameStr;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.helper.ParcelableHashMap;

import java.util.ArrayList;

public class SharedTest {

    public static final CollectionListInfo[] COLLECTION_LIST_INFO_SCENARIOS =
            {
                    new CollectionListInfo("My Pennies", 275, 0, getIndexFromCollectionNameStr("Pennies"), 0, 1909, 2020, "15", "1"),
                    new CollectionListInfo("@#$#@%$#^$#$%^%$&^^&%$", 19, 0, getIndexFromCollectionNameStr("Nickels"), 0, 1938, 2020, "9", "0"),
                    new CollectionListInfo("My\\Collection/", 2, 0, getIndexFromCollectionNameStr("Dimes"), 0, 1950, 1951, "2", "1"),
                    new CollectionListInfo("my,coll,ection.", 65, 0, getIndexFromCollectionNameStr("Quarters"), 0, 1932, 1998, "3", "0"),
                    new CollectionListInfo("<>STATE QUARTERS\n\n", 112, 0, getIndexFromCollectionNameStr("State Quarters"), 0, 0, 0, "7", "4"),
                    new CollectionListInfo("</></>", 56, 0, getIndexFromCollectionNameStr("National Park Quarters"), 0, 0, 0, "5", "0"),
                    new CollectionListInfo("~", 56, 0, getIndexFromCollectionNameStr("Half-Dollars"), 0, 1964, 2020, "2", "1"),
                    new CollectionListInfo("Eisenhower", 2, 0, getIndexFromCollectionNameStr("Eisenhower Dollars"), 0, 1971, 1971, "7", "1"),
                    new CollectionListInfo("Susan's \"Dollars!", 4, 0, getIndexFromCollectionNameStr("Susan B. Anthony Dollars"), 0, 1979, 1999, "2", "0"),
                    new CollectionListInfo("; DROP TABLES;", 16, 0, getIndexFromCollectionNameStr("Sacagawea/Native American Dollars"), 0, 2002, 2009, "7", "1"),
                    new CollectionListInfo("😁😁😁😁😁😍😄", 80, 0, getIndexFromCollectionNameStr("Presidential Dollars"), 0, 0, 0, "7", "0"),
                    new CollectionListInfo("| | | | | ", 2, 0, getIndexFromCollectionNameStr("Indian Head Cents"), 0, 1859, 1909, "9", "0"),
                    new CollectionListInfo(" ", 32, 0, getIndexFromCollectionNameStr("Liberty Head Nickels"), 0, 1883, 1912, "7", "1"),
                    new CollectionListInfo("  ", 25, 0, getIndexFromCollectionNameStr("Barber Dimes"), 0, 1892, 1916, "2", "0"),
                    new CollectionListInfo("Barb Quarts", 25, 0, getIndexFromCollectionNameStr("Barber Quarters"), 0, 1892, 1916, "2", "0"),
                    new CollectionListInfo("_)(*&^%", 12, 0, getIndexFromCollectionNameStr("Standing Liberty Quarters"), 0, 1916, 1930, "9", "0"),
                    new CollectionListInfo("Frankline", 19, 0, getIndexFromCollectionNameStr("Franklin Half Dollars"), 0, 1948, 1963, "13", "1"),
                    new CollectionListInfo("✓✓✓✓✓™", 96, 0, getIndexFromCollectionNameStr("Morgan Dollars"), 0, 1878, 1921, "63", "0"),
                    new CollectionListInfo("🐶✌️", 1, 0, getIndexFromCollectionNameStr("Peace Dollars"), 0, 1921, 1921, "2", "1"),
                    new CollectionListInfo("Eagles", 39, 0, getIndexFromCollectionNameStr("American Eagle Silver Dollars"), 0, 1986, 2020, "0", "2"),
                    new CollectionListInfo("()", 42, 0, getIndexFromCollectionNameStr("First Spouse Gold Coins"), 0, 0, 0, "0", "0"),
                    new CollectionListInfo("S Pennies", 0, 0, getIndexFromCollectionNameStr("Pennies"), 0, 2000, 2020, "9", "1"),
                    new CollectionListInfo("50-States", 50, 0, getIndexFromCollectionNameStr("State Quarters"), 0, 0, 0, "3", "0"),
                    new CollectionListInfo("$PATH", 56, 0, getIndexFromCollectionNameStr("National Park Quarters"), 0, 0, 0, "5", "0"),
                    new CollectionListInfo("CHAR(0x54)", 11, 0, getIndexFromCollectionNameStr("Sacagawea/Native American Dollars"), 0, 2010, 2020, "2", "1"),
                    new CollectionListInfo("<a href=\"#\">Click Here</a>", 14, 0, getIndexFromCollectionNameStr("American Eagle Silver Dollars"), 0, 1986, 1999, "0", "1"),
                    new CollectionListInfo("# comment", 18, 0, getIndexFromCollectionNameStr("Barber Quarters"), 0, 1892, 1916, "17", "0"),
                    new CollectionListInfo("American_Women", 45, 0, getIndexFromCollectionNameStr("American Women Quarters"), 0, 0, 0, "15", "0"),
                    new CollectionListInfo("Small Cents", 442, 0, getIndexFromCollectionNameStr("Small Cents"), 0, 1856, 2023, "3151", "4063265"),
                    new CollectionListInfo("Large Cents", 68, 0, getIndexFromCollectionNameStr("Large Cents"), 0, 1793, 1857, "0", "1089"),
                    new CollectionListInfo("All Nickels", 380, 0, getIndexFromCollectionNameStr("All Nickels"), 0, 1866, 2022, "2255", "1006632993"),
                    new CollectionListInfo("Half Dimes", 96, 0, getIndexFromCollectionNameStr("Half Dimes"), 0, 1794, 1873, "27", "577"),
                    new CollectionListInfo("Silver Dimes", 249, 0, getIndexFromCollectionNameStr("Silver Dimes"), 0, 1793, 2023, "287", "7516192801"),
                    new CollectionListInfo("Early Dimes", 144, 0, getIndexFromCollectionNameStr("Early Dimes"), 0, 1796, 1891, "59", "929"),
                    new CollectionListInfo("Clad Quarters", 614, 0, getIndexFromCollectionNameStr("Clad Quarters"), 0, 0, 0, "2383", "122880"),
                    new CollectionListInfo("Washington Silver", 225, 0, getIndexFromCollectionNameStr("Washington Silver Quarters"), 0, 0, 0, "15", "32985348841472"),
                    new CollectionListInfo("Early Quarters", 244, 0, getIndexFromCollectionNameStr("Early Quarters"), 0, 1776, 1930, "63", "6753"),
                    new CollectionListInfo("Small Dollars", 206, 0, getIndexFromCollectionNameStr("Small Dollars"), 0, 0, 0, "143", "962072674304"),
                    new CollectionListInfo("Silver Half Dollars", 231, 0, getIndexFromCollectionNameStr("Silver Half Dollars"), 0, 1892, 2023, "287", "62914593"),
                    new CollectionListInfo("West Point", 21, 0, getIndexFromCollectionNameStr("West Point Mint"), 0, 0, 0, "0", "0"),
            };

    public static final CoinSlot[] COIN_SLOT_SCENARIOS =
            {
                    new CoinSlot(0, "1989", "", true, 0, 0, "", 0, false, -1),
                    new CoinSlot(1, "1776-1976", "P", false, 2, 10, "Advanced Notes", 1, false, -1),
                    new CoinSlot(2, "State Park", "CC", true, 20, 1, "293370#$%@#^$#@^", 2, false, -1),
                    new CoinSlot(3, "2000 Type 1", "D", true, 0, 0, "These are my notes\n\n", 3, false, -1),
                    new CoinSlot(4, "Some Coin", "S", false, 11, 11, "", 4, false, -1),
            };

    public static final ParcelableHashMap[] PARAMETER_SCENARIOS =
            {
                    new ParcelableHashMap() {{
                        put(OPT_SHOW_MINT_MARKS, Boolean.TRUE);
                        put(OPT_EDIT_DATE_RANGE, Boolean.TRUE);
                        put(OPT_START_YEAR, 1990);
                        put(OPT_STOP_YEAR, 2020);
                        put(OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
                        put(OPT_SHOW_MINT_MARK_2, Boolean.TRUE);
                        put(OPT_SHOW_MINT_MARK_3, Boolean.FALSE);
                        put(OPT_SHOW_MINT_MARK_4, Boolean.TRUE);
                        put(OPT_SHOW_MINT_MARK_5, Boolean.FALSE);
                        put(OPT_CHECKBOX_1, Boolean.TRUE);
                        put(OPT_CHECKBOX_2, Boolean.FALSE);
                    }},
                    new ParcelableHashMap() {{
                        put(OPT_SHOW_MINT_MARKS, Boolean.FALSE);
                        put(OPT_EDIT_DATE_RANGE, Boolean.TRUE);
                        put(OPT_START_YEAR, 1800);
                        put(OPT_STOP_YEAR, 1991);
                        put(OPT_SHOW_MINT_MARK_1, Boolean.FALSE);
                        put(OPT_SHOW_MINT_MARK_2, Boolean.FALSE);
                        put(OPT_SHOW_MINT_MARK_3, Boolean.TRUE);
                        put(OPT_SHOW_MINT_MARK_4, Boolean.FALSE);
                        put(OPT_SHOW_MINT_MARK_5, Boolean.TRUE);
                        put(OPT_CHECKBOX_1, Boolean.FALSE);
                        put(OPT_CHECKBOX_2, Boolean.TRUE);
                    }},
                    new ParcelableHashMap() {{
                        put(OPT_SHOW_MINT_MARKS, Boolean.TRUE);
                        put(OPT_EDIT_DATE_RANGE, Boolean.FALSE);
                        put(OPT_START_YEAR, 0);
                        put(OPT_STOP_YEAR, 0);
                        put(OPT_SHOW_MINT_MARK_1, Boolean.TRUE);
                    }}
            };

    /**
     * Compare two CollectionListInfo objects to ensure they're the same
     *
     * @param base  CollectionListInfo
     * @param check CollectionListInfo
     * @return true if they have the same contents, false otherwise
     */
    public static boolean compareCollectionListInfos(CollectionListInfo base, CollectionListInfo check) {
        // TODO - Not sure how to add assertions here
        return (base.getName().equals(check.getName()) &&
                (base.getMax() == check.getMax()) &&
                (base.getCollected() == check.getCollected()) &&
                (base.getCollectionTypeIndex() == check.getCollectionTypeIndex()) &&
                (base.getDisplayType() == check.getDisplayType()) &&
                (base.getCoinImageIdentifier() == check.getCoinImageIdentifier()) &&
                (base.getStartYear() == check.getStartYear()) &&
                (base.getEndYear() == check.getEndYear()) &&
                (base.getMintMarkFlags().equals(check.getMintMarkFlags())) &&
                (base.getCheckboxFlags().equals(check.getCheckboxFlags())));
    }

    /**
     * Compare two CoinSlot objects to ensure they're the same
     *
     * @param base           CoinSlot
     * @param check          CoinSlot
     * @param compareAdvInfo if true, enables comparison of advanced details
     * @return true if they have the same contents, false otherwise
     */
    public static boolean compareCoinSlots(CoinSlot base, CoinSlot check, boolean compareAdvInfo) {
        // TODO - Not sure how to add assertions here
        return (base.getIdentifier().equals(check.getIdentifier()) &&
                (base.getMint().equals(check.getMint())) &&
                (base.isInCollection() == check.isInCollection()) &&
                (!compareAdvInfo || (base.getAdvancedGrades().equals(check.getAdvancedGrades()))) &&
                (!compareAdvInfo || (base.getAdvancedQuantities().equals(check.getAdvancedQuantities()))) &&
                (!compareAdvInfo || (base.getAdvancedNotes().equals(check.getAdvancedNotes()))));
    }

    /**
     * Compare two lists of CoinSlot objects
     *
     * @param base           list of CoinSlots
     * @param check          list of CoinSlots
     * @param compareAdvInfo if true, enables comparison of advanced details
     * @return true if they have the same contents, false otherwise
     */
    public static boolean compareCoinSlotLists(ArrayList<CoinSlot> base, ArrayList<CoinSlot> check, boolean compareAdvInfo) {
        if (base.size() != check.size()) {
            return false;
        }
        for (int i = 0; i < base.size(); i++) {
            if (!compareCoinSlots(base.get(i), check.get(i), compareAdvInfo)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two ParcelableHashMap objects to ensure they're the same
     *
     * @param base  ParcelableHashMap
     * @param check ParcelableHashMap
     * @return true if they have the same contents, false otherwise
     */
    public static boolean compareParameters(ParcelableHashMap base, ParcelableHashMap check) {
        // TODO - Not sure how to add assertions here
        if (base.keySet().size() != check.keySet().size()) {
            return false;
        }
        for (String key : base.keySet()) {
            Object value = base.get(key);
            if (value == null || !check.containsKey(key) || !value.equals(check.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two lists of CoinSlot lists
     *
     * @param base           list of CoinSlots lists
     * @param check          list of CoinSlots lists
     * @param compareAdvInfo if true, enables comparison of advanced details
     * @return true if they have the same contents, false otherwise
     */
    public static boolean compareListOfCoinSlotLists(ArrayList<ArrayList<CoinSlot>> base, ArrayList<ArrayList<CoinSlot>> check, boolean compareAdvInfo) {
        if (base.size() != check.size()) {
            return false;
        }
        for (int i = 0; i < base.size(); i++) {
            if (!compareCoinSlotLists(base.get(i), check.get(i), compareAdvInfo)) {
                return false;
            }
        }
        return true;
    }
}
