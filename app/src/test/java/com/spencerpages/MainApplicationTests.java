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

import static com.coincollection.CollectionListInfo.BARBER_QUARTERS;
import static com.coincollection.CollectionListInfo.BURNISHED;
import static com.coincollection.CollectionListInfo.BUST_COINS;
import static com.coincollection.CollectionListInfo.CAPPED_BUST_COINS;
import static com.coincollection.CollectionListInfo.CLAD_COINS;
import static com.coincollection.CollectionListInfo.CLASSIC_QUARTERS;
import static com.coincollection.CollectionListInfo.CORONET_COINS;
import static com.coincollection.CollectionListInfo.CUSTOM_DATES;
import static com.coincollection.CollectionListInfo.DRAPED_BUST_COINS;
import static com.coincollection.CollectionListInfo.EAGLE_CENTS;
import static com.coincollection.CollectionListInfo.INDIAN_CENTS;
import static com.coincollection.CollectionListInfo.MEMORIAL_CENTS;
import static com.coincollection.CollectionListInfo.MINT_CC;
import static com.coincollection.CollectionListInfo.MINT_D;
import static com.coincollection.CollectionListInfo.MINT_FRANKLIN_PROOF;
import static com.coincollection.CollectionListInfo.MINT_MEM_PROOF;
import static com.coincollection.CollectionListInfo.MINT_O;
import static com.coincollection.CollectionListInfo.MINT_P;
import static com.coincollection.CollectionListInfo.MINT_REV_PROOF;
import static com.coincollection.CollectionListInfo.MINT_S;
import static com.coincollection.CollectionListInfo.MINT_SATIN;
import static com.coincollection.CollectionListInfo.MINT_SETS;
import static com.coincollection.CollectionListInfo.MINT_SILVER_PROOF;
import static com.coincollection.CollectionListInfo.MINT_S_PROOF;
import static com.coincollection.CollectionListInfo.MINT_W;
import static com.coincollection.CollectionListInfo.NICKEL_COINS;
import static com.coincollection.CollectionListInfo.OLD_COINS;
import static com.coincollection.CollectionListInfo.PARKS_QUARTERS;
import static com.coincollection.CollectionListInfo.PARKS_QUARTERS_PROOF;
import static com.coincollection.CollectionListInfo.PRES_DOLLARS;
import static com.coincollection.CollectionListInfo.PROOF_SETS;
import static com.coincollection.CollectionListInfo.SAC_DOLLARS;
import static com.coincollection.CollectionListInfo.SBA_DOLLARS;
import static com.coincollection.CollectionListInfo.SEATED_COINS;
import static com.coincollection.CollectionListInfo.SHIELD_CENTS;
import static com.coincollection.CollectionListInfo.BARBER_HALF;
import static com.coincollection.CollectionListInfo.WALKER_HALF;
import static com.coincollection.CollectionListInfo.FRANKLIN_HALF;
import static com.coincollection.CollectionListInfo.KENNEDY_HALF;
import static com.coincollection.CollectionListInfo.SHIELD_NICKELS;
import static com.coincollection.CollectionListInfo.LIBERTY_NICKELS;
import static com.coincollection.CollectionListInfo.BUFFALO_NICKELS;
import static com.coincollection.CollectionListInfo.JEFFERSON_NICKELS;
import static com.coincollection.CollectionListInfo.BARBER_DIMES;
import static com.coincollection.CollectionListInfo.MERCURY_DIMES;
import static com.coincollection.CollectionListInfo.ROOSEVELT_DIMES;
import static com.coincollection.CollectionListInfo.MORGAN_DOLLARS;
import static com.coincollection.CollectionListInfo.PEACE_DOLLARS;
import static com.coincollection.CollectionListInfo.IKE_DOLLARS;
import static com.coincollection.CollectionListInfo.EAGLE_DOLLARS;
import static com.coincollection.CollectionListInfo.SHOW_MINT_MARKS;
import static com.coincollection.CollectionListInfo.SILVER_COINS;
import static com.coincollection.CollectionListInfo.SILVER_PROOF_SETS;
import static com.coincollection.CollectionListInfo.STANDING_QUARTERS;
import static com.coincollection.CollectionListInfo.STATES_QUARTERS;
import static com.coincollection.CollectionListInfo.STATES_QUARTERS_PROOF;
import static com.coincollection.CollectionListInfo.TERRITORIES;
import static com.coincollection.CollectionListInfo.TERRITORIES_QUARTERS_PROOF;
import static com.coincollection.CollectionListInfo.TRADE_DOLLARS;
import static com.coincollection.CollectionListInfo.WHEAT_CENTS;
import static com.coincollection.CollectionListInfo.WOMEN_QUARTERS;
import static com.coincollection.CollectionListInfo.WOMEN_QUARTERS_PROOF;

import static com.spencerpages.MainApplication.getIndexFromCollectionClass;
import static org.junit.Assert.assertEquals;

