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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Coin contained in a collection
 */
public class CoinSlot implements Parcelable {

    /** mIdentifierList Contains a list of the main coin identifiers (Ex: "2009", or "Kentucky") */
    private final String mIdentifier;

    /** mMintList Contains a list of the mint marks associated with each coin, if any */
    private final String mMint;

    // These are public so that we can reach in and grab these values to save them off
    // We will keep track of which items need to be pushed back

    // List holding whether each coin is in the collection
    private boolean mInCollection;

    // Lists needed to support the advanced view
    private boolean mIndexHasChanged = false;

    // In the database, we store the index into the grade and quantity arrays
    // so we can use these values efficiently.  For notes, we have to store the
    private Integer mAdvancedGrades;
    private Integer mAdvancedQuantities;
    private String mAdvancedNotes;

    // Database keys
    public final static String COL_COIN_IDENTIFIER = "coinIdentifier";
    public final static String COL_COIN_MINT = "coinMint";
    final static String COL_IN_COLLECTION = "inCollection";
    final static String COL_ADV_GRADE_INDEX = "advGradeIndex";
    final static String COL_ADV_QUANTITY_INDEX = "advQuantityIndex";
    final static String COL_ADV_NOTES = "advNotes";

    // Database helpers
    final static String COIN_SLOT_WHERE_CLAUSE = COL_COIN_IDENTIFIER + "=? AND " + COL_COIN_MINT + "=?";

    public CoinSlot (String identifier, String mint, boolean inCollection){
        mIdentifier = identifier;
        mMint = mint;
        mInCollection = inCollection;
    }

    public CoinSlot (String identifier, String mint){
        mIdentifier = identifier;
        mMint = mint;
        mInCollection = false;
    }

    void setInCollection (boolean inCollection) {
        mInCollection = inCollection;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public String getMint() {
        return mMint;
    }

    void setIndexChanged(boolean changed) {
        mIndexHasChanged = changed;
    }

    public boolean isInCollection() {
        return mInCollection;
    }

    boolean hasIndexChanged() {
        return mIndexHasChanged;
    }

    Integer getAdvancedGrades() {
        return mAdvancedGrades;
    }

    Integer getAdvancedQuantities() {
        return mAdvancedQuantities;
    }

    String getAdvancedNotes() {
        return mAdvancedNotes;
    }

    void setAdvancedGrades(Integer advancedGrades) {
        this.mAdvancedGrades = advancedGrades;
    }

    void setAdvancedQuantities(Integer advancedQuantities) {
        this.mAdvancedQuantities = advancedQuantities;
    }

    void setAdvancedNotes(String advancedNotes) {
        this.mAdvancedNotes = advancedNotes;
    }

    /* We make this object Parcelable so that the list can be passed between Activities in the case
     * where a screen orientation change occurs.
     */
    private CoinSlot(Parcel in) {
        mIdentifier = in.readString();
        mMint = in.readString();
        mInCollection = in.readByte() != 0;
        mIndexHasChanged = in.readByte() != 0;
        if (in.readByte() == 0) {
            mAdvancedGrades = null;
        } else {
            mAdvancedGrades = in.readInt();
        }
        if (in.readByte() == 0) {
            mAdvancedQuantities = null;
        } else {
            mAdvancedQuantities = in.readInt();
        }
        mAdvancedNotes = in.readString();
    }

    public static final Creator<CoinSlot> CREATOR = new Creator<CoinSlot>() {
        @Override
        public CoinSlot createFromParcel(Parcel in) {
            return new CoinSlot(in);
        }

        @Override
        public CoinSlot[] newArray(int size) {
            return new CoinSlot[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIdentifier);
        dest.writeString(mMint);
        dest.writeByte((byte) (mInCollection ? 1 : 0));
        dest.writeByte((byte) (mIndexHasChanged ? 1 : 0));
        if (mAdvancedGrades == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mAdvancedGrades);
        }
        if (mAdvancedQuantities == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mAdvancedQuantities);
        }
        dest.writeString(mAdvancedNotes);
    }
}