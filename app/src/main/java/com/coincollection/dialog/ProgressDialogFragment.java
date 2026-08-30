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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.spencerpages.R;

/**
 * Replacement for the deprecated {@code android.app.ProgressDialog}. The
 * FragmentManager owns this dialog, so a long-running task's progress UI is
 * restored automatically when the host activity is recreated instead of being
 * leaked along with the destroyed window.
 */
public class ProgressDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    private TextView mMessageView;

    /**
     * Creates a progress dialog
     *
     * @param message the message to show alongside the spinner
     * @return the new dialog fragment
     */
    public static ProgressDialogFragment newInstance(@NonNull String message) {
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams") // A dialog's view has no parent to attach to
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // The task can't be interrupted, so the dialog can't be dismissed by
        // the user - only by the task completing
        setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View progressView = inflater.inflate(R.layout.dialog_progress, null);
        mMessageView = progressView.findViewById(R.id.progress_message);
        mMessageView.setText(requireArguments().getString(ARG_MESSAGE, ""));

        Dialog dialog = new AlertDialog.Builder(requireContext())
                .setView(progressView)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        mMessageView = null;
        super.onDestroyView();
    }

    /**
     * Updates the message shown alongside the spinner. Lets an already visible
     * progress dialog be reused when a task re-reports its progress (e.g. after
     * the host activity is recreated) instead of being torn down and re-shown
     *
     * @param message the new message
     */
    public void setMessage(@NonNull String message) {
        Bundle args = getArguments();
        if (args != null) {
            args.putString(ARG_MESSAGE, message);
        }
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }
}
