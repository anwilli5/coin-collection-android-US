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

import com.spencerpages.BuildConfig;
import com.spencerpages.R;

/**
 * The app's about box, showing the version and the attribution text.
 */
public class AboutDialogFragment extends DialogFragment {

    private static final String ARG_ATTRIBUTION = "attribution";

    /**
     * Creates the about dialog
     *
     * @param attributionText the attribution text to display
     * @return the new dialog fragment
     */
    public static AboutDialogFragment newInstance(@NonNull String attributionText) {
        Bundle args = new Bundle();
        args.putString(ARG_ATTRIBUTION, attributionText);
        AboutDialogFragment fragment = new AboutDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams") // A dialog's view has no parent to attach to
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.info_popup, null);

        TextView titleView = layout.findViewById(R.id.info_title);
        titleView.setText(getString(R.string.info_name_version, BuildConfig.VERSION_NAME));
        TextView attributionView = layout.findViewById(R.id.info_attribution);
        attributionView.setText(requireArguments().getString(ARG_ATTRIBUTION, ""));

        return new AlertDialog.Builder(requireContext())
                .setView(layout)
                .create();
    }
}