import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.AmericanWomenQuarters;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BasicDimes;
import com.spencerpages.collections.BasicHalfDollars;
import com.spencerpages.collections.BasicInnovationDollars;
import com.spencerpages.collections.BasicQuarters;
import com.spencerpages.collections.BuffaloNickels;
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
import com.spencerpages.collections.MercuryDimes;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NationalParkQuarters;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.PresidentialDollars;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.SilverDimes;
import com.spencerpages.collections.SilverHalfDollars;
import com.spencerpages.collections.SilverQuarters;
import com.spencerpages.collections.SmallCents;
import com.spencerpages.collections.SmallDollars;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.Trimes;
import com.spencerpages.collections.TwentyCents;
import com.spencerpages.collections.TwoCents;
import com.spencerpages.collections.WalkingLibertyHalfDollars;
import com.spencerpages.collections.WashingtonQuarters;
import com.spencerpages.collections.WestPoint;

import org.junit.Test;

public class MainApplicationTests extends BaseTestCase {
    /**
     * Ensure the order of collections does not change, as this
     * will impact import of exported collections
     */
    @Test
    public void testCollectionTypesOrder() {
        assertEquals(0, getIndexFromCollectionClass(LincolnCents.class));
        assertEquals(1, getIndexFromCollectionClass(JeffersonNickels.class));
        assertEquals(2, getIndexFromCollectionClass(BasicDimes.class));
        assertEquals(3, getIndexFromCollectionClass(BasicQuarters.class));
        assertEquals(4, getIndexFromCollectionClass(StateQuarters.class));
        assertEquals(5, getIndexFromCollectionClass(NationalParkQuarters.class));
        assertEquals(6, getIndexFromCollectionClass(BasicHalfDollars.class));
        assertEquals(7, getIndexFromCollectionClass(EisenhowerDollar.class));
        assertEquals(8, getIndexFromCollectionClass(SusanBAnthonyDollars.class));
        assertEquals(9, getIndexFromCollectionClass(NativeAmericanDollars.class));
        assertEquals(10, getIndexFromCollectionClass(PresidentialDollars.class));
        assertEquals(11, getIndexFromCollectionClass(IndianHeadCents.class));
        assertEquals(12, getIndexFromCollectionClass(LibertyHeadNickels.class));
        assertEquals(13, getIndexFromCollectionClass(BuffaloNickels.class));
        assertEquals(14, getIndexFromCollectionClass(BarberDimes.class));
        assertEquals(15, getIndexFromCollectionClass(MercuryDimes.class));
        assertEquals(16, getIndexFromCollectionClass(BarberQuarters.class));
        assertEquals(17, getIndexFromCollectionClass(StandingLibertyQuarters.class));
        assertEquals(18, getIndexFromCollectionClass(BarberHalfDollars.class));
        assertEquals(19, getIndexFromCollectionClass(WalkingLibertyHalfDollars.class));
        assertEquals(20, getIndexFromCollectionClass(FranklinHalfDollars.class));
        assertEquals(21, getIndexFromCollectionClass(MorganDollars.class));
        assertEquals(22, getIndexFromCollectionClass(PeaceDollars.class));
        assertEquals(23, getIndexFromCollectionClass(AmericanEagleSilverDollars.class));
        assertEquals(24, getIndexFromCollectionClass(FirstSpouseGoldCoins.class));
        assertEquals(25, getIndexFromCollectionClass(BasicInnovationDollars.class));
        assertEquals(26, getIndexFromCollectionClass(AmericanWomenQuarters.class));
        assertEquals(27, getIndexFromCollectionClass(SmallCents.class));
        assertEquals(28, getIndexFromCollectionClass(LargeCents.class));
        assertEquals(29, getIndexFromCollectionClass(AllNickels.class));
        assertEquals(30, getIndexFromCollectionClass(HalfDimes.class));
        assertEquals(31, getIndexFromCollectionClass(SilverDimes.class));
        assertEquals(32, getIndexFromCollectionClass(EarlyDimes.class));
        assertEquals(33, getIndexFromCollectionClass(CladQuarters.class));
        assertEquals(34, getIndexFromCollectionClass(SilverQuarters.class));
        assertEquals(35, getIndexFromCollectionClass(EarlyQuarters.class));
        assertEquals(36, getIndexFromCollectionClass(SmallDollars.class));
        assertEquals(37, getIndexFromCollectionClass(SilverHalfDollars.class));
        assertEquals(38, getIndexFromCollectionClass(Trimes.class));
        assertEquals(39, getIndexFromCollectionClass(TwentyCents.class));
        assertEquals(40, getIndexFromCollectionClass(TwoCents.class));
        assertEquals(41, getIndexFromCollectionClass(WestPoint.class));
        assertEquals(42, getIndexFromCollectionClass(EarlyDollars.class));
        assertEquals(43, getIndexFromCollectionClass(EarlyHalfDollars.class));
        assertEquals(44, getIndexFromCollectionClass(Cartwheels.class));
        assertEquals(45, getIndexFromCollectionClass(HalfCents.class));
        assertEquals(46, getIndexFromCollectionClass(CoinSets.class));
        assertEquals(47, getIndexFromCollectionClass(KennedyHalfDollars.class));
        assertEquals(48, getIndexFromCollectionClass(RooseveltDimes.class));
        assertEquals(49, getIndexFromCollectionClass(WashingtonQuarters.class));
        assertEquals(50, getIndexFromCollectionClass(AmericanInnovationDollars.class));
    }

