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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.Collections;

/** Activity for managing each collection page
 *
 * http://developer.android.com/resources/tutorials/views/hello-gridview.html
 */
public class CollectionPage extends AppCompatActivity {
	private String mCollectionName;
	private String mCoinType;
	private int mImageIdentifier;
	private int mActionBarImage;
	private final ArrayList<String> mIdentifierList = new ArrayList<>();
	private final ArrayList<String> mMintList = new ArrayList<>();
	private ArrayList<Boolean> mInCollectionList = new ArrayList<>();
	private CoinSlotAdapter mCoinSlotAdapter;
	private Bundle mSavedInstanceState = null;
    private Resources mRes;

    // Intent Argument Keywords
	public final static String COLLECTION_NAME  = "Collection_Name";
    public final static String COIN_TYPE        = "Coin_Type";
    public final static String IMAGE_IDENTIFIER = "Image_Identifier";
    public final static String COIN_TYPE_IMG    = "Coin_Type_Img";
    public final static String VIEW_INDEX       = "view_index";
    public final static String VIEW_POSITION    = "view_position";
	
	private int mDisplayType = MainApplication.SIMPLE_DISPLAY;
	
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        mRes = getResources();

	    // Save off this bundle so that after the database is open we can use it
	    // to get the previous CoinSlotAdapter, if present
	    mSavedInstanceState = savedInstanceState;
	            
        // Need to get the coin type from the intent that started this process
        Intent callingIntent = getIntent();
        mCollectionName = callingIntent.getStringExtra(COLLECTION_NAME);
        mCoinType = callingIntent.getStringExtra(COIN_TYPE);
        mImageIdentifier = callingIntent.getIntExtra(IMAGE_IDENTIFIER, 0);
        mActionBarImage = callingIntent.getIntExtra(COIN_TYPE_IMG, 0);
        
        if(callingIntent.hasExtra(VIEW_INDEX)){
        	mViewIndex = callingIntent.getIntExtra(VIEW_INDEX, 0);
        	mViewPosition = callingIntent.getIntExtra(VIEW_POSITION, 0);
        }
	    
        // Update the title
        // http://stackoverflow.com/questions/2198410/how-to-change-title-of-activity-in-android
	    this.setTitle(mCollectionName);
	    
	    // Tell the user they can now lock the collections
    	// Check whether it is the users first time using the app
    	SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
    	if(mainPreferences.getBoolean("first_Time_screen3", true)){
    		// Show the user how to do everything
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage(mRes.getString(R.string.tutorial_add_to_and_lock_collection))
    		       .setCancelable(false)
    		       .setPositiveButton(mRes.getString(R.string.okay), new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		               dialog.cancel();
    		               SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
    		       		   SharedPreferences.Editor editor = mainPreferences.edit();
    		       		   editor.putBoolean("first_Time_screen3", false);
    		       		   editor.commit(); // .apply() in later APIs
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    	}

        // TODO We use to break this up with an AsyncTask, but doesn't look like that's needed
        // anymore.  Consider combining functions.
    	finishViewSetup();
	}

