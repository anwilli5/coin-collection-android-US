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

import com.coincollection.CoinSlot;
import com.coincollection.CollectionListInfo;

import static com.spencerpages.MainApplication.getIndexFromCollectionNameStr;

public class SharedTest {

    public static final CollectionListInfo[] COLLECTION_LIST_INFO_SCENARIOS =
            {
                    new CollectionListInfo("My Pennies", 275, 0, getIndexFromCollectionNameStr("Pennies"), 0, 1909, 2020, 15, 1),
                    new CollectionListInfo("@#$#@%$#^$#$%^%$&^^&%$", 19, 0, getIndexFromCollectionNameStr("Nickels"), 0, 1938, 2020, 9, 0),
                    new CollectionListInfo("My\\Collection/", 2, 0, getIndexFromCollectionNameStr("Dimes"), 0, 1950, 1951, 2, 1),
                    new CollectionListInfo("my,coll,ection.", 65, 0, getIndexFromCollectionNameStr("Quarters"), 0, 1932, 1998, 3, 0),
                    new CollectionListInfo("<>STATE QUARTERS\0\n\n", 112, 0, getIndexFromCollectionNameStr("State Quarters"), 0, 0, 0, 7, 4),
                    new CollectionListInfo("</></>", 55, 0, getIndexFromCollectionNameStr("National Park Quarters"), 0, 0, 0, 5, 0),
                    new CollectionListInfo("~", 56, 0, getIndexFromCollectionNameStr("Half-Dollars"), 0, 1964, 2020, 2, 1),
                    new CollectionListInfo("Eisenhower", 2, 0, getIndexFromCollectionNameStr("Eisenhower Dollars"), 0, 1971, 1971, 7, 1),
                    new CollectionListInfo("Susan's \"Dollars!", 4, 0, getIndexFromCollectionNameStr("Susan B. Anthony Dollars"), 0, 1979, 1999, 2, 0),
                    new CollectionListInfo("; DROP TABLES;", 16, 0, getIndexFromCollectionNameStr("Sacagawea/Native American Dollars"), 0, 2002, 2009, 7, 1),
                    new CollectionListInfo("😁😁😁😁😁😍😄", 78, 0, getIndexFromCollectionNameStr("Presidential Dollars"), 0, 0, 0, 7, 0),
                    new CollectionListInfo("| | | | | ", 2, 0, getIndexFromCollectionNameStr("Indian Head Cents"), 0, 1859, 1909, 9, 0),
                    new CollectionListInfo(" ", 32, 0, getIndexFromCollectionNameStr("Liberty Head Nickels"), 0, 1883, 1912, 7, 1),
                    new CollectionListInfo("  ", 25, 0, getIndexFromCollectionNameStr("Barber Dimes"), 0, 1892, 1916, 2, 0),
                    new CollectionListInfo("Barb Quarts", 25, 0, getIndexFromCollectionNameStr("Barber Quarters"), 0, 1892, 1916, 2, 0),
                    new CollectionListInfo("_)(*&^%", 12, 0, getIndexFromCollectionNameStr("Standing Liberty Quarters"), 0, 1916, 1930, 9, 0),
                    new CollectionListInfo("Frankline", 19, 0, getIndexFromCollectionNameStr("Franklin Half Dollars"), 0, 1948, 1963, 13, 1),
                    new CollectionListInfo("✓✓✓✓✓™", 96, 0, getIndexFromCollectionNameStr("Morgan Dollars"), 0, 1878, 1921, 63, 0),
                    new CollectionListInfo("🐶✌️", 1, 0, getIndexFromCollectionNameStr("Peace Dollars"), 0, 1921, 1921, 2, 1),
                    new CollectionListInfo("Eagles", 39, 0, getIndexFromCollectionNameStr("American Eagle Silver Dollars"), 0, 1986, 2020, 0, 2),
                    new CollectionListInfo("()", 41, 0, getIndexFromCollectionNameStr("First Spouse Gold Coins"), 0, 0, 0, 0, 0),
                    new CollectionListInfo("S Pennies", 0, 0, getIndexFromCollectionNameStr("Pennies"), 0, 2000, 2020, 9, 1),
                    new CollectionListInfo("50-States", 50, 0, getIndexFromCollectionNameStr("State Quarters"), 0, 0, 0, 3, 0),
                    new CollectionListInfo("$PATH", 55, 0, getIndexFromCollectionNameStr("National Park Quarters"), 0, 0, 0, 5, 0),
                    new CollectionListInfo("CHAR(0x54)", 11, 0, getIndexFromCollectionNameStr("Sacagawea/Native American Dollars"), 0, 2010, 2020, 2, 1),
                    new CollectionListInfo("<a href=\"#\">Click Here</a>", 14, 0, getIndexFromCollectionNameStr("American Eagle Silver Dollars"), 0, 1986, 1999, 0, 1),
                    new CollectionListInfo("# comment", 18, 0, getIndexFromCollectionNameStr("Barber Quarters"), 0, 1892, 1916, 17, 0),
            };

    public static final CoinSlot[] COIN_SLOT_SCENARIOS =
            {
                    new CoinSlot("1989", "", true, 0, 0, ""),
                    new CoinSlot("1776-1976", "P", false, 2, 10, "Advanced Notes"),
                    new CoinSlot("State Park", "CC", true, 20, 1, "293370#$%@#^$#@^"),
                    new CoinSlot("2000 Type 1", "D", true, 0, 0, "These are my notes\n\n"),
                    new CoinSlot("Some Coin", "S", false, 11, 11, ""),
            };

    /**
     * Compare two CollectionListInfo objects to ensure they're the same
     * @param base CollectionListInfo
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
                (base.getMintMarkFlags() == check.getMintMarkFlags()) &&
                (base.getCheckboxFlags() == check.getCheckboxFlags()));
    }

    /**
     * Compare two CoinSlot objects to ensure they're the same
     * @param base CoinSlot
     * @param check CoinSlot
     * @return true if they have the same contents, false otherwise
     */
    public static boolean compareCoinSlots(CoinSlot base, CoinSlot check) {
        // TODO - Not sure how to add assertions here
        return (base.getIdentifier().equals(check.getIdentifier()) &&
                (base.getMint().equals(check.getMint())) &&
                (base.isInCollection() == check.isInCollection()) &&
                (base.getAdvancedGrades().equals(check.getAdvancedGrades())) &&
                (base.getAdvancedQuantities().equals(check.getAdvancedQuantities())) &&
                (base.getAdvancedNotes().equals(check.getAdvancedNotes())));
    }
}
