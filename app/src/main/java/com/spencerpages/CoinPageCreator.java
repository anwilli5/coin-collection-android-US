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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Activity responsible for managing the collection creation page
 */
public class CoinPageCreator extends AppCompatActivity {

    // These instance variables are initialized in onCreate and/or resetViews(String coinType), and
    // then updated via the various UI options presented in the collection creation view (or from
    // the previous activity state in the case of a screen orientation change.)
    //
    // Having this data in instance variables makes accessing this data cleaner (so we don't have to
    // constantly reach into the UI elements themselves to obtain the values) but comes at the cost
    // of requiring a lot of code to maintain this state when the UI is updated.  It also allows us
    // to do testing easier (without requiring the UI.)
    //
    // TODO Are the benefits worth the cost?  This code is (overly?) complicated! lol

    private String mCoinType;
    private int mCoinTypeIndex;
    private boolean mShowMintMark;
    private boolean mEditDateRange;
    private boolean mShowTerritories;
    private boolean mShowBurnished;
	private boolean mShowP;
	private boolean mShowD;
	private boolean mShowS;
	private boolean mShowO;
	private boolean mShowCC;
    private int mStartYear;
    private int mStopYear;
    private Resources mRes;

    /** mIdentifierList Upon selecting to create a collection, gets populated with a list of the
     *                  individual coin identifiers (years, states, people, etc.) created after
     *                  taking into account the various options above. */
    private final ArrayList<String> mIdentifierList = new ArrayList<>();

    /** mMintList Upon selecting to create a collection, gets populated with a list of the
     *            individual coin mint marks after taking into account the various options above.*/
    private final ArrayList<String> mMintList = new ArrayList<>();

    /** mStartYears Holds the first year of mintage for each of the coin sets, loaded in from the
     *              corresponding int array resource. */
    private int[] mStartYears;

    /** mStopYears Holds the last year of mintage for each of the coin sets, loaded in from the
     *             corresponding int array resource. */
    private int[] mStopYears;

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

