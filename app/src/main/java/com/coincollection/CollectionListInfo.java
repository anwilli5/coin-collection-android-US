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

import static com.coincollection.CollectionPage.SIMPLE_DISPLAY;
import static com.coincollection.ExportImportHelper.JSON_COIN_LIST;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.spencerpages.MainApplication;
import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BuffaloNickels;
import com.spencerpages.collections.EisenhowerDollar;
import com.spencerpages.collections.FranklinHalfDollars;
import com.spencerpages.collections.HalfDimes;
import com.spencerpages.collections.IndianHeadCents;
import com.spencerpages.collections.JeffersonNickels;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LargeCents;
import com.spencerpages.collections.LibertyHeadNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.MercuryDimes;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.SmallCents;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.WalkingLibertyHalfDollars;
import com.spencerpages.collections.WashingtonQuarters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Object used to represent each collection in the various list of collections
 * (the main page and the reorder page)
 */
public class CollectionListInfo implements Parcelable {
    private final String mCollectionName;
    private int mTotalCoinsInCollection;
    private int mTotalCoinsCollected;
    private final int mCollectionTypeIndex;
    private final int mDisplayType;
    private int mStartYear;
    private int mEndYear;
    private String mMintMarkFlags;
    private String mCheckboxFlags;
    private final CollectionInfo mCollectionInfo;

    // Flags for selected mint marks
    public final static long ALL_MINT_MASK = 0x7FFFL;
    public final static long SHOW_MINT_MARKS = 0x1L;
    public final static long MINT_P = 0x2L;
    public final static long MINT_D = 0x4L;
    public final static long MINT_S = 0x8L;
    public final static long MINT_O = 0x10L;
    public final static long MINT_CC = 0x20L;
    public final static long MINT_W = 0x40L;
    public final static long MINT_S_PROOF = 0x80L;
    public final static long MINT_SILVER_PROOF = 0x100L;
    public final static long MINT_REV_PROOF = 0x200L;
    public final static long MINT_MEM_PROOF = 0x400L;
    public final static long MINT_SATIN = 0x800L;
    public final static long MINT_SETS = 0x1000L;
    public final static long MINT_PROOF_SETS = 0x2000L;
    public final static long MINT_SILVER_PROOF_SETS = 0x4000L;

    // Flags for show checkboxes options
    public final static long ALL_CHECKBOXES_MASK = 0x1FFFFFFFFFFL;
    public final static long CUSTOM_DATES = 0x1L;
    public final static long BURNISHED = 0x2L;
    public final static long TERRITORIES = 0x4L;
    public final static long SILVER_COINS = 0x8L;
    public final static long NICKEL_COINS = 0x10L;
    public final static long OLD_COINS = 0x20L;
    public final static long BUST_COINS = 0x40L;
    public final static long DRAPED_BUST_COINS = 0x80L;
    public final static long CAPPED_BUST_COINS = 0x100L;
    public final static long SEATED_COINS = 0x200L;
    public final static long CORONET_COINS = 0x400L;
    public final static long BARBER_QUARTERS = 0x800L;
    public final static long STANDING_QUARTERS = 0x1000L;
    public final static long CLASSIC_QUARTERS = 0x2000L;
    public final static long STATES_QUARTERS = 0x4000L;
    public final static long PARKS_QUARTERS = 0x8000L;
    public final static long WOMEN_QUARTERS = 0x10000L;
    public final static long EAGLE_CENTS = 0x20000L;
    public final static long INDIAN_CENTS = 0x40000L;
    public final static long WHEAT_CENTS = 0x80000L;
    public final static long MEMORIAL_CENTS = 0x100000L;
    public final static long SHIELD_CENTS = 0x200000L;
    public final static long BARBER_HALF = 0x400000L;
    public final static long WALKER_HALF = 0x800000L;
    public final static long FRANKLIN_HALF = 0x1000000L;
    public final static long KENNEDY_HALF = 0x2000000L;
    public final static long SHIELD_NICKELS = 0x4000000L;
    public final static long LIBERTY_NICKELS = 0x8000000L;
    public final static long BUFFALO_NICKELS = 0x10000000L;
    public final static long JEFFERSON_NICKELS = 0x20000000L;
    public final static long BARBER_DIMES = 0x40000000L;
    public final static long MERCURY_DIMES = 0x80000000L;
    public final static long ROOSEVELT_DIMES = 0x100000000L;
    public final static long MORGAN_DOLLARS = 0x200000000L;
    public final static long PEACE_DOLLARS = 0x400000000L;
    public final static long IKE_DOLLARS = 0x800000000L;
    public final static long EAGLE_DOLLARS = 0x1000000000L;
    public final static long SBA_DOLLARS = 0x2000000000L;
    public final static long SAC_DOLLARS = 0x4000000000L;
    public final static long PRES_DOLLARS = 0x8000000000L;
    public final static long TRADE_DOLLARS = 0x10000000000L;

