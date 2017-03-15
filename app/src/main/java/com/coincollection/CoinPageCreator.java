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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.spencerpages.MainApplication;
import com.spencerpages.R;

/**
 * Activity responsible for managing the collection creation page
 */
public class CoinPageCreator extends AppCompatActivity {

    /** mCoinTypeIndex The index of the currently selected coin type in the
     *                 MainApplication.COLLECTION_TYPES list. */
    private int mCoinTypeIndex;

    /** mCollectionObj The CollectionInfo object associated with this index. */
    private CollectionInfo mCollectionObj;

    /** mParameters The HashMap that is used to keep track of the changes that
     *              the user has requested (via the UI) to the detault
     *              collection settings. */
    private HashMap<String, Object> mParameters;

    /** mDefaults The default parameters provided from a call to the current
     *             mCollectionObj getCreationParameters method. */
    private HashMap<String, Object> mDefaults;

    /** mIdentifierList Upon selecting to create a collection, gets populated with a list of the
     *                  individual coin identifiers (years, states, people, etc.) created after
     *                  taking into account the various options above. */
    private final ArrayList<String> mIdentifierList = new ArrayList<>();

    /** mMintList Upon selecting to create a collection, gets populated with a list of the
     *            individual coin mint marks after taking into account the various options above.*/
    private final ArrayList<String> mMintList = new ArrayList<>();

    /** mTask Holds the AsyncTask that we use to interact with the database (so that our database
     *        activity doesn't run on the main thread and trigger an Application Not Responding
     *        error.)  If we change screen orientation and our activity is going to be destroyed,
     *        we have to save this off and pass it to the new activity. */
    private InitTask mTask = null;

    /** mProgressDialog Holds the dialog that we display when the AsyncTask is running to update
     *                  the database.
     *                  TODO There are some issues with how we handle this dialog during the screen
     *                  orientation change case. See other TODOs below and get this working
     *                  correctly if it is indeed broken! */
    private ProgressDialog mProgressDialog = null;

    /**
     * mContext This is currently needed for our UnitTesting, where we use the coin creation
     *          functions in this class and do some sanity checks based on the list that is
     *          created.  Don't use this elsewhere - we should really just be using 'this'.
     *          TODO take this out once we have better unit tests.
     */
    private Context mContext = this;

    /* Internal keys to use for passing data via saved instance state */
    private final static String _COIN_TYPE_INDEX = "CoinTypeIndex";
    private final static String _PARAMETERS = "Parameters";

