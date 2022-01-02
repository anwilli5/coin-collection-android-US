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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;

/**
 * BaseAdapter for the collection pages
 */
class CoinSlotAdapter extends BaseAdapter {

    /** mContext The context of the activity we are running in (for things like Toasts that have UI
     *           components) */
    private final CollectionPage mCollectionPageContext;
    private final Resources mRes;

    // Information about the collections needed for the basic coin list view

    private final CollectionInfo mCollectionTypeObj;
    private String mTableName;

    private final ArrayList<CoinSlot> mCoinList;

    private OnItemSelectedListener mGradeOnItemSelectedListener = null;
    private ArrayAdapter<CharSequence> mGradeArrayAdapter;
    private OnItemSelectedListener mQuantityOnItemSelectedListener;
    private ArrayAdapter<CharSequence> mQuantityArrayAdapter;

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
    CoinSlotAdapter(CollectionPage context, String tableName, CollectionInfo collectionTypeObj, ArrayList<CoinSlot> coinList, int displayType) {
        // Used for State, National Park, Presidential Coins, and Native American coins
        // and Pennies, Nickels, American Innovation Dollars
        super();
        mCollectionPageContext = context;
        mTableName = tableName;
        mCollectionTypeObj = collectionTypeObj;
        mCoinList = coinList;
        mDisplayType = displayType;

        mRes = mCollectionPageContext.getResources();
        SharedPreferences mainPreferences = mCollectionPageContext.getSharedPreferences(MainApplication.PREFS, Context.MODE_PRIVATE);
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
        final boolean coinViewWasRecycled = (coinView != null);

        if (!coinViewWasRecycled) {
            // If we couldn't get a recycled one, create a new one
            LayoutInflater vi = (LayoutInflater) mCollectionPageContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

        // Make lint happy
        if(coinView == null){
            return null;
        }

        // Register a callback for when the view gets detached
        if (!coinViewWasRecycled && mDisplayType == CollectionPage.ADVANCED_DISPLAY && !mDisplayIsLocked) {
            coinView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) { }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    onCoinSlotAdvNotesChanged(view);
                }
            });
        }

        // Display the basic info first
        CoinSlot coinSlot = mCoinList.get(position);
        String identifier = coinSlot.getIdentifier();
        String mint = coinSlot.getMint();
        TextView coinText = coinView.findViewById(R.id.coinText);

        // Set the coin identifier text (Year and Mint in most cases)
        // TODO Fix this so there is no space if there is no mint
        coinText.setText(mRes.getString(R.string.coin_text_template, identifier, mint));

        //Set this image based on whether the coin has been obtained
        ImageView coinImage = coinView.findViewById(R.id.coinImage);
        int imageIdentifier = mCollectionTypeObj.getCoinSlotImage(coinSlot);
        coinImage.setImageResource(imageIdentifier);

        // Add an accessibility string to indicate that the coin has been found or not
        String contextDesc = mRes.getString(coinSlot.isInCollectionStringRes());
        contextDesc = mRes.getString(R.string.coin_content_desc_template, identifier, mint, contextDesc);
        coinImage.setContentDescription(contextDesc);

        // Setup the rest of the view if it is the advanced view
        if(mDisplayType == CollectionPage.ADVANCED_DISPLAY){
            setupAdvancedView(coinView, position, coinViewWasRecycled);
        }

        return coinView;
    }

    /**
     * Setup advanced view state shared by all views in the adapter
     */
    private void setupAdvancedSharedViews() {

        // Create the adapter that will handle grade selections
        mGradeArrayAdapter = ArrayAdapter.createFromResource(
                mCollectionPageContext, R.array.coin_grades, android.R.layout.simple_spinner_item);
        mGradeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create the listener that will handle grade selections
        mGradeOnItemSelectedListener = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {

                CoinSlot coinSlot = (CoinSlot) parent.getTag();

                // Update the values in the lists if this is a new value
                if(pos != coinSlot.getAdvancedGrades()){

                    // Update the data structure and set index changed
                    // - Changes will be committed to the database when the user presses save
                    coinSlot.setAdvancedGrades(pos);
                    coinSlot.setAdvInfoChanged(true);

                    // Tell the parent page to show the unsaved changes view
                    mCollectionPageContext.showUnsavedTextView();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}

        };

        // Create the adapter that will handle quantity selections
        mQuantityArrayAdapter = ArrayAdapter.createFromResource (
                mCollectionPageContext, R.array.coin_quantities, android.R.layout.simple_spinner_item);
        mQuantityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create the listener that will handle quantity selected
        mQuantityOnItemSelectedListener = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {

                CoinSlot coinSlot = (CoinSlot) parent.getTag();

                // Update the values in the lists if this is a new value
                if(pos != coinSlot.getAdvancedQuantities()){

                    // Update the data structure and set index changed
                    // - Changes will be committed to the database when the user presses save
                    coinSlot.setAdvancedQuantities(pos);
                    coinSlot.setAdvInfoChanged(true);

                    // Tell the parent page to show the unsaved changes view
                    mCollectionPageContext.showUnsavedTextView();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    /**
     * Handles setting up the advanced view components in the case that we should display them
     * @param coinView The view that we are setting up
     * @param position The list index of the coin that we are making the view for
     * @param coinViewWasRecycled true if the view was recycled and is being reused
     */
    private void setupAdvancedView(View coinView, int position, boolean coinViewWasRecycled) {

        // Get the coin slot at the position in the list
        CoinSlot coinSlot = mCoinList.get(position);

        // Set up on-click listeners for the image
        final ImageView imageView = coinView.findViewById(R.id.coinImage);
        imageView.setTag(coinSlot);
        imageView.setOnClickListener(view -> {
            // Need to check whether the collection is locked
            if(mDisplayIsLocked){
                // Collection is locked
                String text = mRes.getString(R.string.collection_locked);
                Toast toast = Toast.makeText(mCollectionPageContext, text, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                // Update the data structure and set index changed
                // - Changes will be committed to the database when the user presses save
                CoinSlot viewTagCoinSlot = (CoinSlot) view.getTag();
                boolean oldValue = viewTagCoinSlot.isInCollection();
                viewTagCoinSlot.setInCollection(!oldValue);
                viewTagCoinSlot.setAdvInfoChanged(true);

                // Notify the adapter to re-draw the view
                CoinSlotAdapter.this.notifyDataSetChanged();

                // Tell the parent page to show the unsaved changes view
                mCollectionPageContext.showUnsavedTextView();
            }
        });

        // Add long-press handler for additional actions
        imageView.setOnLongClickListener(view -> {
            mCollectionPageContext.promptCoinSlotActions(position);
            return true;
        });

        // Everything below here is specific to whether the collection is locked or not.
        // Take care of the locked case first, since it is easier.

        if(mDisplayIsLocked){
            // Setup the locked view and return
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

        // Setup shared advanced view state if needed
        if (mGradeOnItemSelectedListener == null) {
            setupAdvancedSharedViews();
        }

        // Setup the spinner that will let you select the coin grade
        Spinner gradeSelector = coinView.findViewById(R.id.grade_selector);
        gradeSelector.setTag(coinSlot);
        gradeSelector.setAdapter(mGradeArrayAdapter);
        gradeSelector.setSelection(coinSlot.getAdvancedGrades(), false);
        gradeSelector.setOnItemSelectedListener(mGradeOnItemSelectedListener);

        // Setup the spinner that will let you select the coin quantity
        Spinner quantitySelector = coinView.findViewById(R.id.quantity_selector);
        quantitySelector.setTag(coinSlot);
        quantitySelector.setAdapter(mQuantityArrayAdapter);
        quantitySelector.setSelection(coinSlot.getAdvancedQuantities(), false);
        quantitySelector.setOnItemSelectedListener(mQuantityOnItemSelectedListener);

        // Setup the edit text to allow for coin notes
        EditText notesEditText = coinView.findViewById(R.id.notes_edit_text);
        notesEditText.setTag(coinSlot);

        // Set the EditText to the string previously entered by the user
        // - This will trigger the TextWatcher if recycled but the new/old values should match
        String advancedNotesText = coinSlot.getAdvancedNotes();
        notesEditText.setText(advancedNotesText);
        notesEditText.setSelection(advancedNotesText.length());

        // Make the hint specific for this coin's notes field
        String notes = mRes.getString(R.string.notes);
        String contextDesc = mRes.getString(R.string.coin_content_desc_template, coinSlot.getIdentifier(), coinSlot.getMint(), notes);
        notesEditText.setHint(contextDesc);

        // If the display is not locked, we also need to set up a TextWatcher so that we can know
        // when the user types into the notes field. Create one for each unique EditText
        if(!coinViewWasRecycled) {

            // Add the TextWatcher for this EditText
            notesEditText.addTextChangedListener(new TextWatcher() {
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

                    onCoinSlotAdvNotesChanged(coinView);
                }
            });
        }

        // Make the edittext scrollable
        // TODO Get scrolling working all the way
        // notesEditText.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Called when advanced view notes are changed to capture the updated value
     * @param coinView view to update
     */
    private void onCoinSlotAdvNotesChanged(View coinView) {

        // Ignore if the text matches what's already saved
        EditText notesEditText = coinView.findViewById(R.id.notes_edit_text);
        CoinSlot coinSlot = (CoinSlot) notesEditText.getTag();
        String newText = notesEditText.getText().toString();

        if (coinSlot.getAdvancedNotes().equals(newText)) {
            return;
        }

        // Update the data structure and set index changed
        // - Changes will be committed to the database when the user presses save
        coinSlot.setAdvancedNotes(newText);
        coinSlot.setAdvInfoChanged(true);

        // Tell the parent page to show the unsaved changes view
        mCollectionPageContext.showUnsavedTextView();
    }
}
