package com.teskola.molkky;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ImageHandler {
    private static ImageHandler instance;
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = firebaseStorage.getReference();
    private final FirebaseManager firebaseManager;
    private final Map<String, Long> images;
    public static final String PATH = "/Mölkky";

    public interface OnImageAdded {
        void onSuccess();
        void onError();
    }

    public static ImageHandler getInstance(Context context) {
        if (instance == null)
            instance = new ImageHandler(context.getApplicationContext());
        return instance;
    }

    private ImageHandler(Context context) {
        firebaseManager = FirebaseManager.getInstance(context);
        ChildEventListener imagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String pid = snapshot.getKey();
                Long timestamp = snapshot.getValue(Long.class);
                images.put(pid, timestamp);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                images.put(snapshot.getKey(), snapshot.getValue(Long.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                images.remove(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        firebaseManager.registerImagesListener(imagesListener);
        images = new HashMap<>();
    }

    public StorageReference getStorageReference(String id) {
        return storageReference.child("images/" + id + ".jpg");
    }

    public boolean hasImage  (String id) {
        return images.containsKey(id);
    }

    public Long getTimestamp (String id) {
        return images.get(id);
    }

    /*
    *
    *  Saves image to local storage
    *
    * */

    public void save (Context context, Bitmap photo, String id, String name) throws IOException {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(id + ".jpg", Context.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveToExternal(context, photo, name);
    }

    public void upload (Bitmap photo, String id, OnImageAdded listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();
        StorageReference imageRef = storageReference.child("images/" + id + ".jpg");
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("uid", FirebaseAuth.getInstance().getUid())
                .build();
        imageRef.putBytes(data, metadata).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseManager.addImage(id, s -> {
                    if (listener != null)
                        listener.onSuccess();
                }, e -> {
                    if (listener != null)
                        listener.onError();
                });
            }
            else {
                if (listener != null)
                    listener.onError();
            }
        });
    }

    // https://stackoverflow.com/questions/63243403/android-picture-was-not-added-to-gallery-but-onscancompletedlistener-is-called

   private void saveToExternal (Context context, Bitmap bitmap, String name) throws IOException {
       String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           try {
               ContentResolver resolver = context.getContentResolver();
               ContentValues contentValues = new ContentValues();
               contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + "_" + timeStamp);
               contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
               contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
               Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

               OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
               bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
               outputStream.flush();
               outputStream.close();

           }catch (Exception e){
               e.printStackTrace();
           }

       }else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

           String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + PATH;
           try {
               File dir = new File(fullPath);
               if (!dir.exists()) {
                   //noinspection ResultOfMethodCallIgnored
                   dir.mkdirs();
               }
           }
           catch(Exception ignored){
           }
           File imageFile = new File(fullPath, name + "_" + timeStamp + ".png");
           FileOutputStream outputStream = new FileOutputStream(imageFile);
           bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
           outputStream.flush();
           outputStream.close();
           context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
       }
   }

}

