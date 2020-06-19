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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;

import static com.coincollection.CoinSlot.COL_ADV_GRADE_INDEX;
import static com.coincollection.CoinSlot.COL_ADV_NOTES;
import static com.coincollection.CoinSlot.COL_ADV_QUANTITY_INDEX;
import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.coincollection.MainActivity.createAndShowHelpDialog;
import static com.coincollection.MainActivity.showUnsavedChangesAlertAndExitActivity;
import static com.coincollection.MainActivity.showUnsavedChangesAlertViewChange;
import static com.spencerpages.MainApplication.APP_NAME;

/** Activity for managing each collection page
 *
 * http://developer.android.com/resources/tutorials/views/hello-gridview.html
 */
public class CollectionPage extends AppCompatActivity {
    private String mCollectionName;
    private CollectionInfo mCollectionTypeObj;
    private int mActionBarImage;
    private ArrayList<CoinSlot> mCoinList;
    private CoinSlotAdapter mCoinSlotAdapter;
    private Bundle mSavedInstanceState = null;
    private Resources mRes;

    // Saved Instance State Keywords

    // Intent Argument Keywords
    public final static String COLLECTION_NAME        = "Collection_Name";
    public final static String COLLECTION_TYPE_INDEX  = "Collection_Type_Index";
    private final static String VIEW_INDEX            = "view_index";
    private final static String VIEW_POSITION         = "view_position";
    private final static String COIN_LIST             = "coin_list";

    // Global "enum" values
    public static final int SIMPLE_DISPLAY = 0;
    public static final int ADVANCED_DISPLAY = 1;

    private int mDisplayType = SIMPLE_DISPLAY;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRes = getResources();

        // Save off this bundle so that after the database is open we can use it
        // to get the previous CoinSlotAdapter, if present
        mSavedInstanceState = savedInstanceState;

        // Need to get the coin type from the intent that started this process
        Intent callingIntent = getIntent();
        int collectionTypeIndex = callingIntent.getIntExtra(COLLECTION_TYPE_INDEX, 0);

        // Capture the collection name from the saved instance state if it's there,
        // otherwise capture from the calling intent. Note that the calling intent
        // is updated with any renames before re-creating the view.
        if(mSavedInstanceState != null){
            mCollectionName = mSavedInstanceState.getString(COLLECTION_NAME);
        } else{
            mCollectionName = callingIntent.getStringExtra(COLLECTION_NAME);
        }

        mCollectionTypeObj = MainApplication.COLLECTION_TYPES[collectionTypeIndex];
        mActionBarImage = mCollectionTypeObj.getCoinImageIdentifier();

        // Restore the view index and position
        if(mSavedInstanceState != null){
            mViewIndex = mSavedInstanceState.getInt(VIEW_INDEX);
            mViewPosition = mSavedInstanceState.getInt(VIEW_POSITION);
        } else if(callingIntent.hasExtra(VIEW_INDEX)){
            mViewIndex = callingIntent.getIntExtra(VIEW_INDEX, 0);
            mViewPosition = callingIntent.getIntExtra(VIEW_POSITION, 0);
        }

        // Update the title
        // http://stackoverflow.com/questions/2198410/how-to-change-title-of-activity-in-android
        this.setTitle(mCollectionName);

        // Tell the user they can now lock the collections
        // Check whether it is the users first time using the app
        createAndShowHelpDialog("first_Time_screen3", R.string.tutorial_add_to_and_lock_collection, this);

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
        if(actionBar != null){
            actionBar.setIcon(mActionBarImage);
            // Set the actionbar so that clicking the icon takes you back
            // SO 1010877
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        GridView gridview = null;
        ListView listview = null;

        if(mDisplayType == SIMPLE_DISPLAY) {

            setContentView(R.layout.standard_collection_page);
            gridview = findViewById(R.id.standard_collection_page);

        } else if(mDisplayType == ADVANCED_DISPLAY){

            setContentView(R.layout.advanced_collection_page);
            listview = findViewById(R.id.advanced_collection_page);

            // Make it so that the elements in the listview cells can get focus
            listview.setItemsCanFocus(true);
        }

        // Populate the coin list
        if(mSavedInstanceState == null){

            mCoinList = new ArrayList<>();
            Cursor resultCursor = dbAdapter.getBasicCoinInfo(mCollectionName);
            // THanks! http://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
            if (resultCursor.moveToFirst()){
                do{
                    mCoinList.add(new CoinSlot(
                            resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_IDENTIFIER)),
                            resultCursor.getString(resultCursor.getColumnIndex(COL_COIN_MINT)),
                            (resultCursor.getInt(resultCursor.getColumnIndex(COL_IN_COLLECTION)) == 1)
                    ));
                } while(resultCursor.moveToNext());
            }
            resultCursor.close();