    // Saved instance variable names
    private final static String COIN_TYPE = "CoinType";
    private final static String COIN_TYPE_INDEX = "CoinTypeIndex";
    private final static String SHOW_MINT_MARKS = "ShowMintMarks";
    private final static String SHOW_TERRITORIES = "ShowTerritories";
    private final static String SHOW_BURNISHED = "ShowBurnished";
    private final static String EDIT_DATE_RANGE = "EditDateRange";
    private final static String SHOW_P = "ShowP";
    private final static String SHOW_D = "ShowD";
    private final static String SHOW_S = "ShowS";
    private final static String SHOW_O = "ShowO";
    private final static String SHOW_CC = "ShowCC";
    private final static String START_YEAR = "StartYear";
    private final static String STOP_YEAR = "StopYear";
    private final static String START_YEARS = "StartYears";
    private final static String STOP_YEARS = "StopYears";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set the actionbar so that clicking the icon takes you back (SO 1010877)
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.collection_creation_page);
        mRes = getResources();

        // Initialize our instance variables
        if(savedInstanceState != null)
        {
            // Pull in enough of the saved state to initialize the UI
            mCoinType = savedInstanceState.getString(COIN_TYPE);
            mCoinTypeIndex = savedInstanceState.getInt(COIN_TYPE_INDEX);
            mStartYears = savedInstanceState.getIntArray(START_YEARS);
            mStopYears = savedInstanceState.getIntArray(STOP_YEARS);

            // Reset the UI so that it matches what we expect. The values of the various UI elements
            // get set to their previous values for us, but which
            resetView(mCoinType);

            // Finish pulling in the rest of the data
            mShowMintMark = savedInstanceState.getBoolean(SHOW_MINT_MARKS);
            mShowTerritories = savedInstanceState.getBoolean(SHOW_TERRITORIES);
            mShowBurnished = savedInstanceState.getBoolean(SHOW_BURNISHED);
            mEditDateRange = savedInstanceState.getBoolean(EDIT_DATE_RANGE);
            mShowP = savedInstanceState.getBoolean(SHOW_P);
            mShowD = savedInstanceState.getBoolean(SHOW_D);
            mShowS = savedInstanceState.getBoolean(SHOW_S);
            mShowO = savedInstanceState.getBoolean(SHOW_O);
            mShowCC = savedInstanceState.getBoolean(SHOW_CC);
            mStartYear = savedInstanceState.getInt(START_YEAR);
            mStopYear = savedInstanceState.getInt(STOP_YEAR);

            // And update the UI with the rest of the data
            updateViewFromState();

        } else {

            // Need to get the array of the first/last years
            mStartYears = mRes.getIntArray(R.array.year_of_first_production);
            mStopYears = mRes.getIntArray(R.array.year_of_most_recent_production);

            // Initialize mCoinType and mCoinTypeIndex
            String[] coinTypes = mRes.getStringArray(R.array.types_of_coins);
            mCoinTypeIndex = 0;
            mCoinType = coinTypes[mCoinTypeIndex];

            // Set the variables keeping track of start/stop years
            mStartYear = mStartYears[mCoinTypeIndex];
            mStopYear = mStopYears[mCoinTypeIndex];

            // Initialize the rest of the UI and instance vars
            resetView(mCoinType);
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

        // Prepare the Spinner that gets what type of collection they want to make
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
				this, R.array.types_of_coins, android.R.layout.simple_spinner_item);
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

                // When an item is selected, we want to store the choice the person made
				mCoinType = parent.getAdapter().getItem(pos).toString();
				mCoinTypeIndex = pos;

				// Reset the view for the new coin type
				resetView(mCoinType);
				
			}
			public void onNothingSelected(AdapterView<?> parent) { }
		});

        // Create an OnKeyListener that can be used to hide the soft keyboard when the enter key
        // (or a few others) are pressed.
        //
        // TODO OnKeyListeners aren't guaranteed to work software keyboards... find a better way
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
				// Perform action depending on whether it's now checked

                if(mShowMintMark == isChecked){
                    return;
                }

                mShowMintMark = isChecked;

                if(mShowMintMark){
                    // When this is newly checked, default to checking mShowP, but leave the others
                    // unchecked (they get cleared during initialization or from the last uncheck)
                    mShowP = true;
                }

                updateViewFromState();
			}
		});

        // Set the listener for the edit date range
		final CheckBox editDateRangeCheckBox = (CheckBox) findViewById(R.id.check_edit_date_range);
		editDateRangeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked

                if(mEditDateRange == isChecked){
                    return;
                }

                mEditDateRange = isChecked;

                // Reset the start/stop year when they check and uncheck
                mStartYear = mStartYears[mCoinTypeIndex];
                mStopYear = mStopYears[mCoinTypeIndex];

                updateViewFromState();
			}
		});

        // Set the listener for the show territories checkbox
		final CheckBox showTerritoriesCheckBox = (CheckBox) findViewById(R.id.check_show_territories);
		showTerritoriesCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowTerritories = isChecked;
			}
		});
		
		// Set the listener for the show burnished silver eagle checkbox
		final CheckBox showBurnishedCheckBox = (CheckBox) findViewById(R.id.check_show_burnished_eagles);
		showBurnishedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowBurnished = isChecked;
			}
		});

		// Set the listener for the include P checkbox
		final CheckBox includePCheckBox = (CheckBox) findViewById(R.id.check_include_p);
		includePCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowP = isChecked;
			}
		});

		// Set the listener for the include D checkbox
		final CheckBox includeDCheckBox = (CheckBox) findViewById(R.id.check_include_d);
		includeDCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowD = isChecked;
			}
		});

		// Set the listener for the include S checkbox
		final CheckBox includeSCheckBox = (CheckBox) findViewById(R.id.check_include_s);
		includeSCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowS = isChecked;
			}
		});
		
		// Set the listener for the include O checkbox
		final CheckBox includeOCheckBox = (CheckBox) findViewById(R.id.check_include_o);
		includeOCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowO = isChecked;
			}
		});
		
		// Set the listener for the include CC checkbox
		final CheckBox includeCCCheckBox = (CheckBox) findViewById(R.id.check_include_cc);
		includeCCCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				// Perform action depending on whether it's now checked
				mShowCC = isChecked;
			}
		});

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
                    mStartYear = Integer.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    // The only case that should trigger this is the empty string case, so set
                    // mStartYear to the default
                    mStartYear = mStartYears[mCoinTypeIndex];
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
                    mStopYear = Integer.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    // The only case that should trigger this is the empty string case, so set
                    // mStopYear to the default
                    mStopYear = mStopYears[mCoinTypeIndex];
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
				if(mEditDateRange){

				    boolean result = validateStartAndStopYears();
				    if(!result){
				    	// The function will have already displayed a toast, so return
				    	return;
				    }
				}

				// Ensure that at least one mint mark is selected
				if(mShowMintMark){
				    if(!mShowP && !mShowD && !mShowS && !mShowO && !mShowCC){
						
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
				makeTable(mCoinType);
				
				mTask = new InitTask();

				// TODO Probably a more elegant way to pass these arguments
				mTask.tableName = collectionName;
				mTask.coinType = mCoinType;
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
			builder.setMessage(mRes.getString(R.string.tutorial_select_coin_and_create))
			.setCancelable(false)
			.setPositiveButton(mRes.getString(R.string.okay), new DialogInterface.OnClickListener() {
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

        Log.d(MainApplication.APP_NAME, "Finished in onCreate");

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(COIN_TYPE, mCoinType);
        savedInstanceState.putInt(COIN_TYPE_INDEX, mCoinTypeIndex);
        savedInstanceState.putBoolean(SHOW_MINT_MARKS, mShowMintMark);
        savedInstanceState.putBoolean(SHOW_TERRITORIES, mShowTerritories);
        savedInstanceState.putBoolean(SHOW_BURNISHED, mShowBurnished);
        savedInstanceState.putBoolean(EDIT_DATE_RANGE, mEditDateRange);
        savedInstanceState.putBoolean(SHOW_P, mShowP);
        savedInstanceState.putBoolean(SHOW_D, mShowD);
        savedInstanceState.putBoolean(SHOW_S, mShowS);
        savedInstanceState.putBoolean(SHOW_O, mShowO);
        savedInstanceState.putBoolean(SHOW_CC, mShowCC);
        savedInstanceState.putInt(START_YEAR, mStartYear);
        savedInstanceState.putInt(STOP_YEAR, mStopYear);
        savedInstanceState.putIntArray(START_YEARS, mStartYears);
        savedInstanceState.putIntArray(START_YEARS, mStopYears);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     *  Updates the UI and the internal state so that all of the optional components are hidden and
     *  set to their default values.
     *
     *  NOTE: This doesn't rely on the UI elements having listeners that update the internal state
     *        vars so that we can use this before the listeners have been created (or if no
     *        listeners will be created, in the case of testing.)
     */
	private void resetView(){
		
		EditText editStartYear = (EditText) findViewById(R.id.edit_start_year);
		EditText editStopYear = (EditText) findViewById(R.id.edit_stop_year);
		CheckBox editDateRangeCheckBox = (CheckBox) findViewById(R.id.check_edit_date_range);
		LinearLayout editStartYearLayout = (LinearLayout) findViewById(R.id.start_year_layout);
		LinearLayout editStopYearLayout = (LinearLayout) findViewById(R.id.stop_year_layout);
		CheckBox showTerritoriesCheckBox = (CheckBox) findViewById(R.id.check_show_territories);
		CheckBox showMintMarkCheckBox = (CheckBox) findViewById(R.id.check_show_mint_mark);
		CheckBox showBurnishedCheckBox = (CheckBox) findViewById(R.id.check_show_burnished_eagles);

		// Reset everything to the default state

        mEditDateRange = false;
		editDateRangeCheckBox.setChecked(mEditDateRange);
		editDateRangeCheckBox.setVisibility(View.GONE);

		editStartYearLayout.setVisibility(View.GONE);

		editStopYearLayout.setVisibility(View.GONE);

        // This one defaults to true, since a lot of people have missed this and most users are
        // likely to want it.
        mShowTerritories = true;
        showTerritoriesCheckBox.setChecked(mShowTerritories);
		showTerritoriesCheckBox.setVisibility(View.GONE);

        mShowMintMark = false;
        showMintMarkCheckBox.setChecked(mShowMintMark);
		showMintMarkCheckBox.setVisibility(View.GONE);

		uncheckAndHideAllMintMarkCheckBoxes();

        mShowBurnished = false;
        showBurnishedCheckBox.setChecked(mShowBurnished);
		showBurnishedCheckBox.setVisibility(View.GONE);

		// Update start and end years for the series
		mStartYear = mStartYears[mCoinTypeIndex];
		editStartYear.setText(Integer.toString(mStartYear));

		mStopYear = mStopYears[mCoinTypeIndex];
		editStopYear.setText(Integer.toString(mStopYear));
		
	}

    /**
     *  Updates the UI and the internal state based on what should be shown/set for a given coin
     *  type.
     *  @param coinType The type of the collection we are setting up the UI for
     *
     *  NOTE: This doesn't rely on the UI elements having listeners that update the internal state
     *        vars so that we can use this before the listeners have been created (or if no
     *        listeners will be created, in the case of testing.)
     */
    private void resetView(String coinType){

        resetView();

        CheckBox showMintMarkCheckBox = (CheckBox) findViewById(R.id.check_show_mint_mark);
        CheckBox editDateRangeCheckBox = (CheckBox) findViewById(R.id.check_edit_date_range);

        switch (mCoinType) {
            case "State Quarters":

                CheckBox showTerritoriesCheckBox = (CheckBox) findViewById(R.id.check_show_territories);

                showMintMarkCheckBox.setVisibility(View.VISIBLE);
                // Also show the 'Include D.C. and Territories' checkbox
                showTerritoriesCheckBox.setVisibility(View.VISIBLE);
                break;

            case "National Park Quarters":
            case "Presidential Dollars":
                // Only show the Show Mint Marks checkbox
                showMintMarkCheckBox.setVisibility(View.VISIBLE);
                break;

            case "First Spouse Gold Coins":
                // Don't show any check boxes
                break;

            case "American Eagle Silver Dollars":
                // Only show the check box to select whether to include the burnished coins
                CheckBox showBurnishedCheckBox = (CheckBox) findViewById(R.id.check_show_burnished_eagles);
                showBurnishedCheckBox.setVisibility(View.VISIBLE);
                break;

            default:
                // For all other coin types, display Show Mint Marks and Edit Date Range
                editDateRangeCheckBox.setVisibility(View.VISIBLE);
                showMintMarkCheckBox.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     *  Updates the UI from the internal state.
     */
    private void updateViewFromState(){

        CheckBox showMintMarkCheckBox = (CheckBox) findViewById(R.id.check_show_mint_mark);
        CheckBox editDateRangeCheckBox = (CheckBox) findViewById(R.id.check_edit_date_range);
        CheckBox showPCheckBox = (CheckBox) findViewById(R.id.check_include_p);
        CheckBox showDCheckBox = (CheckBox) findViewById(R.id.check_include_d);
        CheckBox showSCheckBox = (CheckBox) findViewById(R.id.check_include_s);
        CheckBox showOCheckBox = (CheckBox) findViewById(R.id.check_include_o);
        CheckBox showCCCheckBox = (CheckBox) findViewById(R.id.check_include_cc);
        LinearLayout editStartYearLayout = (LinearLayout) findViewById(R.id.start_year_layout);
        LinearLayout editStopYearLayout = (LinearLayout) findViewById(R.id.stop_year_layout);
        EditText editStartYear = (EditText) findViewById(R.id.edit_start_year);
        EditText editStopYear = (EditText) findViewById(R.id.edit_stop_year);

        // showMintMarkCheckBox will have already have been made visible
        showMintMarkCheckBox.setChecked(mShowMintMark);

        if (mShowMintMark) {

            showPCheckBox.setVisibility(View.VISIBLE);
            showPCheckBox.setChecked(mShowP);

            showDCheckBox.setVisibility(View.VISIBLE);
            showDCheckBox.setChecked(mShowD);

            showSCheckBox.setVisibility(View.VISIBLE);
            showSCheckBox.setChecked(mShowS);

            // Pro-tip, if you add a coin here, be sure to add it in the section below (validation)
            if(mCoinType.equals("Barber Dimes") || mCoinType.equals("Morgan Dollars") ||
                    mCoinType.equals("Barber Quarters") || mCoinType.equals("Barber Half Dollars")){
                showOCheckBox.setVisibility(View.VISIBLE);
                showOCheckBox.setChecked(mShowO);
            }
            if(mCoinType.equals("Morgan Dollars")){
                showCCCheckBox.setVisibility(View.VISIBLE);
                showCCCheckBox.setChecked(mShowCC);
            }
        } else {
            uncheckAndHideAllMintMarkCheckBoxes();
        }

        // editDateRangeCheckbox will already have been made visible
        editDateRangeCheckBox.setChecked(mEditDateRange);

        if (mEditDateRange) {
            editStartYearLayout.setVisibility(View.VISIBLE);

            editStopYearLayout.setVisibility(View.VISIBLE);

            Log.d(MainApplication.APP_NAME,"startYear: " + Integer.toString(mStartYear));

            editStartYear.setText(Integer.toString(mStartYear));

            editStopYear.setText(Integer.toString(mStopYear));

        } else {
            editStartYearLayout.setVisibility(View.GONE);

            editStopYearLayout.setVisibility(View.GONE);
        }
    }

    /**
     *  Helper function to uncheck and hide all of the mint mark check boxes
     */
	private void uncheckAndHideAllMintMarkCheckBoxes(){
		
		CheckBox showPCheckBox = (CheckBox) findViewById(R.id.check_include_p);
		CheckBox showDCheckBox = (CheckBox) findViewById(R.id.check_include_d);
		CheckBox showSCheckBox = (CheckBox) findViewById(R.id.check_include_s);
		CheckBox showOCheckBox = (CheckBox) findViewById(R.id.check_include_o);
		CheckBox showCCCheckBox = (CheckBox) findViewById(R.id.check_include_cc);

        mShowP = false;
        showPCheckBox.setChecked(mShowP);
        showPCheckBox.setVisibility(View.GONE);

        mShowD = false;
        showDCheckBox.setChecked(mShowD);
        showDCheckBox.setVisibility(View.GONE);

        mShowS = false;
        showSCheckBox.setChecked(mShowS);
        showSCheckBox.setVisibility(View.GONE);

        mShowO = false;
        showOCheckBox.setChecked(mShowO);
        showOCheckBox.setVisibility(View.GONE);

        mShowCC = false;
        showCCCheckBox.setChecked(mShowCC);
        showCCCheckBox.setVisibility(View.GONE);
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
	
		if(mStopYear > mStopYears[mCoinTypeIndex]){

			Toast.makeText(CoinPageCreator.this,
				"Highest possible ending year is " + String.valueOf(mStopYears[mCoinTypeIndex]) +
						".  Note, new years will automatically be added as they come.",
				Toast.LENGTH_LONG).show();

			mStopYear = mStopYears[mCoinTypeIndex];
            editStopYear.setText(Integer.toString(mStopYear));
			return false;
	    }
		if(mStopYear < mStartYears[mCoinTypeIndex]){
		
			Toast.makeText(CoinPageCreator.this,
				"Ending year can't be less than the collection starting year (" + String.valueOf(mStartYears[mCoinTypeIndex]) +
						")",
				Toast.LENGTH_SHORT).show();

			mStopYear = mStopYears[mCoinTypeIndex];
            editStopYear.setText(Integer.toString(mStopYear));
			return false;
		}
	
		if(mStartYear < mStartYears[mCoinTypeIndex]){

			Toast.makeText(CoinPageCreator.this,
				"Lowest possible starting year is " + String.valueOf(mStartYears[mCoinTypeIndex]),
				Toast.LENGTH_LONG).show();

			mStartYear = mStartYears[mCoinTypeIndex];
            editStartYear.setText(Integer.toString(mStartYear));
			return false;
		
		} else if(mStartYear > mStopYears[mCoinTypeIndex]){
		
			Toast.makeText(CoinPageCreator.this,
				"Starting year can't be greater than the collection ending year (" + String.valueOf(mStopYears[mCoinTypeIndex] +
						")"),
				Toast.LENGTH_SHORT).show();

			mStartYear = mStartYears[mCoinTypeIndex];
            editStartYear.setText(Integer.toString(mStartYear));
			return false;
		}
		
		// Finally, validate them with respect to each other
		if(mStartYear > mStopYear){
			Toast.makeText(CoinPageCreator.this, "Starting year can't be greater than the ending year", Toast.LENGTH_SHORT).show();

			mStartYear = mStartYears[mCoinTypeIndex];
            editStartYear.setText(Integer.toString(mStartYear));
			mStopYear = mStopYears[mCoinTypeIndex];
            editStopYear.setText(Integer.toString(mStopYear));
			return false;
		}
		
		// Yay, validation succeeded
		return true;
		
	}

    /**
     *  Helper function to call the make collection method corresponding to the coinType
     *  NOTE: This is public so we can use it with our current test bench
     *  @param coinType The type of the collection we are making tables for
     */
    public void makeTable(String coinType){

        switch (coinType) {
            case "Pennies":
                makePennyTable();
                break;
            case "Nickels":
                makeNickelsTable();
                break;
            case "Dimes":
                makeDimesTable();
                break;
            case "Quarters":
                makeQuartersTable();
                break;
            case "State Quarters":
                makeStateQuartersTable();
                break;
            case "National Park Quarters":
                makeNationalParkQuartersTable();
                break;
            case "Half-Dollars":
                makeHalfDollarsTable();
                break;
            case "Eisenhower Dollars":
                makeEisenhowerDollarsTable();
                break;
            case "Susan B. Anthony Dollars":
                makeSusanBAnthonyDollarsTable();
                break;
            case "Sacagawea/Native American Dollars":
                makeSacagaweaDollarsTable();
                break;
            case "Presidential Dollars":
                makePresidentialDollarsTable();
                break;
            case "Indian Head Cents":
                makeIndianHeadPenniesTable();
                break;
            case "Liberty Head Nickels":
                makeLibertyHeadNickelsTable();
                break;
            case "Buffalo Nickels":
                makeBuffaloNickelsTable();
                break;
            case "Mercury Dimes":
                makeMercuryDimesTable();
                break;
            case "Barber Dimes":
                makeBarberDimesTable();
                break;
            case "Barber Quarters":
                makeBarberQuartersTable();
                break;
            case "Standing Liberty Quarters":
                makeStandingLibertyQuartersTable();
                break;
            case "Barber Half Dollars":
                makeBarberHalfDollarsTable();
                break;
            case "Walking Liberty Half Dollars":
                makeWalkingLibertyHalfDollarsTable();
                break;
            case "Franklin Half Dollars":
                makeFranklinHalfDollarsTable();
                break;
            case "Peace Dollars":
                makePeaceDollarsTable();
                break;
            case "Morgan Dollars":
                makeMorganDollarsTable();
                break;
            case "American Eagle Silver Dollars":
                makeAmericanEagleSilverDollarsTable();
                break;
            case "First Spouse Gold Coins":
                makeFirstSpousesTable();
                break;
        }
    }
	
	private void makePennyTable(){
		
		boolean addedVdb = false;
		for(int i = mStartYear; i <= mStopYear; i++){
			
			// Support V.D.B.
			String newValue = Integer.toString(i);
			if(i == 1909 && !addedVdb){
				newValue = "1909 V.D.B";
			}
			
			if(i == 2009){
				// Add support for 2009 Lincoln Presidential Pennies
				addBicentennialPennies();
				continue;
			}

			if(mShowMintMark){
				if(mShowP){
					// The P was never on any Pennies
					mIdentifierList.add(newValue);
					mMintList.add("");
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}

			if(i != 1909 && i != 1910 && i != 1921 && i != 1923 && i != 1965 && i != 1966 && i != 1967){
				if(mShowMintMark && mShowD){
					mIdentifierList.add(newValue);
					mMintList.add(" D");
				}
			}

			if(i <= 1974 && i != 1922 && i != 1932 && i != 1933 && i != 1934 && (i < 1956 || i > 1967)){
				if(mShowMintMark && mShowS){
					mIdentifierList.add(newValue);
					mMintList.add(" S");
				}
			}
			
			// If we are adding in the VDB, turn this off
			if(i == 1909 && !addedVdb){
			    i--;
			    addedVdb = true;
		    }
		}
	}
	private void addBicentennialPennies() {
		String[] tempList = mContext.getResources().getStringArray(R.array.Bicentennial_Pennies_types);

		for(int i = 0; i < tempList.length; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(tempList[i]);
					mMintList.add("");
				}
				if(mShowD){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" D");
				}
			} else {
				mIdentifierList.add(tempList[i]);
				mMintList.add("");
			}
		}
	}

	private void makeNickelsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if(i == 2004){
				// 2004 Jefferson Presidential Nickels
				add2004WestwardJourneyNickels();
				continue;
			}
			
			if(i == 2005){
				// 2005 Jefferson Presidential Nickels
				add2005WestwardJourneyNickels();
				continue;
			}

			if(mShowMintMark){
				if(i != 1968 && i != 1969 && i != 1970){
					if(mShowP && i >= 1980){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" P");
					} else if(mShowP){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add("");
					}
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}

			if(i != 1965 && i != 1966 && i != 1967){
				if(mShowMintMark && mShowD){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" D");
				}
			}
			if(i <= 1970 && i != 1950 && (i < 1955 || i > 1967)){
				if(mShowMintMark && mShowS){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" S");
				}
			}
		}

	}
	private void add2004WestwardJourneyNickels() {
		String[] tempList = mContext.getResources().getStringArray(R.array.Westward_Journey_2004_Nickels_types);

		for(int i = 0; i < tempList.length; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" P");
				}
				if(mShowD){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" D");
				}
			} else {
				mIdentifierList.add(tempList[i]);
				mMintList.add("");
			}
		}
	}
	private void add2005WestwardJourneyNickels() {
		String[] tempList = mContext.getResources().getStringArray(R.array.Westward_Journey_2005_Nickels_types);

		for(int i = 0; i < tempList.length; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" P");
				}
				if(mShowD){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" D");
				}
			} else {
				mIdentifierList.add(tempList[i]);
				mMintList.add("");
			}
		}
	}
	private void makeDimesTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if(mShowMintMark){
				if(mShowP && i >= 1980){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" P");
				} else if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}

			if(i != 1965 && i != 1966 && i != 1967){
				if(mShowMintMark && mShowD){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" D");
				}
			}
			// if(i < 1975 && (i < 1956 || i > 1967)){
			// Greater than 1967 were only in proof sets
			if(i < 1975 && (i < 1956)){
				if(mShowMintMark && mShowS){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" S");
				}
			}
		}
	}
	
	private void makeQuartersTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			String newValue = Integer.toString(i);
			if(i == 1975 || i == 1976){
				newValue = "1776-1976";
			}
			if(i == 1933 || (i == 1976 && mStartYear != 1976))
				continue;

			if(mShowMintMark){
				if(mShowP && i >= 1980){
					mIdentifierList.add(newValue);
					mMintList.add(" P");
				} else if(mShowP) {
					mIdentifierList.add(newValue);
					mMintList.add("");
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}

			if(i != 1938 && (i < 1965 || i > 1967)){
				if(mShowMintMark && mShowD){
					mIdentifierList.add(newValue);
					mMintList.add(" D");
				}
			}
			if(i < 1955 && i != 1934 && i != 1949){
				if(mShowMintMark && mShowS){
					mIdentifierList.add(newValue);
					mMintList.add(" S");
				}
			}
		}
	}

	private void makeStateQuartersTable(){
		String[] tempList = mContext.getResources().getStringArray(R.array.State_Quarters_types);

		for(int i = 0; i < tempList.length; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" P");
				}
				if(mShowD){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" D");
				}
			} else {
				mIdentifierList.add(tempList[i]);
				mMintList.add("");
			}
		}
		if(mShowTerritories){
			// Add those to the list
			tempList = mContext.getResources().getStringArray(R.array.DC_and_US_Territories_types);
			for(int i = 0; i < tempList.length; i++){
				if(mShowMintMark){
					if(mShowP){
						mIdentifierList.add(tempList[i]);
						mMintList.add(" P");
					}
					if(mShowD){
						mIdentifierList.add(tempList[i]);
						mMintList.add(" D");
					}
				} else {
					mIdentifierList.add(tempList[i]);
					mMintList.add("");
				}
			}
		}
	}
	private void makeNationalParkQuartersTable(){
		String[] tempList = mContext.getResources().getStringArray(R.array.State_Parks_types);
		for(int i = 0; i < tempList.length; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" P");
				}
				if(mShowD){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" D");
				}
			} else {
				mIdentifierList.add(tempList[i]);
				mMintList.add("");
			}
		}
	}	
	private void makeHalfDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			String newValue = Integer.toString(i);
			if(i == 1975 || i == 1976){
				newValue = "1776-1976";
			}
			if(i == 1976 && mStartYear != 1976)
				continue;


			if(mShowMintMark){
				if(i < 1968 || i > 1970){
					if(mShowP && i >= 1980){
						mIdentifierList.add(newValue);
						mMintList.add(" P");
					} else if(mShowP) {
						mIdentifierList.add(newValue);
						mMintList.add("");
					}
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}

			if(i != 1965 && i != 1966 && i != 1967){
				if(mShowMintMark && mShowD){
					mIdentifierList.add(newValue);
					mMintList.add(" D");
				}
			}
		}
	}

	private void makeEisenhowerDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			String newValue = Integer.toString(i);
			if(i == 1975 || i == 1976){
				newValue = "1776-1976";
			}
			if(i == 1976 && mStartYear != 1976)
				continue; // (what if start date is 1976)


			if(mShowMintMark){
				if(mShowP) {
					mIdentifierList.add(newValue);
					mMintList.add("");
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}

			if(mShowMintMark && mShowD){
				mIdentifierList.add(newValue);
				mMintList.add(" D");
			}

			//if(i < 1973){
			//	if(mShowS){
			//	    mIdentifierList.add(newValue);
			//	    mMintList.add(" S");
			//	}
			//}
		}
	}

	private void makeSusanBAnthonyDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			if(i > 1981 && i < 1999)
				continue;

			if(mShowMintMark){
				// 1979 showed the P mint mark
				if(mShowP && i >= 1979){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" P");
				} else if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}

			if(mShowMintMark && mShowD){
				mIdentifierList.add(Integer.toString(i));
				mMintList.add(" D");
			}
			if(i != 1999){
				if(mShowMintMark && mShowS){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" S");
				}
			}
		}
	}

	private void makeSacagaweaDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" P");
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}

			if(mShowMintMark && mShowD){
				mIdentifierList.add(Integer.toString(i));
				mMintList.add(" D");
			}
		}
	}


	private void makePresidentialDollarsTable(){
		String[] tempList = mContext.getResources().getStringArray(R.array.Presidential_Coins_type);
		for(int i = 0; i < tempList.length; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" P");
				}
				if(mShowD){
					mIdentifierList.add(tempList[i]);
					mMintList.add(" D");
				}
			} else {
				mIdentifierList.add(tempList[i]);
				mMintList.add("");
			}
		}
	}
	
	private void makeIndianHeadPenniesTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			
			if(mShowMintMark){
				if(mShowP){
					// A few special ones
					// 1864 Copper
					// 1864 Bronze
					// 1864 L
					
					if(i == 1864){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" Copper");
						
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" Bronze");
						
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" L");

					} else {
						mIdentifierList.add(Integer.toString(i));
						mMintList.add("");
					}
				}
				if(mShowS){
					if(i == 1908 || i == 1909){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" S");
					}					
				}
				
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}
	private void makeLibertyHeadNickelsTable(){
		
		boolean added1883WithCents = false;
		for(int i = mStartYear; i <= mStopYear; i++){
			
			String newValue = Integer.toString(i);
			
			if(i == 1883){
				if(!added1883WithCents){
				    newValue = "1883 w/ Cents";
				} else {
				    newValue = "1883 w/o Cents";
				}
			}

			if(mShowMintMark){
				
				if(mShowP){
					mIdentifierList.add(newValue);
					mMintList.add("");
				}
				
				if(i == 1912){
					if(mShowD){
					    mIdentifierList.add(newValue);
					    mMintList.add(" D");
				    }
					if(mShowS){
						mIdentifierList.add(newValue);
					    mMintList.add(" S");
					}
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}
			
			if(i == 1883 && !added1883WithCents){
				added1883WithCents = true;
				i--;
			}
		}
	}
	
	private void makeBuffaloNickelsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if(i == 1922 || i == 1932 || i == 1933){
				continue;
			}
			
			if(mShowMintMark){
				if(mShowP){
					if(i != 1931 && i != 1938){
						if(i == 1913){
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" Type 1");
							
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" Type 2");
						} else {
						    mIdentifierList.add(Integer.toString(i));
						    mMintList.add("");
						}
					}
				}
				if(mShowD){
					if(i != 1921 && i != 1923 && i != 1930 && i != 1931){
						if(i == 1913){
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" D Type 1");
							
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" D Type 2");
						} else {
						    mIdentifierList.add(Integer.toString(i));
						    mMintList.add(" D");
						}
					}
				}
				if(mShowS){
					if(i != 1934 && i != 1938){
						if(i == 1913){
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" S Type 1");
							
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" S Type 2");
						} else {
						    mIdentifierList.add(Integer.toString(i));
						    mMintList.add(" S");
						}
					}					
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}

	
	private void makeMercuryDimesTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if(i == 1922 || i == 1932 || i == 1933)
				continue;

			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}


			if(i != 1923 && i != 1930){
				if(mShowMintMark && mShowD){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" D");
				}
			}

			if(i != 1921 && i != 1934){
				if(mShowMintMark && mShowS){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" S");
				}
			}
		}
	}
	private void makeBarberDimesTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
				if(mShowD){
					if( (i >= 1906 && i <= 1912) || i == 1914 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" D");
					}
				}
				if(mShowS){
					if(i != 1894){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" S");
					}					
				}
				if(mShowO){
					if(i != 1904 && i < 1910 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" O");
					}							
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}
	private void makeBarberQuartersTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
				if(mShowD){
					if( i >= 1906 && i != 1912 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" D");
					}
				}
				if(mShowS){
					if(i != 1904 && i != 1906 && i != 1910 && i != 1916){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" S");
					}					
				}
				if(mShowO){
					if(i <= 1909){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" O");
					}							
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}
    private void makeStandingLibertyQuartersTable(){
		
    	boolean addedTypeOne = false;
		for(int i = mStartYear; i <= mStopYear; i++){
			
			if(i == 1922){
				continue;
			}
			
			String newValue;
			if(i == 1917){
				if(!addedTypeOne){
					newValue = "1917 Type 1";
				} else {
					newValue = "1917 Type 2";
				}
			} else {
				newValue = Integer.toString(i);
			}

			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(newValue);
					mMintList.add("");
				}
				
				if(i != 1916 && i != 1921 && i != 1925){
					if(mShowD && i != 1923 && i != 1930){
					    mIdentifierList.add(newValue);
					    mMintList.add(" D");
				    }
					if(mShowS){
						mIdentifierList.add(newValue);
					    mMintList.add(" S");
					}
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}
			
			if(i == 1917 && !addedTypeOne){
				addedTypeOne = true;
				i--;
			}
		}
	}
	
	private void makeBarberHalfDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
				if(mShowD){
					if( i >= 1906 && i != 1909 && i != 1910 && i != 1914){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" D");
					}
				}
				if(mShowS){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" S");
				}
				if(mShowO){
					if( i <= 1909 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" O");
					}							
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}
    private void makeWalkingLibertyHalfDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if(i == 1922 || i == 1924 || i == 1925 || i == 1926 ||
			   i == 1930 || i == 1931 || i == 1932){
				continue;
			}
			
			if(mShowMintMark){
				if(mShowP){
					if( (i < 1923 || i > 1933) ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add("");
					}
				}
				if(mShowD){
					if( (i < 1923 || i > 1928) && i != 1933 && i != 1940){
						if(i == 1917){
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" D Obv");
							
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" D Rev");
						} else {
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" D");
						}
					}
				}
				if(mShowS){
					if(i != 1938 && i != 1947){
						if(i == 1917){
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" S Obv");
							
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" S Rev");
						} else {
							mIdentifierList.add(Integer.toString(i));
							mMintList.add(" S");
						}
					}					
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}
	
	private void makeFranklinHalfDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){
			
			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add("");
				}
				if(mShowD){
					if( i != 1955 && i != 1956 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" D");
					}
				}
				if(mShowS){
					if(i != 1948 && i != 1950 && i <= 1954 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" S");
					}					
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}	
    private void makeMorganDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			if( (i > 1904 && i < 1921) ){
				continue;
			}
			
			if(mShowMintMark){
				if(mShowP){
					if(i == 1878){
						mIdentifierList.add("1878 8 Feathers");
					    mMintList.add("");
						mIdentifierList.add("1878 7 Feathers");
					    mMintList.add("");
					} else if(i != 1895){
					    mIdentifierList.add(Integer.toString(i));
					    mMintList.add("");
					}
				}
				if(mShowD){
					if(i == 1921){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" D");
					}
				}
				if(mShowO){
					if(i != 1878 && i != 1921){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" O");
					}
				}
				if(mShowCC){
					if( i != 1886 && i != 1887 && i != 1888 && i <= 1893 ){
						mIdentifierList.add(Integer.toString(i));
						mMintList.add(" CC");
					}
				}
				if(mShowS){
					mIdentifierList.add(Integer.toString(i));
					mMintList.add(" S");
				}
			} else {
				mIdentifierList.add(Integer.toString(i));
				mMintList.add("");
			}
		}
	}
    private void makePeaceDollarsTable(){
		
		for(int i = mStartYear; i <= mStopYear; i++){
			
			if(i >= 1929 && i <= 1933){
				continue;
			}
			
			String newValue = Integer.toString(i);

			if(mShowMintMark){
				if(mShowP){
					mIdentifierList.add(newValue);
					mMintList.add("");
				}
				if(mShowD){
					if(i != 1921 && i != 1924 && i != 1925 && i != 1928 && i != 1935){
				        mIdentifierList.add(newValue);
				        mMintList.add(" D");
				    }
				}
				if(mShowS){
					if(i != 1921){
					    mIdentifierList.add(newValue);
				        mMintList.add(" S");
					}
				}
			} else {
				mIdentifierList.add(newValue);
				mMintList.add("");
			}
		}
	}

    private void makeFirstSpousesTable(){
		String[] tempList = mContext.getResources().getStringArray(R.array.First_Spouse_types);
		for(int i = 0; i < tempList.length; i++){
			mIdentifierList.add(tempList[i]);
			mMintList.add("");
		}
	}
	
    private void makeAmericanEagleSilverDollarsTable(){
		for(int i = mStartYear; i <= mStopYear; i++){

			mIdentifierList.add(Integer.toString(i));
			mMintList.add("");
			
			if(mShowBurnished){
				if(i == 2006){
					mIdentifierList.add("2006 W Burnished");
					mMintList.add("");
				}
			
				else if(i == 2007){
					mIdentifierList.add("2007 W Burnished");
					mMintList.add("");
				}
			
				else if(i == 2008){
					mIdentifierList.add("2008 W Burnished");
					mMintList.add("");
				}
				else if(i == 2011){
					mIdentifierList.add("2011 W Burnished");
					mMintList.add("");
				}
			}
		}
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
    public void testSetInternalState(boolean showMintMark, boolean editDateRange,
                                     boolean showTerritories, boolean showBurnished,
                                     boolean showP, boolean showD, boolean showS, boolean showO,
                                     boolean showCC, int startYear, int stopYear) {

        mShowMintMark = showMintMark;
        mEditDateRange = editDateRange;
        mShowTerritories = showTerritories;
        mShowBurnished = showBurnished;
        mShowP = showP;
        mShowD = showD;
        mShowS = showS;
        mShowO = showO;
        mShowCC = showCC;
        mStartYear = startYear;
        mStopYear = stopYear;
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