    /** These are the options supported in the parameter HashMaps.  In general,
     *  this is how they work:
     *  - If an option is present in the parameters HashMap after the call to
     *    getCreationParameters, the associated UI element will be displayed to
     *    the user
     *  - The UI element's default value will be set to the value specified in
     *    the HashMap after a call to getCreationParameters, and the associated
     *    value in the HashMap passed to populateCollectionLists will change
     *    based on changes made to the UI element.
     *  The specifics for the various options are as follows:
     *
     *  OPT_SHOW_MINT_MARKS
     *  - Associated UI Element: 'Show Mint Marks' checkbox
     *  - Associated Value Type: Boolean
     *  - This option MUST be used in conjunction with at least one of the
     *    specific show mint mark options. (Ex: OPT_SHOW_MINT_MARK_1)
     *
     *  OPT_SHOW_MINT_MARK_# (where # is a number between 1 and 5)
     *  - Associated UI Element: Checkboxes that can be used for mint markers
     *  - Associated Value Type: Boolean
     *  - These checkboxes will get hidden and displayed depending on the 'Show Mint Marks'
     *    checkbox.  The text associated with the checkbox must be specified via the
     *    associated OPT_SHOW_MINT_MARK_#_STRING_ID.  There are currently five of these
     *    that can be used per collection (if more are needed, minor changes to the core
     *    code will be necessary)
     *  - These options MUST be used in conjunction with OPT_SHOW_MINT_MARKS, and MUST
     *    be accompanied by the respective OPT_SHOW_MINT_MARK_#_STRING_ID
     *
     *  OPT_SHOW_MINT_MARK_#_STRING_ID (where # is a number between 1 and 5)
     *  - Associated UI Element: Special - see above
     *  - Associated Value Type: Integer
     *  - This option is special - it is used in conjunction with the option above
     *    to indicate the resource ID associated with a String to display next
     *    to the checkbox (Ex: R.string.show_p in the U.S. Coin Collection app)
     *
     *  OPT_EDIT_DATE_RANGE
     *  - Associated UI Element: 'Edit Date Range' checkbox
     *  - Associated Value Type: Boolean
     *  - This option MUST be used in conjunction with OPT_START_YEAR and
     *    OPT_STOP_YEAR
     *
     *  OPT_START_YEAR
     *  - Associated UI Element: 'Edit Start Year' EditText
     *  - Associated Value Type: Integer
     *  - This option MUST be used in conjunction with OPT_EDIT_DATE_RANGE
     *
     *  OPT_STOP_YEAR
     *  - Associated UI Element: 'Edit Stop Year' EditText
     *  - Associated Value Type: Integer
     *  - This option MUST be used in conjunction with OPT_EDIT_DATE_RANGE
     *
     *  OPT_CHECKBOX_# (where # is a number between 1 and 5)
     *  - Associated UI Element: a standalone checkbox
     *  - Associated Value Type: Boolean
     *  - The text associated with the checkbox must be specified via the
     *    associated OPT_SHOW_MINT_MARK_#_STRING_ID.  There are currently five
     *    of these that can be used per collection (if more are needed, minor
     *    changes to the core code will be necessary.)
     *  - These options MUST be accompanied by the respective
     *    OPT_CHECKBOX_#_STRING_ID
     *
     *  OPT_CHECKBOX_#_STRING_ID
     *  - Associated UI Element: Special - see above
     *  - Associated Value Type: Integer
     *  - This option is special - it is used in conjunction with the option above
     *    to indicate the resource ID associated with a String to display next
     *    to the checkbox (Ex: R.string.show_territories in the U.S. Coin
     *    Collection app)
     */
    public final static String OPT_SHOW_MINT_MARKS = "ShowMintMarks";
    public final static String OPT_SHOW_MINT_MARK_1 = "ShowMintMark1";
    public final static String OPT_SHOW_MINT_MARK_2 = "ShowMintMark2";
    public final static String OPT_SHOW_MINT_MARK_3 = "ShowMintMark3";
    public final static String OPT_SHOW_MINT_MARK_4 = "ShowMintMark4";
    public final static String OPT_SHOW_MINT_MARK_5 = "ShowMintMark5";
    public final static String OPT_EDIT_DATE_RANGE = "EditDateRange";
    public final static String OPT_START_YEAR = "StartYear";
    public final static String OPT_STOP_YEAR = "StopYear";
    public final static String OPT_CHECKBOX_1 = "ShowCheckbox1";
    public final static String OPT_CHECKBOX_2 = "ShowCheckbox2";
    public final static String OPT_CHECKBOX_3 = "ShowCheckbox3";
    public final static String OPT_CHECKBOX_4 = "ShowCheckbox4";
    public final static String OPT_CHECKBOX_5 = "ShowCheckbox5";

    // TODO Is there a better way to pass this info?  Maybe we can
    // store default values in ecah app's MainApplication and use
    // those if not specified?
    public final static String OPT_SHOW_MINT_MARK_1_STRING_ID = "ShowMintMark1StringId";
    public final static String OPT_SHOW_MINT_MARK_2_STRING_ID = "ShowMintMark2StringId";
    public final static String OPT_SHOW_MINT_MARK_3_STRING_ID = "ShowMintMark3StringId";
    public final static String OPT_SHOW_MINT_MARK_4_STRING_ID = "ShowMintMark4StringId";
    public final static String OPT_SHOW_MINT_MARK_5_STRING_ID = "ShowMintMark5StringId";

