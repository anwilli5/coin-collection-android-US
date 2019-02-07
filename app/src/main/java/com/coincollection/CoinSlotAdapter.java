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

import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * BaseAdapter for the collection pages
 */
class CoinSlotAdapter extends BaseAdapter {

    /** mContext The context of the activity we are running in (for things like Toasts that have UI
     *           components) */
    private final Context mContext;

    // Information about the collections needed for the basic coin list view

    private final CollectionInfo mCollectionTypeObj;
    private final String mTableName;

    /** mIdentifierList Contains a list of the main coin identifiers (Ex: "2009", or "Kentucky") */
    private final ArrayList<String> mIdentifierList;

    /** mMintList Contains a list of the mint marks associated with each coin, if any */
    private final ArrayList<String> mMintList;

    // These are public so that we can reach in and grab these values to save them off
    // We will keep track of which items need to be pushed back

    // List holding whether each coin is in the collection
    public ArrayList<Boolean> inCollectionList = null;

    // Lists needed to support the advanced view

    public boolean[] indexHasChanged = null;

    // In the database, we store the index into the grade and quantity arrays
    // so we can use these values efficiently.  For notes, we have to store the
    // strings
    // TODO Can we make these private?
    public ArrayList<Integer> advancedGrades = null;
    public ArrayList<Integer> advancedQuantities = null;
    public ArrayList<String> advancedNotes = null;

    private OnItemSelectedListener mGradeOnItemSelectedListener = null;
    private ArrayAdapter<CharSequence> mGradeArrayAdapter = null;
    private OnItemSelectedListener mQuantityOnItemSelectedListener = null;
    private ArrayAdapter<CharSequence> mQuantityArrayAdapter = null;

    // Keep track of whether we are showing the advanced view so we can do extra setup
    private int displayType = CollectionPage.SIMPLE_DISPLAY;
    // This variable will only be set for the advanced view, where we have separate
    // views for the locked and unlocked views
    private boolean displayIsLocked = false;

    /**
     * Constructor which passes the data necessary for the adapter to work, along with a list of
     * resource identifiers for those collections that don't use the same imageIdentifier for every
     * coin.
     * @param context The Activity context that we should use for any UI things
     * @param tableName The collection name
     * @param collectionTypeObj The backing object in the COLLECTION_TYPE list
     * @param identifierList The list of coin identifiers in the collection
     * @param mintList The list of coin mints in the collection
     * @param inCollectionList The list of whether each coin has been marked as being in the
     *                         collection
     */
    public CoinSlotAdapter(Context context, String tableName, CollectionInfo collectionTypeObj, ArrayList<String> identifierList, ArrayList<String> mintList, ArrayList<Boolean> inCollectionList) {
        // Used for State, National Park, Presidential Coins, and Native American coins
        // and Pennies, Nickels, American Innovation Dollars
        super();
        mContext = context;
        mTableName = tableName;
        mCollectionTypeObj = collectionTypeObj;
        mIdentifierList = identifierList;
        mMintList = mintList;
        this.inCollectionList = inCollectionList;

    }

    /**
     * Sets the adapter instance variables with data needed to support the advanced view
     * @param grades A list of the grades associated with each coin in the collection
     * @param quantities A list of quantities associated with each coin in the collection
     * @param notes A list of notes associated with each coin in the collection
     * @param hasChanged A list used to track whether the data associated with a given coin has
     *                   changed since the last time a 'Save' occurred
     */
    public void setAdvancedLists(ArrayList<Integer> grades, ArrayList<Integer> quantities, ArrayList<String> notes, boolean[] hasChanged) {

        advancedGrades = grades;
        advancedQuantities = quantities;
        advancedNotes = notes;
        indexHasChanged = hasChanged;

        displayType = CollectionPage.ADVANCED_DISPLAY;

        if(BuildConfig.DEBUG) {
            // Everything should be the same size
            if( advancedGrades.size() != mIdentifierList.size() ||
                advancedQuantities.size() != advancedGrades.size() ||
                advancedNotes.size() != advancedQuantities.size() ||
                hasChanged.length != advancedNotes.size()) {

                throw new AssertionError("Inconsistent array list lengths in CoinSlotAdapter");
            }
        }

        SharedPreferences mainPreferences = mContext.getSharedPreferences(MainApplication.PREFS, Context.MODE_PRIVATE);

        displayIsLocked = mainPreferences.getBoolean(mTableName + "_isLocked", false);
    }

    @Override
    public int getCount() {
        return mIdentifierList.size();
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

            if(displayType == CollectionPage.ADVANCED_DISPLAY){

                if(!displayIsLocked){
                  // If the collection isn't locked, we show spinners and an EditText
                  coinView = vi.inflate(R.layout.advanced_collection_slot, parent, false);
                } else {
                  // The collection is locked, so we just show the advanced details in TextViews
                  coinView = vi.inflate(R.layout.advanced_collection_slot_locked, parent, false);
                }

            } else if(displayType == CollectionPage.SIMPLE_DISPLAY){
                coinView = vi.inflate(R.layout.coin_slot, parent, false);
            }
        }

        // Display the basic info first
        String identifier = mIdentifierList.get(position);
        String mint = mMintList.get(position);
        TextView coinText = (TextView) coinView.findViewById(R.id.coinText);
        boolean inCollection = inCollectionList.get(position);

        // Set the coin identifier text (Year and Mint in most cases)
        // TODO Fix this so there is no space if there is no mint
        // This actually puts in two spaces, since mint has a space as well
        coinText.setText(identifier + " " + mint);

