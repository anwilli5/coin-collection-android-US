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

package com.coincollection;

import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.ExportImportHelper.LEGACY_EXPORT_FOLDER_NAME;
import static com.coincollection.ReorderCollections.REORDER_COLLECTION;
import static com.coincollection.dialog.DialogRequests.KEY_PAYLOAD;
import static com.coincollection.dialog.DialogRequests.KEY_SELECTED_INDEX;
import static com.coincollection.dialog.DialogRequests.PAYLOAD_COLLECTION_NAME;
import static com.coincollection.dialog.DialogRequests.PAYLOAD_COLLECTION_NAMES;
import static com.coincollection.dialog.DialogRequests.REQUEST_COLLECTION_ACTIONS;
import static com.coincollection.dialog.DialogRequests.REQUEST_DELETE_COLLECTION;
import static com.coincollection.dialog.DialogRequests.REQUEST_EXPORT_COLLECTIONS;
import static com.coincollection.dialog.DialogRequests.REQUEST_EXPORT_FORMAT;
import static com.coincollection.dialog.DialogRequests.REQUEST_IMPORT_COLLECTIONS;
import static com.coincollection.dialog.DialogRequests.REQUEST_IMPORT_SOURCE;
import static com.coincollection.dialog.DialogRequests.REQUEST_KEY_MAIN_ACTIVITY;
import static com.coincollection.dialog.DialogRequests.REQUEST_SELECT_COLLECTION_TO_DELETE;
import static com.coincollection.dialog.DialogRequests.TAG_ABOUT;
import static com.coincollection.dialog.DialogRequests.TAG_CONFIRMATION;
import static com.coincollection.dialog.DialogRequests.TAG_LIST_CHOICE;
import static com.spencerpages.MainApplication.APP_NAME;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.coincollection.dialog.AboutDialogFragment;
import com.coincollection.dialog.ConfirmationDialogFragment;
import com.coincollection.dialog.ListChoiceDialogFragment;
import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * The main Activity for the app.  Implements a ListView which lets the user view a previously
 * created collection or add/delete/reorder/export/import collections
 */
public class MainActivity extends BaseActivity {

    public final ArrayList<CollectionListInfo> mCollectionListEntries = new ArrayList<>();
    private FrontAdapter mListAdapter;

    // The number of actual collections in mCollectionListEntries
    public int mNumberOfCollections = 0;

    // Tracks whether the "unknown collection type" warning has already been shown, so that
    // it appears at most once per activity instance instead of on every list refresh
    private boolean mShownUnknownTypeWarning = false;

    // Import/export task inputs live in mActivityViewModel.mTaskRequest (not in
    // activity fields) so they survive configuration changes while a task runs

    // App permission requests
    private final static int IMPORT_PERMISSIONS_REQUEST = 0;
    private final static int EXPORT_PERMISSIONS_REQUEST = 1;
    public final static int PICK_IMPORT_FILE = 2;
    private final static int PICK_EXPORT_FILE = 3;

    // Default list item view positions
    //  0. Add Collection
    //  1. Remove Collection
    //  2. Import Collections
    //  3. Export Collections
    //  4. Re-order Collections
    //  5. About
    // Note: Using constants instead of an enum based on this:
    // https://developer.android.com/training/articles/memory.html#Overhead
    // - Enums often require more than twice as much memory as static constants.
    private final static int ADD_COLLECTION = 0;
    private final static int REMOVE_COLLECTION = 1;
    public final static int IMPORT_COLLECTIONS = 2;
    public final static int EXPORT_COLLECTIONS = 3;
    private final static int REORDER_COLLECTIONS = 4;
    private final static int ABOUT = 5;
    // As a hack to get the static strings at the bottom of the list, we add spacers into
    // mCollectionListEntries.  This tracks the number of those spacers, which we use in several
    // places.
    public final static int NUMBER_OF_COLLECTION_LIST_SPACERS = 6;

    // Action menu items
    private final static int NUM_ACTIONS = 4;
    private final static int ACTIONS_VIEW = 0;
    private final static int ACTIONS_EDIT = 1;
    private final static int ACTIONS_COPY = 2;
    private final static int ACTIONS_DELETE = 3;

    // Import source menu items
    private final static int IMPORT_SOURCE_LEGACY = 1;

    // Export format menu items
    private final static int EXPORT_FORMAT_JSON = 0;
    private final static int EXPORT_FORMAT_CSV = 1;
    private final static int EXPORT_FORMAT_LEGACY = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Listen for results from this activity's dialogs
        registerDialogResultListener(REQUEST_KEY_MAIN_ACTIVITY);

        setContentView(R.layout.main_activity_layout);
        View rootView = findViewById(R.id.main_activity_frame);
        applyWindowInsets(rootView);

