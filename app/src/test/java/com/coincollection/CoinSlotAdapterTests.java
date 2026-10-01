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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Looper;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.spencerpages.BaseTestCase;
import com.spencerpages.MainApplication;
import com.spencerpages.R;
import com.spencerpages.collections.LincolnCents;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;

/**
 * Tests that {@link CoinSlotAdapter} binds each coin to its view and turns edits made in the
 * advanced view into unsaved coin changes. The rows used are the ones the page's own list lays
 * out, so the spinner and text-watcher callbacks fire the way they do on a device.
 */
@RunWith(RobolectricTestRunner.class)
public class CoinSlotAdapterTests extends BaseTestCase {

    private static final String COLLECTION_NAME = "Adapter Pennies";
    private static final int COLLECTION_TYPE_INDEX =
            MainApplication.getIndexFromCollectionNameStr(LincolnCents.COLLECTION_TYPE);

    /**
     * The test coins: a mix of collected and missing coins, with and without mints, and
     * with and without advanced-view details
     */
    private static ArrayList<CoinSlot> getTestCoins() {
        ArrayList<CoinSlot> coins = new ArrayList<>();
        coins.add(new CoinSlot(0, "1909", "", true, 3, 2, "First note", 0, false, -1));
        coins.add(new CoinSlot(0, "1909", "S", false, 0, 0, "", 1, false, -1));
        coins.add(new CoinSlot(0, "1910", "D", true, 1, 1, "Another note", 2, false, -1));
        coins.add(new CoinSlot(0, "1911", "", false, 0, 0, "", 3, false, -1));
        return coins;
    }

    /**
     * Creates the test collection with the given display type and lock state
     */
    private static void createCollection(int displayType, boolean locked) {
        createCollection(displayType, locked, getTestCoins());
    }