    public final static HashMap<String, Long> MINT_STRING_TO_FLAGS = new HashMap<>();

    static {
        MINT_STRING_TO_FLAGS.put("P", MINT_P);
        MINT_STRING_TO_FLAGS.put("D", MINT_D);
        MINT_STRING_TO_FLAGS.put("S", MINT_S);
        MINT_STRING_TO_FLAGS.put("O", MINT_O);
        MINT_STRING_TO_FLAGS.put("CC", MINT_CC);
    }

    // Database tables and keys
    public final static String TBL_COLLECTION_INFO = "collection_info";
    public final static String COL_ID = "_id";
    public final static String COL_NAME = "name";
    public final static String COL_COIN_TYPE = "coinType";
    public final static String COL_TOTAL = "total";
    public final static String COL_DISPLAY_ORDER = "displayOrder";
    public final static String COL_DISPLAY = "display";
    public final static String COL_START_YEAR = "startYear";
    public final static String COL_END_YEAR = "endYear";
    public final static String COL_SHOW_MINT_MARKS_LEGACY = "showMintMarks";
    public final static String COL_SHOW_MINT_MARKS = "showMintMarksStr";
    public final static String COL_SHOW_CHECKBOXES_LEGACY = "showCheckboxes";
    public final static String COL_SHOW_CHECKBOXES = "showCheckboxesStr";
    public final static String JSON_KEY_COLLECTED = "collected";

    // Collections in this list use the start/end years
    private final static ArrayList<String> HAS_DATE_RANGE = new ArrayList<>(Arrays.asList(
            AmericanEagleSilverDollars.COLLECTION_TYPE,
            BarberDimes.COLLECTION_TYPE,
            BarberHalfDollars.COLLECTION_TYPE,
            BarberQuarters.COLLECTION_TYPE,
            BuffaloNickels.COLLECTION_TYPE,
            EisenhowerDollar.COLLECTION_TYPE,
            FranklinHalfDollars.COLLECTION_TYPE,
            IndianHeadCents.COLLECTION_TYPE,
            JeffersonNickels.COLLECTION_TYPE,
            KennedyHalfDollars.COLLECTION_TYPE,
            LibertyHeadNickels.COLLECTION_TYPE,
            LincolnCents.COLLECTION_TYPE,
            MercuryDimes.COLLECTION_TYPE,
            MorganDollars.COLLECTION_TYPE,
            NativeAmericanDollars.COLLECTION_TYPE,
            PeaceDollars.COLLECTION_TYPE,
            RooseveltDimes.COLLECTION_TYPE,
            StandingLibertyQuarters.COLLECTION_TYPE,
            SusanBAnthonyDollars.COLLECTION_TYPE,
            WalkingLibertyHalfDollars.COLLECTION_TYPE,
            WashingtonQuarters.COLLECTION_TYPE,
            SmallCents.COLLECTION_TYPE,
            LargeCents.COLLECTION_TYPE,
            AllNickels.COLLECTION_TYPE,
            HalfDimes.COLLECTION_TYPE));

    public CollectionListInfo(String name, int max, int collected, int index, int displayType,
                              int startYear, int stopYear, String mintMarkFlags,
                              String checkboxFlags) {
        mCollectionName = name;
        mTotalCoinsInCollection = max;
        mTotalCoinsCollected = collected;
        mCollectionTypeIndex = index;
        mDisplayType = displayType;
        mStartYear = startYear;
        mEndYear = stopYear;
        mMintMarkFlags = mintMarkFlags;
        mCheckboxFlags = checkboxFlags;
        mCollectionInfo = MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
    }

