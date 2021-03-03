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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

public class ParcelableHashMap extends HashMap<String, Object> implements Parcelable {

    /**
     * Creates a parcelable hash map object
     */
    public ParcelableHashMap() {
        super();
    }

    /**
     * Creates a hashmap from a parcel containing strings, ints, and booleans
     * @param in Parcel object
     */
    protected ParcelableHashMap(Parcel in) {
        super();
        int numStrings = in.readInt();
        int numInts = in.readInt();
        int numBooleans = in.readInt();
        for (int i = 0; i < numStrings; i++) {
            this.put(in.readString(), in.readString());
        }
        for (int i = 0; i < numInts; i++) {
            this.put(in.readString(), in.readInt());
        }
        for (int i = 0; i < numBooleans; i++) {
            this.put(in.readString(), (in.readByte() & 0x1) != 0);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ArrayList<String> stringMap = new ArrayList<>();
        ArrayList<String> intMap = new ArrayList<>();
        ArrayList<String> booleanMap = new ArrayList<>();
        for (String key : this.keySet()) {
            Object value = this.get(key);
            if (value instanceof String) {
                stringMap.add(key);
            } else if (value instanceof Integer) {
                intMap.add(key);
            } else if (value instanceof Boolean) {
                booleanMap.add(key);
            } else {
                throw new UnsupportedOperationException("Found unsupported type in ParcelableHashMap");
            }
        }
        dest.writeInt(stringMap.size());
        dest.writeInt(intMap.size());
        dest.writeInt(booleanMap.size());
        for (String key : stringMap) {
            dest.writeString(key);
            dest.writeString((String) this.get(key));
        }
        for (String key : intMap) {
            dest.writeString(key);
            Integer value = (Integer) this.get(key);
            dest.writeInt(value != null ? value : 0);
        }
        for (String key : booleanMap) {
            dest.writeString(key);
            Boolean value = (Boolean) this.get(key);
            dest.writeByte((byte) ((value != null && value) ? 0x1 : 0x0));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParcelableHashMap> CREATOR = new Creator<ParcelableHashMap>() {
        @Override
        public ParcelableHashMap createFromParcel(Parcel in) {
            return new ParcelableHashMap(in);
        }

        @Override
        public ParcelableHashMap[] newArray(int size) {
            return new ParcelableHashMap[size];
        }
    };
}
