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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

import com.spencerpages.MainApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Utility class for managing custom coin images
 */
public class CoinImageUtils {
    
    private static final String TAG = "CoinImageUtils";
    private static final String COIN_IMAGES_DIR = "coin_images";
    private static final int COIN_IMAGE_SIZE = 200; // Target size for processed images
    private static final int COIN_IMAGE_QUALITY = 85; // JPEG quality
    
    /**
     * Get the directory where custom coin images are stored
     */
    public static File getCoinImagesDirectory(Context context) {
        File imagesDir = new File(context.getFilesDir(), COIN_IMAGES_DIR);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        return imagesDir;
    }
    
    /**
     * Generate a unique filename for a coin image
     */
    public static String generateUniqueImageFilename() {
        return "coin_" + UUID.randomUUID().toString() + ".jpg";
    }
    
    /**
     * Save a bitmap as a circular cropped coin image
     * 
     * @param context The application context
     * @param bitmap The source bitmap to process
     * @return The relative path to the saved image, or null if failed
     */
    public static String saveCoinImage(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Cannot save null bitmap");
            return null;
        }
        
        try {
            // Create processed image
            Bitmap processedBitmap = createCircularCoinImage(bitmap);
            if (processedBitmap == null) {
                Log.e(TAG, "Failed to process bitmap into circular coin image");
                return null;
            }
            
            // Generate filename and save
            String filename = generateUniqueImageFilename();
            File imageFile = new File(getCoinImagesDirectory(context), filename);
            
            FileOutputStream out = new FileOutputStream(imageFile);
            processedBitmap.compress(Bitmap.CompressFormat.JPEG, COIN_IMAGE_QUALITY, out);
            out.close();
            
            // Clean up
            if (processedBitmap != bitmap) {
                processedBitmap.recycle();
            }
            
            Log.d(TAG, "Saved coin image: " + filename);
            return filename;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to save coin image", e);
            return null;
        }
    }
    
    /**
     * Load a coin image from internal storage
     * 
     * @param context The application context
     * @param filename The filename of the image to load
     * @return The loaded bitmap, or null if failed
     */
    public static Bitmap loadCoinImage(Context context, String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        
        try {
            File imageFile = new File(getCoinImagesDirectory(context), filename);
            if (!imageFile.exists()) {
                Log.w(TAG, "Coin image file not found: " + filename);
                return null;
            }
            
            FileInputStream in = new FileInputStream(imageFile);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            
            return bitmap;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to load coin image: " + filename, e);
            return null;
        }
    }
    
    /**
     * Delete a coin image from internal storage
     * 
     * @param context The application context
     * @param filename The filename of the image to delete
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteCoinImage(Context context, String filename) {
        if (filename == null || filename.isEmpty()) {
            return true; // Nothing to delete
        }
        
        try {
            File imageFile = new File(getCoinImagesDirectory(context), filename);
            boolean deleted = imageFile.delete();
            
            if (deleted) {
                Log.d(TAG, "Deleted coin image: " + filename);
            } else {
                Log.w(TAG, "Failed to delete coin image (may not exist): " + filename);
            }
            
            return deleted;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting coin image: " + filename, e);
            return false;
        }
    }
    
    /**
     * Create a circular coin image from a source bitmap
     * This scales the image to the target size and crops it to a circle
     * 
     * @param source The source bitmap
     * @return A circular bitmap scaled to COIN_IMAGE_SIZE
     */
    public static Bitmap createCircularCoinImage(Bitmap source) {
        if (source == null) {
            return null;
        }
        
        // Create a square bitmap at target size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, COIN_IMAGE_SIZE, COIN_IMAGE_SIZE, true);
        
        // Create circular bitmap
        Bitmap circularBitmap = Bitmap.createBitmap(COIN_IMAGE_SIZE, COIN_IMAGE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Draw circle mask
        canvas.drawCircle(COIN_IMAGE_SIZE / 2f, COIN_IMAGE_SIZE / 2f, COIN_IMAGE_SIZE / 2f, paint);
        
        // Apply source image with mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);
        
        // Clean up
        if (scaledBitmap != source) {
            scaledBitmap.recycle();
        }
        
        return circularBitmap;
    }
    
    /**
     * Load and decode a bitmap from a URI with proper scaling
     * 
     * @param context The application context
     * @param uri The URI of the image
     * @param targetSize The target size for the longest dimension
     * @return The decoded bitmap, or null if failed
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri uri, int targetSize) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return null;
            }
            
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize);
            
            // Decode bitmap with inSampleSize set
            input = context.getContentResolver().openInputStream(uri);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            
            return bitmap;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to load bitmap from URI", e);
            return null;
        }
    }
    
    /**
     * Calculate appropriate inSampleSize for bitmap loading
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * Get the absolute path for a coin image filename
     * 
     * @param context The application context
     * @param filename The relative filename
     * @return The absolute path, or null if filename is empty
     */
    public static String getAbsolutePath(Context context, String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        
        File imageFile = new File(getCoinImagesDirectory(context), filename);
        return imageFile.getAbsolutePath();
    }
    
    /**
     * Check if a coin image file exists
     * 
     * @param context The application context
     * @param filename The filename to check
     * @return true if the file exists, false otherwise
     */
    public static boolean coinImageExists(Context context, String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        File imageFile = new File(getCoinImagesDirectory(context), filename);
        return imageFile.exists();
    }
}
