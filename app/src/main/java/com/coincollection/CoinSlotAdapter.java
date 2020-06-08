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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import com.spencerpages.MainApplication;
import com.spencerpages.R;
import java.util.ArrayList;

/**
 * BaseAdapter for the collection pages
 */
class CoinSlotAdapter extends BaseAdapter {

    /** mContext The context of the activity we are running in (for things like Toasts that have UI
     *           components) */
    private final Context mContext;
    private final Resources mRes;

    // Information about the collections needed for the basic coin list view

    private final CollectionInfo mCollectionTypeObj;
    private String mTableName;

    private final ArrayList<CoinSlot> mCoinList;

    private OnItemSelectedListener mGradeOnItemSelectedListener = null;
    private ArrayAdapter<CharSequence> mGradeArrayAdapter = null;
    private OnItemSelectedListener mQuantityOnItemSelectedListener = null;
    private ArrayAdapter<CharSequence> mQuantityArrayAdapter = null;

    // Keep track of whether we are showing the advanced view so we can do extra setup
    private final int mDisplayType;
    // This variable will only be set for the advanced view, where we have separate
    // views for the locked and unlocked views
    private final boolean mDisplayIsLocked;

    /**
     * Constructor which passes the data necessary for the adapter to work, along with a list of
     * resource identifiers for those collections that don't use the same imageIdentifier for every
     * coin.
     * @param context The Activity context that we should use for any UI things
     * @param tableName The collection name
     * @param collectionTypeObj The backing object in the COLLECTION_TYPE list
     * @param coinList The list of coins
     */
    CoinSlotAdapter(Context context, String tableName, CollectionInfo collectionTypeObj, ArrayList<CoinSlot> coinList, int displayType) {
        // Used for State, National Park, Presidential Coins, and Native American coins
        // and Pennies, Nickels, American Innovation Dollars
        super();
        mContext = context;
        mTableName = tableName;
        mCollectionTypeObj = collectionTypeObj;
        mCoinList = coinList;
        mDisplayType = displayType;

        mRes = mContext.getResources();
        SharedPreferences mainPreferences = mContext.getSharedPreferences(MainApplication.PREFS, Context.MODE_PRIVATE);
        mDisplayIsLocked = mainPreferences.getBoolean(mTableName + CollectionPage.IS_LOCKED, false);
    }

    /**
     * Sets the table name
     * @param tableName Table name
     */
    public void setTableName(String tableName){
        mTableName = tableName;
    }

    @Override
    public int getCount() {
        return mCoinList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create or recycle a new View for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View coinView = convertView;

        if (coinView == null) {  // If we couldn't get a recycled one, create a new one

            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(mDisplayType == CollectionPage.ADVANCED_DISPLAY){

                if(!mDisplayIsLocked){
                  // If the collection isn't locked, we show spinners and an EditText
                  coinView = vi.inflate(R.layout.advanced_collection_slot, parent, false);
                } else {
                  // The collection is locked, so we just show the advanced details in TextViews
                  coinView = vi.inflate(R.layout.advanced_collection_slot_locked, parent, false);
                }

            } else if(mDisplayType == CollectionPage.SIMPLE_DISPLAY){
                coinView = vi.inflate(R.layout.coin_slot, parent, false);
            }
        }

        // Display the basic info first
        CoinSlot coinSlot = mCoinList.get(position);
        String identifier = coinSlot.getIdentifier();
        String mint = coinSlot.getMint();
        TextView coinText = coinView.findViewById(R.id.coinText);
        boolean inCollection = coinSlot.isInCollection();

        // Set the coin identifier text (Year and Mint in most cases)
        // TODO Fix this so there is no space if there is no mint
        // This actually puts in two spaces, since mint has a space as well
        coinText.setText(mRes.getString(R.string.coin_text_template, identifier, mint));

        //Set this image based on whether the coin has been obtained
        ImageView coinImage = coinView.findViewById(R.id.coinImage);

        // TODO Not sure if this improves accessibility, but better than nothing
        coinImage.setContentDescription(mRes.getString(R.string.coin_content_desc_template, identifier, mint));

        int imageIdentifier = mCollectionTypeObj.getCoinSlotImage(coinSlot);
        coinImage.setImageResource(imageIdentifier);

        // Setup the rest of the view if it is the advanced view
        if(mDisplayType == CollectionPage.ADVANCED_DISPLAY){
            setupAdvancedView(coinView, position);
        }

        return coinView;
    }

