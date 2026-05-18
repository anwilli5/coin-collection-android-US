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

import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CollectionInfo;
import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.BasicDimes;
import com.spencerpages.collections.BasicQuarters;
import com.spencerpages.collections.Cartwheels;
import com.spencerpages.collections.CladQuarters;
import com.spencerpages.collections.CoinSets;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.SilverDimes;
import com.spencerpages.collections.SilverHalfDollars;
import com.spencerpages.collections.SilverQuarters;
import com.spencerpages.collections.SmallCents;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Targeted upgrade tests for collections with SemiQ (Semiquincentennial) coins.
 * These tests exercise specific non-default parameter configurations that catch
 * bugs in the V23→V24 database migration, particularly:
 *
 * 1. "With proofs" tests: Enable non-default proof/special mint marks and verify
 *    the upgrade adds the correct proof variants. Catches bugs where addFromYear
 *    is used instead of custom logic (addFromYear only supports P/D).
 *
 * 2. "With mint marks" tests: For year-based collections, create a V23 database
 *    with OPT_STOP_YEAR=2025 (excluding 2026 SemiQ coins) and all mint marks
 *    enabled. Validates the upgrade adds only the correct mint marks for SemiQ
 *    coins. Catches bugs where addFromArrayList adds all enabled mint flags.
 */
@RunWith(RobolectricTestRunner.class)
public class CollectionUpgradeSemiQParamTests extends BaseTestCase {

    // -----------------------------------------------------------------------
    // "With proofs" tests — enable non-default proof mint marks
    // -----------------------------------------------------------------------