	/**
	 * Finish setting up the view
	 */
    private void finishViewSetup(){
    	
		DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
		dbAdapter.open();
    	
        // Determine whether we should show the advanced view or the basic view
    	mDisplayType = dbAdapter.fetchTableDisplay(mCollectionName);
    	
	    // Update the icon
	    ActionBar actionBar = this.getSupportActionBar();
	    actionBar.setIcon(mActionBarImage);
	    
	    // Set the actionbar so that clicking the icon takes you back
	    // SO 1010877
	    actionBar.setDisplayHomeAsUpEnabled(true);
	        	
    	GridView gridview = null;
    	ListView listview = null;
    	
    	if(mDisplayType == MainApplication.SIMPLE_DISPLAY) {

	        setContentView(R.layout.standard_collection_page);
	        
		    gridview = (GridView) findViewById(R.id.standard_collection_page);
	        
    	} else if(mDisplayType == MainApplication.ADVANCED_DISPLAY){

            setContentView(R.layout.advanced_collection_page);
            
        	listview = (ListView) findViewById(R.id.advanced_collection_page);
        	
        	// Make it so that the elements in the listview cells can get focus
            listview.setItemsCanFocus(true);
    	}
    	
        Cursor resultCursor = dbAdapter.getAllIdentifiers(mCollectionName);
        Cursor resultCursor2 = dbAdapter.getInCollectionInfo(mCollectionName);
        // THanks! http://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
        if (resultCursor.moveToFirst() && resultCursor2.moveToFirst()){
            do{
            	mIdentifierList.add(resultCursor.getString(resultCursor.getColumnIndex("coinIdentifier")));
            	mMintList.add(resultCursor.getString(resultCursor.getColumnIndex("coinMint")));
            	int inCollectionInt = resultCursor2.getInt(resultCursor2.getColumnIndex("inCollection"));
            	mInCollectionList.add(inCollectionInt == 1);
            	
            }while(resultCursor.moveToNext() && resultCursor2.moveToNext());
        }
        resultCursor.close();
        resultCursor2.close();
        
        // In the advanced case, the mInCollectionList that we generated could be inaccurate if we
        // had unsaved data and then a screen rotation or something like that occurred.  We have to
        // do this same stuff for the advanced specific information below, but we also need to do it
        // to the mInCollectionList since in the advanced view the coin additions/deletions are tied
        // to the save button and haven't been propagated back yet.  It'd be better to architect this
        // a bit better, but this is being hacked on so for now just go with it.  :)  TODO
    	if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
    		
    		if(mSavedInstanceState != null){

    			boolean[] inCollectionBools = mSavedInstanceState.getBooleanArray("in_collection");

    			assert inCollectionBools != null;

    			mInCollectionList = new ArrayList<>();
				for (boolean inCollectionBool : inCollectionBools) {
					mInCollectionList.add(inCollectionBool);
				}
    		}
    	}

