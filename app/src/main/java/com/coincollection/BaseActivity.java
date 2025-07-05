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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.coincollection.helper.NonLeakingAlertDialogBuilder;
import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

/**
 * Base activity containing shared functions and resources between the activities
 */
public class BaseActivity extends AppCompatActivity implements AsyncProgressInterface {

    /**
     * ViewModel to hold state that needs to survive configuration changes
     */
    public static class ActivityViewModel extends ViewModel {
        public AsyncTaskRunner mSavedTaskRunner;
    }

    // Unit test flag for disabling async tasks
    public static boolean isUnitTest = false;

    // Async Task info
    protected ActivityViewModel mActivityViewModel = null;

    protected AsyncTaskRunner mTaskRunner = null;
    public static final int TASK_NONE = -1;
    public static final int TASK_OPEN_DATABASE = 0;
    public static final int TASK_IMPORT_COLLECTIONS = 1;
    public static final int TASK_CREATE_UPDATE_COLLECTION = 2;
    public static final int TASK_EXPORT_COLLECTIONS = 3;

    // Common activity variables
    protected final Context mContext = this;
    protected ProgressDialog mProgressDialog;
    public Resources mRes;
    protected Intent mCallingIntent;
    public DatabaseAdapter mDbAdapter = null;
    protected ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the database adapter
        mDbAdapter = ((MainApplication) getApplication()).getDbAdapter();

