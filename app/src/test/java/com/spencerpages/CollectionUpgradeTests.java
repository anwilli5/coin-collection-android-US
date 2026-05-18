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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.CollectionListInfo;
import com.coincollection.MainActivity;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.AmericanWomenQuarters;
import com.spencerpages.collections.Cartwheels;
import com.spencerpages.collections.CladQuarters;
import com.spencerpages.collections.CoinSets;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BasicDimes;
import com.spencerpages.collections.BasicHalfDollars;
import com.spencerpages.collections.BasicInnovationDollars;
import com.spencerpages.collections.BasicQuarters;
import com.spencerpages.collections.BuffaloNickels;
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
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class CollectionUpgradeTests extends BaseTestCase {

    /**
     * For AmericanEagleSilverDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_AmericanEagleSilverDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new AmericanEagleSilverDollars();
        String coinType = "American Eagle Silver Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1986;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= VERSION_1_YEAR; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BasicInnovationDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BasicInnovationDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BasicInnovationDollars();
        String coinType = "American Innovation Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        coinList.add(new Object[]{"Introductory", "", 0});
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BarberDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BarberDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BarberDimes();
        String coinType = "Barber Dimes";
        String collectionName = coinType + " Upgrade";
        int startYear = 1892;
        int endYear = 1916;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BarberHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BarberHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BarberHalfDollars();
        String coinType = "Barber Half Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1892;
        int endYear = 1915;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BarberQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BarberQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BarberQuarters();
        String coinType = "Barber Quarters";
        String collectionName = coinType + " Upgrade";
        int startYear = 1892;
        int endYear = 1916;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BuffaloNickels
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BuffaloNickelsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BuffaloNickels();
        String coinType = "Buffalo Nickels";
        String collectionName = coinType + " Upgrade";
        int startYear = 1913;
        int endYear = 1938;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1922 || i == 1932 || i == 1933) {
                continue;
            }
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For EisenhowerDollar
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_EisenhowerDollarUpgrade() {

        // Test Parameters
        CollectionInfo collection = new EisenhowerDollar();
        String coinType = "Eisenhower Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1971;
        int endYear = 1978;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1975) {
                continue;
            }
            if (i == 1976) {
                coinList.add(new Object[]{"1776-1976", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For FirstSpouseGoldCoins
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_FirstSpouseGoldCoinsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new FirstSpouseGoldCoins();
        String coinType = "First Spouse Gold Coins";
        String collectionName = coinType + " Upgrade";

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        coinList.add(new Object[]{"Martha Washington", "", 0});
        coinList.add(new Object[]{"Abigail Adams", "", 0});
        coinList.add(new Object[]{"Thomas Jefferson's Liberty", "", 0});
        coinList.add(new Object[]{"Dolley Madison", "", 0});
        coinList.add(new Object[]{"Elizabeth Monroe", "", 0});
        coinList.add(new Object[]{"Louisa Adams", "", 0});
        coinList.add(new Object[]{"Andrew Jackson's Liberty", "", 0});
        coinList.add(new Object[]{"Martin Van Buren's Liberty", "", 0});
        coinList.add(new Object[]{"Anna Harrison", "", 0});
        coinList.add(new Object[]{"Letitia Tyler", "", 0});
        coinList.add(new Object[]{"Julia Tyler", "", 0});
        coinList.add(new Object[]{"Sarah Polk", "", 0});
        coinList.add(new Object[]{"Margaret Taylor", "", 0});
        coinList.add(new Object[]{"Abigail Fillmore", "", 0});
        coinList.add(new Object[]{"Jane Pierce", "", 0});
        coinList.add(new Object[]{"James Buchanan's Liberty", "", 0});
        coinList.add(new Object[]{"Mary Todd Lincoln", "", 0});
        coinList.add(new Object[]{"Eliza Johnson", "", 0});
        coinList.add(new Object[]{"Julia Grant", "", 0});
        coinList.add(new Object[]{"Lucy Hayes", "", 0});
        coinList.add(new Object[]{"Lucretia Garfield", "", 0});
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For FranklinHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_FranklinHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new FranklinHalfDollars();
        String coinType = "Franklin Half Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1948;
        int endYear = 1963;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For IndianHeadCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_IndianHeadCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new IndianHeadCents();
        String coinType = "Indian Head Cents";
        String collectionName = coinType + " Upgrade";
        int startYear = 1859;
        int endYear = 1909;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For JeffersonNickels
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_JeffersonNickelsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new JeffersonNickels();
        String coinType = "Nickels";
        String collectionName = coinType + " Upgrade";
        int startYear = 1938;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= VERSION_1_YEAR; i++) {
            if (i == 2004) {
                coinList.add(new Object[]{"Peace Medal", "", 0});
                coinList.add(new Object[]{"Keelboat", "", 0});
            } else if (i == 2005) {
                coinList.add(new Object[]{"American Bison", "", 0});
                coinList.add(new Object[]{"Ocean in View!", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BasicHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BasicHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BasicHalfDollars();
        String coinType = "Half-Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1964;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= VERSION_1_YEAR; i++) {
            if (i == 1975) {
                continue;
            }
            if (i == 1976) {
                coinList.add(new Object[]{"1776-1976", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For LibertyHeadNickels
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_LibertyHeadNickelsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new LibertyHeadNickels();
        String coinType = "Liberty Head Nickels";
        String collectionName = coinType + " Upgrade";
        int startYear = 1883;
        int endYear = 1912;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1883) {
                coinList.add(new Object[]{"1883 w/ Cents", "", 0});
                coinList.add(new Object[]{"1883 w/o Cents", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For LincolnCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_LincolnCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new LincolnCents();
        String coinType = "Pennies";
        String collectionName = coinType + " Upgrade";
        int startYear = 1909;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= VERSION_1_YEAR; i++) {
            if (i == 1909) {
                coinList.add(new Object[]{"1909 V.D.B", "", 0});
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            } else if (i == 2009) {
                coinList.add(new Object[]{"Early Childhood", "", 0});
                coinList.add(new Object[]{"Formative Years", "", 0});
                coinList.add(new Object[]{"Professional Life", "", 0});
                coinList.add(new Object[]{"Presidency", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For MercuryDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_MercuryDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new MercuryDimes();
        String coinType = "Mercury Dimes";
        String collectionName = coinType + " Upgrade";
        int startYear = 1916;
        int endYear = 1945;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1922 || i == 1932 || i == 1933) {
                continue;
            }
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For MorganDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_MorganDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new MorganDollars();
        String coinType = "Morgan Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1878;
        int endYear = 1921;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i > 1904 && i < 1921) {
                continue;
            }
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For NationalParkQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_NationalParkQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new NationalParkQuarters();
        String coinType = "National Park Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        coinList.add(new Object[]{"Hot Springs", "", 0});
        coinList.add(new Object[]{"Yellowstone", "", 0});
        coinList.add(new Object[]{"Yosemite", "", 0});
        coinList.add(new Object[]{"Grand Canyon", "", 0});
        coinList.add(new Object[]{"Mt. Hood", "", 0});
        coinList.add(new Object[]{"Gettysburg", "", 0});
        coinList.add(new Object[]{"Glacier", "", 0});
        coinList.add(new Object[]{"Olympic", "", 0});
        coinList.add(new Object[]{"Vicksburg", "", 0});
        coinList.add(new Object[]{"Chickasaw", "", 0});
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For NativeAmericanDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_NativeAmericanDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new NativeAmericanDollars();
        String coinType = "Sacagawea Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 2000;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= VERSION_1_YEAR; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For PeaceDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_PeaceDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new PeaceDollars();
        String coinType = "Peace Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1921;
        int endYear = 1935;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i >= 1929 && i <= 1933) {
                continue;
            }
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For PresidentialDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_PresidentialDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new PresidentialDollars();
        String coinType = "Presidential Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        coinList.add(new Object[]{"George Washington", "", 0});
        coinList.add(new Object[]{"John Adams", "", 0});
        coinList.add(new Object[]{"Thomas Jefferson", "", 0});
        coinList.add(new Object[]{"James Madison", "", 0});
        coinList.add(new Object[]{"James Monroe", "", 0});
        coinList.add(new Object[]{"John Quincy Adams", "", 0});
        coinList.add(new Object[]{"Andrew Jackson", "", 0});
        coinList.add(new Object[]{"Martin Van Buren", "", 0});
        coinList.add(new Object[]{"William Henry Harrison", "", 0});
        coinList.add(new Object[]{"John Tyler", "", 0});
        coinList.add(new Object[]{"James K. Polk", "", 0});
        coinList.add(new Object[]{"Zachary Taylor", "", 0});
        coinList.add(new Object[]{"Millard Fillmore", "", 0});
        coinList.add(new Object[]{"Franklin Pierce", "", 0});
        coinList.add(new Object[]{"James Buchanan", "", 0});
        coinList.add(new Object[]{"Abraham Lincoln", "", 0});
        coinList.add(new Object[]{"Andrew Johnson", "", 0});
        coinList.add(new Object[]{"Ulysses S. Grant", "", 0});
        coinList.add(new Object[]{"Rutherford B. Hayes", "", 0});
        coinList.add(new Object[]{"James Garfield", "", 0});
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BasicDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BasicDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BasicDimes();
        String coinType = "Dimes";
        String collectionName = coinType + " Upgrade";
        int startYear = 1946;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= VERSION_1_YEAR; i++) {
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For StandingLibertyQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_StandingLibertyQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new StandingLibertyQuarters();
        String coinType = "Standing Liberty Quarters";
        String collectionName = coinType + " Upgrade";
        int startYear = 1916;
        int endYear = 1930;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1922) {
                continue;
            }
            if (i == 1917) {
                coinList.add(new Object[]{"1917 Type 1", "", 0});
                coinList.add(new Object[]{"1917 Type 2", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For StateQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_StateQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new StateQuarters();
        String coinType = "State Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        coinList.add(new Object[]{"Delaware", "", 0});
        coinList.add(new Object[]{"Pennsylvania", "", 0});
        coinList.add(new Object[]{"New Jersey", "", 0});
        coinList.add(new Object[]{"Georgia", "", 0});
        coinList.add(new Object[]{"Connecticut", "", 0});
        coinList.add(new Object[]{"Massachusetts", "", 0});
        coinList.add(new Object[]{"Maryland", "", 0});
        coinList.add(new Object[]{"South Carolina", "", 0});
        coinList.add(new Object[]{"New Hampshire", "", 0});
        coinList.add(new Object[]{"Virginia", "", 0});
        coinList.add(new Object[]{"New York", "", 0});
        coinList.add(new Object[]{"North Carolina", "", 0});
        coinList.add(new Object[]{"Rhode Island", "", 0});
        coinList.add(new Object[]{"Vermont", "", 0});
        coinList.add(new Object[]{"Kentucky", "", 0});
        coinList.add(new Object[]{"Tennessee", "", 0});
        coinList.add(new Object[]{"Ohio", "", 0});
        coinList.add(new Object[]{"Louisiana", "", 0});
        coinList.add(new Object[]{"Indiana", "", 0});
        coinList.add(new Object[]{"Mississippi", "", 0});
        coinList.add(new Object[]{"Illinois", "", 0});
        coinList.add(new Object[]{"Alabama", "", 0});
        coinList.add(new Object[]{"Maine", "", 0});
        coinList.add(new Object[]{"Missouri", "", 0});
        coinList.add(new Object[]{"Arkansas", "", 0});
        coinList.add(new Object[]{"Michigan", "", 0});
        coinList.add(new Object[]{"Florida", "", 0});
        coinList.add(new Object[]{"Texas", "", 0});
        coinList.add(new Object[]{"Iowa", "", 0});
        coinList.add(new Object[]{"Wisconsin", "", 0});
        coinList.add(new Object[]{"California", "", 0});
        coinList.add(new Object[]{"Minnesota", "", 0});
        coinList.add(new Object[]{"Oregon", "", 0});
        coinList.add(new Object[]{"Kansas", "", 0});
        coinList.add(new Object[]{"West Virginia", "", 0});
        coinList.add(new Object[]{"Nevada", "", 0});
        coinList.add(new Object[]{"Nebraska", "", 0});
        coinList.add(new Object[]{"Colorado", "", 0});
        coinList.add(new Object[]{"North Dakota", "", 0});
        coinList.add(new Object[]{"South Dakota", "", 0});
        coinList.add(new Object[]{"Montana", "", 0});
        coinList.add(new Object[]{"Washington", "", 0});
        coinList.add(new Object[]{"Idaho", "", 0});
        coinList.add(new Object[]{"Wyoming", "", 0});
        coinList.add(new Object[]{"Utah", "", 0});
        coinList.add(new Object[]{"Oklahoma", "", 0});
        coinList.add(new Object[]{"New Mexico", "", 0});
        coinList.add(new Object[]{"Arizona", "", 0});
        coinList.add(new Object[]{"Alaska", "", 0});
        coinList.add(new Object[]{"Hawaii", "", 0});
        coinList.add(new Object[]{"District of Columbia", "", 0});
        coinList.add(new Object[]{"Puerto Rico", "", 0});
        coinList.add(new Object[]{"Guam", "", 0});
        coinList.add(new Object[]{"American Samoa", "", 0});
        coinList.add(new Object[]{"U.S. Virgin Islands", "", 0});
        coinList.add(new Object[]{"Northern Mariana Islands", "", 0});
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();
        // Compare against a new database
        ParcelableHashMap parameters = new ParcelableHashMap();
        parameters.put(CoinPageCreator.OPT_CHECKBOX_1, Boolean.TRUE);
        validateUpdatedDb(collection, collectionName, parameters);
    }

    /**
     * For SusanBAnthonyDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_SusanBAnthonyDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new SusanBAnthonyDollars();
        String coinType = "Susan B. Anthony Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1979;
        int endYear = 1999;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i > 1981 && i < 1999) {
                continue;
            }
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For WalkingLibertyHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_WalkingLibertyHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new WalkingLibertyHalfDollars();
        String coinType = "Walking Liberty Half Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1916;
        int endYear = 1947;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1922 || (i >= 1924 && i <= 1926) || (i >= 1930 && i <= 1932)) {
                continue;
            }
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BasicQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_BasicQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new BasicQuarters();
        String coinType = "Quarters";
        String collectionName = coinType + " Upgrade";
        int startYear = 1932;
        int endYear = 2021;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            if (i == 1933 || i == 1975) {
                continue;
            }
            if (i > 1998 && i < 2021) {
                continue;
            }
            if (i == 2021) {
                coinList.add(new Object[]{"Crossing the Delaware", "", 0});
            } else if (i == 1976) {
                coinList.add(new Object[]{"1776-1976", "", 0});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            }
        }
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For BasicQuarters (V23 upgrade path)
     * - Tests that a V23 BasicQuarters collection with endYear=2021 (the V23-era STOP_YEAR)
     *   correctly receives the 2026 SemiQ coins on upgrade. The V1 test above masks this
     *   because setCreationParametersFromCoinData uses the current STOP_YEAR (2026) during
     *   the V14 upgrade, giving V1 collections a higher endYear than real V23 collections had.
     */
    @Test
    public void test_BasicQuartersV23Upgrade() {

        // Test Parameters
        CollectionInfo collection = new BasicQuarters();
        String coinType = "Quarters";
        String collectionName = coinType + " V23 Upgrade";

        // Build a V23 coin list matching what a real V23 user would have had
        // (coins up to "Crossing the Delaware", endYear=2021)
        int startYear = 1932;
        int v23EndYear = 2021;
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (int i = startYear; i <= v23EndYear; i++) {
            if (i == 1933 || i == 1975) {
                continue;
            }
            if (i > 1998 && i < 2021) {
                continue;
            }
            if (i == 2021) {
                coinList.add(new Object[]{"Crossing the Delaware", "", 0, -1});
            } else if (i == 1976) {
                coinList.add(new Object[]{"1776-1976", "", 0, -1});
            } else {
                coinList.add(new Object[]{Integer.toString(i), "", 0, -1});
            }
        }

        // Create V23 database with endYear=2021 (matching the real V23 STOP_YEAR)
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23Collection(db, collectionName, coinType, coinList, startYear, v23EndYear, 0, 0);
        db.close();
        testDbHelper.close();

        // Compare against a new database — should include 2026 SemiQ coins
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For AllNickels
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_AllNickelsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new AllNickels();
        String coinType = "All Nickels";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For AmericanInnovationDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_AmericanInnovationDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new AmericanInnovationDollars();
        String coinType = "American Innovation Dollars w/ Proofs";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For AmericanWomenQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_AmericanWomenQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new AmericanWomenQuarters();
        String coinType = "American Women Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        coinList.add(new Object[]{"Maya Angelou", "", 0});
        coinList.add(new Object[]{"Dr. Sally Ride", "", 0});
        coinList.add(new Object[]{"Wilma Mankiller", "", 0});
        coinList.add(new Object[]{"Nina Otero-Warren", "", 0});
        coinList.add(new Object[]{"Anna May Wong", "", 0});
        createV1Collection(db, collectionName, coinType, coinList);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For Cartwheels
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_CartwheelsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new Cartwheels();
        String coinType = "Cartwheels";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For CladQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_CladQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new CladQuarters();
        String coinType = "Clad Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For CoinSets
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_CoinSetsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new CoinSets();
        String coinType = "Coin Sets";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For EarlyDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_EarlyDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new EarlyDimes();
        String coinType = "Early Dimes";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For EarlyDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_EarlyDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new EarlyDollars();
        String coinType = "Early Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For EarlyHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_EarlyHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new EarlyHalfDollars();
        String coinType = "Early Half Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For EarlyQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_EarlyQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new EarlyQuarters();
        String coinType = "Early Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For HalfCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_HalfCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new HalfCents();
        String coinType = "Half Cents";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For HalfDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_HalfDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new HalfDimes();
        String coinType = "Half Dimes";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For KennedyHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_KennedyHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new KennedyHalfDollars();
        String coinType = "Kennedy Half Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For LargeCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_LargeCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new LargeCents();
        String coinType = "Large Cents";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For RooseveltDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_RooseveltDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new RooseveltDimes();
        String coinType = "Roosevelt Dimes";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For SilverDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_SilverDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new SilverDimes();
        String coinType = "Silver Dimes";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For SilverHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_SilverHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new SilverHalfDollars();
        String coinType = "Silver Half Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For SilverQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_SilverQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new SilverQuarters();
        String coinType = "Silver Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For SmallCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_SmallCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new SmallCents();
        String coinType = "Small Cents";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For SmallDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_SmallDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new SmallDollars();
        String coinType = "Small Dollars";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, "2026");
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For Trimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_TrimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new Trimes();
        String coinType = "Three Cents";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For TwentyCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_TwentyCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new TwentyCents();
        String coinType = "Twenty Cents";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For TwoCents
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_TwoCentsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new TwoCents();
        String coinType = "Two Cents";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For WashingtonQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_WashingtonQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new WashingtonQuarters();
        String coinType = "Washington Quarters";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * For WestPoint
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_WestPointUpgrade() {

        // Test Parameters
        CollectionInfo collection = new WestPoint();
        String coinType = "West Point Mint";
        String collectionName = coinType + " Upgrade";

        // Create V23 database and run upgrade
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulate(db, collection, coinType, collectionName, null);
        db.close();
        testDbHelper.close();

        // Compare against a new database
        validateUpdatedDb(collection, collectionName);
    }

    /**
     * Test that the duplicate coin removal migration works correctly.
     * Simulates the off-by-one bug (PR #280) by inserting duplicate coins into
     * a V23 database, then verifying the upgrade removes duplicates while
     * preserving collected status.
     */
    @Test
    public void test_DuplicateCoinRemoval() {

        // Use BasicDimes as a representative year-based collection
        CollectionInfo collection = new BasicDimes();
        String coinType = "Dimes";
        String collectionName = coinType + " Dup Test";

        // Create a V23 database with the correct coins plus intentional duplicates
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();

        // Build the normal coin list (excludes 2026 so upgrade will add it)
        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        ArrayList<CoinSlot> fullCoinList = new ArrayList<>();
        collection.populateCollectionLists(parameters, fullCoinList);
        long mintMarkFlags = CoinPageCreator.getMintMarkFlagsFromParameters(parameters);
        long checkboxFlags = CoinPageCreator.getCheckboxFlagsFromParameters(parameters);

        int endYear = 0;
        ArrayList<Object[]> coinList = new ArrayList<>();
        for (CoinSlot coin : fullCoinList) {
            if (coin.getIdentifier().contains("2026")) {
                continue;
            }
            coinList.add(new Object[]{coin.getIdentifier(), coin.getMint(), 0, coin.getImageId()});
            try {
                int year = Integer.parseInt(coin.getIdentifier());
                endYear = Math.max(endYear, year);
            } catch (NumberFormatException ignored) {
            }
        }

        createV23Collection(db, collectionName, coinType, coinList,
                collection.getStartYear(), endYear, mintMarkFlags, checkboxFlags);

        // Now insert duplicate coins for 2021-2024 (simulating the off-by-one bug)
        // Mark one 2022 duplicate as collected to test that collected status is preserved
        String[] dupYears = {"2021", "2022", "2023", "2024"};
        for (String year : dupYears) {
            ContentValues values = new ContentValues();
            values.put(CoinSlot.COL_COIN_IDENTIFIER, year);
            values.put(CoinSlot.COL_COIN_MINT, "");
            values.put(CoinSlot.COL_IN_COLLECTION, year.equals("2022") ? 1 : 0);
            values.put(CoinSlot.COL_SORT_ORDER, 9000 + Integer.parseInt(year));
            db.insert("[" + collectionName + "]", null, values);
        }

        // Insert a custom (user-added) coin that duplicates an existing identifier.
        // Custom coins must be preserved and NOT removed by the migration.
        ContentValues customCoin = new ContentValues();
        customCoin.put(CoinSlot.COL_COIN_IDENTIFIER, "2023");
        customCoin.put(CoinSlot.COL_COIN_MINT, "");
        customCoin.put(CoinSlot.COL_IN_COLLECTION, 1);
        customCoin.put(CoinSlot.COL_SORT_ORDER, 9999);
        customCoin.put(CoinSlot.COL_CUSTOM_COIN, 1);
        db.insert("[" + collectionName + "]", null, customCoin);

        // Update the total to include the duplicates and the custom coin
        int extraRows = dupYears.length + 1;
        ContentValues totalUpdate = new ContentValues();
        totalUpdate.put(CollectionListInfo.COL_TOTAL, coinList.size() + extraRows);
        db.update(CollectionListInfo.TBL_COLLECTION_INFO, totalUpdate,
                CollectionListInfo.COL_NAME + "=?", new String[]{collectionName});

        // Verify duplicates exist before upgrade
        Cursor preCursor = db.rawQuery(
                "SELECT COUNT(*) FROM [" + collectionName + "]"
                        + " WHERE " + CoinSlot.COL_COIN_IDENTIFIER + "='2021'"
                        + " AND " + CoinSlot.COL_COIN_MINT + "=''",
                null);
        preCursor.moveToFirst();
        assertTrue("Expected duplicates before upgrade", preCursor.getInt(0) > 1);
        preCursor.close();

        db.close();
        testDbHelper.close();

        // Opening the database triggers the upgrade (v23 -> v24) which should
        // remove non-custom duplicates. Validate by launching the app and
        // comparing the upgraded collection against a freshly created one,
        // accounting for the extra custom coin that must survive.
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {

                // Build a fresh reference coin list
                ParcelableHashMap refParams = new ParcelableHashMap();
                collection.getCreationParameters(refParams);
                ArrayList<CoinSlot> expectedCoins = new ArrayList<>();
                collection.populateCollectionLists(refParams, expectedCoins);

                // Get the upgraded coin list from the database
                ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(collectionName, true);
                assertNotNull(dbCoins);

                // Separate custom coins from non-custom coins
                ArrayList<CoinSlot> dbNonCustom = new ArrayList<>();
                ArrayList<CoinSlot> dbCustom = new ArrayList<>();
                for (CoinSlot coin : dbCoins) {
                    if (coin.isCustomCoin()) {
                        dbCustom.add(coin);
                    } else {
                        dbNonCustom.add(coin);
                    }
                }

                // Non-custom coins should match the fresh list exactly (no duplicates)
                assertEquals("Non-custom coin count should match fresh collection",
                        expectedCoins.size(), dbNonCustom.size());
                for (int i = 0; i < expectedCoins.size(); i++) {
                    assertEquals(expectedCoins.get(i).getIdentifier(), dbNonCustom.get(i).getIdentifier());
                    assertEquals(expectedCoins.get(i).getMint(), dbNonCustom.get(i).getMint());
                }

                // The custom coin ("2023", "") must have survived the migration
                assertEquals("Custom coin should be preserved", 1, dbCustom.size());
                assertEquals("2023", dbCustom.get(0).getIdentifier());
                assertTrue("Custom coin should still be collected", dbCustom.get(0).isInCollection());

                // Verify the stored total accounts for all coins including the custom one
                ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(collectionListEntries);
                boolean foundTable = false;
                for (CollectionListInfo info : collectionListEntries) {
                    if (collectionName.equals(info.getName())) {
                        foundTable = true;
                        assertEquals("Total should include custom coin",
                                expectedCoins.size() + 1, info.getMax());
                        break;
                    }
                }
                assertTrue(foundTable);
            });
        }
    }

    /**
     * Test that the duplicate coin removal migration works correctly for
     * named-identifier collections, including identifiers with special
     * characters (apostrophes, diacritics). Uses AmericanWomenQuarters
     * which has identifiers like "Edith Kanaka'ole" and "Zitkala-Ša".
     */
    @Test
    public void test_DuplicateCoinRemovalNamedIdentifiers() {

        // Use AmericanWomenQuarters as a representative named-identifier collection
        CollectionInfo collection = new AmericanWomenQuarters();
        String coinType = "American Women Quarters";
        String collectionName = coinType + " Dup Test";

        // Create a V23 database with the correct coins
        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();

        // Build the normal coin list (no exclusions — AWQ has no new coins in v24)
        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        ArrayList<CoinSlot> fullCoinList = new ArrayList<>();
        collection.populateCollectionLists(parameters, fullCoinList);
        long mintMarkFlags = CoinPageCreator.getMintMarkFlagsFromParameters(parameters);
        long checkboxFlags = CoinPageCreator.getCheckboxFlagsFromParameters(parameters);

        ArrayList<Object[]> coinList = new ArrayList<>();
        for (CoinSlot coin : fullCoinList) {
            coinList.add(new Object[]{coin.getIdentifier(), coin.getMint(), 0, coin.getImageId()});
        }

        createV23Collection(db, collectionName, coinType, coinList,
                collection.getStartYear(), 0, mintMarkFlags, checkboxFlags);

        // Insert duplicate coins for identifiers with special characters
        // (simulating the off-by-one bug). Mark one duplicate as collected
        // to test that collected status is preserved.
        String[] dupIdentifiers = {
                "Edith Kanaka'ole",
                "Zitkala-Ša",
                "Bessie Coleman",
                "Celia Cruz"
        };
        for (String identifier : dupIdentifiers) {
            ContentValues values = new ContentValues();
            values.put(CoinSlot.COL_COIN_IDENTIFIER, identifier);
            values.put(CoinSlot.COL_COIN_MINT, "");
            values.put(CoinSlot.COL_IN_COLLECTION, identifier.equals("Zitkala-Ša") ? 1 : 0);
            values.put(CoinSlot.COL_SORT_ORDER, 9000);
            db.insert("[" + collectionName + "]", null, values);
        }

        // Insert a custom (user-added) coin that duplicates an existing identifier.
        // Custom coins must be preserved and NOT removed by the migration.
        ContentValues customCoin = new ContentValues();
        customCoin.put(CoinSlot.COL_COIN_IDENTIFIER, "Bessie Coleman");
        customCoin.put(CoinSlot.COL_COIN_MINT, "");
        customCoin.put(CoinSlot.COL_IN_COLLECTION, 1);
        customCoin.put(CoinSlot.COL_SORT_ORDER, 9999);
        customCoin.put(CoinSlot.COL_CUSTOM_COIN, 1);
        db.insert("[" + collectionName + "]", null, customCoin);

        // Update the total to include the duplicates and the custom coin
        int extraRows = dupIdentifiers.length + 1;
        ContentValues totalUpdate = new ContentValues();
        totalUpdate.put(CollectionListInfo.COL_TOTAL, coinList.size() + extraRows);
        db.update(CollectionListInfo.TBL_COLLECTION_INFO, totalUpdate,
                CollectionListInfo.COL_NAME + "=?", new String[]{collectionName});

        // Verify duplicates exist before upgrade
        Cursor preCursor = db.rawQuery(
                "SELECT COUNT(*) FROM [" + collectionName + "]"
                        + " WHERE " + CoinSlot.COL_COIN_IDENTIFIER + "=?"
                        + " AND " + CoinSlot.COL_COIN_MINT + "=''",
                new String[]{"Bessie Coleman"});
        preCursor.moveToFirst();
        assertTrue("Expected duplicates before upgrade", preCursor.getInt(0) > 1);
        preCursor.close();

        db.close();
        testDbHelper.close();

        // Opening the database triggers the upgrade (v23 -> v24) which should
        // remove non-custom duplicates.
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class))) {
            scenario.onActivity(activity -> {

                // Build a fresh reference coin list
                ParcelableHashMap refParams = new ParcelableHashMap();
                collection.getCreationParameters(refParams);
                ArrayList<CoinSlot> expectedCoins = new ArrayList<>();
                collection.populateCollectionLists(refParams, expectedCoins);

                // Get the upgraded coin list from the database
                ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(collectionName, true);
                assertNotNull(dbCoins);

                // Separate custom coins from non-custom coins
                ArrayList<CoinSlot> dbNonCustom = new ArrayList<>();
                ArrayList<CoinSlot> dbCustom = new ArrayList<>();
                for (CoinSlot coin : dbCoins) {
                    if (coin.isCustomCoin()) {
                        dbCustom.add(coin);
                    } else {
                        dbNonCustom.add(coin);
                    }
                }

                // Non-custom coins should match the fresh list exactly (no duplicates)
                assertEquals("Non-custom coin count should match fresh collection",
                        expectedCoins.size(), dbNonCustom.size());
                for (int i = 0; i < expectedCoins.size(); i++) {
                    assertEquals(expectedCoins.get(i).getIdentifier(), dbNonCustom.get(i).getIdentifier());
                    assertEquals(expectedCoins.get(i).getMint(), dbNonCustom.get(i).getMint());
                }

                // The custom coin ("Bessie Coleman", "") must have survived the migration
                assertEquals("Custom coin should be preserved", 1, dbCustom.size());
                assertEquals("Bessie Coleman", dbCustom.get(0).getIdentifier());
                assertTrue("Custom coin should still be collected", dbCustom.get(0).isInCollection());

                // Verify the stored total accounts for all coins including the custom one
                ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
                activity.mDbAdapter.getAllTables(collectionListEntries);
                boolean foundTable = false;
                for (CollectionListInfo info : collectionListEntries) {
                    if (collectionName.equals(info.getName())) {
                        foundTable = true;
                        assertEquals("Total should include custom coin",
                                expectedCoins.size() + 1, info.getMax());
                        break;
                    }
                }
                assertTrue(foundTable);
            });
        }
    }

}