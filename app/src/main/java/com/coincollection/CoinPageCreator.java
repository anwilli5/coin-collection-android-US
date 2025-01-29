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
import static com.spencerpages.MainApplication.APP_NAME;

import android.content.Context;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.coincollection.helper.ParcelableHashMap;
import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity responsible for managing the collection creation page
 */
public class CoinPageCreator extends BaseActivity {

    public final static String EXISTING_COLLECTION_EXTRA = "existing-collection";

    /**
     * mCoinTypeIndex The index of the currently selected coin type in the
     * MainApplication.COLLECTION_TYPES list.
     */
    public int mCoinTypeIndex;

    /**
     * mCollectionObj The CollectionInfo object associated with this index.
     */
    public CollectionInfo mCollectionObj;

    /**
     * mParameters The HashMap that is used to keep track of the changes that
     * the user has requested (via the UI) to the default
     * collection settings.
     */
    public ParcelableHashMap mParameters;

    /**
     * mDefaults The default parameters provided from a call to the current
     * mCollectionObj getCreationParameters method.
     */
    private HashMap<String, Object> mDefaults;

    /**
     * mCoinList Upon selecting to create a collection, gets populated with coin identifiers
     * and mint marks
     */
    public ArrayList<CoinSlot> mCoinList = new ArrayList<>();

    /* Internal keys to use for passing data via saved instance state */
    private final static String _COIN_TYPE_INDEX = "CoinTypeIndex";
    private final static String _PARAMETERS = "Parameters";

    public CollectionListInfo mExistingCollection = null;

    /**
     * These are the options supported in the parameter HashMaps.  In general,
     * this is how they work:
     * - If an option is present in the parameters HashMap after the call to
     * getCreationParameters, the associated UI element will be displayed to
     * the user
     * - The UI element's default value will be set to the value specified in
     * the HashMap after a call to getCreationParameters, and the associated
     * value in the HashMap passed to populateCollectionLists will change
     * based on changes made to the UI element.
     * The specifics for the various options are as follows:
     * <p>
     * OPT_SHOW_MINT_MARKS
     * - Associated UI Element: 'Show Mint Marks' checkbox
     * - Associated Value Type: Boolean
     * - This option MUST be used in conjunction with at least one of the
     * specific show mint mark options. (Ex: OPT_SHOW_MINT_MARK_1)
     * <p>
     * OPT_SHOW_MINT_MARK_# (where # is a number between 1 and 5)
     * - Associated UI Element: Checkboxes that can be used for mint markers
     * - Associated Value Type: Boolean
     * - These checkboxes will get hidden and displayed depending on the 'Show Mint Marks'
     * checkbox.  The text associated with the checkbox must be specified via the
     * associated OPT_SHOW_MINT_MARK_#_STRING_ID.  There are currently five of these
     * that can be used per collection (if more are needed, minor changes to the core
     * code will be necessary)
     * - These options MUST be used in conjunction with OPT_SHOW_MINT_MARKS, and MUST
     * be accompanied by the respective OPT_SHOW_MINT_MARK_#_STRING_ID
     * <p>
     * OPT_SHOW_MINT_MARK_#_STRING_ID (where # is a number between 1 and 5)
     * - Associated UI Element: Special - see above
     * - Associated Value Type: Integer
     * - This option is special - it is used in conjunction with the option above
     * to indicate the resource ID associated with a String to display next
     * to the checkbox (Ex: R.string.show_p in the U.S. Coin Collection app)
     * <p>
     * OPT_EDIT_DATE_RANGE
     * - Associated UI Element: 'Edit Date Range' checkbox
     * - Associated Value Type: Boolean
     * - This option MUST be used in conjunction with OPT_START_YEAR and
     * OPT_STOP_YEAR
     * <p>
     * OPT_START_YEAR
     * - Associated UI Element: 'Edit Start Year' EditText
     * - Associated Value Type: Integer
     * - This option MUST be used in conjunction with OPT_EDIT_DATE_RANGE
     * <p>
     * OPT_STOP_YEAR
     * - Associated UI Element: 'Edit Stop Year' EditText
     * - Associated Value Type: Integer
     * - This option MUST be used in conjunction with OPT_EDIT_DATE_RANGE
     * <p>
     * OPT_CHECKBOX_# (where # is a number between 1 and 5)
     * - Associated UI Element: a standalone checkbox
     * - Associated Value Type: Boolean
     * - The text associated with the checkbox must be specified via the
     * associated OPT_SHOW_MINT_MARK_#_STRING_ID.  There are currently five
     * of these that can be used per collection (if more are needed, minor
     * changes to the core code will be necessary.)
     * - These options MUST be accompanied by the respective
     * OPT_CHECKBOX_#_STRING_ID
     * <p>
     * OPT_CHECKBOX_#_STRING_ID
     * - Associated UI Element: Special - see above
     * - Associated Value Type: Integer
     * - This option is special - it is used in conjunction with the option above
     * to indicate the resource ID associated with a String to display next
     * to the checkbox (Ex: R.string.show_territories in the U.S. Coin
     * Collection app)
     */

    public final static String OPT_SHOW_MINT_MARKS = "ShowMintMarks";
    public final static String OPT_EDIT_DATE_RANGE = "EditDateRange";
    public final static String OPT_START_YEAR = "StartYear";
    public final static String OPT_STOP_YEAR = "StopYear";