        // Add a manual inset handler
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (BuildConfig.DEBUG) {
            // Set StrictMode policies to help debug potential issues
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .permitDiskReads() // TODO - Fix these and remove
                    .permitDiskWrites() // TODO - Fix these and remove
                    .penaltyLog()
                    //.penaltyDeath() // TODO - Uncomment once fixed
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    //.penaltyDeath() // TODO - Uncomment once fixed
                    .build());
        }

        // Setup variables used across all activities
        mRes = getResources();
        mCallingIntent = getIntent();
        mActionBar = getSupportActionBar();

        // Look for async tasks kicked-off prior to an orientation change
        mActivityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        if (mActivityViewModel.mSavedTaskRunner != null) {
            mTaskRunner = mActivityViewModel.mSavedTaskRunner;
        } else {
            mTaskRunner = new AsyncTaskRunner(this);
            mActivityViewModel.mSavedTaskRunner = mTaskRunner;
        }

        // Open the database if it isn't already open
        if (!mDbAdapter.isOpen()) {
            // Use AsyncTaskRunner to open the database in case onUpgrade() is called, which is slow
            kickOffAsyncTaskRunner(TASK_OPEN_DATABASE);
        } else {
            // Display the progress dialog for the existing tasks (i.e. import/export)
            asyncProgressOnPreExecute(mTaskRunner.getLatestTaskId());
        }
    }

    /**
     * This method should be called when mDbAdapter can be opened on the UI thread
     *
     * @return An error message if the open failed, otherwise -1
     */
    public String openDbAdapterForAsyncThread() {
        try {
            mDbAdapter.open();
        } catch (SQLException e) {
            return mRes.getString(R.string.error_opening_database);
        }
        return "";
    }

    /**
     * This should be overridden by Activities that use the AsyncTaskRunner
     * - This is method contains the work that needs to be performed on the async task
     *
     * @param taskId an integer representing the task ID
     * @return a string result to display, or "" if no result
     */
    @Override
    public String asyncProgressDoInBackground(int taskId) {
        if (taskId == TASK_OPEN_DATABASE) {
            return openDbAdapterForAsyncThread();
        }
        return "";
    }

    /**
     * This should be overridden by Activities that use the AsyncTaskRunner
     * - This is method is called on the UI thread ahead of executing DoInBackground
     * 
     * @param taskId an integer representing the task ID
     */
    @Override
    public void asyncProgressOnPreExecute(int taskId) {
        switch (taskId) {
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
            case TASK_CREATE_UPDATE_COLLECTION: {
                createProgressDialog(mRes.getString(R.string.creating_collection));
                break;
            }
        }
    }

    /**
     * This should be overridden by Activities that use the AsyncTask
     * - This is method is called on the UI thread after executing DoInBackground
     * - Activities should call super.asyncProgressOnPostExecute to display the error
     *
     * @param taskId an integer representing the task ID
     * @param resultStr a string result to display, or "" if no result
     */
    @Override
    public void asyncProgressOnPostExecute(int taskId, String resultStr) {
        if (!resultStr.isEmpty()) {
            showCancelableAlert(resultStr);
        }
    }

    /**
     * Activities that make use of the async task should call this once their UI state
     * is ready for an already running async task to call back
     */
    protected void setActivityReadyForAsyncCallbacks() {
        mTaskRunner.setListener(this);
    }

    /**
     * Displays a message to the user
     *
     * @param text The text to be displayed
     */
    public void showCancelableAlert(String text) {
        showAlert(newBuilder().setMessage(text).setCancelable(true));
    }

    @Override
    public void onPause() {
        // Dismiss any open alerts to prevent memory leaks
        dismissAllAlerts();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            dismissProgressDialog();
        }
        // If an async task is running, set the listener to null to have it wait before
        // trying its callback. Setting the listener to null also prevents memory leaks
        if (mTaskRunner != null) {
            mTaskRunner.clearListener();
            mTaskRunner = null;
        }
        super.onDestroy();
    }

    /**
     * Create a new progress dialog
     */
    protected void createProgressDialog(String message) {
        dismissProgressDialog();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(message);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
    }

    /**
     * Hides the progress dialog
     */
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mProgressDialog = null;
        }
    }

    /**
     * Hide the dialog and finish the activity
     */
    protected void completeProgressDialogAndFinishActivity() {
        dismissProgressDialog();
        this.finish();
    }

    /**
     * Builds the list element for displaying collections
     *
     * @param item Collection list info item
     * @param view view that needs to be populated
     * @param res  Used to access project string values
     */
    public static void buildListElement(CollectionListInfo item, View view, Resources res) {

        String tableName = item.getName();

        int total = item.getCollected();
        if (tableName != null) {

            ImageView image = view.findViewById(R.id.coinImageView);
            if (image != null) {
                image.setBackgroundResource(item.getCoinImageIdentifier());
            }

            TextView nameTextView = view.findViewById(R.id.collectionNameTextView);
            if (nameTextView != null) {
                nameTextView.setText(tableName);
            }

            TextView progressTextView = view.findViewById(R.id.progressTextView);
            if (progressTextView != null) {
                progressTextView.setText(res.getString(R.string.collection_completion_template, total, item.getMax()));
            }

            TextView completionTextView = view.findViewById(R.id.completeTextView);
            if (total >= item.getMax()) {
                // The collection is complete
                if (completionTextView != null) {
                    completionTextView.setText(res.getString(R.string.collection_complete));
                }
            } else {
                completionTextView.setText("");
            }
        }
    }

    /**
     * Create a help dialog to show the user how to do something
     *
     * @param helpStrKey key uniquely identifying this boolean key
     * @param helpStrId  Help message to display
     * @return true if the help dialog was displayed, otherwise false
     */
    public boolean createAndShowHelpDialog(final String helpStrKey, int helpStrId) {
        final SharedPreferences mainPreferences = this.getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE);
        final Resources res = this.getResources();
        if (mainPreferences.getBoolean(helpStrKey, true)) {
            showAlert(newBuilder()
                    .setMessage(res.getString(helpStrId))
                    .setCancelable(false)
                    .setPositiveButton(res.getString(R.string.okay_exp), (dialog, id) -> {
                        dialog.dismiss();
                        SharedPreferences.Editor editor = mainPreferences.edit();
                        editor.putBoolean(helpStrKey, false);
                        editor.apply();
                    }));
            return true;
        }
        return false;
    }

    /**
     * Creates a new alerter builder and cleans up any previous builders,
     * to prevent memory leaks
     *
     * @return new builder object
     */
    protected NonLeakingAlertDialogBuilder newBuilder() {
        return new NonLeakingAlertDialogBuilder(this);
    }

    /**
     * Uses builder to create and show an alert
     *
     * @param builder to use to create alert
     */
    protected void showAlert(NonLeakingAlertDialogBuilder builder) {
        // Don't show alerts in unit tests since there isn't a UI, and
        // it will spam the log with this: Invalid ID 0x00000000.
        if (!isUnitTest || !BuildConfig.DEBUG) {
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Cleans up any notifications currently shown to users
     */
    protected void dismissAllAlerts() {
        dismissProgressDialog();
    }

    /**
     * Create and kick-off an async task to finish long-running tasks
     *
     * @param taskId type of task
     */
    public void kickOffAsyncTaskRunner(int taskId) {
        if (!isUnitTest || !BuildConfig.DEBUG) {
            mTaskRunner.execute(taskId);
        } else {
            // Call the tasks on the current thread (used for unit tests)
            asyncProgressOnPreExecute(taskId);
            String resultStr = asyncProgressDoInBackground(taskId);
            asyncProgressOnPostExecute(taskId, resultStr);
        }
    }

    /**
     * Applies window insets to the view, setting padding based on system bars
     *
     * @param view The view to apply insets to
     */
    void applyWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });
    }
}
