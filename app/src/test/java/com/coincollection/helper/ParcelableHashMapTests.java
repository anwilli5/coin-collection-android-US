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
package com.coincollection.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Tests for {@link ParcelableHashMap}, which carries collection parameters
 * across configuration changes
 */
@RunWith(RobolectricTestRunner.class)
public class ParcelableHashMapTests {

    private static ParcelableHashMap roundTrip(ParcelableHashMap map) {
        Parcel parcel = Parcel.obtain();
        try {
            map.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return ParcelableHashMap.CREATOR.createFromParcel(parcel);
        } finally {
            parcel.recycle();
        }
    }

    @Test
    public void roundTripsSupportedTypes() {
        ParcelableHashMap map = new ParcelableHashMap();
        map.put("string", "value");
        map.put("int", 42);
        map.put("boolean", true);
        assertEquals(map, roundTrip(map));
    }

    /**
     * A null value used to match none of the supported types, so writing the
     * map threw UnsupportedOperationException
     */
    @Test
    public void roundTripsNullValue() {
        ParcelableHashMap map = new ParcelableHashMap();
        map.put("string", "value");
        map.put("missing", null);
        ParcelableHashMap result = roundTrip(map);
        assertEquals(map, result);
        assertTrue(result.containsKey("missing"));
        assertNull(result.get("missing"));
    }
}