		switch (mCoinType) {
			case "State Quarters": {
				// Need to get the array of the correct images
				String[] stateQuarterIdentifierList = getResources().getStringArray(R.array.State_Quarters_Regular_Images);

				if (mIdentifierList.size() == 56 || mIdentifierList.size() == 112) {
					// Then we need to add in the D.C. and Territory images
					ArrayList<String> tempArrayList = new ArrayList<>();
					Collections.addAll(tempArrayList, stateQuarterIdentifierList);

					String[] tempStringArray = getResources().getStringArray(R.array.DC_and_US_Territories_Regular_Images);
					Collections.addAll(tempArrayList, tempStringArray);

                    // TODO mIdentifierList should equal tempArrayList at this point

					// http://www.coderanch.com/t/405818/java/java/casting-object-array-string-array
					mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, tempArrayList.toArray(new String[tempArrayList.size()]));
				} else {
					mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, stateQuarterIdentifierList);
				}
				break;
			}
			case "National Park Quarters": {
				// Need to get the array of the correct images
				String[] stateQuarterIdentifierList = getResources().getStringArray(R.array.State_Parks_Regular_Images);
				mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, stateQuarterIdentifierList);

				break;
			}
			case "Presidential Dollars":
				// Need to get the array of the correct images
				String[] presidentialDollarIdentifierList = getResources().getStringArray(R.array.Presidential_Coins_Regular_Images);
				mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, presidentialDollarIdentifierList);

				break;
			case "Pennies":
				// Need to get the array of additional images
				String[] bicentennialPennyIdentifierList = getResources().getStringArray(R.array.Bicentennial_Pennies_Regular_Images);
				mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, bicentennialPennyIdentifierList);

				break;
			case "Nickels":
				// Need to get the arrays of additional images
				String[] westwardNickelIdentifierList1 = getResources().getStringArray(R.array.Westward_Journey_2004_Nickels_Regular_Images);
				ArrayList<String> tempArrayList = new ArrayList<>();
				Collections.addAll(tempArrayList, westwardNickelIdentifierList1);

				String[] tempStringArray = getResources().getStringArray(R.array.Westward_Journey_2005_Nickels_Regular_Images);
				Collections.addAll(tempArrayList, tempStringArray);

                // Note: unlike for other coin types, it isn't expected
                // that tempArrayList.size() == mIdentifierList.size()

				mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, tempArrayList.toArray
                        (new String[tempArrayList.size()]));

				break;
			case "First Spouse Gold Coins":
				// Need to get the array of the correct images
				String[] firstSpouseDollarIdentifierList = getResources().getStringArray(R.array.First_Spouse_images);
				mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList, firstSpouseDollarIdentifierList);

				break;
			default:
				// No special images required
				mCoinSlotAdapter = new CoinSlotAdapter(this, mCoinType, mCollectionName, mIdentifierList, mMintList, mImageIdentifier, mInCollectionList);
				break;
		}
    	  
    	// If we are the advanced view we need to set up the coin slot adapters advanced tables
    	if(mDisplayType == MainApplication.ADVANCED_DISPLAY){

    		if(mSavedInstanceState == null){
    		    // This is the first time the page has loaded, so we haven't
    			// loaded this info yet
    			
    	        ArrayList<Integer> grades = new ArrayList<>();
    	        ArrayList<Integer> quantities = new ArrayList<>();
    	        ArrayList<String> notes = new ArrayList<>();
    	        
    		    // We need to give the coin adapter the extra info
    	        Cursor advResultCursor = dbAdapter.getAdvInfo(mCollectionName);
    	        
                if (advResultCursor.moveToFirst()){
                    do{
                	    grades.add(advResultCursor.getInt(advResultCursor.getColumnIndex("advGradeIndex")));
                	    quantities.add(advResultCursor.getInt(advResultCursor.getColumnIndex("advQuantityIndex")));
                	    notes.add(advResultCursor.getString(advResultCursor.getColumnIndex("advNotes")));
                    }while(advResultCursor.moveToNext());
                } else {
            	    Log.e(MainApplication.APP_NAME, "cursor.moveToFirst() returned false - getAdvInfo");
                }
                advResultCursor.close();
                
    			// Mark everything as not having changed
    			boolean[] hasChanged = new boolean[grades.size()];
    			for(int i = 0; i < grades.size(); i++){
    				hasChanged[i] = false;
    			}
                mCoinSlotAdapter.setAdvancedLists(grades, quantities, notes, hasChanged);
    		    
    	    } else {
    	    	
    	    	// We have already loaded the advanced lists, so use those instead.
    	        // That way we have all of the state from before the page loaded.
    	    	// yay 

				if(BuildConfig.DEBUG) {
					Log.e(MainApplication.APP_NAME, "Successfully restored previous state");
				}
    	    	
    	        ArrayList<Integer> grades = mSavedInstanceState.getIntegerArrayList("coin_grades");
    	        ArrayList<Integer> quantities = mSavedInstanceState.getIntegerArrayList("coin_quantities");
    	        ArrayList<String> notes = mSavedInstanceState.getStringArrayList("coin_notes");
    	        boolean[] hasChanged = mSavedInstanceState.getBooleanArray("change_list");
    	        mViewIndex = mSavedInstanceState.getInt(VIEW_INDEX);
    	        mViewPosition = mSavedInstanceState.getInt(VIEW_POSITION);

				if(BuildConfig.DEBUG) {

					if(grades == null || quantities == null || notes == null || hasChanged == null ||
					   grades.size() != hasChanged.length) {

						throw new AssertionError("mSavedInstanceState didn't contain the values expected");

					}
				}
    	        
    	        // Search through the hasChanged history and see whether we should
    	        // re-display the "Unsaved Changes" view
    	        
    			for(int i = 0; i < grades.size(); i++){
    				if(hasChanged[i]){
    					this.showUnsavedTextView();
    					break;
    				}
    			}
    	    	
    	    	mCoinSlotAdapter.setAdvancedLists(grades, quantities, notes, hasChanged);
    	    }
    	} else {
    		// Simple view, get the gridview location
    		if(mSavedInstanceState != null){
    	        mViewIndex = mSavedInstanceState.getInt(VIEW_INDEX);
    	        mViewPosition = mSavedInstanceState.getInt(VIEW_POSITION);
    		}
    	}

        dbAdapter.close();
        dbAdapter = null;

    	OnScrollListener scrollListener = new OnScrollListener(){
	        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	             // Auto-generated method stub
	        	// TODO Something we can put here that isn't slow but fixes the scrolling issue
	        	// Anything we put here is going to be hit a lot :(
	        }
	        public void onScrollStateChanged(AbsListView view, int scrollState) {
	          
	          if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
	        	  // Refresh the view, fixing any layout issues
	        	  mCoinSlotAdapter.notifyDataSetChanged();
	          }
	          
	          // If this is the advanced view, we want to hide the soft keyboard if it exists
	          // This only gets called when a scroll starts (SCROLL_STATE_TOUCH_SCROLL),
	          // when the person has flung the view (SCROLL_STATE_FLING), and when the
	          // scrolling comes to an end (SCROLL_STATE_IDLE), so this won't cause any performance
	          // issues
	          // TODO Is there an easy way to determine if the soft keyboard is shown?
			  InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			  imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	        }
	    };

		if(mDisplayType == MainApplication.SIMPLE_DISPLAY){
	       
    		// Apply the adapter to handle each entry in the grid
    		gridview.setAdapter(mCoinSlotAdapter);
    		
	    	// Restore the position in the list that the user was at
    		// (or go to the default of the first item)
	    	gridview.setSelection(mViewIndex);
    		
    		// Set the scroll listener so that the view re-adjusts to the new view
    		gridview.setOnScrollListener(scrollListener);
	        
    	} else if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
    		// Apply the adapter to handle each entry in the list
    		listview.setAdapter(mCoinSlotAdapter);
    		
	    	// Restore the position in the list that the user was at
    		// (or go to the default of the first item)
	    	listview.setSelectionFromTop(mViewIndex, mViewPosition);
    		
    		// Set the scroll listener so that the view re-adjusts to the new view
    		listview.setOnScrollListener(scrollListener);

    	}
    	
	    // Set the onClick listener that will handle changing the coin state
    	// Note, for the advanced view we have to put the click listener on the
    	// coin image, so we can't do it here. boo
    	
    	if(mDisplayType == MainApplication.SIMPLE_DISPLAY){
    		
    	    gridview.setOnItemClickListener(new OnItemClickListener() {
    	    	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	    		// Need to check whether the collection is locked
    	    		SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
	        	
    	    		if(mainPreferences.getBoolean(mCollectionName + "_isLocked", false)){
    	    			// Collection is locked
    	    			Context context = getApplicationContext();
    	    			CharSequence text = "Collection is currently locked, hit the 'Edit' action to unlock";
    	    			int duration = Toast.LENGTH_SHORT;

    	    			Toast toast = Toast.makeText(context, text, duration);
    	    			toast.show();
    	    		} else {
    	    			// Preference doesn't exist or Collection is unlocked
    	    			
    	    			DatabaseAdapter dbAdapter = new DatabaseAdapter(CollectionPage.this);
    	    			dbAdapter.open();
    	    			
    	    			dbAdapter.updateInfo(mCollectionName, mIdentifierList.get(position), mMintList.get(position));
    	    			
    	    			dbAdapter.close();
    	    			
    	    			// Update the mCoinSlotAdapters copy of the coins in this collection
    	    			boolean oldValue = mCoinSlotAdapter.inCollectionList.get(position);
    	    			mCoinSlotAdapter.inCollectionList.set(position, !oldValue);
    	    			
	    			    // And have the adapter redraw with this new info

    	    			mCoinSlotAdapter.notifyDataSetChanged();
    	    		}
    	    	}
    	    });
    	}
	    
	}
    
    public void showUnsavedTextView() {
    	
    	TextView unsavedMessageView = (TextView) findViewById(R.id.unsaved_message_textview);
    	
    	unsavedMessageView.setVisibility(View.VISIBLE);
    }
    
    private void hideUnsavedTextView() {
    	
    	TextView unsavedMessageView = (TextView) findViewById(R.id.unsaved_message_textview);
    	
    	unsavedMessageView.setVisibility(View.GONE);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
                
		inflater.inflate(R.menu.collection_page_menu_all, menu);
        
        // Need to check the preferences to see whether the collection is locked or unlocked
    	SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
    	MenuItem item = menu.findItem(R.id.lock_unlock_collection);
    	
    	if(mainPreferences.getBoolean(mCollectionName + "_isLocked", false)){
    		// Current Locked, set text to unlock it
    		item.setTitle(R.string.unlock_collection);
    	} else {
    		// Currently unlocked, set text to lock it
    		// Default is unlocked
    		if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
    		    item.setTitle(R.string.lock_collection_adv);
    		} else {
    		    item.setTitle(R.string.lock_collection);
    		}
    	}
    	
    	// If we are in the advanced mode, we need to show the save and switch view
        MenuItem changeViewItem = menu.findItem(R.id.change_view);

        if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
        	changeViewItem.setTitle(R.string.simple_view_string);
            //saveItem.setVisible(true);
        } else {
        	changeViewItem.setTitle(R.string.advanced_view_string);
        	//saveItem.setVisible(false);
        }

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {

        case R.id.lock_unlock_collection:
        	
        	// Need to check the preferences to see whether the collection is locked or unlocked
        	SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        	SharedPreferences.Editor editor = mainPreferences.edit();
        	
        	boolean isLocked = mainPreferences.getBoolean(mCollectionName + "_isLocked", false);
        	
        	// If we are going from unlocked to lock in advance mode, we need to save the
        	// changes the user may have made (if any)
        	if(mDisplayType == MainApplication.ADVANCED_DISPLAY &&
					!isLocked &&
					this.doUnsavedChangesExist()){
        		
            	// In the advanced display case, we also need to save

        		// TODO Show some kind of spinner
        		// Save everything in the database
        		boolean finishedSuccessfully = true;
        		
				DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
				dbAdapter.open();

        		for(int i = 0; i < mCoinSlotAdapter.advancedGrades.size(); i++){
        			if(mCoinSlotAdapter.indexHasChanged[i]){

        				String coinIdentifier = mIdentifierList.get(i);
        				String coinMint = mMintList.get(i);
        				Integer grade = mCoinSlotAdapter.advancedGrades.get(i);
        				Integer quantity = mCoinSlotAdapter.advancedQuantities.get(i);
        				String notes = mCoinSlotAdapter.advancedNotes.get(i);
        				int inCollection = mCoinSlotAdapter.inCollectionList.get(i) ? 1 : 0;

        				if(dbAdapter.updateAdvInfo(
        						mCollectionName,
        						coinIdentifier,
        						coinMint,
								grade,
								quantity,
        						notes,
        						inCollection) != 1){
        					finishedSuccessfully = false;
        					// Keep going, though    	
        					continue;
        				}
        				// Mark this data as being unchanged
        				mCoinSlotAdapter.indexHasChanged[i] = false;
        			}
        		}
        		
        		dbAdapter.close();

        		if(finishedSuccessfully){
        			// Hide the unsaved changes view
        			Context context = this.getApplicationContext();
        			CharSequence text = "Saved changes successfully.  :)";
        			int duration = Toast.LENGTH_SHORT;

        			Toast toast = Toast.makeText(context, text, duration);
        			toast.show();

        			this.hideUnsavedTextView();
        		} else {

        			// Hide the unsaved changes view
        			Context context = this.getApplicationContext();
        			CharSequence text = "Error saving everything to the database... Maybe try again?";
        			int duration = Toast.LENGTH_SHORT;

        			Toast toast = Toast.makeText(context, text, duration);
        			toast.show();
        		}
        	}
    	
        	if(isLocked){
        		// Locked, change to unlocked
        		editor.putBoolean(mCollectionName + "_isLocked", false);
        		// Change the text for next time
        		if(mDisplayType == MainApplication.SIMPLE_DISPLAY){
        		    item.setTitle(R.string.lock_collection);
        		}
            	// Don't update in the advance case, because we are going to blow
            	// away this
        	} else {
        		// Unlocked or preference doesn't exist, change preference to locked
        		editor.putBoolean(mCollectionName + "_isLocked", true);
        		// Change the text for next time
        		if(mDisplayType == MainApplication.SIMPLE_DISPLAY){
        		    item.setTitle(R.string.unlock_collection);
        		}
        	}
        	
        	// Save changes
        	// TODO Consider not saving these if in advance mode and the db update
        	// fails below
        	editor.commit(); // .apply() in later APIs
        	
        	if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
        		// We need to restart the view so we can show the locked
        		// view.  Also, at this point there are no unsaved changes
        		
        		// Save the position that the user was at for convenience
            	// http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
            	ListView listview = (ListView) findViewById(R.id.advanced_collection_page);
            	int index = listview.getFirstVisiblePosition();
            	View v = listview.getChildAt(0);
            	int top = (v == null) ? 0 : v.getTop();
            	
        		Intent callingIntent = getIntent();
        		callingIntent.putExtra(VIEW_INDEX, index);
        		callingIntent.putExtra(VIEW_POSITION, top);

        		finish();
        		startActivity(callingIntent);
        	}
        	
        	return true;
        	
        case R.id.change_view:

            if(mDisplayType == MainApplication.SIMPLE_DISPLAY){
            	// Setup the advanced view
            		
    			DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
    			dbAdapter.open();
            		
            	dbAdapter.updateTableDisplay(mCollectionName, MainApplication.ADVANCED_DISPLAY) ;

            	dbAdapter.close();
            		
            	// Save the position that the user was at for convenience
               	// http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
               	GridView gridview = (GridView) findViewById(R.id.standard_collection_page);
               	int index = gridview.getFirstVisiblePosition();
               	View v = gridview.getChildAt(0);
               	int top = (v == null) ? 0 : v.getTop();
                	
            	Intent callingIntent = getIntent();
            	callingIntent.putExtra(VIEW_INDEX, index);
            	callingIntent.putExtra(VIEW_POSITION, top);
            		
            	// Restart the activity
            	finish();
            	startActivity(callingIntent);
            		
            	return true;
            		
            } else if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
            	// Setup the basic view
            		
            	// We need to see if there are any unsaved changes, and if so,
            	// present an alert
                		
            	if(this.doUnsavedChangesExist()){
                		
            		showUnsavedChangesAlert();
                   	return true;
            	}
               		
            	// The user doesn't have any unsaved changes
            		
            	// TODO Try catch
               		
    			DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
    			dbAdapter.open();
               		
            	dbAdapter.updateTableDisplay(mCollectionName, MainApplication.SIMPLE_DISPLAY) ;
            		
            	dbAdapter.close();
            		
            	// Save the position that the user was at for convenience
               	// http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
               	ListView listview = (ListView) findViewById(R.id.advanced_collection_page);
               	int index = listview.getFirstVisiblePosition();
               	View v = listview.getChildAt(0);
               	int top = (v == null) ? 0 : v.getTop();
                	
            	Intent callingIntent = getIntent();
            	callingIntent.putExtra(VIEW_INDEX, index);
            	callingIntent.putExtra(VIEW_POSITION, top);

            	// Restart the activity
            	finish();
            	startActivity(callingIntent);
            	return true;
            }
            	
            // Shouldn't get here
            return true;
        	
        case android.R.id.home:
        	// To support having a back arrow on the page
        	
       		if(this.doUnsavedChangesExist()){
            	
       			// If we have unsaved changes, don't go back right away but
       			// instead let the user decide
       			showUnsavedChangesMessage();
       			
       		} else {
        	    this.onBackPressed();
       		}
        	return true;
        	
        default:
            return super.onOptionsItemSelected(item);
        	
        }
    }
    
    private boolean doUnsavedChangesExist(){
    	
    	if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
		    // There are probably better ways to do this check, but this one is easy
		    TextView unsavedChangesView = (TextView) this.findViewById(R.id.unsaved_message_textview);
		    return (unsavedChangesView.getVisibility() == View.VISIBLE);
    	} else {
    		// In the simple view, there will never be unsaved changes
    		return false;
    	}
    }
        
    private void showUnsavedChangesAlert() {
    	
       	AlertDialog.Builder builder = new AlertDialog.Builder(this);
       	builder.setMessage("This collection has unsaved changes, please save before changing views.")
       	       .setCancelable(false)
       	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
           	           public void onClick(DialogInterface dialog, int id) {
       	                // Nothing to do, just a warning
       	           }
           	       });
       	AlertDialog alert = builder.create();
       	alert.show();
    }
    
    private void showUnsavedChangesMessage(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Leaving this page will erase your unsaved changes, are you sure you want to exit?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
           	           public void onClick(DialogInterface dialog, int id) {
                	   finish();
				   }})
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           	           public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
				   }});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    @Override
    // http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html
    public boolean onKeyDown(final int keyCode, final KeyEvent event)  {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // If the back key is pressed, we want to warn the user if there are unsaved changes
            	
        	if(this.doUnsavedChangesExist()){
        	
        		showUnsavedChangesMessage();
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
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // In the advanced view, if we change orientation or something we need
        // to save off the lists storing the uncommitted changes of coin grades,
        // coin quantities, and coin notes.  This is pretty hacked together,
        // so fix sometime, maybe
        
        int index;
        int top;
        
        if(mDisplayType == MainApplication.ADVANCED_DISPLAY){
        	
        	// Save off these lists that may have unsaved user data
        	outState.putIntegerArrayList("coin_grades", mCoinSlotAdapter.advancedGrades);
        	outState.putIntegerArrayList("coin_quantities", mCoinSlotAdapter.advancedQuantities);
        	outState.putStringArrayList("coin_notes", mCoinSlotAdapter.advancedNotes);
        	outState.putBooleanArray("change_list", mCoinSlotAdapter.indexHasChanged);
        	
            // Save off the list of the coins in the collection
        	boolean[] array = new boolean[mCoinSlotAdapter.inCollectionList.size()];
        	for (int i = 0; i < mCoinSlotAdapter.inCollectionList.size(); i++) {
        	    array[i] = mCoinSlotAdapter.inCollectionList.get(i);
        	}
        	
        	outState.putBooleanArray("in_collection", array);
        	
        	// Finally, save off the position of the listview
        	ListView listview = (ListView) findViewById(R.id.advanced_collection_page);
        	
        	// save index and top position
        	// http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
        	index = listview.getFirstVisiblePosition();
        	View v = listview.getChildAt(0);
        	top = (v == null) ? 0 : v.getTop();
        	
        } else {
        	
        	GridView gridview = (GridView) findViewById(R.id.standard_collection_page);
        	
        	index = gridview.getFirstVisiblePosition();
        	View v = gridview.getChildAt(0);
        	top = (v == null) ? 0 : v.getTop();
        }
        
    	outState.putInt(VIEW_INDEX, index);
    	outState.putInt(VIEW_POSITION, top);
    }
}
