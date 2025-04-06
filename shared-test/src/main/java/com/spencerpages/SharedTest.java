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
import static com.spencerpages.MainApplication.getIndexFromCollectionClass;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanWomenQuarters;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BasicDimes;
import com.spencerpages.collections.BasicHalfDollars;
import com.spencerpages.collections.Cartwheels;
import com.spencerpages.collections.CladQuarters;
import com.spencerpages.collections.CoinSets;
import com.spencerpages.collections.EarlyDimes;
import com.spencerpages.collections.EarlyDollars;
import com.spencerpages.collections.EarlyHalfDollars;
import com.spencerpages.collections.EarlyQuarters;
import com.spencerpages.collections.EisenhowerDollar;
import com.spencerpages.collections.FirstSpouseGoldCoins;
import com.spencerpages.collections.FranklinHalfDollars;
import com.spencerpages.collections.HalfCents;
import com.spencerpages.collections.HalfDimes;
import com.spencerpages.collections.IndianHeadCents;
import com.spencerpages.collections.JeffersonNickels;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LargeCents;
import com.spencerpages.collections.LibertyHeadNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NationalParkQuarters;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.PresidentialDollars;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.SilverDimes;
import com.spencerpages.collections.SilverHalfDollars;
import com.spencerpages.collections.SmallCents;
import com.spencerpages.collections.SmallDollars;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.Trimes;
import com.spencerpages.collections.TwentyCents;
import com.spencerpages.collections.TwoCents;
import com.spencerpages.collections.BasicQuarters;
import com.spencerpages.collections.WashingtonSilver;
import com.spencerpages.collections.WashingtonQuarters;
import com.spencerpages.collections.WestPoint;

import java.util.ArrayList;

public class SharedTest {

