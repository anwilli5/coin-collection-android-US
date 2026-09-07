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

package com.coincollection.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.spencerpages.R;

/**
 * A yes/no confirmation dialog. The host is only told about the positive
 * answer - declining simply closes the dialog, which matches how every
 * confirmation in the app behaves.
 */
public class ConfirmationDialogFragment extends BaseDialogFragment {

    private static final String ARG_POSITIVE_TEXT = "positiveText";
    private static final String ARG_NEGATIVE_TEXT = "negativeText";

    /**
     * Creates a confirmation dialog
     *
     * @param requestKey     key the host listens on
     * @param requestId      identifies which confirmation this is
     * @param title          dialog title, or null for none
     * @param message        dialog message
     * @param positiveTextId resource id of the confirm button label
     * @param negativeTextId resource id of the decline button label, or 0 to
     *                       omit the decline button
     * @param payload        optional data echoed back with the result
     * @return the new dialog fragment
     */
    public static ConfirmationDialogFragment newInstance(@NonNull String requestKey, int requestId,
                                                         @Nullable String title, @NonNull String message,
                                                         int positiveTextId, int negativeTextId,
                                                         @Nullable Bundle payload) {
        Bundle args = baseArgs(requestKey, requestId, payload);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putInt(ARG_POSITIVE_TEXT, positiveTextId);
        args.putInt(ARG_NEGATIVE_TEXT, negativeTextId);
        return newInstance(args);
    }

    /**
     * Creates a confirmation dialog from a prepared argument bundle
     *
     * @param args the argument bundle
     * @return the new dialog fragment
     */
    private static ConfirmationDialogFragment newInstance(Bundle args) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        // Confirmations must be answered deliberately, so they can't be
        // dismissed by tapping outside or pressing back
        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setMessage(getArgMessage())
                .setPositiveButton(args.getInt(ARG_POSITIVE_TEXT, R.string.yes),
                        (dialog, which) -> sendResult(new Bundle()));

        String title = getArgTitle();
        if (title != null) {
            builder.setTitle(title);
        }
        int negativeTextId = args.getInt(ARG_NEGATIVE_TEXT, 0);
        if (negativeTextId != 0) {
            builder.setNegativeButton(negativeTextId, (dialog, which) -> dialog.dismiss());
        }
        return builder.create();
    }
}