        //Set this image based on whether the coin has been obtained
        ImageView coinImage = (ImageView) coinView.findViewById(R.id.coinImage);

        // TODO Not sure if this improves accessibility, but better than nothing
        coinImage.setContentDescription(identifier + " " + mint + " Button");

        int imageIdentifier = mCollectionTypeObj.getCoinSlotImage(identifier, mint, inCollection);
        coinImage.setImageResource(imageIdentifier);

        // Setup the rest of the view if it is the advanced view
        if(displayType == CollectionPage.ADVANCED_DISPLAY){
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

        final ImageView imageView = (ImageView) coinView.findViewById(R.id.coinImage);

        imageView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // Need to check whether the collection is locked
                if(displayIsLocked){
                    // Collection is locked
                    Context context = mContext.getApplicationContext();
                    CharSequence text = "Collection is currently locked, hit 'Menu' and then 'Unlock Collection' to unlock";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    // Collection is unlocked, update value
                    //mDbAdapter.updateInfo(mTableName, mIdentifierList.get(position), mMintList.get(position));

                    // Tie the update to the save button
                    boolean oldValue = CoinSlotAdapter.this.inCollectionList.get(position);
                    CoinSlotAdapter.this.inCollectionList.set(position, !oldValue);
                    CoinSlotAdapter.this.indexHasChanged[position] = true;

                    CoinSlotAdapter.this.notifyDataSetChanged();

                    CollectionPage collectionPage = (CollectionPage) mContext;
                    collectionPage.showUnsavedTextView();
                }
            }});


        // Everything below here is specific to whether the collection is locked or not.
        // Take care of the locked case first, since it is easier.

        if(displayIsLocked){

            Resources res = mContext.getResources();

            String[] grades = res.getStringArray(R.array.coin_grades);
            TextView gradeTextView = (TextView) coinView.findViewById(R.id.grade_textview);
            int gradeIndex = advancedGrades.get(position);
            if(gradeIndex != 0){
                // Preface the grade with 'Grade:'
                gradeTextView.setText("Grade: " + grades[gradeIndex] + "  ");
            } else {
                // 'Grade:' will be printed
                gradeTextView.setText(grades[gradeIndex] + "  ");
            }

            String[] quantities = res.getStringArray(R.array.coin_quantities);
            TextView quantitiesTextView = (TextView) coinView.findViewById(R.id.quantity_textview);
            quantitiesTextView.setText("Quantity: " + quantities[advancedQuantities.get(position)] + "  ");

            TextView notesTextView = (TextView) coinView.findViewById(R.id.notes_textview);
            notesTextView.setText("Notes:\n" + advancedNotes.get(position));
            return;
        }

        // The collection is not locked, we need to set up the spinners and edittext

        // Setup the spinner that will let you select the coin grade
        Spinner gradeSelector = (Spinner) coinView.findViewById(R.id.grade_selector);
        gradeSelector.setTag(positionObj);

        if(mGradeArrayAdapter == null){
            // Create the adapter that will handle spinner selections
            mGradeArrayAdapter = ArrayAdapter.createFromResource(
                mContext, R.array.coin_grades, android.R.layout.simple_spinner_item);
            mGradeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        gradeSelector.setAdapter(mGradeArrayAdapter);

        // Put the spinner at the value we have stored for this coin
        gradeSelector.setSelection(advancedGrades.get(position), false);

        if(mGradeOnItemSelectedListener == null){
            mGradeOnItemSelectedListener = new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent,
                        View view, int pos, long id) {

                    int posPrim = (int) parent.getTag();

                    // Update the values in the lists if this is a new value
                    if(pos != CoinSlotAdapter.this.advancedGrades.get(posPrim)){
                        // Value has changed
                        CoinSlotAdapter.this.advancedGrades.set(posPrim, pos);
                        CoinSlotAdapter.this.indexHasChanged[posPrim] = true;

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
        Spinner quantitySelector = (Spinner) coinView.findViewById(R.id.quantity_selector);
        quantitySelector.setTag(positionObj);

        if(mQuantityArrayAdapter == null){
            mQuantityArrayAdapter = ArrayAdapter.createFromResource(
                mContext, R.array.coin_quantities, android.R.layout.simple_spinner_item);
            mQuantityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        quantitySelector.setAdapter(mQuantityArrayAdapter);

        // Put the spinner at the value we have stored for this coin
        quantitySelector.setSelection(advancedQuantities.get(position), false);

        if(mQuantityOnItemSelectedListener == null) {
            mQuantityOnItemSelectedListener = new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent,
                                           View view, int pos, long id) {

                    int posPrim = (Integer) parent.getTag();

                    // Update the values in the lists if this is a new value
                    if(pos != CoinSlotAdapter.this.advancedQuantities.get(posPrim)){
                        // Value has changed
                        CoinSlotAdapter.this.advancedQuantities.set(posPrim, pos);
                        CoinSlotAdapter.this.indexHasChanged[posPrim] = true;

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
        EditText notesEditText = (EditText) coinView.findViewById(R.id.notes_edit_text);

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
        String text = advancedNotes.get(position);
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
                    String newText = s.toString();
                    if (CoinSlotAdapter.this.advancedNotes.get(posPrim).equals(newText)) {
                        //Log.e(MainApplication.APP_NAME, "Text equals what is currently stored, not updating");
                        return;
                    }

                    // Change detected - Temporarily store the modified text
                    CoinSlotAdapter.this.advancedNotes.set(posPrim, newText);
                    CoinSlotAdapter.this.indexHasChanged[posPrim] = true;
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
