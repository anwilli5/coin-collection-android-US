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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.spencerpages.R;
import com.coincollection.helper.ItemTouchHelperAdapter;
import com.coincollection.helper.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * RecyclerView.Adapter that supports reordering the coin collections
 */
public class ReorderAdapter extends RecyclerView.Adapter<ReorderViewHolder>
        implements ItemTouchHelperAdapter{
    private final ArrayList<CollectionListInfo> mItems;
    private final OnStartDragListener mDragStartListener;

    ReorderAdapter(ArrayList<CollectionListInfo> items, OnStartDragListener dragStartListener) {
        super();
        mItems = items;
        mDragStartListener = dragStartListener;
    }

    @Override
    @NonNull
    public ReorderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element, parent, false);

        return new ReorderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReorderViewHolder holder, int position) {

        // Populate the list element
        CollectionListInfo item = mItems.get(position);
        Resources res = holder.view.getResources();
        MainActivity.buildListElement(item, holder.view, res);

        // Add the reorder on-touch adapter
        String tableName = item.getName();
        if (tableName != null) {
            // Get the parent view
            LinearLayout parent = holder.view.findViewById(R.id.listElementParent);

            // Start a drag whenever the handle view is touched
            parent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mDragStartListener.onStartDrag(holder);
                            break;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            break;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
