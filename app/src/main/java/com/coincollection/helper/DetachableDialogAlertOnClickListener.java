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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * This class is needed to resolve memory leaks caused by the AlertDialogs
 * - https://developer.squareup.com/blog/a-small-leak-will-sink-a-great-ship/
 */
public final class DetachableDialogAlertOnClickListener implements DialogInterface.OnClickListener {

    public static DetachableDialogAlertOnClickListener wrapOnClickListener(DialogInterface.OnClickListener delegate) {
        return new DetachableDialogAlertOnClickListener(delegate);
    }

    private DialogInterface.OnClickListener mOnClickListener;

    private DetachableDialogAlertOnClickListener(DialogInterface.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(dialog, which);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void setOnWindowDetachedListener(@NonNull final Dialog dialog) {
        dialog.getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
                    @Override
                    public void onWindowAttached() {
                        // Nothing needed here
                    }

                    @Override
                    public void onWindowDetached() {
                        // Disconnect the listener so that GC can perform clean-up
                        mOnClickListener = null;
                    }
                });
    }
}
