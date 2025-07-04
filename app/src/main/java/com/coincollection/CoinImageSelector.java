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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.spencerpages.R;

import java.io.File;
import java.io.IOException;

/**
 * Dialog and helper class for capturing or selecting coin images
 */
public class CoinImageSelector {
    
    private static final String TAG = "CoinImageSelector";
    private static final int CAMERA_IMAGE_SIZE = 800; // Size for camera images before processing
    public static final int REQUEST_CODE_CAMERA = 101;
    public static final int REQUEST_CODE_GALLERY = 102;
    
    public interface CoinImageCallback {
        void onImageSelected(Bitmap bitmap, String imagePath);
    }
    
    private final Activity mActivity;
    private final Fragment mFragment;
    private CoinImageCallback mCallback;
    private Uri mPhotoUri;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> mCameraLauncher;
    private ActivityResultLauncher<Intent> mGalleryLauncher;
    private ActivityResultLauncher<String> mPermissionLauncher;
    
    /**
     * Constructor for use with Activity
     */
    public CoinImageSelector(Activity activity) {
        mActivity = activity;
        mFragment = null;
        setupActivityResultLaunchers();
    }
    
    /**
     * Constructor for use with Fragment
     */
    public CoinImageSelector(Fragment fragment) {
        mActivity = fragment.getActivity();
        mFragment = fragment;
        setupActivityResultLaunchers();
    }
    
    private void setupActivityResultLaunchers() {
        if (mFragment != null) {
            // Fragment-based launchers
            mCameraLauncher = mFragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleCameraResult(result.getResultCode(), result.getData())
            );
            
            mGalleryLauncher = mFragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGalleryResult(result.getResultCode(), result.getData())
            );
            
            mPermissionLauncher = mFragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                this::handlePermissionResult
            );
        } else if (mActivity instanceof androidx.activity.ComponentActivity) {
            // Activity-based launchers (for newer activities that extend ComponentActivity)
            androidx.activity.ComponentActivity componentActivity = (androidx.activity.ComponentActivity) mActivity;
            
            mCameraLauncher = componentActivity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleCameraResult(result.getResultCode(), result.getData())
            );
            
            mGalleryLauncher = componentActivity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGalleryResult(result.getResultCode(), result.getData())
            );
            
            mPermissionLauncher = componentActivity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                this::handlePermissionResult
            );
        }
    }
    
    /**
     * Show image selection dialog
     */
    public void showImageSelectionDialog(CoinImageCallback callback) {
        mCallback = callback;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.select_coin_image_title);
        
        String[] options = {
            mActivity.getString(R.string.take_photo),
            mActivity.getString(R.string.choose_from_gallery),
            mActivity.getString(R.string.remove_custom_image)
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Take photo
                    checkCameraPermissionAndCapture();
                    break;
                case 1: // Choose from gallery
                    selectFromGallery();
                    break;
                case 2: // Remove custom image
                    removeCustomImage();
                    break;
            }
        });
        
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (mCallback != null) {
                // Do nothing on cancel
            }
        });
        
        builder.show();
    }
    
    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            capturePhoto();
        } else {
            // Request camera permission
            if (mPermissionLauncher != null) {
                mPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                // Fallback for older activities
                ActivityCompat.requestPermissions(mActivity, 
                    new String[]{Manifest.permission.CAMERA}, 
                    100);
            }
        }
    }
    
    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            
            // Create temporary file for the photo
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error occurred while creating the file", ex);
                if (mCallback != null) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.error_temp_file_creation), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            if (photoFile != null) {
                mPhotoUri = FileProvider.getUriForFile(mActivity,
                    mActivity.getPackageName() + ".fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                
                if (mCameraLauncher != null) {
                    mCameraLauncher.launch(takePictureIntent);
                } else {
                    // Fallback for older activities
                    mActivity.startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
                }
            }
        } else {
            if (mCallback != null) {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_no_camera_app), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void selectFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.setType("image/*");
        
        if (pickPhoto.resolveActivity(mActivity.getPackageManager()) != null) {
            if (mGalleryLauncher != null) {
                mGalleryLauncher.launch(pickPhoto);
            } else {
                // Fallback for older activities
                mActivity.startActivityForResult(pickPhoto, REQUEST_CODE_GALLERY);
            }
        } else {
            if (mCallback != null) {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_no_gallery_app), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void removeCustomImage() {
        if (mCallback != null) {
            mCallback.onImageSelected(null, null); // null indicates removal
        }
    }
    
    private File createImageFile() throws IOException {
        String imageFileName = "COIN_" + System.currentTimeMillis();
        File storageDir = mActivity.getExternalCacheDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    private void handleCameraResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (mPhotoUri != null) {
                // Load the captured image
                Bitmap bitmap = CoinImageUtils.loadBitmapFromUri(mActivity, mPhotoUri, CAMERA_IMAGE_SIZE);
                if (bitmap != null && mCallback != null) {
                    // Process the image (crop to circle and save)
                    Bitmap processedBitmap = CoinImageUtils.createCircularCoinImage(bitmap);
                    String imagePath = CoinImageUtils.saveCoinImage(mActivity, processedBitmap);
                    mCallback.onImageSelected(processedBitmap, imagePath);
                } else if (mCallback != null) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.error_loading_captured_photo), Toast.LENGTH_SHORT).show();
                }
            } else if (mCallback != null) {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_photo_uri_null), Toast.LENGTH_SHORT).show();
            }
        }
        // If canceled, do nothing
    }
    
    private void handleGalleryResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // Load the selected image
                Bitmap bitmap = CoinImageUtils.loadBitmapFromUri(mActivity, selectedImage, CAMERA_IMAGE_SIZE);
                if (bitmap != null && mCallback != null) {
                    // Process the image (crop to circle and save)
                    Bitmap processedBitmap = CoinImageUtils.createCircularCoinImage(bitmap);
                    String imagePath = CoinImageUtils.saveCoinImage(mActivity, processedBitmap);
                    mCallback.onImageSelected(processedBitmap, imagePath);
                } else if (mCallback != null) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.error_loading_selected_image), Toast.LENGTH_SHORT).show();
                }
            } else if (mCallback != null) {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_selected_image_uri_null), Toast.LENGTH_SHORT).show();
            }
        }
        // If canceled, do nothing
    }
    
    private void handlePermissionResult(boolean granted) {
        if (granted) {
            capturePhoto();
        } else {
            Toast.makeText(mActivity, mActivity.getString(R.string.error_camera_permission_required), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle permission request result for older activities
     * Call this from the activity's onRequestPermissionsResult method
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturePhoto();
            } else {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_camera_permission_required), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Handle activity result for older activities
     * Call this from the activity's onActivityResult method
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA: // Camera
                handleCameraResult(resultCode, data);
                break;
            case REQUEST_CODE_GALLERY: // Gallery
                handleGalleryResult(resultCode, data);
                break;
        }
    }
}