    @Test
    public void test_AllNickelsUpgradeWithSProof() {
        CollectionInfo collection = new AllNickels();
        String coinType = "All Nickels";
        String collectionName = coinType + " Upgrade SProof";

        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE); // S Proof

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_RooseveltDimesUpgradeWithProofs() {
        CollectionInfo collection = new RooseveltDimes();
        String coinType = "Roosevelt Dimes";
        String collectionName = coinType + " Upgrade Proofs";

        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.TRUE); // S Proof
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_7, Boolean.TRUE); // S Silver Proof

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_KennedyHalfDollarsUpgradeWithProofs() {
        CollectionInfo collection = new KennedyHalfDollars();
        String coinType = "Kennedy Half Dollars";
        String collectionName = coinType + " Upgrade Proofs";

        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_4, Boolean.TRUE); // S Proof
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.TRUE); // S Silver Proof

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_SilverHalfDollarsUpgradeWithProofs() {
        CollectionInfo collection = new SilverHalfDollars();
        String coinType = "Silver Half Dollars";
        String collectionName = coinType + " Upgrade Proofs";

        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.TRUE); // Franklin Proof
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.TRUE); // Silver Proof

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_SmallCentsUpgradeWithProofs() {
        CollectionInfo collection = new SmallCents();
        String coinType = "Small Cents";
        String collectionName = coinType + " Upgrade Proofs";

        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_5, Boolean.TRUE); // Memorial Proof
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.TRUE); // W

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_CladQuartersUpgradeWithSProof() {
        CollectionInfo collection = new CladQuarters();
        String coinType = "Clad Quarters";
        String collectionName = coinType + " Upgrade SProof";

        ParcelableHashMap parameters = new ParcelableHashMap();
        collection.getCreationParameters(parameters);
        parameters.put(CoinPageCreator.OPT_SHOW_MINT_MARK_6, Boolean.TRUE); // S Proof

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_CartwheelsUpgradeAllEnabled() {
        CollectionInfo collection = new Cartwheels();
        String coinType = "Cartwheels";
        String collectionName = coinType + " Upgrade AllEnabled";

        ParcelableHashMap parameters = getAllEnabledParams(collection);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_SilverDimesUpgradeAllEnabled() {
        CollectionInfo collection = new SilverDimes();
        String coinType = "Silver Dimes";
        String collectionName = coinType + " Upgrade AllEnabled";

        ParcelableHashMap parameters = getAllEnabledParams(collection);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    @Test
    public void test_SilverQuartersUpgradeAllEnabled() {
        CollectionInfo collection = new SilverQuarters();
        String coinType = "Silver Quarters";
        String collectionName = coinType + " Upgrade AllEnabled";

        ParcelableHashMap parameters = getAllEnabledParams(collection);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, "2026", parameters);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, parameters);
    }

    // -----------------------------------------------------------------------
    // "With mint marks" tests — constrained year range with mint marks enabled
    // -----------------------------------------------------------------------

    @Test
    public void test_AllNickelsUpgradeWithMintMarks() {
        CollectionInfo collection = new AllNickels();
        String coinType = "All Nickels";
        String collectionName = coinType + " Upgrade MintMarks";

        ParcelableHashMap createParams = getAllEnabledParams(collection);
        createParams.put(CoinPageCreator.OPT_STOP_YEAR, 2025);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, createParams);
        db.close();
        testDbHelper.close();

        ParcelableHashMap validateParams = getAllEnabledParams(collection);
        validateUpdatedDbWithParams(collection, collectionName, validateParams);
    }

    @Test
    public void test_RooseveltDimesUpgradeWithMintMarks() {
        CollectionInfo collection = new RooseveltDimes();
        String coinType = "Roosevelt Dimes";
        String collectionName = coinType + " Upgrade MintMarks";

        ParcelableHashMap createParams = getAllEnabledParams(collection);
        createParams.put(CoinPageCreator.OPT_STOP_YEAR, 2025);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, createParams);
        db.close();
        testDbHelper.close();

        ParcelableHashMap validateParams = getAllEnabledParams(collection);
        validateUpdatedDbWithParams(collection, collectionName, validateParams);
    }

    @Test
    public void test_KennedyHalfDollarsUpgradeWithMintMarks() {
        CollectionInfo collection = new KennedyHalfDollars();
        String coinType = "Kennedy Half Dollars";
        String collectionName = coinType + " Upgrade MintMarks";

        ParcelableHashMap createParams = getAllEnabledParams(collection);
        createParams.put(CoinPageCreator.OPT_STOP_YEAR, 2025);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, createParams);
        db.close();
        testDbHelper.close();

        ParcelableHashMap validateParams = getAllEnabledParams(collection);
        validateUpdatedDbWithParams(collection, collectionName, validateParams);
    }

    @Test
    public void test_SmallCentsUpgradeWithMintMarks() {
        CollectionInfo collection = new SmallCents();
        String coinType = "Small Cents";
        String collectionName = coinType + " Upgrade MintMarks";

        ParcelableHashMap createParams = getAllEnabledParams(collection);
        createParams.put(CoinPageCreator.OPT_STOP_YEAR, 2025);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, createParams);
        db.close();
        testDbHelper.close();

        ParcelableHashMap validateParams = getAllEnabledParams(collection);
        validateUpdatedDbWithParams(collection, collectionName, validateParams);
    }

    @Test
    public void test_CartwheelsUpgradeWithMintMarks() {
        CollectionInfo collection = new Cartwheels();
        String coinType = "Cartwheels";
        String collectionName = coinType + " Upgrade MintMarks";

        ParcelableHashMap createParams = getAllEnabledParams(collection);
        createParams.put(CoinPageCreator.OPT_STOP_YEAR, 2025);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, createParams);
        db.close();
        testDbHelper.close();

        ParcelableHashMap validateParams = getAllEnabledParams(collection);
        validateUpdatedDbWithParams(collection, collectionName, validateParams);
    }

    @Test
    public void test_BasicQuartersUpgradeWithMintMarks() {
        CollectionInfo collection = new BasicQuarters();
        String coinType = "Quarters";
        String collectionName = coinType + " Upgrade MintMarks";

        ParcelableHashMap createParams = new ParcelableHashMap();
        collection.getCreationParameters(createParams);
        createParams.put(CoinPageCreator.OPT_STOP_YEAR, 2025);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, createParams);
        db.close();
        testDbHelper.close();

        ParcelableHashMap validateParams = new ParcelableHashMap();
        collection.getCreationParameters(validateParams);
        validateUpdatedDbWithParams(collection, collectionName, validateParams);
    }

    // -----------------------------------------------------------------------
    // "Early stop year" tests — collection ends well before latest year,
    // upgrade should NOT add any new coins
    // -----------------------------------------------------------------------

    @Test
    public void test_AllNickelsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new AllNickels();
        String coinType = "All Nickels";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_RooseveltDimesUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new RooseveltDimes();
        String coinType = "Roosevelt Dimes";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_KennedyHalfDollarsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new KennedyHalfDollars();
        String coinType = "Kennedy Half Dollars";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_SmallCentsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new SmallCents();
        String coinType = "Small Cents";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_CartwheelsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new Cartwheels();
        String coinType = "Cartwheels";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_SilverDimesUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new SilverDimes();
        String coinType = "Silver Dimes";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_SilverHalfDollarsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new SilverHalfDollars();
        String coinType = "Silver Half Dollars";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_CoinSetsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new CoinSets();
        String coinType = "Coin Sets";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_BasicQuartersUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new BasicQuarters();
        String coinType = "Quarters";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    // Control tests — addFromYear-based collections (should already handle early stop year)

    @Test
    public void test_LincolnCentsUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new LincolnCents();
        String coinType = "Pennies";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }

    @Test
    public void test_BasicDimesUpgradeWithEarlyStopYear() {
        CollectionInfo collection = new BasicDimes();
        String coinType = "Dimes";
        String collectionName = coinType + " Upgrade EarlyStop";

        ParcelableHashMap params = new ParcelableHashMap();
        collection.getCreationParameters(params);
        params.put(CoinPageCreator.OPT_STOP_YEAR, 2020);

        TestDatabaseHelperV23 testDbHelper = new TestDatabaseHelperV23(
                ApplicationProvider.getApplicationContext());
        SQLiteDatabase db = testDbHelper.getWritableDatabase();
        createV23FromPopulateWithParams(db, collection, coinType, collectionName, null, params);
        db.close();
        testDbHelper.close();

        validateUpdatedDbWithParams(collection, collectionName, params);
    }
}