    /**
     * Creates the test collection with the given display type, lock state and coins
     */
    private static void createCollection(int displayType, boolean locked, ArrayList<CoinSlot> coins) {
        Context context = ApplicationProvider.getApplicationContext();
        int collected = 0;
        for (CoinSlot coin : coins) {
            collected += coin.isInCollection() ? 1 : 0;
        }
        CollectionListInfo info = new CollectionListInfo(COLLECTION_NAME, coins.size(), collected,
                COLLECTION_TYPE_INDEX, displayType, 1909, 1911, "0", "0");
        DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
        dbAdapter.open();
        dbAdapter.createAndPopulateNewTable(info, 0, coins);
        dbAdapter.close();
        context.getSharedPreferences(MainApplication.PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(COLLECTION_NAME + CollectionPage.IS_LOCKED, locked).commit();
    }

    private static Intent collectionPageIntent() {
        return new Intent(ApplicationProvider.getApplicationContext(), CollectionPage.class)
                .putExtra(CollectionPage.COLLECTION_TYPE_INDEX, COLLECTION_TYPE_INDEX)
                .putExtra(CollectionPage.COLLECTION_NAME, COLLECTION_NAME);
    }

    /**
     * Returns the laid-out row for a list position
     */
    private static View getRow(AbsListView listView, int position) {
        shadowOf(Looper.getMainLooper()).idle();
        View row = listView.getChildAt(position - listView.getFirstVisiblePosition());
        assertNotNull("Row " + position + " isn't laid out", row);
        return row;
    }

    /**
     * Checks the parts of a row that every display type shows
     */
    private static void assertBasicRow(CollectionPage activity, View row, CoinSlot coin) {
        Resources res = activity.getResources();
        String expectedText = coin.getMint().isEmpty() ? coin.getIdentifier()
                : res.getString(R.string.coin_text_template, coin.getIdentifier(), coin.getMint());
        assertEquals(expectedText, ((TextView) row.findViewById(R.id.coinText)).getText().toString());

        ImageView image = row.findViewById(R.id.coinImage);
        int expectedImage = MainApplication.COLLECTION_TYPES[COLLECTION_TYPE_INDEX].getCoinSlotImage(coin, false);
        assertEquals(expectedImage, shadowOf(image.getDrawable()).getCreatedFromResId());
        assertEquals(coin.isInCollection() ? 255 : 64, image.getImageAlpha());
        String collectedText = res.getString(coin.isInCollection() ? R.string.collected : R.string.missing);
        assertEquals(res.getString(R.string.coin_content_desc_template, coin.getIdentifier(), coin.getMint(),
                collectedText), image.getContentDescription().toString());
    }

    private static boolean isUnsavedIndicatorShown(CollectionPage activity) {
        View unsaved = activity.findViewById(R.id.unsaved_message_textview);
        return unsaved != null && unsaved.getVisibility() == View.VISIBLE;
    }

    @Test
    public void test_simpleViewBindsCollectedAndMissingCoins() {
        createCollection(CollectionPage.SIMPLE_DISPLAY, false);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                AbsListView gridView = activity.findViewById(R.id.standard_collection_page);
                assertSame(activity.mCoinSlotAdapter, gridView.getAdapter());
                assertEquals(getTestCoins().size(), activity.mCoinSlotAdapter.getCount());
                for (int i = 0; i < activity.mCoinList.size(); i++) {
                    View row = getRow(gridView, i);
                    assertBasicRow(activity, row, activity.mCoinList.get(i));
                    // The simple view has no advanced-view controls
                    assertEquals(null, row.findViewById(R.id.grade_selector));
                }
            });
        }
    }

    @Test
    public void test_advancedViewBindsDetails() {
        createCollection(CollectionPage.ADVANCED_DISPLAY, false);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                AbsListView listView = activity.findViewById(R.id.advanced_collection_page);
                for (int i = 0; i < activity.mCoinList.size(); i++) {
                    CoinSlot coin = activity.mCoinList.get(i);
                    View row = getRow(listView, i);
                    assertBasicRow(activity, row, coin);
                    assertEquals((int) coin.getAdvancedGrades(),
                            ((Spinner) row.findViewById(R.id.grade_selector)).getSelectedItemPosition());
                    assertEquals((int) coin.getAdvancedQuantities(),
                            ((Spinner) row.findViewById(R.id.quantity_selector)).getSelectedItemPosition());
                    assertEquals(coin.getAdvancedNotes(),
                            ((EditText) row.findViewById(R.id.notes_edit_text)).getText().toString());
                    assertFalse(coin.hasAdvInfoChanged());
                }
                // Binding the rows is not an edit
                assertFalse(isUnsavedIndicatorShown(activity));
            });
        }
    }

    @Test
    public void test_advancedViewEditsBecomeUnsavedChanges() {
        createCollection(CollectionPage.ADVANCED_DISPLAY, false);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                AbsListView listView = activity.findViewById(R.id.advanced_collection_page);

                // Grade
                CoinSlot gradeCoin = activity.mCoinList.get(0);
                ((Spinner) getRow(listView, 0).findViewById(R.id.grade_selector)).setSelection(5);
                shadowOf(Looper.getMainLooper()).idle();
                assertEquals(5, (int) gradeCoin.getAdvancedGrades());
                assertTrue(gradeCoin.hasAdvInfoChanged());
                assertTrue(isUnsavedIndicatorShown(activity));

                // Quantity
                CoinSlot quantityCoin = activity.mCoinList.get(1);
                ((Spinner) getRow(listView, 1).findViewById(R.id.quantity_selector)).setSelection(4);
                shadowOf(Looper.getMainLooper()).idle();
                assertEquals(4, (int) quantityCoin.getAdvancedQuantities());
                assertTrue(quantityCoin.hasAdvInfoChanged());

                // Notes
                CoinSlot notesCoin = activity.mCoinList.get(2);
                ((EditText) getRow(listView, 2).findViewById(R.id.notes_edit_text)).setText("Typed, with \"quotes\"");
                assertEquals("Typed, with \"quotes\"", notesCoin.getAdvancedNotes());
                assertTrue(notesCoin.hasAdvInfoChanged());

                // Tapping the image toggles whether the coin is collected
                CoinSlot toggledCoin = activity.mCoinList.get(3);
                assertFalse(toggledCoin.isInCollection());
                getRow(listView, 3).findViewById(R.id.coinImage).performClick();
                assertTrue(toggledCoin.isInCollection());
                assertTrue(toggledCoin.hasAdvInfoChanged());
                assertEquals(255, ((ImageView) getRow(listView, 3).findViewById(R.id.coinImage)).getImageAlpha());

                // None of it is written until the user saves
                ArrayList<CoinSlot> dbCoins = activity.mDbAdapter.getCoinList(COLLECTION_NAME, true);
                ArrayList<CoinSlot> original = getTestCoins();
                for (int i = 0; i < original.size(); i++) {
                    assertEquals(original.get(i).getAdvancedGrades(), dbCoins.get(i).getAdvancedGrades());
                    assertEquals(original.get(i).getAdvancedQuantities(), dbCoins.get(i).getAdvancedQuantities());
                    assertEquals(original.get(i).getAdvancedNotes(), dbCoins.get(i).getAdvancedNotes());
                    assertEquals(original.get(i).isInCollection(), dbCoins.get(i).isInCollection());
                }
            });
        }
    }

    /**
     * Coins whose grade/quantity indexes are outside the arrays, as a hand-edited or corrupt
     * backup can leave them (import doesn't range-check them)
     */
    private static ArrayList<CoinSlot> getOutOfRangeCoins() {
        ArrayList<CoinSlot> coins = new ArrayList<>();
        coins.add(new CoinSlot(0, "1909", "", false, 81, -1, "", 0, false, -1));
        coins.add(new CoinSlot(0, "1910", "", false, -1, 81, "", 1, false, -1));
        return coins;
    }

    @Test
    public void test_outOfRangeGradeAndQuantityShowAsUnset() {
        createCollection(CollectionPage.ADVANCED_DISPLAY, false, getOutOfRangeCoins());
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                AbsListView listView = activity.findViewById(R.id.advanced_collection_page);
                for (int i = 0; i < activity.mCoinList.size(); i++) {
                    CoinSlot coin = activity.mCoinList.get(i);
                    View row = getRow(listView, i);
                    assertEquals(0, ((Spinner) row.findViewById(R.id.grade_selector)).getSelectedItemPosition());
                    assertEquals(0, ((Spinner) row.findViewById(R.id.quantity_selector)).getSelectedItemPosition());
                    assertEquals(0, (int) coin.getAdvancedGrades());
                    assertEquals(0, (int) coin.getAdvancedQuantities());
                    // Showing the value as unset isn't an edit the user has to save
                    assertFalse(coin.hasAdvInfoChanged());
                }
                assertFalse(isUnsavedIndicatorShown(activity));
            });
        }
    }

    @Test
    public void test_outOfRangeGradeAndQuantityShowAsUnsetWhenLocked() {
        createCollection(CollectionPage.ADVANCED_DISPLAY, true, getOutOfRangeCoins());
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                Resources res = activity.getResources();
                String expectedGrade = res.getString(R.string.grade_text_view_template_without_grade,
                        res.getStringArray(R.array.coin_grades)[0]);
                String expectedQuantity = res.getString(R.string.quantities_text_view_template,
                        res.getStringArray(R.array.coin_quantities)[0]);
                AbsListView listView = activity.findViewById(R.id.advanced_collection_page);
                for (int i = 0; i < activity.mCoinList.size(); i++) {
                    View row = getRow(listView, i);
                    assertEquals(expectedGrade, ((TextView) row.findViewById(R.id.grade_textview)).getText().toString());
                    assertEquals(expectedQuantity, ((TextView) row.findViewById(R.id.quantity_textview)).getText().toString());
                }
            });
        }
    }

    /**
     * A row reused for another coin must show that coin, and rebinding must not count as
     * an edit to either coin (setText() on the notes field fires its TextWatcher)
     */
    @Test
    public void test_recycledAdvancedRowRebindsWithoutEdits() {
        createCollection(CollectionPage.ADVANCED_DISPLAY, false);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                AbsListView listView = activity.findViewById(R.id.advanced_collection_page);
                View row = getRow(listView, 0);
                CoinSlot firstCoin = activity.mCoinList.get(0);
                CoinSlot secondCoin = activity.mCoinList.get(2);

                View recycled = activity.mCoinSlotAdapter.getView(2, row, listView);
                assertSame(row, recycled);
                assertBasicRow(activity, recycled, secondCoin);
                assertEquals(secondCoin.getAdvancedNotes(),
                        ((EditText) recycled.findViewById(R.id.notes_edit_text)).getText().toString());
                assertEquals("First note", firstCoin.getAdvancedNotes());
                assertFalse(firstCoin.hasAdvInfoChanged());
                assertFalse(secondCoin.hasAdvInfoChanged());

                // Typing now edits the coin the row shows, not the one it showed before
                ((EditText) recycled.findViewById(R.id.notes_edit_text)).setText("Edited");
                assertEquals("Edited", secondCoin.getAdvancedNotes());
                assertEquals("First note", firstCoin.getAdvancedNotes());
            });
        }
    }

    @Test
    public void test_lockedAdvancedViewShowsDetailsReadOnly() {
        createCollection(CollectionPage.ADVANCED_DISPLAY, true);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                Resources res = activity.getResources();
                String[] grades = res.getStringArray(R.array.coin_grades);
                String[] quantities = res.getStringArray(R.array.coin_quantities);
                AbsListView listView = activity.findViewById(R.id.advanced_collection_page);

                CoinSlot coin = activity.mCoinList.get(0);
                View row = getRow(listView, 0);
                assertBasicRow(activity, row, coin);
                assertEquals(null, row.findViewById(R.id.grade_selector));
                assertEquals(res.getString(R.string.grade_text_view_template, grades[coin.getAdvancedGrades()]),
                        ((TextView) row.findViewById(R.id.grade_textview)).getText().toString());
                assertEquals(res.getString(R.string.quantities_text_view_template,
                                quantities[coin.getAdvancedQuantities()]),
                        ((TextView) row.findViewById(R.id.quantity_textview)).getText().toString());
                assertEquals(res.getString(R.string.notes_text_view_template, coin.getAdvancedNotes()),
                        ((TextView) row.findViewById(R.id.notes_textview)).getText().toString());

                // A coin without a grade shows the grade text on its own
                CoinSlot ungraded = activity.mCoinList.get(1);
                assertEquals(res.getString(R.string.grade_text_view_template_without_grade, grades[0]),
                        ((TextView) getRow(listView, 1).findViewById(R.id.grade_textview)).getText().toString());

                // Tapping the image explains the lock instead of toggling the coin
                getRow(listView, 1).findViewById(R.id.coinImage).performClick();
                assertFalse(ungraded.isInCollection());
                assertFalse(ungraded.hasAdvInfoChanged());
                assertEquals(res.getString(R.string.collection_locked), ShadowToast.getTextOfLatestToast());
            });
        }
    }

    @Test
    public void test_countTracksFilterAndSearch() {
        createCollection(CollectionPage.SIMPLE_DISPLAY, false);
        try (ActivityScenario<CollectionPage> scenario = ActivityScenario.launch(collectionPageIntent())) {
            scenario.onActivity(activity -> {
                CoinSlotAdapter adapter = activity.mCoinSlotAdapter;
                assertEquals(4, adapter.getCount());

                adapter.setFilter(CollectionPage.FILTER_SHOW_COLLECTED);
                assertEquals(2, adapter.getCount());
                for (CoinSlot coin : adapter.getFilteredCoinList()) {
                    assertTrue(coin.isInCollection());
                }

                adapter.setFilter(CollectionPage.FILTER_SHOW_MISSING);
                assertEquals(2, adapter.getCount());

                // Search matches the identifier or the mint, ignoring case, on top of the filter
                adapter.setSearchQuery("S");
                assertEquals(1, adapter.getCount());
                assertEquals("S", adapter.getFilteredCoinList().get(0).getMint());

                adapter.setFilter(CollectionPage.FILTER_SHOW_ALL);
                adapter.setSearchQuery("1909");
                assertEquals(2, adapter.getCount());

                adapter.setSearchQuery("");
                assertEquals(4, adapter.getCount());
                // Filtering never changes the underlying list
                assertEquals(4, adapter.getOriginalCoinList().size());
            });
        }
    }
}
