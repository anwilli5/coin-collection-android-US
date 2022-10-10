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
import android.util.JsonReader;
import android.util.JsonWriter;

import com.spencerpages.R;

import java.io.IOException;

/**
 * Coin contained in a collection
 */
public class CoinSlot implements Parcelable {

    /**
     * id of the row for this coin in the database
     */
    private long mDatabaseId = 0;

    /**
     * Name of the coin (Ex: "2009", or "Kentucky")
     */
    private String mIdentifier;

    /**
     * Mint mark of the coin, if any
     */
    private String mMint;

    /**
     * Whether the coin is collected or not
     */
    private boolean mInCollection = false;

    /**
     * Whether the advanced info has changed, and has not yet been written to the database
     */
    private boolean mAdvInfoHasChanged = false;

    // In the database, we store the index into the grade and quantity arrays
    // so we can use these values efficiently.  For notes, we have to store the
    // - Note: these default values must match the DB defaults
    private Integer mAdvancedGrades = 0;
    private Integer mAdvancedQuantities = 0;
    private String mAdvancedNotes = "";

    /**
     * Sort order
     **/
    private int mSortOrder;

    /**
     * Whether the coin is custom (i.e. added by the user after the collection was created)
     */
    private boolean mCustomCoin = false;

    // Database keys
    public final static String COL_COIN_ID = "_id";
    public final static String COL_COIN_IDENTIFIER = "coinIdentifier";
    public final static String COL_COIN_MINT = "coinMint";
    public final static String COL_IN_COLLECTION = "inCollection";
    public final static String COL_ADV_GRADE_INDEX = "advGradeIndex";
    public final static String COL_ADV_QUANTITY_INDEX = "advQuantityIndex";
    public final static String COL_ADV_NOTES = "advNotes";
    public final static String COL_SORT_ORDER = "sortOrder";
    public final static String COL_CUSTOM_COIN = "customCoin";

    // Database helpers
    public final static String COIN_SLOT_COIN_ID_WHERE_CLAUSE = COL_COIN_ID + "=?";

    // In earlier versions of the app (prior to DB version 17), coins were guaranteed to have
    // unique name/mints, so those were used to index into the DB. But DB version 17 lets users
    // add custom coins, breaking this assumption. However old DB upgrades should use the legacy
    // where clause, to continue to work correctly.
    public final static String COIN_SLOT_NAME_MINT_WHERE_CLAUSE = COL_COIN_IDENTIFIER + "=? AND " + COL_COIN_MINT + "=?";

    /**
     * Constructor used when pulling the collection from the database with advanced info
     *
     * @param databaseId         id of the coin in the database
     * @param identifier         coin name
     * @param mint               coin mint
     * @param inCollection       whether the coin has been collected
     * @param advancedGrades     coin grade info
     * @param advancedQuantities coin quantity info
     * @param advancedNotes      coin notes
     * @param sortOrder          sort order in the collection
     * @param customCoin         whether the coin was manually added by the user
     */
    public CoinSlot(long databaseId, String identifier, String mint, boolean inCollection, Integer advancedGrades,
                    Integer advancedQuantities, String advancedNotes, int sortOrder, boolean customCoin) {
        mDatabaseId = databaseId;
        mIdentifier = identifier;
        mMint = mint;
        mInCollection = inCollection;
        mAdvancedGrades = advancedGrades;
        mAdvancedQuantities = advancedQuantities;
        mAdvancedNotes = advancedNotes;
        mSortOrder = sortOrder;
        mCustomCoin = customCoin;
    }

    /**
     * Constructor used when pulling the collection from the database, without advanced info
     *
     * @param databaseId   id of the coin in the database
     * @param identifier   coin name
     * @param mint         coin mint
     * @param inCollection whether the coin has been collected
     * @param sortOrder    sort order in the collection
     * @param customCoin   whether the coin was manually added by the user
     */
    public CoinSlot(long databaseId, String identifier, String mint, boolean inCollection, int sortOrder, boolean customCoin) {
        mDatabaseId = databaseId;
        mIdentifier = identifier;
        mMint = mint;
        mInCollection = inCollection;
        mSortOrder = sortOrder;
        mCustomCoin = customCoin;
    }

    /**
     * Constructor used when creating a new collection from scratch
     *
     * @param identifier coin name
     * @param mint       coin mint
     * @param sortOrder  sort order in collection
     */
    public CoinSlot(String identifier, String mint, int sortOrder) {
        mIdentifier = identifier;
        mMint = mint;
        mSortOrder = sortOrder;
    }

