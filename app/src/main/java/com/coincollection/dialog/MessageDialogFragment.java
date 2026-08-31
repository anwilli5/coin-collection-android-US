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

import static com.coincollection.dialog.DialogRequests.REQUEST_NONE;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * A simple informational dialog showing a message, optionally with a single
 * acknowledgement button. Used for error messages, warnings that need no
 * decision, and the one-time help dialogs (which need the acknowledgement
 * reported back so the "don't show again" preference can be written).
 */
public class MessageDialogFragment extends BaseDialogFragment {

    private static final String ARG_BUTTON_TEXT = "buttonText";

    /**
     * Creates a dismissible message dialog with no buttons
     *
     * @param message the message to show
     * @return the new dialog fragment
     */
    public static MessageDialogFragment newCancelableInstance(@NonNull String message) {
        Bundle args = baseArgs(DialogRequests.REQUEST_KEY_BASE_ACTIVITY, REQUEST_NONE, null);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_CANCELABLE, true);
        args.putInt(ARG_BUTTON_TEXT, 0);
        return newInstance(args);
    }

    /**
     * Creates a message dialog with a single acknowledgement button
     *
     * @param requestKey   key the host listens on
     * @param requestId    identifies this message, or REQUEST_NONE if the host
     *                     doesn't need to be told it was acknowledged
     * @param message      the message to show
     * @param buttonTextId resource id of the acknowledgement button label
     * @param payload      optional data echoed back with the result
     * @return the new dialog fragment
     */
    public static MessageDialogFragment newAcknowledgeInstance(@NonNull String requestKey, int requestId,
                                                               @NonNull String message, int buttonTextId,
                                                               @Nullable Bundle payload) {
        Bundle args = baseArgs(requestKey, requestId, payload);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_CANCELABLE, false);
        args.putInt(ARG_BUTTON_TEXT, buttonTextId);
        return newInstance(args);
    }

    /**
     * Creates a message dialog from a prepared argument bundle
     *
     * @param args the argument bundle
     * @return the new dialog fragment
     */
    private static MessageDialogFragment newInstance(Bundle args) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        setCancelable(args.getBoolean(ARG_CANCELABLE, true));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setMessage(getArgMessage());

        String title = getArgTitle();
        if (title != null) {
            builder.setTitle(title);
        }
        int buttonTextId = args.getInt(ARG_BUTTON_TEXT, 0);
        if (buttonTextId != 0) {
            builder.setPositiveButton(buttonTextId, (dialog, which) -> sendResult(new Bundle()));
        }
        return builder.create();
    }
}