    /**
     * Handles setting up the advanced view components in the case that we should display them
     * @param coinView The view that we are setting up
     * @param position The list index of the coin that we are making the view for
     */
    private void setupAdvancedView(View coinView, final int position) {

        // Use this so the listeners know the position of the item in the list
        Integer positionObj = position;
        CoinSlot coinSlot = mCoinList.get(position);

        final ImageView imageView = coinView.findViewById(R.id.coinImage);

        imageView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // Need to check whether the collection is locked
                if(mDisplayIsLocked){
                    // Collection is locked
                    Context context = mContext.getApplicationContext();
                    CharSequence text = "Collection is currently locked, hit 'Menu' and then 'Unlock Collection' to unlock";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    // Collection is unlocked, update value
                    //mDbAdapter.toggleInCollection(mTableName, mIdentifierList.get(position), mMintList.get(position));

                    // Tie the update to the save button
                    CoinSlot coinSlot = CoinSlotAdapter.this.mCoinList.get(position);
                    boolean oldValue = coinSlot.isInCollection();
                    coinSlot.setInCollection(!oldValue);
                    coinSlot.setIndexChanged(true);

                    CoinSlotAdapter.this.notifyDataSetChanged();

                    CollectionPage collectionPage = (CollectionPage) mContext;
                    collectionPage.showUnsavedTextView();
                }
            }});

        // Everything below here is specific to whether the collection is locked or not.
        // Take care of the locked case first, since it is easier.

        if(mDisplayIsLocked){

            String[] grades = mRes.getStringArray(R.array.coin_grades);
            TextView gradeTextView = coinView.findViewById(R.id.grade_textview);
            int gradeIndex = coinSlot.getAdvancedGrades();
            if(gradeIndex != 0){
                // Prefix the grade with 'Grade:'
                gradeTextView.setText(mRes.getString(R.string.grade_text_view_template, grades[gradeIndex]));
            } else {
                // 'Grade:' will be printed
                gradeTextView.setText(mRes.getString(R.string.grade_text_view_template_without_grade, grades[gradeIndex]));
            }

            String[] quantities = mRes.getStringArray(R.array.coin_quantities);
            TextView quantitiesTextView = coinView.findViewById(R.id.quantity_textview);
            quantitiesTextView.setText(mRes.getString(R.string.quantities_text_view_template, quantities[coinSlot.getAdvancedQuantities()]));

            TextView notesTextView = coinView.findViewById(R.id.notes_textview);
            notesTextView.setText(mRes.getString(R.string.notes_text_view_template, coinSlot.getAdvancedNotes()));
            return;
        }

        // The collection is not locked, we need to set up the spinners and edittext

        // Setup the spinner that will let you select the coin grade
        Spinner gradeSelector = coinView.findViewById(R.id.grade_selector);
        gradeSelector.setTag(positionObj);

        if(mGradeArrayAdapter == null){
            // Create the adapter that will handle spinner selections
            mGradeArrayAdapter = ArrayAdapter.createFromResource(
                mContext, R.array.coin_grades, android.R.layout.simple_spinner_item);
            mGradeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        gradeSelector.setAdapter(mGradeArrayAdapter);

        // Put the spinner at the value we have stored for this coin
        gradeSelector.setSelection(coinSlot.getAdvancedGrades(), false);

        if(mGradeOnItemSelectedListener == null){
            mGradeOnItemSelectedListener = new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent,
                        View view, int pos, long id) {

                    int posPrim = (int) parent.getTag();
                    CoinSlot coinSlot = CoinSlotAdapter.this.mCoinList.get(posPrim);

                    // Update the values in the lists if this is a new value
                    if(pos != coinSlot.getAdvancedGrades()){
                        // Value has changed
                        coinSlot.setAdvancedGrades(pos);
                        coinSlot.setIndexChanged(true);

                        // Tell the parent page to show the unsaved changes view
                        CollectionPage collectionPage = (CollectionPage) mContext;
                        collectionPage.showUnsavedTextView();
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) {}
            };
        }
        gradeSelector.setOnItemSelectedListener(mGradeOnItemSelectedListener);

        // Setup the spinner that will let you select the coin quantity
        Spinner quantitySelector = coinView.findViewById(R.id.quantity_selector);
        quantitySelector.setTag(positionObj);

        if(mQuantityArrayAdapter == null){
            mQuantityArrayAdapter = ArrayAdapter.createFromResource(
                mContext, R.array.coin_quantities, android.R.layout.simple_spinner_item);
            mQuantityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        quantitySelector.setAdapter(mQuantityArrayAdapter);

        // Put the spinner at the value we have stored for this coin
        quantitySelector.setSelection(coinSlot.getAdvancedQuantities(), false);

        if(mQuantityOnItemSelectedListener == null) {
            mQuantityOnItemSelectedListener = new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent,
                                           View view, int pos, long id) {

                    int posPrim = (Integer) parent.getTag();
                    CoinSlot coinSlot = CoinSlotAdapter.this.mCoinList.get(posPrim);

                    // Update the values in the lists if this is a new value
                    if(pos != coinSlot.getAdvancedQuantities()){
                        // Value has changed
                        coinSlot.setAdvancedQuantities(pos);
                        coinSlot.setIndexChanged(true);

                        // Tell the parent page to show the unsaved changes view
                        CollectionPage collectionPage = (CollectionPage) mContext;
                        collectionPage.showUnsavedTextView();
                    }
                }

                public void onNothingSelected(AdapterView<?> parent) {}
            };
        }
        quantitySelector.setOnItemSelectedListener(mQuantityOnItemSelectedListener);

        // Setup the edit text to allow for coin notes
        EditText notesEditText = coinView.findViewById(R.id.notes_edit_text);

        // Get the current tag associated with this EditText.  We use this to know whether or not
        // this is a new EditText that doesn't have a TextWatcher yet (as opposed to a recycled
        // EditText that does.)
        // http://stackoverflow.com/questions/14117204
        Object previousTag = notesEditText.getTag();

        // Set the tag on this EditText
        if(previousTag != null){
            // If the user has their cursor in the EditText when the view is recycled, the
            // TextWatcher may not be triggered. So force the TextWatcher to trigger by
            // setting the EditText text to itself.
            notesEditText.setText(notesEditText.getText());
        }
        notesEditText.setTag(positionObj);

        // Set the EditText to the string previously entered by the user
        // - This will trigger the TextWatcher if recycled but the new/old values should match
        String text = coinSlot.getAdvancedNotes();
        notesEditText.setText(text);
        // http://stackoverflow.com/questions/6217378/place-cursor-at-the-end-of-text-in-edittext
        notesEditText.setSelection(text.length());

        // If the display is not locked, we also need to set up a TextWatcher so that we can know
        // when the user types into the notes field. Create one for each unique EditText
        if(previousTag == null) {
            final EditText textWatcherEditText = notesEditText;
            TextWatcher notesTextWatcher = new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    // Note: This does not remove '[', ']', and ',' characters - bracket characters
                    // will be replaced by spaces if exported/imported but commas will be preserved

                    // If field was empty and remains empty, ignore
                    if (before == 0 && count == 0) {
                        return;
                    }

                    // Ignore if the text matches what's already temporarily saved
                    int posPrim = (Integer) textWatcherEditText.getTag();
                    CoinSlot coinSlot = CoinSlotAdapter.this.mCoinList.get(posPrim);
                    String newText = s.toString();
                    if (coinSlot.getAdvancedNotes().equals(newText)) {
                        //Log.e(APP_NAME, "Text equals what is currently stored, not updating");
                        return;
                    }

                    // Change detected - Temporarily store the modified text
                    coinSlot.setAdvancedNotes(newText);
                    coinSlot.setIndexChanged(true);
                    //Log.e("CoinCollection", "Text has changed: " + newText + " position: " + Integer.toString(posPrim));

                    CollectionPage collectionPage = (CollectionPage) mContext;
                    collectionPage.showUnsavedTextView();
                }
            };
            // Add the TextWatcher for this EditText
            notesEditText.addTextChangedListener(notesTextWatcher);
        }

        // Make the edittext scrollable
        // TODO Get scrolling working all the way
        // notesEditText.setMovementMethod(new ScrollingMovementMethod());

    }
}
