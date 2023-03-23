package com.teskola.molkky;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public abstract class ImagesActivity extends DatabaseActivity implements ListAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private OnImageAdded listener;
    private SharedPreferences preferences;
    private ActivityResultLauncher<Intent> launcher;
    private String playerId, playerName;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        preferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                try {
                    ImageHandler.getInstance(ImagesActivity.this).save(ImagesActivity.this, photo, playerId, playerName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageHandler.getInstance(ImagesActivity.this).upload(photo, playerId);
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

    public void setImage (ImageView view, String id) {
        if (view == null)
            return;
        if (preferences.getBoolean("SHOW_IMAGES", false)) {
            Bitmap photo = ImageHandler.getInstance(this).getPhoto(id);
            if (photo != null) {
                view.setImageBitmap(photo);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                view.setVisibility(View.VISIBLE);
            } else
                view.setVisibility(View.GONE);
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
    public void onImageClicked(String id, String name, int position, OnImageAdded listener) {
        playerId = id;
        playerName = name;
        this.listener = listener;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            launcher.launch(intent);
        }
    }

}