    /**
     * Ensure the fixed mint mark IDs are correct
     * These are used in the database and should not change
     */
    @Test
    public void testFixedMintMarkIds() {
        assertEquals(SHOW_MINT_MARKS, 0x1L);
        assertEquals(MINT_P, 0x2L);
        assertEquals(MINT_D, 0x4L);
        assertEquals(MINT_S, 0x8L);
        assertEquals(MINT_O, 0x10L);
        assertEquals(MINT_CC, 0x20L);
        assertEquals(MINT_W, 0x40L);
        assertEquals(MINT_S_PROOF, 0x80L);
        assertEquals(MINT_SILVER_PROOF, 0x100L);
        assertEquals(MINT_REV_PROOF, 0x200L);
        assertEquals(MINT_MEM_PROOF, 0x400L);
        assertEquals(MINT_SATIN, 0x800L);
        assertEquals(MINT_FRANKLIN_PROOF, 0x1000L);
    }

    /**
     * Ensure the fixed check box IDs are correct
     * These are used in the database and should not change
     */
    @Test
    public void testFixedCheckBoxIds() {
        assertEquals(CUSTOM_DATES, 0x1L);
        assertEquals(BURNISHED, 0x2L);
        assertEquals(TERRITORIES, 0x4L);
        assertEquals(SILVER_COINS, 0x8L);
        assertEquals(NICKEL_COINS, 0x10L);
        assertEquals(OLD_COINS, 0x20L);
        assertEquals(BUST_COINS, 0x40L);
        assertEquals(DRAPED_BUST_COINS, 0x80L);
        assertEquals(CAPPED_BUST_COINS, 0x100L);
        assertEquals(SEATED_COINS, 0x200L);
        assertEquals(CORONET_COINS, 0x400L);
        assertEquals(BARBER_QUARTERS, 0x800L);
        assertEquals(STANDING_QUARTERS, 0x1000L);
        assertEquals(CLASSIC_QUARTERS, 0x2000L);
        assertEquals(STATES_QUARTERS, 0x4000L);
        assertEquals(PARKS_QUARTERS, 0x8000L);
        assertEquals(WOMEN_QUARTERS, 0x10000L);
        assertEquals(EAGLE_CENTS, 0x20000L);
        assertEquals(INDIAN_CENTS, 0x40000L);
        assertEquals(WHEAT_CENTS, 0x80000L);
        assertEquals(MEMORIAL_CENTS, 0x100000L);
        assertEquals(SHIELD_CENTS, 0x200000L);
        assertEquals(BARBER_HALF, 0x400000L);
        assertEquals(WALKER_HALF, 0x800000L);
        assertEquals(FRANKLIN_HALF, 0x1000000L);
        assertEquals(KENNEDY_HALF, 0x2000000L);
        assertEquals(SHIELD_NICKELS, 0x4000000L);
        assertEquals(LIBERTY_NICKELS, 0x8000000L);
        assertEquals(BUFFALO_NICKELS, 0x10000000L);
        assertEquals(JEFFERSON_NICKELS, 0x20000000L);
        assertEquals(BARBER_DIMES, 0x40000000L);
        assertEquals(MERCURY_DIMES, 0x80000000L);
        assertEquals(ROOSEVELT_DIMES, 0x100000000L);
        assertEquals(MORGAN_DOLLARS, 0x200000000L);
        assertEquals(PEACE_DOLLARS, 0x400000000L);
        assertEquals(IKE_DOLLARS, 0x800000000L);
        assertEquals(EAGLE_DOLLARS, 0x1000000000L);
        assertEquals(SBA_DOLLARS, 0x2000000000L);
        assertEquals(SAC_DOLLARS, 0x4000000000L);
        assertEquals(PRES_DOLLARS, 0x8000000000L);
        assertEquals(TRADE_DOLLARS, 0x10000000000L);
        assertEquals(STATES_QUARTERS_PROOF, 0x20000000000L);
        assertEquals(PARKS_QUARTERS_PROOF, 0x40000000000L);
        assertEquals(WOMEN_QUARTERS_PROOF, 0x80000000000L);
        assertEquals(TERRITORIES_QUARTERS_PROOF, 0x100000000000L);
        assertEquals(CLAD_COINS, 0x200000000000L);
        assertEquals(MINT_SETS, 0x400000000000L);
        assertEquals(PROOF_SETS, 0x800000000000L);
        assertEquals(SILVER_PROOF_SETS, 0x1000000000000L);
    }
}
