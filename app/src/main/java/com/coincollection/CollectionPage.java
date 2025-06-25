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

import static com.coincollection.CoinPageCreator.getCollectionOrCoinNameFilter;
import static com.spencerpages.MainApplication.APP_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;

/**
 * Activity for managing each collection page
 * <p>
 * http://developer.android.com/resources/tutorials/views/hello-gridview.html
 */
public class CollectionPage extends BaseActivity {
    private String mCollectionName;
    public ArrayList<CoinSlot> mCoinList;
    private CoinSlotAdapter mCoinSlotAdapter;
    private int mCollectionTypeIndex;

    // Saved Instance State Keywords

    // Intent Argument Keywords
    public final static String COLLECTION_NAME = "Collection_Name";
    public final static String COLLECTION_TYPE_INDEX = "Collection_Type_Index";
    private final static String VIEW_INDEX = "view_index";
    private final static String VIEW_POSITION = "view_position";
    private final static String COIN_LIST = "coin_list";

    // Global "enum" values
    public static final int SIMPLE_DISPLAY = 0;
    public static final int ADVANCED_DISPLAY = 1;

    private int mDisplayType = SIMPLE_DISPLAY;

    // Coin filter states
    public static final int FILTER_SHOW_ALL = 0;
    public static final int FILTER_SHOW_COLLECTED = 1;
    public static final int FILTER_SHOW_MISSING = 2;

    public int mCoinFilter = FILTER_SHOW_ALL;
    public ArrayList<CoinSlot> mOriginalCoinList;
    private ArrayList<CoinSlot> mFilteredCoinList;

    // Action menu items
    private final static int NUM_ACTIONS = 4;
    private final static int ACTIONS_TOGGLE = 0;
    private final static int ACTIONS_EDIT = 1;
    private final static int ACTIONS_COPY = 2;
    private final static int ACTIONS_DELETE = 3;

    /* Used in conjunction with the ListView in the advance view case to scroll the view to the last
     * location.  Defaults to the first item, and will be set by:
     *     1 The index and position saved in the Intent that started us
     *         - Used to pass data when switching from the simple view to the advanced view
     *     2 The index and position saved in the mSavedInstanceState
     *         - Used to pass data when the screen rotates
     *
     * Info in the mSavedInstanceState will overwrite the info from the Intent (so that if the user
     * switched from simple into advanced and then rotated the screen, where they were when they
     * rotated the screen will display
     */
    private int mViewIndex = 0;
    private int mViewPosition = 0;

    public static final String IS_LOCKED = "_isLocked";
    public static final String COIN_FILTER = "_coinFilter";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save off this bundle so that after the database is open we can use it
        // to get the previous CoinSlotAdapter, if present

