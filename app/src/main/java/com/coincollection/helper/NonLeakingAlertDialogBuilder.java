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

package com.coincollection.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

/**
 * This class wraps AlertDialog.Builder to prevent memory leaks
 */
public class NonLeakingAlertDialogBuilder extends AlertDialog.Builder {

    private DetachableDialogAlertOnClickListener mPositiveButtonOnClickListener = null;
    private DetachableDialogAlertOnClickListener mNegativeButtonOnClickListener = null;
    private DetachableDialogAlertOnClickListener mSetItemsOnClickListener = null;

    public NonLeakingAlertDialogBuilder(Context context) {
        super(context);
    }

    @Override
    public AlertDialog create() {
        AlertDialog alertDialog = super.create();
        // Register callbacks to remove the OnClickListener once the alert detaches
        // - This is only supported on higher APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mPositiveButtonOnClickListener != null) {
                mPositiveButtonOnClickListener.setOnWindowDetachedListener(alertDialog);
            }
            if (mNegativeButtonOnClickListener != null) {
                mNegativeButtonOnClickListener.setOnWindowDetachedListener(alertDialog);
            }
            if (mSetItemsOnClickListener != null) {
                mSetItemsOnClickListener.setOnWindowDetachedListener(alertDialog);
            }
        }
        return alertDialog;
    }

    @Override
    public AlertDialog.Builder setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
        super.setPositiveButton(textId, listener);
        mPositiveButtonOnClickListener = DetachableDialogAlertOnClickListener.wrapOnClickListener(listener);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        super.setPositiveButton(text, listener);
        mPositiveButtonOnClickListener = DetachableDialogAlertOnClickListener.wrapOnClickListener(listener);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setNegativeButton(int textId, DialogInterface.OnClickListener listener) {
        super.setNegativeButton(textId, listener);
        mNegativeButtonOnClickListener = DetachableDialogAlertOnClickListener.wrapOnClickListener(listener);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        super.setNegativeButton(text, listener);
        mNegativeButtonOnClickListener = DetachableDialogAlertOnClickListener.wrapOnClickListener(listener);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setItems(int itemsId, DialogInterface.OnClickListener listener) {
        super.setItems(itemsId, listener);
        mSetItemsOnClickListener = DetachableDialogAlertOnClickListener.wrapOnClickListener(listener);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
        super.setItems(items, listener);
        mSetItemsOnClickListener = DetachableDialogAlertOnClickListener.wrapOnClickListener(listener);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setTitle(int titleId) {
        super.setTitle(titleId);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setTitle(CharSequence title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setMessage(int messageId) {
        super.setMessage(messageId);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setMessage(CharSequence message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    @Override
    public NonLeakingAlertDialogBuilder setView(View view) {
        super.setView(view);
        return this;
    }

    // Note: May need to extend more of the AlertDialog.Builder methods that can register
    //       OnClickListener, if our app eventually makes use of those
}
