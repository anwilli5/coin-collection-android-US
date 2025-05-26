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
}
