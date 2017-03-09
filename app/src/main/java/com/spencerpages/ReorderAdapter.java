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

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spencerpages.helper.ItemTouchHelperAdapter;
import com.spencerpages.helper.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * RecyclerView.Adapter that supports reordering the coin collections
 */
public class ReorderAdapter extends RecyclerView.Adapter<ReorderViewHolder>
        implements ItemTouchHelperAdapter{

    private final ArrayList<CollectionListInfo> mItems;
    private final OnStartDragListener mDragStartListener;

    public ReorderAdapter(ArrayList<CollectionListInfo> items, OnStartDragListener dragStartListener) {
        super();
        mItems = items;
        mDragStartListener = dragStartListener;
    }

    @Override
    public ReorderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element, parent, false);

        return new ReorderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReorderViewHolder holder, int position) {

        // TODO Copied from the adapter that handles mainActivity, refactor it
        // so that this code only lives in one place
        CollectionListInfo item = mItems.get(position);

        String tableName = item.getName();

        int total = item.getCollected();
        if (tableName != null) {

            //Get the image
            ImageView image = (ImageView) holder.view.findViewById(R.id.imageView1);
            if (image != null) {
                image.setBackgroundResource(item.getCoinImageIdentifier());
            }

            TextView tt = (TextView) holder.view.findViewById(R.id.textView1);
            if (tt != null) {
                tt.setText(tableName);
            }

            TextView mt = (TextView) holder.view.findViewById(R.id.textView2);
            if (mt != null) {
                mt.setText(total + "/" + item.getMax());
            }

            TextView bt = (TextView) holder.view.findViewById(R.id.textView3);
            if (total >= item.getMax()) {
                // The collection is complete
                if (bt != null) {
                    bt.setText("Collection Complete!");
                }
            } else {
                bt.setText("");
            }

            // Start a drag whenever the handle view is touched
            image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
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
