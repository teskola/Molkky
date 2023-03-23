package com.teskola.molkky;

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

public abstract class ImagesActivity extends DatabaseActivity implements ListAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int REQUEST_CODE = 200;
    private String playerId;
    private OnImageAdded listener;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        preferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ImageHandler.getInstance(this).save(this, photo, playerId);
            ImageHandler.getInstance(this).upload(photo, playerId);
            if (listener != null) listener.onSuccess(photo);
        }
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
    public void onImageClicked(String id, int position, OnImageAdded listener) {
        playerId = id;
        this.listener = listener;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }
}