    public final static String OPT_SHOW_MINT_MARK_1 = "ShowMintMark1";
    public final static String OPT_SHOW_MINT_MARK_2 = "ShowMintMark2";
    public final static String OPT_SHOW_MINT_MARK_3 = "ShowMintMark3";
    public final static String OPT_SHOW_MINT_MARK_4 = "ShowMintMark4";
    public final static String OPT_SHOW_MINT_MARK_5 = "ShowMintMark5";
    public final static String OPT_SHOW_MINT_MARK_6 = "ShowMintMark6";
    public final static String OPT_SHOW_MINT_MARK_7 = "ShowMintMark7";
    public final static String OPT_SHOW_MINT_MARK_8 = "ShowMintMark8";
    public final static String OPT_SHOW_MINT_MARK_9 = "ShowMintMark9";
    public final static String OPT_SHOW_MINT_MARK_10 = "ShowMintMark10";
    public final static String OPT_SHOW_MINT_MARK_1_STRING_ID = "ShowMintMark1StringId";
    public final static String OPT_SHOW_MINT_MARK_2_STRING_ID = "ShowMintMark2StringId";
    public final static String OPT_SHOW_MINT_MARK_3_STRING_ID = "ShowMintMark3StringId";
    public final static String OPT_SHOW_MINT_MARK_4_STRING_ID = "ShowMintMark4StringId";
    public final static String OPT_SHOW_MINT_MARK_5_STRING_ID = "ShowMintMark5StringId";
    public final static String OPT_SHOW_MINT_MARK_6_STRING_ID = "ShowMintMark6StringId";
    public final static String OPT_SHOW_MINT_MARK_7_STRING_ID = "ShowMintMark7StringId";
    public final static String OPT_SHOW_MINT_MARK_8_STRING_ID = "ShowMintMark8StringId";
    public final static String OPT_SHOW_MINT_MARK_9_STRING_ID = "ShowMintMark9StringId";
    public final static String OPT_SHOW_MINT_MARK_10_STRING_ID = "ShowMintMark10StringId";

    public final static String OPT_CHECKBOX_1 = "ShowCheckbox1";
    public final static String OPT_CHECKBOX_2 = "ShowCheckbox2";
    private final static String OPT_CHECKBOX_3 = "ShowCheckbox3";
    private final static String OPT_CHECKBOX_4 = "ShowCheckbox4";
    private final static String OPT_CHECKBOX_5 = "ShowCheckbox5";
    private final static String OPT_CHECKBOX_6 = "ShowCheckbox6";
    private final static String OPT_CHECKBOX_7 = "ShowCheckbox7";
    private final static String OPT_CHECKBOX_8 = "ShowCheckbox8";
    private final static String OPT_CHECKBOX_9 = "ShowCheckbox9";
    private final static String OPT_CHECKBOX_10 = "ShowCheckbox10";
    public final static String OPT_CHECKBOX_1_STRING_ID = "ShowCheckbox1StringId";
    public final static String OPT_CHECKBOX_2_STRING_ID = "ShowCheckbox2StringId";
    private final static String OPT_CHECKBOX_3_STRING_ID = "ShowCheckbox3StringId";
    private final static String OPT_CHECKBOX_4_STRING_ID = "ShowCheckbox4StringId";
    private final static String OPT_CHECKBOX_5_STRING_ID = "ShowCheckbox5StringId";
    private final static String OPT_CHECKBOX_6_STRING_ID = "ShowCheckbox6StringId";
    private final static String OPT_CHECKBOX_7_STRING_ID = "ShowCheckbox7StringId";
    private final static String OPT_CHECKBOX_8_STRING_ID = "ShowCheckbox8StringId";
    private final static String OPT_CHECKBOX_9_STRING_ID = "ShowCheckbox9StringId";
    private final static String OPT_CHECKBOX_10_STRING_ID = "ShowCheckbox10StringId";

    /**
     * This flag should be used by collections whose year of most recent
     * production should track the current year.
     * <p>
     * TODO Make this easier to maintain, but make sure it doesn't break database
     *      upgrade functionality
     */
    public final static Integer OPTVAL_STILL_IN_PRODUCTION = 2024;


    private final static HashMap<String, String> SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP = new HashMap<>();

