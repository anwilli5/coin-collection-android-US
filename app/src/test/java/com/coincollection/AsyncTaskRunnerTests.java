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

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.Executor;

/**
 * Unit tests for {@link AsyncTaskRunner}, focusing on the pending-result model,
 * progress re-show on re-attach, and per-instance task id isolation. The runner
 * is driven with an injected executor + main-thread handler so the background
 * work and the main looper can be stepped deterministically.
 */
@RunWith(RobolectricTestRunner.class)
public class AsyncTaskRunnerTests {

    /**
     * Records the callbacks delivered by the runner and supplies a fixed
     * background result.
     */
    private static class RecordingListener implements AsyncProgressInterface {
        int preCount = 0;
        int postCount = 0;
        int lastPostTaskId = BaseActivity.TASK_NONE;
        String lastPostResult = null;
        final String backgroundResult;

        RecordingListener(String backgroundResult) {
            this.backgroundResult = backgroundResult;
        }

        @Override
        public String asyncProgressDoInBackground(int taskId) {
            return backgroundResult;
        }

        @Override
        public void asyncProgressOnPreExecute(int taskId) {
            preCount++;
        }

        @Override
        public void asyncProgressOnPostExecute(int taskId, String resultStr) {
            postCount++;
            lastPostTaskId = taskId;
            lastPostResult = resultStr;
        }
    }

    /**
     * Executor that stores the submitted work so the test can run it on demand,
     * allowing a listener swap to be simulated between task start and completion.
     */
    private static class ManualExecutor implements Executor {
        private Runnable mPending;

        @Override
        public void execute(Runnable command) {
            mPending = command;
        }

        void runPending() {
            Runnable command = mPending;
            mPending = null;
            if (command != null) {
                command.run();
            }
        }
    }

    private static Handler mainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    private static void idleMainLooper() {
        shadowOf(Looper.getMainLooper()).idle();
    }

    /**
     * A task that completes while no listener is attached must deliver its real
     * result to the next listener that attaches - exactly once.
     */
    @Test
    public void resultDeliveredToListenerAttachedAfterCompletion() {
        RecordingListener starter = new RecordingListener("import-done");
        ManualExecutor executor = new ManualExecutor();
        AsyncTaskRunner runner = new AsyncTaskRunner(starter, executor, mainHandler());

        runner.execute(BaseActivity.TASK_IMPORT_COLLECTIONS);
        idleMainLooper();
        assertEquals(1, starter.preCount);

        // Simulate a configuration change: detach the current listener before the
        // background work completes
        runner.clearListener();
        executor.runPending();
        idleMainLooper();

        // The detached starter must not receive the result
        assertEquals(0, starter.postCount);

        // The next listener to attach receives the pending result exactly once
        RecordingListener next = new RecordingListener("ignored");
        runner.setListener(next);
        assertEquals(1, next.postCount);
        assertEquals("import-done", next.lastPostResult);
        assertEquals(BaseActivity.TASK_IMPORT_COLLECTIONS, next.lastPostTaskId);
    }

    /**
     * When a listener attaches while a task is still running, its progress UI is
     * re-shown (pre-execute), and it later receives the completed result.
     */
    @Test
    public void progressReshownWhenListenerAttachesDuringRunningTask() {
        RecordingListener starter = new RecordingListener("export-done");
        ManualExecutor executor = new ManualExecutor();
        AsyncTaskRunner runner = new AsyncTaskRunner(starter, executor, mainHandler());

        runner.execute(BaseActivity.TASK_EXPORT_COLLECTIONS);
        idleMainLooper();
        assertEquals(1, starter.preCount);

        // Rotate: detach and attach a fresh listener while the task is still running
        runner.clearListener();
        RecordingListener next = new RecordingListener("ignored");
        runner.setListener(next);

        // Progress is re-shown on the new listener, and no result yet
        assertEquals(1, next.preCount);
        assertEquals(0, next.postCount);

        // Completing the task delivers the real result to the attached listener
        executor.runPending();
        idleMainLooper();
        assertEquals(1, next.postCount);
        assertEquals("export-done", next.lastPostResult);
    }

    /**
     * The latest task id is tracked per runner instance, so a task in one
     * activity's runner does not surface in another's.
     */
    @Test
    public void taskIdIsTrackedPerInstance() {
        RecordingListener l1 = new RecordingListener("");
        RecordingListener l2 = new RecordingListener("");
        AsyncTaskRunner r1 = new AsyncTaskRunner(l1, new ManualExecutor(), mainHandler());
        AsyncTaskRunner r2 = new AsyncTaskRunner(l2, new ManualExecutor(), mainHandler());

        r1.execute(BaseActivity.TASK_IMPORT_COLLECTIONS);
        idleMainLooper();

        assertEquals(BaseActivity.TASK_IMPORT_COLLECTIONS, r1.getLatestTaskId());
        assertEquals(BaseActivity.TASK_NONE, r2.getLatestTaskId());
    }

    /**
     * Happy path: with a listener attached throughout, the task runs and delivers
     * its result, and the latest task id is cleared on completion.
     */
    @Test
    public void resultDeliveredWhenListenerAttachedThroughout() {
        RecordingListener listener = new RecordingListener("ok");
        AsyncTaskRunner runner = new AsyncTaskRunner(listener, Runnable::run, mainHandler());

        runner.execute(BaseActivity.TASK_EXPORT_COLLECTIONS);
        idleMainLooper();

        assertEquals(1, listener.preCount);
        assertEquals(1, listener.postCount);
        assertEquals("ok", listener.lastPostResult);
        assertEquals(BaseActivity.TASK_NONE, runner.getLatestTaskId());
    }
}
