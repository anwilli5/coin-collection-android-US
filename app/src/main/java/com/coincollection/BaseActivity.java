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

import static com.coincollection.dialog.DialogRequests.KEY_PAYLOAD;
import static com.coincollection.dialog.DialogRequests.KEY_REQUEST_ID;
import static com.coincollection.dialog.DialogRequests.PAYLOAD_HELP_KEY;
import static com.coincollection.dialog.DialogRequests.REQUEST_HELP_DIALOG;
import static com.coincollection.dialog.DialogRequests.REQUEST_KEY_BASE_ACTIVITY;
import static com.coincollection.dialog.DialogRequests.REQUEST_NONE;
import static com.coincollection.dialog.DialogRequests.TAG_ALERT_PREFIX;
import static com.coincollection.dialog.DialogRequests.TAG_HELP;
import static com.coincollection.dialog.DialogRequests.TAG_PROGRESS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.coincollection.dialog.MessageDialogFragment;
import com.coincollection.dialog.ProgressDialogFragment;
import com.spencerpages.BuildConfig;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Base activity containing shared functions and resources between the activities
 */
public class BaseActivity extends AppCompatActivity implements AsyncProgressInterface {

    /**
     * ViewModel to hold state that needs to survive configuration changes
     */
    public static class ActivityViewModel extends ViewModel {
        public AsyncTaskRunner mSavedTaskRunner;
        // Inputs for the in-flight async task, captured on the UI thread so the
        // background thread never has to read them from a View and so they survive
        // a configuration change.
        public final TaskRequest mTaskRequest = new TaskRequest();
        // Alert text captured when the activity wasn't in a state where a dialog
        // could be shown, held here so it survives a configuration change and can
        // be shown once the activity is resumed again. More than one alert can be
        // raised while stopped, so they are held in order instead of the later one
        // replacing the earlier.
        public final ArrayList<String> mPendingAlertText = new ArrayList<>();
        // Counter used to give each cancelable alert a tag of its own. Held here so
        // that a tag is never reused by an alert shown after a configuration change.
        public int mAlertCount = 0;
    }

    /**
     * Holder for async-task inputs that must be captured on the UI thread ahead
     * of running the task on the background thread.
     */
    public static class TaskRequest {
        // Collection name for a create/update, delete or copy collection task
        public String collectionName;
        // Import/export task inputs - file to read/write and format flags
        public Uri importExportFileUri;
        public boolean importExportLegacyCsv;
        public boolean exportSingleFileCsv;
        // True while an import, delete or copy task is writing the database, so
        // the UI thread knows not to read the collection list until it's done.
        // A read would either see a half-imported database or block behind the
        // task's transaction
        public boolean isWritingDatabase;
    }

    // Test seams, turned on by the Robolectric unit tests. There is no window
    // to show dialogs in there, and running tasks inline on the calling thread
    // lets a test see a task's result without waiting on a background thread
    public static boolean sSkipDialogs = false;
    public static boolean sRunTasksInline = false;

    // Async Task info
    protected ActivityViewModel mActivityViewModel = null;

    protected AsyncTaskRunner mTaskRunner = null;
    public static final int TASK_NONE = -1;
    public static final int TASK_OPEN_DATABASE = 0;
    public static final int TASK_IMPORT_COLLECTIONS = 1;
    public static final int TASK_CREATE_UPDATE_COLLECTION = 2;
    public static final int TASK_EXPORT_COLLECTIONS = 3;
    public static final int TASK_DELETE_COLLECTION = 4;
    public static final int TASK_COPY_COLLECTION = 5;

    // The dialog last shown under each tag, so an in-flight show or dismiss is
    // taken into account without executing pending fragment transactions
    private final Map<String, ShownDialog> mShownDialogs = new HashMap<>();

    /**
     * Record of the dialog last shown under a tag and whether this activity has
     * already dismissed it. Tracked here rather than asking the FragmentManager,
     * because a committed but not yet executed show or dismiss isn't reflected by
     * findFragmentByTag() - and executing pending transactions to settle it isn't
     * allowed from inside a fragment transaction
     */
    private static class ShownDialog {
        final DialogFragment mFragment;
        final boolean mDismissed;

        ShownDialog(DialogFragment fragment, boolean dismissed) {
            mFragment = fragment;
            mDismissed = dismissed;
        }
    }

    // Common activity variables
    protected final Context mContext = this;
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

