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

/**
 * Keys and identifiers shared between the dialog fragments and their hosts.
 * <p>
 * Every dialog result is delivered through the AndroidX fragment result API. A
 * host registers a single listener for its request key and switches on the
 * request id carried in the result bundle, so a dialog's outcome survives the
 * host being recreated (e.g. on rotation) without any manual re-wiring.
 */
public final class DialogRequests {

    private DialogRequests() {
    }

    // Request keys - one per host, so hosts sharing a FragmentManager (an
    // activity and a fragment it displays) don't overwrite each other
    public static final String REQUEST_KEY_BASE_ACTIVITY = "com.coincollection.dialog.BASE_ACTIVITY";
    public static final String REQUEST_KEY_MAIN_ACTIVITY = "com.coincollection.dialog.MAIN_ACTIVITY";
    public static final String REQUEST_KEY_COLLECTION_PAGE = "com.coincollection.dialog.COLLECTION_PAGE";
    public static final String REQUEST_KEY_COIN_PAGE_CREATOR = "com.coincollection.dialog.COIN_PAGE_CREATOR";
    public static final String REQUEST_KEY_REORDER_COLLECTIONS = "com.coincollection.dialog.REORDER_COLLECTIONS";

    // Result bundle keys
    public static final String KEY_REQUEST_ID = "requestId";
    public static final String KEY_PAYLOAD = "payload";
    public static final String KEY_SELECTED_INDEX = "selectedIndex";
    public static final String KEY_TEXT = "text";
    public static final String KEY_COIN_NAME = "coinName";
    public static final String KEY_COIN_MINT = "coinMint";
    public static final String KEY_COIN_IMAGE_ID = "coinImageId";

    // Payload keys
    public static final String PAYLOAD_COLLECTION_NAME = "collectionName";
    public static final String PAYLOAD_COLLECTION_NAMES = "collectionNames";
    public static final String PAYLOAD_HELP_KEY = "helpKey";
    public static final String PAYLOAD_COIN_DATABASE_ID = "coinDatabaseId";
    public static final String PAYLOAD_CREATE_NEW_COIN = "createNewCoin";

    // Request ids. Kept globally unique so a request id can never be
    // misinterpreted if it reaches the wrong host
    public static final int REQUEST_NONE = 0;
    public static final int REQUEST_HELP_DIALOG = 1;
    public static final int REQUEST_DELETE_COLLECTION = 2;
    public static final int REQUEST_EXPORT_COLLECTIONS = 3;
    public static final int REQUEST_IMPORT_COLLECTIONS = 4;
    public static final int REQUEST_SELECT_COLLECTION_TO_DELETE = 5;
    public static final int REQUEST_COLLECTION_ACTIONS = 6;
    public static final int REQUEST_IMPORT_SOURCE = 7;
    public static final int REQUEST_EXPORT_FORMAT = 8;
    public static final int REQUEST_RENAME_COLLECTION = 9;
    public static final int REQUEST_UNSAVED_CHANGES_EXIT_PAGE = 10;
    public static final int REQUEST_COIN_ACTIONS = 11;
    public static final int REQUEST_COIN_FILTER = 12;
    public static final int REQUEST_EDIT_COIN = 13;
    public static final int REQUEST_COLLECTION_OPTIONS_WARNING = 14;
    public static final int REQUEST_UNSAVED_CHANGES_EXIT_REORDER = 15;

    // Fragment tags. Only one dialog of each kind is ever shown at a time
    public static final String TAG_MESSAGE = "message_dialog";
    public static final String TAG_CONFIRMATION = "confirmation_dialog";
    public static final String TAG_LIST_CHOICE = "list_choice_dialog";
    public static final String TAG_TEXT_INPUT = "text_input_dialog";
    public static final String TAG_COIN_EDIT = "coin_edit_dialog";
    public static final String TAG_ABOUT = "about_dialog";
    public static final String TAG_PROGRESS = "progress_dialog";
}
