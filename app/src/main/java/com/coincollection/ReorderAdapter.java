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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coincollection.helper.ItemTouchHelperAdapter;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * RecyclerView.Adapter that supports reordering the coin collections
 */
public class ReorderAdapter extends RecyclerView.Adapter<ReorderViewHolder>
        implements ItemTouchHelperAdapter {
    public final ArrayList<CollectionListInfo> mItems;

    ReorderAdapter(ArrayList<CollectionListInfo> items) {
        super();
        mItems = items;
    }

    @Override
    @NonNull
    public ReorderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reorder_list_element, parent, false);

        return new ReorderViewHolder(view);
    }

    // Suppressing this as we've added up and down arrows for accessibility (in addition to drag support)
    @Override
    public void onBindViewHolder(final ReorderViewHolder holder, int position) {

        // Populate the list element
        CollectionListInfo item = mItems.get(position);
        Resources res = holder.view.getResources();
        MainActivity.buildListElement(item, holder.view, res);

        // Add the reorder on-touch adapter
        String tableName = item.getName();
        if (tableName != null) {

            // Move the view up when the up arrow is clicked
            ImageView upArrowView = holder.view.findViewById(R.id.move_up_arrow);
            upArrowView.setOnClickListener(v -> {
                int clickIndex = holder.getBindingAdapterPosition();
                if (clickIndex != 0) {
                    onItemMove(clickIndex, clickIndex - 1);
                }
            });
            upArrowView.setContentDescription(res.getString(R.string.reorder_move_up_context_desc, tableName));

            // Move the view down when the down arrow is clicked
            ImageView downArrowView = holder.view.findViewById(R.id.move_down_arrow);
            downArrowView.setOnClickListener(v -> {
                int clickIndex = holder.getBindingAdapterPosition();
                if ((clickIndex + 1) < getItemCount()) {
                    onItemMove(clickIndex, clickIndex + 1);
                }
            });
            downArrowView.setContentDescription(res.getString(R.string.reorder_move_down_context_desc, tableName));
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