    public final static String OPT_CHECKBOX_1_STRING_ID = "ShowCheckbox1StringId";
    public final static String OPT_CHECKBOX_2_STRING_ID = "ShowCheckbox2StringId";
    public final static String OPT_CHECKBOX_3_STRING_ID = "ShowCheckbox3StringId";
    public final static String OPT_CHECKBOX_4_STRING_ID = "ShowCheckbox4StringId";
    public final static String OPT_CHECKBOX_5_STRING_ID = "ShowCheckbox5StringId";

    /** This flag should be used by collections whose year of most recent
     *  production should track the current year.
     *
     * TODO Make this easier to maintain, but make sure it doesn't break database
     *      upgrade functionality */
    public final static Integer OPTVAL_STILL_IN_PRODUCTION = 2017;


    private final static HashMap<String,String> SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP = new HashMap<>();

    static {
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_1, OPT_SHOW_MINT_MARK_1_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_2, OPT_SHOW_MINT_MARK_2_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_3, OPT_SHOW_MINT_MARK_3_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_4, OPT_SHOW_MINT_MARK_4_STRING_ID);
        SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_SHOW_MINT_MARK_5, OPT_SHOW_MINT_MARK_5_STRING_ID);
    };

    private final static HashMap<String,String> CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP = new HashMap();

    static {
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_1, OPT_CHECKBOX_1_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_2, OPT_CHECKBOX_2_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_3, OPT_CHECKBOX_3_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_4, OPT_CHECKBOX_4_STRING_ID);
        CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.put(OPT_CHECKBOX_5, OPT_CHECKBOX_5_STRING_ID);
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the actionbar so that clicking the icon takes you back (SO 1010877)
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.collection_creation_page);

        // NOTE: the UI is not fully inflated at this point (specifically, some
        // of the checkboxes which are added programmatically have not been
        // instantiated yet.)

        // Initialize our instance variables
        if(savedInstanceState != null)
        {
            // Pull in enough of the saved state to initialize the UI
            setInternalStateFromCollectionIndex(
                    savedInstanceState.getInt(_COIN_TYPE_INDEX),
                    (HashMap<String, Object>) savedInstanceState.getSerializable(_PARAMETERS));

        } else {

            // Initialize mCoinTypeIndex and related internal state to index 0
            setInternalStateFromCollectionIndex(0, null);
        }

        // If we have an InitTask already running, inherit it
        InitTask check = (InitTask) getLastCustomNonConfigurationInstance();

        // TODO If there is a screen orientation change, it looks like a mProgressDialog gets leaked. :(
        if(check != null){
            mTask = check;

            // Change the task's activity so that we are the "parent". See note above AsyncTask
            // definition for more info.
            mTask.activity = this;

            // Make a new dialog
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Creating Collection...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setProgress(0);
            mProgressDialog.show();

        }

        // Next, we will finish setting up the various UI elements (creating
        // adapters, listeners, etc..  We won't set any of the values yet -
        // we will do that at the end.

        // Prepare the Spinner that gets what type of collection they want to make
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);

        for(int i = 0; i < MainApplication.COLLECTION_TYPES.length; i++)
        {
            spinnerAdapter.add(MainApplication.COLLECTION_TYPES[i].getCoinType());
        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner coinTypeSelector = (Spinner) findViewById(R.id.coin_selector);
        coinTypeSelector.setAdapter(spinnerAdapter);
        coinTypeSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                    View view, int pos, long id) {

                // No need to do anything if onItemSelected was called but the selected index hasn't
                // changed since:
                //  - first activity initialization, or
                //  - activity initialization from SavedInstanceState
                if(mCoinTypeIndex == pos) {
                    return;
                }

                // When an item is selected, switch our internal state based on the collection type
                setInternalStateFromCollectionIndex(pos, null);

                // Reset the view for the new coin type
                updateViewFromState();

            }
            public void onNothingSelected(AdapterView<?> parent) { }
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

        OnKeyListener hideKeyboardListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                    // This should hide the keyboard
                    // Thanks! http://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    // Returning true prevents the action that would ordinarily have happened from taking place
                    return keyCode == KeyEvent.KEYCODE_ENTER;
                }
                return false;
            }
        };

        // Set the OnKeyListener for the EditText
        final EditText nameEditText = (EditText) findViewById(R.id.edit_enter_collection_name);
        nameEditText.setOnKeyListener(hideKeyboardListener);

        // Make a filter to block out bad characters
        InputFilter nameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) == '[' || source.charAt(i) == ']') {
                        // Don't allow these characters to break the sql
                        return "";
                    } else if(source.charAt(i) == ','){
                        // This will break the csv format
                        return "";
                    }
                }
                return null;
            }
        };

        nameEditText.setFilters(new InputFilter[]{nameFilter});

        // Set the listener for the show mint mark checkbox
        final CheckBox showMintMarkCheckBox = (CheckBox) findViewById(R.id.check_show_mint_mark);
        showMintMarkCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

                // Don't take any action if the value isn't changing - needed to prevent
                // loops that would get created by the call to updateViewFromState()
                if((Boolean)mParameters.get(OPT_SHOW_MINT_MARKS) == isChecked){
                    return;
                }

                mParameters.put(OPT_SHOW_MINT_MARKS, isChecked);

                // Restore defaults for all of the mint mark checkboxes when this is unchecked
                if(!isChecked) {
                    for (String key : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                        if (mParameters.containsKey(key)) {
                            mParameters.put(key, mDefaults.get(key));
                        }
                    }
                }

                // Refresh the UI so that the individual mint mark checkboxes are either
                // hidden or displayed
                updateViewFromState();
            }
        });

        // Set the listener for the edit date range
        final CheckBox editDateRangeCheckBox = (CheckBox) findViewById(R.id.check_edit_date_range);
        editDateRangeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

                // Don't take any action if the value isn't changing - needed to prevent
                // loops that would get created by the call to updateViewFromState()
                if((Boolean)mParameters.get(OPT_EDIT_DATE_RANGE) == isChecked){
                    return;
                }

                mParameters.put(OPT_EDIT_DATE_RANGE, isChecked);

                // Reset the start/stop year when the field is unchecked
                if(!isChecked) {
                    mParameters.put(OPT_START_YEAR, mDefaults.get(OPT_START_YEAR));
                    mParameters.put(OPT_STOP_YEAR, mDefaults.get(OPT_STOP_YEAR));
                }

                // Refresh the UI so that the start/stop year EditTexts are hidden or displayed
                updateViewFromState();
            }
        });

        // Instantiate an onCheckedChangeListener for use by all the simple checkboxes
        OnCheckedChangeListener checkboxChangeListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // The tag store the OPT_NAME associated with the button
                String optName = (String) compoundButton.getTag();
                mParameters.put(optName, isChecked);
            }
        };

        // Instantiate a LayoutParams for the simple checkboxes
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LinearLayout showMintMarksContainer = (LinearLayout) findViewById(R.id.show_mint_mark_checkbox_container);

        // Create the ShowMintMark Checkboxes (even if they aren't needed right now)
        for (String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
            // Instantiate a checkbox in the UI for this option
            CheckBox box = new AppCompatCheckBox(this);
            box.setLayoutParams(layoutParams);
            box.setTag(optName);
            box.setOnCheckedChangeListener(checkboxChangeListener);

            showMintMarksContainer.addView(box);
        }

        // Add any stand-alone, customizable checkboxes
        LinearLayout customizableCheckboxContainer = (LinearLayout) findViewById(R.id.customizable_checkbox_container);

        for(String optName : CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()){
            // Instantiate a checkbox in the UI for this option
            CheckBox box = new AppCompatCheckBox(this);
            box.setLayoutParams(layoutParams);
            box.setTag(optName);
            box.setOnCheckedChangeListener(checkboxChangeListener);

            customizableCheckboxContainer.addView(box);
        }

        // Make a filter to block out non-numeric characters
        InputFilter digitFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) < '0' || source.charAt(i) > '9') {
                        // Don't allow these characters
                        return "";
                    }
                }
                return null;
            }
        };

        // Make a filter limiting the year text fields to 4 characters
        InputFilter yearLengthFilter = new InputFilter.LengthFilter(4);

        InputFilter[] yearEditTextFilters = new InputFilter[]{digitFilter, yearLengthFilter};

        // Set the OnKeyListener and InputFilters for the EditText
        final EditText startYearEditText = (EditText) findViewById(R.id.edit_start_year);
        startYearEditText.setOnKeyListener(hideKeyboardListener);
        startYearEditText.setFilters(yearEditTextFilters);

        startYearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
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
        final EditText stopYearEditText = (EditText) findViewById(R.id.edit_stop_year);
        stopYearEditText.setOnKeyListener(hideKeyboardListener);
        stopYearEditText.setFilters(yearEditTextFilters);

        stopYearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
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

        final Button makeCollectionButton = (Button) findViewById(R.id.create_page);
        makeCollectionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Go ahead and grab what is in the EditText
                String collectionName = nameEditText.getText().toString();

                // Perform action on click
                if(collectionName.equals("")){
                    Toast.makeText(CoinPageCreator.this, "Please enter a name for the collection", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate the last year in the collection, if necessary
                if(mParameters.containsKey(OPT_EDIT_DATE_RANGE) &&
                        mParameters.get(OPT_EDIT_DATE_RANGE) == Boolean.TRUE){

                    boolean result = validateStartAndStopYears();
                    if(!result){
                        // The function will have already displayed a toast, so return
                        return;
                    }
                }

                // Ensure that at least one mint mark is selected
                if(mParameters.containsKey(OPT_SHOW_MINT_MARKS) &&
                        mParameters.get(OPT_SHOW_MINT_MARKS) == Boolean.TRUE){

                    boolean atLeastOneMintMarkSelected = false;
                    for(String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()) {
                        if (mParameters.containsKey(optName) && mParameters.get(optName) == Boolean.TRUE) {
                            atLeastOneMintMarkSelected = true;
                            break;
                        }
                    }

                    if(!atLeastOneMintMarkSelected){

                        Toast.makeText(CoinPageCreator.this, "Please select at least one mint to collect coins from", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Otherwise, good to go
                }

                //Get a list of all the database tables

                // TODO Move this into the AsyncTask so that we don't have to do two calls
                // to open the db
                // Open it again.  This one shouldn't take long
                DatabaseAdapter dbAdapter = new DatabaseAdapter(CoinPageCreator.this);
                dbAdapter.open();

                // By the time the user is able to click this mDbAdapter should not be NULL anymore
                Cursor resultCursor = dbAdapter.getAllCollectionNames();
                if(resultCursor == null){
                    Toast.makeText(CoinPageCreator.this, "Failed to get list of current collections, low on memory perhaps?", Toast.LENGTH_SHORT).show();
                    return;
                }
                // We will count the collections for here for convenience as well
                int numberOfCollections = 0;
                // THanks! http://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
                if (resultCursor.moveToFirst()){
                    do{
                        Locale defaultLocale = Locale.getDefault();
                        if(resultCursor.getString(resultCursor.getColumnIndex("name")).toLowerCase(defaultLocale).equals(collectionName.toLowerCase(defaultLocale))){
                            Toast.makeText(CoinPageCreator.this, "A collection with this name already exists, please choose a different name", Toast.LENGTH_SHORT).show();
                            resultCursor.close();
                            return;
                        }

                        numberOfCollections++;

                    }while(resultCursor.moveToNext());
                }

                resultCursor.close();

                dbAdapter.close();

                //Now actually set up the mIdentifierList and mMintList
                populateCollectionArrays();

                mTask = new InitTask();

                // TODO Probably a more elegant way to pass these arguments
                mTask.tableName = collectionName;
                mTask.coinType = mCollectionObj.getCoinType();
                mTask.coinIdentifiers = mIdentifierList;
                mTask.coinMints = mMintList;
                mTask.displayOrder = numberOfCollections;

                mTask.activity = CoinPageCreator.this;

                mTask.execute();

                // Wait for it to finish and trigger the callback method
            }
        });

        // Check whether it is the user's first time using the app
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        if(mainPreferences.getBoolean("first_Time_screen2", true)){
            // Show the user how to do everything
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.tutorial_select_coin_and_create))
            .setCancelable(false)
            .setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = mainPreferences.edit();
                    editor.putBoolean("first_Time_screen2", false);
                    editor.commit(); // .apply() in later APIs
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        // Finally, update the UI element values and display state
        // (VISIBLE vs. GONE) of the UI from the internal state.
        updateViewFromState();

        Log.d(MainApplication.APP_NAME, "Finished in onCreate");

    }

    /**
     * Updates the internal state based on a new coin type index
     * @param index The index of this coin type in the list of all collection types
     * @param parameters If not null, set mParameters to parameters.  Otherwise,
     *                   create a new HashMap for mParameters and assign it default
     *                   values based on the new collection type.
     *
     */
    private void setInternalStateFromCollectionIndex(int index, HashMap<String, Object> parameters){

        mCoinTypeIndex = index;

        mCollectionObj = MainApplication.COLLECTION_TYPES[mCoinTypeIndex];

        // Get the defaults for the parameters that this new collection type cares about
        mDefaults = new HashMap<>();
        mCollectionObj.getCreationParameters(mDefaults);

        if (parameters == null) {
            mParameters = new HashMap<>();
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
     *  Updates the UI from the internal state.  This allows us to easily
     *  reset the state of the UI when a big change has occurred (Ex: the
     *  individual showMintMark checkboxes should be shown because the
     *  showMintMarks checkbox was set to True)
     */
    private void updateViewFromState(){

        Spinner coinTypeSelector = (Spinner) findViewById(R.id.coin_selector);
        CheckBox showMintMarkCheckBox = (CheckBox) findViewById(R.id.check_show_mint_mark);
        LinearLayout showMintMarkCheckboxContainer = (LinearLayout) findViewById(R.id.show_mint_mark_checkbox_container);

        CheckBox editDateRangeCheckBox = (CheckBox) findViewById(R.id.check_edit_date_range);
        LinearLayout editStartYearLayout = (LinearLayout) findViewById(R.id.start_year_layout);
        LinearLayout editStopYearLayout = (LinearLayout) findViewById(R.id.stop_year_layout);
        EditText editStartYear = (EditText) findViewById(R.id.edit_start_year);
        EditText editStopYear = (EditText) findViewById(R.id.edit_stop_year);

        LinearLayout customizableCheckboxContainer = (LinearLayout) findViewById(R.id.customizable_checkbox_container);

        // Start with the Collection Type list index
        coinTypeSelector.setSelection(mCoinTypeIndex, false);

        // Handle the showMintMarks checkbox
        Boolean showMintMarks = false;
        if(mParameters.containsKey(OPT_SHOW_MINT_MARKS)) {

            showMintMarks = (Boolean)mParameters.get(OPT_SHOW_MINT_MARKS);

            showMintMarkCheckBox.setChecked(showMintMarks);
            showMintMarkCheckBox.setVisibility(View.VISIBLE);

        } else {
            showMintMarkCheckBox.setVisibility(View.GONE);
        }

        // Now handle the individual showMintMark checkboxes
        for(String optName : SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.keySet()){
            CheckBox uiElement = (CheckBox) showMintMarkCheckboxContainer.findViewWithTag(optName);

            if(mParameters.containsKey(optName) && showMintMarks){

                String stringIdOptName = SHOW_MINT_MARK_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
                uiElement.setText((Integer)mParameters.get(stringIdOptName));

                uiElement.setChecked((Boolean)mParameters.get(optName));
                uiElement.setVisibility(View.VISIBLE);
            } else {
                uiElement.setVisibility(View.GONE);
            }
        }

        // Update the UI of the editDateRange checkbox and the associated
        // start/stop year EditTexts
        if(mParameters.containsKey(OPT_EDIT_DATE_RANGE)){
            Boolean editDateRange = (Boolean) mParameters.get(OPT_EDIT_DATE_RANGE);
            Integer startYear = (Integer) mParameters.get(OPT_START_YEAR);
            Integer stopYear = (Integer) mParameters.get(OPT_STOP_YEAR);

            editDateRangeCheckBox.setChecked(editDateRange);
            editDateRangeCheckBox.setVisibility(View.VISIBLE);

            if(editDateRange) {
                editStartYearLayout.setVisibility(View.VISIBLE);
                editStartYear.setText(Integer.toString(startYear));

                editStopYearLayout.setVisibility(View.VISIBLE);
                editStopYear.setText(Integer.toString(stopYear));
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
        for(String optName : CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.keySet()){
            CheckBox uiElement = (CheckBox) customizableCheckboxContainer.findViewWithTag(optName);

            if(mParameters.containsKey(optName)){
                String stringIdOptName = CUSTOMIZABLE_CHECKBOX_STRING_ID_OPT_MAP.get(optName);
                uiElement.setText((Integer)mParameters.get(stringIdOptName));

                uiElement.setChecked((Boolean)mParameters.get(optName));
                uiElement.setVisibility(View.VISIBLE);
            } else {
                uiElement.setVisibility(View.GONE);
            }
        }
    }

    /**
     *  Helper function to validate the collection start and stop years
     *
     *  NOTE: This doesn't rely on the UI elements having listeners that update the internal state
     *        vars so that we can use this before the listeners have been created (or if no
     *        listeners will be created, in the case of testing.)
     */
    private boolean validateStartAndStopYears(){

        EditText editStartYear = (EditText) findViewById(R.id.edit_start_year);
        EditText editStopYear = (EditText) findViewById(R.id.edit_stop_year);

        Integer startYear = (Integer) mParameters.get(OPT_START_YEAR);
        Integer stopYear = (Integer) mParameters.get(OPT_STOP_YEAR);

        Integer minStartYear = (Integer) mDefaults.get(OPT_START_YEAR);
        Integer maxStartYear = (Integer) mDefaults.get(OPT_STOP_YEAR);

        if(stopYear > maxStartYear){

            Toast.makeText(CoinPageCreator.this,
                "Highest possible ending year is " + String.valueOf(maxStartYear) +
                        ".  Note, new years will automatically be added as they come.",
                Toast.LENGTH_LONG).show();

            mParameters.put(OPT_STOP_YEAR, maxStartYear);
            editStopYear.setText(Integer.toString(maxStartYear));
            return false;
        }
        if(stopYear < minStartYear){

            Toast.makeText(CoinPageCreator.this,
                "Ending year can't be less than the collection starting year (" + String.valueOf(minStartYear) +
                        ")",
                Toast.LENGTH_SHORT).show();

            mParameters.put(OPT_STOP_YEAR, maxStartYear);
            editStopYear.setText(Integer.toString(maxStartYear));
            return false;
        }

        if(startYear < minStartYear){

            Toast.makeText(CoinPageCreator.this,
                "Lowest possible starting year is " + String.valueOf(minStartYear),
                Toast.LENGTH_LONG).show();

            mParameters.put(OPT_START_YEAR, minStartYear);
            editStartYear.setText(Integer.toString(minStartYear));
            return false;

        } else if(startYear > maxStartYear){

            Toast.makeText(CoinPageCreator.this,
                "Starting year can't be greater than the collection ending year (" + String.valueOf(maxStartYear) +
                        ")",
                Toast.LENGTH_SHORT).show();

            mParameters.put(OPT_START_YEAR, minStartYear);
            editStartYear.setText(Integer.toString(minStartYear));
            return false;
        }

        // Finally, validate them with respect to each other
        if(startYear > stopYear){
            Toast.makeText(CoinPageCreator.this, "Starting year can't be greater than the ending year", Toast.LENGTH_SHORT).show();

            mParameters.put(OPT_START_YEAR, minStartYear);
            editStartYear.setText(Integer.toString(minStartYear));
            mParameters.put(OPT_STOP_YEAR, maxStartYear);
            editStopYear.setText(Integer.toString(maxStartYear));
            return false;
        }

        // Yay, validation succeeded
        return true;

    }

    /**
     *  Helper function to call the make collection method corresponding to the creation parameters
     *  NOTE: This is public so we can use it with our current test bench
     */
    public void populateCollectionArrays(){

        mCollectionObj.populateCollectionLists(mParameters, mIdentifierList, mMintList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            this.onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public Object onRetainCustomNonConfigurationInstance(){

        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
            return mTask;
        } else {
            // No dialog showing, do nothing
            return null;
        }
    }

    @Override
    public void onDestroy(){

        // TODO Not a perfect solution, but assuming this gets called, we should cut down on the
        // race condition inherent in how we do our AsyncTask
        if(mTask != null) {
            mTask.activity = null;
        }
        super.onDestroy();

    }

    /**
     * sub-class of AsyncTask
     * See: http://stackoverflow.com/questions/6450275/android-how-to-work-with-asynctasks-progressdialog
     */
    // TODO For passing the AsyncTask between Activity instances, see this post:
    // http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
    // Our method is subject to the race conditions described therein :O
    class InitTask extends AsyncTask<Void, Void, Void>
    {
        String tableName;
        String coinType;
        ArrayList<String> coinIdentifiers;
        ArrayList<String> coinMints;
        int displayOrder;

        CoinPageCreator activity;

        @Override
        protected Void doInBackground( Void... params )
        {
            // Open it again.  This one shouldn't take long
            DatabaseAdapter dbAdapter = new DatabaseAdapter(activity);
            dbAdapter.open();

            dbAdapter.createNewTable(this.tableName, this.coinType, this.coinIdentifiers, this.coinMints, this.displayOrder);

            dbAdapter.close();

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // TODO Move the mProgressDialog handling code into a CoinPageCreator method and don't
            // update it directly here.

            if(activity == null){
                return;
            }

            ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);
            dialog.setMessage("Creating Collection...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);
            dialog.show();

            if(activity != null){
                activity.mProgressDialog = dialog;
            }

        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute( Void result )
        {
            super.onPostExecute(result);

            if(activity == null){
                return;
            }

            if(activity.mProgressDialog != null) {
                if (activity.mProgressDialog.isShowing()) {
                    activity.mProgressDialog.dismiss();
                }
            }

            // TODO Not sure if this does anything/is needed
            activity.mProgressDialog = null;

            activity.finish();
            activity = null;
        }
    }

    /**
     * Function used by our unit tests - sets the internal state of the CoinPageCreator such that
     * we can make collection tests for testing
     */
    public void testSetInternalState(int coinTypeIndex, HashMap<String, Object>parameters) {

        setInternalStateFromCollectionIndex(coinTypeIndex, parameters);
    }

    /**
     * Testing function - getter for mIdentifierList
     */
    public ArrayList<String> testGetIdentifierList(){
        return mIdentifierList;
    }

    /**
     * Testing function - getter for mMintList
     * @return
     */
    public ArrayList<String> testGetMintList(){
        return mMintList;
    }

    /**
     * Testing function - clears mIdentifierList and mMintList
     */
    public void testClearLists(){
        mIdentifierList.clear();
        mMintList.clear();
    }

    /**
     * Testing function - changes the context that is used for resource lookups. Without this, the
     * calls to getResource in some of the collection creation functions will trigger
     * NullPointerExceptions. Instead we lend our CoinPageCreator our context so that we can
     * sidestep actually going through the CoinPageCreator android lifecycle.
     */
    public void testSetContext(Context context){
        mContext = context;
    }
}