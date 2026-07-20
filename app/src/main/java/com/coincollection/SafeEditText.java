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

import static com.spencerpages.MainApplication.APP_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * An EditText that tolerates a framework race in touch-driven selection.
 * <p>
 * When an EditText is hosted in a recycling ListView (the advanced collection
 * view), the adapter can replace the text between a touch-down and touch-up
 * (e.g. notifyDataSetChanged() rebinding the row when a fling settles). The
 * selection offsets ArrowKeyMovementMethod computes at touch-up are then -1:
 * {@code buffer.getSpanStart(LAST_TAP_DOWN)} returns -1 because the span set
 * at touch-down died with the replaced buffer, and
 * {@code getOffsetForPosition()} returns -1 while the text layout is null
 * before the next layout pass. Either way the movement method calls
 * {@code Selection.setSelection()} with a -1 bound, which throws
 * IndexOutOfBoundsException ("setSpan (-1 ... -1) starts before 0"). The
 * getSpanStart() source is internal to the movement method, so the offsets
 * can't be sanitized from the widget; catching the exception (and just not
 * moving the cursor for that racy tap) is the guard. The same selection can
 * be driven from a posted long-press runnable (View.performLongClick), which
 * runs outside onTouchEvent, so both entry points are guarded here.
 */
public class SafeEditText extends AppCompatEditText {

    public SafeEditText(Context context) {
        super(context);
    }

    public SafeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (IndexOutOfBoundsException e) {
            // The tap raced with a text rebind - skip the cursor move but log
            // the exception so real defects still show up in bug reports
            Log.e(APP_NAME, "Ignoring selection error from touch on notes field", e);
            return true;
        }
    }

    @Override
    public boolean performLongClick() {
        try {
            return super.performLongClick();
        } catch (IndexOutOfBoundsException e) {
            // The long-press word selection raced with a text rebind - skip it
            // but log the exception so real defects still show up in bug reports
            Log.e(APP_NAME, "Ignoring selection error from long press on notes field", e);
            return true;
        }
    }

    @Override
    public boolean performLongClick(float x, float y) {
        try {
            return super.performLongClick(x, y);
        } catch (IndexOutOfBoundsException e) {
            // The long-press word selection raced with a text rebind - skip it
            // but log the exception so real defects still show up in bug reports
            Log.e(APP_NAME, "Ignoring selection error from long press on notes field", e);
            return true;
        }
    }
}
