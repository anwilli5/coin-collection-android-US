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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import static com.coincollection.CoinSlot.COL_ADV_GRADE_INDEX;
import static com.coincollection.CoinSlot.COL_ADV_NOTES;
import static com.coincollection.CoinSlot.COL_ADV_QUANTITY_INDEX;
import static com.coincollection.CoinSlot.COL_COIN_IDENTIFIER;
import static com.coincollection.CoinSlot.COL_COIN_MINT;
import static com.coincollection.CoinSlot.COL_IN_COLLECTION;
import static com.spencerpages.MainApplication.APP_NAME;

/**
 * The main Activity for the app.  Implements a ListView which lets the user view a previously
 * created collection or add/delete/reorder/export/import collections
 */
public class MainActivity extends AppCompatActivity {

    public final ArrayList<CollectionListInfo> mCollectionListEntries = new ArrayList<>();
    private final Context mContext = this;
    private FrontAdapter mListAdapter;
    public DatabaseAdapter mDbAdapter = new DatabaseAdapter(this);
    private Resources mRes;

    // Data from caller intent
    public static final String UNIT_TEST_USE_ASYNC_TASKS = "unit-test-use-async-tasks";
    private boolean mUseAsyncTasks = true;

    // The number of actual collections in mCollectionListEntries
    public int mNumberOfCollections;

    // To be used with a simple cancel-able alert.  For more complicated alerts use a different one
    private AlertDialog.Builder mBuilder = null;


    // Used for the Update Database functionality
    private ProgressDialog mProgressDialog = null;
    private AsyncProgressTask mTask = null;
    private AsyncProgressInterface mImportInterface;
    private static final int ASYNC_TASK_OPEN_ID = 1;
    private static final int ASYNC_TASK_IMPORT_ID = 2;
    private boolean mDatabaseHasBeenOpened = false;
    private boolean mIsImportingCollection = false;

    // See notes in onCreate below.  Used to handle the case where we are importing collections and
    // the screen orientation changes
    private boolean mShouldFinishViewSetupToo = false;

