package com.teskola.molkky;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public class ImageHandler {
    Context context;

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


}