    /**
     * Creates a copy of the current collection with a different name
     *
     * @param newCollectionName The new collection name
     * @return The new collection object
     */
    public CollectionListInfo copy(String newCollectionName) {
        return new CollectionListInfo(
                newCollectionName,
                mTotalCoinsInCollection,
                mTotalCoinsCollected,
                mCollectionTypeIndex,
                mDisplayType,
                mStartYear,
                mEndYear,
                mMintMarkFlags,
                mCheckboxFlags);
    }

    public void setMax(int max) {
        mTotalCoinsInCollection = max;
    }

    public void setCollected(int numCollected) {
        mTotalCoinsCollected = numCollected;
    }

    public String getName() {
        return mCollectionName;
    }

    public int getMax() {
        return mTotalCoinsInCollection;
    }

    public int getCollected() {
        return mTotalCoinsCollected;
    }

    public int getCollectionTypeIndex() {
        return mCollectionTypeIndex;
    }

    public int getDisplayType() {
        return mDisplayType;
    }

    public String getType() {
        return mCollectionInfo.getCoinType();
    }

    public int getCoinImageIdentifier() {
        return mCollectionInfo.getCoinImageIdentifier();
    }

    /**
     * Gets the CollectionInfo object associated with this CollectionListInfo
     *
     * @return The CollectionInfo object
     */
    public CollectionInfo getCollectionObj() {
        return mCollectionInfo;
    }

    public int getStartYear() {
        return mStartYear;
    }

    public int getEndYear() {
        return mEndYear;
    }

    public String getMintMarkFlags() {
        return mMintMarkFlags;
    }

    public long getMintMarkFlagsAsLong() {
        if (mMintMarkFlags.isEmpty()){
            return 0L;
        } else {
            return Long.parseLong(mMintMarkFlags);
        }
    }

    public String getCheckboxFlags() {
        return mCheckboxFlags;
    }

    public long getCheckboxFlagsAsLong() {
        if (mCheckboxFlags.isEmpty()){
            return 0L;
        } else {
            return Long.parseLong(mCheckboxFlags);
        }
    }

    public boolean hasMintMarks() {
        return (getMintMarkFlagsAsLong() & SHOW_MINT_MARKS) != 0;
    }

