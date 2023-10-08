package com.uet.fwork.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImagePicker {
    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private ComponentActivity activity;

    public ImagePicker() {
    }

    public void pickImage() {
        Intent intent = getPickImageIntent(this.activity);
        getImageActivityLauncher.launch(intent);
    }

    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempFile(context));

        PackageManager packageManager = context.getPackageManager();

        //  Lấy tất cả Intent cung cấp chức năng Camera
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setPackage(res.activityInfo.packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempFile(context));
            intentList.add(intent);
        }

        //  Lấy tất cả Intent cung cấp Ảnh
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setPackage(res.activityInfo.packageName);
            intentList.add(intent);
        }

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    "Chọn nguồn ảnh");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static Uri getTempFile(Context context) {
        File path = new File(context.getExternalCacheDir(), "camera");
        if (!path.exists()) path.mkdirs();
        File image = new File(path, "image_camera");
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", image);
    }

    public static Bitmap getImageFromResult(Context context, ActivityResult result) {
        Uri imageFile = getTempFile(context);
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();

            if (data != null) {
                boolean isCamera =
                        data.getAction() != null
                                && data.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE);
                System.out.println(isCamera);
                if (!isCamera && data.getData() != null) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap
                                = MediaStore.Images.Media.getBitmap(
                                context.getContentResolver(),
                                imageUri);
                        return bitmap;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (isCamera) {
                    try {
                        Uri imageUri = imageFile;
                        Bitmap bitmap
                                = MediaStore.Images.Media.getBitmap(
                                context.getContentResolver(),
                                imageUri);
                        return bitmap;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Uri imageUri = imageFile;
                    Bitmap bitmap
                            = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(),
                            imageUri);
                    return bitmap;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