    static {
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_1, OPT_SHOW_MINT_MARK_1_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_2, OPT_SHOW_MINT_MARK_2_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_3, OPT_SHOW_MINT_MARK_3_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_4, OPT_SHOW_MINT_MARK_4_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_5, OPT_SHOW_MINT_MARK_5_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_6, OPT_SHOW_MINT_MARK_6_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_7, OPT_SHOW_MINT_MARK_7_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_8, OPT_SHOW_MINT_MARK_8_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_9, OPT_SHOW_MINT_MARK_9_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_10, OPT_SHOW_MINT_MARK_10_STRING_ID);
    }

    private final static HashMap<String, String> CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP = new HashMap<>();

    static {
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_1, OPT_CHECKBOX_1_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_2, OPT_CHECKBOX_2_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_3, OPT_CHECKBOX_3_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_4, OPT_CHECKBOX_4_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_5, OPT_CHECKBOX_5_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_6, OPT_CHECKBOX_6_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_7, OPT_CHECKBOX_7_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_8, OPT_CHECKBOX_8_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_9, OPT_CHECKBOX_9_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_10, OPT_CHECKBOX_10_STRING_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the actionbar so that clicking the icon takes you back (SO 1010877)
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.collection_creation_page);

        // Get the existing collection info if provided
        mExistingCollection = mCallingIntent.getParcelableExtra(EXISTING_COLLECTION_EXTRA);

        // Initialize our instance variables
        if (savedInstanceState != null) {
            // Screen rotated - Load the previous settings
            setInternalStateFromCollectionIndex(
                    savedInstanceState.getInt(_COIN_TYPE_INDEX),
                    savedInstanceState.getParcelable(_PARAMETERS));
        } else if (mExistingCollection != null) {
            // Updating collection - Setup the parameters based on the existing collection
            setInternalStateFromCollectionIndex(
                    mExistingCollection.getCollectionTypeIndex(),
                    getParametersFromCollectionListInfo(mExistingCollection));
        } else {
            // New collection - Setup default options
            setInternalStateFromCollectionIndex(0, null);
        }

        // Restore the progress dialog if the previous task was running
        if (mPreviousTask != null) {
            asyncProgressOnPreExecute();
        }

        // At this point the UI is ready to handle any async callbacks
        setActivityReadyForAsyncCallbacks();

        // Next, we will finish setting up the various UI elements (creating
        // adapters, listeners, etc..  We won't set any of the values yet -
        // we will do that at the end.

        // Prepare the Spinner that gets what type of collection they want to make
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (int i = 0; i < MainApplication.COLLECTION_TYPES.length; i++) {
            spinnerAdapter.add(MainApplication.COLLECTION_TYPES[i].getCoinType());
        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner coinTypeSelector = findViewById(R.id.coin_selector);
        coinTypeSelector.setAdapter(spinnerAdapter);
        coinTypeSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {

                // No need to do anything if onItemSelected was called but the selected index hasn't
                // changed since:
                //  - first activity initialization, or
                //  - activity initialization from SavedInstanceState
                if (mCoinTypeIndex == position) {
                    return;
                }

                // When an item is selected, switch our internal state based on the collection type
                setInternalStateFromCollectionIndex(position, null);

                // Reset the view for the new coin type
                updateViewFromState();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Create an OnKeyListener that can be used to hide the soft keyboard when the enter key
        // (or a few others) are pressed.
        //
        // TODO OnKeyListeners aren't guaranteed to work with software keyboards... find a better way
        // From https://developer.android.com/reference/android/view/View.OnKeyListener.html:
        // Interface definition for a callback to be invoked when a hardware key event is dispatched
        // to this view. The callback will be invoked before the key event is given to the view.
        // This is only useful for hardware keyboards; a software input method has no obligation to
        // trigger this listener.
        //
        // Has worked on all the devices I've tested on (which are all Samsung devices)
        OnKeyListener hideKeyboardListener = (v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                // This should hide the keyboard
                // Thanks! http://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                // Returning true prevents the action that would ordinarily have happened from taking place
                return keyCode == KeyEvent.KEYCODE_ENTER;
            }
            return false;
        };

        // Set the OnKeyListener for the EditText
        final EditText nameEditText = findViewById(R.id.edit_enter_collection_name);
        nameEditText.setOnKeyListener(hideKeyboardListener);
        // Make a filter to block out bad characters
        InputFilter nameFilter = getCollectionOrCoinNameFilter();
        nameEditText.setFilters(new InputFilter[]{nameFilter});
        // Set the name if editing an existing collection
        if (mExistingCollection != null && savedInstanceState == null) {
            nameEditText.setText(mExistingCollection.getName());
        }

        // Set the listener for the show mint mark checkbox
        final CheckBox showMintMarkCheckBox = findViewById(R.id.check_show_mint_mark);
        showMintMarkCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // Don't take any action if the value isn't changing - needed to prevent
            // loops that would get created by the call to updateViewFromState()
            Boolean optMintMarks = (Boolean) mParameters.get(OPT_SHOW_MINT_MARKS);
            if (optMintMarks != null && optMintMarks == isChecked) {
                return;
            }

            mParameters.put(OPT_SHOW_MINT_MARKS, isChecked);

            // Restore defaults for all of the mint mark checkboxes when this is unchecked
            if (!isChecked) {
                for (String key : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                    if (mParameters.containsKey(key)) {
                        mParameters.put(key, mDefaults.get(key));
                    }
                }
            }

            // Refresh the UI so that the individual mint mark checkboxes are either
            // hidden or displayed
            updateViewFromState();
        });

        // Set the listener for the edit date range
        final CheckBox editDateRangeCheckBox = findViewById(R.id.check_edit_date_range);
        editDateRangeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // Don't take any action if the value isn't changing - needed to prevent
            // loops that would get created by the call to updateViewFromState()
            Boolean optEditDateRange = (Boolean) mParameters.get(OPT_EDIT_DATE_RANGE);
            if (optEditDateRange != null && optEditDateRange == isChecked) {
                return;
            }

            mParameters.put(OPT_EDIT_DATE_RANGE, isChecked);

            // Reset the start/stop year when the field is unchecked
            if (!isChecked) {
                mParameters.put(OPT_START_YEAR, mDefaults.get(OPT_START_YEAR));
                mParameters.put(OPT_STOP_YEAR, mDefaults.get(OPT_STOP_YEAR));
            }

            // Refresh the UI so that the start/stop year EditTexts are hidden or displayed
            updateViewFromState();
        });

        // Instantiate an onCheckedChangeListener for use by all the simple checkboxes
        OnCheckedChangeListener checkboxChangeListener = (compoundButton, isChecked) -> {
            // The tag store the OPT_NAME associated with the button
            String optName = (String) compoundButton.getTag();
            mParameters.put(optName, isChecked);
        };

        // Create the ShowMintMark Checkboxes (even if they aren't needed right now)
        LinearLayout showMintMarksContainer = findViewById(R.id.show_mint_mark_checkbox_container);
        for (String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            // Instantiate a checkbox in the UI for this option
            CheckBox box = showMintMarksContainer.findViewWithTag(optName);
            box.setOnCheckedChangeListener(checkboxChangeListener);
        }

        // Add any stand-alone, customizable checkboxes
        LinearLayout customizableCheckboxContainer = findViewById(R.id.customizable_checkbox_container);
        for (String optName : CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            // Instantiate a checkbox in the UI for this option
            CheckBox box = customizableCheckboxContainer.findViewWithTag(optName);
            box.setOnCheckedChangeListener(checkboxChangeListener);
        }

        // Make a filter to block out non-numeric characters
        InputFilter digitFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (source.charAt(i) < '0' || source.charAt(i) > '9') {
                    // Don't allow these characters
                    return "";
                }
            }
            return null;
        };

        // Make a filter limiting the year text fields to 4 characters
        InputFilter yearLengthFilter = new InputFilter.LengthFilter(4);
        InputFilter[] yearEditTextFilters = new InputFilter[]{digitFilter, yearLengthFilter};

        // Set the OnKeyListener and InputFilters for the EditText
        final EditText startYearEditText = findViewById(R.id.edit_start_year);
        startYearEditText.setOnKeyListener(hideKeyboardListener);
        startYearEditText.setFilters(yearEditTextFilters);
        startYearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    mParameters.put(OPT_START_YEAR, Integer.valueOf(s.toString()));
                } catch (NumberFormatException e) {
                    // The only case that should trigger this is the empty string case, so set
                    // mStartYear to the default
                    mParameters.put(OPT_START_YEAR, mDefaults.get(OPT_START_YEAR));
                }
            }
        });

        // Set the OnKeyListener and InputFilters for the EditText
        final EditText stopYearEditText = findViewById(R.id.edit_stop_year);
        stopYearEditText.setOnKeyListener(hideKeyboardListener);
        stopYearEditText.setFilters(yearEditTextFilters);
        stopYearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    mParameters.put(OPT_STOP_YEAR, Integer.valueOf(s.toString()));
                } catch (NumberFormatException e) {
                    // The only case that should trigger this is the empty string case, so set
                    // mStopYear to the default
                    mParameters.put(OPT_STOP_YEAR, mDefaults.get(OPT_STOP_YEAR));
                }
            }
        });

        final Button makeCollectionButton = findViewById(R.id.create_page);
        if (mExistingCollection != null) {
            makeCollectionButton.setText(R.string.update_page);
        }
        makeCollectionButton.setOnClickListener(v -> {
            if (mExistingCollection == null) {
                // Create new collections right away without displaying any warnings
                performCreateOrUpdateCollection();
            } else {
                showUpdateCollectionsWarning();
            }
        });

        // Create help dialog to create a new collection
        createAndShowHelpDialog("first_Time_screen2", R.string.tutorial_select_coin_and_create);

        // Finally, update the UI element values and display state
        // (VISIBLE vs. GONE) of the UI from the internal state.
        updateViewFromState();

        if (BuildConfig.DEBUG) {
            Log.d(APP_NAME, "Finished in onCreate");
        }
    }

    /**
     * Show a warning (if needed) before updating an existing collection
     */
    void showUpdateCollectionsWarning() {

        int warningResId = -1;
        // Show a warning if trying to change an existing collection's type
        if (mExistingCollection.getCollectionTypeIndex() != mCoinTypeIndex) {
            warningResId = R.string.warning_collection_type_change;
        }

        // Show a warning if changing the dates would remove some coins
        if (warningResId == -1 &&
                CollectionListInfo.doesCollectionTypeUseDates(mExistingCollection.getType())) {
            Integer newStartYear = (Integer) mParameters.get(OPT_START_YEAR);
            Integer newStopYear = (Integer) mParameters.get(OPT_STOP_YEAR);
            if (newStartYear == null || newStopYear == null) {
                return;
            } else if ((mExistingCollection.getStartYear() < newStartYear)
                    || (mExistingCollection.getEndYear() > newStopYear)) {
                warningResId = R.string.warning_collection_dates_change;
            }
        }

        // Show a warning if changing the mint marks would remove some coins
        if (warningResId == -1) {
            int mintMarkFlags = getMintMarkFlagsFromParameters(mParameters);
            int checkboxFlags = getCheckboxFlagsFromParameters(mParameters);
            if (mExistingCollection.checkIfNewFlagsRemoveCoins(mintMarkFlags, checkboxFlags)) {
                warningResId = R.string.warning_collection_options_changed;
            }
        }

        if (warningResId != -1) {
            // Display a warning before updating
            showAlert(newBuilder()
                    .setTitle(mRes.getString(R.string.warning))
                    .setMessage(mRes.getString(warningResId))
                    .setCancelable(false)
                    .setPositiveButton(mRes.getString(R.string.yes), (dialog, id) -> {
                        // Update collection
                        dialog.dismiss();
                        performCreateOrUpdateCollection();
                    })
                    .setNegativeButton(mRes.getString(R.string.no), (dialog, id) -> {
                        // Abort
                        dialog.cancel();
                    }));
        } else {
            // Update without displaying a warning
            performCreateOrUpdateCollection();
        }
    }

    /**
     * Performs the create or update collection
     * - Run this after displaying any warnings to the user
     */
    void performCreateOrUpdateCollection() {

        // Go ahead and grab what is in the EditText
        EditText nameEditText = findViewById(R.id.edit_enter_collection_name);
        String collectionName = nameEditText.getText().toString();

        // Perform action on click
        if (collectionName.isEmpty()) {
            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(R.string.error_missing_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate the last year in the collection, if necessary
        if (mParameters.containsKey(OPT_EDIT_DATE_RANGE) &&
                mParameters.get(OPT_EDIT_DATE_RANGE) == Boolean.TRUE) {

            if (!validateStartAndStopYears()) {
                // The function will have already displayed a toast, so return
                return;
            }
        }

        // Ensure that at least one mint mark is selected
        if (mParameters.containsKey(OPT_SHOW_MINT_MARKS) &&
                mParameters.get(OPT_SHOW_MINT_MARKS) == Boolean.TRUE) {

            boolean atLeastOneMintMarkSelected = false;
            for (String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                if (mParameters.containsKey(optName) && mParameters.get(optName) == Boolean.TRUE) {
                    atLeastOneMintMarkSelected = true;
                    break;
                }
            }

            if (!atLeastOneMintMarkSelected) {
                Toast.makeText(CoinPageCreator.this,
                        mRes.getString(R.string.error_no_mint_selected),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Make sure the collection name is good to use
        int checkNameResult = mDbAdapter.checkCollectionName(collectionName);

        // Allow updates to the same collection name
        boolean allowExistingNameForUpdate = (mExistingCollection != null) &&
                (checkNameResult == R.string.collection_name_exists) &&
                (collectionName.equals(mExistingCollection.getName()));

        if (checkNameResult != -1 && !allowExistingNameForUpdate) {
            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(checkNameResult),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Passed all checks - start the creation/update and wait for callbacks to be called
        kickOffAsyncProgressTask(TASK_CREATE_UPDATE_COLLECTION);
    }

    @Override
    public String asyncProgressDoInBackground() {

        // Go ahead and grab what is in the EditText
        EditText nameEditText = findViewById(R.id.edit_enter_collection_name);
        String collectionName = nameEditText.getText().toString();

        // Get the new display order for new collections (display order is preserved
        // for existing collections)
        int newDisplayOrder = (mExistingCollection == null) ?
                mDbAdapter.getNextDisplayOrder() : 0;

        // Create the coin list or update the existing list
        createOrUpdateCoinListForAsyncThread();

        // Create or modify the database
        CollectionListInfo collectionListInfo = getCollectionInfoFromParameters(collectionName);
        return asyncCreateOrUpdateCollection(collectionListInfo, mCoinList, newDisplayOrder);
    }

    @Override
    public void asyncProgressOnPreExecute() {
        createProgressDialog(mRes.getString(R.string.creating_collection));
    }

    @Override
    public void asyncProgressOnPostExecute(String resultStr) {
        super.asyncProgressOnPostExecute(resultStr);
        completeProgressDialogAndFinishActivity();
    }

    /**
     * Returns an input filter for sanitizing collection names
     *
     * @return The input filter
     */
    static InputFilter getCollectionOrCoinNameFilter() {
        return (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (source.charAt(i) == '[' || source.charAt(i) == ']') {
                    // Don't allow these characters as they break the sql queries
                    return "";
                }
            }
            return null;
        };
    }

    /**
     * Updates the internal state based on a new coin type index
     *
     * @param index      The index of this coin type in the list of all collection types
     * @param parameters If not null, set mParameters to parameters.  Otherwise,
     *                   create a new HashMap for mParameters and assign it default
     *                   values based on the new collection type.
     */
    public void setInternalStateFromCollectionIndex(int index, ParcelableHashMap parameters) {

        mCoinTypeIndex = index;

        mCollectionObj = MainApplication.COLLECTION_TYPES[mCoinTypeIndex];

        // Get the defaults for the parameters that this new collection type cares about
        mDefaults = new HashMap<>();
        mCollectionObj.getCreationParameters(mDefaults);

        if (parameters == null) {
            mParameters = new ParcelableHashMap();
            mCollectionObj.getCreationParameters(mParameters);
        } else {
            // Allow the parameters to be passed in for things like testing and on screen rotation
            mParameters = parameters;
        }

        // TODO Validate mParameters
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putInt(_COIN_TYPE_INDEX, mCoinTypeIndex);
        savedInstanceState.putSerializable(_PARAMETERS, mParameters);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Updates the UI from the internal state.  This allows us to easily
     * reset the state of the UI when a big change has occurred (Ex: the
     * individual showMintMark checkboxes should be shown because the
     * showMintMarks checkbox was set to True)
     */
    private void updateViewFromState() {

        Spinner coinTypeSelector = findViewById(R.id.coin_selector);
        CheckBox showMintMarkCheckBox = findViewById(R.id.check_show_mint_mark);
        LinearLayout showMintMarkCheckboxContainer = findViewById(R.id.show_mint_mark_checkbox_container);

        CheckBox editDateRangeCheckBox = findViewById(R.id.check_edit_date_range);
        LinearLayout editStartYearLayout = findViewById(R.id.start_year_layout);
        LinearLayout editStopYearLayout = findViewById(R.id.stop_year_layout);
        EditText editStartYear = findViewById(R.id.edit_start_year);
        EditText editStopYear = findViewById(R.id.edit_stop_year);

        LinearLayout customizableCheckboxContainer = findViewById(R.id.customizable_checkbox_container);

        // Start with the Collection Type list index
        coinTypeSelector.setSelection(mCoinTypeIndex, false);

        // Handle the showMintMarks checkbox
        Boolean showMintMarks = (Boolean) mParameters.get(OPT_SHOW_MINT_MARKS);
        if (showMintMarks != null) {
            showMintMarkCheckBox.setChecked(showMintMarks);
            showMintMarkCheckBox.setVisibility(View.VISIBLE);
        } else {
            showMintMarks = false;
            showMintMarkCheckBox.setVisibility(View.GONE);
        }

        // Now handle the individual showMintMark checkboxes
        for (String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            CheckBox uiElement = showMintMarkCheckboxContainer.findViewWithTag(optName);
            Boolean paramOptValue = (Boolean) mParameters.get(optName);
            if (paramOptValue != null && showMintMarks) {
                String stringIdOptName = SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
                Integer optStringId = (Integer) mParameters.get(stringIdOptName);
                if (optStringId != null) {
                    uiElement.setText(optStringId);
                    uiElement.setChecked(paramOptValue);
                    uiElement.setVisibility(View.VISIBLE);
                } else {
                    // Should never reach this
                    uiElement.setVisibility(View.GONE);
                }
            } else {
                uiElement.setVisibility(View.GONE);
            }
        }

        // Update the UI of the editDateRange checkbox and the associated
        // start/stop year EditTexts
        Boolean editDateRange = (Boolean) mParameters.get(OPT_EDIT_DATE_RANGE);
        if (editDateRange != null) {
            Integer startYear = (Integer) mParameters.get(OPT_START_YEAR);
            Integer stopYear = (Integer) mParameters.get(OPT_STOP_YEAR);

            editDateRangeCheckBox.setChecked(editDateRange);
            editDateRangeCheckBox.setVisibility(View.VISIBLE);

            if (editDateRange && startYear != null && stopYear != null) {
                editStartYearLayout.setVisibility(View.VISIBLE);
                editStartYear.setText(String.valueOf(startYear));
                editStopYearLayout.setVisibility(View.VISIBLE);
                editStopYear.setText(String.valueOf(stopYear));
            } else {
                editStartYearLayout.setVisibility(View.GONE);
                editStopYearLayout.setVisibility(View.GONE);
            }
        } else {
            editDateRangeCheckBox.setVisibility(View.GONE);
            editStartYearLayout.setVisibility(View.GONE);
            editStopYearLayout.setVisibility(View.GONE);
        }

        // Handle the customizable checkboxes
        for (String optName : CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            CheckBox uiElement = customizableCheckboxContainer.findViewWithTag(optName);
            Boolean paramOptValue = (Boolean) mParameters.get(optName);
            if (paramOptValue != null) {
                String stringIdOptName = CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
                Integer optStringId = (Integer) mParameters.get(stringIdOptName);
                if (optStringId != null) {
                    uiElement.setText(optStringId);
                    uiElement.setChecked(paramOptValue);
                    uiElement.setVisibility(View.VISIBLE);
                } else {
                    // Should never reach this
                    uiElement.setVisibility(View.GONE);
                }
            } else {
                uiElement.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Helper function to validate the collection start and stop years
     * <p>
     * NOTE: This doesn't rely on the UI elements having listeners that update the internal state
     * vars so that we can use this before the listeners have been created (or if no
     * listeners will be created, in the case of testing.)
     */
    public boolean validateStartAndStopYears() {

        EditText editStartYear = findViewById(R.id.edit_start_year);
        EditText editStopYear = findViewById(R.id.edit_stop_year);

        Integer startYear = (Integer) mParameters.get(OPT_START_YEAR);
        Integer stopYear = (Integer) mParameters.get(OPT_STOP_YEAR);

        Integer minStartYear = (Integer) mDefaults.get(OPT_START_YEAR);
        Integer maxStartYear = (Integer) mDefaults.get(OPT_STOP_YEAR);

        if (startYear == null || stopYear == null || minStartYear == null || maxStartYear == null) {
            // Shouldn't reach this as all collections should have start/end dates
            return true;
        }

        if (stopYear > maxStartYear) {

            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(R.string.error_ending_year_too_high, maxStartYear),
                    Toast.LENGTH_LONG).show();

            mParameters.put(OPT_STOP_YEAR, maxStartYear);
            editStopYear.setText(String.valueOf(maxStartYear));
            return false;
        }
        if (stopYear < minStartYear) {

            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(R.string.error_ending_year_too_low, minStartYear),
                    Toast.LENGTH_SHORT).show();

            mParameters.put(OPT_STOP_YEAR, maxStartYear);
            editStopYear.setText(String.valueOf(maxStartYear));
            return false;
        }

        if (startYear < minStartYear) {

            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(R.string.error_starting_year_too_low, minStartYear),
                    Toast.LENGTH_LONG).show();

            mParameters.put(OPT_START_YEAR, minStartYear);
            editStartYear.setText(String.valueOf(minStartYear));
            return false;

        } else if (startYear > maxStartYear) {

            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(R.string.error_starting_year_too_high, maxStartYear),
                    Toast.LENGTH_SHORT).show();

            mParameters.put(OPT_START_YEAR, minStartYear);
            editStartYear.setText(String.valueOf(minStartYear));
            return false;
        }

        // Finally, validate them with respect to each other
        if (startYear > stopYear) {
            Toast.makeText(CoinPageCreator.this,
                    mRes.getString(R.string.error_start_gt_end_year),
                    Toast.LENGTH_SHORT).show();

            mParameters.put(OPT_START_YEAR, minStartYear);
            editStartYear.setText(String.valueOf(minStartYear));
            mParameters.put(OPT_STOP_YEAR, maxStartYear);
            editStopYear.setText(String.valueOf(maxStartYear));
            return false;
        }

        // Yay, validation succeeded
        return true;
    }

    /**
     * Helper function to call the make collection method corresponding to the creation parameters
     * NOTE: This is public so we can use it with our current test bench
     */
    public void createOrUpdateCoinListForAsyncThread() {
        mCollectionObj.populateCollectionLists(mParameters, mCoinList);
        if (mExistingCollection != null && mExistingCollection.getCollectionTypeIndex() == mCoinTypeIndex) {
            // If the user is modifying a collection and has selected the same type of coin,
            // preserve any data they may have already entered
            boolean hasMintMarks = (getMintMarkFlagsFromParameters(mParameters) & CollectionListInfo.SHOW_MINT_MARKS) != 0;
            ArrayList<CoinSlot> existingCoinList = mDbAdapter.getCoinList(
                    mExistingCollection.getName(), true);
            ArrayList<CoinSlot> mergedCoinList = new ArrayList<>();

            // Add any custom coins at the beginning of the list
            while ((!existingCoinList.isEmpty()) && existingCoinList.get(0).isCustomCoin()) {
                mergedCoinList.add(existingCoinList.remove(0));
            }

            for (int i = 0; i < mCoinList.size(); i++) {
                CoinSlot newCoin = mCoinList.get(i);
                boolean foundExistingCoinMatch = false;
                for (int j = 0; j < existingCoinList.size(); j++) {
                    CoinSlot existingCoin = existingCoinList.get(j);

                    // Skip custom coins added by the user, as those may spuriously match
                    if (existingCoin.isCustomCoin()) {
                        continue;
                    }

                    if (!mExistingCollection.hasMintMarks() && hasMintMarks) {
                        // If going from no mint marks to having mint marks, copy the coin progress
                        // for the existing identifier into each of the coin mints selected.
                        if (newCoin.getIdentifier().equals(existingCoin.getIdentifier())) {
                            foundExistingCoinMatch = true;
                            newCoin = existingCoin.copy(newCoin.getIdentifier(), newCoin.getMint(), false);
                            break;
                        }
                    } else if (mExistingCollection.hasMintMarks() && !hasMintMarks) {
                        // If going from mint marks to no mint marks, copy at least 1 of the existing
                        // coin's advanced info and merge the inCollection attribute
                        if (newCoin.getIdentifier().equals(existingCoin.getIdentifier())) {
                            existingCoin.setInCollection(existingCoin.isInCollection() || newCoin.isInCollection());
                            existingCoin.setMint(newCoin.getMint());
                            foundExistingCoinMatch = true;
                            newCoin = existingCoin;
                            // No break here to allow merging across all mints
                        }
                    } else {
                        // In all other cases, copy any coins that match identifier and mint
                        if (newCoin.equals(existingCoin)) {
                            foundExistingCoinMatch = true;
                            newCoin = existingCoin;
                            existingCoinList.remove(j);
                            break;
                        }
                    }
                }

                if (foundExistingCoinMatch) {
                    // When a match is found, insert any custom coins with a lower display order ahead
                    // of the match and remove from the list
                    for (int j = 0; j < existingCoinList.size(); j++) {
                        CoinSlot existingCoin = existingCoinList.get(j);
                        if (existingCoin.isCustomCoin() && existingCoin.getSortOrder() < newCoin.getSortOrder()) {
                            mergedCoinList.add(existingCoinList.remove(j--));
                        }
                    }
                }
                mergedCoinList.add(newCoin);
            }

            // Add any remaining custom coins to the end of the list
            for (int j = 0; j < existingCoinList.size(); j++) {
                CoinSlot existingCoin = existingCoinList.get(j);
                if (existingCoin.isCustomCoin()) {
                    mergedCoinList.add(existingCoin);
                }
            }

            // Replace the coin list with the merged coin list
            mCoinList = mergedCoinList;
        }
    }

    /**
     * Returns mint mark flags based on the parameters
     *
     * @param parameters the user-selected parameters
     * @return mint mark flags
     */
    public static int getMintMarkFlagsFromParameters(HashMap<String, Object> parameters) {
        int mintMarkFlags = 0;
        for (String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            String stringIdOptName = SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
            Boolean optionValue = (Boolean) parameters.get(optName);
            Integer optionStrId = (Integer) parameters.get(stringIdOptName);
            if (optionValue == null || optionStrId == null) {
                continue;
            }
            if (optionValue) {
                if (optionStrId.equals(R.string.include_p)) {
                    mintMarkFlags |= CollectionListInfo.MINT_P;
                } else if (optionStrId.equals(R.string.include_d)) {
                    mintMarkFlags |= CollectionListInfo.MINT_D;
                } else if (optionStrId.equals(R.string.include_s)) {
                    mintMarkFlags |= CollectionListInfo.MINT_S;
                } else if (optionStrId.equals(R.string.include_o)) {
                    mintMarkFlags |= CollectionListInfo.MINT_O;
                } else if (optionStrId.equals(R.string.include_cc)) {
                    mintMarkFlags |= CollectionListInfo.MINT_CC;
                }
            }
        }
        // Show mint marks is included in mint mark flags as well
        Boolean optMintMarks = (Boolean) parameters.get(OPT_SHOW_MINT_MARKS);
        if (optMintMarks != null && optMintMarks) {
            mintMarkFlags |= CollectionListInfo.SHOW_MINT_MARKS;
        }
        return mintMarkFlags;
    }

    /**
     * Returns checkbox flags based on the parameters
     *
     * @param parameters the user-selected parameters
     * @return checkbox flags
     */
    public static int getCheckboxFlagsFromParameters(HashMap<String, Object> parameters) {
        int checkboxFlags = 0;
        for (String optName : CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            String stringIdOptName = CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
            Boolean optionValue = (Boolean) parameters.get(optName);
            Integer optionStrId = (Integer) parameters.get(stringIdOptName);
            if (optionValue == null || optionStrId == null) {
                continue;
            }
            if (optionValue) {
                if (optionStrId.equals(R.string.check_show_burnished_eagles)) {
                    checkboxFlags |= CollectionListInfo.BURNISHED;
                } else if (optionStrId.equals(R.string.show_territories)) {
                    checkboxFlags |= CollectionListInfo.TERRITORIES;
                }
            }
        }
        // Custom dates is included in checkbox flags as well
        Boolean optEditDateRange = (Boolean) parameters.get(OPT_EDIT_DATE_RANGE);
        if (optEditDateRange != null && optEditDateRange) {
            checkboxFlags |= CollectionListInfo.CUSTOM_DATES;
        }
        return checkboxFlags;
    }

    /**
     * Returns a new CollectionListInfo object populated based on parameters and existing collection
     *
     * @param collectionName the new or modified collection name
     * @return the CollectionListInfo object
     */
    public CollectionListInfo getCollectionInfoFromParameters(String collectionName) {
        int totalCollected = 0;
        for (CoinSlot coinSlot : mCoinList) {
            totalCollected += coinSlot.isInCollectionInt();
        }
        int mintMarkFlags = getMintMarkFlagsFromParameters(mParameters);
        int checkboxFlags = getCheckboxFlagsFromParameters(mParameters);
        int displayType = (mExistingCollection != null)
                ? mExistingCollection.getDisplayType() : SIMPLE_DISPLAY;
        Integer startYear = (Integer) mParameters.get(OPT_START_YEAR);
        Integer stopYear = (Integer) mParameters.get(OPT_STOP_YEAR);
        return new CollectionListInfo(
                collectionName,
                mCoinList.size(),
                totalCollected,
                mCoinTypeIndex,
                displayType,
                (startYear != null) ? startYear : 0,
                (stopYear != null) ? stopYear : 0,
                mintMarkFlags,
                checkboxFlags);
    }

    /**
     * Returns a parameters HashMap based on an existing collection
     *
     * @param existingCollection the existing collection
     * @return HashMap containing parameters
     */
    public static ParcelableHashMap getParametersFromCollectionListInfo(CollectionListInfo existingCollection) {

        ParcelableHashMap parameters = new ParcelableHashMap();
        existingCollection.getCollectionObj().getCreationParameters(parameters);
        if (parameters.containsKey(OPT_SHOW_MINT_MARKS)) {
            parameters.put(OPT_SHOW_MINT_MARKS, existingCollection.hasMintMarks());
        }
        if (parameters.containsKey(OPT_EDIT_DATE_RANGE)) {
            parameters.put(OPT_EDIT_DATE_RANGE, existingCollection.hasCustomDates());
        }
        if (parameters.containsKey(OPT_START_YEAR)) {
            parameters.put(OPT_START_YEAR, existingCollection.getStartYear());
        }
        if (parameters.containsKey(OPT_STOP_YEAR)) {
            parameters.put(OPT_STOP_YEAR, existingCollection.getEndYear());
        }
        // Mint marks
        for (String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            if (!parameters.containsKey(optName)) {
                continue;
            }
            String stringIdOptName = SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
            Integer optionStrId = (Integer) parameters.get(stringIdOptName);
            if (optionStrId == null) {
                continue;
            }
            if (optionStrId.equals(R.string.include_p)) {
                parameters.put(optName, existingCollection.hasPMintMarks());
            } else if (optionStrId.equals(R.string.include_d)) {
                parameters.put(optName, existingCollection.hasDMintMarks());
            } else if (optionStrId.equals(R.string.include_s)) {
                parameters.put(optName, existingCollection.hasSMintMarks());
            } else if (optionStrId.equals(R.string.include_o)) {
                parameters.put(optName, existingCollection.hasOMintMarks());
            } else if (optionStrId.equals(R.string.include_cc)) {
                parameters.put(optName, existingCollection.hasCCMintMarks());
            }
        }
        // Checkboxes
        for (String optName : CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            if (!parameters.containsKey(optName)) {
                continue;
            }
            String stringIdOptName = CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
            Integer optionStrId = (Integer) parameters.get(stringIdOptName);
            if (optionStrId == null) {
                continue;
            }
            if (optionStrId.equals(R.string.check_show_burnished_eagles)) {
                parameters.put(optName, existingCollection.hasBurnishedCoins());
            } else if (optionStrId.equals(R.string.show_territories)) {
                parameters.put(optName, existingCollection.hasTerritoryCoins());
            }
        }
        return parameters;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Create a database table for a new collection
     *
     * @param collectionListInfo The collection details
     * @param coinList           List of coin slots
     * @param displayOrder       Display order of the collection
     * @return "" if successful, otherwise an error string
     */
    public String asyncCreateOrUpdateCollection(CollectionListInfo collectionListInfo, ArrayList<CoinSlot> coinList,
                                                int displayOrder) {
        try {
            if (mExistingCollection == null) {
                mDbAdapter.createAndPopulateNewTable(collectionListInfo, displayOrder, coinList);
            } else {
                String oldTableName = mExistingCollection.getName();
                mDbAdapter.updateExistingCollection(oldTableName, collectionListInfo, coinList);
            }
        } catch (SQLException e) {
            return mRes.getString(R.string.error_creating_database);
        }
        return "";
    }
}