    public boolean hasPMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_P) != 0;
    }

    public boolean hasDMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_D) != 0;
    }

    public boolean hasSMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_S) != 0;
    }

    public boolean hasOMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_O) != 0;
    }

    public boolean hasCCMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_CC) != 0;
    }

    public boolean hasWMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_W) != 0;
    }

    public boolean hasSProofMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_S_PROOF) != 0;
    }

    public boolean hasSilverProofMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_SILVER_PROOF) != 0;
    }

    public boolean hasRevProofMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_REV_PROOF) != 0;
    }

    public boolean hasMemProofMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_MEM_PROOF) != 0;
    }

    public boolean hasSatinMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_SATIN) != 0;
    }

    public boolean hasMintSetsMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_SETS) != 0;
    }

    public boolean hasProofSetsMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_PROOF_SETS) != 0;
    }

    public boolean hasSilverProofSetsMintMarks() {
        return (getMintMarkFlagsAsLong() & MINT_SILVER_PROOF_SETS) != 0;
    }

    public boolean hasCustomDates() {
        return (getCheckboxFlagsAsLong() & CUSTOM_DATES) != 0;
    }

    public boolean hasBurnishedCoins() {
        return (getCheckboxFlagsAsLong() & BURNISHED) != 0;
    }

    public boolean hasTerritoryCoins() {
        return (getCheckboxFlagsAsLong() & TERRITORIES) != 0;
    }

    public boolean hasSilverCoins() {
        return (getCheckboxFlagsAsLong() & SILVER_COINS) != 0;
    }

    public boolean hasNickelCoins() {
        return (getCheckboxFlagsAsLong() & NICKEL_COINS) != 0;
    }

    public boolean hasOldCoins() {
        return (getCheckboxFlagsAsLong() & OLD_COINS) != 0;
    }

    public boolean hasBustCoins() {
        return (getCheckboxFlagsAsLong() & BUST_COINS) != 0;
    }

    public boolean hasDrapedBustCoins() {
        return (getCheckboxFlagsAsLong() & DRAPED_BUST_COINS) != 0;
    }

    public boolean hasCappedBustCoins() {
        return (getCheckboxFlagsAsLong() & CAPPED_BUST_COINS) != 0;
    }

    public boolean hasSeatedCoins() {
        return (getCheckboxFlagsAsLong() & SEATED_COINS) != 0;
    }

    public boolean hasCoronetCoins() {
        return (getCheckboxFlagsAsLong() & CORONET_COINS) != 0;
    }

    public boolean hasBarberQuarters() {
        return (getCheckboxFlagsAsLong() & BARBER_QUARTERS) != 0;
    }

    public boolean hasStandingQuarters() {
        return (getCheckboxFlagsAsLong() & STANDING_QUARTERS) != 0;
    }

    public boolean hasClassicQuarters() {
        return (getCheckboxFlagsAsLong() & CLASSIC_QUARTERS) != 0;
    }

    public boolean hasStatesQuarters() {
        return (getCheckboxFlagsAsLong() & STATES_QUARTERS) != 0;
    }

    public boolean hasParksQuarters() {
        return (getCheckboxFlagsAsLong() & PARKS_QUARTERS) != 0;
    }

    public boolean hasWomenQuarters() {
        return (getCheckboxFlagsAsLong() & WOMEN_QUARTERS) != 0;
    }

    public boolean hasEagleCents() {
        return (getCheckboxFlagsAsLong() & EAGLE_CENTS) != 0;
    }

    public boolean hasIndianCents() {
        return (getCheckboxFlagsAsLong() & INDIAN_CENTS) != 0;
    }

    public boolean hasWheatCents() {
        return (getCheckboxFlagsAsLong() & WHEAT_CENTS) != 0;
    }

    public boolean hasMemorialCents() {
        return (getCheckboxFlagsAsLong() & MEMORIAL_CENTS) != 0;
    }

    public boolean hasShieldCents() {
        return (getCheckboxFlagsAsLong() & SHIELD_CENTS) != 0;
    }

    public boolean hasBarberHalf() {
        return (getCheckboxFlagsAsLong() & BARBER_HALF) != 0;
    }

    public boolean hasWalkerHalf() {
        return (getCheckboxFlagsAsLong() & WALKER_HALF) != 0;
    }

    public boolean hasFranklinHalf() {
        return (getCheckboxFlagsAsLong() & FRANKLIN_HALF) != 0;
    }

    public boolean hasKennedyHalf() {
        return (getCheckboxFlagsAsLong() & KENNEDY_HALF) != 0;
    }

    public boolean hasShieldNickels() {
        return (getCheckboxFlagsAsLong() & SHIELD_NICKELS) != 0;
    }

    public boolean hasLibertyNickels() {
        return (getCheckboxFlagsAsLong() & LIBERTY_NICKELS) != 0;
    }

    public boolean hasBuffaloNickels() {
        return (getCheckboxFlagsAsLong() & BUFFALO_NICKELS) != 0;
    }

    public boolean hasJeffersonNickels() {
        return (getCheckboxFlagsAsLong() & JEFFERSON_NICKELS) != 0;
    }

    public boolean hasBarberDimes() {
        return (getCheckboxFlagsAsLong() & BARBER_DIMES) != 0;
    }

    public boolean hasMercuryDimes() {
        return (getCheckboxFlagsAsLong() & MERCURY_DIMES) != 0;
    }

    public boolean hasRooseveltDimes() {
        return (getCheckboxFlagsAsLong() & ROOSEVELT_DIMES) != 0;
    }

    public boolean hasMorganDollars() {
        return (getCheckboxFlagsAsLong() & MORGAN_DOLLARS) != 0;
    }

    public boolean hasPeaceDollars() {
        return (getCheckboxFlagsAsLong() & PEACE_DOLLARS) != 0;
    }

    public boolean hasIkeDollars() {
        return (getCheckboxFlagsAsLong() & IKE_DOLLARS) != 0;
    }

    public boolean hasEagleDollars() {
        return (getCheckboxFlagsAsLong() & EAGLE_DOLLARS) != 0;
    }

    public boolean hasSBADollars() {
        return (getCheckboxFlagsAsLong() & SBA_DOLLARS) != 0;
    }

    public boolean hasSACDollars() {
        return (getCheckboxFlagsAsLong() & SAC_DOLLARS) != 0;
    }

    public boolean hasPresDollars() {
        return (getCheckboxFlagsAsLong() & PRES_DOLLARS) != 0;
    }

    public boolean hasTradeDollars() {
        return (getCheckboxFlagsAsLong() & TRADE_DOLLARS) != 0;
    }

    public void setEndYear(int endYear) {
        mEndYear = endYear;
    }

    /* setMintMarkFlags() used in unit tests
     */
    public void setMintMarkFlags(String flags) {
        mMintMarkFlags = flags;
    }

    /* setCheckboxFlags() used in unit tests
     */
    public void setCheckboxFlags(String flags) {
        mCheckboxFlags = flags;
    }

    /**
     * Populates the creation parameters from the coin data itself. This is used to figure out
     * the creation properties of existing collections
     *
     * @param coinList list of coins in the database
     */
    public void setCreationParametersFromCoinData(ArrayList<CoinSlot> coinList) {
        String coinType = this.getType();
        int startYear = this.getCollectionObj().getStartYear();
        int endYear = this.getCollectionObj().getStopYear();
        boolean showBurnished = false;
        boolean showTerritories = false;
        boolean hasBlankMint = false;
        boolean showP = false;
        boolean showD = false;
        boolean showS = false;
        boolean showO = false;
        boolean showCC = false;
        boolean hideMintMarksCheckValid = (coinType.equals(WalkingLibertyHalfDollars.COLLECTION_TYPE));
        boolean hideMintMarks = false;

        for (int i = 0; i < coinList.size(); i++) {
            String mintMark = coinList.get(i).getMint();
            String coinId = coinList.get(i).getIdentifier();
            if (mintMark.isEmpty()) {
                hasBlankMint = true;
            }
            if (mintMark.equals("P") || mintMark.equals(" P") || mintMark.contains(" P ")
                    || isPMintSpecialCase(coinType, coinId, mintMark)) {
                showP = true;
            }
            if (mintMark.equals("D") || mintMark.equals(" D") || mintMark.contains(" D ")) {
                showD = true;
            }
            if (mintMark.equals("S") || mintMark.equals(" S") || mintMark.contains(" S ")) {
                showS = true;
            }
            if (mintMark.equals("O") || mintMark.equals(" O") || mintMark.contains(" O ")) {
                showO = true;
            }
            if (mintMark.equals("CC") || mintMark.equals(" CC") || mintMark.contains(" CC ")) {
                showCC = true;
            }
            if (coinId.contains("Burnished")) {
                showBurnished = true;
            }
            if (coinType.equals(StateQuarters.COLLECTION_TYPE) &&
                    coinId.equals(StateQuarters.DC_AND_TERR_COIN_IDENTIFIERS[0][0])) {
                showTerritories = true;
            }
            if (isHideMintMarkSpecialCase(coinType, coinId, mintMark)) {
                hideMintMarks = true;
            }
        }
        // Show mint marks if any mint is explicitly displayed
        boolean showMintMarks = showP || showD || showS || showO || showCC;

        // In certain cases we need to differentiate between "P" and no mints by seeing if a
        // coin only in the hide mint mark list exists in the collection
        if (hideMintMarksCheckValid && !hideMintMarks) {
            showMintMarks = true;
        }

        // Include P mint mark if the collection has blanks or does not show mint marks
        showP = showP || hasBlankMint || !showMintMarks;

        // Get the start and end date
        boolean useCustomDateRange = false;
        if (doesCollectionTypeUseDates(coinType)) {
            if (!coinList.isEmpty()) {
                try {
                    // Start Year
                    int newStartYear = parseDateString(coinList.get(0).getIdentifier());
                    useCustomDateRange = (newStartYear != startYear);
                    startYear = newStartYear;
                    // End Year
                    int newEndYear = parseDateString(coinList.get(coinList.size() - 1).getIdentifier());
                    useCustomDateRange |= (newEndYear != endYear);
                    endYear = newEndYear;
                } catch (NumberFormatException ignored) {
                    // If a parsing error occurs for some reason, just use the start/end dates
                }
            }
        }

        // Combine flags for mint marks
        long mintMarkFlags = showMintMarks ? CollectionListInfo.SHOW_MINT_MARKS : 0;
        mintMarkFlags |= showP ? CollectionListInfo.MINT_P : 0;
        mintMarkFlags |= showD ? CollectionListInfo.MINT_D : 0;
        mintMarkFlags |= showS ? CollectionListInfo.MINT_S : 0;
        mintMarkFlags |= showO ? CollectionListInfo.MINT_O : 0;
        mintMarkFlags |= showCC ? CollectionListInfo.MINT_CC : 0;

        // Combine flags for checkboxes
        long checkboxFlags = useCustomDateRange ? CollectionListInfo.CUSTOM_DATES : 0;
        checkboxFlags |= showBurnished ? CollectionListInfo.BURNISHED : 0;
        checkboxFlags |= showTerritories ? CollectionListInfo.TERRITORIES : 0;

        // Populate the collection creation details
        this.setCreationParameters(
                startYear,
                endYear,
                Long.toString(mintMarkFlags),
                Long.toString(checkboxFlags)
        );
    }

    /**
     * Parse date string to extract the start/stop year
     *
     * @param dateStr the data string
     * @return parsed start/end date
     * @throws NumberFormatException if the string does not contain a parsable integer.
     */
    private int parseDateString(String dateStr) throws NumberFormatException {
        if (dateStr.equals("1776-1976")) {
            return 1976;
        } else {
            return Integer.parseInt(dateStr.substring(0, 4));
        }
    }

    /**
     * Detect special cases indicating that the user selected "P" mint marks
     *
     * @param coinType collection type name
     * @param coinId   coin date or name
     * @param mintMark coin mint mark
     * @return true if the coin is a special case P mint mark coin, false otherwise
     */
    private boolean isPMintSpecialCase(String coinType, String coinId, String mintMark) {
        return (coinType.equals(BuffaloNickels.COLLECTION_TYPE) && coinId.equals("1913") && mintMark.equals(" Type 1"))
                || (coinType.equals(IndianHeadCents.COLLECTION_TYPE) && coinId.equals("1864") && mintMark.equals(" Copper"))
                || (coinType.equals(MorganDollars.COLLECTION_TYPE) && coinId.equals("1878 8 Feathers"));
    }

    /**
     * Detect special cases indicating that the user did not check 'show mint marks'
     *
     * @param coinType collection type name
     * @param coinId   coin date or name
     * @param mintMark coin mint mark
     * @return true if the coin is a special case of no mint marks, false otherwise
     */
    private boolean isHideMintMarkSpecialCase(String coinType, String coinId, String mintMark) {
        if (coinType.equals(WalkingLibertyHalfDollars.COLLECTION_TYPE) && mintMark.isEmpty()) {
            int dateInt = Integer.parseInt(coinId.substring(0, 4));
            return (dateInt >= 1923 && dateInt <= 1933);
        }
        return false;
    }

    /**
     * Sets the properties associated with database creation
     *
     * @param startYear     int indicating the starting year
     * @param endYear       int indicating the ending year
     * @param mintMarkFlags int flags indicating which mint marks were used
     * @param checkboxFlags int flags indicating which checkboxes were check
     */
    void setCreationParameters(int startYear, int endYear, String mintMarkFlags, String checkboxFlags) {
        mStartYear = startYear;
        mEndYear = endYear;
        mMintMarkFlags = mintMarkFlags;
        mCheckboxFlags = checkboxFlags;
    }

    /**
     * Get the collection parameters to export to CSV
     *
     * @param dbAdapter database adapter
     * @return string array with collection data
     */
    public String[] getCsvExportProperties(DatabaseAdapter dbAdapter) {

        // NOTE For display, don't use item.getDisplayType bc I don't
        // think we populate that value except when importing...
        // TODO Update the code so that this value is used instead
        // of the separate fetchTableDisplay calls
        String displayType = String.valueOf(dbAdapter.fetchTableDisplay(mCollectionName));

        return new String[]{
                mCollectionName,
                this.getType(),
                String.valueOf(mTotalCoinsCollected),
                String.valueOf(mTotalCoinsInCollection),
                displayType, // See note above
                String.valueOf(mStartYear),
                String.valueOf(mEndYear),
                "0", // Legacy mint mark flags
                "0", // Legacy checkbox flags
                mMintMarkFlags,
                mCheckboxFlags};
    }

    /**
     * Get the headers for the coin CSV file
     *
     * @return string array with column names
     */
    public static String[] getCsvExportHeader() {
        return new String[]{
                COL_NAME,
                COL_COIN_TYPE,
                JSON_KEY_COLLECTED,
                COL_TOTAL,
                COL_DISPLAY,
                COL_START_YEAR,
                COL_END_YEAR,
                COL_SHOW_MINT_MARKS,
                COL_SHOW_CHECKBOXES};
    }

    /**
     * Returns true if the collection type (string) uses date range
     *
     * @param collectionTypeStr collection type
     * @return true if uses dates, false otherwise
     */
    public static boolean doesCollectionTypeUseDates(String collectionTypeStr) {
        return HAS_DATE_RANGE.contains(collectionTypeStr);
    }

    /**
     * Checks if new mint marks for an update would result in fewer coins in the collection
     *
     * @param mintMarkFlags new mint mark flags
     * @param checkboxFlags new checkbox flags
     * @return true if the number of coins may be less, false otherwise
     */
    public boolean checkIfNewFlagsRemoveCoins(long mintMarkFlags, long checkboxFlags) {
        // Return true if:
        // - A mint mark that is set is unset
        // - a checkbox option that is set is unset (ignores custom dates)
        return ((getMintMarkFlagsAsLong() & ~mintMarkFlags)
                | (getCheckboxFlagsAsLong() & ~checkboxFlags & ~CUSTOM_DATES)) != 0;
    }

    /**
     * Write out the JSON representation (for exporting)
     *
     * @param writer    JsonWriter to write to
     * @param dbAdapter database adapter
     * @param coinList  coins associated with the collection
     * @throws IOException if an error occurred
     */
    public void writeToJson(JsonWriter writer, DatabaseAdapter dbAdapter, ArrayList<CoinSlot> coinList) throws IOException {

        // NOTE For display, don't use item.getDisplayType bc I don't
        // think we populate that value except when importing...
        // TODO Update the code so that this value is used instead
        // of the separate fetchTableDisplay calls
        int displayType = dbAdapter.fetchTableDisplay(mCollectionName);

        writer.beginObject();
        writer.name(COL_NAME).value(mCollectionName);
        writer.name(COL_COIN_TYPE).value(getType());
        writer.name(JSON_KEY_COLLECTED).value(mTotalCoinsCollected);
        writer.name(COL_TOTAL).value(mTotalCoinsInCollection);
        writer.name(COL_DISPLAY).value(displayType);
        writer.name(COL_START_YEAR).value(mStartYear);
        writer.name(COL_END_YEAR).value(mEndYear);
        writer.name(COL_SHOW_MINT_MARKS).value(mMintMarkFlags);
        writer.name(COL_SHOW_CHECKBOXES).value(mCheckboxFlags);
        writer.name(JSON_COIN_LIST);
        writer.beginArray();
        for (CoinSlot coinSlot : coinList) {
            coinSlot.writeToJson(writer);
        }
        writer.endArray();
        writer.endObject();
    }

    /**
     * Create a collection list info from imported JSON file
     *
     * @param reader   JsonReader to read from
     * @param coinList List of coins with collection populated
     * @throws IOException if an error occurred
     */
    public CollectionListInfo(JsonReader reader, ArrayList<CoinSlot> coinList) throws IOException {

        String collectionName = "";
        int totalCoinsCollected = 0;
        int totalCoinsInCollection = 0;
        int displayType = SIMPLE_DISPLAY;
        int startYear = 0;
        int endYear = 0;
        String mintMarkFlags = "";
        String checkboxFlags = "";
        int collectionTypeIndex = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case COL_NAME:
                    // Strip out all bad characters.  They shouldn't be there anyway ;)
                    collectionName = reader.nextString().replace('[', ' ').replace(']', ' ');
                    break;
                case JSON_KEY_COLLECTED:
                    totalCoinsCollected = reader.nextInt();
                    break;
                case COL_TOTAL:
                    totalCoinsInCollection = reader.nextInt();
                    break;
                case COL_DISPLAY:
                    displayType = reader.nextInt();
                    break;
                case COL_START_YEAR:
                    startYear = reader.nextInt();
                    break;
                case COL_END_YEAR:
                    endYear = reader.nextInt();
                    break;
                case COL_SHOW_MINT_MARKS_LEGACY:
                    mintMarkFlags = Integer.toString(reader.nextInt());
                    break;
                case COL_SHOW_MINT_MARKS:
                    mintMarkFlags = reader.nextString();
                    break;
                case COL_SHOW_CHECKBOXES_LEGACY:
                    checkboxFlags = Integer.toString(reader.nextInt());
                    break;
                case COL_SHOW_CHECKBOXES:
                    checkboxFlags = reader.nextString();
                    break;
                case COL_COIN_TYPE:
                    // If the coin type isn't recognized, an error occurred so just choose a safe value
                    collectionTypeIndex = MainApplication.getIndexFromCollectionNameStr(reader.nextString());
                    collectionTypeIndex = (collectionTypeIndex != -1) ? collectionTypeIndex : 0;
                    break;
                case JSON_COIN_LIST:
                    // Since the coin list is stored inside of the same JSON object, we'll populate the
                    // list of coinSlot objects here as well and return those to the caller
                    reader.beginArray();
                    int coinIndex = 0;
                    while (reader.hasNext()) {
                        coinList.add(new CoinSlot(reader, coinIndex++));
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        mCollectionName = collectionName;
        mTotalCoinsCollected = totalCoinsCollected;
        mTotalCoinsInCollection = totalCoinsInCollection;
        mDisplayType = displayType;
        mStartYear = startYear;
        mEndYear = endYear;
        mMintMarkFlags = mintMarkFlags;
        mCheckboxFlags = checkboxFlags;
        mCollectionTypeIndex = collectionTypeIndex;
        mCollectionInfo = MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
    }

    /**
     * Create a collection list info from imported string array
     *
     * @param in input String[]
     */
    public CollectionListInfo(String[] in) {

        // Strip out all bad characters.  They shouldn't be there anyway ;)
        mCollectionName = in[0].replace('[', ' ').replace(']', ' ');
        mTotalCoinsCollected = Integer.parseInt(in[2]);
        mTotalCoinsInCollection = Integer.parseInt(in[3]);
        mDisplayType = (in.length > 4) ? Integer.parseInt(in[4]) : 0;

        // If the properties below aren't present, they will be determined
        // using setCreationParametersFromCoinData()
        mStartYear = (in.length > 5) ? Integer.parseInt(in[5]) : 0;
        mEndYear = (in.length > 6) ? Integer.parseInt(in[6]) : 0;
        int mintMarkFlagsLegacy = (in.length > 7) ? Integer.parseInt(in[7]) : 0;
        int checkboxFlagsLegacy = (in.length > 8) ? Integer.parseInt(in[8]) : 0;
        mMintMarkFlags = (in.length > 9) ? in[9] : Integer.toString(mintMarkFlagsLegacy);
        mCheckboxFlags = (in.length > 10) ? in[10] : Integer.toString(checkboxFlagsLegacy);

        // If the coin type isn't recognized, an error occurred so just choose a safe value
        int collectionTypeIndex = MainApplication.getIndexFromCollectionNameStr(in[1]);
        mCollectionTypeIndex = (collectionTypeIndex != -1) ? collectionTypeIndex : 0;
        mCollectionInfo = MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
    }

    /**
     * We make this object Parcelable so that the list can be passed between Activities
     *
     * @param in Parcel object
     */
    protected CollectionListInfo(Parcel in) {
        mCollectionName = in.readString();
        mTotalCoinsInCollection = in.readInt();
        mTotalCoinsCollected = in.readInt();
        mCollectionTypeIndex = in.readInt();
        mDisplayType = in.readInt();
        mStartYear = in.readInt();
        mEndYear = in.readInt();
        mMintMarkFlags = in.readString();
        mCheckboxFlags = in.readString();
        mCollectionInfo = MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCollectionName);
        dest.writeInt(mTotalCoinsInCollection);
        dest.writeInt(mTotalCoinsCollected);
        dest.writeInt(mCollectionTypeIndex);
        dest.writeInt(mDisplayType);
        dest.writeInt(mStartYear);
        dest.writeInt(mEndYear);
        dest.writeString(mMintMarkFlags);
        dest.writeString(mCheckboxFlags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollectionListInfo> CREATOR = new Creator<CollectionListInfo>() {
        @Override
        public CollectionListInfo createFromParcel(Parcel in) {
            return new CollectionListInfo(in);
        }

        @Override
        public CollectionListInfo[] newArray(int size) {
            return new CollectionListInfo[size];
        }
    };
}
