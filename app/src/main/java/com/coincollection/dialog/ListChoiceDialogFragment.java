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

import static com.coincollection.dialog.DialogRequests.KEY_SELECTED_INDEX;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * A dialog presenting a list of choices. The index of the selected item is
 * delivered to the host, which is responsible for mapping it back to an action.
 */
public class ListChoiceDialogFragment extends BaseDialogFragment {

    private static final String ARG_ITEMS = "items";

    /**
     * Creates a list choice dialog
     *
     * @param requestKey key the host listens on
     * @param requestId  identifies which list this is
     * @param title      dialog title, or null for none
     * @param items      the choices to display
     * @param payload    optional data echoed back with the result
     * @return the new dialog fragment
     */
    public static ListChoiceDialogFragment newInstance(@NonNull String requestKey, int requestId,
                                                       @Nullable String title, @NonNull String[] items,
                                                       @Nullable Bundle payload) {
        Bundle args = baseArgs(requestKey, requestId, payload);
        args.putString(ARG_TITLE, title);
        args.putStringArray(ARG_ITEMS, items);
        ListChoiceDialogFragment fragment = new ListChoiceDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String[] items = args.getStringArray(ARG_ITEMS);
        if (items == null) {
            items = new String[0];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setItems(items, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putInt(KEY_SELECTED_INDEX, which);
                    sendResult(result);
                });

        String title = getArgTitle();
        if (title != null) {
            builder.setTitle(title);
        }
        return builder.create();
    }
}