    // These are used to support importing the collection data.  After we have read everything in,
    // we save the info here while we ask the user whether they really want to delete all of their
    // existing collections.
    // TODO Rename this to indicated that they are associated with importing
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_layout);

        mBuilder = new AlertDialog.Builder(this);
        mRes = getResources();
        Intent callingIntent = getIntent();
        mUseAsyncTasks = callingIntent.getBooleanExtra(UNIT_TEST_USE_ASYNC_TASKS, true);

        // In legacy code we used first_Time_screen2 here so that the message would be displayed
        // until they made it to the create collection screen.  That isn't necessary anymore, but
        // if they are upgrading from that don't show them the help screen if first_Time_screen1
        // isn't set
        createAndShowHelpDialog("first_Time_screen1", R.string.intro_message, this);

        // Set up the asynchronous portion of onCreate()
        setupOnCreateAsyncTasks();

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

    /**
     * Performs the async portion of the onCreate process
     */
    public void setupOnCreateAsyncTasks() {

        AsyncProgressInterface openInterface = new AsyncProgressInterface() {
            @Override
            public void asyncProgressDoInBackground() {
                DatabaseAdapter dbAdapter = new DatabaseAdapter(MainActivity.this);
                dbAdapter.open();
                // Now just close it, since the database will have updated and
                // that's really all we need this AsyncTask for
                dbAdapter.close();
            }
            @Override
            public void asyncProgressOnPreExecute() {
                createProgressDialog("Opening Databases...");
            }
            @Override
            public void asyncProgressOnPostExecute() {
                completeProgressDialogAndFinishViewSetup();
            }
        };

        // TODO If there is a screen orientation change, it looks like a ProgressDialog gets leaked. :(
        AsyncProgressTask check = (AsyncProgressTask) getLastCustomNonConfigurationInstance();
        if(check == null){
            if(BuildConfig.DEBUG) {
                Log.d(APP_NAME, "No previous state so kicking off AsyncProgressTask to doOpen");
            }
            // Kick off the AsyncProgressTask to open the database.  This will likely be the first open,
            // so we want it in the AsyncTask in case we have to go into onUpgrade and it takes
            // a long time.
            kickOffAsyncProgressTask(openInterface, ASYNC_TASK_OPEN_ID);
            // The AsyncProgressTask will call finishViewSetup once the database has been opened
            // for the first time
        } else {
            if(BuildConfig.DEBUG) {
                Log.d(APP_NAME, "Taking over existing mTask");
            }
            // An AsyncProgressTask is running, make a new dialog to show it
            mTask = check;
            // Change the task's listener the new activity will be the listener. See note above AsyncTask
            // definition for more info.
            mTask.mListener = openInterface;

            // There's two possible AsyncProgressTask's that could be running:
            //     - The one to open the database for the first time
            //     - The one to import collections
            // In the case of the former, we just want to show the dialog that the user had on the
            // screen.  For the latter case, we still need something to call finishViewSetup, and
            // we don't want to call it here bc it will try to use the database too early.  Instead,
            // set a flag that will have that AsyncProgressTask call finishViewSetup for us as well.
            if(mProgressDialog != null && mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }

            // Make a new dialog
            if(mTask.mAsyncTaskId == ASYNC_TASK_OPEN_ID) {
                createProgressDialog("Opening Databases...");
            } else if(mTask.mAsyncTaskId == ASYNC_TASK_IMPORT_ID){
                mDatabaseHasBeenOpened = true; // This has to have happened at this point
                mIsImportingCollection = true;
                mShouldFinishViewSetupToo = true;
                createProgressDialog("Importing Collections...");
            }
        }

        // Interface for kicking off collection import
        mImportInterface = new AsyncProgressInterface() {
            @Override
            public void asyncProgressDoInBackground() {
                // Start doing all of the file reading and database importing
                handleImportCollectionsPart2();
            }
            @Override
            public void asyncProgressOnPreExecute() {
                createProgressDialog("Importing Collections...");
            }
            @Override
            public void asyncProgressOnPostExecute() {
                completeProgressDialogAndFinishViewSetup();
            }
        };
    }

    /**
     * Finishes setting up the view once the database has been opened.
     */
    private void finishViewSetup(){

        // Called from the AsyncProgressTask after the database has been successfully
        // opened for the first time.  Now we can be fairly sure that future
        // open's won't trigger an ANR issue.

        if(BuildConfig.DEBUG) {
            Log.v("mainActivity", "finishViewSetup");
        }

        // Populate mCollectionListEntries with the data from the database
        updateCollectionListFromDatabase();

        // Instantiate the FrontAdapter
        mListAdapter = new FrontAdapter(mContext, mCollectionListEntries, mNumberOfCollections);

        ListView lv = findViewById(R.id.main_activity_listview);

        lv.setAdapter(mListAdapter);

        // TODO Not sure what this does?
        lv.setTextFilterEnabled(true); // Typing narrows down the list

        // For when we use fragments, listen to the back stack so we can transition back here from
        // the fragment

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {

                        if(0 == getSupportFragmentManager().getBackStackEntryCount()){

                            // We are back at this activity, so restore the ActionBar
                            ActionBar actionBar = getSupportActionBar();
                            if(actionBar != null){
                                actionBar.setTitle(mRes.getString(R.string.app_name));
                                actionBar.setDisplayHomeAsUpEnabled(false);
                                actionBar.setHomeButtonEnabled(false);
                            }

                            // The collections may have been re-ordered, so update them here.
                            updateCollectionListFromDatabase();

                            // Change it out with the new list
                            mListAdapter.items = mCollectionListEntries;
                            mListAdapter.numberOfCollections = mNumberOfCollections;
                            mListAdapter.notifyDataSetChanged();
                        }
                    }
                });

        // Now set the onItemClickListener to perform a certain action based on what's clicked
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // See whether it was one of the special list entries (Add collection, delete
                // collection, etc.)
                if(position >= mNumberOfCollections){

                    int newPosition = position - mNumberOfCollections;
                    Intent intent;

                    switch(newPosition){
                        case ADD_COLLECTION:
                            intent = new Intent(mContext, CoinPageCreator.class);
                            startActivity(intent);
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

                            AlertDialog.Builder delete_builder = new AlertDialog.Builder(mContext);
                            delete_builder.setTitle(mRes.getString(R.string.select_collection_delete));
                            delete_builder.setItems(names, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    showDeleteConfirmation(mCollectionListEntries.get(item).getName());
                                }
                            });
                            AlertDialog alert = delete_builder.create();
                            alert.show();
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
                                    .add(R.id.main_activity_frame, fragment, "ReorderFragment")
                                    .addToBackStack(null)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .commit();

                            // Setup the actionbar for the reorder page
                            // TODO Check for NULL
                            ActionBar actionBar = getSupportActionBar();
                            if(actionBar != null){
                                actionBar.setTitle(mRes.getString(R.string.reorder_collection));
                                actionBar.setDisplayHomeAsUpEnabled(true);
                                actionBar.setHomeButtonEnabled(true);
                            }

                            break;
                        case ABOUT:

                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                            View layout = inflater.inflate(R.layout.info_popup,
                                                           (ViewGroup) findViewById(R.id.info_layout_root));

                            AlertDialog.Builder info_builder = new AlertDialog.Builder(mContext);
                            info_builder.setView(layout);

                            TextView tv = layout.findViewById(R.id.info_textview);
                            tv.setText(buildInfoText());

                            AlertDialog alertDialog = info_builder.create();
                            alertDialog.show();
                            break;
                    }

                    return;
                }
                // If it gets here, the user has selected a collection

                Intent intent = new Intent(mContext, CollectionPage.class);

                CollectionListInfo listEntry = mCollectionListEntries.get(position);

                intent.putExtra(CollectionPage.COLLECTION_NAME, listEntry.getName());
                intent.putExtra(CollectionPage.COLLECTION_TYPE_INDEX, listEntry.getCollectionTypeIndex());

                startActivity(intent);
            }
        });
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

        if(-1 == mDatabaseVersion){
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
            while(null != (items = in.readNext())){

                // Perform some sanity checks here
                if(items.length != 4 && items.length != 5){
                    errorOccurred = true;
                    showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 1));
                    break;
                }

                String name = items[0];
                String type = items[1];
                int totalCollected = Integer.parseInt(items[2]);
                int total = Integer.parseInt(items[3]);
                int displayType = (items.length > 4) ? Integer.parseInt(items[4]) : 0;

                // Strip out all bad characters.  They shouldn't be there anyway ;)
                name = name.replace('[', ' ');
                name = name.replace(']', ' ');

                // Must be a valid coin type
                int i;
                for(i = 0; i < MainApplication.COLLECTION_TYPES.length; i++){

                    if(MainApplication.COLLECTION_TYPES[i].getCoinType().equals(type)){
                        break;
                    } else if(MainApplication.COLLECTION_TYPES.length == i + 1){
                        errorOccurred = true;
                        showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 2));
                        break;
                    }
                }

                int coinTypeIndex = i;

                if(errorOccurred){
                    break;
                }

                // Must have a positive number collected and a positive max
                if(totalCollected < 0 || total < 0){
                    errorOccurred = true;
                    showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 3));
                    break;
                }

                // Must not have a name that is the same as a previous one
                for(i = 0; i < collectionInfo.size(); i++){

                    CollectionListInfo previousCollectionListInfo = collectionInfo.get(i);
                    if(name.equals(previousCollectionListInfo.getName())){
                        errorOccurred = true;
                        showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 4));
                        break;
                    }
                }

                if(displayType != CollectionPage.SIMPLE_DISPLAY && displayType != CollectionPage.ADVANCED_DISPLAY){
                    errorOccurred = true;
                    showCancelableAlert(mRes.getString(R.string.error_invalid_backup_file, 5));
                    break;
                }

                if(errorOccurred){
                    break;
                }

                // Everything checks out, so create a new CollectionListInfo
                // for this
                CollectionListInfo info = new CollectionListInfo(name, total, totalCollected, coinTypeIndex, displayType);

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
                    int numberOfColumns = 6;
                    CoinSlot coinData = new CoinSlot(
                            (items.length > 0 ? items[0] : ""),
                            (items.length > 1 ? items[1] : ""),
                            (items.length > 2 ? (Integer.valueOf(items[2]) != 0) : false),
                            (items.length > 3 ? Integer.valueOf(items[3]) : 0),
                            (items.length > 4 ? Integer.valueOf(items[4]) : 0),
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

        if(0 == mNumberOfCollections){
            // Finish the import by kicking off an AsyncTask to do the heavy lifting
            kickOffAsyncProgressTask(mImportInterface, ASYNC_TASK_IMPORT_ID);
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
     */
    public void handleImportCollectionsPart2(){

        // Take the data we've stored and replace what's in the database with it

        // NOTE We can't use the showCancelableAlert here because this doesn't get
        // executed on the main thread.

        mDbAdapter.open();

        // TODO Consider how to make this more robust to failures
        for(int i = 0; i < mNumberOfCollections; i++){

            CollectionListInfo info = mCollectionListEntries.get(i);
            mDbAdapter.dropTable(info.getName());
        }

        mDbAdapter.dropCollectionInfoTable();

        mDbAdapter.createCollectionInfoTable();

        for(int i = 0; i < mImportedCollectionListInfos.size(); i++){
            CollectionListInfo collectionListInfo = mImportedCollectionListInfos.get(i);
            ArrayList<CoinSlot> collectionContents = mCollectionContents.get(i);

            String name = collectionListInfo.getName();
            String coinType = collectionListInfo.getCollectionObj().getCoinType();
            int total = collectionListInfo.getMax();
            int displayType = collectionListInfo.getDisplayType();

            mDbAdapter.createNewTable(name, coinType, total, displayType, i, collectionContents);
        }

        // Release the memory associated with the collection info we read in
        mImportedCollectionListInfos = null;
        mCollectionContents = null;

        // Update any imported tables, if necessary
        if(mDatabaseVersion != MainApplication.DATABASE_VERSION) {
            mDbAdapter.upgradeCollections(mDatabaseVersion, true);
        }
        mDatabaseVersion = -1;

        mDbAdapter.close();

        // Looks like the view gets reloaded automatically... Hooray!
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

        boolean errorOccurred = false;

        // Write out the collection_info table
        File outputFile = new File(dir, EXPORT_COLLECTION_LIST_FILE_NAME + EXPORT_COLLECTION_LIST_FILE_EXT);
        CSVWriter out = openFileForWriting(outputFile);
        if(out == null) return;

        mDbAdapter.open();

        // Iterate through the list of collections and write the files
        for(int i = 0; i < mNumberOfCollections; i++){
            // name, coinType, total, max, display

            CollectionListInfo item = mCollectionListEntries.get(i);

            String name = item.getName();
            String type = item.getType();
            String totalCollected = String.valueOf(item.getCollected());
            String total = String.valueOf(item.getMax());

            // NOTE For display, don't use item.getDisplayType bc I don't
            // think we populate that value except when importing...
            // TODO Update the code so that this value is used instead
            // of the separate fetchTableDisplay calls
            String display = String.valueOf(mDbAdapter.fetchTableDisplay(name));

            String [] values = new String[] {name, type, totalCollected, total, display};

            out.writeNext(values);
        }

        if(!closeOutputFile(out, outputFile)){
            return;
        }

        if(errorOccurred){
            return;
        }

        // Write out the database version
        outputFile = new File(dir, EXPORT_DB_VERSION_FILE);
        out = openFileForWriting(outputFile);
        if(out == null) return;

        String[] version = new String[] { String.valueOf(MainApplication.DATABASE_VERSION) };
        out.writeNext(version);

        if(!closeOutputFile(out, outputFile)){
            return;
        }

        // Write out all of the other tables
        for(int i = 0; i < mNumberOfCollections; i++){
            CollectionListInfo item = mCollectionListEntries.get(i);
            String name = item.getName();

            // Handle '/''s in the file names (otherwise importing will fail, because the OS will
            // think the '/' characters are folder delimiters.)  This will be undone when we import.
            String cleanName = name.replaceAll("/", "_SL_");

            outputFile = new File(dir, cleanName + ".csv");
            out = openFileForWriting(outputFile);
            if(out == null) return;

            // coinIdentifier, coinMint, inCollection, advGradeIndex, advQuantityIndex, advNotes
            Cursor resultCursor = mDbAdapter.getAllCollectionInfo(name);
            if (resultCursor.moveToFirst()) {
                do {
                    String coinIdentifier = resultCursor.getString(resultCursor.getColumnIndex("coinIdentifier"));
                    String coinMint = resultCursor.getString(resultCursor.getColumnIndex("coinMint"));
                    String inCollection = String.valueOf(resultCursor.getInt(resultCursor.getColumnIndex("inCollection")));
                    String advGradeIndex = String.valueOf(resultCursor.getInt(resultCursor.getColumnIndex("advGradeIndex")));
                    String advQuantityIndex = String.valueOf(resultCursor.getInt(resultCursor.getColumnIndex("advQuantityIndex")));
                    String advNotes = resultCursor.getString(resultCursor.getColumnIndex("advNotes"));

                    String[] values = new String[] {coinIdentifier, coinMint, inCollection, advGradeIndex, advQuantityIndex, advNotes};

                    out.writeNext(values);

                } while(resultCursor.moveToNext());
            }
            resultCursor.close();

            if(!closeOutputFile(out, outputFile)){
                return;
            }
        }

        mDbAdapter.close();

        showCancelableAlert(mRes.getString(R.string.success_export, EXPORT_FOLDER_NAME));
    }

    private void showCancelableAlert(String text) {
        mBuilder.setMessage(text).setCancelable(true);
        AlertDialog alert = mBuilder.create();
        alert.show();
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
            super(context, R.layout.list_element, R.id.textView1, items);
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

            if(1 == viewType){
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

        if(hasFocus && mDatabaseHasBeenOpened && !mIsImportingCollection){

            // Only do this if the database has been opened with the AsyncTask first
            // and we aren't modifying the database like crazy (importing)
            // We need this so that new collections that are added/removed get shown

            updateCollectionListFromDatabase();

            // Change it out with the new list
            mListAdapter.items = mCollectionListEntries;
            mListAdapter.numberOfCollections = mNumberOfCollections;
            mListAdapter.notifyDataSetChanged();

        }
    }

    /**
     * Reloads the collection list from the database.  This is useful after changes have been made
     * (collections reordered, deleted, etc.)
     */
    public void updateCollectionListFromDatabase(){

        // Get rid of the other items in the list (if any)
        mCollectionListEntries.clear();

        // Open the database.  The "big" open should have been called already.
        mDbAdapter.open();

        //Get a list of all the database tables
        Cursor resultCursor = mDbAdapter.getAllTables();

        if (resultCursor.moveToFirst()){
            do{
                CollectionListInfo listEntry = new CollectionListInfo();

                String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
                int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));

                // Figure out what collection type maps to this
                // TODO Not the best way to do this, find a better one
                int index;
                for(index = 0; index < MainApplication.COLLECTION_TYPES.length; index++){
                    if(MainApplication.COLLECTION_TYPES[index].getCoinType().equals(coinType)){
                        break;
                    }
                }
                // TODO Consider adding error check in case we didn't find the name in typeOfCoins...
                // This is pretty unlikely, though
                listEntry.setName(name);
                listEntry.setMax(total);
                listEntry.setCollected(mDbAdapter.fetchTotalCollected(name));
                listEntry.setCollectionTypeIndex(index);

                // Add it to the list of collections
                mCollectionListEntries.add(listEntry);

            } while(resultCursor.moveToNext());
        }
        resultCursor.close();

        mDbAdapter.close();

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
     * Show dialog for user to confirm export
     */
    private void showExportConfirmation(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mRes.getString(R.string.export_warning))
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
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Show dialog for user to confirm import
     */
    private void showImportConfirmation(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mRes.getString(R.string.import_warning))
               .setCancelable(false)
               .setPositiveButton(mRes.getString(R.string.yes), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Finish the import by kicking off an AsyncTask to do the heavy lifting
                       mIsImportingCollection = true;
                       kickOffAsyncProgressTask(mImportInterface, ASYNC_TASK_IMPORT_ID);
                   }
               })
               .setNegativeButton(mRes.getString(R.string.no), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        handleImportCollectionsCancel();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Show dialog for user to confirm deletion of collection
     * @param name collection name
     */
    private void showDeleteConfirmation(final String name){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mRes.getString(R.string.delete_warning, name))
               .setCancelable(false)
               .setPositiveButton(mRes.getString(R.string.yes), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       //Do the deleting
                       mDbAdapter.open();
                       mDbAdapter.dropTable(name);

                       //Get a list of all the database tables
                       Cursor resultCursor = mDbAdapter.getAllCollectionNames();
                       int i = 0;
                       if (resultCursor.moveToFirst()){
                           do{
                               String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                               // Fix up the displayOrder
                               mDbAdapter.updateDisplayOrder(name,i);
                               i++;
                           }while(resultCursor.moveToNext());
                       }
                       resultCursor.close();

                       mDbAdapter.close();
                   }
               })
               .setNegativeButton(mRes.getString(R.string.no), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // https://raw.github.com/commonsguy/cw-android/master/Rotation/RotationAsync/src/com/commonsware/android/rotation/async/RotationAsync.java
    // TODO Consider only using one of onSaveInstanceState and onRetainNonConfigurationInstanceState
    // TODO Also, read the notes on this better and make sure we are using it correctly
    @Override
    public Object onRetainCustomNonConfigurationInstance(){

        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
            return mTask;
        } else {
            // No dialog showing, do nothing
            return null;
        }
    }

    @Override
    public void onDestroy(){
        // TODO Not a perfect solution, but assuming this gets called, we should cut down on the
        // race condition inherent in how we do our AsyncTask
        if(mTask != null) {
            mTask.mListener = null;
        }
        super.onDestroy();
    }

    /**
     * Takes the reordered list of collections in from the ReorderCollections fragment and updates
     * the ordering in the database.
     *
     * @param reorderedList The reordered list of collections
     */
    public void handleCollectionsReordered(ArrayList<CollectionListInfo> reorderedList){

        mDbAdapter.open();
        for(int i = 0; i < reorderedList.size(); i++){
            CollectionListInfo info = reorderedList.get(i);
            mDbAdapter.updateDisplayOrder(info.getName(),i);
            mCollectionListEntries.set(i, info);
        }
        mDbAdapter.close();
    }

    private String buildInfoText(){
        HashSet<String> attributions = new HashSet<>();
        for(CollectionInfo collection : MainApplication.COLLECTION_TYPES){
            String attribution = collection.getAttributionString();

            if(attribution.equals("")){
                continue;
            }

            attributions.add(attribution);
        }

        StringBuilder builder = new StringBuilder();

        builder.append(getResources().getString(R.string.info_overview));
        builder.append("\n\n");
        for(String attribution : attributions){
            builder.append(attribution);
            builder.append("\n\n");
        }

        return builder.toString();
    }

    /**
     * Create a new progress dialog for initial collection creation
     */
    private void createProgressDialog(String message){
        if (mProgressDialog != null){
            // Progress bar already being displayed
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(message);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
    }

    /**
     * Hide the dialog and finish view setup
     */
    private void completeProgressDialogAndFinishViewSetup(){

        if(mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mProgressDialog = null;
        }
        if(mTask.mAsyncTaskId == ASYNC_TASK_OPEN_ID){
            mDatabaseHasBeenOpened = true;
            finishViewSetup();
        } else if (mTask.mAsyncTaskId == ASYNC_TASK_IMPORT_ID){
            mIsImportingCollection = false;
            // If we've rotated and need to finish setting up the view, do it
            if(mShouldFinishViewSetupToo) {
                finishViewSetup();
                mShouldFinishViewSetupToo = false;
            }
        }
    }

    /**
     * Builds the list element for displaying collections
     * @param item Collection list info item
     * @param view view that needs to be populated
     * @param res Used to access project string values
     */
    public static void buildListElement(CollectionListInfo item, View view, Resources res){

        String tableName = item.getName();

        int total = item.getCollected();
        if (tableName != null) {

            ImageView image = view.findViewById(R.id.imageView1);
            if (image != null) {
                image.setBackgroundResource(item.getCoinImageIdentifier());
            }

            TextView nameTextView = view.findViewById(R.id.textView1);
            if (nameTextView != null) {
                nameTextView.setText(tableName);
            }

            TextView progressTextView = view.findViewById(R.id.textView2);
            if(progressTextView != null){
                progressTextView.setText(res.getString(R.string.collection_completion_template, total, item.getMax()));
            }

            TextView completionTextView = view.findViewById(R.id.textView3);
            if(total >= item.getMax()){
                // The collection is complete
                if(completionTextView != null){
                    completionTextView.setText(res.getString(R.string.collection_complete));
                }
            } else {
                completionTextView.setText("");
            }
        }
    }

    /**
     * Create a help dialog to show the user how to do something
     * @param helpStrKey key uniquely identifying this boolean key
     * @param helpStrId Help message to display
     * @param activity activity reference
     */
    public static void createAndShowHelpDialog (final String helpStrKey, int helpStrId, Activity activity){
        final SharedPreferences mainPreferences = activity.getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        final Resources res = activity.getResources();
        if(mainPreferences.getBoolean(helpStrKey, true)){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(res.getString(helpStrId))
                    .setCancelable(false)
                    .setPositiveButton(res.getString(R.string.okay_exp), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            SharedPreferences.Editor editor = mainPreferences.edit();
                            editor.putBoolean(helpStrKey, false);
                            editor.apply();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Show an alert that changes aren't saved before changing views
     */
    public static void showUnsavedChangesAlertViewChange(Resources res, final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(res.getString(R.string.dialog_unsaved_changes_change_views))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nothing to do, just a warning
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Show an alert that changes aren't saved before exiting activity
     */
    public static void showUnsavedChangesAlertAndExitActivity(Resources res, final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(res.getString(R.string.dialog_unsaved_changes_exit))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }})
                .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }});
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Returns the path to the external storage directory
     * @return path string
     */
    public String getExportFolderName(){
        File sdCard = Environment.getExternalStorageDirectory();
        return sdCard.getAbsolutePath() + EXPORT_FOLDER_NAME;
    }

    /**
     * Create and kick-off an async task to finish long-running tasks
     * @param asyncInterface caller interface
     * @param taskId type of task
     * @return async task
     */
    public void kickOffAsyncProgressTask(AsyncProgressInterface asyncInterface, int taskId){
        if (this.mUseAsyncTasks || !BuildConfig.DEBUG) {
            mTask = new AsyncProgressTask(asyncInterface);
            mTask.mAsyncTaskId = taskId;
            mTask.execute();
        } else {
            // Call the tasks on the current thread (used for unit tests)
            mTask = new AsyncProgressTask(asyncInterface);
            mTask.mAsyncTaskId = taskId;
            asyncInterface.asyncProgressOnPreExecute();
            asyncInterface.asyncProgressDoInBackground();
            asyncInterface.asyncProgressOnPostExecute();
        }
    }
}
