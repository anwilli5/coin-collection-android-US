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

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.spencerpages.helper.ItemTouchHelperViewHolder;

/**
 * ViewHolder used by the ReorderCollections Activity
 */
public class ReorderViewHolder extends RecyclerView.ViewHolder implements
        ItemTouchHelperViewHolder{

    public final View view;

    public ReorderViewHolder(View itemView) {
        super(itemView);
        view = itemView;
    }

    @Override
    public void onItemSelected() {
        view.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear() {
        view.setBackgroundColor(0);
    }

}