package com.teskola.molkky;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

public abstract class ImagesActivity extends AppCompatActivity implements ListAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int PERMISSIONS_REQUEST = 100;
    private OnImageAdded listener;
    private SharedPreferences preferences;
    private ActivityResultLauncher<Intent> launcher;
    private String id, name;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        preferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                if (onSavedInstanceState != null) {
                    id = onSavedInstanceState.getString("id");
                    name = onSavedInstanceState.getString("name");
                }
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                try {
                    ImageHandler.getInstance(ImagesActivity.this).save(ImagesActivity.this, photo, id, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageHandler.getInstance(ImagesActivity.this).upload(photo, id);
                if (listener != null) listener.onSuccess(photo);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public interface OnImageAdded {
        void onSuccess (Bitmap photo);
    }

    public SharedPreferences getPreferences () {
        return preferences;
    }

    public void setImage (ImageView view, String id, boolean showCamera) {
        if (view == null)
            return;
        if (preferences.getBoolean("SHOW_IMAGES", false)) {
            view.setVisibility(View.VISIBLE);
            Bitmap photo = ImageHandler.getInstance(this).getPhoto(id);
            if (photo != null) {
                view.setImageBitmap(photo);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            else
            {
                if (showCamera) {
                    view.setImageResource(R.drawable.camera);
                    view.setScaleType(ImageView.ScaleType.CENTER);
                }
                else
                    view.setVisibility(View.GONE);
            }
        }
        else
            view.setVisibility(View.GONE);
    }


    @Override
    public void onSelectClicked(int position) {

    }

    @Override
    public void onDeleteClicked(int position) {

    }

    @Override
    public void onImageClicked(PlayerInfo playerInfo, int position, OnImageAdded listener) {
        id = playerInfo.getId();
        name = playerInfo.getName();
        this.listener = listener;
        if (requestPermissions()) return;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launcher.launch(intent);
    }

    /*
    *
    * Request camera permission and write external storage if needed. Returs true if permissions are requested.
    * Returns false if there is no need to ask for permissions.
    *
    * */

    public boolean requestPermissions () {
        ArrayList<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT < 29) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CAMERA);
        }
        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PackageManager.PERMISSION_DENIED)
                    return;
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            launcher.launch(intent);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (id != null && name != null) {
            savedInstanceState.putString("id", id);
            savedInstanceState.putString("name", name);
        }
    }

}