        // In legacy code we used first_Time_screen2 here so that the message would be displayed
        // until they made it to the create collection screen.  That isn't necessary anymore, but
        // if they are upgrading from that don't show them the help screen if first_Time_screen1
        // isn't set
        createAndShowHelpDialog("first_Time_screen1", R.string.intro_message);

        // Instantiate the FrontAdapter
        mListAdapter = new FrontAdapter(mContext, mCollectionListEntries, mNumberOfCollections);
        ListView lv = findViewById(R.id.main_activity_listview);
        lv.setAdapter(mListAdapter);
        // TODO Not sure what this does?
        lv.setTextFilterEnabled(true); // Typing narrows down the list

        // At this point the UI is ready to handle any async callbacks
        setActivityReadyForAsyncCallbacks();

        // For when we use fragments, listen to the back stack so we can transition back here from
        // the fragment
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {

            if (0 == getSupportFragmentManager().getBackStackEntryCount()) {

                // We are back at this activity, so restore the ActionBar
                if (mActionBar != null) {
                    mActionBar.setTitle(mRes.getString(R.string.app_name));
                    mActionBar.setDisplayHomeAsUpEnabled(false);
                    mActionBar.setHomeButtonEnabled(false);
                }

                // The collections may have been re-ordered, so update them here.
                updateCollectionListFromDatabaseAndUpdateViewForUIThread();
            }
        });

        // Now set the onItemClickListener to perform a certain action based on what's clicked
        lv.setOnItemClickListener((parent, view, position, id) -> {

            // See whether it was one of the special list entries (Add collection, delete
            // collection, etc.)
            if (position >= mNumberOfCollections) {
                int newPosition = position - mNumberOfCollections;
                switch (newPosition) {
                    case ADD_COLLECTION:
                        launchCoinPageCreatorActivity(null);
                        break;
                    case REMOVE_COLLECTION:
                        if (mNumberOfCollections == 0) {
                            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        showSelectCollectionToDelete();
                        break;
                    case IMPORT_COLLECTIONS:
                        promptCsvOrJsonImport();
                        break;
                    case EXPORT_COLLECTIONS:
                        promptCsvOrJsonExport();
                        break;
                    case REORDER_COLLECTIONS:
                        launchReorderFragment();
                        break;
                    case ABOUT:
                        showDialogFragment(AboutDialogFragment.newInstance(buildInfoText().toString()), TAG_ABOUT);
                        break;
                }

                return;
            }
            // If it gets here, the user has selected a collection
            launchCoinPageActivity(mCollectionListEntries.get(position));
        });

        // Add long-press handler for additional actions
        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < mNumberOfCollections) {
                showCollectionActions(mCollectionListEntries.get(position).getName());
                return true;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // If the collection has coins then show the more options help (if not yet shown)
        if (mNumberOfCollections > 0) {
            createAndShowHelpDialog("first_Time_screen4", R.string.tutorial_more_options);
        }
    }

    @Override
    public String asyncProgressDoInBackground(int taskId) {
        String parentResult = super.asyncProgressDoInBackground(taskId);
        if (!parentResult.isEmpty()) {
            return parentResult;
        }
        switch (taskId) {
            case TASK_IMPORT_COLLECTIONS: {
                ExportImportHelper helper = new ExportImportHelper(mRes, mDbAdapter);
                if (mActivityViewModel.mTaskRequest.importExportLegacyCsv) {
                    return helper.importCollectionsFromLegacyCSV(getLegacyExportFolderName());
                } else {
                    final Uri fileUri = mActivityViewModel.mTaskRequest.importExportFileUri;
                    if (fileUri == null) {
                        return mRes.getString(R.string.error_importing,
                                mRes.getString(R.string.error_no_file_selected));
                    }
                    if (!isSafeDocumentUri(fileUri)) {
                        return mRes.getString(R.string.error_importing,
                                mRes.getString(R.string.error_unsupported_file_location));
                    }
                    try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
                        String fileName = getFileNameFromUri(fileUri);
                        if (fileName.endsWith(".csv")) {
                            return helper.importCollectionsFromSingleCSV(inputStream);
                        } else {
                            return helper.importCollectionsFromJson(inputStream);
                        }
                    } catch (IOException e) {
                        return mRes.getString(R.string.error_importing, e.getMessage());
                    }
                }
            }
            case TASK_EXPORT_COLLECTIONS: {
                ExportImportHelper helper = new ExportImportHelper(mRes, mDbAdapter);
                if (mActivityViewModel.mTaskRequest.importExportLegacyCsv) {
                    return helper.exportCollectionsToLegacyCSV(getLegacyExportFolderName());
                } else {
                    final Uri fileUri = mActivityViewModel.mTaskRequest.importExportFileUri;
                    if (fileUri == null) {
                        return mRes.getString(R.string.error_exporting,
                                mRes.getString(R.string.error_no_file_selected));
                    }
                    if (!isSafeDocumentUri(fileUri)) {
                        return mRes.getString(R.string.error_exporting,
                                mRes.getString(R.string.error_unsupported_file_location));
                    }
                    try (OutputStream outputStream = getContentResolver().openOutputStream(fileUri)) {
                        String fileName = getFileNameFromUri(fileUri);
                        if (fileName.endsWith(".csv")) {
                            return helper.exportCollectionsToSingleCSV(outputStream, fileName);
                        } else {
                            return helper.exportCollectionsToJson(outputStream, fileName);
                        }
                    } catch (IOException e) {
                        return mRes.getString(R.string.error_exporting, e.getMessage());
                    }
                }
            }
        }
        return "";
    }

    @Override
    public void asyncProgressOnPostExecute(int taskId, String resultStr) {
        super.asyncProgressOnPostExecute(taskId, resultStr);
        if (taskId == TASK_IMPORT_COLLECTIONS) {
            mActivityViewModel.mTaskRequest.isImportingCollection = false;
        }
        updateCollectionListFromDatabaseAndUpdateViewForUIThread();
    }

    /**
     * Launches the collection page for a collection list entry
     *
     * @param listEntry The collection to view
     * @return Intent (used for testing)
     */
    public Intent launchCoinPageActivity(CollectionListInfo listEntry) {
        Intent intent = new Intent(mContext, CollectionPage.class);
        intent.putExtra(CollectionPage.COLLECTION_NAME, listEntry.getName());
        intent.putExtra(CollectionPage.COLLECTION_TYPE_INDEX, listEntry.getCollectionTypeIndex());
        startActivity(intent);
        return intent;
    }

    /**
     * Launches the collection creation page, either for creating a new collection or for editing
     *
     * @param existingCollection if null, creates a new collection otherwise edits an existing one
     */
    private void launchCoinPageCreatorActivity(CollectionListInfo existingCollection) {
        Intent intent = new Intent(mContext, CoinPageCreator.class);
        if (existingCollection != null) {
            intent.putExtra(CoinPageCreator.EXISTING_COLLECTION_EXTRA, existingCollection);
        }
        startActivity(intent);
    }

    /**
     * Launch the reorder fragment
     *
     * @return ReorderCollections (used for testing)
     */
    public ReorderCollections launchReorderFragment() {

        if (mNumberOfCollections == 0) {
            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
            return null;
        }

        // Get a list that excludes the spacers
        List<CollectionListInfo> tmp = mCollectionListEntries.subList(0, mNumberOfCollections);
        ArrayList<CollectionListInfo> collections = new ArrayList<>(tmp);

        ReorderCollections fragment = new ReorderCollections();
        fragment.setCollectionList(collections);

        // Show the fragment used for reordering collections
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_activity_frame, fragment, REORDER_COLLECTION)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        return fragment;
    }

    /**
     * Handle when the user starts importing a collection
     */
    private void launchImportTask() {
        if (!mActivityViewModel.mTaskRequest.importExportLegacyCsv) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = {"text/comma-separated-values", "text/csv", "application/json"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The files should preferably be placed in the downloads folder
                Uri pickerInitialUri = Uri.parse(Environment.DIRECTORY_DOWNLOADS);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
            }

            try {
                startActivityForResult(intent, PICK_IMPORT_FILE);
            } catch (ActivityNotFoundException e) {
                // Handle if there isn't an activity to handle the intent
                Toast.makeText(this, mRes.getString(R.string.error_no_file_manager), Toast.LENGTH_LONG).show();
            }
        } else {
            // Check for READ_EXTERNAL_STORAGE permissions (must request starting in API Level 23)
            // hasPermissions() will kick off the permissions request and the handler will re-call
            // this method after prompting the user.
            if (checkNoLegacyExternalPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, IMPORT_PERMISSIONS_REQUEST)) {
                return;
            }

            if (mNumberOfCollections == 0) {
                // Finish the import using AsyncTaskRunner to do the heavy lifting
                startImportTask();
            } else {
                showImportConfirmation();
            }
        }
    }

    /**
     * Starts the import task, flagging that the database is being rewritten so that the
     * collection list isn't read back mid-import (see onWindowFocusChanged). All import
     * entry points must go through this method.
     */
    private void startImportTask() {
        mActivityViewModel.mTaskRequest.isImportingCollection = true;
        kickOffAsyncTaskRunner(TASK_IMPORT_COLLECTIONS);
    }

    /**
     * Indicates whether an import task is currently rewriting the database
     *
     * @return true while an import is in progress
     */
    public boolean isImportInProgress() {
        return mActivityViewModel.mTaskRequest.isImportingCollection;
    }

    /**
     * Handle when the user starts exporting a collection
     */
    private void launchExportTask() {

        if (mNumberOfCollections == 0) {
            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mActivityViewModel.mTaskRequest.importExportLegacyCsv) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (mActivityViewModel.mTaskRequest.exportSingleFileCsv) {
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "coin-collection-" + getTodayDateString() + ".csv");
            } else {
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, "coin-collection-" + getTodayDateString() + ".json");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The files should preferably be placed in the downloads folder
                Uri pickerInitialUri = Uri.parse(Environment.DIRECTORY_DOWNLOADS);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
            }
            startActivityForResult(intent, PICK_EXPORT_FILE);
        } else {
            // Check for WRITE_EXTERNAL_STORAGE permissions (must request starting in API Level 23)
            // hasPermissions() will kick off the permissions request and the handler will re-call
            // this method after prompting the user.
            if (checkNoLegacyExternalPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, EXPORT_PERMISSIONS_REQUEST)) {
                return;
            }

            // Indicate that we're using the legacy CSV
            mActivityViewModel.mTaskRequest.importExportLegacyCsv = true;

            // Check to see if the folder exists already
            File dir = new File(getLegacyExportFolderName());
            if (dir.isDirectory() || dir.exists()) {
                // Let the user decide whether they want to delete this
                showExportConfirmation();
            } else {
                // Finish the export using AsyncTaskRunner to do the heavy lifting
                kickOffAsyncTaskRunner(TASK_EXPORT_COLLECTIONS);
            }
        }
    }

    // https://developer.android.com/training/permissions/requesting.html
    // Expected: Manifest.permission.{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}

    /**
     * Checks if the user has given external READ/WRITE permission
     *
     * @param permission  read or write permission
     * @param callbackTag string identifier passed to the callback method
     * @return true if the user hasn't given permission
     */
    private boolean checkNoLegacyExternalPermissions(String permission, int callbackTag) {

        int permissionState = ContextCompat.checkSelfPermission(this, permission);
        if (permissionState != PackageManager.PERMISSION_GRANTED) {

            // Not providing an explanation but the user should know what this is for
            // This will prompt the user to grant/deny permissions, and the result will
            // be delivered via a callback.
            ActivityCompat.requestPermissions(this, new String[]{permission}, callbackTag);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((grantResults.length > 0)
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            // Request Granted!
            switch (requestCode) {
                case IMPORT_PERMISSIONS_REQUEST: {
                    // Retry import, now with permissions granted
                    launchImportTask();
                    break;
                }
                case EXPORT_PERMISSIONS_REQUEST: {
                    // Retry export, now with permissions granted
                    launchExportTask();
                    break;
                }
            }
        } else {
            // Request Denied!
            switch (requestCode) {
                case IMPORT_PERMISSIONS_REQUEST: {
                    showCancelableAlert(mRes.getString(R.string.import_canceled));
                    break;
                }
                case EXPORT_PERMISSIONS_REQUEST: {
                    showCancelableAlert(mRes.getString(R.string.export_canceled));
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_EXPORT_FILE: {
                    if (resultData != null) {
                        mActivityViewModel.mTaskRequest.importExportFileUri = resultData.getData();
                        // Finish the export using AsyncTaskRunner to do the heavy lifting
                        kickOffAsyncTaskRunner(TASK_EXPORT_COLLECTIONS);
                    }
                    break;
                }
                case PICK_IMPORT_FILE: {
                    if (resultData != null) {
                        mActivityViewModel.mTaskRequest.importExportFileUri = resultData.getData();
                        if (mNumberOfCollections != 0) {
                            showImportConfirmation();
                        } else {
                            // Finish the import by kicking off an AsyncTask to do the heavy lifting
                            startImportTask();
                        }
                    }
                    break;
                }
            }
        }
    }

    // Need to make our own Array Adapter to handle the special list (list of collections + entries
    // for 'Create Collections', 'Reorder Collections', etc.)
    // Thanks! http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
    private class FrontAdapter extends ArrayAdapter<CollectionListInfo> {

        ArrayList<CollectionListInfo> items;
        int numberOfCollections;
        private final Resources mRes;

        FrontAdapter(Context context, ArrayList<CollectionListInfo> items, int numberOfCollections) {
            super(context, R.layout.list_element, R.id.collectionNameTextView, items);
            this.items = items;
            this.numberOfCollections = numberOfCollections;
            mRes = context.getResources();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position >= this.numberOfCollections) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            int viewType = getItemViewType(position);
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (0 == viewType) {
                    view = vi.inflate(R.layout.list_element, parent, false);
                } else {
                    view = vi.inflate(R.layout.list_element_navigation, parent, false);
                }
            }

            if (viewType == 1) {
                // Set up the non-collection views
                ImageView image = view.findViewById(R.id.navImageView);
                TextView text = view.findViewById(R.id.navTextView);

                int newPosition = position - this.numberOfCollections;

                switch (newPosition) {
                    case ADD_COLLECTION:
                        image.setBackgroundResource(R.drawable.icon_circle_add);
                        text.setText(mRes.getString(R.string.create_new_collection));
                        break;
                    case REMOVE_COLLECTION:
                        image.setBackgroundResource(R.drawable.icon_minus);
                        text.setText(mRes.getString(R.string.delete_collection));
                        break;
                    case IMPORT_COLLECTIONS:
                        image.setBackgroundResource(R.drawable.icon_cloud_upload);
                        text.setText(mRes.getString(R.string.import_collection));
                        break;
                    case EXPORT_COLLECTIONS:
                        image.setBackgroundResource(R.drawable.icon_cloud_download);
                        text.setText(mRes.getString(R.string.export_collection));
                        break;
                    case REORDER_COLLECTIONS:
                        image.setBackgroundResource(R.drawable.icon_sort);
                        text.setText(mRes.getString(R.string.reorder_collection));
                        break;
                    case ABOUT:
                        image.setBackgroundResource(R.drawable.icon_info);
                        text.setText(mRes.getString(R.string.app_info));
                        break;
                }
                return view;
            }

            // If it gets here, we need to set up a view for a collection
            CollectionListInfo item = items.get(position);
            buildListElement(item, view, mRes);
            return view;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Note that this provides information about global focus state, which is managed
        // independently of activity lifecycle. As such, while focus changes will generally have
        // some relation to lifecycle changes (an activity that is stopped will not generally get
        // window focus), you should not rely on any particular order between the callbacks here
        // and those in the other lifecycle methods such as onResume().

        // We use this function as a convenience for updating the database once the list gets focus
        // after returning from the add/delete/reorder views.

        if (hasFocus && !isImportInProgress()) {
            // Only do this if the database has been opened with AsyncTaskRunner first
            // and we aren't modifying the database like crazy (importing)
            // We need this so that new collections that are added/removed get shown

            updateCollectionListFromDatabaseAndUpdateViewForUIThread();
        }
    }

    /**
     * Reloads the collection list from the database.  This is useful after changes have been made
     * (collections reordered, deleted, etc.)
     */
    public void updateCollectionListFromDatabase() {

        //Get a list of all the database tables
        ArrayList<String> skippedCollections = new ArrayList<>();
        try {
            mDbAdapter.getAllTables(mCollectionListEntries, skippedCollections);
        } catch (SQLException e) {
            showCancelableAlert(mRes.getString(R.string.error_reading_database));
        }

        // Let the user know once if any collections couldn't be loaded, instead of
        // silently dropping them from the list
        if (!skippedCollections.isEmpty() && !mShownUnknownTypeWarning) {
            mShownUnknownTypeWarning = true;
            StringBuilder names = new StringBuilder();
            for (String name : skippedCollections) {
                names.append("\n").append(name);
            }
            showCancelableAlert(mRes.getString(R.string.warning_unknown_collection_types, names.toString()));
        }

        // Record the actual number of collections before spacers are added
        mNumberOfCollections = mCollectionListEntries.size();

        // We use an ArrayAdapter to power the ListView, but since we want to add in somethings that
        // don't have items in the list, we add in some blank entries to account for them.  Pretty
        // hacked together but it should work.
        for (int i = 0; i < NUMBER_OF_COLLECTION_LIST_SPACERS; i++) {
            mCollectionListEntries.add(null);
        }
    }

    /**
     * Reloads the collection list from the database and updates the list adapter. This method
     * should only be called from the UI Thread
     */
    public void updateCollectionListFromDatabaseAndUpdateViewForUIThread() {

        // mDbAdapter may be null in some corner cases where this method gets called
        // before the DB has been opened or after it has closed - ignore the update
        // in that case
        try {
            // Refresh mCollectionListEntries and mNumberOfCollections from the database
            updateCollectionListFromDatabase();
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) {
                Log.e(APP_NAME, "Called updateCollectionListFromDatabaseAndUpdateViewForUIThread() before mDbAdapter initialized ");
            }
            return;
        }

        // Update the list view adapter
        if (mListAdapter != null) {
            mListAdapter.items = mCollectionListEntries;
            mListAdapter.numberOfCollections = mNumberOfCollections;
            mListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Show dialog for user to confirm export
     */
    private void showExportConfirmation() {
        showDialogFragment(ConfirmationDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_EXPORT_COLLECTIONS,
                null, mRes.getString(R.string.export_warning),
                R.string.yes, R.string.no, null), TAG_CONFIRMATION);
    }

    /**
     * Show dialog for user to confirm import
     */
    private void showImportConfirmation() {
        showDialogFragment(ConfirmationDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_IMPORT_COLLECTIONS,
                mRes.getString(R.string.warning), mRes.getString(R.string.import_warning),
                R.string.yes, R.string.no, null), TAG_CONFIRMATION);
    }

    /**
     * Show dialog for user to confirm deletion of collection
     *
     * @param name collection name
     */
    private void showDeleteConfirmation(final String name) {
        // The collection name travels with the dialog, so the confirmation still
        // knows what it applies to if this activity is recreated while it's open
        Bundle payload = new Bundle();
        payload.putString(PAYLOAD_COLLECTION_NAME, name);
        showDialogFragment(ConfirmationDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_DELETE_COLLECTION,
                mRes.getString(R.string.warning), mRes.getString(R.string.delete_warning, name),
                R.string.yes, R.string.no, payload), TAG_CONFIRMATION);
    }

    /**
     * Show a list of collections for the user to pick one to delete
     */
    private void showSelectCollectionToDelete() {
        // Thanks!
        // http://stackoverflow.com/questions/2397106/listview-in-alertdialog
        String[] names = new String[mNumberOfCollections];
        for (int i = 0; i < mNumberOfCollections; i++) {
            names[i] = mCollectionListEntries.get(i).getName();
        }
        // Carry the displayed names so the choice resolves against what the user
        // actually saw, even if the collection list changes underneath
        Bundle payload = new Bundle();
        payload.putStringArray(PAYLOAD_COLLECTION_NAMES, names);
        showDialogFragment(ListChoiceDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_SELECT_COLLECTION_TO_DELETE,
                mRes.getString(R.string.select_collection_delete), names, payload), TAG_LIST_CHOICE);
    }

    /**
     * Show the actions available for a collection
     *
     * @param name collection name
     */
    private void showCollectionActions(final String name) {
        String[] actionsList = new String[NUM_ACTIONS];
        actionsList[ACTIONS_VIEW] = mRes.getString(R.string.view);
        actionsList[ACTIONS_EDIT] = mRes.getString(R.string.edit);
        actionsList[ACTIONS_COPY] = mRes.getString(R.string.copy);
        actionsList[ACTIONS_DELETE] = mRes.getString(R.string.delete);
        // Identify the collection by name rather than list position, which can
        // point at a different collection by the time an action is picked
        Bundle payload = new Bundle();
        payload.putString(PAYLOAD_COLLECTION_NAME, name);
        showDialogFragment(ListChoiceDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_COLLECTION_ACTIONS,
                mRes.getString(R.string.collection_actions), actionsList, payload), TAG_LIST_CHOICE);
    }

    /**
     * Finds a collection by name in the current list
     *
     * @param name collection name
     * @return the collection info, or null if it is no longer in the list
     */
    private CollectionListInfo getCollectionListInfoByName(String name) {
        for (int i = 0; i < mNumberOfCollections; i++) {
            CollectionListInfo listEntry = mCollectionListEntries.get(i);
            if (listEntry.getName().equals(name)) {
                return listEntry;
            }
        }
        return null;
    }

    /**
     * Deletes a collection and fixes up the display order of the remaining ones
     *
     * @param name collection name
     */
    private void deleteCollection(final String name) {
        Cursor cursor = null;
        try {
            mDbAdapter.dropCollectionTable(name);
            //Get a list of all the database tables
            cursor = mDbAdapter.getAllCollectionNames();
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String name1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                    // Fix up the displayOrder
                    mDbAdapter.updateDisplayOrder(name1, i);
                    i++;
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            showCancelableAlert(mRes.getString(R.string.error_delete_database));
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Makes a copy of the collection specified by tableName
     *
     * @param tableName The collection name to make a copy of
     */
    public void copyCollection(String tableName) {

        // Get the source collection details
        CollectionListInfo sourceCollectionListInfo = null;
        int insertIndex = 0;
        for (int i = 0; i < mNumberOfCollections; i++) {
            if (mCollectionListEntries.get(i).getName().equals(tableName)) {
                sourceCollectionListInfo = mCollectionListEntries.get(i);
                insertIndex = i + 1;
                break;
            }
        }
        if (sourceCollectionListInfo == null) {
            showCancelableAlert(mRes.getString(R.string.error_copying_database));
            return;
        }

        // If this is a copy of a copy, reduce down to the base name
        String baseNewTableName = tableName;
        String suffixBase = mRes.getString(R.string.copy_name_suffix);
        int copySuffixMatch = tableName.lastIndexOf(suffixBase);
        if (copySuffixMatch != -1) {
            String remainingChars = tableName.substring(copySuffixMatch + suffixBase.length());
            if (remainingChars.matches("^\\d*$")) {
                baseNewTableName = tableName.substring(0, copySuffixMatch);
            }
        }

        // Pick a new table name
        String newTableName;
        int checkNameResult;
        int attemptNumber = 0;
        do {
            String suffixIndex = (attemptNumber == 0) ? "" : Integer.toString(attemptNumber);
            newTableName = baseNewTableName + mRes.getString(R.string.copy_name_suffix) + suffixIndex;
            checkNameResult = mDbAdapter.checkCollectionName(newTableName);
            attemptNumber++;
        } while (checkNameResult != -1);

        // Create the new table
        CollectionListInfo newCollectionListInfo;
        try {
            newCollectionListInfo = mDbAdapter.createCollectionCopy(sourceCollectionListInfo, newTableName, insertIndex);
        } catch (SQLException e) {
            showCancelableAlert(mRes.getString(R.string.error_copying_database));
            return;
        }

        // Insert into the collection list and update the database sort order
        mCollectionListEntries.add(insertIndex, newCollectionListInfo);
        mNumberOfCollections += 1;
        handleCollectionsReordered(new ArrayList<>(mCollectionListEntries.subList(0, mNumberOfCollections)));
    }

    /**
     * Takes the reordered list of collections in from the ReorderCollections fragment and updates
     * the ordering in the database.
     *
     * @param reorderedList The reordered list of collections
     */
    public void handleCollectionsReordered(ArrayList<CollectionListInfo> reorderedList) {

        for (int i = 0; i < reorderedList.size(); i++) {
            CollectionListInfo info = reorderedList.get(i);
            try {
                mDbAdapter.updateDisplayOrder(info.getName(), i);
            } catch (SQLException e) {
                showCancelableAlert(mRes.getString(R.string.error_reordering_databases));
                return;
            }
            mCollectionListEntries.set(i, info);
        }
    }

    /**
     * Construct the attribution string for the info text
     *
     * @return info text string
     */
    public String buildInfoText() {
        HashSet<String> attributions = new HashSet<>();
        for (CollectionInfo collection : MainApplication.COLLECTION_TYPES) {
            int attributionResId = collection.getAttributionResId();
            if (attributionResId == -1 || attributionResId == R.string.attr_mint) {
                // US mint attribution is included at the end
                continue;
            }
            String attributionStr = mRes.getString(attributionResId);
            if (attributionStr.isEmpty()) {
                continue;
            }
            attributions.add(attributionStr);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(mRes.getString(R.string.info_overview));
        builder.append("\n\n");
        for (String attribution : attributions) {
            builder.append(attribution);
            builder.append("\n\n");
        }
        builder.append(mRes.getString(R.string.attr_mint));
        builder.append("\n\n");
        builder.append(mRes.getString(R.string.attr_icons));

        return builder.toString();
    }

    /**
     * Returns the path to the file storage directory
     *
     * @return path string
     */
    public String getLegacyExportFolderName() {
        File sdCard = Environment.getExternalStorageDirectory();
        return sdCard.getAbsolutePath() + LEGACY_EXPORT_FOLDER_NAME;
    }

    /**
     * Checks that a URI handed back by the system file picker is one we're willing to
     * resolve. The SAF picker only ever returns content:// URIs, so anything else (in
     * particular file:// URIs, or content URIs served by this app's own provider) is
     * rejected rather than being read from or written to the app's private storage.
     *
     * @param uri uri to check
     * @return true if the uri is safe to resolve
     */
    private boolean isSafeDocumentUri(Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return false;
        }
        String authority = uri.getAuthority();
        if (authority == null || authority.isEmpty()) {
            return false;
        }
        // Reject content providers exported by this app, which would resolve into our
        // own private storage rather than a user-chosen document
        return !authority.equals(getPackageName()) && !authority.startsWith(getPackageName() + ".");
    }

    /**
     * Returns the display name from a file URI
     *
     * @param uri file uri
     * @return string display name or "Unknown" if an error occurs
     */
    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return "Unknown";
        }
        int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String fileName = cursor.getString(index);
        cursor.close();
        return fileName;
    }

    /**
     * Gets a simple date string, for example 012019 for January 20th, 2019
     *
     * @return date string
     */
    private String getTodayDateString() {
        return new SimpleDateFormat("MMddyy", Locale.getDefault()).format(new Date());
    }

    /**
     * For now, allow users to pick between an import file or legacy storage
     * - Eventually legacy storage won't be an option
     */
    private void promptCsvOrJsonImport() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // In API 30+, access to the SD card is disabled, so don't give the user the option
            // to import from legacy storage. Since there is no choice, go directly to the picker
            mActivityViewModel.mTaskRequest.importExportLegacyCsv = false;
            launchImportTask();
            return;
        }

        // Populate a menu of actions for import
        String[] actionsList = new String[2];
        actionsList[0] = mRes.getString(R.string.pick_backup_file);
        actionsList[1] = mRes.getString(R.string.legacy_storage);
        showDialogFragment(ListChoiceDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_IMPORT_SOURCE,
                mRes.getString(R.string.import_place_message), actionsList, null), TAG_LIST_CHOICE);
    }

    /**
     * Allow users to pick between an export file format
     */
    private void promptCsvOrJsonExport() {

        if (mNumberOfCollections == 0) {
            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
            return;
        }

        // In API 30+, access to the SD card is disabled, so don't show this option after that
        boolean showLegacyExport = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q);

        // Populate a menu of actions for export
        String[] actionsList = new String[showLegacyExport ? 3 : 2];
        actionsList[EXPORT_FORMAT_JSON] = mRes.getString(R.string.json_file);
        actionsList[EXPORT_FORMAT_CSV] = mRes.getString(R.string.csv_file);
        if (showLegacyExport) {
            actionsList[EXPORT_FORMAT_LEGACY] = mRes.getString(R.string.legacy_storage);
        }
        showDialogFragment(ListChoiceDialogFragment.newInstance(
                REQUEST_KEY_MAIN_ACTIVITY, REQUEST_EXPORT_FORMAT,
                mRes.getString(R.string.export_format_message), actionsList, null), TAG_LIST_CHOICE);
    }

    @Override
    protected void onDialogResult(int requestId, Bundle result) {
        // Dialogs survive a configuration change, and the recreated activity
        // doesn't regain window focus while one is up, so the collection list it
        // was rebuilt with is still empty. Refresh it before acting on the answer
        if (mDbAdapter.isOpen() && !isImportInProgress()) {
            updateCollectionListFromDatabaseAndUpdateViewForUIThread();
        }
        Bundle payload = result.getBundle(KEY_PAYLOAD);
        switch (requestId) {
            case REQUEST_EXPORT_COLLECTIONS: {
                // Finish the export using AsyncTaskRunner to do the heavy lifting
                kickOffAsyncTaskRunner(TASK_EXPORT_COLLECTIONS);
                break;
            }
            case REQUEST_IMPORT_COLLECTIONS: {
                // Finish the import using AsyncTaskRunner to do the heavy lifting
                startImportTask();
                break;
            }
            case REQUEST_DELETE_COLLECTION: {
                if (payload != null) {
                    deleteCollection(payload.getString(PAYLOAD_COLLECTION_NAME, ""));
                }
                break;
            }
            case REQUEST_SELECT_COLLECTION_TO_DELETE: {
                String[] names = (payload != null) ? payload.getStringArray(PAYLOAD_COLLECTION_NAMES) : null;
                int index = result.getInt(KEY_SELECTED_INDEX, -1);
                if (names != null && index >= 0 && index < names.length) {
                    showDeleteConfirmation(names[index]);
                }
                break;
            }
            case REQUEST_COLLECTION_ACTIONS: {
                String name = (payload != null) ? payload.getString(PAYLOAD_COLLECTION_NAME) : null;
                if (name == null) {
                    break;
                }
                // The collection may have been removed while the dialog was open
                CollectionListInfo listEntry = getCollectionListInfoByName(name);
                if (listEntry == null) {
                    break;
                }
                switch (result.getInt(KEY_SELECTED_INDEX, -1)) {
                    case ACTIONS_VIEW:
                        launchCoinPageActivity(listEntry);
                        break;
                    case ACTIONS_EDIT:
                        launchCoinPageCreatorActivity(listEntry);
                        break;
                    case ACTIONS_COPY:
                        copyCollection(name);
                        break;
                    case ACTIONS_DELETE:
                        showDeleteConfirmation(name);
                        break;
                }
                break;
            }
            case REQUEST_IMPORT_SOURCE: {
                mActivityViewModel.mTaskRequest.importExportLegacyCsv =
                        (result.getInt(KEY_SELECTED_INDEX, -1) == IMPORT_SOURCE_LEGACY);
                launchImportTask();
                break;
            }
            case REQUEST_EXPORT_FORMAT: {
                int selected = result.getInt(KEY_SELECTED_INDEX, -1);
                if (selected < 0) {
                    break;
                }
                mActivityViewModel.mTaskRequest.importExportLegacyCsv = (selected == EXPORT_FORMAT_LEGACY);
                mActivityViewModel.mTaskRequest.exportSingleFileCsv = (selected == EXPORT_FORMAT_CSV);
                launchExportTask();
                break;
            }
            default: {
                super.onDialogResult(requestId, result);
                break;
            }
        }
    }
}
