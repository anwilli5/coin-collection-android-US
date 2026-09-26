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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Tests for how {@link MainActivity} resolves the name and format of a
 * document picked for import or export. Not every document provider reports a
 * display name, and the name may lack an extension, so a CSV file used to be
 * imported as JSON (or the lookup crashed) depending on the provider.
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityFileNameTests {

    private static final String AUTHORITY = "com.coincollection.test.documents";
    private static final Uri DOCUMENT_URI =
            Uri.parse("content://" + AUTHORITY + "/document/primary%3ADownload%2Fexport.csv");

    private FakeDocumentProvider mProvider;

    /**
     * A document provider whose query result and MIME type each test sets up
     */
    public static class FakeDocumentProvider extends ContentProvider {
        Cursor mCursor;
        String mType;

        @Override
        public boolean onCreate() {
            return true;
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
            return mCursor;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return mType;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                          @Nullable String[] selectionArgs) {
            throw new UnsupportedOperationException();
        }
    }

    @Before
    public void setUp() {
        mProvider = Robolectric.setupContentProvider(FakeDocumentProvider.class, AUTHORITY);
    }

    private String resolveFileName() {
        return MainActivity.getFileNameFromUri(
                ApplicationProvider.getApplicationContext().getContentResolver(), DOCUMENT_URI);
    }

    @Test
    public void getFileNameFromUri_usesDisplayName() {
        MatrixCursor cursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME});
        cursor.addRow(new Object[]{"my-collections.csv"});
        mProvider.mCursor = cursor;
        assertEquals("my-collections.csv", resolveFileName());
    }

    @Test
    public void getFileNameFromUri_fallsBackWithoutDisplayNameColumn() {
        MatrixCursor cursor = new MatrixCursor(new String[]{OpenableColumns.SIZE});
        cursor.addRow(new Object[]{100});
        mProvider.mCursor = cursor;
        assertEquals("primary:Download/export.csv", resolveFileName());
        assertTrue("Cursor should be closed", cursor.isClosed());
    }

    @Test
    public void getFileNameFromUri_fallsBackForEmptyCursor() {
        mProvider.mCursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME});
        assertEquals("primary:Download/export.csv", resolveFileName());
    }

    @Test
    public void getFileNameFromUri_fallsBackForNullCursor() {
        mProvider.mCursor = null;
        assertEquals("primary:Download/export.csv", resolveFileName());
    }

    @Test
    public void isCsvFile_prefersMimeType() {
        // A CSV MIME type wins even when the name has no extension
        assertTrue(MainActivity.isCsvFile("export", "text/csv"));
        assertTrue(MainActivity.isCsvFile("export", "text/comma-separated-values"));
        assertTrue(MainActivity.isCsvFile("export", "TEXT/CSV"));
        // A JSON MIME type wins over a misleading extension
        assertFalse(MainActivity.isCsvFile("export.csv", "application/json"));
    }

    @Test
    public void isCsvFile_fallsBackToExtension() {
        assertTrue(MainActivity.isCsvFile("export.csv", null));
        assertTrue(MainActivity.isCsvFile("EXPORT.CSV", "application/octet-stream"));
        assertFalse(MainActivity.isCsvFile("export.json", "application/octet-stream"));
        assertFalse(MainActivity.isCsvFile("", null));
    }
}