        // Listen for results from the shared dialogs. Registering here (rather
        // than where the dialog is shown) means a dialog answered after this
        // activity is recreated still reaches the right handler
        registerDialogResultListener(REQUEST_KEY_BASE_ACTIVITY);

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
        }
        // Note: If a task (e.g. import/export) is already running when this activity
        // is (re)created, its progress UI is re-shown and any completed result is
        // delivered when the subclass calls setActivityReadyForAsyncCallbacks().
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
            case TASK_DELETE_COLLECTION: {
                createProgressDialog(mRes.getString(R.string.deleting_collection));
                break;
            }
            case TASK_COPY_COLLECTION: {
                createProgressDialog(mRes.getString(R.string.copying_collection));
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
        // The task is done, so its progress UI goes away regardless of outcome
        dismissProgressDialog();
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
        // Attaching either re-showed the progress UI for a still-running task or
        // delivered a result that dismissed it. If neither happened, any progress
        // dialog the FragmentManager restored belongs to a task that is long gone,
        // so drop it rather than leaving the user stuck behind a spinner
        if (mTaskRunner.getLatestTaskId() == TASK_NONE) {
            dismissProgressDialog();
        }
    }

    @Override
    public void onDestroy() {
        // Note: Dialogs are owned by the FragmentManager, so they are torn down
        // with the activity and restored with the recreated one - there is
        // nothing to dismiss here
        // If an async task is running, set the listener to null to have it wait before
        // trying its callback. Setting the listener to null also prevents memory leaks
        if (mTaskRunner != null) {
            mTaskRunner.clearListener();
            mTaskRunner = null;
        }
        super.onDestroy();
    }

    /**
     * Displays a message to the user
     *
     * @param text The text to be displayed
     */
    public void showCancelableAlert(String text) {
        // Each alert gets a tag of its own so that a second alert stacks on top of
        // the first instead of replacing it - two messages can be raised back to
        // back (e.g. an import error followed by a warning) and both must be seen
        String tag = TAG_ALERT_PREFIX + (mActivityViewModel.mAlertCount++);
        if (!showDialogFragment(MessageDialogFragment.newCancelableInstance(text), tag)) {
            // A task can finish while the app is in the background, and a
            // transaction can't be committed then. Hold the message rather than
            // dropping it, so the user still finds out what went wrong
            mActivityViewModel.mPendingAlertText.add(text);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // Now that fragment transactions are safe again, show anything that
        // couldn't be shown while the activity was stopped, in the order raised
        if (!mActivityViewModel.mPendingAlertText.isEmpty()) {
            ArrayList<String> pendingAlerts = new ArrayList<>(mActivityViewModel.mPendingAlertText);
            mActivityViewModel.mPendingAlertText.clear();
            for (String pendingAlertText : pendingAlerts) {
                showCancelableAlert(pendingAlertText);
            }
        }
    }

    /**
     * Create a new progress dialog, or update the message on the one already
     * shown. Reusing an existing dialog keeps the progress UI stable when a
     * still-running task re-reports itself after the activity is recreated
     *
     * @param message message to display alongside the spinner
     */
    protected void createProgressDialog(String message) {
        if (sSkipDialogs) {
            return;
        }
        // Reuse the progress dialog already showing under this tag, whether it
        // was shown by this activity or restored by the FragmentManager. A dialog
        // this activity has already dismissed is not reused, even if its removal
        // hasn't been executed yet, so the user isn't left without a spinner
        DialogFragment existing = getShownDialogFragment(TAG_PROGRESS);
        if (existing instanceof ProgressDialogFragment) {
            ((ProgressDialogFragment) existing).setMessage(message);
            return;
        }
        showDialogFragment(ProgressDialogFragment.newInstance(message), TAG_PROGRESS);
    }

    /**
     * Hides the progress dialog
     */
    protected void dismissProgressDialog() {
        // Covers both a dialog shown by this activity and one the FragmentManager
        // restored for a task that was running before a configuration change
        DialogFragment existing = getShownDialogFragment(TAG_PROGRESS);
        if (existing != null) {
            // The task can finish after the activity has saved its state, so
            // the dismissal must tolerate state loss
            existing.dismissAllowingStateLoss();
            mShownDialogs.put(TAG_PROGRESS, new ShownDialog(existing, true));
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
            // Only one tip is shown at a time. Keep the one already up (e.g. the
            // one the FragmentManager restored after a configuration change, or an
            // earlier tip the user hasn't acknowledged yet) rather than replacing
            // it - replacing it would leave the first tip's preference set, so it
            // would pop up again later
            if (getShownDialogFragment(TAG_HELP) != null) {
                return true;
            }
            // The preference is cleared once the user acknowledges the tip,
            // which is reported back through onDialogResult()
            Bundle payload = new Bundle();
            payload.putString(PAYLOAD_HELP_KEY, helpStrKey);
            showDialogFragment(MessageDialogFragment.newAcknowledgeInstance(
                    REQUEST_KEY_BASE_ACTIVITY, REQUEST_HELP_DIALOG,
                    res.getString(helpStrId), R.string.okay_exp, payload), TAG_HELP);
            return true;
        }
        return false;
    }

    /**
     * Shows a dialog fragment, replacing any dialog already shown under the
     * same tag. The FragmentManager owns the dialog from here on, so it is
     * restored automatically if this activity is recreated
     *
     * @param fragment the dialog to show
     * @param tag      tag identifying this kind of dialog
     * @return false if the activity isn't in a state where a dialog can be
     *         shown, so the caller can decide whether to hold onto it
     */
    protected boolean showDialogFragment(DialogFragment fragment, String tag) {
        // Don't show dialogs in unit tests since there isn't a UI, and
        // it will spam the log with this: Invalid ID 0x00000000.
        if (sSkipDialogs) {
            return true;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        // A transaction can't be committed while the activity is stopped or has
        // saved its state, and there is nothing worth showing to an activity
        // that is going away
        if (isFinishing() || fragmentManager.isStateSaved()) {
            return false;
        }
        // Each alert is shown under a tag of its own, so drop the records of
        // dialogs that are fully gone rather than holding onto them for as long
        // as this activity lives
        forgetFinishedDialogs();
        // Take down whatever is showing under this tag. The lookup goes through
        // the remembered dialogs instead of executing pending fragment
        // transactions: this can be called from inside a FragmentManager
        // transaction (e.g. from a fragment's onCreateView), and executing
        // transactions from there throws
        DialogFragment existing = getShownDialogFragment(tag);
        if (existing != null) {
            existing.dismissAllowingStateLoss();
        }
        fragment.show(fragmentManager, tag);
        mShownDialogs.put(tag, new ShownDialog(fragment, false));
        return true;
    }

    /**
     * Forgets the records of dialogs the FragmentManager has finished tearing
     * down. A dialog that is merely dismissed is kept, since its record is what
     * tells a later lookup not to reuse it while its removal is still pending
     */
    private void forgetFinishedDialogs() {
        Iterator<Map.Entry<String, ShownDialog>> iterator = mShownDialogs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ShownDialog> entry = iterator.next();
            if (!isDialogStillUnderTag(entry.getValue().mFragment, entry.getKey())) {
                iterator.remove();
            }
        }
    }

    /**
     * Reports whether a dialog fragment is still the one held under a tag. The
     * tag is set when the show transaction is built and cleared again when the
     * FragmentManager tears the fragment down (it resets a destroyed fragment so
     * the instance can be reused), so it covers a show that hasn't been executed
     * yet while not being fooled by a dialog that has already gone away. The
     * fragment's lifecycle state can't be used for this - a destroyed dialog is
     * reset all the way back to INITIALIZED, which is indistinguishable from a
     * dialog that is only waiting for its transaction to run
     *
     * @param fragment the dialog fragment to check
     * @param tag      tag it was shown under
     * @return true if the fragment is still held under the tag
     */
    private static boolean isDialogStillUnderTag(Fragment fragment, String tag) {
        return tag.equals(fragment.getTag());
    }

    /**
     * Returns the dialog currently showing under a tag, whether it was shown by
     * this activity (including a show that hasn't been executed yet) or restored
     * by the FragmentManager after this activity was recreated
     *
     * @param tag tag identifying this kind of dialog
     * @return the dialog fragment, or null if no dialog is showing under the tag
     */
    private DialogFragment getShownDialogFragment(String tag) {
        ShownDialog shown = mShownDialogs.get(tag);
        if (shown != null) {
            // What this activity did with the tag last is authoritative, so a
            // dialog it dismissed isn't reported even if the FragmentManager can
            // still find the dying fragment by tag
            if (!shown.mDismissed && !shown.mFragment.isRemoving()
                    && isDialogStillUnderTag(shown.mFragment, tag)) {
                return shown.mFragment;
            }
            // The dialog is gone (the user dismissed it, or it was torn down),
            // so drop the record and report that nothing is showing
            mShownDialogs.remove(tag);
            return null;
        }
        // Nothing was shown under this tag by this instance of the activity, so
        // look for one the FragmentManager restored
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        return (existing instanceof DialogFragment) ? (DialogFragment) existing : null;
    }

    /**
     * Registers a listener for results delivered by the shared dialog
     * fragments. The listener is scoped to this activity's lifecycle, so it is
     * re-established automatically after a configuration change and a dialog
     * answered afterwards is still handled
     *
     * @param requestKey the request key the dialogs report back on
     */
    protected void registerDialogResultListener(String requestKey) {
        getSupportFragmentManager().setFragmentResultListener(requestKey, this,
                (key, result) -> onDialogResult(result.getInt(KEY_REQUEST_ID, REQUEST_NONE), result));
    }

    /**
     * Handles a result reported by one of the shared dialog fragments.
     * Subclasses should handle their own request ids and defer to this for any
     * they don't recognize
     *
     * @param requestId identifies which dialog reported the result
     * @param result    the result values, including any echoed-back payload
     */
    protected void onDialogResult(int requestId, Bundle result) {
        if (requestId == REQUEST_HELP_DIALOG) {
            Bundle payload = result.getBundle(KEY_PAYLOAD);
            String helpStrKey = (payload != null) ? payload.getString(PAYLOAD_HELP_KEY) : null;
            if (helpStrKey != null) {
                // The user has seen this tip, so don't show it again
                SharedPreferences.Editor editor =
                        getSharedPreferences(MainApplication.PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(helpStrKey, false);
                editor.apply();
            }
        }
    }

    /**
     * Create and kick-off an async task to finish long-running tasks
     *
     * @param taskId type of task
     */
    public void kickOffAsyncTaskRunner(int taskId) {
        if (sRunTasksInline) {
            // Run the task to completion on the current thread (used for unit tests)
            asyncProgressOnPreExecute(taskId);
            String resultStr = asyncProgressDoInBackground(taskId);
            asyncProgressOnPostExecute(taskId, resultStr);
            return;
        }
        mTaskRunner.execute(taskId);
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
