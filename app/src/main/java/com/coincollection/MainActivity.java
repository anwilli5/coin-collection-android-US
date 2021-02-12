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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.coincollection.CollectionListInfo.COL_NAME;
import static com.coincollection.ReorderCollections.REORDER_COLLECTION;
import static com.spencerpages.MainApplication.APP_NAME;

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

    // These are used to support importing the collection data.  After we have read everything in,
    // we save the info here while we ask the user whether they really want to delete all of their
    // existing collections.
    private ArrayList<CollectionListInfo> mImportedCollectionListInfos = null;
    private ArrayList<ArrayList<CoinSlot>> mCollectionContents = null;
    private int mDatabaseVersion = -1;

    // Export directory path
    public final static String EXPORT_FOLDER_NAME = "/coin-collection-app-files";
    public final static String EXPORT_COLLECTION_LIST_FILE_NAME = "list-of-collections";
    public final static String EXPORT_COLLECTION_LIST_FILE_EXT = ".csv";
    public final static String EXPORT_DB_VERSION_FILE = "database_version.txt";

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

    // App permission requests
    private final static int IMPORT_PERMISSIONS_REQUEST = 0;
    private final static int EXPORT_PERMISSIONS_REQUEST = 1;

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
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {

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
                    }
                });

        // Now set the onItemClickListener to perform a certain action based on what's clicked
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

                            mBuilder = new AlertDialog.Builder(MainActivity.this);
                            mBuilder.setTitle(mRes.getString(R.string.select_collection_delete));
                            mBuilder.setItems(names, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    showDeleteConfirmation(mCollectionListEntries.get(item).getName());
                                }
                            });
                            showAlert();
                            break;
                        case IMPORT_COLLECTIONS:
                            handleImportCollectionsPart1();
                            break;
                        case EXPORT_COLLECTIONS:
                            handleExportCollectionsPart1();
                            break;
                        case REORDER_COLLECTIONS:

                            if(mNumberOfCollections == 0){
                                Toast.makeText(mContext, mRes.getString(R.string.no_collections), Toast.LENGTH_SHORT).show();
                                break;
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
                            break;
                        case ABOUT:

                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                            View layout = inflater.inflate(R.layout.info_popup,
                                    (ViewGroup) findViewById(R.id.info_layout_root));

                            mBuilder = new AlertDialog.Builder(MainActivity.this);
                            mBuilder.setView(layout);

                            TextView tv = layout.findViewById(R.id.info_textview);
                            tv.setText(buildInfoText());

                            showAlert();
                            break;
                    }

                    return;
                }
                // If it gets here, the user has selected a collection
                launchCoinPageActivity(mCollectionListEntries.get(position));
            }
        });

        // Add long-press handler for additional actions
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < mNumberOfCollections) {
                    // For each collection item, populate a menu of actions for the collection
                    mBuilder = new AlertDialog.Builder(MainActivity.this);
                    mBuilder.setTitle(mRes.getString(R.string.collection_actions));
                    CharSequence[] actionsList = new CharSequence[NUM_ACTIONS];
                    actionsList[ACTIONS_VIEW] = mRes.getString(R.string.view);
                    actionsList[ACTIONS_EDIT] = mRes.getString(R.string.edit);
                    actionsList[ACTIONS_COPY] = mRes.getString(R.string.copy);
                    actionsList[ACTIONS_DELETE] = mRes.getString(R.string.delete);
                    final int actionPosition = position;
                    mBuilder.setItems(actionsList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                                case ACTIONS_VIEW: {
                                    // Launch collection page
                                    launchCoinPageActivity(mCollectionListEntries.get(actionPosition));
                                    break;
                                }
                                case ACTIONS_EDIT: {
                                    // Launch edit view
                                    launchCoinPageCreatorActivity(mCollectionListEntries.get(actionPosition));
                                    break;
                                }
                                case ACTIONS_COPY: {
                                    // Perform copy
                                    copyCollection(mCollectionListEntries.get(actionPosition).getName());
                                    break;
                                }
                                case ACTIONS_DELETE: {
                                    // Perform delete
                                    showDeleteConfirmation(mCollectionListEntries.get(actionPosition).getName());
                                    break;
                                }
                            }
                        }
                    });
                    showAlert();
                    return true;
                }
                return false;
            }
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
        // unbind in onDestroy.  Once the app is terminating, all the activity onDestroy's
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
    public void onDestroy() {
        // Only MainActivity closes the DB adapter, as it's shared between all activities
        if (mDbAdapter != null) {
            mDbAdapter.close();
        }
        // Don't try and stop any tasks, as they could be in the middle of a DB upgrade
        super.onDestroy();
    }

    @Override
    public int asyncProgressDoInBackground() {
        switch (mTask.mAsyncTaskId) {
            case TASK_OPEN_DATABASE: {
                return openDbAdapterForAsyncThread();
            }
            case TASK_IMPORT_COLLECTIONS: {
                // Start doing all of the database importing
                return asyncHandleImportCollectionsPart2();
            }
        }
        return -1;
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
        }
    }

    @Override
    public void asyncProgressOnPostExecute(int errorResId) {
        super.asyncProgressOnPostExecute(errorResId);
        dismissProgressDialog();
        switch (mTask.mAsyncTaskId) {
            case TASK_OPEN_DATABASE: {
                updateCollectionListFromDatabaseAndUpdateViewForUIThread();
                break;
            }
            case TASK_IMPORT_COLLECTIONS: {
                mIsImportingCollection = false;
                updateCollectionListFromDatabaseAndUpdateViewForUIThread();
                break;
            }
        }
    }

    /**
     * Launches the collection page for a collection list entry
     * @param listEntry The collection to view
     */
    private void launchCoinPageActivity(CollectionListInfo listEntry) {
        Intent intent = new Intent(mContext, CollectionPage.class);
        intent.putExtra(CollectionPage.COLLECTION_NAME, listEntry.getName());
        intent.putExtra(CollectionPage.COLLECTION_TYPE_INDEX, listEntry.getCollectionTypeIndex());
        startActivity(intent);
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
     * Kicks off the import process by reading in the import files into our internal representation.
     * Once this is complete, it kicks off an AsyncTask to actually store the data in the database.
     */
    public void handleImportCollectionsPart1(){

        // Check for READ_EXTERNAL_STORAGE permissions (must request starting in API Level 23)
        // hasPermissions() will kick off the permissions request and the handler will re-call
        // this method after prompting the user.
        if(!hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, IMPORT_PERMISSIONS_REQUEST)){
            return;
        }

        // See whether we can read from the external storage
        String state = Environment.getExternalStorageState();
        //noinspection StatementWithEmptyBody
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Should be able to read from it without issue
        } else if (Environment.MEDIA_SHARED.equals(state)) {
            // Shared with PC so can't write to it
            showCancelableAlert(mRes.getString(R.string.cannot_rd_ext_media_shared));
            return;
        } else {
            // Doesn't exist, so notify user
            showCancelableAlert(mRes.getString(R.string.cannot_rd_ext_media_state, state));
            return;
        }

        String path = getExportFolderName();
        File dir = new File(path);

        if(!dir.isDirectory()){
            // The directory doesn't exist, notify the user
            showCancelableAlert(mRes.getString(R.string.cannot_find_export_dir, path));
            return;
        }

        boolean errorOccurred = false;

        // Read the database version
        File inputFile = new File(dir, EXPORT_DB_VERSION_FILE);
        CSVReader in = openFileForReading(inputFile);
        if(in == null) return;

        try {
            String[] values = in.readNext();
            if(values == null || values.length != 1){
                throw new Exception();
            }
            mDatabaseVersion = Integer.parseInt(values[0]);
        } catch (Exception e) {
            showCancelableAlert(mRes.getString(R.string.error_reading_file, inputFile.getAbsolutePath()));
            return;
        }

        if(mDatabaseVersion == -1){
            showCancelableAlert(mRes.getString(R.string.error_db_version));
            return;
        }

        if(!closeInputFile(in, inputFile)){
            return;
        }

        // Read the collection_info table
        inputFile = new File(dir, EXPORT_COLLECTION_LIST_FILE_NAME + EXPORT_COLLECTION_LIST_FILE_EXT);
        in = openFileForReading(inputFile);
        if(in == null) return;

        ArrayList<CollectionListInfo> collectionInfo = new ArrayList<>();

        try {
            String[] items;

            // TODO Raise an error if in.readNext returns null
            while (null != (items = in.readNext())) {

                CollectionListInfo info = new CollectionListInfo(items);

                if (info.getCollectionTypeIndex() == -1) {
                    showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 2));
                    break;
                }

                // Must have a positive number collected and a positive max
                if (info.getMax() < 0 || info.getCollected() < 0) {
                    errorOccurred = true;
                    showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 3));
                    break;
                }

                if (info.getDisplayType() != CollectionPage.SIMPLE_DISPLAY &&
                        info.getDisplayType() != CollectionPage.ADVANCED_DISPLAY) {
                    errorOccurred = true;
                    showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 5));
                    break;
                }

                if(errorOccurred){
                    break;
                }

                // Good to go, add it to the list
                collectionInfo.add(info);
            }
        } catch(Exception e){
            errorOccurred = true;
            showCancelableAlert(mRes.getString(R.string.error_unknown_read, inputFile.getAbsolutePath()));
        }

        // Close the input file.  If we've already shown an error then no
        // need to show another one if the close fails
        if(!closeInputFile(in, inputFile, errorOccurred)){
            return;
        }

        if(errorOccurred){
            // Don't continue on
            return;
        }

        // The ArrayList will be indexed by collection, the outer String[] will be indexed
        // by line number, and the inner String[] will be each cell in the row
        ArrayList<ArrayList<CoinSlot>> collectionContents = new ArrayList<>();

        // We loaded in the collection "metadata" table, so now load in each collection
        for(int i = 0; i < collectionInfo.size(); i++){

            CollectionListInfo collectionData = collectionInfo.get(i);

            // If any '/''s exist in the collection name, change them to "_SL_" to match
            // the export logic (used to prevent slashes from being confused as path
            // delimiters when opening the file.)
            String collectionFileName = collectionData.getName().replaceAll("/", "_SL_");

            inputFile = new File(dir, collectionFileName + ".csv");

            if(!inputFile.isFile()){
                showCancelableAlert(mRes.getString(R.string.cannot_find_input_file, inputFile.getAbsolutePath()));
                return;
            }

            in = openFileForReading(inputFile);
            if(in == null) return;

            ArrayList<CoinSlot> collectionContent = new ArrayList<>();

            try {
                String[] items;
                while(null != (items = in.readNext())){

                    // Perform some sanity checks and clean-up here
                    CoinSlot coinData = new CoinSlot(
                            (items.length > 0 ? items[0] : ""),
                            (items.length > 1 ? items[1] : ""),
                            (items.length > 2 && (Integer.parseInt(items[2]) != 0)),
                            (items.length > 3 ? Integer.parseInt(items[3]) : 0),
                            (items.length > 4 ? Integer.parseInt(items[4]) : 0),
                            (items.length > 5 ? items[5] : ""));

                    // TODO Maybe add more checks
                    collectionContent.add(coinData);
                }

            } catch(Exception e){
                errorOccurred = true;
                showCancelableAlert(mRes.getString(R.string.error_unknown_read, inputFile.getAbsolutePath()));
            }

            if(errorOccurred){
                // Don't continue on
                closeInputFile(in, inputFile, true);
                return;
            }

            // Verify that we read in the correct number of records
            if(collectionContent.size() != collectionData.getMax()){
                errorOccurred = true;
                showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 12));
            }
            collectionContents.add(collectionContent);

            if(!closeInputFile(in, inputFile)){
                return;
            }

            if(errorOccurred){
                // Don't continue on
                return;
            }
        }

        // Cool, at this point we've read in the data successfully and we've passed all of
        // the sanity checks.  We should put this data aside, show the user a message to
        // have them confirm that they want to do this... Although if they don't have any
        // collections we can optimize this step out
        mImportedCollectionListInfos = collectionInfo;
        mCollectionContents = collectionContents;

        if(mNumberOfCollections == 0){
            // Finish the import by kicking off an AsyncTask to do the heavy lifting
            kickOffAsyncProgressTask(TASK_IMPORT_COLLECTIONS);
        } else {
            showImportConfirmation();
        }
    }

    private void handleImportCollectionsCancel(){
        // Release the memory associated with the collection info we read in
        this.mImportedCollectionListInfos = null;
        this.mCollectionContents = null;
        this.mDatabaseVersion = -1;
    }

    /**
     * Finishes strong with some heavy lifting (putting the imported data into the database.)  This
     * should be done from an AsyncTask, since it could cause an ANR error if done on the main
     * thread!
     * @return -1 if successful, otherwise an error resource ID to display
     */
    public int asyncHandleImportCollectionsPart2() {

        // Take the data we've stored and replace what's in the database with it

        // NOTE We can't use the showCancelableAlert here because this doesn't get
        // executed on the main thread.

        try {
            // Drop existing tables
            for (int i = 0; i < mNumberOfCollections; i++) {
                CollectionListInfo info = mCollectionListEntries.get(i);
                mDbAdapter.dropCollectionTable(info.getName());
            }
            mDbAdapter.dropCollectionInfoTable();

            // Add new collections
            mDbAdapter.createCollectionInfoTable();
            for (int i = 0; i < mImportedCollectionListInfos.size(); i++) {
                CollectionListInfo collectionListInfo = mImportedCollectionListInfos.get(i);
                ArrayList<CoinSlot> collectionContents = mCollectionContents.get(i);

                // Check for duplicate or illegal names
                int checkName = mDbAdapter.checkCollectionName(collectionListInfo.getName());
                if (checkName != -1) {
                    return R.string.error_import;
                }
                mDbAdapter.createAndPopulateNewTable(collectionListInfo, i, collectionContents);
            }

            // Release the memory associated with the collection info we read in
            mImportedCollectionListInfos = null;
            mCollectionContents = null;

            // Update any imported tables, if necessary
            if (mDatabaseVersion != MainApplication.DATABASE_VERSION) {
                mDbAdapter.upgradeCollections(mDatabaseVersion, true);
            }
            mDatabaseVersion = -1;
        } catch (SQLException e) {
            // Report an import error message to display on the UI thread
            return R.string.error_import;
        }

        return -1;
    }

    /**
     * Begins the collection export process by doing some preliminary external media checks and
     * prompts the user if an export will overwrite previous backup files.
     */
    public void handleExportCollectionsPart1(){
        // TODO Move this function to be more resistant to ANR, if reports show that it is a
        // problem

        // Check for WRITE_EXTERNAL_STORAGE permissions (must request starting in API Level 23)
        // hasPermissions() will kick off the permissions request and the handler will re-call
        // this method after prompting the user.
        if(!hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, EXPORT_PERMISSIONS_REQUEST)){
            return;
        }

        // See whether we can write to the external storage
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
                // Should be able to write to it without issue
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                // Can't write to it, so notify user
                showCancelableAlert(mRes.getString(R.string.cannot_wr_ext_media_ro));
                return;
            case Environment.MEDIA_SHARED:
                // Shared with PC so can't write to it
                showCancelableAlert(mRes.getString(R.string.cannot_wr_ext_media_shared));
                return;
            default:
                // Doesn't exist, so notify user
                showCancelableAlert(mRes.getString(R.string.cannot_wr_ext_media_state, state));
                return;
        }

        String path = getExportFolderName();
        File dir = new File(path);

        if(dir.isDirectory() || dir.exists()){
            // Let the user decide whether they want to delete this
            showExportConfirmation();
        } else {
            // Proceed with exporting directly
            handleExportCollectionsPart2();
        }
    }

    /**
     * Finishes the collection export process (after giving the user a chance to cancel if this will
     * cause an existing backup to be deleted.)
     */
    private void handleExportCollectionsPart2(){

        // At this point we know we can write to storage and the user is ok
        // if we blow away existing imported files

        // TODO Move this function to be more resistant to ANR, if necessary

        String path = getExportFolderName();
        File dir = new File(path);

        if(!dir.isDirectory() && !dir.mkdir()){
            // The directory doesn't exist, notify the user
            showCancelableAlert(mRes.getString(R.string.failed_mk_dir, path));
            return;
        }

        // Write out the collection_info table
        File outputFile = new File(dir, EXPORT_COLLECTION_LIST_FILE_NAME + EXPORT_COLLECTION_LIST_FILE_EXT);
        CSVWriter out = openFileForWriting(outputFile);
        if(out == null) return;

        // Iterate through the list of collections and write the files
        for(int i = 0; i < mNumberOfCollections; i++){
            CollectionListInfo item = mCollectionListEntries.get(i);
            out.writeNext(item.getCsvExportProperties(mDbAdapter));
        }

        if(!closeOutputFile(out, outputFile)) return;

        // Write out the database version
        outputFile = new File(dir, EXPORT_DB_VERSION_FILE);
        out = openFileForWriting(outputFile);
        if (out == null) return;

        String[] version = new String[] { String.valueOf(MainApplication.DATABASE_VERSION) };
        out.writeNext(version);
        if(!closeOutputFile(out, outputFile)) return;

        // Write out all of the other tables
        for(int i = 0; i < mNumberOfCollections; i++){
            CollectionListInfo item = mCollectionListEntries.get(i);
            String name = item.getName();

            // Handle '/''s in the file names (otherwise importing will fail, because the OS will
            // think the '/' characters are folder delimiters.)  This will be undone when we import.
            String cleanName = name.replaceAll("/", "_SL_");

            outputFile = new File(dir, cleanName + ".csv");
            out = openFileForWriting(outputFile);
            if (out == null) return;

            // coinIdentifier, coinMint, inCollection, advGradeIndex, advQuantityIndex, advNotes
            ArrayList<CoinSlot> coinList = mDbAdapter.getCoinList(name, true);
            for (CoinSlot coinSlot : coinList) {
                String[] values = new String[] {
                        coinSlot.getIdentifier(),
                        coinSlot.getMint(),
                        String.valueOf(coinSlot.isInCollectionInt()),
                        String.valueOf(coinSlot.getAdvancedGrades()),
                        String.valueOf(coinSlot.getAdvancedQuantities()),
                        coinSlot.getAdvancedNotes()};
                out.writeNext(values);
            }

            if(!closeOutputFile(out, outputFile)) return;
        }
        showCancelableAlert(mRes.getString(R.string.success_export, EXPORT_FOLDER_NAME));
    }

    private CSVReader openFileForReading(File file){

        try {
            // Tell the CSVReader to use the NULL character as the escape
            // character to effectively allow no escape characters
            // (otherwise, '\' is the escape character, and it can be
            // typed by users!)
            return new CSVReader(new FileReader(file),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    '\0');
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString());
            showCancelableAlert(mRes.getString(R.string.error_open_file_reading, file.getAbsolutePath()));
            return null;
        }
    }

    private boolean closeInputFile(CSVReader in, File file){
        return closeInputFile(in, file, false);
    }

    private boolean closeInputFile(CSVReader in, File file, boolean silent){
        try {
            in.close();
        } catch (IOException e) {
            if(!silent){
                showCancelableAlert(mRes.getString(R.string.error_closing_input_file, file.getAbsolutePath()));
            }
            return false;
        }
        return true;
    }

    private CSVWriter openFileForWriting(File file){

        try {
            return new CSVWriter(new FileWriter(file));
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString());
            showCancelableAlert(mRes.getString(R.string.error_open_file_writing, file.getAbsolutePath()));
            return null;
        }
    }

    private boolean closeOutputFile(CSVWriter out, File file){
        try {
            out.close();
        } catch (IOException e) {
            showCancelableAlert(mRes.getString(R.string.error_closing_output_file, file.getAbsolutePath()));
            return false;
        }
        return true;
    }

    // https://developer.android.com/training/permissions/requesting.html
    // Expected: Manifest.permission.{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}
    private boolean hasPermissions(String permission, int callbackTag){

        int permissionState = ContextCompat.checkSelfPermission(this, permission);
        if (permissionState != PackageManager.PERMISSION_GRANTED) {

            // Not providing an explanation but the user should know what this is for
            // This will prompt the user to grant/deny permissions, and the result will
            // be delivered via a callback.
            ActivityCompat.requestPermissions(this, new String[]{permission}, callbackTag);

            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {

        if(  (grantResults.length > 0)
          && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            // Request Granted!
            switch (requestCode) {
                case IMPORT_PERMISSIONS_REQUEST: {
                    // Retry import, now with permissions granted
                    handleImportCollectionsPart1();
                    break;
                }
                case EXPORT_PERMISSIONS_REQUEST: {
                    // Retry export, now with permissions granted
                    handleExportCollectionsPart1();
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

        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage(mRes.getString(R.string.export_warning))
               .setCancelable(false)
               .setPositiveButton(mRes.getString(R.string.yes), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // TODO Maybe use AsyncTask, if necessary
                        handleExportCollectionsPart2();
                   }
               })
               .setNegativeButton(mRes.getString(R.string.no), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        showAlert();
    }

    /**
     * Show dialog for user to confirm import
     */
    private void showImportConfirmation(){

        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage(mRes.getString(R.string.import_warning))
               .setCancelable(false)
               .setPositiveButton(mRes.getString(R.string.yes), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Finish the import by kicking off an AsyncTask to do the heavy lifting
                       mIsImportingCollection = true;
                       kickOffAsyncProgressTask(TASK_IMPORT_COLLECTIONS);
                   }
               })
               .setNegativeButton(mRes.getString(R.string.no), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        handleImportCollectionsCancel();
                   }
               });
        showAlert();
    }

    /**
     * Show dialog for user to confirm deletion of collection
     * @param name collection name
     */
    private void showDeleteConfirmation(final String name){

        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage(mRes.getString(R.string.delete_warning, name))
               .setCancelable(false)
               .setPositiveButton(mRes.getString(R.string.yes), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       //Do the deleting
                       Cursor cursor = null;
                       try {
                           mDbAdapter.dropCollectionTable(name);
                           //Get a list of all the database tables
                           cursor = mDbAdapter.getAllCollectionNames();
                           int i = 0;
                           if (cursor.moveToFirst()) {
                               do {
                                   String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                                   // Fix up the displayOrder
                                   mDbAdapter.updateDisplayOrder(name, i);
                                   i++;
                               } while(cursor.moveToNext());
                           }
                           cursor.close();
                       } catch (SQLException e) {
                           showCancelableAlert(mRes.getString(R.string.error_delete_database));
                           if (cursor != null) {
                               cursor.close();
                           }
                       }
                   }
               })
               .setNegativeButton(mRes.getString(R.string.no), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        showAlert();
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
    private String buildInfoText(){
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
     * Returns the path to the external storage directory
     * @return path string
     */
    public String getExportFolderName(){
        File sdCard = Environment.getExternalStorageDirectory();
        return sdCard.getAbsolutePath() + EXPORT_FOLDER_NAME;
    }
}
