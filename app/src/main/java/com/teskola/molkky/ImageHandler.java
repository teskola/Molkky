package com.teskola.molkky;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;

public class ImageHandler {
    private final Context context;
    public static final int TITLE_BAR = 0;

    public ImageHandler(Context context) {
        this.context = context;
    }

    /*
    * Saves image to jpg and adds to Gallery
    * */

    public void BitmapToJpg (Bitmap photo, String name) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(name + ".jpg", Context.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getImagePath (String id) {
        File file = new File(context.getFilesDir(), id + ".jpg");
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

    public void addPictureToGallery (String name) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(getImagePath(name));
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
    }


}

