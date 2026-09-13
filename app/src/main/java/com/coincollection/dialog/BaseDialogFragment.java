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

import static com.coincollection.dialog.DialogRequests.KEY_PAYLOAD;
import static com.coincollection.dialog.DialogRequests.KEY_REQUEST_ID;
import static com.coincollection.dialog.DialogRequests.REQUEST_NONE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Shared plumbing for the app's dialogs.
 * <p>
 * Each dialog carries the request key and request id that identify it, plus an
 * optional opaque payload that is echoed back with the result. Because the
 * arguments are part of the fragment's saved state, the dialog rebuilds itself
 * after a configuration change and the host doesn't have to remember which
 * dialog was open or what it referred to.
 */
public abstract class BaseDialogFragment extends DialogFragment {

    protected static final String ARG_REQUEST_KEY = "requestKey";
    protected static final String ARG_REQUEST_ID = "requestId";
    protected static final String ARG_PAYLOAD = "payload";
    protected static final String ARG_TITLE = "title";
    protected static final String ARG_MESSAGE = "message";
    protected static final String ARG_CANCELABLE = "cancelable";

    /**
     * Builds the argument bundle shared by every dialog
     *
     * @param requestKey key the host listens on
     * @param requestId  identifies which dialog the result came from
     * @param payload    optional data echoed back with the result
     * @return the argument bundle
     */
    protected static Bundle baseArgs(@NonNull String requestKey, int requestId, @Nullable Bundle payload) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putInt(ARG_REQUEST_ID, requestId);
        if (payload != null) {
            args.putBundle(ARG_PAYLOAD, payload);
        }
        return args;
    }

    /**
     * Delivers a result to the host, tagged with this dialog's request id and
     * payload. Does nothing for dialogs created without a request id, which are
     * purely informational
     *
     * @param result the result values to deliver, may be empty
     */
    protected void sendResult(@NonNull Bundle result) {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        int requestId = args.getInt(ARG_REQUEST_ID, REQUEST_NONE);
        String requestKey = args.getString(ARG_REQUEST_KEY);
        if (requestId == REQUEST_NONE || requestKey == null) {
            return;
        }
        result.putInt(KEY_REQUEST_ID, requestId);
        Bundle payload = args.getBundle(ARG_PAYLOAD);
        if (payload != null) {
            result.putBundle(KEY_PAYLOAD, payload);
        }
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    /**
     * @return the title supplied in the arguments, or null if none
     */
    @Nullable
    protected String getArgTitle() {
        Bundle args = getArguments();
        return (args != null) ? args.getString(ARG_TITLE) : null;
    }

    /**
     * @return the message supplied in the arguments, or null if none
     */
    @Nullable
    protected String getArgMessage() {
        Bundle args = getArguments();
        return (args != null) ? args.getString(ARG_MESSAGE) : null;
    }
}