        // Need to get the coin type from the intent that started this process
        mCollectionTypeIndex = mCallingIntent.getIntExtra(COLLECTION_TYPE_INDEX, 0);
        CollectionInfo collectionTypeObj = MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];

        // Capture the collection name from the saved instance state if it's there,
        // otherwise capture from the calling intent. Note that the calling intent
        // is updated with any renames before re-creating the view.
        if (savedInstanceState != null) {
            mCollectionName = savedInstanceState.getString(COLLECTION_NAME);
        } else {
            mCollectionName = mCallingIntent.getStringExtra(COLLECTION_NAME);
        }

        // Restore the view index and position
        if (savedInstanceState != null) {
            mViewIndex = savedInstanceState.getInt(VIEW_INDEX);
            mViewPosition = savedInstanceState.getInt(VIEW_POSITION);
        } else if (mCallingIntent.hasExtra(VIEW_INDEX)) {
            mViewIndex = mCallingIntent.getIntExtra(VIEW_INDEX, 0);
            mViewPosition = mCallingIntent.getIntExtra(VIEW_POSITION, 0);
        }

        // Update the title
        // http://stackoverflow.com/questions/2198410/how-to-change-title-of-activity-in-android
        this.setTitle(mCollectionName);

        // Tell the user they can now lock the collections
        // Check whether it is the users first time using the app
        boolean displayedHelp = createAndShowHelpDialog("first_Time_screen3", R.string.tutorial_add_to_and_lock_collection);

        // Tell the user they can edit/copy coins now
        if (!displayedHelp) {
            createAndShowHelpDialog("first_Time_screen5", R.string.tutorial_edit_copy_delete_coins);
        }

        // Determine whether we should show the advanced view or the basic view
        mDisplayType = mDbAdapter.fetchTableDisplay(mCollectionName);

        // Update the icon
        if (mActionBar != null) {
            mActionBar.setIcon(collectionTypeObj.getCoinImageIdentifier());
            // Set the actionbar so that clicking the icon takes you back
            // SO 1010877
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        GridView gridview = null;
        ListView listview = null;

        if (mDisplayType == SIMPLE_DISPLAY) {

            setContentView(R.layout.standard_collection_page);
            gridview = findViewById(R.id.standard_collection_page);

        } else if (mDisplayType == ADVANCED_DISPLAY) {

            setContentView(R.layout.advanced_collection_page);
            listview = findViewById(R.id.advanced_collection_page);

            // Make it so that the elements in the listview cells can get focus
            listview.setItemsCanFocus(true);
        }

        // Populate the coin list
        if (savedInstanceState == null) {
            boolean populateAdvInfo = (mDisplayType == ADVANCED_DISPLAY);
            mCoinList = mDbAdapter.getCoinList(mCollectionName, populateAdvInfo);
        } else {

            // We have already loaded the advanced lists, so use those instead.
            // That way we have all of the state from before the page loaded.
            // yay
            if (BuildConfig.DEBUG) {
                Log.d(APP_NAME, "Successfully restored previous state");
            }
            mCoinList = savedInstanceState.getParcelableArrayList(COIN_LIST);
            // Search through the hasChanged history and see whether we should
            // re-display the "Unsaved Changes" view
            if (mCoinList != null) {
                for (int i = 0; i < mCoinList.size(); i++) {
                    if (mCoinList.get(i).hasAdvInfoChanged()) {
                        this.showUnsavedTextView();
                        break;
                    }
                }
            }
        }

        // Initialize coin filter state
        SharedPreferences filterPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        mCoinFilter = filterPreferences.getInt(mCollectionName + COIN_FILTER, FILTER_SHOW_ALL);
        
        // Create adapter with original coin list - it will handle filtering internally
        mCoinSlotAdapter = new CoinSlotAdapter(this, mCollectionName, collectionTypeObj, mCoinList, mDisplayType);
        mCoinSlotAdapter.setFilter(mCoinFilter);
        
        // Update mCoinList to reference the adapter's filtered list for compatibility
        // Store original list reference for tests and other compatibility
        mOriginalCoinList = mCoinSlotAdapter.getOriginalCoinList();
        mCoinList = mCoinSlotAdapter.getFilteredCoinList();

        OnScrollListener scrollListener = new OnScrollListener() {
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Auto-generated method stub
                // TODO Something we can put here that isn't slow but fixes the scrolling issue
                // Anything we put here is going to be hit a lot :(
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    // Refresh the view, fixing any layout issues
                    mCoinSlotAdapter.notifyDataSetChanged();
                }

                // If this is the advanced view, we want to hide the soft keyboard if it exists
                // This only gets called when a scroll starts (SCROLL_STATE_TOUCH_SCROLL),
                // when the person has flung the view (SCROLL_STATE_FLING), and when the
                // scrolling comes to an end (SCROLL_STATE_IDLE), so this won't cause any performance
                // issues
                // TODO Is there an easy way to determine if the soft keyboard is shown?
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        };

        if (mDisplayType == SIMPLE_DISPLAY) {

            // Apply the adapter to handle each entry in the grid
            gridview.setAdapter(mCoinSlotAdapter);

            // Set the scroll listener so that the view re-adjusts to the new view
            gridview.setOnScrollListener(scrollListener);

            // Set the onClick listener that will handle changing the coin state
            gridview.setOnItemClickListener((parent, v, position, id) -> toggleCoinSlotInCollection(mCoinList.get(position)));

            // Add long-press handler for additional actions
            gridview.setOnItemLongClickListener((parent, view, position, id) -> {
                promptCoinSlotActions(position);
                return true;
            });

        } else if (mDisplayType == ADVANCED_DISPLAY) {
            // Apply the adapter to handle each entry in the list
            listview.setAdapter(mCoinSlotAdapter);

            // Set the scroll listener so that the view re-adjusts to the new view
            listview.setOnScrollListener(scrollListener);

            // Set the onClick listener for the whole view to provide a notice
            // to users if the collection is locked. There's also a onClick listener
            // on the imageView in CoinSlotAdapter
            listview.setOnItemClickListener((parent, v, position, id) -> {
                // Need to check whether the collection is locked
                SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
                if (mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)) {
                    // Collection is locked
                    showLockedMessage();
                }
            });

            // Add long-press handler for additional actions
            listview.setOnItemLongClickListener((parent, view, position, id) -> {
                promptCoinSlotActions(position);
                return true;
            });
        }

        // Setup filter status indicator
        setupFilterStatusIndicator();

        // Scroll to the last position viewed (if saved)
        scrollToIndex(mViewIndex, mViewPosition, false);
    }

    /**
     * Report unsaved changes to the user
     */
    public void showUnsavedTextView() {

        TextView unsavedMessageView = findViewById(R.id.unsaved_message_textview);
        unsavedMessageView.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the unsaved changes view
     */
    private void hideUnsavedTextView() {

        TextView unsavedMessageView = findViewById(R.id.unsaved_message_textview);
        unsavedMessageView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.collection_page_menu_all, menu);

        // Need to check the preferences to see whether the collection is locked or unlocked
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        MenuItem item = menu.findItem(R.id.lock_unlock_collection);

        if (mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)) {
            // Current Locked, set text to unlock it
            item.setTitle(R.string.unlock_collection);
        } else {
            // Currently unlocked, set text to lock it
            // Default is unlocked
            if (mDisplayType == ADVANCED_DISPLAY) {
                item.setTitle(R.string.lock_collection_adv);
            } else {
                item.setTitle(R.string.lock_collection);
            }
        }

        // If we are in the advanced mode, we need to show the save and switch view
        MenuItem changeViewItem = menu.findItem(R.id.change_view);

        if (mDisplayType == ADVANCED_DISPLAY) {
            changeViewItem.setTitle(R.string.simple_view_string);
            //saveItem.setVisible(true);
        } else {
            changeViewItem.setTitle(R.string.advanced_view_string);
            //saveItem.setVisible(false);
        }

        // Update the filter button text to show current filter state
        MenuItem filterItem = menu.findItem(R.id.toggle_coin_filter);
        switch (mCoinFilter) {
            case FILTER_SHOW_ALL:
                filterItem.setTitle(R.string.show_all_coins);
                break;
            case FILTER_SHOW_COLLECTED:
                filterItem.setTitle(R.string.show_collected_coins);
                break;
            case FILTER_SHOW_MISSING:
                filterItem.setTitle(R.string.show_missing_coins);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int itemId = item.getItemId();
        if (itemId == R.id.lock_unlock_collection) {
            // Need to check the preferences to see whether the collection is locked or unlocked
            SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = mainPreferences.edit();

            boolean isLocked = mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false);
            boolean finishedSuccessfully = true;

            // If we are going from unlocked to lock in advance mode, we need to save the
            // changes the user may have made (if any)
            if (mDisplayType == ADVANCED_DISPLAY &&
                    !isLocked &&
                    this.doUnsavedChangesExist()) {

                // In the advanced display case, we also need to save

                // TODO Show some kind of spinner

                for (int i = 0; i < mCoinList.size(); i++) {
                    CoinSlot coinSlot = mCoinList.get(i);
                    if (coinSlot.hasAdvInfoChanged()) {
                        try {
                            mDbAdapter.updateAdvInfo(mCollectionName, coinSlot);
                        } catch (SQLException e) {
                            finishedSuccessfully = false;
                            // Keep going, though
                            continue;
                        }
                        // Mark this data as being unchanged
                        coinSlot.setAdvInfoChanged(false);
                    }
                }

                if (finishedSuccessfully) {
                    // Hide the unsaved changes view
                    Toast.makeText(this, mRes.getString(R.string.changes_saved), Toast.LENGTH_SHORT).show();
                    this.hideUnsavedTextView();
                } else {
                    showCancelableAlert(mRes.getString(R.string.error_updating_database));
                }
            }

            if (finishedSuccessfully) {
                if (isLocked) {
                    // Locked, change to unlocked
                    editor.putBoolean(mCollectionName + IS_LOCKED, false);
                    // Change the text for next time
                    if (mDisplayType == SIMPLE_DISPLAY) {
                        item.setTitle(R.string.lock_collection);
                    }
                    // Don't update in the advance case, because we are going to blow
                    // away this
                } else {
                    // Unlocked or preference doesn't exist, change preference to locked
                    editor.putBoolean(mCollectionName + IS_LOCKED, true);
                    // Change the text for next time
                    if (mDisplayType == SIMPLE_DISPLAY) {
                        item.setTitle(R.string.unlock_collection);
                    }
                }
            }

            // Save changes
            // TODO Consider not saving these if in advance mode and the db update
            // fails below
            editor.apply();

            if (mDisplayType == ADVANCED_DISPLAY) {
                // We need to restart the view so we can show the locked
                // view.  Also, at this point there are no unsaved changes

                // Save the position that the user was at for convenience
                ListView listview = findViewById(R.id.advanced_collection_page);
                Integer[] viewPos = getAbsListViewPosition(listview);

                mCallingIntent.putExtra(VIEW_INDEX, viewPos[0]);
                mCallingIntent.putExtra(VIEW_POSITION, viewPos[1]);
                mCallingIntent.putExtra(COLLECTION_NAME, mCollectionName);

                finish();
                startActivity(mCallingIntent);
            }

            return true;
        } else if (itemId == R.id.change_view) {
            if (mDisplayType == SIMPLE_DISPLAY) {
                // Setup the advanced view
                try {
                    mDbAdapter.updateTableDisplay(mCollectionName, ADVANCED_DISPLAY);
                } catch (SQLException e) {
                    showCancelableAlert(mRes.getString(R.string.error_updating_database));
                }

                // Save the position that the user was at for convenience
                GridView gridview = findViewById(R.id.standard_collection_page);
                Integer[] viewPos = getAbsListViewPosition(gridview);

                mCallingIntent.putExtra(VIEW_INDEX, viewPos[0]);
                mCallingIntent.putExtra(VIEW_POSITION, viewPos[1]);
                mCallingIntent.putExtra(COLLECTION_NAME, mCollectionName);

                // Restart the activity
                finish();
                startActivity(mCallingIntent);

                return true;

            } else if (mDisplayType == ADVANCED_DISPLAY) {
                // Setup the basic view

                // We need to see if there are any unsaved changes, and if so,
                // present an alert

                if (this.doUnsavedChangesExist()) {

                    showUnsavedChangesAlertViewChange();
                    return true;
                }

                // The user doesn't have any unsaved changes

                try {
                    mDbAdapter.updateTableDisplay(mCollectionName, SIMPLE_DISPLAY);
                } catch (SQLException e) {
                    showCancelableAlert(mRes.getString(R.string.error_updating_database));
                }

                // Save the position that the user was at for convenience
                ListView listview = findViewById(R.id.advanced_collection_page);
                Integer[] viewPos = getAbsListViewPosition(listview);

                mCallingIntent.putExtra(VIEW_INDEX, viewPos[0]);
                mCallingIntent.putExtra(VIEW_POSITION, viewPos[1]);
                mCallingIntent.putExtra(COLLECTION_NAME, mCollectionName);

                // Restart the activity
                finish();
                startActivity(mCallingIntent);
                return true;
            }

            // Shouldn't get here
            return true;
        } else if (itemId == R.id.rename_collection) {
            // Prompt user for new name via alert dialog
            showCollectionRenamePrompt();
            return true;
        } else if (itemId == android.R.id.home) {
            // To support having a back arrow on the page

            if (this.doUnsavedChangesExist()) {
                // If we have unsaved changes, don't go back right away but
                // instead let the user decide
                showUnsavedChangesAlertAndExitActivity();
            } else {
                this.onBackPressed();
            }
            return true;
        } else if (itemId == R.id.toggle_coin_filter) {
            // Show filter selection menu
            showFilterMenu();
            return true;
        } else if (itemId == R.id.add_coin_button) {
            // Show add coin prompt
            showCoinCreateOrRenamePrompt(0, true);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the collection name when the user renames a collection
     *
     * @param newCollectionName Name of the new collection
     */
    private void updateCollectionName(String newCollectionName) {

        String oldCollectionName = mCollectionName;

        // Do nothing if the name isn't actually changed
        if (newCollectionName.equals(oldCollectionName)) {
            return;
        }

        // Make sure the new name isn't taken and is valid
        int checkNameResult = mDbAdapter.checkCollectionName(newCollectionName);
        if (checkNameResult != -1) {
            Toast.makeText(this, mRes.getString(checkNameResult), Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform all actions needed to rename the collection

        // Update database
        try {
            mDbAdapter.updateCollectionName(oldCollectionName, newCollectionName);
        } catch (SQLException e) {
            showCancelableAlert(mRes.getString(R.string.error_updating_database));
        }

        // Update app state
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = mainPreferences.edit();
        boolean isLocked = mainPreferences.getBoolean(oldCollectionName + IS_LOCKED, false);
        editor.remove(oldCollectionName + IS_LOCKED);
        editor.putBoolean(newCollectionName + IS_LOCKED, isLocked);
        
        // Transfer coin filter preference
        int coinFilter = mainPreferences.getInt(oldCollectionName + COIN_FILTER, FILTER_SHOW_ALL);
        editor.remove(oldCollectionName + COIN_FILTER);
        editor.putInt(newCollectionName + COIN_FILTER, coinFilter);
        
        editor.apply();

        // Update current view
        mCollectionName = newCollectionName;
        mCoinSlotAdapter.setTableName(newCollectionName);
        this.setTitle(newCollectionName);
    }

    /**
     * Update the coin name/mint details
     *
     * @param coinSlot coin slot to update
     * @param coinName new name for the coin
     * @param coinMint new mint mark for the coin
     * @param imageId image id for the coin
     */
    public void updateCoinDetails(CoinSlot coinSlot, String coinName, String coinMint, int imageId) {

        // Do nothing if the name/mint isn't actually changed
        if (coinName.equals(coinSlot.getIdentifier()) && coinMint.equals(coinSlot.getMint())
            && (imageId == coinSlot.getImageId())) {
            return;
        }

        // Update the coin in the coin list
        try {
            coinSlot.setIdentifier(coinName);
            coinSlot.setMint(coinMint);
            coinSlot.setImageId(imageId);
            mDbAdapter.updateCoinNameMintImage(mCollectionName, coinSlot);
        } catch (SQLException e) {
            showCancelableAlert(mRes.getString(R.string.error_updating_coin));
            return;
        }

        // Update the view
        mCoinSlotAdapter.notifyDataSetChanged();
    }

    /**
     * Add the coin with name/mint
     *
     * @param newName  coin name
     * @param coinMint coin mint
     * @param imageId  coin image id
     */
    public void addNewCoin(String newName, String coinMint, int imageId) {
        int sortOrder = mDbAdapter.getNextCoinSortOrder(mCollectionName);
        CoinSlot newCoinSlot = new CoinSlot(newName, coinMint, sortOrder, imageId);
        try {
            // Insert the new coin into the database
            mDbAdapter.addCoinSlotToCollection(newCoinSlot, mCollectionName, true, mCoinSlotAdapter.getOriginalCoinList().size() + 1);
        } catch (SQLException e) {
            showCancelableAlert(mRes.getString(R.string.error_editing_coin));
            return;
        }
        // Insert the new coin and update the view
        mCoinSlotAdapter.getOriginalCoinList().add(newCoinSlot);
        mCoinSlotAdapter.setFilter(mCoinFilter); // Refresh filter to update filtered list
        mCoinList = mCoinSlotAdapter.getFilteredCoinList(); // Update reference
        updateFilterStatusIndicator(); // Update filter status counts
        scrollToIndex(mCoinList.size() - 1, 0, true);
    }

    /**
     * Get the position that the user was at for convenience
     * http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     *
     * @param view to capture position from
     */
    private static Integer[] getAbsListViewPosition(AbsListView view) {
        int index = view.getFirstVisiblePosition();
        View v = view.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        return new Integer[]{index, top};
    }

    /**
     * Prompts the user to rename the collection
     */
    private void showCollectionRenamePrompt() {
        // Create a text box for the new collection name
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        // Make a filter to block out bad characters
        InputFilter nameFilter = getCollectionOrCoinNameFilter();
        input.setFilters(new InputFilter[]{nameFilter});
        input.setText(mCollectionName);

        // Build the alert dialog
        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.select_collection_name))
                .setView(input)
                .setPositiveButton(mRes.getString(R.string.okay), (dialog, which) -> {
                    dialog.dismiss();
                    String newName = input.getText().toString();
                    if (newName.isEmpty()) {
                        Toast.makeText(CollectionPage.this, mRes.getString(R.string.dialog_enter_collection_name), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateCollectionName(newName);
                })
                .setNegativeButton(mRes.getString(R.string.cancel), (dialog, which) -> dialog.cancel()));
    }

    /**
     * @return true if a collection has unsaved changes (only possible in advanced view)
     */
    private boolean doUnsavedChangesExist() {

        if (mDisplayType == ADVANCED_DISPLAY) {
            // There are probably better ways to do this check, but this one is easy
            TextView unsavedChangesView = this.findViewById(R.id.unsaved_message_textview);
            return (unsavedChangesView.getVisibility() == View.VISIBLE);
        } else {
            // In the simple view, there will never be unsaved changes
            return false;
        }
    }

    @Override
    // http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // If the back key is pressed, we want to warn the user if there are unsaved changes

            if (doUnsavedChangesExist()) {
                showUnsavedChangesAlertAndExitActivity();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /* We have one problem, specifically with the advancedView, where all of the
     * state is stored inside lists in the instance.  Normally, on an orientation
     * change or something, onDestroy and then onCreate would be called, destroying
     * the CoinSlotAdapter instance and all the state with it.  To prevent this,
     * we want to save off this object and use it for the newly created view.
     * http://stackoverflow.com/questions/7088816/my-views-are-being-reset-on-orientation-change
     * http://stackoverflow.com/questions/4249897/how-to-send-objects-through-bundle
     */

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // In the advanced view, if we change orientation or something we need
        // to save off the lists storing the uncommitted changes of coin grades,
        // coin quantities, and coin notes.  This is pretty hacked together,
        // so fix sometime, maybe

        // Save off position of listview/gridview
        Integer[] viewPos;
        if (mDisplayType == ADVANCED_DISPLAY) {
            // Finally, save off the position of the listview
            ListView listview = findViewById(R.id.advanced_collection_page);
            viewPos = getAbsListViewPosition(listview);
        } else {
            GridView gridview = findViewById(R.id.standard_collection_page);
            viewPos = getAbsListViewPosition(gridview);
        }

        // Save off these lists that may have unsaved user data
        outState.putParcelableArrayList(COIN_LIST, mCoinList);
        outState.putInt(VIEW_INDEX, viewPos[0]);
        outState.putInt(VIEW_POSITION, viewPos[1]);
        outState.putString(COLLECTION_NAME, mCollectionName);
    }

    /**
     * Displays to the user that the collection is locked
     */
    private void showLockedMessage() {
        String text = mRes.getString(R.string.collection_locked);
        Toast toast = Toast.makeText(CollectionPage.this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Display message to save changes before performing an advanced action
     */
    private void showSaveChangesMessage() {
        String text = mRes.getString(R.string.save_changes_first);
        Toast toast = Toast.makeText(CollectionPage.this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Show an alert that changes aren't saved before changing views
     */
    public void showUnsavedChangesAlertViewChange() {
        showAlert(newBuilder()
                .setMessage(mRes.getString(R.string.dialog_unsaved_changes_change_views))
                .setCancelable(false)
                .setPositiveButton(mRes.getString(R.string.okay), (dialog, id) -> {
                    // Nothing to do, just a warning
                    dialog.dismiss();
                }));
    }

    /**
     * Show an alert that changes aren't saved before exiting activity
     */
    public void showUnsavedChangesAlertAndExitActivity() {
        showAlert(newBuilder()
                .setMessage(mRes.getString(R.string.dialog_unsaved_changes_exit))
                .setCancelable(false)
                .setPositiveButton(mRes.getString(R.string.okay), (dialog, id) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton(mRes.getString(R.string.cancel), (dialog, id) -> dialog.cancel()));
    }

    /**
     * Toggle whether a given coin slot is collected or not
     *
     * @param coinSlot the CoinSlot to update
     */
    private void toggleCoinSlotInCollection(CoinSlot coinSlot) {
        // Need to check whether the collection is locked
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);

        if (mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)) {
            // Collection is locked
            showLockedMessage();
        } else {
            // Save current scroll position before making changes
            Integer[] savedScrollPosition = null;
            if (mDisplayType == SIMPLE_DISPLAY) {
                GridView gridview = findViewById(R.id.standard_collection_page);
                savedScrollPosition = getAbsListViewPosition(gridview);
            } else {
                ListView listview = findViewById(R.id.advanced_collection_page);
                savedScrollPosition = getAbsListViewPosition(listview);
            }
            
            // Find the current position of the coin being toggled for smarter scroll restoration
            int coinPositionInCurrentList = mCoinSlotAdapter.getPositionInFilteredList(coinSlot);
            
            // Preference doesn't exist or Collection is unlocked
            try {
                mDbAdapter.toggleInCollection(mCollectionName, coinSlot);
            } catch (SQLException e) {
                showCancelableAlert(mRes.getString(R.string.error_updating_database));
            }

            // Update the coin's collection status
            boolean oldValue = coinSlot.isInCollection();
            coinSlot.setInCollection(!oldValue);
            
            // Since the adapter holds the original list, the change is automatically reflected
            // Just reapply the current filter to update the filtered view
            mCoinSlotAdapter.setFilter(mCoinFilter);
            // Update mCoinList reference for compatibility with existing code
            mCoinList = mCoinSlotAdapter.getFilteredCoinList();
            
            // Update filter status indicator
            updateFilterStatusIndicator();
            
            // Restore scroll position intelligently
            restoreScrollPositionAfterFilterChange(savedScrollPosition, coinSlot, coinPositionInCurrentList);
        }
    }

    /**
     * Makes a copy of the coin slot in the collection
     *
     * @param coinSlot the CoinSlot to copy
     */
    public void copyCoinSlot(CoinSlot coinSlot, int coinListInsertIndex) {
        // Need to check whether the collection is locked
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);

        if (mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)) {
            // Collection is locked
            showLockedMessage();
        } else {
            // Create the new coin slot
            // - copy() also sets the sort order to original + 1
            // - Mark as custom coin since it wasn't added when the collection was created
            CoinSlot newCoinSlot = coinSlot.copy(coinSlot.getIdentifier(), coinSlot.getMint(), true);
            try {
                // Update the sort order in the database and coin list
                mDbAdapter.updateCoinSortOrderForInsert(mCollectionName, newCoinSlot.getSortOrder());
                for (CoinSlot currCoinSlot : mCoinSlotAdapter.getOriginalCoinList()) {
                    if (currCoinSlot.getSortOrder() >= newCoinSlot.getSortOrder()) {
                        currCoinSlot.setSortOrder(currCoinSlot.getSortOrder() + 1);
                    }
                }

                // Insert the new coin into the database
                mDbAdapter.addCoinSlotToCollection(newCoinSlot, mCollectionName, true, mCoinSlotAdapter.getOriginalCoinList().size() + 1);
            } catch (SQLException e) {
                showCancelableAlert(mRes.getString(R.string.error_copying_coin));
                return;
            }

            // Insert the new coin and update the view
            mCoinSlotAdapter.getOriginalCoinList().add(coinListInsertIndex, newCoinSlot);
            mCoinSlotAdapter.setFilter(mCoinFilter); // Refresh filter to update filtered list
            mCoinList = mCoinSlotAdapter.getFilteredCoinList(); // Update reference
            updateFilterStatusIndicator(); // Update filter status counts
        }
    }

    /**
     * Deletes the coin slot in the collection at a given position
     *
     * @param position the CoinSlot index to delete
     */
    public void deleteCoinSlotAtPosition(int position) {
        // Need to check whether the collection is locked
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);

        if (mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)) {
            // Collection is locked
            showLockedMessage();
        } else {
            // Delete the coin from the coin list
            CoinSlot coinSlot = mCoinList.remove(position);
            // Also remove from the original list
            mCoinSlotAdapter.getOriginalCoinList().remove(coinSlot);
            try {
                mDbAdapter.removeCoinSlotFromCollection(coinSlot, mCollectionName, mCoinSlotAdapter.getOriginalCoinList().size());
            } catch (SQLException e) {
                showCancelableAlert(mRes.getString(R.string.error_delete_coin));
                return;
            }
            
            // Refresh the filter to update the filtered list
            mCoinSlotAdapter.setFilter(mCoinFilter);
            mCoinList = mCoinSlotAdapter.getFilteredCoinList(); // Update reference
            updateFilterStatusIndicator(); // Update filter status counts

            // Update the view
            mCoinSlotAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Prompts the user to create or rename a coin
     *
     * @param position      the CoinSlot index to update
     * @param createNewCoin if true, creates a new coin at the end of the list
     */
    private void showCoinCreateOrRenamePrompt(int position, boolean createNewCoin) {
        // Need to check whether the collection is locked
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);

        if (mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)) {
            // Collection is locked
            showLockedMessage();
        } else {
            // Get inputs and set default text
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout coinRenameView = (LinearLayout) inflater.inflate(R.layout.coin_update_layout, null);
            EditText nameInput = coinRenameView.findViewById(R.id.coin_name_edittext);
            EditText mintInput = coinRenameView.findViewById(R.id.coin_mint_edittext);
            Spinner imgSpinner = coinRenameView.findViewById(R.id.coin_image_select);
            LinearLayout imgRow = coinRenameView.findViewById(R.id.coin_image_row);

            if (!createNewCoin) {
                // Get coin slot at the position
                CoinSlot coinSlot = mCoinList.get(position);
                nameInput.setText(coinSlot.getIdentifier());
                mintInput.setText(coinSlot.getMint());
                setupCoinImageSpinner(coinSlot, imgSpinner, imgRow);
            } else {
                nameInput.setText("");
                mintInput.setText("");
                setupCoinImageSpinner(null, imgSpinner, imgRow);
            }

            // Set filters to block out bad characters
            InputFilter nameFilter = getCollectionOrCoinNameFilter();
            nameInput.setFilters(new InputFilter[]{nameFilter});
            mintInput.setFilters(new InputFilter[]{nameFilter});

            // Build the alert dialog
            showAlert(newBuilder()
                    .setTitle(mRes.getString(R.string.edit_coin_info))
                    .setView(coinRenameView)
                    .setPositiveButton(mRes.getString(R.string.okay), (dialog, which) -> {
                        dialog.dismiss();
                        String newName = nameInput.getText().toString();
                        int imageId = getSpinnerImageId(imgSpinner);
                        if (newName.isEmpty()) {
                            Toast.makeText(CollectionPage.this, mRes.getString(R.string.dialog_enter_coin_name), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!createNewCoin) {
                            updateCoinDetails(mCoinList.get(position), newName, mintInput.getText().toString(), imageId);
                        } else {
                            addNewCoin(newName, mintInput.getText().toString(), imageId);
                        }
                    })
                    .setNegativeButton(mRes.getString(R.string.cancel), (dialog, which) -> dialog.cancel()));
        }
    }

    /**
     * Get image id value from the coin image spinner
     *
     * @param imgSpinner coin image spinner
     * @return image id
     */
    private int getSpinnerImageId(Spinner imgSpinner) {
        if (imgSpinner.getVisibility() == View.VISIBLE) {
            // Get the selected image ID when the button is pressed
            int selectedPosition = imgSpinner.getSelectedItemPosition();
            if (selectedPosition == AdapterView.INVALID_POSITION) {
                return -1;
            } else {
                return selectedPosition-1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Set up the coin image spinner, if needed
     *
     * @param coinSlot coin slot being modified
     * @param imgSpinner coin image spinner
     */
    private void setupCoinImageSpinner(CoinSlot coinSlot, Spinner imgSpinner, LinearLayout imgRow) {
        CollectionInfo collectionTypeObj = MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
        Object[][] imageIdData = collectionTypeObj.getImageIds();

        if (imageIdData.length != 0) {
            imgSpinner.setVisibility(View.VISIBLE);
            imgRow.setVisibility(View.VISIBLE);

            // Ignore image id here to show the actual default for this name
            int defaultResId = (coinSlot != null) ? collectionTypeObj.getCoinSlotImage(coinSlot, true)
                    : collectionTypeObj.getCoinImageIdentifier();
            int defaultImageId = (coinSlot != null) ? coinSlot.getImageId() : -1;

            // Create lists to hold the names and resIds
            ArrayList<String> names = new ArrayList<>();
            ArrayList<Integer> resIds = new ArrayList<>();
            names.add(mContext.getString(R.string.img_default));
            resIds.add(defaultResId);
            for (Object[] entry : imageIdData) {
                names.add((String) entry[0]);
                resIds.add((Integer) entry[1]);
            }

            // Set up the image select spinner
            ImageSpinnerAdapter adapter = new ImageSpinnerAdapter(this, names, resIds);
            imgSpinner.setAdapter(adapter);

            // Set the selected position based on the current image id
            imgSpinner.setSelection(defaultImageId+1);
        } else {
            imgSpinner.setVisibility(View.GONE);
            imgRow.setVisibility(View.GONE);
        }
    }

    /**
     * Display additional actions list
     *
     * @param position the CoinSlot index to update
     */
    public void promptCoinSlotActions(int position) {

        // Populate a menu of actions for the collection
        CharSequence[] actionsList = new CharSequence[NUM_ACTIONS];
        actionsList[ACTIONS_TOGGLE] = mRes.getString(R.string.toggle_collected);
        actionsList[ACTIONS_EDIT] = mRes.getString(R.string.edit);
        actionsList[ACTIONS_COPY] = mRes.getString(R.string.copy);
        actionsList[ACTIONS_DELETE] = mRes.getString(R.string.delete);
        final int actionPosition = position;
        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.coin_actions))
                .setItems(actionsList, (dialog, item) -> {
                    // Clear the dialog after any option is pressed
                    dialog.dismiss();

                    // Currently if there are unsaved changes, block any of these actions from
                    // occurring, because the methods don't yet support delayed updating of
                    // the database (they take effect right away). A cleaner user experience
                    // would be for all of these changes to happen 'unsaved' (only to the
                    // data structure) and to committed to the DB when the save is performed.
                    if (doUnsavedChangesExist()) {
                        showSaveChangesMessage();
                        return;
                    }

                    switch (item) {
                        case ACTIONS_TOGGLE: {
                            // Toggle collected or not
                            toggleCoinSlotInCollection(mCoinList.get(actionPosition));
                            break;
                        }
                        case ACTIONS_EDIT: {
                            // Launch edit view
                            showCoinCreateOrRenamePrompt(actionPosition, false);
                            break;
                        }
                        case ACTIONS_COPY: {
                            // Perform copy
                            copyCoinSlot(mCoinList.get(actionPosition), actionPosition + 1);
                            break;
                        }
                        case ACTIONS_DELETE: {
                            // Perform delete
                            deleteCoinSlotAtPosition(actionPosition);
                            break;
                        }
                    }
                }));
    }

    /**
     * Sets the coin view to a specific index and position in the list
     *
     * @param index        view index to scroll to
     * @param position     position offset (only used for advanced view)
     * @param smoothScroll if true, does a smooth scroll to the position
     */
    private void scrollToIndex(int index, int position, boolean smoothScroll) {
        if (mDisplayType == SIMPLE_DISPLAY) {
            GridView gridview = findViewById(R.id.standard_collection_page);
            if (smoothScroll) {
                gridview.smoothScrollToPosition(index);
            } else {
                gridview.setSelection(index);
            }
        } else if (mDisplayType == ADVANCED_DISPLAY) {
            ListView listview = findViewById(R.id.advanced_collection_page);
            if (smoothScroll) {
                listview.smoothScrollToPosition(index);
            } else {
                listview.setSelectionFromTop(index, position);
            }
        }
    }

    /**
     * Apply the current coin filter to create a filtered list for display
     */
    public void applyCurrentFilter() {
        if (mCoinSlotAdapter != null) {
            mCoinSlotAdapter.setFilter(mCoinFilter);
            // Update mCoinList reference for compatibility with existing code
            mCoinList = mCoinSlotAdapter.getFilteredCoinList();
            // Update filter status indicator
            updateFilterStatusIndicator();
        }
    }

    /**
     * Setup the filter status indicator view and its click handler
     */
    private void setupFilterStatusIndicator() {
        TextView filterStatusView = findViewById(R.id.filter_status_indicator);
        if (filterStatusView != null) {
            filterStatusView.setOnClickListener(v -> showFilterMenu());
            updateFilterStatusIndicator();
        }
    }

    /**
     * Update the filter status indicator visibility and text based on current filter
     */
    private void updateFilterStatusIndicator() {
        TextView filterStatusView = findViewById(R.id.filter_status_indicator);
        if (filterStatusView == null || mCoinSlotAdapter == null) {
            return;
        }

        if (mCoinFilter == FILTER_SHOW_ALL) {
            // Hide the indicator when showing all coins
            filterStatusView.setVisibility(View.GONE);
        } else {
            // Show the indicator with appropriate text and counts
            filterStatusView.setVisibility(View.VISIBLE);
            
            int totalCoins = mCoinSlotAdapter.getOriginalCoinList().size();
            int filteredCoins = mCoinSlotAdapter.getFilteredCoinList().size();
            
            String statusText;
            if (mCoinFilter == FILTER_SHOW_COLLECTED) {
                statusText = mRes.getString(R.string.filter_status_collected, filteredCoins, totalCoins);
            } else { // FILTER_SHOW_MISSING
                statusText = mRes.getString(R.string.filter_status_missing, filteredCoins, totalCoins);
            }
            
            filterStatusView.setText(statusText);
        }
    }

    /**
     * Intelligently restore scroll position after a filter change or coin toggle
     * @param savedScrollPosition The original scroll position [index, top]
     * @param toggledCoin The coin that was toggled (null if this was just a filter change)
     * @param originalCoinPosition The position of the toggled coin in the previous list
     */
    private void restoreScrollPositionAfterFilterChange(Integer[] savedScrollPosition, CoinSlot toggledCoin, int originalCoinPosition) {
        if (savedScrollPosition == null || mCoinSlotAdapter == null || mCoinSlotAdapter.getCount() == 0) {
            return;
        }
        
        ArrayList<CoinSlot> filteredList = mCoinSlotAdapter.getFilteredCoinList();
        
        int targetIndex = 0;
        int targetTop = 0;
        
        // If a coin was toggled, try to find its new position or stay near where it was
        if (toggledCoin != null) {
            int newCoinPosition = mCoinSlotAdapter.getPositionInFilteredList(toggledCoin);
            if (newCoinPosition != -1) {
                // The coin is still visible, scroll to show it
                targetIndex = newCoinPosition;
                targetTop = savedScrollPosition[1]; // Try to maintain same top offset
            } else {
                // The coin is no longer visible due to filtering
                // Try to maintain relative position based on list size changes
                if (originalCoinPosition != -1 && savedScrollPosition[0] < filteredList.size()) {
                    // Use the original scroll position if it's still valid
                    targetIndex = Math.min(savedScrollPosition[0], filteredList.size() - 1);
                    targetTop = savedScrollPosition[1];
                } else {
                    // Calculate proportional position in the new list
                    ArrayList<CoinSlot> originalList = mCoinSlotAdapter.getOriginalCoinList();
                    int originalListSize = originalList.size();
                    if (originalListSize > 0) {
                        double relativePosition = (double) savedScrollPosition[0] / originalListSize;
                        targetIndex = Math.min((int) (relativePosition * filteredList.size()), filteredList.size() - 1);
                        targetTop = savedScrollPosition[1];
                    }
                }
            }
        } else {
            // No specific coin was toggled, try to maintain the original position
            targetIndex = Math.min(savedScrollPosition[0], mCoinList.size() - 1);
            targetTop = savedScrollPosition[1];
        }
        
        // Apply the scroll position
        scrollToIndex(targetIndex, targetTop, false);
    }

    /**
     * Show a menu to select the coin filter state
     */
    private void showFilterMenu() {
        String[] filterOptions = new String[3];
        filterOptions[FILTER_SHOW_ALL] = mRes.getString(R.string.show_all_coins);
        filterOptions[FILTER_SHOW_COLLECTED] = mRes.getString(R.string.show_collected_coins);
        filterOptions[FILTER_SHOW_MISSING] = mRes.getString(R.string.show_missing_coins);
        
        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.filter_dialog_title))
                .setItems(filterOptions, (dialog, selectedFilter) -> {
                    dialog.dismiss();
                    
                    // Only apply filter if it's different from current
                    if (selectedFilter != mCoinFilter) {
                        applyFilterState(selectedFilter);
                    }
                }));
    }
    
    /**
     * Apply the specified filter state and update the display
     */
    private void applyFilterState(int newFilter) {
        // Save current scroll position before making changes
        Integer[] savedScrollPosition = null;
        if (mDisplayType == SIMPLE_DISPLAY) {
            GridView gridview = findViewById(R.id.standard_collection_page);
            savedScrollPosition = getAbsListViewPosition(gridview);
        } else {
            ListView listview = findViewById(R.id.advanced_collection_page);
            savedScrollPosition = getAbsListViewPosition(listview);
        }
        
        // Set the new filter state
        mCoinFilter = newFilter;
        
        // Save the new filter state
        SharedPreferences filterPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = filterPreferences.edit();
        editor.putInt(mCollectionName + COIN_FILTER, mCoinFilter);
        editor.apply();
        
        // Apply the filter to the adapter
        if (mCoinSlotAdapter != null) {
            mCoinSlotAdapter.setFilter(mCoinFilter);
            // Update mCoinList reference for compatibility with existing code
            mCoinList = mCoinSlotAdapter.getFilteredCoinList();
        }
        
        // Update filter status indicator
        updateFilterStatusIndicator();
        
        // Restore scroll position after filter change
        restoreScrollPositionAfterFilterChange(savedScrollPosition, null, -1);
        
        // Update the menu to show the new filter state
        invalidateOptionsMenu();
    }
}
