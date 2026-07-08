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
package com.coincollection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Regression tests for issue #406: {@link NumberFormatException} thrown from
 * {@link CollectionListInfo#getCheckboxFlagsAsLong()} /
 * {@link CollectionListInfo#getMintMarkFlagsAsLong()} during a database upgrade.
 *
 * A collection imported from a hand-edited or spreadsheet-mangled file can carry
 * a non-numeric flag string (e.g. {@code "2.68435E+8"}) in the checkbox/mint-mark
 * column. Nothing parsed the value at import time, so it sat dormant in the
 * database until a later upgrade (SmallDollars.onCollectionDatabaseUpgrade →
 * hasSACDollars → getCheckboxFlagsAsLong) tried to parse it and crashed the app
 * on every startup.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionListInfoFlagParsingTests {

    private static CollectionListInfo makeInfoWithFlags(String mintMarkFlags, String checkboxFlags) {
        return new CollectionListInfo("Test Collection", 0, 0, 0, 0, 0, 0,
                mintMarkFlags, checkboxFlags);
    }

    @Test
    public void parseFlagString_handlesValidValue() {
        assertEquals(268435456L, CollectionListInfo.parseFlagString("268435456"));
    }

    @Test
    public void parseFlagString_returnsZeroForEmptyOrNull() {
        assertEquals(0L, CollectionListInfo.parseFlagString(""));
        assertEquals(0L, CollectionListInfo.parseFlagString(null));
    }

    @Test
    public void parseFlagString_recoversSpreadsheetFormats() {
        // Excel/Sheets rewrite large integers when the CSV is edited and saved.
        // Trailing-decimal and stray-whitespace forms recover exactly.
        assertEquals(268435456L, CollectionListInfo.parseFlagString("268435456.0"));
        assertEquals(268435456L, CollectionListInfo.parseFlagString(" 268435456 "));
        // Scientific notation is inherently lossy (the spreadsheet already
        // dropped precision) but is still parsed to its nearest integer.
        assertEquals(268435000L, CollectionListInfo.parseFlagString("2.68435E+8"));
    }

    @Test
    public void parseFlagString_returnsZeroForUnrecoverableValues() {
        assertEquals(0L, CollectionListInfo.parseFlagString("true"));
        assertEquals(0L, CollectionListInfo.parseFlagString("not a number"));
        // Numeric overflow beyond Long.MAX_VALUE cannot be represented
        assertEquals(0L, CollectionListInfo.parseFlagString("99999999999999999999"));
    }

    /**
     * Reproduces issue #406: the exact call chain that crashed was
     * SmallDollars.onCollectionDatabaseUpgrade → hasSACDollars →
     * getCheckboxFlagsAsLong → Long.parseLong. It must no longer throw.
     */
    @Test
    public void getCheckboxFlagsAsLong_doesNotThrowOnMangledValue() {
        CollectionListInfo info = makeInfoWithFlags("0", "2.68435E+8");
        assertEquals(268435000L, info.getCheckboxFlagsAsLong());
        info.hasSACDollars();
    }

    @Test
    public void getMintMarkFlagsAsLong_doesNotThrowOnMangledValue() {
        CollectionListInfo info = makeInfoWithFlags("2.68435E+8", "0");
        assertEquals(268435000L, info.getMintMarkFlagsAsLong());
        info.hasMintMarks();
    }

    /**
     * Import path: the {@code String[]} constructor (CSV import) must recover
     * spreadsheet-mangled flag values so a poisoned value never reaches the
     * database and the user's configuration is preserved where possible.
     */
    @Test
    public void stringArrayConstructor_recoversMangledFlags() {
        String[] in = new String[]{
                "Test Collection", // 0 name
                "Lincoln Cents",   // 1 coin type
                "0",               // 2 collected
                "0",               // 3 total
                "0",               // 4 display
                "2019",            // 5 start year
                "2020",            // 6 end year
                "0",               // 7 mint mark legacy
                "0",               // 8 checkbox legacy
                "2.68435E+8",      // 9 mint mark flags (scientific notation)
                "268435456.0"      // 10 checkbox flags (trailing decimal)
        };
        CollectionListInfo info = new CollectionListInfo(in);
        assertEquals(268435000L, info.getMintMarkFlagsAsLong());
        assertEquals(268435456L, info.getCheckboxFlagsAsLong());
    }
}
