package com.teskola.molkky;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public abstract class ImagesActivity extends FirebaseActivity implements ListAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int PERMISSIONS_REQUEST = 100;
    private ImageHandler.OnImageAdded listener;
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
                Bitmap photo = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
                try {
                    ImageHandler.getInstance(ImagesActivity.this).save(ImagesActivity.this, photo, id, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageHandler.getInstance(ImagesActivity.this).upload(photo, id, listener);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void showImageDialog () {
        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.image_change_view, null);
        ImageView imageView = dialogView.findViewById(R.id.dialog_playerIW);

        GlideApp
                .with(this)
                .load(ImageHandler.getInstance(this).getStorageReference(id))
                .signature(new ObjectKey(ImageHandler.getInstance(this).getTimestamp(id)))
                .into(imageView);
        imageDialog.setTitle(name);
        imageDialog.setView(dialogView);
        imageDialog.setNegativeButton(R.string.cancel, null);
        imageDialog.setPositiveButton(R.string.new_picture, (dialogInterface, i) -> takePicture());
        imageDialog.create().show();
    }

    public SharedPreferences getPreferences () {
        return preferences;
    }

    public void setImage (ImageView view, String id, boolean showCamera) {
        if (view == null)
            return;
        if (preferences.getBoolean("SHOW_IMAGES", false)) {
            view.setVisibility(View.VISIBLE);
            if (ImageHandler.getInstance(this).hasImage(id)) {
                GlideApp
                        .with(this)
                        .load(ImageHandler.getInstance(this).getStorageReference(id))
                        .signature(new ObjectKey(ImageHandler.getInstance(this).getTimestamp(id)))
                        .centerCrop()
                        .placeholder(R.color.gray)
                        .into(view);
            }
            else {
                if (showCamera) {
                    view.setScaleType(ImageView.ScaleType.CENTER);
                    view.setImageResource(R.drawable.camera);
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
    public void onImageClicked(PlayerInfo playerInfo, int position, ImageHandler.OnImageAdded listener) {
        id = playerInfo.getId();
        name = playerInfo.getName();
        this.listener = listener;
        if (ImageHandler.getInstance(this).hasImage(id))
            showImageDialog();
        else
            takePicture();
    }

    public void takePicture() {
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