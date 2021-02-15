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

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CollectionInfo;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BuffaloNickels;
import com.spencerpages.collections.EisenhowerDollar;
import com.spencerpages.collections.FirstSpouseGoldCoins;
import com.spencerpages.collections.FranklinHalfDollars;
import com.spencerpages.collections.IndianHeadCents;
import com.spencerpages.collections.JeffersonNickels;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LibertyHeadNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.MercuryDimes;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NationalParkQuarters;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.PresidentialDollars;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.WalkingLibertyHalfDollars;
import com.spencerpages.collections.WashingtonQuarters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
// TODO - Must keep at 28 until Robolectric supports Java 9 (required to use 29+)
@Config(sdk = Build.VERSION_CODES.P)
public class CollectionUpgradeTests extends BaseTestCase {

    // TODO - Improve tests by testing with all options set instead of none

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
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
            coinList.add(new Object[]{Integer.toString(i), "", 0});
        }
        createV1Collection(db, collectionName, coinType, coinList);
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
        for(int i = startYear; i <= endYear; i++){
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
        for(int i = startYear; i <= endYear; i++){
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
        for(int i = startYear; i <= endYear; i++){
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
        for(int i = startYear; i <= endYear; i++){
            if(i == 1922 || i == 1932 || i == 1933){
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
        for(int i = startYear; i <= endYear; i++){
            if (i == 1975){
                continue;
            }
            if(i == 1976){
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
        for(int i = startYear; i <= endYear; i++){
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
        for(int i = startYear; i <= endYear; i++){
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
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
            if(i == 2004){
                coinList.add(new Object[]{"Peace Medal", "", 0});
                coinList.add(new Object[]{"Keelboat", "", 0});
            } else if(i == 2005){
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
     * For KennedyHalfDollars
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_KennedyHalfDollarsUpgrade() {

        // Test Parameters
        CollectionInfo collection = new KennedyHalfDollars();
        String coinType = "Half-Dollars";
        String collectionName = coinType + " Upgrade";
        int startYear = 1964;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
            if (i == 1975){
                continue;
            }
            if(i == 1976){
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
        for(int i = startYear; i <= endYear; i++){
            if(i == 1883){
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
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
            if(i == 1909){
                coinList.add(new Object[]{"1909 V.D.B", "", 0});
                coinList.add(new Object[]{Integer.toString(i), "", 0});
            } else if(i == 2009){
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
        for(int i = startYear; i <= endYear; i++){
            if(i == 1922 || i == 1932 || i == 1933){
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
        for(int i = startYear; i <= endYear; i++){
            if(i > 1904 && i < 1921){
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
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
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
        for(int i = startYear; i <= endYear; i++){
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
     * For RooseveltDimes
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_RooseveltDimesUpgrade() {

        // Test Parameters
        CollectionInfo collection = new RooseveltDimes();
        String coinType = "Dimes";
        String collectionName = coinType + " Upgrade";
        int startYear = 1946;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for(int i = startYear; i <= VERSION_1_YEAR; i++){
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
        for(int i = startYear; i <= endYear; i++){
            if (i == 1922) {
                continue;
            }
            if(i == 1917){
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
        HashMap<String, Object> parameters = new HashMap<>();
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
        for(int i = startYear; i <= endYear; i++) {
            if (i > 1981 && i < 1999){
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
        for(int i = startYear; i <= endYear; i++) {
            if(i == 1922 || (i >= 1924 && i <= 1926) || (i >= 1930 && i <= 1932)){
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
     * For WashingtonQuarters
     * - Test that the number of coins is correct upon collection upgrades
     */
    @Test
    public void test_WashingtonQuartersUpgrade() {

        // Test Parameters
        CollectionInfo collection = new WashingtonQuarters();
        String coinType = "Quarters";
        String collectionName = coinType + " Upgrade";
        int startYear = 1932;
        int endYear = 2021;

        // Create V1 database and run upgrade
        TestDatabaseHelper testDbHelper = new TestDatabaseHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        ArrayList<Object[]> coinList = new ArrayList<>();
        for(int i = startYear; i <= endYear; i++) {
            if(i == 1933 || i == 1975){
                continue;
            }
            if(i > 1998 && i < 2021) {
                continue;
            }
            if(i == 2021){
                coinList.add(new Object[]{"Crossing the Delaware", "", 0});
            } else if(i == 1976){
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
}