    public static final CollectionListInfo[] COLLECTION_LIST_INFO_SCENARIOS =
            {
                    new CollectionListInfo("My Pennies", 275, 0, getIndexFromCollectionClass(LincolnCents.class), 0, 1909, 2020, "15", "1"),
                    new CollectionListInfo("@#$#@%$#^$#$%^%$&^^&%$", 19, 0, getIndexFromCollectionClass(JeffersonNickels.class), 0, 1938, 2020, "9", "0"),
                    new CollectionListInfo("My\\Collection/", 2, 0, getIndexFromCollectionClass(BasicDimes.class), 0, 1950, 1951, "2", "1"),
                    new CollectionListInfo("my,coll,ection.", 65, 0, getIndexFromCollectionClass(BasicQuarters.class), 0, 1932, 1998, "3", "0"),
                    new CollectionListInfo("<>STATE QUARTERS\n\n", 112, 0, getIndexFromCollectionClass(StateQuarters.class), 0, 0, 0, "7", "4"),
                    new CollectionListInfo("</></>", 56, 0, getIndexFromCollectionClass(NationalParkQuarters.class), 0, 0, 0, "5", "0"),
                    new CollectionListInfo("~", 56, 0, getIndexFromCollectionClass(BasicHalfDollars.class), 0, 1964, 2020, "2", "1"),
                    new CollectionListInfo("Eisenhower", 2, 0, getIndexFromCollectionClass(EisenhowerDollar.class), 0, 1971, 1971, "7", "1"),
                    new CollectionListInfo("Susan's \"Dollars!", 4, 0, getIndexFromCollectionClass(SusanBAnthonyDollars.class), 0, 1979, 1999, "2", "0"),
                    new CollectionListInfo("; DROP TABLES;", 16, 0, getIndexFromCollectionClass(NativeAmericanDollars.class), 0, 2002, 2009, "7", "1"),
                    new CollectionListInfo("😁😁😁😁😁😍😄", 80, 0, getIndexFromCollectionClass(PresidentialDollars.class), 0, 0, 0, "7", "0"),
                    new CollectionListInfo("| | | | | ", 2, 0, getIndexFromCollectionClass(IndianHeadCents.class), 0, 1859, 1909, "9", "0"),
                    new CollectionListInfo(" ", 32, 0, getIndexFromCollectionClass(LibertyHeadNickels.class), 0, 1883, 1912, "7", "1"),
                    new CollectionListInfo("  ", 25, 0, getIndexFromCollectionClass(BarberDimes.class), 0, 1892, 1916, "2", "0"),
                    new CollectionListInfo("Barb Quarts", 25, 0, getIndexFromCollectionClass(BarberQuarters.class), 0, 1892, 1916, "2", "0"),
                    new CollectionListInfo("_)(*&^%", 12, 0, getIndexFromCollectionClass(StandingLibertyQuarters.class), 0, 1916, 1930, "9", "0"),
                    new CollectionListInfo("Frankline", 19, 0, getIndexFromCollectionClass(FranklinHalfDollars.class), 0, 1948, 1963, "13", "1"),
                    new CollectionListInfo("✓✓✓✓✓™", 96, 0, getIndexFromCollectionClass(MorganDollars.class), 0, 1878, 1921, "63", "0"),
                    new CollectionListInfo("🐶✌️", 1, 0, getIndexFromCollectionClass(PeaceDollars.class), 0, 1921, 1921, "2", "1"),
                    new CollectionListInfo("Eagles", 39, 0, getIndexFromCollectionClass(AmericanEagleSilverDollars.class), 0, 1986, 2020, "0", "2"),
                    new CollectionListInfo("()", 42, 0, getIndexFromCollectionClass(FirstSpouseGoldCoins.class), 0, 0, 0, "0", "0"),
                    new CollectionListInfo("S Pennies", 0, 0, getIndexFromCollectionClass(LincolnCents.class), 0, 2000, 2020, "9", "1"),
                    new CollectionListInfo("50-States", 50, 0, getIndexFromCollectionClass(StateQuarters.class), 0, 0, 0, "3", "0"),
                    new CollectionListInfo("$PATH", 56, 0, getIndexFromCollectionClass(NationalParkQuarters.class), 0, 0, 0, "5", "0"),
                    new CollectionListInfo("CHAR(0x54)", 11, 0, getIndexFromCollectionClass(NativeAmericanDollars.class), 0, 2010, 2020, "2", "1"),
                    new CollectionListInfo("<a href=\"#\">Click Here</a>", 14, 0, getIndexFromCollectionClass(AmericanEagleSilverDollars.class), 0, 1986, 1999, "0", "1"),
                    new CollectionListInfo("# comment", 18, 0, getIndexFromCollectionClass(BarberQuarters.class), 0, 1892, 1916, "17", "0"),
                    new CollectionListInfo("American_Women", 60, 0, getIndexFromCollectionClass(AmericanWomenQuarters.class), 0, 0, 0, "15", "0"),
                    new CollectionListInfo("Small Cents", 442, 0, getIndexFromCollectionClass(SmallCents.class), 0, 1856, 2023, "3151", "4063265"),
                    new CollectionListInfo("Large Cents", 68, 0, getIndexFromCollectionClass(LargeCents.class), 0, 1793, 1857, "0", "1089"),
                    new CollectionListInfo("All Nickels", 380, 0, getIndexFromCollectionClass(AllNickels.class), 0, 1866, 2022, "2255", "1006632993"),
                    new CollectionListInfo("Half Dimes", 96, 0, getIndexFromCollectionClass(HalfDimes.class), 0, 1794, 1873, "27", "577"),
                    new CollectionListInfo("Silver Dimes", 249, 0, getIndexFromCollectionClass(SilverDimes.class), 0, 1793, 2023, "287", "7516192801"),
                    new CollectionListInfo("Early Dimes", 144, 0, getIndexFromCollectionClass(EarlyDimes.class), 0, 1796, 1891, "59", "929"),
                    new CollectionListInfo("Clad Quarters", 634, 0, getIndexFromCollectionClass(CladQuarters.class), 0, 0, 0, "2383", "122880"),
                    new CollectionListInfo("Washington Silver", 230, 0, getIndexFromCollectionClass(WashingtonSilver.class), 0, 0, 0, "15", "32985348841472"),
                    new CollectionListInfo("Early Quarters", 244, 0, getIndexFromCollectionClass(EarlyQuarters.class), 0, 1796, 1930, "63", "6753"),
                    new CollectionListInfo("Small Dollars", 212, 0, getIndexFromCollectionClass(SmallDollars.class), 0, 0, 0, "143", "962072674304"),
                    new CollectionListInfo("Silver Half Dollars", 231, 0, getIndexFromCollectionClass(SilverHalfDollars.class), 0, 1892, 2023, "287", "62914593"),
                    new CollectionListInfo("Trimes", 49, 0, getIndexFromCollectionClass(Trimes.class), 0, 1851, 1889, "0", "25"),
                    new CollectionListInfo("Twenty Cents", 7, 0, getIndexFromCollectionClass(TwentyCents.class), 0, 1875, 1878, "0", "1"),
                    new CollectionListInfo("Two Cents", 10, 0, getIndexFromCollectionClass(TwoCents.class), 0, 1864, 1873, "0", "1"),
                    new CollectionListInfo("West Point", 21, 0, getIndexFromCollectionClass(WestPoint.class), 0, 0, 0, "0", "0"),
                    new CollectionListInfo("Early Dollars", 86, 0, getIndexFromCollectionClass(EarlyDollars.class), 0, 1794, 1885, "59", "1099511628353"),
                    new CollectionListInfo("Early Half Dollars", 158, 0, getIndexFromCollectionClass(EarlyHalfDollars.class), 0, 1794, 1891, "59", "577"),
                    new CollectionListInfo("Cartwheels", 198, 0, getIndexFromCollectionClass(Cartwheels.class), 0, 1878, 2023, "63", "128849018913"),
                    new CollectionListInfo("Half Cents", 44, 0, getIndexFromCollectionClass(HalfCents.class), 0, 1793, 1857, "0", "1"),
                    new CollectionListInfo("Coin Sets", 182, 0, getIndexFromCollectionClass(CoinSets.class), 0, 1947, 2023, "0", "492581209243649"),
                    new CollectionListInfo("Kennedy Half Dollars", 232, 0, getIndexFromCollectionClass(KennedyHalfDollars.class), 0, 1964, 2023, "2439", "35184372088873"),
                    new CollectionListInfo("Roosevelt Dimes", 284, 0, getIndexFromCollectionClass(RooseveltDimes.class), 0, 1946, 2023, "2511", "35184372088873"),
                    new CollectionListInfo("Washington Quarters", 271, 0, getIndexFromCollectionClass(WashingtonQuarters.class), 0, 1932, 1998, "399", "35184372088841"),
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
