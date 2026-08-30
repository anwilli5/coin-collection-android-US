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

import static com.coincollection.CoinPageCreator.getCollectionOrCoinNameFilter;
import static com.coincollection.dialog.DialogRequests.KEY_TEXT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.spencerpages.R;

/**
 * A dialog collecting a single line of text, used for renaming a collection.
 * The input is inflated from a layout with an id so the text the user has typed
 * is restored automatically if the dialog is recreated.
 */
public class TextInputDialogFragment extends BaseDialogFragment {

    private static final String ARG_INITIAL_TEXT = "initialText";

    /**
     * Creates a text input dialog
     *
     * @param requestKey  key the host listens on
     * @param requestId   identifies which input this is
     * @param title       dialog title, or null for none
     * @param initialText text to pre-populate the input with
     * @param payload     optional data echoed back with the result
     * @return the new dialog fragment
     */
    public static TextInputDialogFragment newInstance(@NonNull String requestKey, int requestId,
                                                      @Nullable String title, @NonNull String initialText,
                                                      @Nullable Bundle payload) {
        Bundle args = baseArgs(requestKey, requestId, payload);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_INITIAL_TEXT, initialText);
        TextInputDialogFragment fragment = new TextInputDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams") // A dialog's view has no parent to attach to
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View inputView = inflater.inflate(R.layout.dialog_text_input, null);
        EditText input = inputView.findViewById(R.id.dialog_text_input);
        input.setFilters(new InputFilter[]{getCollectionOrCoinNameFilter()});
        // Only seed the initial text the first time - on a recreate the view
        // state restores whatever the user had typed
        if (savedInstanceState == null) {
            input.setText(args.getString(ARG_INITIAL_TEXT, ""));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(inputView)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putString(KEY_TEXT, input.getText().toString());
                    sendResult(result);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        String title = getArgTitle();
        if (title != null) {
            builder.setTitle(title);
        }
        return builder.create();
    }
}
