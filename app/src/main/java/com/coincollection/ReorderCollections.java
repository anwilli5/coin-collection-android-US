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

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coincollection.helper.OnStartDragListener;
import com.coincollection.helper.SimpleItemTouchHelperCallback;
import com.spencerpages.BuildConfig;
import com.spencerpages.R;

import java.util.ArrayList;

/**
 * Fragment utilizing a RecyclerView to implement a collection re-ordering capability
 */
public class ReorderCollections extends Fragment implements OnStartDragListener {

    public final static String REORDER_COLLECTION = "ReorderFragment";

    private ItemTouchHelper mItemTouchHelper;
    private ArrayList<CollectionListInfo> mItems = null;
    private Boolean mUnsavedChanges = false;
    public ReorderAdapter mAdapter;

    public void setCollectionList(ArrayList<CollectionListInfo> items) {
        mItems = items;
    }

    private void setUnsavedChanges(Boolean unsavedChanges) {
        mUnsavedChanges = unsavedChanges;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Show help to reorder the collection
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.createAndShowHelpDialog("reorder_help1", R.string.tutorial_reorder_collections);

            // Setup the actionbar for the reorder page
            if (activity.mActionBar != null) {
                activity.mActionBar.setTitle(activity.mRes.getString(R.string.reorder_collection));
                activity.mActionBar.setDisplayHomeAsUpEnabled(true);
                activity.mActionBar.setHomeButtonEnabled(true);
            }
        }

        return inflater.inflate(R.layout.activity_reorder_collections, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Indicate that we have an options menu so that we can get the 'Up'
        // menu click and return back to the mainActivity.  Without this,
        // the framework won't call onOptionsItemSelected
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the screen rotated and our view got destroyed/recreated,
        // grab the list of collections from the old view (that we stored
        // in the bundle.)
        if (savedInstanceState != null) {
            mItems = savedInstanceState.getParcelableArrayList("mItems");
            mUnsavedChanges = savedInstanceState.getBoolean("mUnsavedChanges");
        }

        RecyclerView mRecyclerView = view.findViewById(R.id.reorder_collections_recycler_view);

        mRecyclerView.setBackgroundColor(Color.BLACK);

        // Indicate that the contents do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set up the adapter that provides the collection entries
        mAdapter = new ReorderAdapter(mItems);
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

                if (BuildConfig.DEBUG) {
                    Log.d("onItemRangeMoved", fromPosition + " " + toPosition + " " + itemCount);
                }

                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager == null) return;

                ReorderCollections fragment = (ReorderCollections) fragmentManager.findFragmentByTag(REORDER_COLLECTION);
                if (fragment == null) return;

                fragment.setUnsavedChanges(true);
                fragment.showUnsavedTextView();
            }
        });

        if (mUnsavedChanges) {
            showUnsavedTextView();
        }

        //http://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (mUnsavedChanges) {
                    showUnsavedChangesAlertAndExitFragment();
                } else {
                    closeFragment();
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save off our collection list in the event of a screen orientation change
        outState.putParcelableArrayList("mItems", mItems);
        outState.putBoolean("mUnsavedChanges", mUnsavedChanges);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu - it has the 'Save' button.  This is also another
        // thing necessary for proper 'Up' button operation.
        inflater.inflate(R.menu.menu_reorder_collections, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (mUnsavedChanges) {
                showUnsavedChangesAlertAndExitFragment();
            } else {
                closeFragment();
            }
            return true;
        } else if (itemId == R.id.save_reordered_collections) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                Resources res = activity.getResources();

                activity.handleCollectionsReordered(mItems);

                CharSequence text = res.getString(R.string.changes_saved);
                Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
                toast.show();

                this.hideUnsavedTextView();
                this.setUnsavedChanges(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeFragment() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            FragmentManager fm = activity.getSupportFragmentManager();
            fm.beginTransaction()
                    .remove(this)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
            fm.popBackStack();
        }
    }

    /**
     * Shows the unsaved TextView
     */
    private void showUnsavedTextView() {

        View view = getView();
        if (view == null) return;

        TextView unsavedMessageView = view.findViewById(R.id.unsaved_message_textview_reorder);
        unsavedMessageView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the unsaved TextView
     */
    private void hideUnsavedTextView() {

        View view = getView();
        if (view == null) return;

        TextView unsavedMessageView = view.findViewById(R.id.unsaved_message_textview_reorder);
        unsavedMessageView.setVisibility(View.GONE);
    }

    /**
     * Show an alert that changes aren't saved before exiting fragment
     */
    private void showUnsavedChangesAlertAndExitFragment() {
        final MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            Resources res = activity.getResources();
            activity.showAlert(activity.newBuilder()
                    .setMessage(res.getString(R.string.dialog_unsaved_changes_exit))
                    .setCancelable(false)
                    .setPositiveButton(res.getString(R.string.okay), (dialog, id) -> {
                        dialog.dismiss();
                        closeFragment();
                    })
                    .setNegativeButton(res.getString(R.string.cancel), (dialog, id) -> dialog.cancel()));
        }
    }
}