            // If we are the advanced view we need to set up the coin slot adapters advanced tables
            if(mDisplayType == ADVANCED_DISPLAY){

                // We need to give the coin adapter the extra info
                Cursor advResultCursor = dbAdapter.getAdvInfo(mCollectionName);
                if (advResultCursor.moveToFirst()){
                    int i = 0;
                    do{
                        CoinSlot coinSlot = mCoinList.get(i++);
                        coinSlot.setAdvancedGrades(advResultCursor.getInt(advResultCursor.getColumnIndex(COL_ADV_GRADE_INDEX)));
                        coinSlot.setAdvancedQuantities(advResultCursor.getInt(advResultCursor.getColumnIndex(COL_ADV_QUANTITY_INDEX)));
                        coinSlot.setAdvancedNotes(advResultCursor.getString(advResultCursor.getColumnIndex(COL_ADV_NOTES)));
                    } while(advResultCursor.moveToNext());
                } else {
                    Log.e(APP_NAME, "cursor.moveToFirst() returned false - getAdvInfo");
                }
                advResultCursor.close();
            }
        } else {

            // We have already loaded the advanced lists, so use those instead.
            // That way we have all of the state from before the page loaded.
            // yay
            if(BuildConfig.DEBUG) {
                Log.d(APP_NAME, "Successfully restored previous state");
            }
            mCoinList = mSavedInstanceState.getParcelableArrayList(COIN_LIST);
            // Search through the hasChanged history and see whether we should
            // re-display the "Unsaved Changes" view
            if (mCoinList != null){
                for(int i = 0; i < mCoinList.size(); i++){
                    if(mCoinList.get(i).hasIndexChanged()){
                        this.showUnsavedTextView();
                        break;
                    }
                }
            }
        }
        dbAdapter.close();
        mCoinSlotAdapter = new CoinSlotAdapter(this, mCollectionName, mCollectionTypeObj, mCoinList, mDisplayType);

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

        if(mDisplayType == SIMPLE_DISPLAY){

            // Apply the adapter to handle each entry in the grid
            gridview.setAdapter(mCoinSlotAdapter);

            // Restore the position in the list that the user was at
            // (or go to the default of the first item)
            gridview.setSelection(mViewIndex);

            // Set the scroll listener so that the view re-adjusts to the new view
            gridview.setOnScrollListener(scrollListener);

            // Set the onClick listener that will handle changing the coin state
            // Note, for the advanced view we have to put the click listener on the
            // coin image, so we can't do it here. boo
            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // Need to check whether the collection is locked
                    SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);

                    if(mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)){
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
                        CoinSlot coinSlot = mCoinList.get(position);
                        dbAdapter.toggleInCollection(mCollectionName, coinSlot);
                        dbAdapter.close();

                        // Update the mCoinSlotAdapters copy of the coins in this collection
                        boolean oldValue = coinSlot.isInCollection();
                        coinSlot.setInCollection(!oldValue);

                        // And have the adapter redraw with this new info

                        mCoinSlotAdapter.notifyDataSetChanged();
                    }
                }
            });

        } else if(mDisplayType == ADVANCED_DISPLAY){
            // Apply the adapter to handle each entry in the list
            listview.setAdapter(mCoinSlotAdapter);

            // Restore the position in the list that the user was at
            // (or go to the default of the first item)
            listview.setSelectionFromTop(mViewIndex, mViewPosition);

            // Set the scroll listener so that the view re-adjusts to the new view
            listview.setOnScrollListener(scrollListener);

        }
    }
    
    public void showUnsavedTextView() {

        TextView unsavedMessageView = findViewById(R.id.unsaved_message_textview);
        unsavedMessageView.setVisibility(View.VISIBLE);
    }
    
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

        if(mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false)){
            // Current Locked, set text to unlock it
            item.setTitle(R.string.unlock_collection);
        } else {
            // Currently unlocked, set text to lock it
            // Default is unlocked
            if(mDisplayType == ADVANCED_DISPLAY){
                item.setTitle(R.string.lock_collection_adv);
            } else {
                item.setTitle(R.string.lock_collection);
            }
        }

        // If we are in the advanced mode, we need to show the save and switch view
        MenuItem changeViewItem = menu.findItem(R.id.change_view);

        if(mDisplayType == ADVANCED_DISPLAY){
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

            boolean isLocked = mainPreferences.getBoolean(mCollectionName + IS_LOCKED, false);
            boolean finishedSuccessfully = true;

            // If we are going from unlocked to lock in advance mode, we need to save the
            // changes the user may have made (if any)
            if(mDisplayType == ADVANCED_DISPLAY &&
                    !isLocked &&
                    this.doUnsavedChangesExist()){

                // In the advanced display case, we also need to save

                // TODO Show some kind of spinner
                DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
                dbAdapter.open();

                for(int i = 0; i < mCoinList.size(); i++){
                    CoinSlot coinSlot = mCoinList.get(i);
                    if(coinSlot.hasIndexChanged()){
                        if(dbAdapter.updateAdvInfo(mCollectionName, coinSlot) != 1){
                            finishedSuccessfully = false;
                            // Keep going, though
                            continue;
                        }
                        // Mark this data as being unchanged
                        coinSlot.setIndexChanged(false);
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

            if(finishedSuccessfully) {
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

            if(mDisplayType == ADVANCED_DISPLAY){
                // We need to restart the view so we can show the locked
                // view.  Also, at this point there are no unsaved changes

                // Save the position that the user was at for convenience
                // http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
                ListView listview = findViewById(R.id.advanced_collection_page);
                int index = listview.getFirstVisiblePosition();
                View v = listview.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();

                Intent callingIntent = getIntent();
                callingIntent.putExtra(VIEW_INDEX, index);
                callingIntent.putExtra(VIEW_POSITION, top);
                callingIntent.putExtra(COLLECTION_NAME, mCollectionName);

                finish();
                startActivity(callingIntent);
            }

            return true;

        case R.id.change_view:

            if(mDisplayType == SIMPLE_DISPLAY){
                // Setup the advanced view

                DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
                dbAdapter.open();

                dbAdapter.updateTableDisplay(mCollectionName, ADVANCED_DISPLAY) ;

                dbAdapter.close();

                // Save the position that the user was at for convenience
                // http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
                GridView gridview = findViewById(R.id.standard_collection_page);
                int index = gridview.getFirstVisiblePosition();
                View v = gridview.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();

                Intent callingIntent = getIntent();
                callingIntent.putExtra(VIEW_INDEX, index);
                callingIntent.putExtra(VIEW_POSITION, top);
                callingIntent.putExtra(COLLECTION_NAME, mCollectionName);

                // Restart the activity
                finish();
                startActivity(callingIntent);

                return true;

            } else if(mDisplayType == ADVANCED_DISPLAY){
                // Setup the basic view

                // We need to see if there are any unsaved changes, and if so,
                // present an alert

                if(this.doUnsavedChangesExist()){

                    showUnsavedChangesAlertViewChange(mRes, this);
                    return true;
                }

                // The user doesn't have any unsaved changes

                DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
                dbAdapter.open();

                dbAdapter.updateTableDisplay(mCollectionName, SIMPLE_DISPLAY) ;

                dbAdapter.close();

                // Save the position that the user was at for convenience
                // http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
                ListView listview = findViewById(R.id.advanced_collection_page);
                int index = listview.getFirstVisiblePosition();
                View v = listview.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();

                Intent callingIntent = getIntent();
                callingIntent.putExtra(VIEW_INDEX, index);
                callingIntent.putExtra(VIEW_POSITION, top);
                callingIntent.putExtra(COLLECTION_NAME, mCollectionName);

                // Restart the activity
                finish();
                startActivity(callingIntent);
                return true;
            }

            // Shouldn't get here
            return true;

        case R.id.rename_collection:

            // Prompt user for new name via alert dialog
            showRenamePrompt();
            return true;

        case android.R.id.home:
            // To support having a back arrow on the page

            if(this.doUnsavedChangesExist()){
                // If we have unsaved changes, don't go back right away but
                // instead let the user decide
                showUnsavedChangesAlertAndExitActivity(mRes, this);
            } else {
                this.onBackPressed();
            }
            return true;

        default:
            return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Updates the collection name when the user renames a collection
     * @param newCollectionName Name of the new collection
     */
    private void updateCollectionName(String newCollectionName){

        String oldCollectionName = mCollectionName;

        // Do nothing if the name isn't actually changed
        if (newCollectionName.equals(oldCollectionName)){
            return;
        }

        DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();

        // Make sure the new name isn't taken and is valid
        String checkNameResult = dbAdapter.checkCollectionName(newCollectionName);
        if(!checkNameResult.equals("")){
            Context context = this.getApplicationContext();
            Toast.makeText(context, checkNameResult, Toast.LENGTH_SHORT).show();
            dbAdapter.close();
            return;
        }

        // Perform all actions needed to rename the collection

        // Update database
        dbAdapter.updateCollectionName(oldCollectionName, newCollectionName);
        dbAdapter.close();

        // Update app state
        SharedPreferences mainPreferences = getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = mainPreferences.edit();
        boolean isLocked = mainPreferences.getBoolean(oldCollectionName + IS_LOCKED, false);
        editor.remove(oldCollectionName + IS_LOCKED);
        editor.putBoolean(newCollectionName + IS_LOCKED, isLocked);
        editor.apply();

        // Update current view
        mCollectionName = newCollectionName;
        mCoinSlotAdapter.setTableName(newCollectionName);
        this.setTitle(newCollectionName);
    }

    /**
     * Prompts the user to rename the collection
     */
    private void showRenamePrompt(){
        // Create a text box for the new collection name
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(mCollectionName);

        // Build the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(mRes.getString(R.string.select_collection_name));
        builder.setView(input);
        builder.setPositiveButton(mRes.getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if(newName.equals("")){
                    Toast.makeText(CollectionPage.this, mRes.getString(R.string.dialog_enter_collection_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                updateCollectionName(newName);
            }
        });
        builder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private boolean doUnsavedChangesExist(){

        if(mDisplayType == ADVANCED_DISPLAY){
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
    public boolean onKeyDown(final int keyCode, final KeyEvent event)  {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // If the back key is pressed, we want to warn the user if there are unsaved changes

            if(this.doUnsavedChangesExist()){
                showUnsavedChangesAlertAndExitActivity(mRes, this);
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
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // In the advanced view, if we change orientation or something we need
        // to save off the lists storing the uncommitted changes of coin grades,
        // coin quantities, and coin notes.  This is pretty hacked together,
        // so fix sometime, maybe
        
        int index;
        int top;
        
        if(mDisplayType == ADVANCED_DISPLAY){

            // Finally, save off the position of the listview
            ListView listview = findViewById(R.id.advanced_collection_page);

            // save index and top position
            // http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
            index = listview.getFirstVisiblePosition();
            View v = listview.getChildAt(0);
            top = (v == null) ? 0 : v.getTop();

        } else {

            GridView gridview = findViewById(R.id.standard_collection_page);

            index = gridview.getFirstVisiblePosition();
            View v = gridview.getChildAt(0);
            top = (v == null) ? 0 : v.getTop();
        }

        // Save off these lists that may have unsaved user data
        outState.putParcelableArrayList(COIN_LIST, mCoinList);
        outState.putInt(VIEW_INDEX, index);
        outState.putInt(VIEW_POSITION, top);
        outState.putString(COLLECTION_NAME, mCollectionName);
    }
}
