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
import static com.spencerpages.MainApplication.APP_NAME;

import android.Manifest;
import android.app.Activity;
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

import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.io.File;
import java.io.FileNotFoundException;
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

    // Used for the Update Database functionality
    private boolean mIsImportingCollection = false;
    private boolean mImportExportLegacyCsv = false;
    private boolean mExportSingleFileCsv = false;
    private Uri mImportExportFileUri = null;

    // App permission requests
    private final static int IMPORT_PERMISSIONS_REQUEST = 0;
    private final static int EXPORT_PERMISSIONS_REQUEST = 1;
    private final static int PICK_IMPORT_FILE = 2;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Don't have BaseActivity open the database - this activity will call that on an async
        // task the first time so the upgrade happens off of the UI thread.
        mOpenDbAdapterInOnCreate = false;
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG && mActionBar != null) {
            // Update the title indicating this is a debug build, for easier identification
            mActionBar.setTitle(mActionBar.getTitle() + " [DEBUG]");
        }

        setContentView(R.layout.main_activity_layout);

        // In legacy code we used first_Time_screen2 here so that the message would be displayed
        // until they made it to the create collection screen.  That isn't necessary anymore, but
        // if they are upgrading from that don't show them the help screen if first_Time_screen1
        // isn't set
        createAndShowHelpDialog("first_Time_screen1", R.string.intro_message);

        if(mPreviousTask == null){
            if(BuildConfig.DEBUG) {
                Log.d(APP_NAME, "No previous state so kicking off AsyncProgressTask to doOpen");
            }
            // Kick off the AsyncProgressTask to open the database.  This will likely be the first open,
            // so we want it in the AsyncTask in case we have to go into onUpgrade and it takes
            // a long time.
            kickOffAsyncProgressTask(TASK_OPEN_DATABASE);
            // The AsyncProgressTask will update mDbAdapter once the database has been opened
        } else {
            if(BuildConfig.DEBUG) {
                Log.d(APP_NAME, "Taking over existing mTask");
            }

            // There's two possible AsyncProgressTask's that could be running:
            //     - The one to open the database for the first time
            //     - The one to import collections
            // In the case of the former, we just want to show the dialog that the user had on the
            // screen.  For the latter case, we still need something to call finishViewSetup, and
            // we don't want to call it here bc it will try to use the database too early.  Instead,
            // set a flag that will have that AsyncProgressTask call finishViewSetup for us as well.
            asyncProgressOnPreExecute();

            // If we were in the middle of importing, the DB adapter may now be closed
            if(mTask.mAsyncTaskId == TASK_IMPORT_COLLECTIONS){
                openDbAdapterForUIThread();
                mIsImportingCollection = true;
            }
        }

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

                    if(0 == getSupportFragmentManager().getBackStackEntryCount()){

                        // We are back at this activity, so restore the ActionBar
                        if(mActionBar != null){
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
            if(position >= mNumberOfCollections){
                int newPosition = position - mNumberOfCollections;
                switch(newPosition){
                    case ADD_COLLECTION:
                        launchCoinPageCreatorActivity(null);
                        break;
                    case REMOVE_COLLECTION:
                        if(mNumberOfCollections == 0){
                            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // Thanks!
                        // http://stackoverflow.com/questions/2397106/listview-in-alertdialog
                        CharSequence[] names = new CharSequence[mNumberOfCollections];
                        for(int i = 0; i < mNumberOfCollections; i++){
                            names[i] = mCollectionListEntries.get(i).getName();
                        }

                        showAlert(newBuilder()
                                .setTitle(mRes.getString(R.string.select_collection_delete))
                                .setItems(names, (dialog, item) -> {
                                    dialog.dismiss();
                                    showDeleteConfirmation(mCollectionListEntries.get(item).getName());
                                }));
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

                        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.info_popup,
                                findViewById(R.id.info_layout_root));

                        TextView tv = layout.findViewById(R.id.info_textview);
                        tv.setText(buildInfoText());

                        showAlert(newBuilder().setView(layout));
                        break;
                }

                return;
            }
            // If it gets here, the user has selected a collection
            launchCoinPageActivity(mCollectionListEntries.get(position));
        });

        // Add long-press handler for additional actions
        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            if(position < mNumberOfCollections) {
                // For each collection item, populate a menu of actions for the collection
                CharSequence[] actionsList = new CharSequence[NUM_ACTIONS];
                actionsList[ACTIONS_VIEW] = mRes.getString(R.string.view);
                actionsList[ACTIONS_EDIT] = mRes.getString(R.string.edit);
                actionsList[ACTIONS_COPY] = mRes.getString(R.string.copy);
                actionsList[ACTIONS_DELETE] = mRes.getString(R.string.delete);
                final int actionPosition = position;
                showAlert(newBuilder()
                        .setTitle(mRes.getString(R.string.collection_actions))
                        .setItems(actionsList, (dialog, item) -> {
                            switch (item) {
                                case ACTIONS_VIEW: {
                                    // Launch collection page
                                    dialog.dismiss();
                                    launchCoinPageActivity(mCollectionListEntries.get(actionPosition));
                                    break;
                                }
                                case ACTIONS_EDIT: {
                                    // Launch edit view
                                    dialog.dismiss();
                                    launchCoinPageCreatorActivity(mCollectionListEntries.get(actionPosition));
                                    break;
                                }
                                case ACTIONS_COPY: {
                                    // Perform copy
                                    dialog.dismiss();
                                    copyCollection(mCollectionListEntries.get(actionPosition).getName());
                                    break;
                                }
                                case ACTIONS_DELETE: {
                                    // Perform delete
                                    dialog.dismiss();
                                    showDeleteConfirmation(mCollectionListEntries.get(actionPosition).getName());
                                    break;
                                }
                            }
                        }));
                return true;
            }
            return false;
        });

        // HISTORIC - no longer the case:
        //
        // We need to open the database since we are the first activity to run
        // We only want to open the database once, though, because it is a hassle
        // in that it is very expensive and must be opened in an asynchronous mTask
        // background thread as to avoid getting application not responding errors.
        // So, here is what we do:
        // - Instantiate a Service that will hold the database adapter
        //    + This will be around for the lifetime of the application
        // - Once the service has been created, open up the database in an async mTask
        // - Once the database is open, finish setting up the UI from the data pulled

        // After we open it, it must have at least one activity bound to it at all times
        // for it to stay alive.  So, each activity must bind to it on onCreate and
        // unbind in onDestroy.  Once the app is terminating, all the activity onDestroys
        // will have been called and the service's onDestroy will then get called, where
        // we close the database

        // Actually instantiate the database service
        //Intent mServiceIntent = new Intent(this, DatabaseService.class);
        // and bind to it
        //bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //
        // Having the database open for a long time didn't seem to work out well, though - after
        // having the app open for a while, database errors would start popping up.  Now, we just
        // do the first DB open in an AsyncTask to ensure we don't get an ANR if a database upgrade
        // is required, but just open and close the database regularly as needed after that.
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
    public void onDestroy() {
        // Only MainActivity closes the DB adapter, as it's shared between all activities
        if (mDbAdapter != null) {
            mDbAdapter.close();
        }
        // Don't try and stop any tasks, as they could be in the middle of a DB upgrade
        super.onDestroy();
    }

    @Override
    public String asyncProgressDoInBackground() {
        switch (mTask.mAsyncTaskId) {
            case TASK_OPEN_DATABASE: {
                return openDbAdapterForAsyncThread();
            }
            case TASK_IMPORT_COLLECTIONS: {
                ExportImportHelper helper = new ExportImportHelper(mRes, mDbAdapter);
                if (mImportExportLegacyCsv) {
                    return helper.importCollectionsFromLegacyCSV(getLegacyExportFolderName());
                } else {
                    try (InputStream inputStream = getContentResolver().openInputStream(mImportExportFileUri)) {
                        String fileName = getFileNameFromUri(mImportExportFileUri);
                        if (fileName.endsWith(".csv")) {
                            return helper.importCollectionsFromSingleCSV(inputStream);
                        } else {
                            return helper.importCollectionsFromJson(inputStream);
                        }
                    } catch (FileNotFoundException e) {
                        return mRes.getString(R.string.error_importing, e.getMessage());
                    } catch (IOException e) {
                        return mRes.getString(R.string.error_importing, e.getMessage());
                    }
                }
            }
            case TASK_EXPORT_COLLECTIONS: {
                ExportImportHelper helper = new ExportImportHelper(mRes, mDbAdapter);
                if (mImportExportLegacyCsv) {
                    return helper.exportCollectionsToLegacyCSV(getLegacyExportFolderName());
                } else {
                    try (OutputStream outputStream = getContentResolver().openOutputStream(mImportExportFileUri)) {
                        String fileName = getFileNameFromUri(mImportExportFileUri);
                        if (fileName.endsWith(".csv")) {
                            return helper.exportCollectionsToSingleCSV(outputStream, fileName);
                        } else {
                            return helper.exportCollectionsToJson(outputStream, fileName);
                        }
                    } catch (FileNotFoundException e) {
                        return mRes.getString(R.string.error_exporting, e.getMessage());
                    } catch (IOException e) {
                        return mRes.getString(R.string.error_exporting, e.getMessage());
                    }
                }
            }
        }
        return "";
    }

    @Override
    public void asyncProgressOnPreExecute() {
        switch (mTask.mAsyncTaskId) {
            case TASK_OPEN_DATABASE: {
                createProgressDialog(mRes.getString(R.string.opening_database));
                break;
            }
            case TASK_IMPORT_COLLECTIONS: {
                createProgressDialog(mRes.getString(R.string.importing_collections));
                break;
            }
            case TASK_EXPORT_COLLECTIONS: {
                createProgressDialog(mRes.getString(R.string.exporting_collections));
                break;
            }
        }
    }

    @Override
    public void asyncProgressOnPostExecute(String resultStr) {
        super.asyncProgressOnPostExecute(resultStr);
        dismissProgressDialog();
        if (mTask.mAsyncTaskId == TASK_IMPORT_COLLECTIONS) {
            mIsImportingCollection = false;
        }
        updateCollectionListFromDatabaseAndUpdateViewForUIThread();
    }

    /**
     * Launches the collection page for a collection list entry
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
     * @return ReorderCollections (used for testing)
     */
    public ReorderCollections launchReorderFragment() {

        if(mNumberOfCollections == 0){
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
        if (!mImportExportLegacyCsv && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
            startActivityForResult(intent, PICK_IMPORT_FILE);
        } else {
            // Check for READ_EXTERNAL_STORAGE permissions (must request starting in API Level 23)
            // hasPermissions() will kick off the permissions request and the handler will re-call
            // this method after prompting the user.
            if(checkNoLegacyExternalPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, IMPORT_PERMISSIONS_REQUEST)){
                return;
            }

            if(mNumberOfCollections == 0){
                // Finish the import by kicking off an AsyncTask to do the heavy lifting
                kickOffAsyncProgressTask(TASK_IMPORT_COLLECTIONS);
            } else {
                showImportConfirmation();
            }
        }
    }

    /**
     * Handle when the user starts exporting a collection
     */
    private void launchExportTask() {

        if(mNumberOfCollections == 0){
            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mImportExportLegacyCsv && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (mExportSingleFileCsv) {
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
            if(checkNoLegacyExternalPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, EXPORT_PERMISSIONS_REQUEST)){
                return;
            }

            // Indicate that we're using the legacy CSV
            mImportExportLegacyCsv = true;

            // Check to see if the folder exists already
            File dir = new File(getLegacyExportFolderName());
            if(dir.isDirectory() || dir.exists()){
                // Let the user decide whether they want to delete this
                showExportConfirmation();
            } else {
                // Finish the export by kicking off an AsyncTask to do the heavy lifting
                kickOffAsyncProgressTask(TASK_EXPORT_COLLECTIONS);
            }
        }
    }

    // https://developer.android.com/training/permissions/requesting.html
    // Expected: Manifest.permission.{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}

    /**
     * Checks if the user has given external READ/WRITE permission
     * @param permission read or write permission
     * @param callbackTag string identifier passed to the callback method
     * @return true if the user hasn't given permission
     */
    private boolean checkNoLegacyExternalPermissions(String permission, int callbackTag){

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
                        mImportExportFileUri = resultData.getData();
                        // Finish the export by kicking off an AsyncTask to do the heavy lifting
                        kickOffAsyncProgressTask(TASK_EXPORT_COLLECTIONS);
                    }
                    break;
                }
                case PICK_IMPORT_FILE: {
                    if (resultData != null) {
                        mImportExportFileUri = resultData.getData();
                        if(mNumberOfCollections != 0){
                            showImportConfirmation();
                        } else {
                            // Finish the import by kicking off an AsyncTask to do the heavy lifting
                            kickOffAsyncProgressTask(TASK_IMPORT_COLLECTIONS);
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
        public int getViewTypeCount(){
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if(position >= this.numberOfCollections){
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

                if(0 == viewType){
                    view = vi.inflate(R.layout.list_element, parent, false);
                } else {
                    view = vi.inflate(R.layout.list_element_navigation, parent, false);
                }
            }

            if(viewType == 1){
                // Set up the non-collection views
                ImageView image = view.findViewById(R.id.navImageView);
                TextView text = view.findViewById(R.id.navTextView);

                int newPosition = position - this.numberOfCollections;

                switch(newPosition){
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
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);

        // Note that this provides information about global focus state, which is managed
        // independently of activity lifecycle. As such, while focus changes will generally have
        // some relation to lifecycle changes (an activity that is stopped will not generally get
        // window focus), you should not rely on any particular order between the callbacks here
        // and those in the other lifecycle methods such as onResume().

        // We use this function as a convenience for updating the database once the list gets focus
        // after returning from the add/delete/reorder views.

        if (hasFocus && !mIsImportingCollection){
            // Only do this if the database has been opened with the AsyncTask first
            // and we aren't modifying the database like crazy (importing)
            // We need this so that new collections that are added/removed get shown

            updateCollectionListFromDatabaseAndUpdateViewForUIThread();
        }
    }

    /**
     * Reloads the collection list from the database.  This is useful after changes have been made
     * (collections reordered, deleted, etc.)
     */
    public void updateCollectionListFromDatabase(){

        //Get a list of all the database tables
        try {
            mDbAdapter.getAllTables(mCollectionListEntries);
        } catch (SQLException e){
            showCancelableAlert(mRes.getString(R.string.error_reading_database));
        }

        // Record the actual number of collections before spacers are added
        mNumberOfCollections = mCollectionListEntries.size();

        // We use an ArrayAdapter to power the ListView, but since we want to add in somethings that
        // don't have items in the list, we add in some blank entries to account for them.  Pretty
        // hacked together but it should work.
        for(int i = 0; i < NUMBER_OF_COLLECTION_LIST_SPACERS; i++) {
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
            if(BuildConfig.DEBUG) {
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
    private void showExportConfirmation(){

        showAlert(newBuilder()
                .setMessage(mRes.getString(R.string.export_warning))
                .setCancelable(false)
                .setPositiveButton(mRes.getString(R.string.yes), (dialog, id) -> {
                    dialog.dismiss();
                    // Finish the export by kicking off an AsyncTask to do the heavy lifting
                    kickOffAsyncProgressTask(TASK_EXPORT_COLLECTIONS);
                })
                .setNegativeButton(mRes.getString(R.string.no), (dialog, id) -> dialog.cancel()));
    }

    /**
     * Show dialog for user to confirm import
     */
    private void showImportConfirmation(){

        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.warning))
                .setMessage(mRes.getString(R.string.import_warning))
                .setCancelable(false)
                .setPositiveButton(mRes.getString(R.string.yes), (dialog, id) -> {
                    // Finish the import by kicking off an AsyncTask to do the heavy lifting
                    dialog.dismiss();
                    mIsImportingCollection = true;
                    kickOffAsyncProgressTask(TASK_IMPORT_COLLECTIONS);
                })
                .setNegativeButton(mRes.getString(R.string.no), (dialog, id) -> dialog.cancel()));
    }

    /**
     * Show dialog for user to confirm deletion of collection
     * @param name collection name
     */
    private void showDeleteConfirmation(final String name){

        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.warning))
                .setMessage(mRes.getString(R.string.delete_warning, name))
                .setCancelable(false)
                .setPositiveButton(mRes.getString(R.string.yes), (dialog, id) -> {
                    dialog.dismiss();
                    //Do the deleting
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
                })
                .setNegativeButton(mRes.getString(R.string.no), (dialog, id) -> dialog.cancel()));
    }

    /**
     * Makes a copy of the collection specified by tableName
     * @param tableName The collection name to make a copy of
     */
    public void copyCollection(String tableName) {

        // Get the source collection details
        CollectionListInfo sourceCollectionListInfo = null;
        int insertIndex = 0;
        for(int i = 0; i < mNumberOfCollections; i++){
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
            String suffixIndex = (attemptNumber == 0) ?  "" : Integer.toString(attemptNumber);
            newTableName = baseNewTableName + mRes.getString(R.string.copy_name_suffix) + suffixIndex;
            checkNameResult = mDbAdapter.checkCollectionName(newTableName);
            attemptNumber++;
        } while(checkNameResult != -1);

        // Create the new table
        CollectionListInfo newCollectionListInfo;
        try {
            newCollectionListInfo = mDbAdapter.createCollectionCopy(sourceCollectionListInfo, newTableName, insertIndex);
        } catch (SQLException e){
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
     * @param reorderedList The reordered list of collections
     */
    public void handleCollectionsReordered(ArrayList<CollectionListInfo> reorderedList){

        for(int i = 0; i < reorderedList.size(); i++){
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
     * @return info text string
     */
    public String buildInfoText(){
        HashSet<String> attributions = new HashSet<>();
        for(CollectionInfo collection : MainApplication.COLLECTION_TYPES){
            int attributionResId = collection.getAttributionResId();
            if (attributionResId == -1 || attributionResId == R.string.attr_mint) {
                // US mint attribution is included at the end
                continue;
            }
            String attributionStr = mRes.getString(attributionResId);
            if(attributionStr.equals("")){
                continue;
            }
            attributions.add(attributionStr);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(mRes.getString(R.string.info_overview));
        builder.append("\n\n");
        for(String attribution : attributions){
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
     * @return path string
     */
    public String getLegacyExportFolderName() {
        File sdCard = Environment.getExternalStorageDirectory();
        return sdCard.getAbsolutePath() + LEGACY_EXPORT_FOLDER_NAME;
    }

    /**
     * Returns the display name from a file URI
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
            mImportExportLegacyCsv = false;
            launchImportTask();
            return;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // The file picker was added in API 19, so don't give the user that option. Since there
            // is no choice, go directly to using legacy storage
            mImportExportLegacyCsv = true;
            launchImportTask();
            return;
        }

        // Populate a menu of actions for import
        CharSequence[] actionsList = new CharSequence[2];
        actionsList[0] = mRes.getString(R.string.pick_backup_file);
        actionsList[1] = mRes.getString(R.string.legacy_storage);
        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.import_place_message))
                .setItems(actionsList, (dialog, item) -> {
                    switch (item) {
                        case 0: {
                            // Pick back-up file
                            dialog.dismiss();
                            mImportExportLegacyCsv = false;
                            launchImportTask();
                            break;
                        }
                        case 1: {
                            // Legacy Storage
                            dialog.dismiss();
                            mImportExportLegacyCsv = true;
                            launchImportTask();
                            break;
                        }
                    }
                }));
    }

    /**
     * Allow users to pick between an export file format
     */
    private void promptCsvOrJsonExport() {

        if(mNumberOfCollections == 0){
            Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // If API is less than 19, only legacy storage is supported so go directly to that
            mImportExportLegacyCsv = true;
            mExportSingleFileCsv = false;
            launchExportTask();
            return;
        }

        // In API 30+, access to the SD card is disabled, so don't show this option after that
        boolean showLegacyExport = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q);

        // Populate a menu of actions for export
        CharSequence[] actionsList = new CharSequence[showLegacyExport ? 3 : 2];
        actionsList[0] = mRes.getString(R.string.json_file);
        actionsList[1] = mRes.getString(R.string.csv_file);
        if (showLegacyExport) {
            actionsList[2] = mRes.getString(R.string.legacy_storage);
        }
        showAlert(newBuilder()
                .setTitle(mRes.getString(R.string.export_format_message))
                .setItems(actionsList, (dialog, item) -> {
                    switch (item) {
                        case 0: {
                            // JSON file
                            dialog.dismiss();
                            mImportExportLegacyCsv = false;
                            mExportSingleFileCsv = false;
                            launchExportTask();
                            break;
                        }
                        case 1: {
                            // CSV file (single-file)
                            dialog.dismiss();
                            mImportExportLegacyCsv = false;
                            mExportSingleFileCsv = true;
                            launchExportTask();
                            break;
                        }
                        case 2: {
                            // Legacy CSV
                            dialog.dismiss();
                            mImportExportLegacyCsv = true;
                            mExportSingleFileCsv = false;
                            launchExportTask();
                            break;
                        }
                    }
                }));
    }
}
