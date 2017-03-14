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

package com.spencerpages.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.spencerpages.MainApplication;
import com.spencerpages.R;
import com.spencerpages.core.helper.OnStartDragListener;
import com.spencerpages.core.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;

/**
 * Fragment utilizing a RecyclerView to implement a collection re-ordering capability
 */
public class ReorderCollections extends Fragment implements OnStartDragListener {

    private ItemTouchHelper mItemTouchHelper;
    private ArrayList<CollectionListInfo> mItems = null;
    private Boolean mUnsavedChanges = false;

    public void setCollectionList(ArrayList<CollectionListInfo> items) {
        mItems = items;
    }

    private void setUnsavedChanges(Boolean unsavedChanges){
        mUnsavedChanges = unsavedChanges;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Check whether it is the users have seen this page
        SharedPreferences mainPreferences = getActivity().getSharedPreferences(MainApplication.PREFS, Activity.MODE_PRIVATE);
        if(mainPreferences.getBoolean("reorder_help1", true)){
            // Show the user how to do everything
            // TODO Move to the strings resource page
            CharSequence text = "Drag a collection's coin image to reposition it in the list of collections. To scroll through the list, swipe using the right half of the screen. Tap 'Save' when finished.";

            AlertDialog.Builder builder = new AlertDialog.Builder(((MainActivity)getActivity()).getContext());
            builder.setMessage(text)
                    .setCancelable(false)
                    .setPositiveButton("Okay!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            SharedPreferences mainPreferences = getActivity().getSharedPreferences(MainApplication.PREFS, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mainPreferences.edit();
                            editor.putBoolean("reorder_help1", false);
                            editor.commit(); // .apply() in later APIs
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        return inflater.inflate(R.layout.activity_reorder_collections, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Indicate that we have an options menu so that we can get the 'Up'
        // menu click and return back to the mainActivity.  Without this,
        // the framework won't call onOptionsItemSelected
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the screen rotated and our view got destroyed/recreated,
        // grab the list of collections from the old view (that we stored
        // in the bundle.)
        if(savedInstanceState != null){
            mItems = savedInstanceState.getParcelableArrayList("mItems");
            mUnsavedChanges = savedInstanceState.getBoolean("mUnsavedChanges");
        }

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.reorder_collections_recycler_view);

        mRecyclerView.setBackgroundColor(Color.BLACK);

        // Indicate that the contents do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set up the adapter that provides the collection entries
        ReorderAdapter mAdapter = new ReorderAdapter(mItems, this);
        mRecyclerView.setAdapter(mAdapter);

        // Register the ItemTouchHelper Callback so that we can allow reordering
        // collections when the user drags the coin images or long presses and then
        // drags.
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        // Register a callback so we can know when the list has been reordered
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);

                Log.d("onItemRangeMoved", String.valueOf(fromPosition) + " " + String.valueOf(toPosition) + " " + String.valueOf(itemCount));

                ReorderCollections fragment = (ReorderCollections) getFragmentManager().findFragmentByTag("ReorderFragment");

                fragment.setUnsavedChanges(true);
                fragment.showUnsavedTextView();
            }
        });

        if(mUnsavedChanges){
            showUnsavedTextView();
        }

        //http://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if(mUnsavedChanges){
                        showUnsavedChangesMessage();
                    } else {
                        closeFragment();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        // Save off our collection list in the event of a screen orientation change
        outState.putParcelableArrayList("mItems", mItems);
        outState.putBoolean("mUnsavedChanges", mUnsavedChanges);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu - it has the 'Save' button.  This is also another
        // thing necessary for proper 'Up' button operation.
        inflater.inflate(R.menu.menu_reorder_collections, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case android.R.id.home:

                if(mUnsavedChanges){
                    showUnsavedChangesMessage();
                } else {
                    closeFragment();
                }
                return true;

            case R.id.save_reordered_collections:

                MainActivity activity = (MainActivity) getActivity();

                activity.handleCollectionsReordered(mItems);

                Context context = activity.getApplicationContext();
                CharSequence text = "Saved changes successfully";
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();

                this.hideUnsavedTextView();
                this.setUnsavedChanges(false);
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeFragment(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .remove(this)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit();
        fm.popBackStack();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void showUnsavedTextView() {

        TextView unsavedMessageView = (TextView) getView().findViewById(R.id.unsaved_message_textview_reorder);

        unsavedMessageView.setVisibility(View.VISIBLE);
    }

    private void hideUnsavedTextView() {

        TextView unsavedMessageView = (TextView) getView().findViewById(R.id.unsaved_message_textview_reorder);

        unsavedMessageView.setVisibility(View.GONE);
    }

    private void showUnsavedChangesMessage(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Leaving this page will erase your unsaved changes, are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeFragment();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}