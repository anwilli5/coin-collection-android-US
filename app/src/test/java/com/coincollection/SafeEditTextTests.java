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
import static org.junit.Assert.assertTrue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.spencerpages.BaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

/**
 * Regression tests for {@link SafeEditText}, which guards against the
 * "setSpan (-1 ... -1) starts before 0" IndexOutOfBoundsException thrown by
 * ArrowKeyMovementMethod when the text is rebound mid-gesture in the advanced
 * collection view's recycling ListView.
 */
@RunWith(RobolectricTestRunner.class)
public class SafeEditTextTests extends BaseTestCase {

    /**
     * Reproduces the production crash: the notes EditText's text is replaced
     * between touch-down and touch-up (as happens when the recycling ListView
     * rebinds the row, e.g. via notifyDataSetChanged() when a fling settles).
     * The LAST_TAP_DOWN span that ArrowKeyMovementMethod set at touch-down
     * died with the replaced buffer, so at touch-up getSpanStart() returns -1
     * and the framework calls Selection.setSelection() with a -1 bound, which
     * throws IndexOutOfBoundsException. SafeEditText must swallow it instead
     * of crashing the app.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Test
    public void test_touchToleratesTextRebindMidGesture() {
        Context context = ApplicationProvider.getApplicationContext();
        SafeEditText editText = new SafeEditText(context);
        editText.setText("test notes");

        // Give the EditText a parent (the movement method calls
        // getParent().requestDisallowInterceptTouchEvent()) and a layout
        FrameLayout parent = new FrameLayout(context);
        parent.addView(editText);
        layOut(parent);

        // Focus the view before touching it, otherwise the movement method
        // returns early via didTouchFocusSelect(). The plain application
        // context doesn't resolve the EditText style's focusableInTouchMode,
        // so re-enable it explicitly
        editText.setFocusableInTouchMode(true);
        assertTrue(editText.requestFocus());

        // Put the buffer into selection mode (like holding shift) so that
        // ArrowKeyMovementMethod runs its touch-selection branch, which is
        // where the vulnerable getSpanStart()/setSelection() sequence lives
        pressShift(editText);

        long downTime = SystemClock.uptimeMillis();
        MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, 10f, 10f, 0);
        try {
            // Touch-down: ArrowKeyMovementMethod marks the tap position with
            // its LAST_TAP_DOWN span on the current text buffer
            editText.onTouchEvent(downEvent);

            // The recycling ListView rebinds the row mid-gesture: the buffer
            // (and the LAST_TAP_DOWN span with it) is replaced, then the next
            // layout pass runs. Selection mode is re-applied since the fresh
            // buffer starts without it.
            editText.setText("recycled row text");
            layOut(parent);
            pressShift(editText);

            // Touch-up: getSpanStart(LAST_TAP_DOWN) is now -1, so the real
            // ArrowKeyMovementMethod calls Selection.setSelection() with a -1
            // bound. Without the guard in SafeEditText.onTouchEvent this
            // throws IndexOutOfBoundsException ("starts before 0")
            assertTrue(editText.onTouchEvent(upEvent));
        } finally {
            downEvent.recycle();
            upEvent.recycle();
        }

        // Guard against the test passing vacuously (e.g. the movement method
        // never reaching the vulnerable branch): the framework exception must
        // actually have been thrown, caught, and logged
        boolean loggedSelectionError = false;
        for (ShadowLog.LogItem logItem : ShadowLog.getLogs()) {
            if (logItem.throwable instanceof IndexOutOfBoundsException) {
                loggedSelectionError = true;
                break;
            }
        }
        assertTrue("SafeEditText should have caught and logged the selection error",
                loggedSelectionError);
    }

    /**
     * The same selection failure can surface from a long press, which runs the
     * word-selection code from a posted CheckForLongPress runnable
     * (View.performLongClick -> Editor.performLongClick -> Selection.setSelection)
     * outside onTouchEvent. That framework path isn't reachable through
     * Robolectric's Editor, so the throw is injected here from a long-click
     * listener to stand in for the setSelection(-1) failure. SafeEditText's
     * performLongClick override must swallow it (returning true as "handled")
     * instead of letting it crash the app - the test throws out uncaught if
     * that try/catch is removed.
     */
    @Test
    public void test_longPressToleratesSelectionError() {
        Context context = ApplicationProvider.getApplicationContext();
        SafeEditText editText = new SafeEditText(context);
        editText.setText("test notes");

        FrameLayout parent = new FrameLayout(context);
        parent.addView(editText);
        layOut(parent);

        // Reproduce the selection failure the long-press word selection would
        // throw (Selection.setSelection with a -1 bound) from within
        // super.performLongClick()
        editText.setOnLongClickListener(v -> {
            throw new IndexOutOfBoundsException("setSpan (-1 ... -1) starts before 0");
        });

        assertTrue(editText.performLongClick());
        assertTrue(editText.performLongClick(10f, 10f));

        // The framework exception must actually have been thrown, caught, and
        // logged rather than swallowed silently
        boolean loggedSelectionError = false;
        for (ShadowLog.LogItem logItem : ShadowLog.getLogs()) {
            if (logItem.throwable instanceof IndexOutOfBoundsException) {
                loggedSelectionError = true;
                break;
            }
        }
        assertTrue("SafeEditText should have caught and logged the selection error",
                loggedSelectionError);
    }

    /**
     * Sets the shift-key meta state on the EditText's current text buffer so
     * ArrowKeyMovementMethod.isSelecting() returns true
     */
    private static void pressShift(SafeEditText editText) {
        long now = SystemClock.uptimeMillis();
        editText.onKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT, new KeyEvent(now, now,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0));
        assertEquals("Text buffer should be in selection mode", 1,
                MetaKeyKeyListener.getMetaState(editText.getText(),
                        MetaKeyKeyListener.META_SHIFT_ON));
    }

    /**
     * Measures and lays out the view hierarchy so the EditText has a non-null
     * text layout and TextView dispatches touches to the movement method
     */
    private static void layOut(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, 200, 100);
    }
}
