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

import static junit.framework.TestCase.assertEquals;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.AmericanWomenQuarters;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BasicDimes;
import com.spencerpages.collections.BasicHalfDollars;
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
import com.spencerpages.collections.WashingtonSilver;
import com.spencerpages.collections.WestPoint;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class CollectionCreationTests extends BaseTestCase {

    @Override
    protected boolean enableVmPolicyChecking() {
        // This test does not support VM policy checking
        return false;
    }

    /**
     * For AllNickels
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_AllNickelsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        AllNickels coinClass = new AllNickels();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Satin, S Proof, W, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, false, true, true, true, true, true, 161},
                {true, false, true, false, false, false, false, true, true, true, true, true, 125},
                {true, false, false, true, false, false, false, true, true, true, true, true, 60},
                {true, false, false, false, true, false, false, true, true, true, true, true, 78},
                {true, false, false, false, false, true, false, true, true, true, true, true, 32},
                {true, false, false, false, false, false, true, true, true, true, true, true, 20},
                {true, true, true, true, true, true, true, true, false, false, false, false, 3},
                {true, true, true, true, true, true, true, false, true, false, false, false, 18},
                {true, true, true, true, true, true, true, false, false, true, false, false, 33},
                {true, true, true, true, true, true, true, false, false, false, true, false, 64},
                {true, true, true, true, true, true, true, false, false, false, false, true, 271},
                {true, true, true, true, true, true, true, true, true, true, true, true, 386},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[10]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[11]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[12], coinList.size());
        }
    }

    /**
     * For AmericanEagleSilverDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_AmericanEagleSilverDollarsCreationCounts() {

        HashMap<String, Object> parameters = new ParcelableHashMap();
        AmericanEagleSilverDollars coinClass = new AmericanEagleSilverDollars();
        coinClass.getCreationParameters(parameters);

        // Show Burnished, Expected Result
        Object[][] tests = {
                {false, 39},
                {true, 39 + 4},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[0]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[1], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For AmericanInnovationDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_AmericanInnovationDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        AmericanInnovationDollars coinClass = new AmericanInnovationDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Expected Result
        Object[][] tests = {
                {false, true, true, 25},
                {false, false, true, 25},
                {true, false, false, 0},
                {true, true, false, 25},
                {true, false, true, 25},
                {true, true, true, 25 + 25},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For AmericanWomenQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_AmericanWomenQuartersCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        AmericanWomenQuarters coinClass = new AmericanWomenQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 15},
                {false, false, false, false, 15},
                {true, true, false, false, 15},
                {true, false, true, false, 15},
                {true, false, false, true, 15},
                {true, true, true, true, 15 + 15 + 15},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BarberDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BarberDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        BarberDimes coinClass = new BarberDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, O, Expected Result
        Object[][] tests = {
                {false, true, false, true, false, 25},
                {false, false, false, false, false, 25},
                {true, true, false, false, false, 25},
                {true, false, true, false, false, 8},
                {true, false, false, true, false, 24},
                {true, false, false, false, true, 17},
                {true, true, true, true, true, 25 + 8 + 24 + 17},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[5], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BarberHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BarberHalfDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        BarberHalfDollars coinClass = new BarberHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, O, Expected Result
        Object[][] tests = {
                {false, true, false, true, false, 24},
                {false, false, false, false, false, 24},
                {true, true, false, false, false, 24},
                {true, false, true, false, false, 7},
                {true, false, false, true, false, 24},
                {true, false, false, false, true, 18},
                {true, true, true, true, true, 24 + 7 + 24 + 18},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[5], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BarberQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BarberQuartersCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        BarberQuarters coinClass = new BarberQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, O, Expected Result
        Object[][] tests = {
                {false, true, false, true, false, 25},
                {false, false, false, false, false, 25},
                {true, true, false, false, false, 25},
                {true, false, true, false, false, 10},
                {true, false, false, true, false, 21},
                {true, false, false, false, true, 18},
                {true, true, true, true, true, 25 + 10 + 21 + 18},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[5], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BasicDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BasicDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        BasicDimes coinClass = new BasicDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 79},
                {false, false, false, false, 79},
                {true, true, false, false, 79},
                {true, false, true, false, 76},
                {true, false, false, true, 10},
                {true, true, true, true, 79 + 76 + 10},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BasicHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BasicHalfDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        BasicHalfDollars coinClass = new BasicHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Expected Result
        Object[][] tests = {
                {false, true, true, 60},
                {false, false, true, 60},
                {true, false, false, 0},
                {true, true, false, 57},
                {true, false, true, 57},
                {true, true, true, 57 + 57},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }

        // Test special case of start year 1975/1976
        int[] years = {1975, 1976};
        for (int year : years) {
            parameters.put(CoinPageCreator.OPT_START_YEAR, year);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(98, coinList.size());
            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BasicQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BasicQuartersCreationCounts() {

        HashMap<String, Object> parameters = new ParcelableHashMap();
        BasicQuarters coinClass = new BasicQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 66},
                {false, false, false, false, 66},
                {true, true, false, false, 66},
                {true, false, true, false, 62},
                {true, false, false, true, 20},
                {true, true, true, true, 66 + 62 + 20},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }

        // Test special case of start year 1975/1976
        int[] years = {1975, 1976};
        for (int year : years) {
            parameters.put(CoinPageCreator.OPT_START_YEAR, year);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(48, coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For BuffaloNickels
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_BuffaloNickelsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        BuffaloNickels coinClass = new BuffaloNickels();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 23},
                {false, false, false, false, 23},
                {true, true, false, false, 22},
                {true, false, true, false, 20},
                {true, false, false, true, 22},
                {true, true, true, true, 22 + 20 + 22},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For Cartwheels
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_CartwheelsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        Cartwheels coinClass = new Cartwheels();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, true, true, true, true, true, 93},
                {true, false, true, false, false, false, true, true, true, true, true, 18},
                {true, false, false, true, false, false, true, true, true, true, true, 57},
                {true, false, false, false, true, false, true, true, true, true, true, 30},
                {true, false, false, false, false, true, true, true, true, true, true, 17},
                {true, true, true, true, true, true, true, false, false, false, false, 7},
                {true, true, true, true, true, true, false, true, false, false, false, 96},
                {true, true, true, true, true, true, false, false, true, false, false, 24},
                {true, true, true, true, true, true, false, false, false, true, false, 32},
                {true, true, true, true, true, true, false, false, false, false, true, 43},
                {true, true, true, true, true, true, true, true, true, true, true, 199},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[10]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[11], coinList.size());
        }
    }

    /**
     * For CladQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_CladQuartersCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        CladQuarters coinClass = new CladQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, false, true, true, true, true, 164},
                {true, false, true, false, false, false, false, true, true, true, true, 158},
                {true, false, false, true, false, false, false, true, true, true, true, 62},
                {true, false, false, false, true, false, false, true, true, true, true, 10},
                {true, false, false, false, false, true, false, true, true, true, true, 62},
                {true, false, false, false, false, false, true, true, true, true, true, 158},
                {true, true, true, true, true, true, true, true, true, true, true, 614},
                {true, true, true, true, true, true, true, true, false, false, false, 96},
                {true, true, true, true, true, true, true, false, true, false, false, 220},
                {true, true, true, true, true, true, true, false, false, true, false, 238},
                {true, true, true, true, true, true, true, false, false, false, true, 60},
                {true, true, true, true, true, true, true, true, true, true, true, 614},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[10]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[11], coinList.size());
        }
    }

    /**
     * For CoinSets
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_CoinSetsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        CoinSets coinClass = new CoinSets();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, false, false, 0},
                {true, false, false, 77},
                {false, true, false, 60},
                {false, false, true, 48},
                {true, true, true, 77 + 60 + 48},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[0]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[1]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[2]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());
        }
    }

    /**
     * For EarlyDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_EarlyDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        EarlyDimes coinClass = new EarlyDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, true, true, true, 87},
                {true, false, true, false, false, true, true, true, 49},
                {true, false, false, true, false, true, true, true, 59},
                {true, false, false, false, true, true, true, true, 39},
                {true, true, true, true, true, true, false, false, 116},
                {true, true, true, true, true, false, true, false, 124},
                {true, true, true, true, true, false, false, true, 134},
                {true, true, true, true, true, true, true, true, 144},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[7]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[8], coinList.size());
        }
    }

    /**
     * For EarlyDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_EarlyDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        EarlyDollars coinClass = new EarlyDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, true, true, true, 61},
                {true, false, true, false, false, true, true, true, 18},
                {true, false, false, true, false, true, true, true, 23},
                {true, false, false, false, true, true, true, true, 23},
                {true, true, true, true, true, true, false, false, 13},
                {true, true, true, true, true, false, true, false, 49},
                {true, true, true, true, true, false, false, true, 24},
                {true, true, true, true, true, true, true, true, 86},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[7]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[8], coinList.size());
        }
    }

    /**
     * For EarlyHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_EarlyHalfDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        EarlyHalfDollars coinClass = new EarlyHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, true, true, 97},
                {true, false, true, false, false, true, true, 67},
                {true, false, false, true, false, true, true, 68},
                {true, false, false, false, true, true, true, 52},
                {true, true, true, true, true, true, false, 44},
                {true, true, true, true, true, false, true, 114},
                {true, true, true, true, true, true, true, 158},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[6]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[7], coinList.size());
        }
    }

    /**
     * For EarlyQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_EarlyQuartersCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        EarlyQuarters coinClass = new EarlyQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, true, true, true, true, true, 120},
                {true, false, true, false, false, false, true, true, true, true, true, 44},
                {true, false, false, true, false, false, true, true, true, true, true, 81},
                {true, false, false, false, true, false, true, true, true, true, true, 62},
                {true, false, false, false, false, true, true, true, true, true, true, 33},
                {true, true, true, true, true, true, true, false, false, false, false, 4},
                {true, true, true, true, true, true, false, true, false, false, false, 24},
                {true, true, true, true, true, true, false, false, true, false, false, 109},
                {true, true, true, true, true, true, false, false, false, true, false, 74},
                {true, true, true, true, true, true, false, false, false, false, true, 37},
                {true, true, true, true, true, true, true, true, true, true, true, 244},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[10]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[11], coinList.size());
        }
    }

    /**
     * For EisenhowerDollar
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_EisenhowerDollarCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        EisenhowerDollar coinClass = new EisenhowerDollar();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Expected Result
        Object[][] tests = {
                {false, true, true, 7},
                {false, false, true, 7},
                {true, false, false, 0},
                {true, true, false, 7},
                {true, false, true, 7},
                {true, true, true, 7 + 7},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }

        // Test special case of start year 1975/1976
        int[] years = {1975, 1976};
        for (int year : years) {
            parameters.put(CoinPageCreator.OPT_START_YEAR, year);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(6, coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For FirstSpouseGoldCoins
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_FirstSpouseGoldCoinsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        FirstSpouseGoldCoins coinClass = new FirstSpouseGoldCoins();
        coinClass.getCreationParameters(parameters);

        ArrayList<CoinSlot> coinList = new ArrayList<>();
        coinClass.populateCollectionLists(parameters, coinList);
        assertEquals(42, coinList.size());

        checkCreationParamsFromCoinList(coinList, coinClass);
    }

    /**
     * For FranklinHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_FranklinHalfDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        FranklinHalfDollars coinClass = new FranklinHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 16},
                {false, false, false, false, 16},
                {true, true, false, false, 16},
                {true, false, true, false, 14},
                {true, false, false, true, 5},
                {true, true, true, true, 16 + 14 + 5},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For HalfCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_HalfCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        HalfCents coinClass = new HalfCents();
        coinClass.getCreationParameters(parameters);

        Object[][] tests = {
                {44},
        };

        for (Object[] test : tests) {
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[0], coinList.size());
        }
    }

    /**
     * For HalfDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_HalfDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        HalfDimes coinClass = new HalfDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, true, true, 63},
                {true, false, true, false, true, true, 39},
                {true, false, false, true, true, true, 30},
                {true, true, true, true, true, false, 18},
                {true, true, true, true, false, true, 78},
                {true, true, true, true, true, true, 96},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[4]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[5]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[6], coinList.size());
        }
    }

    /**
     * For IndianHeadCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_IndianHeadCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        IndianHeadCents coinClass = new IndianHeadCents();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, S, Expected Result
        Object[][] tests = {
                {false, true, true, 51},
                {false, false, true, 51},
                {true, false, false, 0},
                {true, true, false, 53},
                {true, false, true, 2},
                {true, true, true, 53 + 2},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[2]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For JeffersonNickels
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_JeffersonNickelsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        JeffersonNickels coinClass = new JeffersonNickels();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 89},
                {false, false, false, false, 89},
                {true, true, false, false, 86},
                {true, false, true, false, 86},
                {true, false, false, true, 19},
                {true, true, true, true, 86 + 86 + 19},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For KennedyHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_KennedyHalfDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        KennedyHalfDollars coinClass = new KennedyHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, true, true, true, 69},
                {true, false, true, false, false, false, true, true, true, 66},
                {true, false, false, true, false, false, true, true, true, 8},
                {true, false, false, false, true, false, true, true, true, 62},
                {true, false, false, false, false, true, true, true, true, 51},
                {true, true, true, true, true, true, true, false, false, 7},
                {true, true, true, true, true, true, false, true, false, 55},
                {true, true, true, true, true, true, false, false, true, 174},
                {true, true, true, true, true, true, true, true, true, 236},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[8]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[9], coinList.size());
        }
    }

    /**
     * For LargeCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_LargeCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        LargeCents coinClass = new LargeCents();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, false, 25},
                {false, true, 43},
                {true, true, 68},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[0]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[1]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[2], coinList.size());
        }
    }

    /**
     * For LibertyHeadNickels
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_LibertyHeadNickelsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        LibertyHeadNickels coinClass = new LibertyHeadNickels();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 31},
                {false, false, false, false, 31},
                {true, true, false, false, 31},
                {true, false, true, false, 1},
                {true, false, false, true, 1},
                {true, true, true, true, 31 + 1 + 1},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For LincolnCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_LincolnCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        LincolnCents coinClass = new LincolnCents();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 120},
                {false, false, false, false, 120},
                {true, true, false, false, 120},
                {true, false, true, false, 112},
                {true, false, false, true, 51},
                {true, true, true, true, 120 + 112 + 51},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For MercuryDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_MercuryDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        MercuryDimes coinClass = new MercuryDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 27},
                {false, false, false, false, 27},
                {true, true, false, false, 27},
                {true, false, true, false, 25},
                {true, false, false, true, 25},
                {true, true, true, true, 27 + 25 + 25},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For MorganDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_MorganDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        MorganDollars coinClass = new MorganDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, O, CC, Expected Result
        Object[][] tests = {
                {false, true, false, true, false, true, 28},
                {false, false, true, false, true, false, 28},
                {true, true, false, false, false, false, 28},
                {true, false, true, false, false, false, 1},
                {true, false, false, true, false, false, 28},
                {true, false, false, false, true, false, 26},
                {true, false, false, false, false, true, 13},
                {true, true, true, true, true, true, 28 + 1 + 28 + 26 + 13},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[6], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For NationalParkQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_NationalParkQuartersCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        NationalParkQuarters coinClass = new NationalParkQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Expected Result
        Object[][] tests = {
                {false, true, true, 56},
                {false, false, true, 56},
                {true, false, false, 0},
                {true, true, false, 56},
                {true, false, true, 56},
                {true, true, true, 56 + 56},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For NativeAmericanDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_NativeAmericanDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        NativeAmericanDollars coinClass = new NativeAmericanDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Expected Result
        Object[][] tests = {
                {false, true, true, 25},
                {false, false, true, 25},
                {true, false, false, 0},
                {true, true, false, 25},
                {true, false, true, 25},
                {true, true, true, 25 + 25},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For PeaceDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_PeaceDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        PeaceDollars coinClass = new PeaceDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 10},
                {false, false, false, false, 10},
                {true, true, false, false, 10},
                {true, false, true, false, 5},
                {true, false, false, true, 9},
                {true, true, true, true, 10 + 5 + 9},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For PresidentialDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_PresidentialDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        PresidentialDollars coinClass = new PresidentialDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Expected Result
        Object[][] tests = {
                {false, true, true, 40},
                {false, false, true, 40},
                {true, false, false, 0},
                {true, true, false, 40},
                {true, false, true, 40},
                {true, true, true, 40 + 40},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[3], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For RooseveltDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_RooseveltDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        RooseveltDimes coinClass = new RooseveltDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, false, false, true, true, true, 84},
                {true, false, true, false, false, false, false, false, true, true, true, 84},
                {true, false, false, true, false, false, false, false, true, true, true, 18},
                {true, false, false, false, true, false, false, false, true, true, true, 9},
                {true, false, false, false, false, true, false, false, true, true, true, 8},
                {true, false, false, false, false, false, true, false, true, true, true, 65},
                {true, false, false, false, false, false, false, true, true, true, true, 56},
                {true, true, true, true, true, true, true, true, true, false, false, 5},
                {true, true, true, true, true, true, true, true, false, true, false, 96},
                {true, true, true, true, true, true, true, true, false, false, true, 187},
                {true, true, true, true, true, true, true, true, true, true, true, 288},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, test[6]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_7, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[10]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[11], coinList.size());
        }
    }

    /**
     * For SilverDimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_SilverDimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        SilverDimes coinClass = new SilverDimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, true, true, true, true, 74},
                {true, false, true, false, false, false, true, true, true, true, 55},
                {true, false, false, true, false, false, true, true, true, true, 62},
                {true, false, false, false, true, false, true, true, true, true, 20},
                {true, false, false, false, false, true, true, true, true, true, 51},
                {true, true, true, true, true, true, true, false, false, false, 5},
                {true, true, true, true, true, true, false, true, false, false, 74},
                {true, true, true, true, true, true, false, false, true, false, 77},
                {true, true, true, true, true, true, false, false, false, true, 96},
                {true, true, true, true, true, true, true, true, true, true, 250},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[9]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[10], coinList.size());
        }
    }

    /**
     * For SilverHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_SilverHalfDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        SilverHalfDollars coinClass = new SilverHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, true, true, true, true, true, 71},
                {true, false, true, false, false, false, true, true, true, true, true, 50},
                {true, false, false, true, false, false, true, true, true, true, true, 58},
                {true, false, false, false, true, false, true, true, true, true, true, 22},
                {true, false, false, false, false, true, true, true, true, true, true, 47},
                {true, true, true, true, true, true, true, false, false, false, false, 7},
                {true, true, true, true, true, true, false, true, false, false, false, 73},
                {true, true, true, true, true, true, false, false, true, false, false, 65},
                {true, true, true, true, true, true, false, false, false, true, false, 35},
                {true, true, true, true, true, true, false, false, false, false, true, 55},
                {true, true, true, true, true, true, true, true, true, true, true, 232},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[10]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[11], coinList.size());
        }
    }

    /**
     * For SmallCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_SmallCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        SmallCents coinClass = new SmallCents();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, false, true, true, true, true, true, true, 189},
                {true, false, true, false, false, false, false, true, true, true, true, true, true, 126},
                {true, false, false, true, false, false, false, true, true, true, true, true, true, 64},
                {true, false, false, false, true, false, false, true, true, true, true, true, true, 29},
                {true, false, false, false, false, true, false, true, true, true, true, true, true, 78},
                {true, false, false, false, false, false, true, true, true, true, true, true, true, 14},
                {true, true, true, true, true, true, true, true, false, false, false, false, false, 61},
                {true, true, true, true, true, true, true, false, true, false, false, false, false, 54},
                {true, true, true, true, true, true, true, false, false, true, false, false, false, 106},
                {true, true, true, true, true, true, true, false, false, false, true, false, false, 192},
                {true, true, true, true, true, true, true, false, false, false, false, true, false, 51},
                {true, true, true, true, true, true, true, false, false, false, false, false, true, 238},
                {true, true, true, true, true, true, true, true, true, true, true, true, true, 445},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[8]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[9]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[10]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[11]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[12]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[13], coinList.size());
        }
    }

    /**
     * For SmallDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_SmallDollarsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        SmallDollars coinClass = new SmallDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, true, true, true, 68},
                {true, false, true, false, false, true, true, true, 68},
                {true, false, false, true, false, true, true, true, 3},
                {true, false, false, false, true, true, true, true, 67},
                {true, true, true, true, true, true, false, false, 15},
                {true, true, true, true, true, false, true, false, 72},
                {true, true, true, true, true, false, false, true, 119},
                {true, true, true, true, true, true, true, true, 206},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[7]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[8], coinList.size());
        }
    }

    /**
     * For StandingLibertyQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_StandingLibertyQuartersCreationCounts() {

        HashMap<String, Object> parameters = new ParcelableHashMap();
        StandingLibertyQuarters coinClass = new StandingLibertyQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 15},
                {false, false, false, false, 15},
                {true, true, false, false, 15},
                {true, false, true, false, 10},
                {true, false, false, true, 12},
                {true, true, true, true, 15 + 10 + 12},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For StateQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_StateQuartersCreationCounts() {

        HashMap<String, Object> parameters = new ParcelableHashMap();
        StateQuarters coinClass = new StateQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, Show Territories, Expected Result
        Object[][] tests = {
                {false, true, true, false, 50},
                {false, false, false, true, 56},
                {true, true, false, false, 50},
                {true, false, true, false, 50},
                {true, true, false, true, 56},
                {true, false, true, true, 56},
                {true, true, true, true, 56 + 56},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For SusanBAnthonyDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_SusanBAnthonyDollarsCreationCounts() {

        HashMap<String, Object> parameters = new ParcelableHashMap();
        SusanBAnthonyDollars coinClass = new SusanBAnthonyDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 4},
                {false, false, false, false, 4},
                {true, true, false, false, 4},
                {true, false, true, false, 4},
                {true, false, false, true, 3},
                {true, true, true, true, 4 + 4 + 3},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For Trimes
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_TrimesCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        Trimes coinClass = new Trimes();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, false, 24},
                {false, true, 25},
                {true, true, 49},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[0]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[1]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[2], coinList.size());
        }
    }

    /**
     * For TwentyCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_TwentyCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        TwentyCents coinClass = new TwentyCents();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {7},
        };

        for (Object[] test : tests) {
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[0], coinList.size());
        }
    }

    /**
     * For TwoCents
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_TwoCentsCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        TwoCents coinClass = new TwoCents();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {10},
        };

        for (Object[] test : tests) {
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[0], coinList.size());
        }
    }

    /**
     * For WalkingLibertyHalfDollars
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_WalkingLibertyHalfDollarsCreationCounts() {

        HashMap<String, Object> parameters = new ParcelableHashMap();
        WalkingLibertyHalfDollars coinClass = new WalkingLibertyHalfDollars();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {false, true, false, true, 25},
                {false, false, false, false, 25},
                {true, true, false, false, 20},
                {true, false, true, false, 21},
                {true, false, false, true, 24},
                {true, true, true, true, 20 + 21 + 24},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[4], coinList.size());

            checkCreationParamsFromCoinList(coinList, coinClass);
        }
    }

    /**
     * For WashingtonQuarters
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_WashingtonQuartersCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        WashingtonQuarters coinClass = new WashingtonQuarters();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, false, false, true, true, 100},
                {true, false, true, false, false, false, true, true, 92},
                {true, false, false, true, false, false, true, true, 40},
                {true, false, false, false, true, false, true, true, 30},
                {true, false, false, false, false, true, true, true, 9},
                {true, true, true, true, true, true, true, false, 92},
                {true, true, true, true, true, true, false, true, 179},
                {true, true, true, true, true, true, true, true, 271},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, test[4]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[7]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[8], coinList.size());
        }
    }

    /**
     * For WashingtonSilver
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_WashingtonSilverCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        WashingtonSilver coinClass = new WashingtonSilver();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, Expected Result
        Object[][] tests = {
                {true, true, false, false, true, true, true, true, true, 165},
                {true, false, true, false, true, true, true, true, true, 164},
                {true, false, false, true, true, true, true, true, true, 162},
                {true, true, true, true, true, false, false, false, false, 92},
                {true, true, true, true, false, true, false, false, false, 50},
                {true, true, true, true, false, false, true, false, false, 6},
                {true, true, true, true, false, false, false, true, false, 62},
                {true, true, true, true, false, false, false, false, true, 15},
                {true, true, true, true, true, true, true, true, true, 225},
        };

        for (Object[] test : tests) {
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARKS, test[0]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_1, test[1]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_2, test[2]);
            parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_3, test[3]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_1, test[4]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_2, test[5]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_3, test[6]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_4, test[7]);
            parameters.put(CoinPageCreator.OPT_CHECKBOX_5, test[8]);

            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[9], coinList.size());
        }
    }

    /**
     * For WestPoint
     * - Test that the number of coins is correct upon collection creation
     */
    @Test
    public void test_WestPointCreationCounts() {

        ParcelableHashMap parameters = new ParcelableHashMap();
        WestPoint coinClass = new WestPoint();
        coinClass.getCreationParameters(parameters);

        // Show Mint Marks, P, D, S, W, Expected Result
        Object[][] tests = {
                {21},
        };

        for (Object[] test : tests) {
            ArrayList<CoinSlot> coinList = new ArrayList<>();
            coinClass.populateCollectionLists(parameters, coinList);
            assertEquals(test[0], coinList.size());
        }
    }
}