    public void setDatabaseId(long databaseId) {
        this.mDatabaseId = databaseId;
    }

    public long getDatabaseId() {
        return mDatabaseId;
    }

    public void setInCollection(boolean inCollection) {
        this.mInCollection = inCollection;
    }

    public void setIdentifier(String identifier) {
        this.mIdentifier = identifier;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setMint(String mint) {
        this.mMint = mint;
    }

    public String getMint() {
        return mMint;
    }

    void setAdvInfoChanged(boolean changed) {
        this.mAdvInfoHasChanged = changed;
    }

    public boolean isInCollection() {
        return mInCollection;
    }

    public Integer isInCollectionInt() {
        return this.isInCollection() ? 1 : 0;
    }

    public Integer isInCollectionStringRes() {
        return this.isInCollection() ? R.string.collected : R.string.missing;
    }

    boolean hasAdvInfoChanged() {
        return mAdvInfoHasChanged;
    }

    public Integer getAdvancedGrades() {
        return mAdvancedGrades;
    }

    public Integer getAdvancedQuantities() {
        return mAdvancedQuantities;
    }

    public String getAdvancedNotes() {
        return mAdvancedNotes;
    }

    public void setAdvancedGrades(Integer advancedGrades) {
        this.mAdvancedGrades = advancedGrades;
    }

    public void setAdvancedQuantities(Integer advancedQuantities) {
        this.mAdvancedQuantities = advancedQuantities;
    }

    public void setAdvancedNotes(String advancedNotes) {
        this.mAdvancedNotes = advancedNotes;
    }

    public int getSortOrder() {
        return this.mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.mSortOrder = sortOrder;
    }

    public boolean isCustomCoin() {
        return mCustomCoin;
    }

    public Integer isCustomCoinInt() {
        return this.isCustomCoin() ? 1 : 0;
    }

    public void setCustomCoin(boolean customCoin) {
        this.mCustomCoin = customCoin;
    }

    /**
     * Get the coin slot parameters to export to legacy CSV
     *
     * @return string array with coin slot data
     */
    public String[] getLegacyCsvExportProperties() {
        return new String[]{
                mIdentifier,
                mMint,
                String.valueOf(isInCollectionInt()),
                String.valueOf(mAdvancedGrades),
                String.valueOf(mAdvancedQuantities),
                mAdvancedNotes};
    }

    /**
     * Get the coin slot parameters to export to new CSV
     *
     * @return string array with coin slot data
     */
    public String[] getCsvExportProperties() {
        return new String[]{
                mIdentifier,
                mMint,
                String.valueOf(isInCollectionInt()),
                String.valueOf(mAdvancedGrades),
                String.valueOf(mAdvancedQuantities),
                mAdvancedNotes,
                String.valueOf(mSortOrder),
                String.valueOf(isCustomCoinInt())};
    }

    /**
     * Get the headers for the coin CSV file
     *
     * @return string array with column names
     */
    public static String[] getCsvExportHeader() {
        return new String[]{
                COL_COIN_IDENTIFIER,
                COL_COIN_MINT,
                COL_IN_COLLECTION,
                COL_ADV_GRADE_INDEX,
                COL_ADV_QUANTITY_INDEX,
                COL_ADV_NOTES,
                COL_SORT_ORDER,
                COL_CUSTOM_COIN};
    }

    /**
     * Write out the JSON representation (for exporting)
     *
     * @param writer JsonWriter to write to
     * @throws IOException if an error occurred
     */
    public void writeToJson(JsonWriter writer) throws IOException {

        writer.beginObject();
        writer.name(COL_COIN_IDENTIFIER).value(mIdentifier);
        writer.name(COL_COIN_MINT).value(mMint);
        writer.name(COL_IN_COLLECTION).value(mInCollection);
        writer.name(COL_ADV_GRADE_INDEX).value(mAdvancedGrades);
        writer.name(COL_ADV_QUANTITY_INDEX).value(mAdvancedQuantities);
        writer.name(COL_ADV_NOTES).value(mAdvancedNotes);
        writer.name(COL_SORT_ORDER).value(mSortOrder);
        writer.name(COL_CUSTOM_COIN).value(mCustomCoin);
        writer.endObject();
    }

    /**
     * Create a CoinSlot from imported JSON file
     *
     * @param reader    JsonReader to read from
     * @param coinIndex index of coin in the list used for default sort order
     * @throws IOException if an error occurred
     */
    public CoinSlot(JsonReader reader, int coinIndex) throws IOException {

        String identifier = "";
        String mint = "";
        boolean inCollection = false;
        int advancedGrades = 0;
        int advancedQuantities = 0;
        String advancedNotes = "";
        int sortOrder = coinIndex;
        boolean customCoin = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case COL_COIN_IDENTIFIER:
                    identifier = reader.nextString();
                    break;
                case COL_COIN_MINT:
                    mint = reader.nextString();
                    break;
                case COL_IN_COLLECTION:
                    inCollection = reader.nextBoolean();
                    break;
                case COL_ADV_GRADE_INDEX:
                    advancedGrades = reader.nextInt();
                    break;
                case COL_ADV_QUANTITY_INDEX:
                    advancedQuantities = reader.nextInt();
                    break;
                case COL_ADV_NOTES:
                    advancedNotes = reader.nextString();
                    break;
                case COL_SORT_ORDER:
                    sortOrder = reader.nextInt();
                    break;
                case COL_CUSTOM_COIN:
                    customCoin = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        mIdentifier = identifier;
        mMint = mint;
        mInCollection = inCollection;
        mAdvancedGrades = advancedGrades;
        mAdvancedQuantities = advancedQuantities;
        mAdvancedNotes = advancedNotes;
        mSortOrder = sortOrder;
        mCustomCoin = customCoin;
    }

    /**
     * Returns true if an element is present in a string array
     *
     * @param in    string array to look in
     * @param index string position
     * @return true if the element at the index is present and not empty
     */
    private boolean isPresentInStringArray(String[] in, int index) {
        return in.length > index && in[index].length() != 0;
    }

    /**
     * Create a CoinSlot from imported string array
     *
     * @param in input String[]
     */
    public CoinSlot(String[] in, int coinIndex) {
        mIdentifier = isPresentInStringArray(in, 0) ? in[0] : "";
        mMint = isPresentInStringArray(in, 1) ? in[1] : "";
        mInCollection = (isPresentInStringArray(in, 2) && (Integer.parseInt(in[2]) != 0));
        mAdvancedGrades = isPresentInStringArray(in, 3) ? Integer.parseInt(in[3]) : 0;
        mAdvancedQuantities = isPresentInStringArray(in, 4) ? Integer.parseInt(in[4]) : 0;
        mAdvancedNotes = isPresentInStringArray(in, 5) ? in[5] : "";
        mSortOrder = isPresentInStringArray(in, 6) ? Integer.parseInt(in[6]) : coinIndex;
        mCustomCoin = (isPresentInStringArray(in, 7) && (Integer.parseInt(in[7]) != 0));
    }

    /**
     * Creates a copy of the current coin with a different name and mint mark
     * Note: Sets the sort order to the original + 1
     *
     * @param newIdentifier new coin identifier
     * @param newMint       new mint mark
     * @param isCustomCoin  true if the copy should be marked as a custom coin
     * @return the new CoinSlot object
     */
    public CoinSlot copy(String newIdentifier, String newMint, boolean isCustomCoin) {
        return new CoinSlot(
                0, // Set when the database is written
                newIdentifier,
                newMint,
                mInCollection,
                mAdvancedGrades,
                mAdvancedQuantities,
                mAdvancedNotes,
                mSortOrder + 1,
                isCustomCoin);
    }

    /* We make this object Parcelable so that the list can be passed between Activities in the case
     * where a screen orientation change occurs.
     */
    private CoinSlot(Parcel in) {
        mDatabaseId = in.readLong();
        mIdentifier = in.readString();
        mMint = in.readString();
        mInCollection = in.readByte() != 0;
        mAdvInfoHasChanged = in.readByte() != 0;
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
        mSortOrder = in.readInt();
        mCustomCoin = in.readByte() != 0;
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
        dest.writeLong(mDatabaseId);
        dest.writeString(mIdentifier);
        dest.writeString(mMint);
        dest.writeByte((byte) (mInCollection ? 1 : 0));
        dest.writeByte((byte) (mAdvInfoHasChanged ? 1 : 0));
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
        dest.writeInt(mSortOrder);
        dest.writeByte((byte) (mCustomCoin ? 1 : 0));
    }

    // NOTE: This will return true if identifier and mint are the same
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoinSlot coinSlot = (CoinSlot) o;
        return mIdentifier.equals(coinSlot.mIdentifier) &&
                mMint.equals(coinSlot.mMint);
    }
}