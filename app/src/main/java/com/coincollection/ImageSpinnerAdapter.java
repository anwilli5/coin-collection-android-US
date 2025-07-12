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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.spencerpages.R;

import java.util.ArrayList;

public class ImageSpinnerAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private final ArrayList<String> mNames;
    private final ArrayList<Integer> mResIds;
    private final Float mImgScale;

    public ImageSpinnerAdapter(Context context, ArrayList<String> names, ArrayList<Integer> resIds) {
        super(context, R.layout.coin_image_spinner_item, names);
        this.mContext = context;
        this.mNames = names;
        this.mResIds = resIds;
        this.mImgScale = 1.0f;
    }

    public ImageSpinnerAdapter(Context context, ArrayList<String> names, ArrayList<Integer> resIds,
                               Float imgScale) {
        super(context, R.layout.coin_image_spinner_item, names);
        this.mContext = context;
        this.mNames = names;
        this.mResIds = resIds;
        this.mImgScale = imgScale;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, parent);
    }

    private View createCustomView(int position, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.coin_image_spinner_item, parent, false);

        // Set the text
        TextView textView = (TextView) view;
        textView.setText(mNames.get(position));

        // Set the image
        int imageId = mResIds.get(position);
        if (imageId > 0) {
            Drawable image = ContextCompat.getDrawable(mContext, mResIds.get(position));
            if ((mImgScale != 1.0) && (image != null)) {
                // Scale the image if desired
                Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
                int width = (int) ((float) bitmap.getWidth() * mImgScale);
                int height = (int) ((float) bitmap.getHeight() * mImgScale);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                image = new BitmapDrawable(mContext.getResources(), scaledBitmap);
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null);
        }
        return view;
    }
}
