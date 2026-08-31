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
import static com.coincollection.dialog.DialogRequests.KEY_COIN_IMAGE_ID;
import static com.coincollection.dialog.DialogRequests.KEY_COIN_MINT;
import static com.coincollection.dialog.DialogRequests.KEY_COIN_NAME;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.ImageSpinnerAdapter;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;

/**
 * Dialog for creating a new coin or editing an existing one. The name, mint and
 * image are collected here and reported back to the host, which validates the
 * name and applies the change.
 */
public class CoinEditDialogFragment extends BaseDialogFragment {

    private static final String ARG_COIN_SLOT = "coinSlot";
    private static final String ARG_COLLECTION_TYPE_INDEX = "collectionTypeIndex";

    /**
     * Creates a coin create/edit dialog
     *
     * @param requestKey           key the host listens on
     * @param requestId            identifies this dialog
     * @param title                dialog title
     * @param coinSlot             the coin being edited, or null when creating one
     * @param collectionTypeIndex  index into MainApplication.COLLECTION_TYPES for
     *                             the collection this coin belongs to
     * @param payload              optional data echoed back with the result
     * @return the new dialog fragment
     */
    public static CoinEditDialogFragment newInstance(@NonNull String requestKey, int requestId,
                                                     @Nullable String title, @Nullable CoinSlot coinSlot,
                                                     int collectionTypeIndex, @Nullable Bundle payload) {
        Bundle args = baseArgs(requestKey, requestId, payload);
        args.putString(ARG_TITLE, title);
        args.putParcelable(ARG_COIN_SLOT, coinSlot);
        args.putInt(ARG_COLLECTION_TYPE_INDEX, collectionTypeIndex);
        CoinEditDialogFragment fragment = new CoinEditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams") // A dialog's view has no parent to attach to
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        CoinSlot coinSlot = args.getParcelable(ARG_COIN_SLOT);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout coinEditView = (LinearLayout) inflater.inflate(R.layout.coin_update_layout, null);
        EditText nameInput = coinEditView.findViewById(R.id.coin_name_edittext);
        EditText mintInput = coinEditView.findViewById(R.id.coin_mint_edittext);
        Spinner imgSpinner = coinEditView.findViewById(R.id.coin_image_select);
        LinearLayout imgRow = coinEditView.findViewById(R.id.coin_image_row);

        // Set filters to block out bad characters
        InputFilter nameFilter = getCollectionOrCoinNameFilter();
        nameInput.setFilters(new InputFilter[]{nameFilter});
        mintInput.setFilters(new InputFilter[]{nameFilter});

        setupCoinImageSpinner(coinSlot, imgSpinner, imgRow, args.getInt(ARG_COLLECTION_TYPE_INDEX));

        // Only seed the inputs the first time - on a recreate the view state
        // restores whatever the user had entered
        if (savedInstanceState == null) {
            nameInput.setText((coinSlot != null) ? coinSlot.getIdentifier() : "");
            mintInput.setText((coinSlot != null) ? coinSlot.getMint() : "");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(coinEditView)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putString(KEY_COIN_NAME, nameInput.getText().toString());
                    result.putString(KEY_COIN_MINT, mintInput.getText().toString());
                    result.putInt(KEY_COIN_IMAGE_ID, getSpinnerImageId(imgSpinner));
                    sendResult(result);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        String title = getArgTitle();
        if (title != null) {
            builder.setTitle(title);
        }
        return builder.create();
    }

    /**
     * Get image id value from the coin image spinner
     *
     * @param imgSpinner coin image spinner
     * @return image id
     */
    private static int getSpinnerImageId(Spinner imgSpinner) {
        if (imgSpinner.getVisibility() == View.VISIBLE) {
            // Get the selected image ID when the button is pressed
            int selectedPosition = imgSpinner.getSelectedItemPosition();
            if (selectedPosition == AdapterView.INVALID_POSITION) {
                return -1;
            } else {
                return selectedPosition - 1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Set up the coin image spinner, if needed
     *
     * @param coinSlot            coin slot being modified, or null when creating a coin
     * @param imgSpinner          coin image spinner
     * @param imgRow              row holding the spinner
     * @param collectionTypeIndex index of the collection type the coin belongs to
     */
    private void setupCoinImageSpinner(CoinSlot coinSlot, Spinner imgSpinner, LinearLayout imgRow,
                                       int collectionTypeIndex) {
        CollectionInfo collectionTypeObj = MainApplication.COLLECTION_TYPES[collectionTypeIndex];
        Object[][] imageIdData = collectionTypeObj.getImageIds();

        if (imageIdData.length != 0) {
            imgSpinner.setVisibility(View.VISIBLE);
            imgRow.setVisibility(View.VISIBLE);

            // Ignore image id here to show the actual default for this name
            int defaultResId = (coinSlot != null) ? collectionTypeObj.getCoinSlotImage(coinSlot, true)
                    : collectionTypeObj.getCoinImageIdentifier();
            int defaultImageId = (coinSlot != null) ? coinSlot.getImageId() : -1;

            // Create lists to hold the names and resIds
            ArrayList<String> names = new ArrayList<>();
            ArrayList<Integer> resIds = new ArrayList<>();
            names.add(getString(R.string.img_default));
            resIds.add(defaultResId);
            for (Object[] entry : imageIdData) {
                names.add((String) entry[0]);
                resIds.add((Integer) entry[1]);
            }

            // Set up the image select spinner
            ImageSpinnerAdapter adapter = new ImageSpinnerAdapter(requireContext(), names, resIds);
            imgSpinner.setAdapter(adapter);

            // Set the selected position based on the current image id
            imgSpinner.setSelection(defaultImageId + 1);
        } else {
            imgSpinner.setVisibility(View.GONE);
            imgRow.setVisibility(View.GONE);
        }
    }
}
