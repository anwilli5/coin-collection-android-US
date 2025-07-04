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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class CoinImageUtilsTest {

    private Context context;
    private File tempImageDir;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        tempImageDir = CoinImageUtils.getCoinImagesDirectory(context);
        
        // Clean up any existing test files
        if (tempImageDir.exists()) {
            deleteDirectory(tempImageDir);
        }
        tempImageDir.mkdirs();
    }

    @After
    public void tearDown() {
        // Clean up test files
        if (tempImageDir != null && tempImageDir.exists()) {
            deleteDirectory(tempImageDir);
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }

    private Bitmap createTestBitmap(int width, int height) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    @Test
    public void testGetCoinImagesDirectory() {
        File imagesDir = CoinImageUtils.getCoinImagesDirectory(context);
        
        assertNotNull("Images directory should not be null", imagesDir);
        assertTrue("Images directory should exist", imagesDir.exists());
        assertTrue("Images directory should be a directory", imagesDir.isDirectory());
        assertEquals("Directory name should be coin_images", "coin_images", imagesDir.getName());
    }

    @Test
    public void testGenerateUniqueImageFilename() {
        String filename1 = CoinImageUtils.generateUniqueImageFilename();
        String filename2 = CoinImageUtils.generateUniqueImageFilename();
        
        assertNotNull("First filename should not be null", filename1);
        assertNotNull("Second filename should not be null", filename2);
        assertFalse("Filenames should be unique", filename1.equals(filename2));
        assertTrue("Filename should start with 'coin_'", filename1.startsWith("coin_"));
        assertTrue("Filename should end with '.jpg'", filename1.endsWith(".jpg"));
    }

    @Test
    public void testSaveCoinImage_ValidBitmap() {
        Bitmap testBitmap = createTestBitmap(100, 100);
        
        String filename = CoinImageUtils.saveCoinImage(context, testBitmap);
        
        assertNotNull("Filename should not be null", filename);
        assertTrue("Filename should start with 'coin_'", filename.startsWith("coin_"));
        assertTrue("Filename should end with '.jpg'", filename.endsWith(".jpg"));
        
        // Verify file was actually created
        File savedFile = new File(tempImageDir, filename);
        assertTrue("Saved file should exist", savedFile.exists());
        assertTrue("Saved file should have content", savedFile.length() > 0);
    }

    @Test
    public void testSaveCoinImage_NullBitmap() {
        String filename = CoinImageUtils.saveCoinImage(context, null);
        
        assertNull("Filename should be null for null bitmap", filename);
    }

    @Test
    public void testLoadCoinImage_ExistingFile() throws IOException {
        // Create a test image file
        Bitmap originalBitmap = createTestBitmap(200, 200);
        String filename = CoinImageUtils.saveCoinImage(context, originalBitmap);
        
        // Load the image
        Bitmap loadedBitmap = CoinImageUtils.loadCoinImage(context, filename);
        
        assertNotNull("Loaded bitmap should not be null", loadedBitmap);
        assertEquals("Loaded bitmap width should match saved size", 200, loadedBitmap.getWidth());
        assertEquals("Loaded bitmap height should match saved size", 200, loadedBitmap.getHeight());
    }

    @Test
    public void testLoadCoinImage_NonExistentFile() {
        Bitmap loadedBitmap = CoinImageUtils.loadCoinImage(context, "nonexistent.jpg");
        
        assertNull("Loaded bitmap should be null for non-existent file", loadedBitmap);
    }

    @Test
    public void testLoadCoinImage_NullFilename() {
        Bitmap loadedBitmap = CoinImageUtils.loadCoinImage(context, null);
        
        assertNull("Loaded bitmap should be null for null filename", loadedBitmap);
    }

    @Test
    public void testLoadCoinImage_EmptyFilename() {
        Bitmap loadedBitmap = CoinImageUtils.loadCoinImage(context, "");
        
        assertNull("Loaded bitmap should be null for empty filename", loadedBitmap);
    }

    @Test
    public void testDeleteCoinImage_ExistingFile() {
        // Create a test image file
        Bitmap testBitmap = createTestBitmap(100, 100);
        String filename = CoinImageUtils.saveCoinImage(context, testBitmap);
        
        // Verify file exists
        File savedFile = new File(tempImageDir, filename);
        assertTrue("File should exist before deletion", savedFile.exists());
        
        // Delete the file
        boolean deleted = CoinImageUtils.deleteCoinImage(context, filename);
        
        assertTrue("Deletion should return true", deleted);
        assertFalse("File should not exist after deletion", savedFile.exists());
    }

    @Test
    public void testDeleteCoinImage_NonExistentFile() {
        boolean deleted = CoinImageUtils.deleteCoinImage(context, "nonexistent.jpg");
        
        assertFalse("Deletion should return false for non-existent file", deleted);
    }

    @Test
    public void testDeleteCoinImage_NullFilename() {
        boolean deleted = CoinImageUtils.deleteCoinImage(context, null);
        
        assertTrue("Deletion should return true for null filename (nothing to delete)", deleted);
    }

    @Test
    public void testDeleteCoinImage_EmptyFilename() {
        boolean deleted = CoinImageUtils.deleteCoinImage(context, "");
        
        assertTrue("Deletion should return true for empty filename (nothing to delete)", deleted);
    }

    @Test
    public void testCreateCircularCoinImage_ValidBitmap() {
        Bitmap sourceBitmap = createTestBitmap(300, 300);
        
        Bitmap circularBitmap = CoinImageUtils.createCircularCoinImage(sourceBitmap);
        
        assertNotNull("Circular bitmap should not be null", circularBitmap);
        assertEquals("Circular bitmap width should be 200", 200, circularBitmap.getWidth());
        assertEquals("Circular bitmap height should be 200", 200, circularBitmap.getHeight());
        assertEquals("Circular bitmap should be ARGB_8888", Bitmap.Config.ARGB_8888, circularBitmap.getConfig());
    }

    @Test
    public void testCreateCircularCoinImage_NullBitmap() {
        Bitmap circularBitmap = CoinImageUtils.createCircularCoinImage(null);
        
        assertNull("Circular bitmap should be null for null input", circularBitmap);
    }

    @Test
    public void testCreateCircularCoinImage_NonSquareBitmap() {
        Bitmap sourceBitmap = createTestBitmap(400, 200);
        
        Bitmap circularBitmap = CoinImageUtils.createCircularCoinImage(sourceBitmap);
        
        assertNotNull("Circular bitmap should not be null", circularBitmap);
        assertEquals("Circular bitmap should be square", 200, circularBitmap.getWidth());
        assertEquals("Circular bitmap should be square", 200, circularBitmap.getHeight());
    }

    @Test
    public void testLoadBitmapFromUri_ValidUri() throws IOException {
        // Create a test bitmap and save it to a temporary file
        Bitmap originalBitmap = createTestBitmap(400, 400);
        File tempFile = new File(context.getCacheDir(), "test_image.jpg");
        FileOutputStream out = new FileOutputStream(tempFile);
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        out.close();
        
        // Mock URI and ContentResolver
        Uri mockUri = Uri.fromFile(tempFile);
        Context mockContext = mock(Context.class);
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockContentResolver.openInputStream(eq(mockUri)))
            .thenAnswer(invocation -> new java.io.FileInputStream(tempFile))
            .thenAnswer(invocation -> new java.io.FileInputStream(tempFile));
        
        Bitmap loadedBitmap = CoinImageUtils.loadBitmapFromUri(mockContext, mockUri, 200);
        
        assertNotNull("Loaded bitmap should not be null", loadedBitmap);
        
        // Clean up
        tempFile.delete();
    }

    @Test
    public void testLoadBitmapFromUri_NullInputStream() throws Exception {
        Context mockContext = mock(Context.class);
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        Uri mockUri = mock(Uri.class);
        
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockContentResolver.openInputStream(any(Uri.class))).thenReturn(null);
        
        Bitmap loadedBitmap = CoinImageUtils.loadBitmapFromUri(mockContext, mockUri, 200);
        
        assertNull("Loaded bitmap should be null when InputStream is null", loadedBitmap);
    }

    @Test
    public void testGetAbsolutePath_ValidFilename() {
        String filename = "test_image.jpg";
        
        String absolutePath = CoinImageUtils.getAbsolutePath(context, filename);
        
        assertNotNull("Absolute path should not be null", absolutePath);
        assertTrue("Absolute path should contain filename", absolutePath.contains(filename));
        assertTrue("Absolute path should contain coin_images directory", absolutePath.contains("coin_images"));
    }

    @Test
    public void testGetAbsolutePath_NullFilename() {
        String absolutePath = CoinImageUtils.getAbsolutePath(context, null);
        
        assertNull("Absolute path should be null for null filename", absolutePath);
    }

    @Test
    public void testGetAbsolutePath_EmptyFilename() {
        String absolutePath = CoinImageUtils.getAbsolutePath(context, "");
        
        assertNull("Absolute path should be null for empty filename", absolutePath);
    }

    @Test
    public void testCoinImageExists_ExistingFile() {
        // Create a test image file
        Bitmap testBitmap = createTestBitmap(100, 100);
        String filename = CoinImageUtils.saveCoinImage(context, testBitmap);
        
        boolean exists = CoinImageUtils.coinImageExists(context, filename);
        
        assertTrue("File should exist", exists);
    }

    @Test
    public void testCoinImageExists_NonExistentFile() {
        boolean exists = CoinImageUtils.coinImageExists(context, "nonexistent.jpg");
        
        assertFalse("File should not exist", exists);
    }

    @Test
    public void testCoinImageExists_NullFilename() {
        boolean exists = CoinImageUtils.coinImageExists(context, null);
        
        assertFalse("File should not exist for null filename", exists);
    }

    @Test
    public void testCoinImageExists_EmptyFilename() {
        boolean exists = CoinImageUtils.coinImageExists(context, "");
        
        assertFalse("File should not exist for empty filename", exists);
    }

    @Test
    public void testImageProcessingWorkflow() {
        // Test the complete workflow: create, save, load, delete
        Bitmap originalBitmap = createTestBitmap(500, 300);
        
        // Save the image
        String filename = CoinImageUtils.saveCoinImage(context, originalBitmap);
        assertNotNull("Filename should not be null", filename);
        
        // Check if it exists
        assertTrue("Image should exist after saving", CoinImageUtils.coinImageExists(context, filename));
        
        // Load the image
        Bitmap loadedBitmap = CoinImageUtils.loadCoinImage(context, filename);
        assertNotNull("Loaded bitmap should not be null", loadedBitmap);
        assertEquals("Loaded image should be 200x200", 200, loadedBitmap.getWidth());
        assertEquals("Loaded image should be 200x200", 200, loadedBitmap.getHeight());
        
        // Get absolute path
        String absolutePath = CoinImageUtils.getAbsolutePath(context, filename);
        assertNotNull("Absolute path should not be null", absolutePath);
        assertTrue("File should exist at absolute path", new File(absolutePath).exists());
        
        // Delete the image
        boolean deleted = CoinImageUtils.deleteCoinImage(context, filename);
        assertTrue("Image should be deleted successfully", deleted);
        assertFalse("Image should not exist after deletion", CoinImageUtils.coinImageExists(context, filename));
    }
}
