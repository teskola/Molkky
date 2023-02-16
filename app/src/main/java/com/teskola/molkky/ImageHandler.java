package com.teskola.molkky;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;

public class ImageHandler {
    private Context context;
    public static final int TITLE_BAR = 0;

    public ImageHandler(Context context) {
        this.context = context;
    };
    public void BitmapToJpg (Bitmap photo, String name) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(name + ".jpg", Context.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getImagePath (String name) {
        File file = new File(context.getFilesDir(), name + ".jpg");
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }

    public void takePicture(int position) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 0);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((Activity) context).startActivityForResult(intent, position);
        }
    }

    public boolean findAndDeleteImage (String newPlayerName) {
        String[] files = context.fileList();
        for (String file : files) {
            if (file.equals(newPlayerName + ".jpg")) {
                context.deleteFile(file);
                return true;
            }
        }
        return false;
    }


}

